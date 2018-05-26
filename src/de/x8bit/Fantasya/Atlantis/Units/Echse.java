package de.x8bit.Fantasya.Atlantis.Units;

import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.EchsenNews;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Skills.Alchemie;
import de.x8bit.Fantasya.Atlantis.Skills.Armbrustschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Bergbau;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Holzfaellen;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Atlantis.Skills.Kraeuterkunde;
import de.x8bit.Fantasya.Atlantis.Skills.Pferdedressur;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Religion;
import de.x8bit.Fantasya.Atlantis.Skills.Ruestungsbau;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.Atlantis.Skills.Segeln;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Skills.Spionage;
import de.x8bit.Fantasya.Atlantis.Skills.Steinbau;
import de.x8bit.Fantasya.Atlantis.Skills.Steuereintreiben;
import de.x8bit.Fantasya.Atlantis.Skills.Strassenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Taktik;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Unterhaltung;
import de.x8bit.Fantasya.Atlantis.Skills.Waffenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Wagenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Echse extends Unit // SKILL
{
	public Echse()
	{
		RekrutierungsKosten = 150;

		minHunger = 12;
		maxHunger = 28;
	}
	
	@Override
	public void setTarnRasse(String value) { TarnRasse = value; }
	
	@Override
	public int Talentwert(Skill skill)
	{
		int tw = super.Talentwert(skill);
		if (tw == 0) return 0;
		
		if (skill.getClass().equals(Armbrustschiessen.class))	tw +=  1;	// SKILL
		if (skill.getClass().equals(Bogenschiessen.class))		tw +=  2;	// SKILL
		if (skill.getClass().equals(Katapultbedienung.class))	tw +=  2;	// SKILL
		if (skill.getClass().equals(Hiebwaffen.class)) 			tw +=  2;	// SKILL
		if (skill.getClass().equals(Speerkampf.class))			tw +=  1;	// SKILL
		if (skill.getClass().equals(Reiten.class))				tw += -1;	// SKILL
		if (skill.getClass().equals(Taktik.class))				tw +=  1;	// SKILL
		if (skill.getClass().equals(Bergbau.class))				tw +=  1;	// SKILL
		if (skill.getClass().equals(Burgenbau.class))			tw += -2;	// SKILL
		if (skill.getClass().equals(Handel.class))				tw += -3;	// SKILL
		if (skill.getClass().equals(Holzfaellen.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(de.x8bit.Fantasya.Atlantis.Skills.Magie.class))				tw += -1;	// SKILL
		if (skill.getClass().equals(Pferdedressur.class))		tw += -3;	// SKILL
		if (skill.getClass().equals(Ruestungsbau.class))		tw +=  2;	// SKILL
		if (skill.getClass().equals(Schiffbau.class))			tw += -2;	// SKILL
		if (skill.getClass().equals(Segeln.class))				tw += -3;	// SKILL
		if (skill.getClass().equals(Steinbau.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Strassenbau.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Tarnung.class))				tw +=  2;	// SKILL
		if (skill.getClass().equals(Unterhaltung.class))		tw += -3;	// SKILL
		if (skill.getClass().equals(Waffenbau.class))			tw +=  2;	// SKILL
		if (skill.getClass().equals(Wagenbau.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Wahrnehmung.class))			tw += -2;	// SKILL
		if (skill.getClass().equals(Steuereintreiben.class))	tw +=  2;	// SKILL
		if (skill.getClass().equals(Bogenbau.class))			tw +=  1;	// SKILL
		if (skill.getClass().equals(Alchemie.class))			tw +=  2;	// SKILL
		if (skill.getClass().equals(Kraeuterkunde.class))		tw += -2;	// SKILL
		if (skill.getClass().equals(Spionage.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Ausdauer.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Religion.class))			tw +=  1;	// SKILL

		return (tw < 0 ? 0 : tw);
	}

	@Override
	public int Trefferpunkte() { return 40; }

	/**
	 * Echsen fressen auch Bauern zum überleben
	 */
	@Override
	public boolean actionUnterhalt()
	{
		// das Silber nicht vergessen
		boolean hunger = super.actionUnterhalt();
		
		// dann erst die Bauern
		Region region = Region.Load(getCoords());
		int bauern = region.getBauern();
		int needed = ((getPersonen() - 1) / 10) + 1; // 9E -> 1B ... 10E -> 1B ... 11E -> 2B
		
		if (bauern >= needed) {
			region.setBauern(bauern - needed);
			if (bauern > needed) {
				new EchsenNews(this + " frisst " + needed + " Bauern in " + region + ".", this);
			} else {
				new EchsenNews(this + " hat alle " + needed + " anwesenden Bauern in " + region + " gefressen.", this);
			}
		} else {
			boolean log = ZATMode.CurrentMode().isDebug();
			// abschalten?
			// log = false;
			
			hunger = true;
			needed -= bauern;
			
			int hungrige = needed * 10; // in "needed" sind jetzt soviele Personen die Hungern
			if (hungrige > getPersonen()) hungrige = getPersonen();
			
			
			// Die Folgen des Hungers simulieren:
			List<Person> einzelne = new ArrayList<Person>();
			for (int i=0; i<this.getPersonen(); i++) {
				einzelne.add(new Person()); 
				einzelne.get(i).setMaxLebenspunkte(this.maxLebenspunkte() / this.getPersonen());
			}

			// die vorhandenen "Lebenspunkte" zufällig aufteilen:
			for (int i=0; i<getLebenspunkte(); i++) {
				einzelne.get(Random.rnd(0, einzelne.size())).addLebenspunkte(1);
			}

			// und die Hungerpunkte ebenfalls zufällig verteilen:
			for (int i=0; i<hungrige; i++) {
				int hungerpunkte = Hungerpunkte();
				for (int j=0; j<hungerpunkte; j++) {
					einzelne.get(Random.rnd(0, einzelne.size())).addLebenspunkte(1);
				}
			}

			if (log) {
				new Debug("Echsen-Lechzen: " + hungrige + " von " + getPersonen() + " Echsen darben ohne Bauernblut.");
				new Debug("LP-Status nach dem großen kleinen Bauernhunger:");
				Collections.sort(einzelne, new PersonenLebenspunktComparator());
				for (Person p : einzelne) {
					int lp = p.getLebenspunkte();
					int mx = p.getMaxLebenspunkte();
					new Debug("\t" + lp + "/" + mx + (lp >= mx?"!":""));
				}
			}

			int opfer = 0;
			int gesamtSchaden = 0;
			for (Person p : einzelne) {
				int lp = p.getLebenspunkte();
				int mx = p.getMaxLebenspunkte();
				if (lp >= mx) {
					opfer ++;
				} else {
					gesamtSchaden += lp;
				}
			}

			setLebenspunkte(gesamtSchaden);

		
			StringBuilder msg = new StringBuilder();
			if (opfer > 0) {
				if (this.getPersonen() == 1) {
					msg.append(this + " verhungert in " + region + ", weil keine Bauern da sind");
				} else {
					if (opfer == this.getPersonen()) {
						msg.append("Alle von " + this + " verhungern aus Bauernmangel in " + region);
					} else {
						msg.append(opfer + " von " + this + " verhungern aus Bauernmangel in " + region);
					}
				}
				setPersonen(getPersonen() - opfer);
			} else {
				msg.append(this + " hungert aus Bauernmangel in " + region);
			}

			if (region.getBauern() > 0) {
				msg.append(" und hat die Region leer gefressen.");
				region.setBauern(0); // leer gefressen
			} else {
				msg.append(".");
			}
			
			if (getPersonen() > 0) {
				new EchsenNews(msg.toString(), this);
			} else {
				new EchsenNews(msg.toString(), Partei.getPartei(this.getOwner()));
			}
		}
		
		return hunger;
	}

	@Override
	public boolean istSpielerRasse() { return true;	}

	@Override
	public String getRassenName() {
		if (this.getPersonen() == 1) return getRasse();
		return "Echsen";
	}
}
