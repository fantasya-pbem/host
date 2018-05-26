package de.x8bit.Fantasya.Host.Reports.util;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import java.util.Comparator;

/**
 *
 * @author hapebe
 */
public class ParteiTalentComparator implements Comparator<Partei>{

	@Override
	public int compare(Partei p1, Partei p2) {
		int n1 = 0;
		for (Unit u : p1.getEinheiten()) {
			for (Skill sk : u.getSkills()) {
				n1 += sk.getLerntage();
			}
		}
		int n2 = 0;
		for (Unit u : p2.getEinheiten()) {
			for (Skill sk : u.getSkills()) {
				n2 += sk.getLerntage();
			}
		}
		
		float ratio1 = (float)n1 / (float)p1.getPersonen();
		float ratio2 = (float)n2 / (float)p2.getPersonen();
		
		if (ratio1 < ratio2) return -1;
		if (ratio1 > ratio2) return +1;
		return 0;
	}
	
}
