package de.x8bit.Fantasya.Atlantis.Units;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Items.Greifenei;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.MKlauen;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.MSchnabel;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;

public class Greif extends Monster
{
	public Greif() { }

	@Override
	public int Trefferpunkte() { return 60; }

	@Override
	public boolean Lernen(Class<? extends Skill> skill)	{
		if (skill == Wahrnehmung.class) return allowLernen(skill);	// Greife dürfen Wahrnehmung lernen

		return super.Lernen(skill);
	}



	@Override
	public void planMonster()
	{
        Region region = Region.Load(getCoords());
		if (region instanceof Gletscher) Family(); else Movement();
        
        if (this.getPersonen() == 1) {
            this.setName("Greif");
        } else {
            this.setName("Greife");
        }
        
        // manchmal finden Greife Silber:
        int gefunden = 0;
        for (int i=0; i < getPersonen(); i++) {
            if (Random.W(100) == 100) gefunden += 100;
        }
        if (gefunden > 0) {
            Item silber = getItem(Silber.class);
            silber.setAnzahl(silber.getAnzahl() + gefunden);
        }
	}
	
	/** Wachsen und Lernen - quasi ein glückliche kleine Familie */
	private void Family()
	{
		this.Befehle.clear();
		this.Befehle.add("LERNE Wahrnehmung");
		if (GameRules.isAutumn())
		{
			// Eier legen
			int anzahl = Random.rnd(0, GameRules.Monster.TIER.Greif.Eclosion()) + 1;
			Item eier = getItem(Greifenei.class);
			eier.setAnzahl(eier.getAnzahl() + anzahl);
			new Info(this + " legt " + anzahl + " neue Eier.", this);				
		}
		
		// Abspaltung einer neuen Einheit
		if (getPersonen() > GameRules.Monster.TIER.Greif.FractionStart())
		{
			if (Random.rnd(0, 100) < GameRules.Monster.TIER.Greif.FractionPercent()) Fraction();
		}		
	}
	
	/** auf der Suche nach einer neuen Heimat */
	private void Movement() 
	{
		this.Befehle.clear();
        
//		new SysMsg(" - Bewegung " + this);
		String direction = Richtung.random().getShortcut();
		Region next = Region.Load(getCoords().shift(Richtung.getRichtung(direction)));
		int versuch = 0;
		while(!next.istBetretbar(null) || ((next instanceof Gletscher) && (next.containsRace(Greif.class))))
		{
			direction = Richtung.random().getShortcut();
			next = Region.Load(getCoords().shift(Richtung.getRichtung(direction)));
			if (versuch++ > 10)	{
				new SysMsg(this + " hat keine Richtung zum Bewegen gefunden -> Abgebrochen");
				return;
			}
		}
		Befehle.add("NACH " + direction);
	}
	
	/**
	 * eine Abspaltung zieht automatisch eine Bewegung der neuen Einheit nach sich 
	 */
	private void Fraction()
	{
//		new SysMsg(" - Fraction -> " + this);
		Unit greif = spawnGreif(Region.Load(getCoords())); // einen "Noob" spawnen
		setPersonen(getPersonen() - greif.getPersonen());
		String direction = Richtung.random().getShortcut();
		Region next = Region.Load(getCoords().shift(Richtung.getRichtung(direction)));
		int versuch = 0;
		while(!next.istBetretbar(null))
		{
			if (versuch++ > 10)	{
				new SysMsg(this + " hat keine Richtung zum Bewegen gefunden -> Abgebrochen");
				break;
			}
			direction = Richtung.random().getShortcut();
			next = Region.Load(getCoords().shift(Richtung.getRichtung(direction)));
        }
		greif.Befehle.add("NACH " + direction);
	}

	public static void spawnMonster()
	{
//		new SysMsg("plane Greif");
		for(int insel = 0; insel < GameRules.getInselkennungErzeugung(); insel++)
		{
			List<Region> gletscher = Region.getRegions4Insel(insel, Gletscher.class);
			int count = 0;
			for(Region r : gletscher) for(Unit u : r.getUnits()) if (u instanceof Greif) count++;
			if (count >= GameRules.Monster.TIER.Greif.SpawnMax()) continue;
			if (!gletscher.isEmpty())
			{
				Region spawn = gletscher.get(Random.rnd(0, gletscher.size()));
				count = 0;
				while(spawn.containsRace(Greif.class))
				{
					spawn = gletscher.get(Random.rnd(0, gletscher.size()));
					if (count++ > 10) return; // bereits alles voll mit den Viechern
				}
				new SysMsg(" - Greif: Spawn in Region " + spawn);
				spawnGreif(spawn);
			}
		}
	}

	private static Unit spawnGreif(Region region)
	{
		// Greif erstellenvalue
		Unit greif = Unit.CreateUnit("Greif", Codierung.fromBase36("tier"), region.getCoords());
		greif.setPersonen(Random.rnd(GameRules.Monster.TIER.Greif.PersonsMin(), GameRules.Monster.TIER.Greif.PersonsMax()));
		greif.setSkill(Speerkampf.class, Random.rnd(GameRules.Monster.TIER.Greif.SpeerkampfMin(), GameRules.Monster.TIER.Greif.SpeerkampfMax()));
		greif.setSkill(Hiebwaffen.class, Random.rnd(GameRules.Monster.TIER.Greif.HiebwaffenMin(), GameRules.Monster.TIER.Greif.HiebwaffenMax()));
		greif.setSkill(Wahrnehmung.class, Random.rnd(GameRules.Monster.TIER.Greif.WahrnehmungMin(), GameRules.Monster.TIER.Greif.WahrnehmungMax()));
		greif.setItem(Greifenei.class, Random.rnd(1, greif.getPersonen()));
		greif.setTarnPartei(0);
        if (greif.getPersonen() == 1) {
            greif.setName("Greif");
        } else {
            greif.setName("Greife");
        }
        
		return greif;
	}

	/**
	 * setzt die entsprechenden Greif-Waffen
	 * @param krieger - dieser Krieger wird gesetzt
	 */
	@Override
	public void Factory(Krieger krieger)
	{
		// Waffen sind
		// * Klauen -> Hiebwaffen
		// * Schnabel -> Speerkampf
		if (Random.rnd(0,100) > 80)
		{
			krieger.weapons.add(new MSchnabel(this));
		} else
		{
			krieger.weapons.add(new MKlauen(this));
		}
	}

	@Override
	public int Talentwert(Skill skill)
	{
		int tw = super.Talentwert(skill);
		
		// dadurch lernen die Greifen nicht 'so' schnell
		if (skill.getClass().equals(Wahrnehmung.class))			tw -=  1;	// SKILL
		
		return (tw < 0 ? 0 : tw);
	}

	@Override
	public boolean istSpielerRasse() { return false;	}

	@Override
	public String getRassenName() {
		if (this.getPersonen() == 1) return getRasse();
		return "Greife";
	}

    @Override
    public void meldungenAuswerten() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
	public boolean actionUnterhalt() {
        // wieviele Greife sind hier außer uns?
        int n=0;
        for (Unit u : Region.Load(getCoords()).getUnits()) if (u instanceof Greif) n += u.getPersonen();
        
        if (n > 250) {
            // das sieht schlecht aus:
            if (Random.W(6) < 6) {
                new Info(getPersonen() + " " + this + " sind verhungert.", Region.Load(getCoords()));
                setPersonen(0);
                setLebenspunkte(0);
                setItem(Greifenei.class, 0);
                return true;
            } else {
                setItem(Greifenei.class, 0);
            }
        } else if (n > 60) {
            // das wird eng:
            if (Random.W(6) < 4) {
                new Info(getPersonen() + " " + this + " sind verhungert.", Region.Load(getCoords()));
                setPersonen(0);
                setLebenspunkte(0);
                setItem(Greifenei.class, 0);
                return true;
            }
        }
        
        return false; // nein, wir hungern nicht.
    }
}
