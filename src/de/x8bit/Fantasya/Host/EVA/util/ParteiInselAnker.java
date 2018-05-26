package de.x8bit.Fantasya.Host.EVA.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung.Insel;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.StringUtils;

/**
 * <p>Kapselt die Informationen einer Partei über eine Insel. </p>
 * <p>Ein ParteiInselAnker wird an einer Region "verankert", falls sich 
 * Inselstrukturen durch Terraforming etc. ändern sollten, ist durch die Wahl 
 * dieser Region definiert, für welchen ggf. anderen Bereich der Anker nun 
 * gilt.</p>
 * <p>ParteiInselAnker hat viel mit den Befehlen BENENNE INSEL ... und 
 * BESCHREIBE INSEL ... zu tun; die ID wird für den CR verwendet.
 * 
 * @author hapebe
 */
public class ParteiInselAnker {
    final int parteiNr;
    
    Coords anker;
    
    int privateInselId = -1;
    int entdeckungsRunde = -1;
    String privaterInselname;
    String privateInselbeschreibung;

    public ParteiInselAnker(int parteiNr) {
        this.parteiNr = parteiNr;
    }
    
    public static void Entfernen(Partei p, Coords c) {
        Region r = Region.Load(c);
        r.removeProperty(GameRules.INSEL_ID_FUER_PARTEI + Codierung.toBase36(p.getNummer()));
        r.removeProperty(GameRules.INSEL_ENTDECKUNG_FUER_PARTEI + Codierung.toBase36(p.getNummer()));
        r.removeProperty(GameRules.INSELNAME_FUER_PARTEI + Codierung.toBase36(p.getNummer()));
        r.removeProperty(GameRules.INSELBESCHREIBUNG_FUER_PARTEI + Codierung.toBase36(p.getNummer()));
    }
    
    public static void BenenneInsel(Unit u, String name) {
        BenenneInsel(Partei.getPartei(u.getOwner()), u.getCoords(), name);
    }
    
    public static void BeschreibeInsel(Unit u, String beschr) {
        BeschreibeInsel(Partei.getPartei(u.getOwner()), u.getCoords(), beschr);
    }
    
    public static void BenenneInsel(Partei p, Coords c, String name) {
        ParteiInselAnker pia = FindOrCreateFor(p, c);
        
        // alten Anker lichten!
        ParteiInselAnker.Entfernen(p, pia.getAnker());
        
        // den Anker hierher verlegen:
        pia.setAnker(c);
        pia.setPrivaterInselname(name);
        
        // neuen Anker speichern:
        pia.saveToRegion();
        
        // Insel-Anker auffrischen:
        ParteiInselAnker.Initialisieren(p);
    }
    
    public static void BeschreibeInsel(Partei p, Coords c, String beschr) {
        ParteiInselAnker pia = FindOrCreateFor(p, c);
        
        // alten Anker lichten!
        ParteiInselAnker.Entfernen(p, pia.getAnker());
        
        // den Anker hierher verlegen:
        pia.setAnker(c);
        pia.setPrivateInselbeschreibung(beschr);
        
        // neuen Anker speichern:
        pia.saveToRegion();
        
        // Insel-Anker auffrischen:
        ParteiInselAnker.Initialisieren(p);
    }
    
    public static ParteiInselAnker FindOrCreateFor(Partei p, Coords c) {
        if (c == null) return null;
        Region r = Region.Load(c);
        if (r == null) return null;
        if (r instanceof Chaos) throw new IllegalStateException("Es soll ein ParteiInselAnker im Chaos gesetzt werden - " + p + " bei " + c);
        if (r instanceof Ozean) throw new IllegalStateException("Es soll ein ParteiInselAnker auf dem Wasser gesetzt werden - " + p + " bei " + c);
        
        InselVerwaltung iv = InselVerwaltung.getInstance();
        int publicInselId = iv.getInselNummer(c);
        Set<Coords> inselCoords = iv.getInselCoords(publicInselId);
        
        // alle existierenden berücksichtigen ...
        boolean gefunden = false;
        ParteiInselAnker pia = new ParteiInselAnker(p.getNummer());
        pia.setAnker(c);
        pia.setPrivateInselId(Integer.MIN_VALUE);
        pia.setEntdeckungsRunde(Integer.MAX_VALUE);
        SortedMap<Integer, String> namen = new TreeMap<Integer, String>();
        SortedMap<Integer, String> beschreibungen = new TreeMap<Integer, String>();
        Set<Coords> alteAnker = new HashSet<Coords>();
        
        for (Coords alteAnkerC : p.getInselAnker().keySet()) {
            if (inselCoords.contains(alteAnkerC)) {
                gefunden = true;
                alteAnker.add(alteAnkerC);
                
                ParteiInselAnker alterAnker = new ParteiInselAnker(p.getNummer());
                alterAnker.loadFromRegion(alteAnkerC);
                
                int alteID = alterAnker.getPrivateInselId();
                int alteEntdeckungsRunde = alterAnker.getEntdeckungsRunde();
                
                // wenn die alte ID höher ist: übernehmen
                if (alteID > pia.getPrivateInselId()) {
                    pia.setPrivateInselId(alteID);
                    pia.setAnker(alterAnker.getAnker());
                }
                
                // wenn das der älteste Anker ist: sein Entdeckungsdatum nehmen
                if (alterAnker.getEntdeckungsRunde() < pia.getEntdeckungsRunde()) {
                    pia.setEntdeckungsRunde(alteEntdeckungsRunde);
                }
                
                // Name & Beschreibung aufnehmen:
                namen.put(alteEntdeckungsRunde * 10000 + alteID, alterAnker.getPrivaterInselname());
                beschreibungen.put(alteEntdeckungsRunde * 10000 + alteID, alterAnker.getPrivateInselbeschreibung());
            }
        }
        if (gefunden) {
            List<String> tokens = new ArrayList<String>();
            for (int sortierWert : namen.keySet()) {
                String name = namen.get(sortierWert);
                if (name != null) tokens.add(name);
            }
            pia.setPrivaterInselname(StringUtils.join(tokens, "-"));
            
            tokens = new ArrayList<String>();
            for (int sortierWert : beschreibungen.keySet()) {
                String beschr = beschreibungen.get(sortierWert);
                if (beschr != null) tokens.add(beschr);
            }
            pia.setPrivateInselbeschreibung(StringUtils.join(tokens, "; "));
            
            if (alteAnker.size() > 1) {
                // au Backe - mehr als ein alter Anker auf der gleichen Insel:
                // alle weg:
                for (Coords alteC : alteAnker) {
                    ParteiInselAnker.Entfernen(p, alteC);
                }
                // den neuen speichern:
                pia.setAnker(c); // war vorher eine der alten Koordinaten
                pia.saveToRegion();
                
                // auffrischen:
                ParteiInselAnker.Initialisieren(p);
            }
            
            return pia;
        }
        
        // nichts gefunden - muss angelegt werden:
        ParteiInselAnker.Initialisieren(p); // Insel-Anker auffrischen
        int freieID = ParteiInselAnker.FreiePrivateInselID(p.getNummer()); // für alle Fälle...
        
        pia.setPrivateInselId(freieID);
        // Partei 0 sieht stattdessen immer die "öffentlichen" IDs:
        if (p.getNummer() == 0) pia.setPrivateInselId(publicInselId);
        pia.setEntdeckungsRunde(GameRules.getRunde());
        pia.setPrivaterInselname(null);
        pia.setPrivateInselbeschreibung(null);
        // den neuen speichern:
        pia.saveToRegion();
        
        // und nochmal auffrischen:
        ParteiInselAnker.Initialisieren(p);

        return pia;
    }
    
    public static ParteiInselAnker FindOrCreateFor(Unit u) {
        return FindOrCreateFor(Partei.getPartei(u.getOwner()), u.getCoords());
    }
    
    public static int FreiePrivateInselID(int parteiNr) {
        int retval = 1; // 1 ist die erste ID
        
        Partei p = Partei.getPartei(parteiNr);
        for (Coords c : p.getInselAnker().keySet()) {
            ParteiInselAnker pia = p.getInselAnker().get(c);
            if (pia.getPrivateInselId() >= retval) retval = pia.getPrivateInselId() + 1;
        }
        
        return retval;
    }
    
    public static void Initialisieren(Partei p) {
        p.getInselAnker().clear();
        
        String idKey = GameRules.INSEL_ID_FUER_PARTEI + Codierung.toBase36(p.getNummer());
        String rundeKey = GameRules.INSEL_ENTDECKUNG_FUER_PARTEI + Codierung.toBase36(p.getNummer());
        String nameKey = GameRules.INSELNAME_FUER_PARTEI + Codierung.toBase36(p.getNummer());
        String beschrKey = GameRules.INSELBESCHREIBUNG_FUER_PARTEI + Codierung.toBase36(p.getNummer());

        InselVerwaltung iv = InselVerwaltung.getInstance();
        
        for (int publicInselId : iv.getExistierendeInseln(InselVerwaltung.TYP_LAND)) {
            Insel insel = iv.getInsel(publicInselId);
            
            ParteiInselAnker pia = new ParteiInselAnker(p.getNummer());
            pia.setEntdeckungsRunde(Integer.MAX_VALUE);
            Coords found = null;
            for (Coords c : insel.getCoords()) {
                Region r = Region.Load(c);
                
                int privateInselID = -1;
                try {
                    if (r.hasProperty(idKey)) {
						privateInselID = r.getIntegerProperty(idKey);
					}
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("Partei " + p + ", Region " + r + " (" + r.getCoords() + ")...", ex);
                }
                if (privateInselID != -1) {
                    // gotcha!

					if (r.hasProperty(rundeKey)) {
	                    int entdeckung = r.getIntegerProperty(rundeKey);
                        if (entdeckung < pia.getEntdeckungsRunde()) {
                            // entweder der erste oder ältere Eintrag auf dieser Insel:
                            found = c;
                            
                            pia.setPrivateInselId(privateInselID);
                            // Partei 0 sieht stattdessen immer die "öffentlichen" IDs:
                            if (p.getNummer() == 0) pia.setPrivateInselId(publicInselId); 
                            pia.setEntdeckungsRunde(entdeckung);
                            pia.setPrivaterInselname(r.getStringProperty(nameKey, null));
                            pia.setPrivateInselbeschreibung(r.getStringProperty(beschrKey, null));
                            
                        }
                    }
                }
            }
            
            if (found != null) p.getInselAnker().put(found, pia);
        } // nächste "öffentliche" (globale Karte) Insel
        
        if (ZATMode.CurrentMode().isDebug()) new SysMsg("ParteiInselAnker.Initialisieren(" + p + "): " + p.getInselAnker().size() + " Anker.");
    }


    public void saveToRegion() {
        if (getAnker() == null) throw new IllegalStateException("ParteiInselAnker #" + getPrivateInselId() + " soll gespeichert werden, gehört aber zu keiner Region.");
        
        // new Debug("PIA.saveToRegion(): " + Partei.Load(getParteiNr()) + "@" + Region.Load(getAnker()) + " " + getAnker() + " - ID " + getPrivateInselId() + ", " + getPrivaterInselname() + ", " + getPrivateInselbeschreibung());
        
        String idKey = GameRules.INSEL_ID_FUER_PARTEI + Codierung.toBase36(parteiNr);
        String rundeKey = GameRules.INSEL_ENTDECKUNG_FUER_PARTEI + Codierung.toBase36(parteiNr);
        String nameKey = GameRules.INSELNAME_FUER_PARTEI + Codierung.toBase36(parteiNr);
        String beschrKey = GameRules.INSELBESCHREIBUNG_FUER_PARTEI + Codierung.toBase36(parteiNr);
        
        Region r = Region.Load(getAnker());

        r.setProperty(idKey, getPrivateInselId());
        r.setProperty(rundeKey, getEntdeckungsRunde());
		if (getPrivaterInselname() != null) {
	        r.setProperty(nameKey, getPrivaterInselname());
		}
		if (getPrivateInselbeschreibung() != null) {
	        r.setProperty(beschrKey, getPrivateInselbeschreibung());
		}
    }
    
    public void loadFromRegion(Coords anker) {
        if (anker == null) throw new NullPointerException("anker ist null.");
        
        String idKey = GameRules.INSEL_ID_FUER_PARTEI + Codierung.toBase36(parteiNr);
        String rundeKey = GameRules.INSEL_ENTDECKUNG_FUER_PARTEI + Codierung.toBase36(parteiNr);
        String nameKey = GameRules.INSELNAME_FUER_PARTEI + Codierung.toBase36(parteiNr);
        String beschrKey = GameRules.INSELBESCHREIBUNG_FUER_PARTEI + Codierung.toBase36(parteiNr);
        
        setAnker(anker);
        
        Region r = Region.Load(getAnker());
        
        if (!r.hasProperty(idKey)){
			throw new IllegalStateException("ParteiInselAnker von [" + Codierung.toBase36(parteiNr) + "] soll von " + anker + " geladen werden, aber da ist keine private Insel-ID zu finden.");
		}
        setPrivateInselId(r.getIntegerProperty(idKey));
        
        if (r.hasProperty(rundeKey)) {
            setEntdeckungsRunde(r.getIntegerProperty(rundeKey));
        } else {
            setEntdeckungsRunde(GameRules.getRunde());
        }
        
        setPrivaterInselname(r.getStringProperty(nameKey, null));
        setPrivateInselbeschreibung(r.getStringProperty(beschrKey, null));
    }

    public int getParteiNr() {
        return parteiNr;
    }
    
    public Coords getAnker() {
        return anker;
    }

    public void setAnker(Coords anker) {
        this.anker = anker;
    }

    public int getEntdeckungsRunde() {
        return entdeckungsRunde;
    }

    public void setEntdeckungsRunde(int entdeckungsRunde) {
        this.entdeckungsRunde = entdeckungsRunde;
    }

    public int getPrivateInselId() {
        return privateInselId;
    }

    public void setPrivateInselId(int privateInselId) {
        this.privateInselId = privateInselId;
    }

    public String getPrivateInselbeschreibung() {
        return privateInselbeschreibung;
    }

    public void setPrivateInselbeschreibung(String privateInselbeschreibung) {
        this.privateInselbeschreibung = privateInselbeschreibung;
    }

    public String getPrivaterInselname() {
        return privaterInselname;
    }

    public void setPrivaterInselname(String privaterInselname) {
        this.privaterInselname = privaterInselname;
    }
    
    @Override
    public String toString() {
        return "Insel-Anker für " + Partei.getPartei(getParteiNr()) + ": "
                + Region.Load(getAnker()) + " " + getAnker() + " - "
                + " ID " + getPrivateInselId() + "p/" + InselVerwaltung.getInstance().getInselNummer(getAnker()) + ", "
                + " entdeckt in " + getEntdeckungsRunde() + ", "
                + " Name: " + getPrivaterInselname() + ", "
                + " Beschreibung: " + getPrivateInselbeschreibung() + ".";
    }
    
    
}
