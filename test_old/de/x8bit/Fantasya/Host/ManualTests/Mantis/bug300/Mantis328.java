package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug300;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 * 0000328: BOTSCHAFTen entlarven die Parteitarnung des Absenders
 * "Wald Asopo (-6,9,1):
 * 
 * Herolde von Bund der Zeitlosen [4] verkünden: 'An alle: Gruesse von 
 * Jack Sparrow ! Bitte seid so freundlich und uebergebt uns Euer Silber, 
 * Eure Waffen und Ruestungen! und zwar flott!'"
 * 
 * @author hb
 */
public class Mantis328 extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-echte-Absender");

		Partei p2 = tw.createPartei(Mensch.class);
		p2.setName(getName()+"-gefälschte-Absender");

		Partei p3 = tw.createPartei(Mensch.class);
		p3.setName(getName()+"-Empfänger");

        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+"-Region");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.Befehle.add("LERNE Wahrnehmung");
            
            u = this.createUnit(p2, r);
            u.setName(this.getName()+" 02");
            u.Befehle.add("LERNE Wahrnehmung");
            
            Unit empfaenger = this.createUnit(p3, r);
            empfaenger.setName(this.getName()+" 03");
            empfaenger.Befehle.add("LERNE Wahrnehmung");
            
            u = this.createUnit(p, r);
            u.setName(this.getName()+" 11 hinterlistiger Absender");
            u.Befehle.add("TARNE PARTEI " + p2.getNummerBase36());
            u.Befehle.add("LERNE Wahrnehmung");
            u.Befehle.add("BOTSCHAFT EINHEIT " + empfaenger.getNummerBase36() + " Ihr von " + p3.getName() + " seid ja alle doof!");
            u.Befehle.add("BOTSCHAFT PARTEI " + p3.getNummerBase36() + " Niemand hier kann euren Gestank aushalten.");
            u.Befehle.add("BOTSCHAFT REGION Die Perversen von " + p3.getName() + " schlafen allesamt mit ihren Eltern oder Kindern!");

            new Info(this.getName() + " Setup in " + r + ".", r);
        }
    }

    @Override
    protected boolean verifyTest() { throw new UnsupportedOperationException();  }

}
