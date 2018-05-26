package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug300;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Greif;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 * 
 * C&P -> http://www.fantasya-pbem.de/mantis/view.php?id=363
 * 
 * Eine Person, Reiten 3, Ein Greif. Der NR2 meint XXX kann nicht reisen, es
 * mangelt an Talent um die Tiere zu führen. Sinngemäß hat auch der CR die
 * 'Überlastmeldung', d.h. den Bug, dass rieisige negative Kapazität angezeigt
 * wird. Im Forum (und wenn ich den Code richtig lese) wird gesagt Reiten 3
 * reicht für einen Greif. Ist das nur eine falsche Meldung oder kann ich
 * tatsächlich nicht reiten/fliegen?
 * 
 * @author mogel
 * 
 */
public class Mantis363 extends TestBase {

	@Override
	protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Partei");
		
		Region r = tw.nurBetretbar(getRegions()).get(0);
		
        Unit u = this.createUnit(p, r);
        u.setKampfposition(Kampfposition.Vorne);
        u.setName(this.getName()+" 01");
        u.setPersonen(1);
        u.setItem(Greif.class, 1);
        u.setSkill(Reiten.class, Skill.LerntageFuerTW(6) * u.getPersonen());
        
        u.Befehle.add("nach no");
	}

	@Override
	protected boolean verifyTest() {
		// TODO Auto-generated method stub
		return false;
	}

}
