package de.x8bit.Fantasya.Host.ManualTests;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Bergbau;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.lang.NonsenseTexter;
import java.util.List;

/**
 *
 * @author hapebe
 */
public class LoadTest1 extends TestBase {

	private List<Region> regions;

	@Override
	protected void mySetupTest() {
		Map<Integer, Partei> parteien = new HashMap<Integer, Partei>();
		for (int i = 0 ; i < 40; i++) {
			Partei p = testWorld.createPartei(Mensch.class);
			parteien.put(p.getNummer(), p);
		}

		regions = testWorld.nurBetretbar(getRegions());
		Collections.shuffle(regions);

		for (int nummer: parteien.keySet()) {
			Partei p = parteien.get(nummer);

			Region r = regions.get(0); regions.remove(0);
			createHome(p, r);

		}

		this.getTestWorld().setContinueWithZAT(false);
	}

	private void createHome(Partei p, Region r) {
		int anzahlUnits = Random.rnd(1, 10) * Random.rnd(10, 100);

		new SysMsg("createHome für Partei " + p.getNummerBase36() + "(" + anzahlUnits + " Einheiten)");

		for (int i = 0; i < anzahlUnits; i++) {
			Unit u = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			u.setPersonen(Random.rnd(1, 100));
			u.setItem(Silber.class, u.getPersonen() * 11);
			Item item = (Item) Paket.getPaket("Items").get(Random.rnd(0, Paket.getPaket("Items").size())).Klasse;
			u.setItem(item.getClass(), 2);
			u.Befehle.add("GIB " + Codierung.toBase36(u.getNummer() + 1) + " 1 " + item.getName());
			u.setSkill(Bergbau.class, u.getPersonen() * 180);
			u.setBeschreibung(NonsenseTexter.makeNonsenseWort(50));
			u.Befehle.add("GIB BAUERN 1 PERSON");
			u.Befehle.add("GIB abc 1 Schwert");
			u.Befehle.add("GIB " + Codierung.toBase36(u.getNummer() - 10) + " 10 Silber");
			u.Befehle.add("LERNE Wahrnehmung");
			u.Befehle.add("BESCHREIBE EINHEIT \"" + NonsenseTexter.makeNonsenseWort(50) + "\"");
		}
	}

//    private String makeNonSense(int length)	{
//		String vokale = "aeiou";
//		String kons = "bcdfghjklmnprstvwz";
//
//		boolean Space = false;		// true wenn ein " " gesetzt werden soll
//
//		StringBuffer retval = new StringBuffer("");
//
//		int i;
//
//		int maxsize = Random.rnd(length / 2, length * 2);
//		int lastChar = Random.rnd(0, 2);	// Anfang mit Vokal(0) oder Konsonant (1)
//
//		for(i = 0; i < maxsize; i++) {
//			// kleine Chanche für " "
//			Space = false;
//			if ((Random.rnd(0,255) % 128) <= 16) Space = true;
//
//			// ... aber nicht am Anfang und Ende ...
//			if ((i < 3) || (i == maxsize-1)) Space = false;
//
//			// ... und nicht wenn gerade erst eines war
//			if (i > 0) if ((retval.charAt(i - 1) == '\'') || (retval.charAt(i - 1) == ' ')) Space = false;
//
//			if (!Space)	{
//				// dann setze einen Buchstaben ein
//				char buchstabe;
//				if (lastChar != 0) {
//					// neuer Vokal
//					buchstabe = vokale.charAt(Random.rnd(0, vokale.length()));
//				} else {
//					// neuer Konsonant
//					buchstabe = kons.charAt(Random.rnd(0, kons.length()));
//				}
//
//				// erste Buchstabe immer groß!
//				if (i == 0)	{
//					String s = new StringBuilder().append(buchstabe).toString();
//					s = s.toUpperCase();
//					buchstabe = s.charAt(0);
//				} else {
//					if (retval.charAt(i - 1) == ' ') { // nach Leerzeichen manchmal Punkt und groß
//						String s = new StringBuilder().append(buchstabe).toString();
//                        if (Random.rnd(1, 7) < 2) {
//                            retval.setCharAt(i - 1, '.');
//                            retval.append(" ");
//                            s = s.toUpperCase();
//                            buchstabe = s.charAt(0);
//                        }
//					}
//				}
//
//				// hinzufügen
//				retval.append(buchstabe);
//
//				// Vokale und Konsonanten immer abwechselnd
//				lastChar++;
//				lastChar %= 2;
//			} else {
//				retval.append(' ');
//			}
//		}
//
//		return(retval.toString());
//	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
