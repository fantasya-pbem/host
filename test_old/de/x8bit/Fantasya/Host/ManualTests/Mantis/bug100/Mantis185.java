package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
0000185: Komplexes LEHREN funktioniert nicht
Meine Einheit [456] (13 Mann Bogenschießen T9) hatte den Befehl "LEHRE 4kg 4xb 5kv".
Meine Einheit [1z4] (6 Mann Bogenschießen T9) hatte den Befehl "LEHRE 4kg 4xb 5kv".
Die Einheiten [4kg] (4 Mann), [4xb] (164 Mann) und [5kv] (14 Mann), alle schlechter im TW, haben Bogenschießen gelernt.
Somit haben 19 Mann gelehrt und 182 gelernt. Allerdings hat das nicht geklappt, beispielsweise [4kg] hat nur 30 Lerntage gewonnen.
 * @author hapebe
 */
public class Mantis185 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei p = this.getTestWorld().getSpieler1();
		Region r = getRegions().get(0);

		{ // eigentlicher Bug
			// Lehrer
			Unit l1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit l2 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			// Schüler
			Unit s1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit s2 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit s3 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());

			l1.setName("Lehrer 1"); l2.setName("Lehrer 2");
			s1.setName("Schüler 1"); s2.setName("Schüler 2"); s3.setName("Schüler 3");
			String schueler =
					s1.getNummerBase36() + " " +
					s2.getNummerBase36() + " " +
					s3.getNummerBase36();


			l1.setPersonen(13);
			l1.setSkill(Bogenschiessen.class, 13*1350);
			l1.setItem(Silber.class, 20000);
			l1.setLongOrder("LEHRE " + schueler);

			l2.setPersonen(6);
			l2.setSkill(Bogenschiessen.class, 6*1350);
			l2.setLongOrder("LEHRE " + schueler);

			s1.setPersonen(4);
			s1.setSkill(Bogenschiessen.class, 4*840);
			s1.setLongOrder("LERNE Bogenschiessen");

			s2.setPersonen(164);
			s2.setSkill(Bogenschiessen.class, 164*840);
			s2.setLongOrder("LERNE Bogenschiessen");

			s3.setPersonen(14);
			s3.setSkill(Bogenschiessen.class, 14*840);
			s3.setLongOrder("LERNE Bogenschiessen");
		}

		{ // Gegencheck:
			// Lehrer
			Unit l1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit l2 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			// Schüler
			Unit s1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit s2 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit s3 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());

			l1.setName("Lehrer 1"); l2.setName("Lehrer 2");
			s1.setName("Schüler 1"); s2.setName("Schüler 2"); s3.setName("Schüler 3");
			String schueler =
					s1.getNummerBase36() + " " +
					s2.getNummerBase36() + " " +
					s3.getNummerBase36();


			l1.setPersonen(13);
			l1.setSkill(Speerkampf.class, 13*1350);
			l1.setItem(Silber.class, 20000);
			l1.setLongOrder("LEHRE " + schueler);

			l2.setPersonen(6);
			l2.setSkill(Speerkampf.class, 6*1350);
			l2.setLongOrder("LEHRE " + schueler);

			s1.setPersonen(4);
			s1.setSkill(Speerkampf.class, 4*840);
			s1.setLongOrder("LERNE Speerkampf");

			s2.setPersonen(164);
			s2.setSkill(Speerkampf.class, 164*840);
			s2.setLongOrder("LERNE Speerkampf");

			s3.setPersonen(44);
			s3.setSkill(Speerkampf.class, 44*840);
			s3.setLongOrder("LERNE Speerkampf");
		}

		{ // Gegencheck 2:
			// Lehrer
			Unit l1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit l2 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit l3 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			// Schüler
			Unit s1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit s2 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
			Unit s3 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());

			l1.setName("Lehrer 1 - TW zu niedrig für 1 und 2"); l2.setName("Lehrer 2");  l3.setName("Lehrer 3");
			s1.setName("Schüler 1"); s2.setName("Schüler 2"); s3.setName("wird von Lehrer 1 gelehrt");
			String schueler =
					s1.getNummerBase36() + " " +
					s2.getNummerBase36() + " " +
					s3.getNummerBase36();

			l1.setPersonen(1);
			l1.setSkill(Wahrnehmung.class, 1*840);
			l1.setItem(Silber.class, 20000);
			l1.setLongOrder("LEHRE " + schueler);

			l2.setPersonen(2);
			l2.setSkill(Wahrnehmung.class, 2*1350);
			l2.setLongOrder("LEHRE " + schueler);

			l3.setPersonen(8);
			l3.setSkill(Wahrnehmung.class, 8*1350);
			l3.setLongOrder("LEHRE " + schueler);

			s1.setPersonen(80);
			s1.setSkill(Wahrnehmung.class, 80*840);
			s1.setLongOrder("LERNE Wahrnehmung");

			s2.setPersonen(10);
			s2.setSkill(Wahrnehmung.class, 10*840);
			s2.setLongOrder("LERNE Wahrnehmung");

			s3.setPersonen(100);
			s3.setSkill(Wahrnehmung.class, 100*630);
			s3.setLongOrder("LERNE Wahrnehmung");
		}

		new Info("Mantis #185 Setup in " + r + " " + r.getCoords() + ".", p);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
