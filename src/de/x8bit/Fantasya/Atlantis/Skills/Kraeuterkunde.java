package de.x8bit.Fantasya.Atlantis.Skills;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.util.ComplexName;

public class Kraeuterkunde extends Skill {

	public ComplexName getComplexName() {
		return new ComplexName("Kräuterkunde", "Kräuterkunden",
				new String[]{"Kraeuterkunde", "Kraeuterkunden"});
	}

	public String Lernen(Unit u)
	{
		String message = "";
        int anzahl = u.getPersonen();
		
		// Silber überprüfen
		Item item = u.getItem(Silber.class);
		if (item.getAnzahl() < 250 * u.getPersonen())
		{
			anzahl = item.getAnzahl() / 250;
            if (anzahl == 0) {
                new Fehler(u + " hat zuwenig Silber um Kräuterkunde zu lernen.", u);
                return null;
            } else {
                message = "hat nur für " + anzahl + " Personen Lehrgeld und ";
            }
		}
		
		setLerntage(getLerntage() + anzahl * 30 + ((u.getLehrtage() / u.getPersonen()) * anzahl));
		
		int silber = 250 * anzahl;
		item.setAnzahl(item.getAnzahl() - silber);
		return message + "gibt " + silber + " Silber zum Lernen von Kräuterkunde aus.";
	}
}
