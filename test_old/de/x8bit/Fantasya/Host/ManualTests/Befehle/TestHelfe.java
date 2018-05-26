package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Holzfaellen;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Halbling;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Atlantis.Units.Ork;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;
import de.x8bit.Fantasya.util.Codierung;

/**
 *
 * @author hb
 */
public class TestHelfe extends TestBase {

	private final static String SELTSAME_PARTEI = "t8qn";

    @Override
    protected void mySetupTest() {
		TestWorld tw = this.getTestWorld();

        Partei a = tw.createPartei(Mensch.class);
		a.setName(getName() + "-A");

        Partei b = tw.createPartei(Ork.class);
		b.setName(getName() + "-B");

        Partei c = tw.createPartei(Elf.class);
		c.setName(getName() + "-C");
        
        Partei d = tw.createPartei(Halbling.class);
		d.setName(getName() + "-D");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
		r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(a, r);
            u.setName(this.getName()+" 01 " + b.getNummerBase36() + " " + c.getNummerBase36());
			u.setBeschreibung("Setzt Allianzen mit " + b + " und " + c + ", die Allianz mit " + c + " soll gelöscht werden, weil diese Partei stirbt.");
			u.Befehle.add("HELFE " + b.getNummerBase36() + " ALLES");
			u.Befehle.add("HELFE " + c.getNummerBase36() + " ALLES");

            u = this.createUnit(b, r);
            u.setName(this.getName()+" 02 " + a.getNummerBase36());
			u.setBeschreibung("Setzt Allianz mit " + a + ".");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " KAEMPFE");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " GIB");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " RESOURCEN");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " TREIBEN");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " HANDEL NICHT");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " UNTERHALTE NICHT");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " KONTAKTIERE");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " STEUERN");
			u.Befehle.add("HELFE " + SELTSAME_PARTEI + " ALLES");

            u = this.createUnit(c, r);
            u.setName(this.getName()+" 03 " + a.getNummerBase36() + " " + b.getNummerBase36());
			u.setBeschreibung("Setzt Allianzen mit " + a + " und " + b + "; und löscht danach die eigene Partei mit STIRB.");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " KAEMPFE");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " GIB");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " RESOURCEN");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " TREIBEN");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " HANDEL NICHT");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " UNTERHALTE");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " KONTAKTIERE");
			u.Befehle.add("HELFE " + a.getNummerBase36() + " STEUERN");

			u.Befehle.add("HELFE " + b.getNummerBase36() + " ALLES");

			u.Befehle.add("STIRB \"" + c.getPassword() + "\"");
            
            u = this.createUnit(d, r);
            u.setName(this.getName()+" 04 " + a.getNummerBase36());
			u.setBeschreibung("Setzt Allianz mit " + a + ", Option RESSOURCEN (Mantis #360).");
            u.setPersonen(10);
            u.setItem(Schwert.class, u.getPersonen());
            u.setSkill(Hiebwaffen.class, Skill.LerntageFuerTW(5) * u.getPersonen());
            u.setBewacht(true);
            u.Befehle.add("HELFE " + a.getNummerBase36() + " RESSOURCEN");


            // funktioniert HELFE RESSOURCEN ?
            r.setResource(Holz.class, 100);
            u = this.createUnit(a, r);
            u.setName(this.getName()+" 11");
			u.setBeschreibung("Versucht, Holz zu fällen. Das sollte hinhauen, obwohl " + d + " bewacht, wenn deren HELFE RESSOURCEN Befehl funktioniert.");
            u.setSkill(Holzfaellen.class, Skill.LerntageFuerTW(10) * u.getPersonen());
			u.Befehle.add("MACHE Holz");

            new Info(this.getName() + " Setup in " + r + ".", u);
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();
//        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "04", "11"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            Partei partei = Partei.getPartei(u.getOwner());
			String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
				Partei b = Partei.getPartei(Codierung.fromBase36(tokens[2]));
				Partei c = Partei.getPartei(Codierung.fromBase36(tokens[3]));

				if (c != null) {
					retval = fail(tokens[1] + ": Partei C ist noch vorhanden - STIRB nicht korrekt ausgeführt.");
				} else {
					for (AllianzOption ao : AllianzOption.values()) {
						if (ao == AllianzOption.Alles) continue;
						if (partei.hatAllianz(Codierung.fromBase36(tokens[3]), ao)) {
							retval = fail(tokens[1] + ": A hat eine '" + ao.name() + "'-Allianz mit C (Partei, die es nicht mehr gibt!).");
						}
					}
				}

				if (b == null) {
					retval = fail(tokens[1] + ": Partei B ist nicht vorhanden.");
				} else {
					for (AllianzOption ao : AllianzOption.values()) {
						if (ao == AllianzOption.Alles) continue;
						if (!partei.hatAllianz(b.getNummer(), ao)) {
							retval = fail(tokens[1] + ": A hat keine '" + ao.name() + "'-Allianz mit B.");
						}
					}
				}

                /*
				messages = Message.Retrieve(Partei.Load(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getMessage().toLowerCase();
                    if (text.contains("") && text.contains("")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben."); */
            }

            // unit 02
            if (tokens[1].equals("02")) {
				Partei a = Partei.getPartei(Codierung.fromBase36(tokens[2]));

				if (a == null) {
					retval = fail(tokens[1] + ": Partei A ist nicht vorhanden.");
				} else {
					for (AllianzOption ao : AllianzOption.values()) {
						if (ao == AllianzOption.Alles) continue;

						if ((ao == AllianzOption.Handel) || (ao == AllianzOption.Unterhalte)) {
							if (partei.hatAllianz(a.getNummer(), ao)) {
								retval = fail(tokens[1] + ": B hat eine '" + ao.name() + "'-Allianz mit A.");
							}
						} else {
							if (!partei.hatAllianz(a.getNummer(), ao)) {
								retval = fail(tokens[1] + ": B hat keine '" + ao.name() + "'-Allianz mit A.");
							}
						}
					}
				}

				for (AllianzOption ao : AllianzOption.values()) {
					if (ao == AllianzOption.Alles) continue;
					if (partei.hatAllianz(Codierung.fromBase36(SELTSAME_PARTEI), ao)) {
						retval = fail(tokens[1] + ": B hat eine '" + ao.name() + "'-Allianz mit " + SELTSAME_PARTEI + " (welche nie existiert haben).");
					}
				}
            }

            if (tokens[1].equals("03")) {
				retval = fail(tokens[1] + ": Einheit ist noch da; STIRB nicht ausgeführt.");
			}

            if (tokens[1].equals("11")) {
                if (u.getItem(Holz.class).getAnzahl() <= 0) {
                    retval = fail(tokens[1] + ": Einheit hat kein Holz fällen können - der HELFE-RESSOURCEN-Befehl von Partei D hat nicht funktioniert.");
                }
            }

        } // next unit

        return retval;
    }

}
