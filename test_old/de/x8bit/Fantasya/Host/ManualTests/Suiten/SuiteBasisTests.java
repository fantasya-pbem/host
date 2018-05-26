package de.x8bit.Fantasya.Host.ManualTests.Suiten;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Welt;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastLoader;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastSaver;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestSuite;
import de.x8bit.Fantasya.Host.ManualTests.Befehle.*;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100.Mantis198;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200.Mantis219;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200.Mantis251;
import de.x8bit.Fantasya.Host.ManualTests.Befehle.TestGibEinheit;
import de.x8bit.Fantasya.Host.ManualTests.Kampf.AttackiereParteiTest;
import de.x8bit.Fantasya.Host.ManualTests.Kampf.ParteiTarnungKampf;
import de.x8bit.Fantasya.Host.ManualTests.Misc.KriegselefantenVerhungern;
import de.x8bit.Fantasya.Host.ManualTests.Misc.TestEchsenHunger;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;
import java.sql.SQLException;

/**
 *
 * @author hb
 */
public class SuiteBasisTests extends TestSuite {
    
    public SuiteBasisTests() {
        super(true); // avoid Exception

		// Es wird viel Platz gebraucht:
		if (GameRules.getRunde() <= 1) {
			for (int i=0; i<10; i++) {
				Welt.NeueRegionen(1);
			}
			
			// Unterwelt:
//			String methode = GameRules.GetOption(GameRules.NEUE_INSEL_METHODE);
//			String unterweltAktiv = GameRules.GetOption(GameRules.UNTERWELT_AKTIV);
			GameRules.SetOption(GameRules.NEUE_INSEL_METHODE, "April2011");
			GameRules.SetOption(GameRules.UNTERWELT_AKTIV, "1");
			// Welt.NeueRegionen(-1);
			// Konfiguration wiederherstellen:
			// GameRules.SetOption(GameRules.NEUE_INSEL_METHODE, methode);
			// GameRules.SetOption(GameRules.UNTERWELT_AKTIV, unterweltAktiv);
			GameRules.Save();
			
			
			try {
				EVAFastSaver.saveAll(false);
			} catch (SQLException ex) {
				new BigError(ex);
			}
		}
		try {
			EVAFastLoader.loadAll();
		} catch (SQLException ex) {
			new BigError(ex);
		}
		TestWorld.RegionsVorrat = null; // Neu-laden erzwingen

        // der hohen Regionsabhängigkeit wegen zuerst:
        this.addTest(new SuiteBewegungen());

		// kleine Bewegung / Regions-Abhängigkeit
		this.addTest(new HandelTest());
        this.addTest(new UnterhaltungsTest());
		this.addTest(new TestTreiben());
		this.addTest(new TestBetragTreiben());
		this.addTest(new TestSteuertuerme());
        this.addTest(new MacheStrasse());
		this.addTest(new TestRekrutieren());
		this.addTest(new RekrutiereMitBewachen());
        this.addTest(new TestZerstoeren());

		// TODO ADRESSE und PASSWORT entfernen, wieder rein: this.addTest(new ParteiAngaben());
        this.addTest(new ZweiLangeBefehle());
        this.addTest(new MacheTemp()); // GIB TEMP Item, Personen
		this.addTest(new BetretenKommandoVerlassen());
		this.addTest(new GibBauern());
        this.addTest(new Mantis251()); // GIB BAUERN PERSONEN, GIB BAUERN EINHEIT
		this.addTest(new GibNull());
		this.addTest(new LiefereBauern());
		this.addTest(new LiefereNull());
		this.addTest(new Mantis198()); // DEFAULT
		this.addTest(new Faulenzen());
		this.addTest(new Diebstahl());
		this.addTest(new TestGibZauber()); // GIB ZAUBER, KONTAKTIERE, einfaches LERNE
		this.addTest(new Bewache());
        this.addTest(new Mantis219()); // HELFE KONTAKTIERE, GIB
		this.addTest(new LehrenLernen());

		this.addTest(new BasisKampf());
        this.addTest(new AttackiereParteiTest());
        this.addTest(new ParteiTarnungKampf());

		this.addTest(new TestNummer());
		this.addTest(new TestHelfe()); // HELFE, STIRB und Allianz-Persistenz
        this.addTest(new SortierTest());
        this.addTest(new TarnenTest());
        this.addTest(new PraefixTest());
        this.addTest(new MachePferd());
        this.addTest(new TestBelagerung());
		this.addTest(new TestKommentare());
		this.addTest(new TestAlwaysModifier());
		this.addTest(new TestGibEinheit());
		this.addTest(new TestGibPersonen());
		this.addTest(new TestZeigeZauberbuch());
		this.addTest(new TestStirb());
        this.addTest(new TestBotschaften());
		this.addTest(new TestBestaetigt());
		this.addTest(new TestVergessen());
		this.addTest(new KriegselefantenVerhungern());
		this.addTest(new TestEchsenHunger());
        this.addTest(new KampfzauberSetzen());
    }



}
