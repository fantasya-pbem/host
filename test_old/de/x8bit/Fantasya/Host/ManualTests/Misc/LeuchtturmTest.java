package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Leuchtturm;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Aquaner;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Halbling;
import de.x8bit.Fantasya.Atlantis.Units.Zwerg;
import de.x8bit.Fantasya.Host.GameRules;

/**
 *
 * @author hb
 */
public class LeuchtturmTest extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();

		Partei p = tw.getSpieler1();
		p.setName(getName()+"-Partei");

		Region r = null; Region jwd = null; Region gleichNebenan = null;
		for (Region maybe : tw.nurNachbarVon(tw.nurBetretbar(getRegions()), Ozean.class)) {
			Coords c = maybe.getCoords();
			jwd = Region.Load(new Coords(c.getX() - 4, c.getY(), c.getWelt()));
			if (!(jwd instanceof Ozean)) continue;
			gleichNebenan = Region.Load(new Coords(c.getX() - 1, c.getY(), c.getWelt()));
			if (!(gleichNebenan instanceof Ozean)) continue;

			// gotcha!
			r = maybe;
			break;
		}
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");

		r.setName(getName()+"-Region");
        getRegions().remove(r);

		jwd.setName(getName()+"-j.w.d.");
		getRegions().remove(jwd);

		gleichNebenan.setName(getName()+"-Gleichnebenan");
		getRegions().remove(gleichNebenan);

        {
			Building b = Building.Create(Leuchtturm.class.getSimpleName(), r.getCoords());
			b.setSize(10001);
			String turmId = b.getNummerBase36();

			Building b2 = Building.Create(Leuchtturm.class.getSimpleName(), r.getCoords());
			b2.setSize(8);
			String turm2Id = b2.getNummerBase36();

			Building b3 = Building.Create(Leuchtturm.class.getSimpleName(), r.getCoords());
			b3.setSize(100);
			String turm3Id = b3.getNummerBase36();

			Partei mitnutzer = tw.createPartei(Elf.class);
			mitnutzer.setName(getName()+"-Mitnutzer");

			Partei turm2nutzer = tw.createPartei(Zwerg.class);
			turm2nutzer.setName(getName()+"-Kleinleuchter");

			Partei turm3nutzer = tw.createPartei(Halbling.class);
			turm3nutzer.setName(getName()+"-Mittelleuchter");

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.setBeschreibung("Der Fernseher");
			u.setPersonen(1);
			u.setSkill(Wahrnehmung.class, u.getPersonen() * Skill.LerntageFuerTW(11));
			u.Befehle.add("BETRETE GEBÄUDE " + turmId);
			u.Befehle.add("HELFE " + mitnutzer.getNummerBase36());


			u = this.createUnit(mitnutzer, r);
            u.setName(this.getName()+" 02");
			u.setBeschreibung("Der Mitfernseher");
			u.setPersonen(1);
			u.setSkill(Wahrnehmung.class, u.getPersonen() * Skill.LerntageFuerTW(6));
			u.Befehle.add("BETRETE GEBÄUDE " + turmId);
			u.Befehle.add("HELFE " + p.getNummerBase36());

			u = this.createUnit(mitnutzer, r);
            u.setName(this.getName()+" 03");
			u.setBeschreibung("Der untalentierte Mitnutzer");
			u.setPersonen(1);
			u.Befehle.add("BETRETE GEBÄUDE " + turmId);

            // der andere Turm:
			u = this.createUnit(turm2nutzer, r);
            u.setName(this.getName()+" 04");
			u.setBeschreibung("Der Zwerg vom kleinen Leuchtturm");
			u.setPersonen(1);
			u.setSkill(Wahrnehmung.class, u.getPersonen() * Skill.LerntageFuerTW(4));
			u.Befehle.add("BETRETE GEBÄUDE " + turm2Id);
            
            
            // der dritte Turm (100)
			u = this.createUnit(turm3nutzer, r);
            u.setName(this.getName()+" 05");
			u.setBeschreibung("Der Halbling vom mittelgroßen Leuchtturm");
			u.setPersonen(1);
			u.setSkill(Wahrnehmung.class, u.getPersonen() * Skill.LerntageFuerTW(8));
			u.Befehle.add("BETRETE GEBÄUDE " + turm3Id);
            


			Partei fremde = tw.createPartei(Aquaner.class);
			fremde.setName(getName()+"-Fremde");

			u = this.createKapitaen(fremde, jwd, Langboot.class.getSimpleName());
            u.setName(this.getName()+" 10");
			u.Befehle.add("BENENNE SCHIFF Das Langboot");
			u.Befehle.add("NACH no");

			u = this.createKapitaen(fremde, gleichNebenan, Boot.class.getSimpleName());
            u.setName(this.getName()+" 11");
			u.Befehle.add("BENENNE SCHIFF Das Boot");
			u.Befehle.add("NACH so");


            new Info(this.getName() + " Setup in " + r + ".", u);
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"01", "02", "03", "04", "05", "10", "11"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("") && text.contains("")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }
        } // next unit

        return retval;
    }

}
