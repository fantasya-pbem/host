package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Spell.AttackSpell;
import de.x8bit.Fantasya.Atlantis.Spell.ConfusionSpell;
import de.x8bit.Fantasya.Atlantis.Spell.DefenceSpell;

public class Zauberbuch extends Message {

	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public Zauberbuch() {}

	public Zauberbuch(String msg, Unit u) {
		super();
        print(0, msg, u.getCoords(), u);
    }
	
	/** gibt den Zauberspruch gleich formatiert für NR aus - sieht natürlich beim CR blöd aus */
	public Zauberbuch(Spell spell, Unit u) {
		super();

		String msg =
			spell.getName() + "\n" +
			" \n" +
			spell.getBeschreibung() + "\n" +
			spell.getSpruch() + "\n";
		if (spell.isBattleSpell()) {
			if (spell.isAttackSpell()) msg += ((AttackSpell)spell).getKampfzauberSpruch();
			if (spell.isConfusionSpell()) msg += ((ConfusionSpell)spell).getKampfzauberSpruch();
			if (spell.isDefenceSpell())	msg += ((DefenceSpell)spell).getKampfzauberSpruch();
			msg += "\n";
		}
		msg += "TW: " + spell.getStufe() + " - " + (spell.isOrcus() ? "Mana-Kosten" : "Aura-Kosten") + ": " + spell.getStufe() + "\n";
		msg += "\n";

		print(0, msg, Partei.getPartei(u.getOwner()));
	}
}
