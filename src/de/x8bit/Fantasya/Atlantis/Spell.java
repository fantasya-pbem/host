package de.x8bit.Fantasya.Atlantis;

import java.util.HashMap;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Host.ZAT.Battle.Side;

/**
 * Basis-Klasse für alle Zaubersprüche
 * 
 * @author mogel
 */
public abstract class Spell extends Atlantis implements Comparable<Spell>
{
	/** eine kleiner Überschreibung um keine Nummer am Ende zu haben */
    @Override
	public String toString() { return getName(); }

    private int getIdentNummer() {
        int result = 0;
        for (int i=0; i<this.getName().length(); i++) {
            char c = this.getName().charAt(i);
            result += (c-64);
        }
        result -= this.getName().length();
        result *= this.getStufe();

        return result;
    }

	/** 
	 * Talentwert ab wann der Zauber erhalten werden kann ... auch erst ab dieser Stufe kann gezaubert werden 
	 */
	public abstract int getStufe();
	
	/** gehört dieser Zauberspruch zur Unterwelt */
	public boolean isOrcus() { return false; }
	
	/** 
	 * das sind die Vorlagen für den Zauberspruch (für FCheck2) ... das hat nicht wirklich
	 * was mit der Methode getSpruch() zu tun ... letzteres ist für die Spieler, diese
	 * Methode liefert ein Haufen RegEx Dinger
	 */
	public abstract String [] getTemplates();

	/** 
	 * führt den Effekt dieses Zauber aus ... Aura wird schon vorher
	 * getestet ... wenn der Aura Verbrauch vom Spruch abhängig ist,
	 * dann muss das im Spruch erneut getestet werden ... allgemeines Format für einen Zauberspruch:
	 * <pre>ZAUBERE [Name] [Param1 [Param2 [...]]]</pre>
	 * der Rückgabewert ist die Stufe mit der der Zauber gesprochen wurde ... das
	 * wird für die Aura bzw. Mana Berechnung <b>nach</b> dem ausführen des Spruches
	 * berechnet
	 * @param krieger - diese Einheit führt den Spruch aus
	 * @param param - die komplette Befehlszeile
	 * @return auf dieser Stufe wurde gezaubert
	 */
	public abstract int ExecuteSpell(Unit mage, String[] param);
		
	/** so wird der Zauber ausgesprochen */
	public abstract String getSpruch();

    /**
	 * So wird der Zauber im CR notiert.
	 * 
	 * <p>Zitat von <a href="http://dose.0wnz.at/thewhitewolf/cr-format#syntax">
	 * http://dose.0wnz.at/thewhitewolf/cr-format#syntax</a>:</p>
	 *
	 * <blockquote><pre>
	 * Die Syntax eines Zauber in kryptischer Form. Die Einhaltung ist zum 
	 * erfolgreichen Zaubern notwendig aber nicht hinreichend.
	 * 
	 * Bekannte Beispiele (unvollständig):
	 * 
	 * ZAUBERE [STUFE n] Zauber
	 * "";syntax
	 * 
	 * ZAUBERE [STUFE n] Zauber <Einheit-Nr>
	 * "u";syntax
	 * 
	 * ZAUBERE Zauber <Einheit-Nr> <investierte Aura>
	 * "ui";syntax
	 * 
	 * ZAUBERE [REGION x y] [STUFE n] Zauber REGION
	 * ZAUBERE [REGION x y] [STUFE n] Zauber EINHEIT <Einheit-Nr>
	 * ZAUBERE [REGION x y] [STUFE n] Zauber GEBÄUDE <Gebäude-Nr>
	 * ZAUBERE [REGION x y] [STUFE n] Zauber SCHIFF <Schiff-Nr>
	 * "kc?";syntax
	 * 
	 * ZAUBERE Zauber <Richtung>
	 * "c";syntax
	 * 
	 * ZAUBERE [STUFE n] Zauber <Einheit-Nr> [<Einheit-Nr> ...]
	 * "u+";syntax
	 * 
	 * Bedeutung der einzelnen Buchstaben:
	 * 
	 * 'c' = Zeichenkette
	 * 'k' = REGION|EINHEIT|STUFE|SCHIFF|GEBAEUDE
	 * 'i' = Zahl
	 * 's' = Schiffsnummer
	 * 'b' = Gebaeudenummer
	 * 'r' = Regionskoordinaten (x, y)
	 * 'u' = Einheit
	 * '+' = Wiederholung des vorangehenden Parameters
	 * '?' = vorangegangener Parameter ist nicht zwingend
	 * 
	 * Defaultwert: ZAUBERE [STUFE n] Zauber
	 * oder Syntax nicht bekannt.
	 * </blockquote>
	 */
    public abstract String getCRSyntax();

    public String getCREntry() {
        StringBuilder sb = new StringBuilder();
        sb.append("ZAUBER " + getIdentNummer() + " \n");
        sb.append("\"" + getName() + "\";name\n");
        if (!this.isBattleSpell()) {
            sb.append("\"normal\";class\n");
        } else {
            sb.append("\"combat\";class\n");
        }
        sb.append(getStufe() + ";level\n");
        sb.append(getStufe() + ";rank\n");
        sb.append("\"" + getBeschreibung() + "\";info\n");
        sb.append("\"" + getCRSyntax() + "\";syntax\n");
        sb.append("KOMPONENTEN\n");
        sb.append(getStufe() + " 1;Aura\n");

        // letztes \n abtrennen:
        return sb.substring(0, sb.length() - 1);
    }


	/**
	 * einige Zaubersprüche müssen vor allen anderen <i>Befehlen</i> gezaubert werden
	 * (z.B. Voodoo) ... dann muss das auf TRUE gesetzt werden
	 * @return TRUE wenn es ein FirstSpell ist
	 */
	public boolean isFirstSpell() { return false; }
	
	/**
	 * ob das ein Kampfzauber ist ... dann kann er im Kampf eingesetzt werden,
	 * er kann auch so gezaubert werden
	 * @return TRUE es ist einer *yes!*
	 */
	public boolean isBattleSpell() { return this instanceof AttackSpell || this instanceof DefenceSpell || this instanceof ConfusionSpell; }
	
	/** ob der Kampfzauber ein Pre-Kampfzauber ist ... also vor allen Kämpfen gesprochen wird. */
	public boolean isConfusionSpell() { return this instanceof ConfusionSpell; }

	/** ob es ein Angriffszauber ist */
	public boolean isAttackSpell() { return this instanceof AttackSpell; }
	
	/** ob der Zauber ein Verteidigungszauber ist - der jede Kampfrunde verwendet wird */
	public boolean isDefenceSpell() { return this instanceof DefenceSpell; }
	
	/**
	 * liefert das Elementar zu dem dieser Zauber gehört
	 * @return das Elementar des Zauberspruches
	 */
	public abstract Elementar getElementar();
	
	/**
	 * überprüft ob ein Mager den Spruch verwenden kann (ohne Check auf Aura/Mana)
	 * @param mage - dieser Magier
	 * @return TRUE wenn der magier den Spruch verwenden kann
	 */
	public boolean canUsedBy(Unit mage)	{
        for(Spell spell : mage.getSpells()) {
			if (spell.getClass() == this.getClass()) {
				if (this.getStufe() <= mage.Talentwert(Magie.class)) return true;
            }
        }
		return false;
	}
	
	/**
	 * überprüft vom Spruch bzw. Magie die Stufe die gezaubert werden kann
	 * @param mage - dieser Magier
	 * @param stufe - diese Stufe aus dem Befehl
	 * @return die Stufe mit der gezaubert wird
	 */
	public int getSpellLevel(Unit mage, String s)
	{
		int stufe = 0; // Wunschstufe zum Zaubern
		try { stufe = Integer.parseInt(s); } catch (Exception ex) { /* */ }
		if (stufe == 0) {
			new Fehler(mage + ": '" + s + "'" + " - die Stufe des Zaubers wurde nicht erkannt.", mage);
			return 0;
		}

		// Magie checken
		int aura = mage.getAura();
		if (aura < stufe * getStufe()) {
			stufe = aura / getStufe();
			new Fehler(mage + " kann nur bis Stufe " + stufe + " zaubern.", mage);
		}
		if (stufe > mage.Talentwert(de.x8bit.Fantasya.Atlantis.Skills.Magie.class)) {
            new Fehler(mage + " hat nicht das nötige Talent für Stufe " + stufe + ".", mage); 
            return 0;
        }
		
		return stufe;
	}
	

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues(Unit u) {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("einheit", u.getNummer());
		fields.put("Spruch", this.getClass().getSimpleName());

		return fields;
	}

    @Override
    public int compareTo(Spell o) {
        if (getStufe() < o.getStufe()) return -1;
        if (getStufe() > o.getStufe()) return +1;
        
        // gleiche Stufe...
        return getName().compareTo(o.getName());
    }


	/**
	 * das Interface für normale Kampfzauber
	 * @author mogel
	 */
	public interface AttackSpell
	{
		/** 
		 * führt den Angriffszauber auf einen Krieger (im normal Fall der Magier) aus
		 * @param krieger - Zauber auf der den Spruch ausführt
		 * @param param - der Spruch - zerlegt
		 * @return Stufe mit der gezaubert wird
		 */
		int ExecuteSpell(de.x8bit.Fantasya.Host.ZAT.Battle.Krieger krieger, String [] param);

		/**
		 * @return Befehl zum setzen als Kampfzauber.
		 */
		public String getKampfzauberSpruch();
	}
	
	/**
	 * das Interface für Verteidigungszauber
	 * @author mogel
	 */
	public interface DefenceSpell
	{
		
		/**
		 * @return Befehl zum setzen als Kampfzauber.
		 */
		public String getKampfzauberSpruch();
	}
	
	/**
	 * das Interface für Verwirrungszauber (Pre-Kampfzauber)
	 * @author mogel
	 */
	public interface ConfusionSpell
	{
		/**
		 * für den Verwirrungszauber aus
		 * @param mage - der Magier zaubert
		 * @param my - meine eigene Seite
		 * @param other - die Seite der Gegner
		 * @param param - der Zauberspruch - entsprechend zerlegt
		 * @return die gezauberte Stufe
		 */
		int ExecuteSpell(Unit mage, Side my, Side other, String [] param);
		
		/**
		 * @return Befehl zum setzen als Kampfzauber.
		 */
		public String getKampfzauberSpruch();
	}
}
