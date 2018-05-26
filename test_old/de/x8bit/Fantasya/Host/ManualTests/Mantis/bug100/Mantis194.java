package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Skills.Taktik;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Zwerg;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hapebe
 */
public class Mantis194 extends TestBase {

	protected void mySetupTest() {
		Region wald = this.getTestWorld().nurTerrain(getRegions(), Wald.class).get(0); getRegions().remove(wald);
		Region ebene = this.getTestWorld().nurTerrain(getRegions(), Ebene.class).get(0); getRegions().remove(ebene);
		Region berge = this.getTestWorld().nurTerrain(getRegions(), Berge.class).get(0); getRegions().remove(berge);
		Region gletscher = this.getTestWorld().nurTerrain(getRegions(), Gletscher.class).get(0); getRegions().remove(gletscher);

		Partei elfen = this.getTestWorld().createPartei(Elf.class);

		Unit elf1 = this.createUnit(elfen, wald);
		elf1.setSkill(Wahrnehmung.class, 30);
		elf1.setSkill(Tarnung.class, 30);
		elf1.setBeschreibung("Erwartet: T3 in Wahrnehmung und Tarnung.");

		Unit elf2 = this.createUnit(elfen, ebene);
		elf2.setSkill(Wahrnehmung.class, 30);
		elf2.setSkill(Tarnung.class, 30);
		elf2.setBeschreibung("Erwartet: T2 in Wahrnehmung und Tarnung.");

		new Info("Mantis #194 Setup (Elfen) in " + wald + " " + wald.getCoords() + " und " + ebene + " " + ebene.getCoords() +  ".", elfen);


		Partei zwerge = this.getTestWorld().createPartei(Zwerg.class);

		Unit zwerg1 = this.createUnit(zwerge, ebene);
		zwerg1.setSkill(Taktik.class, 30);
		zwerg1.setBeschreibung("Erwartet: T1 in Taktik.");

		Unit zwerg2 = this.createUnit(zwerge, berge);
		zwerg2.setSkill(Taktik.class, 30);
		zwerg2.setBeschreibung("Erwartet: T2 in Taktik.");

		Unit zwerg3 = this.createUnit(zwerge, gletscher);
		zwerg3.setSkill(Taktik.class, 30);
		zwerg3.setBeschreibung("Erwartet: T2 in Taktik.");

		new Info("Mantis #194 Setup (Zwerge) in " + berge + ", " + gletscher + " und " + ebene + ".", zwerge);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
