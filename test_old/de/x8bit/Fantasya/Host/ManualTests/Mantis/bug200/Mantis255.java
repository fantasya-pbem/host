package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import java.util.Collection;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Atlantis.Units.Aquaner;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;
import de.x8bit.Fantasya.util.Codierung;

/**
 * 0000255: Volle Schiffe und Gebaeude haben keinen Eigentümer
 * Offennbar haben Schiffe und Gebäude deren ursprünglicher Eigentümer 'stirbt'
 * (also verschwindet) keine nachfolgenden Eigentümer. Das Gebäude/Schiff ist
 * bevölkert aber ohne Eigner.
 * Wie wird man also Eigentümer? (d.h. Wer steuert das Schiff, wer benennt die
 * Region um...)
 * 
 * @author hb
 */
public class Mantis255 extends TestBase {

	@Override
	protected void mySetupTest() {
		TestWorld tw = this.getTestWorld();

		Partei alt = tw.createPartei(Aquaner.class);
		alt.setName(getName() + "-Verschwundene");

		Partei neu = tw.createPartei(Aquaner.class);
		neu.setName(getName() + "-Erben");

		Region r = tw.nurTerrain(getRegions(), Ozean.class).get(0);
		getRegions().remove(r);

		{
			Unit kap = this.createKapitaen(alt, r, Boot.class.getSimpleName());
			kap.setName(this.getName() + " 01");
			kap.Befehle.add("STIRB \"" + alt.getPassword() + "\"");
			kap.setItem(Schwert.class, 1);
			Ship ship = Ship.Load(kap.getSchiff());

			Unit u = createUnit(neu, r);
			u.setName(getName() + " 02");
			u.setSortierung(50);
			u.setSchiff(ship.getNummer());
			u.Befehle.add("NACH w");

			kap.Befehle.add("KONTAKTIERE " + u.getNummerBase36());

			u = createUnit(neu, r);
			u.setName(getName() + " 03");
			u.setSortierung(100);
			u.setSchiff(ship.getNummer());

			new Info(this.getName() + "-Schiff Setup in " + r + ".", u);
		}

		r = tw.nurBetretbar(getRegions()).get(0);
		{
			Building b = Building.Create("Burg", r.getCoords());
			b.setSize(10);

			Unit altmeister = this.createUnit(alt, r);
			altmeister.setName(getName() + " 11");
			b.Enter(altmeister);

			Unit u = createUnit(neu, r);
			u.setName(getName() + " 12");
			b.Enter(u);

			new Info(this.getName() + "-Burg Setup in " + r + ".", u);
		}
	}

	@Override
	protected boolean verifyTest() {
		boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
		List<Message> messages = null;

		if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) {
			throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");
		}

		if (GameRules.getRunde() == 3) {
			new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
			return retval;
		}

		new TestMsg("Verifiziere " + this.getName() + "...");

		String missing = this.verifyExpectedUnits(units, new String[]{"02", "03", "12"});
		if (missing != null) {
			retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
		}

		for (Unit u : units) {
			// "fremde" Einheiten überspringen.
			if (!u.getName().startsWith(this.getName())) {
				continue;
			}

			String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

			// unit 02
			if (tokens[1].equals("02")) {
				messages = Message.Retrieve(null, u.getCoords(), u);
				boolean found = false;
				for (Message msg : messages) {
					String text = msg.getText().toLowerCase();
					if (text.contains("erbt") && text.contains("mantis255 01")) {
						found = true;
					}
				}

				if (!found) {
					retval = fail(uRef + "Meldung über das Erbe fehlt.");
				}

				if (u.getSchiff() == 0) {
					retval = fail(uRef + "Hat nicht das Kommando über das Schiff bekommen.");
				}

				for (Unit maybe : Unit.CACHE) {
					if (maybe.getName().equals("Mantis255 01")) {
						retval = fail(uRef + "Einheit 01 ist trotz STIRB nicht aufgelöst worden.");
						break;
					}
				}
			}
		} // next unit

		return retval;
	}
}
