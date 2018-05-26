package de.x8bit.Fantasya.Atlantis.Units;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Sumpf;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;

public class Dragonfly extends Monster {

	public Dragonfly() { }

	@Override
	public int Trefferpunkte() { return 1; }
	
	@Override
	public void planMonster() {
		// nüschts - Libellen liebellen halt einfach :]
        this.setLebenspunkte(0); // ... und sie sind immer gesund!
		
		// die aktuelle region holen
		Region sumpf = Region.Load(this.getCoords());
		
		// gut - sie vermehren sich
		int quartal = GameRules.getQuartal();
		
		// Wachstum der Einheit
		int personen = getPersonen();
		// - Geburten ... aber nur im Sumpf
		if (sumpf instanceof Sumpf)
		{
			int neu = (int) ((float)personen * GameRules.Monster.TIER.DragonFly.Growing()[quartal]);
			personen += Random.rnd(0, neu);
			if (personen > GameRules.Monster.TIER.DragonFly.FractionStart())
			{
				int fraction = personen * (int) (Random.rnd(0, GameRules.Monster.TIER.DragonFly.FractionPercent()) / 100.0f);
				Unit fly = spawn(getCoords());
				fly.setPersonen(fraction);

				// neue Region suchen - nur in der Nachbarregion
				String direction = Richtung.random().getShortcut();
				Region next = Region.Load(getCoords().shift(Richtung.getRichtung(direction)));
				int versuch = 0;
				while(!next.istBetretbar(null) || ((next instanceof Gletscher) && (next.containsRace(Greif.class))))
				{
					direction = Richtung.random().getShortcut();
					next = Region.Load(getCoords().shift(Richtung.getRichtung(direction)));
					if (versuch++ > 10)
					{
						new SysMsg(this + " hat keine Richtung auswählen können -> abgebrochen");
						return;
					}
				}
				BefehleExperimental.clear();
				BefehleExperimental.add(this, "NACH " + direction);

				personen -= fraction;
			}
		}
		// - Sterben der alten Libellen
		int tot = (int) ((float)personen * GameRules.Monster.TIER.DragonFly.Decease()[quartal]);
		personen -= Random.rnd(0, tot);
		
		if (personen < 0) personen = 0;
		setPersonen(personen);
	}

	public static void spawnMonster()
	{
//		new SysMsg("plane DragonFly");
		for(int insel = 0; insel < GameRules.getInselkennungErzeugung(); insel++)
		{
			List<Region> regionen = Region.getRegions4Insel(insel, Sumpf.class);
			for(Region sumpf : regionen)
			{
				if (!sumpf.containsRace(Dragonfly.class)) spawn(sumpf.getCoords());
			}
		}
	}
	
	public static Unit spawn(Coords coords)
	{
		Unit fly = Unit.CreateUnit("Dragonfly", Codierung.fromBase36("tier"), coords);
		fly.setPersonen(Random.rnd(GameRules.Monster.TIER.DragonFly.PersonsMin(), GameRules.Monster.TIER.DragonFly.PersonsMax()));
		fly.setTarnPartei(0);
		fly.setName("Dragonfly");
		return fly;
	}

	@Override
	public boolean istSpielerRasse() { return false;	}

	@Override
	public String getRassenName() {
		if (this.getPersonen() == 1) return "Libelle";
		return "Libellen";
	}

    @Override
    public void meldungenAuswerten() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
