package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Spells.Erdbeben;
import de.x8bit.Fantasya.Atlantis.Spells.Feuerball;
import de.x8bit.Fantasya.Atlantis.Spells.Feuerwalze;
import de.x8bit.Fantasya.Atlantis.Spells.GuterWind;
import de.x8bit.Fantasya.Atlantis.Spells.HainDerTausendEichen;
import de.x8bit.Fantasya.Atlantis.Spells.KlauenDerTiefe;
import de.x8bit.Fantasya.Atlantis.Spells.KleinesErdbeben;
import de.x8bit.Fantasya.Atlantis.Spells.Luftreise;
import de.x8bit.Fantasya.Atlantis.Spells.MeisterDerPlatten;
import de.x8bit.Fantasya.Atlantis.Spells.MeisterDerResourcen;
import de.x8bit.Fantasya.Atlantis.Spells.MeisterDerSchmiede;
import de.x8bit.Fantasya.Atlantis.Spells.MeisterDerWagen;
import de.x8bit.Fantasya.Atlantis.Spells.MeisterDesSchiffs;
import de.x8bit.Fantasya.Atlantis.Spells.Steinschlag;
import de.x8bit.Fantasya.Atlantis.Spells.Sturm;
import de.x8bit.Fantasya.Atlantis.Spells.Voodoo;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;
import java.util.List;

/**
 * Testet die Zauber, Kampfzauber und auch ZEIGE ZAUBERBUCH
 * 
 * @author hapebe
 */
public class TestZaubern extends TestBase {
	private List<Region> regions;

	@Override
	public void mySetupTest() {
		regions = this.getTestWorld().nurBetretbar(this.getRegions());

		testeNormalZauberClean();
		testeNormalZauberErrors();
		testeKampfzauberBasic();
	}

	/**
	 * <p>Hier sollen alle "Normalzauber" (also via ZAUBERE ...) getestet werden,
	 * wobei die Befehle syntaktisch und inhaltlich fehlerfrei sein sollen.</p>
	 * <p>Ebenfalls wird ZEIGE ... getestet.</p>
	 * <p>Fehler bei der eigentlichen Ausführung (kein Platz, hinderliche Umstände, ...)
	 * sind aber willkommen und ggf. auch hier zu checken!</p>
	 */
	private void testeNormalZauberClean() {

		korrektHainDerTausendEichen();
		standardSetup(new KlauenDerTiefe());
		standardSetup(new Erdbeben());
		standardSetup(new Feuerball());
		standardSetup(new Feuerwalze());
		standardSetup(new GuterWind()); // das sollte fehlschlagen, weil es Einheit 1 anzielt.
		korrektGuterWind();
		standardSetup(new KleinesErdbeben());
		standardSetup(new Luftreise()); // das sollte fehlschlagen, weil Koordinate Y fehlt.
		korrektLuftreise();

		standardSetup(new MeisterDerPlatten());
		korrektMeisterDerPlatten();
		standardSetup(new MeisterDerResourcen());
		korrektMeisterDerResourcen();
		standardSetup(new MeisterDerSchmiede());
		korrektMeisterDerSchmiede();
		standardSetup(new MeisterDerWagen());
		korrektMeisterDerWagen();
		standardSetup(new MeisterDesSchiffs());
		korrektMeisterDesSchiffs();

		korrektSteinschlag();
		standardSetup(new Sturm()); // ist ein Kampfzauber, daher nur "Verwirrungsmeldung" erwartet.
		standardSetup(new Voodoo()); // dieser Zauber ist noch nicht implementiert bzw. auf Stufe 600 - ggf. aussagekräftigere Fehlermeldung?
	}

	/**
	 * hier sollten alle möglichen Fehler bei ZAUBER-Befehlen getestet 
	 * werden. Der Schusseligkeit der Spieler sind aber keine Grenzen 
	 * gesetzt, deswegen wird hier wohl NICHT alles abgedeckt werden.
	 */
	private void testeNormalZauberErrors() {
		Unit mage = standardErrorSetup(new Luftreise());
		mage.Befehle.add("ZAUBERE Luftreise");

		mage = standardErrorSetup(new Luftreise());
		mage.Befehle.add("ZAUBERE Luftreise abcd 12 34 56");
		
		mage = standardErrorSetup(new HainDerTausendEichen());
		mage.Befehle.add("ZAUBERE \"Hain der 1000 Eichen\" -45");

		mage = standardErrorSetup(new HainDerTausendEichen());
		mage.Befehle.add("ZAUBERE \"Hain der 1000 Eichen\" Nordosten");

		mage = standardErrorSetup(new HainDerTausendEichen());
		mage.Befehle.add("ZAUBERE Voodoo");

		mage = standardErrorSetup(new HainDerTausendEichen());
		mage.Befehle.add("ZAUBERE Luftreise 1 1");

		mage = standardErrorSetup(new Luftreise());
		mage.Befehle.add("\"ZAUBERE \"Luftreise\" 1 1");

		mage = standardErrorSetup(new Luftreise());
		mage.Befehle.add("ZAUBERE \"Luftreise\" \"1\" \"1\"");

		mage = standardErrorSetup(new Luftreise());
		mage.Befehle.add("ZAUBERE \"Luftreise 1 1");
		
		mage = standardErrorSetup(new Luftreise());
		mage.Befehle.add("ZAUBERE ' OR 1=1 '\"Luftreise 1 1");
	}


	/**
	 * testet, ob der KAMPFZAUBER-Befehl prinzipiell "durchgeht".
	 */
	private void testeKampfzauberBasic() {
		{
			Unit mage = standardSetup(new Sturm());
			mage.Befehle.clear();
			mage.Befehle.add("KAMPFZAUBER VERWIRRUNG \"Sturm\" 1");
			mage.Befehle.add("LERNE Wahrnehmung");
			mage.setBeschreibung("Erwartet: Lernt Wahrnehmung und setzt Sturm als Kampfzauber.");
		}
		{
			Unit mage = standardSetup(new Sturm());
			mage.Befehle.clear();
			mage.Befehle.add("KAMPFZAUBER ANGRIFF \"Sturm\" 1");
			mage.Befehle.add("LERNE Wahrnehmung");
			mage.setBeschreibung("Erwartet: Lernt Wahrnehmung und eine Fehlermeldung zum Kampfzauber.");
		}
		{
			Unit mage = standardSetup(new Feuerball());
			mage.setName("Feuerballer");
			mage.Befehle.clear();
			mage.Befehle.add("KAMPFZAUBER ANGRIFF \"Feuerball\" 1");
			mage.Befehle.add("LERNE Wahrnehmung");
			mage.setBeschreibung("Erwartet: Lernt Wahrnehmung und setzt Feuerball als Kampfzauber.");
		}
		{
			Unit mage = standardSetup(new Sturm());
			mage.Befehle.clear();
			mage.Befehle.add("KAMPFZAUBER VERTEIDIGUNG \"Sturm\" 1");
			mage.Befehle.add("LERNE Wahrnehmung");
			mage.setBeschreibung("Erwartet: Lernt Wahrnehmung und eine Fehlermeldung zum Kampfzauber.");
		}
		// TODO: Es gibt noch keinen Verteidigungszauber (?)
		/* {
			Unit mage = standardSetup(new ());
			mage.Befehle.clear();
			mage.Befehle.add("KAMPFZAUBER VERTEIDIGUNG \"\" 1");
			mage.Befehle.add("LERNE Wahrnehmung");
			mage.setBeschreibung("Erwartet: Lernt Wahrnehmung und setzt als Kampfzauber.");
		} */
		{
			Unit mage = standardSetup(new Sturm());
			mage.Befehle.clear();
			mage.Befehle.add("KAMPFZAUBER VERWIRRUNG NICHT");
			mage.Befehle.add("LERNE Wahrnehmung");
			mage.setBeschreibung("Erwartet: Lernt Wahrnehmung und es gibt eine Meldung über 'keinen Verwirrungszauber'.");
		}
		{
			Unit mage = standardSetup(new Sturm());
			mage.Befehle.clear();
			mage.Befehle.add("KAMPFZAUBER ANGRIFF NICHT");
			mage.Befehle.add("LERNE Wahrnehmung");
			mage.setBeschreibung("Erwartet: Lernt Wahrnehmung und es gibt eine Meldung über 'keinen Angriffszauber'.");
		}
		{
			Unit mage = standardSetup(new Sturm());
			mage.Befehle.clear();
			mage.Befehle.add("KAMPFZAUBER VERTEIDIGUNG NICHT");
			mage.Befehle.add("LERNE Wahrnehmung");
			mage.setBeschreibung("Erwartet: Lernt Wahrnehmung und es gibt eine Meldung über 'keinen Verteidigungszauber'.");
		}
	}

	private void korrektMeisterDerPlatten() {
		TestWorld testWorld = this.getTestWorld();
		Spell sp = new MeisterDerPlatten();
		Region r = regions.get(0); regions.remove(0);

		Unit freak = this.createSpecialist(testWorld.getSpieler1(), r, "Sattlerei", false);
		freak.setName("Sattler");

		Unit mage = createMage(testWorld.getSpieler1(), r, 6);
		mage.setBeschreibung("Erwartet: " + freak + " produziert mehr als normal.");
		mage.setSpell(sp);
		mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" " + freak.getNummerBase36() + " 1");

		new Info(this.getClass().getSimpleName() + " " + sp.getName() + " (korrekt) Setup in " + r + ".", testWorld.getSpieler1());
	}

	private void korrektMeisterDerWagen() {
		TestWorld testWorld = this.getTestWorld();
		Spell sp = new MeisterDerWagen();
		Region r = regions.get(0); regions.remove(0);

		Unit freak = this.createSpecialist(testWorld.getSpieler1(), r, "Werkstatt", false);
		freak.setName("Wagenbauer");

		Unit mage = createMage(testWorld.getSpieler1(), r, 6);
		mage.setBeschreibung("Erwartet: " + freak + " produziert mehr als normal.");
		mage.setSpell(sp);
		mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" " + freak.getNummerBase36() + " 1");

		new Info(this.getClass().getSimpleName() + " " + sp.getName() + " (korrekt) Setup in " + r + ".", testWorld.getSpieler1());
	}

	private void korrektMeisterDesSchiffs() {
		TestWorld testWorld = this.getTestWorld();
		Spell sp = new MeisterDesSchiffs();
		Region r = testWorld.nurNachbarVon(regions, Ozean.class).get(0);
		regions.remove(r);

		Unit freak = this.createSpecialist(testWorld.getSpieler1(), r, "Schiffswerft", false);
		freak.setName("Schiffbauer");

		Unit mage = createMage(testWorld.getSpieler1(), r, 6);
		mage.setBeschreibung("Erwartet: " + freak + " produziert mehr als normal.");
		mage.setSpell(sp);
		mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" " + freak.getNummerBase36() + " 1");

		new Info(this.getClass().getSimpleName() + " " + sp.getName() + " (korrekt) Setup in " + r + ".", testWorld.getSpieler1());
	}

	private void korrektMeisterDerResourcen() {
		TestWorld testWorld = this.getTestWorld();
		Spell sp = new MeisterDerResourcen();
		Region r = testWorld.nurTerrain(regions, Berge.class).get(0);
		regions.remove(r);

		Unit freak = this.createSpecialist(testWorld.getSpieler1(), r, "Bergwerk", true);
		freak.setName("Bergmann");

		Unit mage = createMage(testWorld.getSpieler1(), r, 6);
		mage.setBeschreibung("Erwartet: Bergmann produziert mehr als normal.");
		mage.setSpell(sp);
		mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" " + freak.getNummerBase36() + " 3");

		new Info(this.getClass().getSimpleName() + " " + sp.getName() + " (korrekt) Setup in " + r + ".", testWorld.getSpieler1());
	}

	private void korrektMeisterDerSchmiede() {
		TestWorld testWorld = this.getTestWorld();
		Spell sp = new MeisterDerSchmiede();
		Region r = regions.get(0); regions.remove(0);
		
		Unit freak = this.createSpecialist(testWorld.getSpieler1(), r, "Schmiede", false);
		freak.setName("Schmied");

		Unit mage = createMage(testWorld.getSpieler1(), r, 6);
		mage.setBeschreibung("Erwartet: Schmied produziert mehr als normal.");
		mage.setSpell(sp);
		mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" " + freak.getNummerBase36() + " 1");

		new Info(this.getClass().getSimpleName() + " " + sp.getName() + " (korrekt) Setup in " + r + ".", testWorld.getSpieler1());
	}

	private void korrektLuftreise() {
		TestWorld testWorld = this.getTestWorld();
		Spell sp = new Luftreise();
		Region r = null;
		Region target = null;
		for (Region temp : regions) {
			Coords startCoords = temp.getCoords();
			Coords targetCoords = new Coords(startCoords.getX() + 3, startCoords.getY(), startCoords.getWelt());
			Region tempTarget = Region.Load(targetCoords);
			if (tempTarget.istBetretbar(null)) {
				// gotcha!
				r = temp;
				target = tempTarget;
				break;
			}
		}
		if (r == null)	throw new RuntimeException("Keine Region für eine Luftreise gefunden - einfach nochmal probieren.");

		regions.remove(r);
		regions.remove(target);

		int dx = target.getCoords().getX() - r.getCoords().getX();
		int dy = target.getCoords().getY() - r.getCoords().getY();

		Unit mage = createMage(testWorld.getSpieler1(), r, 6);
		mage.setBeschreibung("Erwartet: Luftreise von " + r + " aus klappt, Magier landet in " + target + ".");
		mage.setSpell(sp);
		mage.Befehle.add("ZEIGE ZAUBERBUCH");
		mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" " + dx + " " + dy);

		new Info(this.getClass().getSimpleName() + " " + sp.getName() + " (korrekt) Setup in " + r + ".", testWorld.getSpieler1());
	}

	private void korrektGuterWind() {
		TestWorld testWorld = this.getTestWorld();
		{
			Spell sp = new GuterWind();
			Region r = testWorld.nurNachbarVon(regions, Ozean.class).get(0);
			regions.remove(r);

			Unit kapitaen = this.createKapitaen(testWorld.getSpieler1(), r, "Boot");
			// so, Richtung finden:
			Region r1 = null;
			for (Region temp:r.getNachbarn()) {
				if (temp instanceof Ozean) {
					r1 = temp;
					break;
				}
			}
			if (r1 == null)	throw new RuntimeException("Keine Nachbarregion zum Segeln gefunden? Das kann nicht passieren?");
			String richtung = r.getCoords().getRichtungNach( r1.getCoords() ).getShortcut();
			kapitaen.Befehle.add("NACH " + richtung + " " + richtung + " " + richtung + " " + richtung + " " + richtung);

			Unit mage = createMage(testWorld.getSpieler1(), r, 6);
			mage.setBeschreibung("Erwartet: Guter Wind klappt, " + kapitaen + " wird beschleunigt.");
			mage.setSpell(sp);
			mage.Befehle.add("ZEIGE ZAUBERBUCH");
			mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" " + kapitaen.getNummerBase36() + " 2");
			
			new Info(this.getClass().getSimpleName() + " " + sp.getName() + " (korrekt) Setup in " + r + ".", testWorld.getSpieler1());
		}

	}

	private void korrektSteinschlag() {
		TestWorld testWorld = this.getTestWorld();
		{
			Spell sp = new Steinschlag();
			Region r = testWorld.nurTerrain(regions, Berge.class).get(0);
			regions.remove(r);
			Unit mage = createMage(testWorld.getSpieler1(), r, 6);
			mage.setBeschreibung("Erwartet: Der Steinschlag klappt, aber es gibt keine Wirkung.");

			mage.setSpell(sp);
			mage.Befehle.add("ZEIGE ZAUBERBUCH");
			mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" 1");
			new Info(this.getClass().getSimpleName() + " " + sp.getName() + " (korrekt) Setup in " + r + ".", testWorld.getSpieler1());
		}
		{
			Spell sp = new Steinschlag();
			Region r = testWorld.nurTerrain(regions, Ebene.class).get(0);
			regions.remove(r);
			Unit mage = createMage(testWorld.getSpieler1(), r, 6);
			mage.setBeschreibung("Erwartet: Der Steinschlag schlägt fehl: Gelände zu flach.");

			mage.setSpell(sp);
			mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" 1");
			new Info(this.getClass().getSimpleName() + " " + sp.getName() + " (falsches Terrain) Setup in " + r + ".", testWorld.getSpieler1());
		}
	}

	private void korrektHainDerTausendEichen() {
		TestWorld testWorld = this.getTestWorld();
		{
			Region r = testWorld.nurTerrain(regions, Ebene.class).get(0);
			regions.remove(r);
			Unit mage = createMage(testWorld.getSpieler1(), r, 6);

			mage.setBeschreibung("Bäume vorher: " + r.getResource(Holz.class).getAnzahl());
			mage.setSpell(new HainDerTausendEichen());
			mage.Befehle.add("ZEIGE ZAUBERBUCH");
			mage.Befehle.add("ZAUBERE \"Hain der 1000 Eichen\" 1");
		}

		{ // Gletscher - das sollte nicht (ganz) hinhauen:
			Region r = testWorld.nurTerrain(regions, Gletscher.class).get(0);
			regions.remove(r);

			// wir helfen noch ein bisschen nach:
			r.setResource(Holz.class, 10); // damit gibt es gar keine freien Arbeitsplätze mehr.

			Unit mage = createMage(testWorld.getSpieler1(), r, 6);

			mage.setBeschreibung("Bäume vorher: " + r.getResource(Holz.class).getAnzahl());
			mage.setSpell(new HainDerTausendEichen());
			mage.Befehle.add("ZAUBERE \"Hain der 1000 Eichen\" 1");
		}
	}

	protected Unit standardErrorSetup(Spell sp) {
		TestWorld testWorld = this.getTestWorld();
		Region r = regions.get(0); regions.remove(0);
		Unit mage = createMage(testWorld.getSpieler1(), r, 6);
		mage.setName("Fehlermax " + mage.getNummerBase36());

		mage.setSpell(sp);
		new Info(this.getClass().getSimpleName() + " " + sp.getName() + " ERROR Setup in " + r + ".", testWorld.getSpieler1());

		return mage;
	}

	protected Unit standardSetup(Spell sp) {
		TestWorld testWorld = this.getTestWorld();
		Region r = regions.get(0); regions.remove(0);
		Unit mage = createMage(testWorld.getSpieler1(), r, 6);

		mage.setSpell(sp);
		mage.Befehle.add("ZEIGE ZAUBERBUCH");
		mage.Befehle.add("ZAUBERE \"" + sp.getName() + "\" 1");
		new Info(this.getClass().getSimpleName() + " " + sp.getName() + " Setup in " + r + ".", testWorld.getSpieler1());

		return mage;
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }



}
