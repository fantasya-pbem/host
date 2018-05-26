package de.x8bit.Fantasya.Atlantis.Spells;

import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Spell.ConfusionSpell;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Side;
import de.x8bit.Fantasya.Host.ZAT.Battle.Effects.BFXSturm;
import de.x8bit.Fantasya.Host.ZAT.Battle.KriegerCounter;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WArmbrust;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WBogen;
import de.x8bit.Fantasya.util.Random;
import java.util.List;

public class Sturm extends Spell implements ConfusionSpell {

	public String getName() { return "Sturm"; }
	public String getBeschreibung() 
	{
		return "Mit diesem Verteidigungszauber sind die eigenen Einheiten vor den gegnerischen Bögen geschützt. " +
				" Der Zauberer kann pro Stufe 'Stufe x 100' (leichte) gegnerische Fluggeschosse ablenken. Dieser " +
				" Zauber wird im Laufe des Kampfes nachlassen.";
	}
	public String getSpruch() { return "ZAUBERE \"Sturm\" [Stufe]"; }
	public String getKampfzauberSpruch() { return "KAMPFZAUBER VERWIRRUNG \"" + getName() + "\" [Stufe]"; }
	public Elementar getElementar() { return Elementar.Erde; }
	public int getStufe() { return 5; }

	public String[] getTemplates() {
		return new String [] { 
				"(\")?(sturm)(\")? [1-9][0-9]?",
		}; 
	}	
	public String getCRSyntax() { return ""; }

	public int ExecuteSpell(Unit mage, String[] param) 
	{
		new Fehler("Die Böen fegen über " + mage + " hinweg, aber die Elementare wissen nicht recht, was sie außerdem tun sollten.", mage);

		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

//		Region region = Region.Load(mage.getCoords());
//		new Magie(mage + " läßt die Erde in " + region + " beben", mage);

		// evt. Verbesserung beim Segeln
		
		return 0;
	}
	
	@Override // von ConfusionSpell geerbt
	public int ExecuteSpell(Unit mage, Side my, Side other, String[] param) {
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel
		
		// Effekte an die Krieger verteilen
		PrepareSpell(other, stufe);
		
		// gezauberte Stufe zurück liefern
		return stufe;
	}
	
	// Effekt auf die Krieger verteilen
	private void PrepareSpell(Side opfer, int stufe) {
        int maxKrieger = stufe * 100;
        int dauer = stufe;
        
		// alle Krieger in einer Liste sammeln ... aber nur die die eine
		// Fernwaffe besitzen
		List<Krieger> krieger = new ArrayList<Krieger>();
        for(Krieger k : opfer.vorne) krieger.add(k);
		for(Krieger k : opfer.hinten) krieger.add(k);
		
		// Effekt verteilen
		KriegerCounter kc = new KriegerCounter();
		while(maxKrieger > 0 && krieger.size() > 0) {
			int nummer = Random.rnd(0, krieger.size() - 1);
			Krieger k = krieger.get(nummer);
			krieger.remove(k);

            if (k.hatFernwaffe()) {
                k.addEffect(new BFXSturm(dauer));
                kc.count(k);
                maxKrieger--;
            }
		}

        opfer.getGefecht().meldung("Ein Sturm behindert " + kc.getReportPhrase() + ".", true);
	}

}
