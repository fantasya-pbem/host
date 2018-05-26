package de.x8bit.Fantasya.util.comparator;

import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import java.util.Comparator;

/**
 * @author hb
 */
public class UnitSkillComparator implements Comparator<Unit> {
    final Class<? extends Skill> skill;

    public UnitSkillComparator(Class<? extends Skill> skill) {
        this.skill = skill;
    }

    @Override
    public int compare(Unit u1, Unit u2) {
        if (u1.Talentwert(skill) > u2.Talentwert(skill)) {
            return -1;
        } else if (u1.Talentwert(skill) < u2.Talentwert(skill)) {
            return +1;
        }
        return 0;
    }

}
