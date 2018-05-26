package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;

/**
 * Quelle: Forum - http://www.fantasya-pbem.de/forum/viewtopic.php?f=16&t=146
 *
 * @author hapebe
 */
public class Fernsicht extends Spell {

	public String getSpruch() { return "ZAUBERE \"Fernsicht\" X-Koordinate Y-Koordinate"; }
	public int getStufe() { return 3; }
	public Elementar getElementar() { return Elementar.Luft; }
	public String getBeschreibung() {
		return
				"Der Magier geht in sich, schliesst die Augen für das Reale und schickt sie im Geiste auf eine ferne Reise. " +
				"Falls er erfolgreich ist, wird er eine Region in der Ferne genau so sehen, als ob er gerade dort wäre. " +
				"Die Bewohner dieser Region werden davon nichts bemerken. " + 
				"Pro Stufe kann der Zauberer 3 Regionen weit sehen. Beim Zaubern " +
				"müssen die Koordinaten der gewünschten Region angegeben werden.";
	}

	public String[] getTemplates() {
		return new String [] {
			"(\")?(fernsicht)(\")? [-+]?[0-9]+ [-+]?[0-9]",
		};
	}
	public String getCRSyntax() { return "r"; }

	public int ExecuteSpell(Unit mage, String[] param) {
		Coords globalTarget = null;

		try {
			param[2] = param[2].replaceAll("\\+", "");
			param[3] = param[3].replaceAll("\\+", "");

			int privateX = Integer.parseInt(param[2]);
			int privateY = Integer.parseInt(param[3]);
			
			Coords privateTarget = new Coords(privateX, privateY, mage.getCoords().getWelt());

			Partei p = Partei.getPartei(mage.getOwner());

			globalTarget = p.getGlobalCoords(privateTarget);
		} catch(Exception ex) {
			new Fehler("Was genau soll ich televisionieren? (" + param[2] + " " + param[3] + "?)", mage);
			return 0;
		}

		if (globalTarget == null) {
			new SysErr("Zielregion ist null in Fernsicht bei Einheit " + mage.getNummerBase36() + ".");
			return 0;
		}

		int distance = globalTarget.getDistance(mage.getCoords());
		int stufe = (distance - 1) / 3 + 1; // d.h. von 0-3 Stufe 1, von 4-6 Stufe 2 usw.
		if (stufe <= 0) stufe = 1;

		int kosten = (this.getStufe() * stufe);

		// ausreichend Aura?
		if (mage.getAura() < kosten) {
			new Fehler(mage + " fehlt genügend Aura (" + kosten + "), um " + distance + " Regionen in die Ferne zu spähen.", mage);
			return 1;
		}

		// do it!
		Partei p = Partei.getPartei(mage.getOwner());
		p.addKnownRegion(globalTarget, true, Fernsicht.class);
		Region ziel = Region.Load(globalTarget);
		for (Region nachbar: ziel.getNachbarn()) {
			p.addKnownRegion(nachbar, false, Fernsicht.class);
		}

		String zielText = ziel.toString();
		if (!ziel.istBetretbar(null)) zielText += " " + p.getPrivateCoords(ziel.getCoords()).xy();

		new Magie(mage + " lässt seinen Blick " + distance + " Regionen weit (" + stufe + ". Stufe) in die Ferne schweifen und erkennt " + zielText + ".", mage);

		return stufe;
	}

}
