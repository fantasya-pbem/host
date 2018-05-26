package de.x8bit.Fantasya.Host.ManualTests.Suiten;

import de.x8bit.Fantasya.Host.ManualTests.TestSuite;
import de.x8bit.Fantasya.Host.ManualTests.Befehle.Nach;
import de.x8bit.Fantasya.Host.ManualTests.Befehle.NachXYLand;
import de.x8bit.Fantasya.Host.ManualTests.Befehle.NachXYMeer;
import de.x8bit.Fantasya.Host.ManualTests.Befehle.Route;
import de.x8bit.Fantasya.Host.ManualTests.Befehle.Schifffahrt;
import de.x8bit.Fantasya.Host.ManualTests.Befehle.TestFolgeBefehl;
import de.x8bit.Fantasya.Host.ManualTests.Misc.DurchreiseTest;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100.Mantis138;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100.Mantis177;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100.Mantis198;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200.Mantis255;
import de.x8bit.Fantasya.Host.ManualTests.Misc.BewegungMitLasten;
import de.x8bit.Fantasya.Host.ManualTests.Misc.LangbootUndSteg;
import de.x8bit.Fantasya.Host.ManualTests.Misc.TrollRitt;
import de.x8bit.Fantasya.Host.ManualTests.Misc.TrollZiehtWagen;

/**
 * @author hb
 */
public class SuiteBewegungen extends TestSuite {

    public SuiteBewegungen() {
        super(true); // avoid Exception

		// Bewegungs-Test u.a. Regions-abhängige: zuerst, damit noch Regionen "vorhanden" sind
		this.addTest(new DurchreiseTest()); // NACH (Land und Schiff), Durchreise
		this.addTest(new TestFolgeBefehl()); // FOLGE zu Land und zur See
		this.addTest(new Mantis138()); // NACH (Schiff), VERLASSE (Schiff)
        this.addTest(new Schifffahrt());
		this.addTest(new NachXYLand());
		this.addTest(new NachXYMeer());
        this.addTest(new Nach());
		this.addTest(new Route());
		this.addTest(new Mantis177());
        this.addTest(new Mantis198());
		this.addTest(new LangbootUndSteg());
		this.addTest(new BewegungMitLasten());
        this.addTest(new TrollZiehtWagen());
        this.addTest(new TrollRitt());
		this.addTest(new Mantis255()); // Kommandoübergabe, wenn Kapitän aufgelöst wird.
    }

}
