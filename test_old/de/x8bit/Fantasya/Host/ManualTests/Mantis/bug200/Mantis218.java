package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Host.ManualTests.TestSuite;
import de.x8bit.Fantasya.Host.ManualTests.Befehle.TestStirb;

/**
 * 0000218: Einheit überlebt STIRB
 * Ich wollte diese Runde mittels STIRB aufhören.
 * Aber die Einheit, die den Befehl gab, hat das überlebt: 
 * Schreiner (6i7) in Sintang (-1, 1, 1). Das einzige Besondere, 
 * das die Einheit (außer STIRB) gemacht hat, war, mit dem Schiffbau 
 * zu beginnen. Keine Ahnung, ob es da einen Zusammenhang gibt.
 * @author hb
 */
public class Mantis218 extends TestSuite {

    public Mantis218() {
        super(true); // avoid Exception
        this.addTest(new TestStirb());
    }

}
