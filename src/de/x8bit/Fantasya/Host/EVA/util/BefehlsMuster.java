package de.x8bit.Fantasya.Host.EVA.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.EVABase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class BefehlsMuster {

	public static final Map<String, Class<? extends Spell>> SPELL_KEY = new TreeMap<String, Class<? extends Spell>>();
	static {
		for (Paket p : Paket.getPaket("Spells")) {
			Spell spell = (Spell)p.Klasse;
            String originalName = spell.getName().toLowerCase();
			SPELL_KEY.put(originalName, spell.getClass());
            
            String asciiName = originalName.replaceAll("ä", "ae");
            asciiName = asciiName.replaceAll("ö", "oe");
            asciiName = asciiName.replaceAll("ü", "ue");
            asciiName = asciiName.replaceAll("ß", "ss");
            
            if (!originalName.equals(asciiName)) SPELL_KEY.put(asciiName, spell.getClass());
		}
	}


    private final Class<? extends EVABase> prozessor;
    private final int variante;
    private final String regex;
    private final Pattern pattern;
    private final String erstesZeichen;
    private final Art art;

	private final List<ParamHint> hints = new ArrayList<ParamHint>();
    private final List<String> keywords = new ArrayList<String>();
	
	/** dieses Flag bestimmt, ob ein Befehl in die neue Befehlsvorlage übernommen wird ("langer Befehl") */
	private boolean keep = false;

    /**
	 * Standard-Konstruktor für die Verwendung mit den EVA-Klassen
	 * @param prozessor die zugeordnete EVA-Klasse, die den Befehl ausführen kann.
	 * @param variante Befehlsvariante - kann zur Fallunterscheidung verschiedener Syntax-Varianten genutzt werden. Standardmäßig 0.
	 * @param regex Das eigentliche Regular Expression Muster für diesen Befehl
	 * @param erstesZeichen Immer vorhandenes erstes Zeichen des Befehls. Wenn nicht feststehend, dann null.
	 * @param art kurz, lang, multi-lang - die Atlantis-Art des Befehls.
	 */
	public BefehlsMuster(Class<? extends EVABase> prozessor, int variante, String regex, String erstesZeichen, Art art) {
        this.prozessor = prozessor;
        this.variante = variante;
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.erstesZeichen = erstesZeichen;
        this.art = art;

		if (art == Art.KURZ) {
			this.keep = false;
		} else {
			this.keep = true;
		}
    }

    /**
	 * provisorischer Konstruktor ohne Angabe des Prozessors für die klassische ZAT-Architektur
	 */
	public BefehlsMuster(int variante, String regex, String erstesZeichen, Art art) {
        this.prozessor = null; // !!!
        this.variante = variante;
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.erstesZeichen = erstesZeichen;
        this.art = art;
    }

    public void setKeywords(String kwd) {
        keywords.clear();
        keywords.add(kwd);
    }

    public void setKeywords(String kwd1, String kwd2) {
        keywords.clear();
        keywords.add(kwd1);
        keywords.add(kwd2);
    }

    public void setKeywords(String kwd1, String kwd2, String kwd3) {
        keywords.clear();
        keywords.add(kwd1);
        keywords.add(kwd2);
        keywords.add(kwd3);
    }

    public void setKeywords(String kwd1, String kwd2, String kwd3, String kwd4) {
        keywords.clear();
        keywords.add(kwd1);
        keywords.add(kwd2);
        keywords.add(kwd3);
        keywords.add(kwd4);
    }

    public void setKeywords(String kwd1, String kwd2, String kwd3, String kwd4, String kwd5) {
        keywords.clear();
        keywords.add(kwd1);
        keywords.add(kwd2);
        keywords.add(kwd3);
        keywords.add(kwd4);
        keywords.add(kwd5);
    }

    /**
     * setzt die Liste aller Schlüsselwörter, die in diesem BefehlsMuster potentiell vorkommen können.
     * @param kwds
     */
    public void setKeywords(Collection<String> kwds) {
        keywords.clear();
        keywords.addAll(kwds);
    }

    /**
     * @return eine Liste aller für dieses BefehlsMuster registrierten Schlüsselwörter
     */
    public List<String> getKeywords() {
        return keywords;
    }

    public Class<? extends EVABase> getProzessor() {
        return prozessor;
    }

    public String getRegex() {
        return regex;
    }

    public Pattern getPattern() {
        return pattern;
    }
    
    public int getVariante() {
        return variante;
    }

    public String getErstesZeichen() {
        return erstesZeichen;
    }

    public Art getArt() {
        return art;
    }

	public void addHint(ParamHint hint) {
		this.hints.add(hint);
	}

	public List<ParamHint> getHints() {
		return hints;
	}

	public boolean isTempMuster() {
		return ((this.getVariante() & EVABase.TEMP) != 0);
	}

	/**
	 * @return true, wenn der Befehl in die neue Befehlsvorlage aufgenommen werden soll.
	 */
	public boolean isKeep() {
		return keep;
	}

	/**
	 * @param keep wenn true, wird der Befehl in die neue Befehlsvorlage aufgenommen.
	 */
	public void setKeep(boolean keep) {
		this.keep = keep;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("'" + getRegex() + "', " + getArt()+" ");
        if (getProzessor() != null) { 
            sb.append(" (" + getProzessor().getSimpleName());
            if (this.getVariante() != 0) sb.append("#" + this.getVariante());
            sb.append(")");
        }

        return sb.toString();
    }

    public enum Art {
       KURZ, MULTILANG, LANG
    }
}
