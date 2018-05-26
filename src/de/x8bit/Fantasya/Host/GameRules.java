package de.x8bit.Fantasya.Host;

import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author  mogel
 */
public final class GameRules
{
	public final static String GAME_RUNDE = "game.runde";
	public final static String GAME_JAHRESZEIT_VERSCHIEBUNG = "game.jahreszeit.verschiebung";
	public final static String INSELKENNUNG_WELT = "inselkennung.welt";
	public final static String INSELKENNUNG_SPIELER = "inselkennung.spieler";
	public final static String INSELKENNUNG_ERZEUGUNG = "inselkennung.erzeugung";
	/**
	 * <p>Wird bei tatsächlicher Verwendung noch genauer benannt, bspw.
	 * inselkennung.erzeugung.hotspot.welt.1 für die normale Oberwelt.</p>
	 * <p>Der Eintrag selbst sollte eine String-Repräsentation von Coords sein,
	 * siehe Coords.toString() und Coords.fromString().</p>
	 * <p>Kennzeichnet die "letzte tektonische Aktivität" - den Mittelpunkt der
	 * zuletzt neu erzeugten Regionen. Ziel der Übung ist es, die Erzeugung
	 * neuer Regionen nicht mehr zufällig am Außenrand der Welt zu verorten,
	 * sonder in einer Art Spirale; bei der neue Regionen jeweils nahe bei den
	 * jüngsten vorhandenen erzeugt werden.</p>
	 */
	public final static String INSELKENNUNG_ERZEUGUNG_HOTSPOT = "inselkennung.erzeugung.hotspot.welt";
	
	/** Bestimmt, welche Methode zum Erzeugen neuer Inseln verwendet wird. Default: "classic". Alternative Werte: "April2011". */
	public final static String NEUE_INSEL_METHODE = "game.option.neueInselMethode";

    /** 
     * Bestimmt, nach wievielen Runden neu entstandene Regionen für "alte"
     * Parteien sichtbar werden - d.h. Parteien, die vor der Region entstanden
     * sind.
     */
    public final static String REGIONEN_SICHTBAR_NACH_RUNDEN = "game.option.regionenSichtbarNachRunden";
	public final static String TERRAIN_UNSICHTBARER_REGIONEN = "Chaos";
	
	/**
	 * Wenn 0, wird die Unterwelt nicht berücksichtigt.
	 * Wenn != 0, wird die Unterwelt ggf. erzeugt und verarbeitet
	 */
	public final static String UNTERWELT_AKTIV = "game.option.unterwelt.hauptschalter";
	
	/**
	 * enthält den aktuellen Modus des Spiel
	 * (z.Z.) mögliche Werte 'normal' bzw. 'skirmish'
	 */
	public final static String GAME_MODE = "game.mode";
    
    
    /**
     * <p>Name eines Regions-Property's - es wird noch die ID (Base36) der jeweiligen Partei angehängt.</p>
     * <p>Die Insel-ID bleibt permanent für jeweils eine Partei erhalten!</p>
     * <p>Siehe BENENNE INSEL ...</p>
     */
    public final static String INSEL_ID_FUER_PARTEI = "insel.id.";
    
    /**
     * <p>Runde, in der die Partei zum ersten Mal eine eigene ID (einen eigenen 
     * ParteiInselAnker) für diese Insel festgelegt hat.</p>
     */
    public final static String INSEL_ENTDECKUNG_FUER_PARTEI = "insel.entdeckungsrunde.";
    
    /**
     * Name eines Regions-Property's - es wird noch die ID (Base36) der jeweiligen Partei angehängt.
     * Siehe BENENNE INSEL ...
     */
    public final static String INSELNAME_FUER_PARTEI = "insel.name.";
    
    /**
     * Name eines Regions-Property's - es wird noch die ID (Base36) der jeweiligen Partei angehängt.
     * Siehe BESCHREIBE INSEL ...
     */
    public final static String INSELBESCHREIBUNG_FUER_PARTEI = "insel.beschreibung.";

    

	private final static Map<String, String> Options = new HashMap<String, String>();

	public static void Init() {
		Datenbank db = new Datenbank("GameRules");
		db.ClearSettings(GameRules.GAME_RUNDE);
		db.ClearSettings(GameRules.GAME_JAHRESZEIT_VERSCHIEBUNG);
		db.ClearSettings(GameRules.GAME_MODE);
		db.ClearSettings(GameRules.INSELKENNUNG_WELT);
		db.ClearSettings(GameRules.INSELKENNUNG_SPIELER);
		db.ClearSettings(GameRules.INSELKENNUNG_ERZEUGUNG);
		db.ClearSettings(GameRules.NEUE_INSEL_METHODE);
		db.ClearSettings(GameRules.REGIONEN_SICHTBAR_NACH_RUNDEN);
		db.ClearSettings(GameRules.UNTERWELT_AKTIV);
		db.Close();
	}

	public static void Load()
	{
		Datenbank db = new Datenbank("GameRules");
		setRunde(db.ReadSettings(GameRules.GAME_RUNDE, 1));
		setVerschiebung(db.ReadSettings(GameRules.GAME_JAHRESZEIT_VERSCHIEBUNG, 0));
		setInselkennungWelt(db.ReadSettings(GameRules.INSELKENNUNG_WELT, 0));
		setInselkennungErzeugung(db.ReadSettings(GameRules.INSELKENNUNG_ERZEUGUNG, 0));
		setInselkennungSpieler(db.ReadSettings(GameRules.INSELKENNUNG_SPIELER, 0));
		setGameMode(db.ReadSettings(GameRules.GAME_MODE, "normal"));
		new SysMsg("Ausgangsrunde: " + getRunde());
        new SysMsg("Jahreszeit: " + new String[]{ "Winter", "Winter",
				"Frühling", "Frühling", "Frühling",
				"Sommer", "Sommer", "Sommer",
				"Herbst", "Herbst", "Herbst",
				"Winter" }[getJahreszeit()] + " - " + getJahreszeit() );
		
		Options.put(GameRules.NEUE_INSEL_METHODE, db.ReadSettings(GameRules.NEUE_INSEL_METHODE, "classic"));
		// klassisch: "classic" als Default,
		// mit dem Insel-Puzzle-Konzept: "April2011"
		
		Options.put(
                GameRules.REGIONEN_SICHTBAR_NACH_RUNDEN,
                db.ReadSettings(
                    GameRules.REGIONEN_SICHTBAR_NACH_RUNDEN,
                    Integer.toString(GetRegionenSichtbarNachRundenDefault())
                )
        );
		
		Options.put(GameRules.UNTERWELT_AKTIV, db.ReadSettings(GameRules.UNTERWELT_AKTIV, "0"));

		db.Close();
	}
	
	private static void setGameMode(String value) {
		gamemode = value;
	}

	public static void Save()
	{
		Datenbank db = new Datenbank("GameRules");
		db.SaveSettings(GameRules.GAME_RUNDE, getRunde());
		db.SaveSettings(GameRules.GAME_JAHRESZEIT_VERSCHIEBUNG, getVerschiebung());
		db.SaveSettings(GameRules.INSELKENNUNG_WELT, getInselkennungWelt());
		db.SaveSettings(GameRules.INSELKENNUNG_ERZEUGUNG, getInselkennungErzeugung());
		db.SaveSettings(GameRules.INSELKENNUNG_SPIELER, getInselkennungSpieler());
		
		for (String k : Options.keySet()) {
			db.SaveSettings(k, Options.get(k));
		}
		db.Close();
	}

	public static String GetOption(String key) {
		if (Options.containsKey(key)) return Options.get(key);
		return null;
	}

	public static void SetOption(String key, String value) {
		if (Options.containsKey(key)) {
			new SysMsg("GameRules.SetOption('" + key + "','" + value + "') überschreibt alten Wert '" + Options.get(key) + "'.");
		} else {
			new SysMsg("GameRules.SetOption('" + key + "','" + value + "') - vorher undefinierter Optionswert.");
		}
		Options.put(key, value);
	}
	
	/** aktuelle Spielrunde */
	private static int runde = 0;
	public static int getRunde() { return runde; }
	public static void setRunde(int value) { runde = value; }
	
	/** die Verschiebung der Jahreszeit in Monaten */
	private static int verschiebung = 0;
	/** setzt die Verschiebung der Jahreszeit in Monaten */
	public static void setVerschiebung(int value) { verschiebung = value; }
	/** liest die Verschiebung der Jahreszeit in Monaten */
	public static int getVerschiebung() { return verschiebung; }
	
	private static String gamemode = "normal";
	public static boolean isNormal() {
		return !isSkirmish();
	}
	public static boolean isSkirmish() {
		return gamemode.toLowerCase().equals("skirmish");
	}
	
	/**
	 * liefert den Monat der Jahreszeit ... also
	 * <table border="0">
	 * <tr><td align="center">0 &amp; 1</td><td>&nbsp;</td><td>Winter</td></tr>
	 * <tr><td align="center">2 - 3 - 4</td><td>&nbsp;</td><td>Fr&uuml;hling</td></tr>
	 * <tr><td align="center">5 - 6 - 7</td><td>&nbsp;</td><td>Sommer</td></tr>
	 * <tr><td align="center">8 - 9 - 10</td><td>&nbsp;</td><td>Herbst</td></tr>
	 * <tr><td align="center">11</td><td>&nbsp;</td><td>Winter</td></tr>
	 * </table>
	 * <br><br><i>die Verschiebung wird am Ende von Environment gesetzt</i>
	 * @return die reale Jahreszeit bzw. Monat
	 */
	public static int getJahreszeit() { return (runde + verschiebung) % 12; }

	public static int getJahr() { return (runde + verschiebung) / 12 + 1; }
	
	// liefert das Quartal des Jahres Winter, Frühling, Summer, Herbst
	public static int getQuartal()
	{
		int jahreszeit = getJahreszeit();
		// das Quartal beginnt im Dezember ... das ist Winter - dann noch Januar und Februar
		jahreszeit++; if (jahreszeit > 11) jahreszeit -= 11;
		int q = jahreszeit / 3;
		return q; 
	}
	public static boolean isSpring() { return getQuartal() == 1; }
	public static boolean isSummer() { return getQuartal() == 2; }
	public static boolean isAutumn() { return getQuartal() == 3; }
	public static boolean isWinter() { return getQuartal() == 0; }
	
	/** Spieler werden auf dieser Insel ausgesetzt */
	private static int inselkennungWelt = 0;
	public static int getInselkennungWelt() { return inselkennungWelt; }
	public static void setInselkennungWelt(int value) { inselkennungWelt = value; }
	
	/** Spieler werden auf dieser Insel ausgesetzt */
	private static int inselkennungspieler = 0;
	public static int getInselkennungSpieler() { return inselkennungspieler; }
	public static void setInselkennungSpieler(int value) { inselkennungspieler = value; }

	/** mit dieser Nummer wird die nächste Insel erzeugt */
	private static int inselkennungErzeugung = 0;
	public static int getInselkennungErzeugung() { return inselkennungErzeugung; }
	public static void setInselkennungErzeugung(int value) { inselkennungErzeugung = value; }

    public static int GetRegionenSichtbarNachRundenDefault() { return 24; }
    /**
     * Bestimmt, nach wievielen Runden neu entstandene Regionen für "alte"
     * Parteien sichtbar werden - d.h. Parteien, die vor der Region entstanden
     * sind.
     * @return Mindestalter einer Region, bevor sie von älteren Parteien gesehen und betreten werden kann.
     */
    public static int GetRegionenSichtbarNachRunden() {
        if (!Options.containsKey(GameRules.REGIONEN_SICHTBAR_NACH_RUNDEN)) return GetRegionenSichtbarNachRundenDefault();
        return Integer.parseInt(Options.get(GameRules.REGIONEN_SICHTBAR_NACH_RUNDEN));
    }

	/**
	 * Kann und soll benutzt werden, wenn neue Regionen angelegt werden (oder Regionstypen geändert werden).
	 * @return Eine Menge aller Terrains, die in der Oberwelt vorkommen (sollten).
	 */
	public static Set<Class<? extends Region>> OberWeltTerrains() {
		Set<Class<? extends Region>> retval = new HashSet<Class<? extends Region>>();

		retval.add(Ebene.class);
		retval.add(Wald.class);
		retval.add(Sumpf.class);
		retval.add(Hochland.class);
		retval.add(Gletscher.class);
		retval.add(Berge.class);
		retval.add(Wueste.class);

		retval.add(Ozean.class);

		return retval;
	}

	/**
	 * Kann und soll benutzt werden, wenn neue Regionen angelegt werden (oder Regionstypen geändert werden).
	 * @return Eine Menge aller Terrains, die in der <em>Unterwelt</em> vorkommen (sollten).
	 */
	public static Set<Class<? extends Region>> UnterWeltTerrains() {
		Set<Class<? extends Region>> retval = new HashSet<Class<? extends Region>>();

		retval.add(Trockenwald.class);
		retval.add(Oedland.class);
		retval.add(Geroellebene.class);
		retval.add(Moor.class);
		retval.add(Wueste.class);
		retval.add(Vulkan.class);
		retval.add(aktiverVulkan.class);

		retval.add(Lavastrom.class);
		retval.add(Sandstrom.class);

		return retval;
	}


	
	/** Rules für Monster */
	public static class Monster
	{
		/** dark - Rules für einfache Monster der Oberwelt */
		public static class DARK
		{
			/** Rules für Kraken */
			public static class Krake
			{
				/** Maximum an Kraken pro Insel <b>beim</b> Aussetzen */ 
				public static int SpawnMax()	{ return 1; }
				/** maximale Anzahl der Personen beim Aussetzen */
				public static int PersonsMax()	{ return 4; }
				/** minimale Anzahl der Personen beim Aussetzen */
				public static int PersonMin()	{ return 1; }
				/** maximale Zufallszahl beim Auswürfeln eines Angriffes */
				public static int AttackMax()	{ return 100; }
				/** alles unterhalt dieses Wertes bedeutet Angriff */
				public static int AttackMin()	{ return 20; }
			}
		}
		
		/** tier - Rules für normale Tiere */
		public static class TIER
		{
			/** Rules für Greif */
			public static class Greif
			{
				/** Maximum an Einheiten pro Insel beim Spawn */
				public static int SpawnMax() { return 1; }
				/** Anzahl der min. Personen beim Spawn */
				public static int PersonsMin() { return 3; }
				/** Anzahl der max. Personen beim Spawn */
				public static int PersonsMax() { return 10; }
				/** ab sovielen Personen beginnt eine Abspaltung */
				public static int FractionStart() { return 20; }
				/** soviele Personen (int Prozent) spalten sich ab */ 
				public static int FractionPercent() { return 20; }
				/** Waffentalent beim Spawn */
				public static int SpeerkampfMin() { return 840; }
				/** Waffentalent beim Spawn */
				public static int SpeerkampfMax() { return 1650; } 
				/** Waffentalent beim Spawn */
				public static int HiebwaffenMin() { return 840; }
				/** Waffentalent beim Spawn */
				public static int HiebwaffenMax() { return 1650; } 
				/** Waffentalent beim Spawn */
				public static int WahrnehmungMin() { return 450; }
				/** Waffentalent beim Spawn */
				public static int WahrnehmungMax() { return 1080; }
				/** soviele Eier schlüpfen max. im Herbst */
				public static int Eclosion() { return 3; }
			}
		
			/** Rules für Libelle */
			public static class DragonFly
			{
				/** soviele Spawnen mindestens */
				public static int PersonsMin()	{ return 10; }
				/** soviele Spawnen maximal */
				public static int PersonsMax()	{ return 100; }
				/** Wachstum in den Jahreszeiten (Winter/Frühling/Sommer/Herbst) */ 
				public static float[] Growing() { return new float[] { 0.0f, 1.0f, 3.0f, 0.5f }; }
				/** Sterberate in den Jahreszeiten */
				public static float[] Decease() { return new float[] { 3.5f, 0.5f, 0.0f, 0.5f }; }
				/** ab sovielen Personen beginnt eine Abspaltung */
				public static int FractionStart() { return 300; }
				/** soviele Personen (int Prozent) spalten sich ab */ 
				public static int FractionPercent() { return 35; }
			}
			
			/** Rules für einen Bären */
			public static class Puschkin {
				/** mit welcher Wahrscheinlichkeit ein Bär spawnt */
				public static int SpawnPercent() { return 10; }
				/** wieviel Runden der Bär max. existiert */
				public static int getLifemax() { return 5; }
				/** die Wahrscheinlichkeit das der Bär angreift */
				public static int AttackPercent() { return 10; }
			}
		}
	}

}
