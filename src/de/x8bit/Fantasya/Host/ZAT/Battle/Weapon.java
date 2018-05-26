package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Armbrust;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Kriegselefant;
import de.x8bit.Fantasya.Atlantis.Items.Kriegshammer;
import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Items.Streitaxt;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WArmbrust;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WBogen;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WEisenschild;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WHolzschild;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WKatapult;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WKettenhemd;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WKriegselefant;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WKriegshammer;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WPegasus;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WPferd;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WPlattenpanzer;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WSchwert;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WSpeer;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WStreitaxt;

/**
 * @author  mogel
 */
public abstract class Weapon
{
    /**
     * Gibt die Waffe zurück, die einem Item entspricht. Wenn null zurückgegeben 
     * wird, entspricht das Item weder einer Waffe noch einem Rüstungs- oder 
     * Ausrüstungsgegenstand.
     * @param item Fragliches Item
     * @return Die entsprechende Waffe zu item - oder null, wenn es keine gibt
     */
    public static Class<? extends Weapon> GetWeaponClassFor(Class<? extends Item> item) {
        if (item == Schwert.class) return WSchwert.class;
        if (item == Speer.class) return WSpeer.class;
        if (item == Streitaxt.class) return WStreitaxt.class;
        if (item == Kriegshammer.class) return WKriegshammer.class;

        if (item == Armbrust.class) return WArmbrust.class;
        if (item == Bogen.class) return WBogen.class;

        if (item == Katapult.class) return WKatapult.class;

        if (item == Pferd.class) return WPferd.class;
        if (item == Pegasus.class) return WPegasus.class;
        if (item == Kriegselefant.class) return WKriegselefant.class;

        if (item == Eisenschild.class) return WEisenschild.class;
        if (item == Holzschild.class) return WHolzschild.class;

        if (item == Kettenhemd.class) return WKettenhemd.class;
        if (item == Plattenpanzer.class) return WPlattenpanzer.class;

        return null;
    }

    public static Weapon FromItem(Unit u, Item it) {
        if (it == null) {
            new SysErr("item ist null in Weapon.FromItem bei Unit " + u + "?");
            return null;
        }

        Class<? extends Weapon> wClass = Weapon.GetWeaponClassFor(it.getClass());
        Weapon retval = null;

        if (wClass == WSchwert.class) retval = new WSchwert(u);
        if (wClass == WSpeer.class) retval = new WSpeer(u);
        if (wClass == WStreitaxt.class) retval = new WStreitaxt(u);
        if (wClass == WKriegshammer.class) retval = new WKriegshammer(u);

        if (wClass == WArmbrust.class) retval = new WArmbrust(u);
        if (wClass == WBogen.class) retval = new WBogen(u);

        if (wClass == WKatapult.class) retval = new WKatapult(u);

        if (wClass == WPferd.class) retval = new WPferd(u);
        if (wClass == WPegasus.class) retval = new WPegasus(u);
        if (wClass == WKriegselefant.class) retval = new WKriegselefant(u);

        if (wClass == WEisenschild.class) retval = new WEisenschild(u);
        if (wClass == WHolzschild.class) retval = new WHolzschild(u);

        if (wClass == WKettenhemd.class) retval = new WKettenhemd(u);
        if (wClass == WPlattenpanzer.class) retval = new WPlattenpanzer(u);

        if (retval != null) retval.ursprungsItem = it.getClass();

        // wenn null, dann ist dieses Item wohl keine Waffe oder Rüstung...
        return retval;
    }

	// Angriff und Verteidigung
	protected Weapon(Unit unit) { this.unit = unit; }

	/**
	 * diese Einheit für diese Waffe
	 * @uml.property  name="unit"
	 * @uml.associationEnd  
	 */
	protected Unit unit = null;
	/**
	 * diese Einheit für diese Waffe
	 * @uml.property  name="unit"
	 */
	public Unit getUnit() { return unit; }
	
	
	
	/** benötigtes Talent um mit der Waffe hantieren zu können */
	abstract public Class<? extends Skill> neededSkill();


    /**
     * Item, aus dem diese Waffe entsteht / entstanden ist
     */
    protected Class<? extends Item> ursprungsItem;
    abstract public Class<? extends Item> getUrsprungsItem();
	
	
	/** liefert den Waffentyp für die aktuelle Waffe */
	abstract public WeaponType getWeaponType();
	

    /**
     * @return true, wenn die Waffe wenigstens 2 Reihen weit reicht, also von 
     * (Freund) hinten nach (Feind) vorn oder von (Freund) vorn nach (Feind) hinten.
     */
    abstract public boolean istFernkampfTauglich();


    /**
     * <p>Zeitabhängige Veränderungen an der Waffe.</p>
     * <p>Wird bspw. von Katapult und Armbrust überschrieben.</p>
     * @see Weapon.istBereit()
     */
    public void naechsteRunde() { }

    /**
     * @return Die Anzahl möglicher Angriffe in dieser Kampfrunde - richtet sich nach Art der Waffe und bspw. Talentwert. Default: 1
     */
    abstract public int numberOfAttacks();

	// --------------------------------------------------------------------------------------
	// Kram für die Angriffsberechnenung ... hierbei kommt aber nur
	// raus ob der Angriff erfolgreich war !!
	// --------------------------------------------------------------------------------------
	
	/**
	 * es wird jemand angegriffen ... Waffen sind aber nur für eine Bestimmte Kampfreihe (1. oder 2.)
	 * daher muss hier die Kampfreihe der beiden Kontrahenten mit übergeben werden ... die Waffe selber
	 * entscheidet dann ob mit Ihr angegriffen wird ... dabei muss aber darauf gachtet werden, das eine
	 * Einheit durchaus 2 Waffen haben kann die für die gleiche Kampfposition sind ... somit wäre es
	 * möglich das die Einheit doppelt in einer Runde angreift ... darum hat sich _1on1_() zu kümmern
	 * @param reihe_attacker - Reihe des Angreifers
	 * @param reihe_defender - Reihe des Verteidigers
	 * @return ganzzahliger Angriffwert (AV)
	 */
	abstract public int AttackValue(int reihe_attacker, int reihe_defender);
	
	/**
	 * der Kram gleicht eigentlich dem AttackValue ... nur halt für den Verteidiger 
	 * @param reihe_attacker - Reihe des Angreifers
	 * @param reihe_defender - Reihe des Verteidigers
	 * @return ganzzahliger Verteidigungswert (DV)
	 */
	abstract public int DefenceValue(int reihe_attacker, int reihe_defender);
	
	/**
	 * <p>Wie verändert diese Waffe <em>des Angreifers</em> den <strong><em>AV</em></strong>?</p>
	 * jeder Angriff kann eine zusätzliche Modifikation enthalten ... diese Modifikation spiegelt
	 * dann die Boni bzw. Mali bei verschiedenen Waffen-Kombinationen wieder <i>Pferd <-> Speer</i> 
	 * <b>Änderung des Defence-Value durch Waffen des Angreifers</b> 
	 * @param attacker - Waffe des Angreifers
	 * @param defender - Waffe des Verteidigers
	 * @return Änderung des Angriffswertes in Prozent
	 */
	abstract public float AttackModifikation_Attacker(Krieger attacker, Krieger defender);

	/**
	 * <p>Wie verändert diese Waffe <em>des Verteidigers</em> den <strong><em>AV</em></strong>?</p>
	 * jeder Angriff kann eine zusätzliche Modifikation enthalten ... diese Modifikation spiegelt
	 * dann die Boni bzw. Mali bei verschiedenen Waffen-Kombinationen wieder <i>Pferd <-> Speer</i> 
	 * <b>Änderung des Defence-Value durch Waffen des Verteidigers</b> 
	 * @param attacker - Waffe des Angreifers
	 * @param defender - Waffe des Verteidigers
	 * @return Änderung des Angriffswertes in Prozent
	 */
	abstract public float AttackModifikation_Defender(Krieger attacker, Krieger defender);

	/**
	 * <p>Wie verändert diese Waffe <em>des Angreifers</em> den <em>DV</em>?</p>
	 * jeder Angriff kann eine zusätzliche Modifikation enthalten ... diese Modifikation spiegelt
	 * dann die Boni bzw. Mali bei verschiedenen Waffen-Kombinationen wieder <i>Pferd <-> Speer</i>
	 * <b>Änderung des Defence-Value durch Waffen des Angreifers</b> 
	 * @param attacker - Waffe des Angreifers
	 * @param defender - Waffe des Verteidigers
	 * @return Änderung des Schadens in Prozent
	 */
	abstract public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender);
	
	/**
	 * <p>Wie verändert diese Waffe <em>des Verteidigers</em> den <em>DV</em>?</p>
     * jeder Angriff kann eine zusätzliche Modifikation enthalten ... diese Modifikation spiegelt
	 * dann die Boni bzw. Mali bei verschiedenen Waffen-Kombinationen wieder <i>Pferd <-> Speer</i>
	 * <b>Änderung des Defence-Value durch Waffen des Verteidigers</b> 
	 * @param attacker - Waffe des Angreifers
	 * @param defender - Waffe des Verteidigers
	 * @return Änderung des Schadens in Prozent
	 */
	abstract public float DefenceModifikation_Defender(Krieger attacker, Krieger defender);

	
	
	// --------------------------------------------------------------------------------------
	// hier wird jetzt der Schaden berechnet der bei einem erfolgreichen Angriff auftrat
	// --------------------------------------------------------------------------------------

	
	
	/**
	 * eine Waffe macht schaden ... den macht sie aber nur wenn AttackValue erfolgreich ist (also > 0 liefert) ...
	 * dann muss auch der DamageValue gemerkt werden ... entgegen AttackValue und DefenceValue werden hier die
	 * Punkte addiert
	 * @return ganzzahliger Angriffsschadens
	 */
	abstract public int DamageValue();	// Schaden durch Angriff


    /**
     * Dient zur Abschätzung der Qualität einer Waffe, wenn systemseitig
     * entschieden werden muss (bspw. bei der Waffenverteilung vor dem Kampf)
	 *
     * @return durchschnittlicher Schaden
     */
    abstract public float AverageDamageValue();
	
	/**
	 * jede Rüstung verhindert Schaden ... dadurch wird hier alles Summiert ... Rest wie bisher
	 * @return ganzahliger Blockschaden, wird von DamageValue subtrahiert
	 */
	abstract public int BlockValue();	// Blockierung des Schadens durch Angriff


    /**
     * Dient zur Abschätzung der Qualität einer Rüstung, wenn systemseitig
     * entschieden werden muss (bspw. bei der Waffenverteilung vor dem Kampf)
     * @return durchschnittlicher Schaden
     */
    abstract public float AverageBlockValue();
	
	/**
	 * Wie verändert diese Waffe <em>des Angreifers</em> den Roh-Schadenswert?
	 * @param defender - Angreifers
	 * @param attacker - Verteidigers
	 * @return Prozentwert der der mit Damage-Value verrechnet wird
	 */
	abstract public float DamageModifikation_Attacker(Krieger attacker, Krieger defender); 

	/**
	 * Wie verändert diese Waffe <em>des Verteidigers</em> den Roh-Schadenswert?
	 * @param attacker - Angreifers
	 * @param defender - Verteidigers
	 * @return Prozentwert der der mit Damage-Value verrechnet wird
	 */
	abstract public float DamageModifikation_Defender(Krieger attacker, Krieger defender); 
	
	/**
	 * Wie verändert diese Waffe <em>des Angreifers</em> die Schadensreduktion (Rüstungsschutz)?
	 * @param attacker - Angreifers
	 * @param defender - Verteidigers
	 * @return Prozentwert der der mit Block-Value verrechnet wird
	 */
	abstract public float BlockModifikation_Attacker(Krieger attacker, Krieger defender); 

	/**
	 * <p>Wie verändert diese Waffe <em>des Verteidigers</em> die
     * Schadensreduktion (Rüstungsschutz)?</p>
     * <p>Der Rüstungsschutz, der von der Ausrüstungs des Angreifers unabhängig ist,
     * sollte eher über BlockValue() angegeben werden.</p>
	 * @param attacker - Angreifers
	 * @param defender - Verteidigers
	 * @return Prozentwert der der mit Block-Value verrechnet wird
	 */
	abstract public float BlockModifikation_Defender(Krieger attacker, Krieger defender); 

	
	
	// --------------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------------

	
	
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * this.getClass().hashCode();
        return hash;
    }

    /**
     * @param other anderes Weapon-Objekt
     * @return true, wenn das andere Object ein Subtyp von Weapon ist - und zwar der selbe.
     */
    public boolean equals(Object other) {
        if (!(other instanceof Weapon)) return false;

        Weapon o = (Weapon)other;
        if (o.getClass() != this.getClass()) return false;

        return true;
    }


	/**
	 * liefert den Waffennamen zurück (ohne das 'W')
	 */
    @Override
	public String toString() {
		return this.getClass().getSimpleName().substring(1);
	}

    public abstract String kurzCode();

    /**
     * @param talenteZeigen
     * @return Gibt einen genau 4 Zeichen langen Code für w (und ggf. das dazugehörige Talent) an.
     */
    public String beschreibeKurz(boolean talenteZeigen) {
        StringBuilder retval = new StringBuilder();
        retval.append(kurzCode());
        if (talenteZeigen) {
            if ((neededSkill() != null) && (Skill.class != neededSkill())) {
                int tw = this.getUnit().Talentwert(neededSkill());
                retval.append(tw);
            }
        }
        return retval.toString();
    }



	/**
	 * eine Auflistung aller Waffenarten (Nahkampf und so)
	 * @author   mogel
	 */
	public enum WeaponType
	{
		keineWaffe,
		Nahkampf,
		Distanzkampf,
		Fernkampf,
		Kampfzauber,
		Schild,
		Panzer,
		Tier,
	}
}
