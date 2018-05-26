package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Unterhaltung;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Goblin;
import de.x8bit.Fantasya.Atlantis.Units.Kobold;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.List;

/**
 *
 * @author hb
 */
public class CasualDARKTest extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei p = this.getTestWorld().getSpieler1();
		Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
		getRegions().remove(r);

		Unit u = this.createUnit(p, r);
		u.setName(this.getName() + " 01");
		u.setSkill(Wahrnehmung.class, Skill.LerntageFuerTW(12) * u.getPersonen());

		List<Region> regs = this.getTestWorld().nurBetretbar(getRegions());
		for (int i = 0; i < 100; i++) {
			if (i >= regs.size()) {
				break;
			}
			u = this.createUnit(p, regs.get(i));
			u.setItem(Silber.class, 5000);
			u.setSkill(Unterhaltung.class, 180);
			if ((i % 10) == 0) {
				u.setSkill(Wahrnehmung.class, Skill.LerntageFuerTW(4) * u.getPersonen());
			}
			u.setName(this.getName() + " 02 Opfer " + i);
			u.Befehle.add("UNTERHALTE");
		}


		GameRules.setInselkennungSpieler(9);
		GameRules.Save();

		int goblinCnt = 0;
		int koboldCnt = 0;
		for (int i = 0; i < 1000; i++) {
			Goblin.NeueRunde();
			Kobold.NeueRunde();
			goblinCnt = 0;
			koboldCnt = 0;

			for (Unit maybe : Unit.CACHE) {
				if (maybe.getRasse().equals("Goblin")) {
					new TestMsg("Goblin in " + Region.Load(maybe.getCoords()) + " belebt: " + maybe);
					goblinCnt++;
				}
				if (maybe.getRasse().equals("Kobold")) {
					new TestMsg("Kobold in " + Region.Load(maybe.getCoords()) + " belebt: " + maybe);
					koboldCnt++;
				}
			}
			if ((goblinCnt > 2) && (koboldCnt > 0)) {
				break;
			}
		}
		if ((goblinCnt == 0) || (koboldCnt == 0)) {
			new BigError("Nanu? Trotz 1000 Versuchen keine Goblins (" + goblinCnt + ") oder Kobolde (" + koboldCnt + ") erzeugt?");
		}

		// Die Krönung: Einen künstlichen Goblin in der Region von 01 erzeugen:
		Partei dark = Partei.getPartei(Codierung.fromBase36("dark"));
		Unit goblin = Unit.CreateUnit("Goblin", dark.getNummer(), r.getCoords());
		goblin.setName(getName() + "-Mustergoblin");
		goblin.Befehle.add("TARNE einheit");
		goblin.Befehle.add("KAEMPFE nicht");
		goblin.Befehle.add("TARNE partei");
		goblin.Befehle.add(";NEU");
		goblin.setPersonen(30);
		goblin.setSkill(Tarnung.class, 180 * goblin.getPersonen());
		goblin.setSkill(Hiebwaffen.class, 180 * goblin.getPersonen());
		goblin.setItem(Silber.class, 3500);

		new Info(this.getName() + " Setup in " + r + ".", u);
	}

	@Override
	protected boolean verifyTest() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
