package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.EVA.Belagerung;
import de.x8bit.Fantasya.Host.EVA.EVABase;
import de.x8bit.Fantasya.Host.EVA.Kriege;
import de.x8bit.Fantasya.Host.EVA.BefehlsCheckMeldungen;
import de.x8bit.Fantasya.Host.EVA.BefehlsFazit;
import de.x8bit.Fantasya.Host.EVA.Reporte;
import de.x8bit.Fantasya.Host.Main;
import java.util.HashSet;
import java.util.Set;

/**
 * Kapselt Voreinstellungen und Optionen für die Durchführung eines ZAT -
 * beispielsweise Debug-Modus, Read-Only-Modus (Simulation)...
 * @author hb
 */
public class ZATMode {
	String name = null;

    boolean debug;

    boolean imapAbrufen;
    boolean befehleLesen;
	boolean befehlsCheck;
    boolean worldReadOnly;
    String sendMailMethode;

    Set<Partei> ignoreParteien;
    Set<Class<? extends EVABase>> skipPhasen;

	private static ZATMode currentMode;

	public static ZATMode CurrentMode() {
		if (currentMode == null) {
			currentMode = ZATMode.MODE_NORMAL();
		}
		return currentMode;
	}

	public static void SetCurrentMode(ZATMode zatMode) {
		currentMode = zatMode;
	}

    /**
	 * Dieser Konstruktor sollte den "ganz normalen" ZAT beschreiben, der im 
	 * echten Spielablauf zur Auswertung dient.
	 */
	public ZATMode() {
        this.debug = false;
        if (Main.getBFlag("debug") || Main.getBFlag("DEBUG")) this.debug = true;

        this.ignoreParteien = new HashSet<Partei>();
        this.skipPhasen = new HashSet<Class<? extends EVABase>>();
        this.skipPhasen.add(BefehlsCheckMeldungen.class);

        this.imapAbrufen = false; // der offizielle Host bekommt die Mails auf anderem Weg
        this.befehleLesen = true;
		this.befehlsCheck = false;

        this.worldReadOnly = false;

        this.sendMailMethode = "classic";
    }

    public void setSkip(Class<? extends EVABase> phase) {
        this.skipPhasen.add(phase);
    }

    public void dontSkip(Class<? extends EVABase> phase) {
        this.skipPhasen.remove(phase);
    }

    public boolean getSkip(Class<? extends EVABase> phase) {
        if (this.skipPhasen.contains(phase)) return true;
        return false;
    }

    public void setIgnore(Partei p) {
        this.ignoreParteien.add(p);
    }

    public boolean getIgnore(Partei p) {
        if (this.ignoreParteien.contains(p)) return true;
        return false;
    }

	public boolean hatIgnorierteParteien() {
		return (!this.ignoreParteien.isEmpty());
	}

	public Set<Integer> getIgnorierteParteiNummern() {
		Set<Integer> retval = new HashSet<Integer>();
		for (Partei p : this.ignoreParteien) {
			retval.add(p.getNummer());
		}
		return retval;
	}

    public boolean isImapAbrufen() {
        return imapAbrufen;
    }

    public void setImapAbrufen(boolean imapAbrufen) {
        this.imapAbrufen = imapAbrufen;
    }

    public boolean isBefehleLesen() {
        return befehleLesen;
    }

	public boolean isBefehlsCheck() {
		return befehlsCheck;
	}

	public void setBefehlsCheck(boolean befehlsCheck) {
		this.befehlsCheck = befehlsCheck;
	}

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getSendMailMethode() {
        return sendMailMethode;
    }

    public boolean isWorldReadOnly() {
        return worldReadOnly;
    }

    public void setWorldReadOnly(boolean worldReadOnly) {
        this.worldReadOnly = worldReadOnly;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getName() != null) sb.append("NAME: " + this.getName() + "\n");
		sb.append("Debug-Modus:                " + (this.isDebug()?"an":"aus") + "\n");
		sb.append("IMAP-Mails holen:           " + (this.isImapAbrufen()?"ja":"nein") + "\n");
		sb.append("Befehle einlesen:           " + (this.isBefehleLesen()?"ja":"nein") + "\n");
		sb.append("Testlauf (nicht Speichern): " + (this.isWorldReadOnly()?"ja":"nein") + "\n");
		if (this.skipPhasen.size() > 0) {
			sb.append("Es werden ZAT-Phasen übersprungen: ");
			int i = 0;
			for (Class<? extends EVABase> clazz : this.skipPhasen) {
				if (i > 0) sb.append(", ");
				sb.append(clazz.getSimpleName());
				i++;
			}
			sb.append(".\n");
		}
		if (this.ignoreParteien.size() > 0) {
			sb.append("Es werden Parteien ignoriert: ");
			int i = 0;
			for (Partei p : this.ignoreParteien) {
				if (i > 0) sb.append(", ");
				sb.append(p.toString());
				i++;
			}
			sb.append(".\n");
		}

		return sb.toString();
	}

    public static ZATMode MODE_BEFEHLE_CHECKEN() {
        ZATMode retval = new ZATMode();
        
        retval.setBefehlsCheck(true);
		retval.setWorldReadOnly(true);

        // TODO
        // retval.setSkip(NeueSpieler.class);
        retval.setSkip(Kriege.class);
        retval.setSkip(Belagerung.class);
        retval.setSkip(Reporte.class);
		retval.setSkip(BefehlsFazit.class);

        retval.dontSkip(BefehlsCheckMeldungen.class);

//        for (Partei maybe : Partei.PROXY) {
////            if (p != null) {
////                if (maybe.getNummer() == p.getNummer()) continue;
////            }
//            // TODO
//            // retval.setIgnore(maybe);
//        }

        retval.setName("Befehle checken.");
		return retval;
    }

    public static ZATMode MODE_NORMAL() {
        ZATMode retval = new ZATMode();
		retval.setName("Normaler ZAT");
        return retval;
    }

}
