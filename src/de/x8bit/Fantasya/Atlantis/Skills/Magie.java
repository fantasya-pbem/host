package de.x8bit.Fantasya.Atlantis.Skills;

import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Zauberbuch;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

public class Magie extends Skill
{
    @Override
	public String Lernen(Unit u)
	{
		String message = "";
        int anzahl = u.getPersonen();
		int alterTW = u.Talentwert(Magie.class);
		
		// alle Magier ohne die aktuelle Einheit ermitteln und Limit überprüfen
		int vorhanden = 0;
		for (Unit u2 : Unit.CACHE.getAll(u.getOwner())) {
			if (u2.getNummer() == u.getNummer()) continue;
			if (u2.getSkill(Magie.class).getLerntage() > 0) {
				vorhanden += u2.getPersonen();
			}
		}
		if (vorhanden + u.getPersonen() > u.maxMagier()) {
			new Fehler(u + " kann keine Magie erlernen - es gibt schon " + vorhanden + " andere Magier.", u);
			return null;
		}
		
		// Silber überprüfen
		Item item = u.getItem(Silber.class);
		int preisProPerson = (100 + u.Talentwert(Magie.class) * 150);
		if (item.getAnzahl() < preisProPerson * u.getPersonen()) {
			anzahl = item.getAnzahl() / preisProPerson;
            if (anzahl == 0) {
                new Fehler(u + " hat zuwenig Silber um Magie zu lernen.", u);
                return null;
            } else {
                message = "hat nur für " + anzahl + " Personen Lehrgeld, ";
            }
		} 
        
        // nötiges Silber berechnen
        int silber = preisProPerson * anzahl;

        setLerntage(getLerntage() + anzahl * 30 + ((u.getLehrtage() / u.getPersonen()) * anzahl));
        item.setAnzahl(item.getAnzahl() - silber);
        message += "lernt " + getClass().getSimpleName() + " und gibt " + silber + " Silber dafür aus.";

        if (alterTW != u.Talentwert(this)) message += neuerSpruch(u);
        
        return message;
	}
	
	private String neuerSpruch(Unit u)
	{
		// bei Bedarf einen Spruch aus der Unterwelt (nur in der Unterwelt)
		boolean orcus = u.getCoords().getWelt() < 0 ? true : false;
		
		ArrayList<Spell> spells = new ArrayList<Spell>();
		for(Paket p : Paket.getPaket("Spells"))	{
			Spell spell = (Spell) p.Klasse;
			if (spell.getStufe() == u.Talentwert(this) && spell.isOrcus() == orcus) spells.add(spell);
		}
		
		// leere Liste ist doof
		if (spells.isEmpty()) return "";
		
		// Meldung und ggf. einen neuen Spruch zuweisen
		Spell spell = spells.get(Random.rnd(0, spells.size()));
		new Zauberbuch(spell, u);
		u.setSpell(spell);
        
		return u + " entdeckt den Zauberspruch " + spell.getName() + ".";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Magie", "Magie", null);
	}
}
