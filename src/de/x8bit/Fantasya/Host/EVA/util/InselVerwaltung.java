package de.x8bit.Fantasya.Host.EVA.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Cave;
import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Atlantis.Regions.Lavastrom;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Reports.util.EinflussKarte;
import de.x8bit.Fantasya.Host.Reports.util.RegionReportComparatorLNR;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.StringUtils;

/**
 *
 * @author hapebe
 */
public final class InselVerwaltung {
    
    public final static int TYP_LAND = 1;
    public final static int TYP_OZEAN = -1;
    public final static int TYP_LAVASTROM = -2;
    
    /**
     * für jede einzelne Region: Mapping Coords => "öffentliche" Insel-ID
     */
    Map<Coords, Integer> inselIds;
    
    /**
     * Datensätze von Inseln / Ozeanen / Lavaströmen
     */
    Map<Integer, Insel> inseln;
    
    /**
     * Reportdaten für alle Parteien - wird erst on demand erzeugt
     */
    Map<Partei, ParteiReportDaten> parteiReportDaten;
    
    private static InselVerwaltung instance;

    public static InselVerwaltung getInstance() { 
        if (instance == null) instance = new InselVerwaltung();
        return instance; 
    }
    
    private InselVerwaltung() {
        inselIds = new HashMap<Coords, Integer>();
        inseln = new HashMap<Integer, Insel>();
        parteiReportDaten = new HashMap<Partei, ParteiReportDaten>();
    }
    
    /**
     * @param p
     * @return ParteiReportDaten für die angegebene Partei, ggf. on demand neu berechnet
     */
    public ParteiReportDaten getParteiReportDaten(Partei p) {
        if (!parteiReportDaten.containsKey(p)) {
            parteiReportDaten.put(p, new ParteiReportDaten(p));
        }
        
        return parteiReportDaten.get(p);
    }
    
    public void karteVerarbeiten() {
        inselIds.clear();
        inseln.clear();
        parteiReportDaten.clear();
        
        new Debug("Inseln werden kartografiert...");
        
        int currentId = 1;
        for (Region r: Region.CACHE.values()) {
            Coords c = r.getCoords();
            if (c.getWelt() == 0) continue; // imaginäre Region...

            if (inselIds.get(c) != null) continue; // schon erfasst
            
            int modus = TYP_LAND;
            if (r instanceof Ozean) modus = TYP_OZEAN;
            if (r instanceof Lavastrom) modus = TYP_LAVASTROM;
            
            // new Debug("Modus @ " + c + " = " + modus);
            // eine noch nicht erfasste Region:
            boolean erkannt = markiereInsel(c, currentId, modus);

            if (erkannt) {
                // new Debug("...erkannt und als #" + currentId + " registriert.");
                inseln.put(currentId, new Insel(currentId, modus));
                currentId ++;
            }
        }
        
        for (int publicInselId : getExistierendeInseln()) {
            Insel i = inseln.get(publicInselId);
            
            i.setCoords(getInselCoords(publicInselId));
            i.setMittelpunkt(Coords.Mittelpunkt(i.getCoords()));
            
            for (Coords c : i.getCoords()) {
                for (Unit u : Unit.CACHE.getAll(c)) {
                    i.getEinheiten().add(u);
                }
            }
            
            Set<Integer> parteiNummern = new HashSet<Integer>();
            for (Unit u : i.getEinheiten()) {
                parteiNummern.add(u.getOwner());
            }
            for (int parteiNr : parteiNummern) {
                i.getParteien().add(Partei.getPartei(parteiNr));
            }
        }

        // verifizieren:
        for (int publicInselId : getExistierendeInseln()) {
            Insel i = inseln.get(publicInselId);
            for (Coords c : i.getCoords()) {
                Region r = Region.Load(c);

                if (i.getTyp() == TYP_LAND) {
                    if ((r instanceof Ozean) || (r instanceof Lavastrom)) {
                        throw new IllegalStateException("Insel #" + publicInselId + " ist eine Land-Insel, enthält aber Region " + r + " (" + r.getTyp() + ", " + c + "):");
                    }
                }
                if (i.getTyp() == TYP_OZEAN) {
                    if (!(r instanceof Ozean)) {
                        throw new IllegalStateException("Insel #" + publicInselId + " ist ein Meer, enthält aber Region " + r + " (" + r.getTyp() + ", " + c + "):");
                    }
                }
                if (i.getTyp() == TYP_LAVASTROM) {
                    if (!(r instanceof Lavastrom)) {
                        throw new IllegalStateException("Insel #" + publicInselId + " ist ein Lavastromg, enthält aber Region " + r + " (" + r.getTyp() + ", " + c + "):");
                    }
                }
            }
        }
        // Wenn wir hier ankommen, sind die Inseln geprüft und für gut befunden.

    }
    
    protected boolean markiereInsel(Coords c, int inselId, int modus) {
        Region hier = Region.Load(c);
        if (hier.getClass() == Chaos.class) return false;
        
        if (modus == TYP_LAND) {
            if (hier.getClass() == Ozean.class) return false;
            if (hier.getClass() == Lavastrom.class) return false;
        } else if (modus == TYP_OZEAN) {
            if (!(hier instanceof Ozean)) return false;
        } else if (modus == TYP_LAVASTROM) {
            if (hier.getClass() != Lavastrom.class) return false;
        }
        
        inselIds.put(c, inselId);
        
        for (Coords nachbar : c.getNachbarn()) {
            if (inselIds.get(nachbar) == null) {
                // eine noch nicht erfasste Region:
                markiereInsel(nachbar, inselId, modus);
            }
        }
        
        return true;
    }
    
    /**
     * @param c Koordinaten
     * @return "öffentliche" Insel-Nummer oder -1, falls es diese aus irgendeinem Grund nicht gibt.
     */
    public int getInselNummer(Coords c) {
    	if (inselIds == null) throw new NullPointerException("Es wird nach einer Insel-Nummer gefragt (" + c + "), aber die Inseln der Welt wurden noch nicht analysiert.");
        if (inselIds.get(c) == null) return -1;
        return inselIds.get(c);
    }
    
    
    public Set<Integer> getExistierendeInseln(int typ) {
        Set<Integer> retval = new HashSet<Integer>();
        
        for (int publicId : inseln.keySet()) {
            if (inseln.get(publicId).getTyp() == typ) retval.add(publicId);
        }

        return retval;
    }
    
    /**
     * @return alle bestehenden "öffentlichen" Insel-Nummern
     */
    public Set<Integer> getExistierendeInseln() {
        Set<Integer> retval = new HashSet<Integer>();
        for (Coords c : inselIds.keySet()) {
            retval.add(inselIds.get(c));
        }
        return retval;
    }
    
    /**
     * @param publicId
     * @return das Insel-Objekt oder null (falls es die Insel gar nicht gibt)
     */
    public Insel getInsel(int publicId) {
        return inseln.get(publicId);
    }
    
    /**
     * @param inselId
     * @return alle Koordinaten, die zur Insel Nr. inselId gehören
     */
    public Set<Coords> getInselCoords(int inselId) {
        Set<Coords> retval = new HashSet<Coords>();
        for (Coords c : inselIds.keySet()) {
            if (inselIds.get(c) == inselId) retval.add(c); 
        }
        return retval;
    }
    
    /**
     * @param p
     * @return "öffentliche" Insel-IDs, sofern sie für die Partei sichtbar sind.
     */
    public Set<Integer> getBekannteInselIds(Partei p) {
        if (p.getNummer() == 0) return getExistierendeInseln();
        
        Set<Integer> retval = new HashSet<Integer>();
        for (Insel i : inseln.values()) {
            if (i.getParteien().contains(p)) retval.add(i.getPublicId());
        }
        
        return retval;
    }
    
    /**
     * @param p
     * @param c
     * @return die private Insel-ID zu publicId, oder -1, falls es keine solche gibt
     */
    public int getPrivateInselNummer(Partei p, Coords c) {
        return getPrivateInselNummer(p, getInselNummer(c));
    }
    
    /**
     * @param p
     * @param publicId
     * @return die private Insel-ID zu publicId, oder -1, falls es keine solche gibt
     */
    public int getPrivateInselNummer(Partei p, int publicId) {
        if (p.getNummer() == 0) return publicId;
        
        if (getInsel(publicId) == null) throw new NullPointerException("Die globale Insel Nr. " + publicId + " ist nicht bekannt.");
        
        return getInsel(publicId).getPrivateNummer(p);
    }
    
    /**
     * @param p Partei
     * @param c Koordinaten
     * @return private Insel-Nummer an der gegebenen Koordinate - oder -1, falls es diese aus irgendeinem Grund nicht gibt.
     */
    public int getInselNummer(Partei p, Coords c) {
        if (inselIds.get(c) == null) return -1;
        
        int publicId = inselIds.get(c);
        
        return getInsel(publicId).getPrivateNummer(p);
    }
    
    /**
     * @param p
     * @param publicInselId
     * @return 
     */
    public String getInselName(Partei p, int publicInselId) {
        int privateInselId = getPrivateInselNummer(p, publicInselId);
        if (privateInselId == -1) return "n/a";
        
        Insel i = getInsel(publicInselId);
        String privaterName = i.getName(p);
        if (privaterName != null) return privaterName;
        
        if (i.getTyp() == TYP_OZEAN) return "Ozean";
        if (i.getTyp() == TYP_LAVASTROM) return "Lavastrom " + privateInselId;
        return "Insel " + privateInselId;
    }
    
    /**
     * @param p
     * @param publicInselId
     * @return 
     */
    public String getInselBeschreibung(Partei p, int publicInselId) {
        int privateInselId = getPrivateInselNummer(p, publicInselId);
        if (privateInselId == -1) return null;
        
        Insel i = getInsel(publicInselId);
        String privateBeschreibung = i.getBeschreibung(p);
        if (privateBeschreibung != null) return privateBeschreibung;

        return null;
    }
    
    /**
     * @param p
     * @param c
     * @param name 
     */
    public void setInselName(Partei p, Coords c, String name) {
        int publicInselId = getInselNummer(c);
        if (publicInselId == -1) return;
        
        Insel i = getInsel(publicInselId);
        
        i.setName(p, name);
    }
    
    /**
     * @param p
     * @param c
     * @param beschr 
     */
    public void setInselBeschreibung(Partei p, Coords c, String beschr) {
        int publicInselId = getInselNummer(c);
        if (publicInselId == -1) return;
        
        Insel i = getInsel(publicInselId);
        
        i.setBeschreibung(p, beschr);
    }
    
    /**
     * @param p
     * @param c
     * @return 
     */
    public String getInselName(Partei p, Coords c) {
        int publicInselId = getInselNummer(c);
        if (publicInselId == -1) return "n/a";
        
        Insel i = getInsel(publicInselId);
        
        return i.getName(p);
    }
    
    /**
     * @param p
     * @param c
     * @return 
     */
    public String getInselBeschreibung(Partei p, Coords c) {
        int publicInselId = getInselNummer(c);
        if (publicInselId == -1) return "n/a";
        
        Insel i = getInsel(publicInselId);
        
        return i.getBeschreibung(p);
    }
    
    /**
     * erzeugt die Daten zu den Insel-Beziehungen aller Parteien (wer sieht was, für wen heißt was wie...)
     */
    public void parteiInseln() {
		EinflussKarte ek = new EinflussKarte();
        
        for (int inselId : getExistierendeInseln(TYP_LAND)) {
            Insel i = getInsel(inselId);
            for (Partei p : i.getParteien()) {
                float summe = 0f;
                for (Coords c : i.getCoords()) {
                    summe += ek.getEinfluss(c, p);
                }
                i.setParteiEinfluss(p, summe);
            }
        }
        
        for (Partei partei : Partei.PROXY) {
            // die vorhandenen Anker auffrischen:
            ParteiInselAnker.Initialisieren(partei);
            
            Set<Integer> bekannteInselIds = new HashSet<Integer>();
            if (partei.getNummer() != 0) {
                for (RegionsSicht rs : partei.getKnownRegions(false)) {
                    // if (rs.hasDetails()) {
                        int inselId = getInselNummer(rs.getCoords());
                        if (inselId == -1) continue;
                        bekannteInselIds.add(inselId);
                    // }
                }
            } else {
                bekannteInselIds.addAll(getExistierendeInseln());
            }
            new Debug("Partei " + partei + " kennt die Inseln: " + StringUtils.aufzaehlung(bekannteInselIds));

            for (int publicId : bekannteInselIds) {
                Insel i = getInsel(publicId);
                
                if (i.getTyp() == TYP_OZEAN) continue;
                if (i.getTyp() == TYP_LAVASTROM) continue;
                
                // den passenden ParteiInselAnker entweder holen oder erzeugen:
                ParteiInselAnker pia = null;
                try {
                    pia = ParteiInselAnker.FindOrCreateFor(partei, i.getCoords().iterator().next());
                } catch (RuntimeException ex) {
                    throw new IllegalStateException("Problem mit dem ParteiInselAnker für Insel #" + publicId + ", Typ " + i.getTyp() + ", Partei " + partei + ".", ex);
                }
                String privaterName = pia.getPrivaterInselname();
                String privateBeschr = pia.getPrivateInselbeschreibung();
                
                if (privaterName != null) {
                    new Debug("Inselname #" + publicId + " " + privaterName + " für " + partei + " von Anker " + pia.getAnker() + " geholt.");
                    i.setName(partei, privaterName);
                } else {
                    new Debug("--- kein Name für Insel #" + publicId + " für " + partei + " an Anker " + pia.getAnker() + ".");
                }
                
                if (privateBeschr != null) i.setBeschreibung(partei, privateBeschr);
            }
        }
    }
    
    
    
    public class Insel {
        int publicId;
        int typ;
        
        Set<Coords> coords = new HashSet<Coords>();
        Set<Partei> parteien = new HashSet<Partei>();
        Set<Unit> einheiten = new HashSet<Unit>();
        Coords mittelpunkt;
        
        /**
         * Partei-Nummer => privater Inselname dieser Partei
         */
        Map<Integer, String> parteiInselNamen = new HashMap<Integer, String>();
        
        /**
         * Partei-Nummer => privater Inselbeschreibung dieser Partei
         */
        Map<Integer, String> parteiInselBeschreibungen = new HashMap<Integer, String>();
        
        /**
         * Partei-Nummer => private Insel-ID dieser Partei
         */
        Map<Integer, Integer> parteiInselIds = new HashMap<Integer, Integer>();
        
        /**
         * Partei-Nummer => Einfluss dieser Partei auf der Insel (siehe Klasse EinflussKarte)
         */
        Map<Integer, Float> parteiEinfluss = new HashMap<Integer, Float>();

        public Insel(int publicId, int typ) {
            this.publicId = publicId;
            this.typ = typ;
        }
        
        public boolean istOberwelt() {
            for (Coords c : getCoords()) {
                if (c.getWelt() > 0) return true;
                if (c.getWelt() < 0) return false;
            }
            throw new IllegalStateException("Insel ist weder Ober- noch Unterwelt (keine Regionen? welt == 0?)");
        }

        public boolean istUnterwelt() {
            for (Coords c : getCoords()) {
                if (c.getWelt() < 0) return true;
                if (c.getWelt() > 0) return false;
            }
            throw new IllegalStateException("Insel ist weder Ober- noch Unterwelt (keine Regionen? welt == 0?)");
        }

        public Coords getMittelpunkt() {
            return mittelpunkt;
        }

        public void setMittelpunkt(Coords mittelpunkt) {
            this.mittelpunkt = mittelpunkt;
        }
        
        /**
         * @return alle Höhlen auf dieser Insel
         */
        public Set<Cave> getHoehlen() {
            Set<Cave> retval = new HashSet<Cave>();
            
            List<Cave> kandidaten = null;
            if (this.istOberwelt()) kandidaten = Cave.GetCaves(1);
            if (this.istUnterwelt()) kandidaten = Cave.GetCaves(-1);
            
            for (Cave c : kandidaten) {
                if (getCoords().contains(c.getCoords())) retval.add(c);
            }
            
            return retval;
        }

        public Set<Coords> getCoords() {
            return coords;
        }
        
        public int size() {
            if (coords == null) return 0;
            
            return coords.size();
        }

        public void setCoords(Set<Coords> coords) {
            this.coords = coords;
        }

        public Set<Unit> getEinheiten() {
            return einheiten;
        }

        public void setEinheiten(Set<Unit> einheiten) {
            this.einheiten = einheiten;
        }

        public Set<Partei> getParteien() {
            return parteien;
        }

        public void setParteien(Set<Partei> parteien) {
            this.parteien = parteien;
        }

        public int getPublicId() {
            return publicId;
        }

        public void setPublicId(int publicId) {
            this.publicId = publicId;
        }

        public int getTyp() {
            return typ;
        }

        public void setTyp(int typ) {
            this.typ = typ;
        }

        public void setName(Partei p, String name) {
            parteiInselNamen.put(p.getNummer(), name);
        }
        
        public String getName(Partei p) {
            StringBuilder sb = new StringBuilder();
            
            if (parteiInselNamen.containsKey(p.getNummer())) {
                String name = parteiInselNamen.get(p.getNummer());
                if (name != null) sb.append(name);
                // if ("null".equals(name)) throw new IllegalStateException();
            }
            
            // fremde Namen für die Insel:
            float einflussSumme = 0f;
            for (int e : parteiEinfluss.keySet()) einflussSumme += parteiEinfluss.get(e);
            
            for (ParteiEinfluss pe : getParteiEinfluesse()) {
                Partei fremde = Partei.getPartei(pe.getPartei());
                if (p.getNummer() == fremde.getNummer()) continue; // p selbst hatten wir schon
                if (fremde.isMonster()) continue; // die verraten nix
                
                // wenn es schon einen Namen gibt und der Einfluss dieser Partei unter 10% ist:
                if (sb.length() > 0) {
                    if ((pe.getEinfluss() / einflussSumme) < 0.1) break;
                }
                
                if (parteiInselNamen.containsKey(pe.getPartei())) {
                    String fremdName = parteiInselNamen.get(pe.getPartei());
                    if (fremdName != null) {
                        if (sb.length() > 0) sb.append(" / ");
                        sb.append(fremdName);
                        sb.append("[").append(Codierung.toBase36(pe.getPartei())).append("]");
                    }
                }
            }
            if (sb.length() > 0) return sb.toString();
            
            // wir haben keinen; und auch sonst niemand:
            return null;
        }
        
        public void setBeschreibung(Partei p, String name) {
            parteiInselBeschreibungen.put(p.getNummer(), name);
        }
        
        public String getBeschreibung(Partei p) {
            StringBuilder sb = new StringBuilder();
            
            if (parteiInselBeschreibungen.containsKey(p.getNummer())) {
                String beschr = parteiInselBeschreibungen.get(p.getNummer());
                if (beschr != null) sb.append(beschr);
            }
            
            // okay - wir haben keinen eigenen Namen für die Insel:
            float einflussSumme = 0f;
            for (int e : parteiEinfluss.keySet()) einflussSumme += parteiEinfluss.get(e);
            
            for (ParteiEinfluss pe : getParteiEinfluesse()) {
                if (p.getNummer() == pe.getPartei()) continue; // p selbst hatten wir schon
                if (p.isMonster()) continue; // die verraten nix
                
                // wenn es schon einen Namen gibt und der Einfluss dieser Partei unter 10% ist:
                if (sb.length() > 0) {
                    if ((pe.getEinfluss() / einflussSumme) < 0.1) break;
                }
                
                if (parteiInselBeschreibungen.containsKey(pe.getPartei())) {
                    String fremdBeschr = parteiInselBeschreibungen.get(pe.getPartei());
                    if (fremdBeschr != null) {
                        if (sb.length() > 0) sb.append(" / ");
                        sb.append(fremdBeschr);
                        sb.append("[").append(Codierung.toBase36(pe.getPartei())).append("]");
                    }
                }
            }
            if (sb.length() > 0) return sb.toString();
            
            // wir haben keinen; und auch sonst niemand:
            return null;
        }
        
        public void setPrivateNummer(Partei p, int privateId) {
            parteiInselIds.put(p.getNummer(), privateId);
        }
        
        public int getPrivateNummer(Partei p) {
            if (parteiInselIds.containsKey(p.getNummer())) {
                return parteiInselIds.get(p.getNummer());
            }
            
            return -1;
        }
        
        public void setParteiEinfluss(Partei p, float einfluss) {
            parteiEinfluss.put(p.getNummer(), einfluss);
        }
        
        public float getParteiEinfluss(Partei p) {
            if (!parteiEinfluss.containsKey(p.getNummer())) return 0f;
            return parteiEinfluss.get(p.getNummer());
        }
        
        public SortedSet<ParteiEinfluss> getParteiEinfluesse() {
            Set<ParteiEinfluss> tmp = new HashSet<ParteiEinfluss>();
            for (int partei : parteiEinfluss.keySet()) {
                tmp.add(new ParteiEinfluss(partei, parteiEinfluss.get(partei)));
            }
            return new TreeSet<ParteiEinfluss>(tmp);
        }
        
        /**
         * @return alle Inselkennungen (Terraforming...), die auf dieser Insel vorkommen
         */
        public Set<Integer> getInselKennungen() {
            Set<Integer> retval = new HashSet<Integer>();
            for (Coords c : this.getCoords()) {
                Region r = Region.Load(c);
                retval.add(r.getInselKennung());
            }
            return retval;
        }
        
        @Override
        public String toString() {
            return "Insel #" + this.getPublicId() + "p";
        }
    }
    
    @SuppressWarnings("rawtypes")
	public class ParteiEinfluss implements Comparable {
        final int partei;
        final float einfluss;

        public ParteiEinfluss(int partei, float einfluss) {
            this.partei = partei;
            this.einfluss = einfluss;
        }

        public float getEinfluss() {
            return einfluss;
        }

        public int getPartei() {
            return partei;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof ParteiEinfluss)) throw new UnsupportedOperationException("Cannot compare to " + o.getClass().getSimpleName() + ".");
            ParteiEinfluss other = (ParteiEinfluss)o;
            
            if (other.getEinfluss() > getEinfluss()) return +1;
            if (other.getEinfluss() < getEinfluss()) return -1;
            return 0;
        }
        
        
    }
    
    public class InselGroesseComparator implements Comparator<Insel> {

        @Override
        public int compare(Insel i1, Insel i2) {
            if (i1.getCoords().size() > i2.getCoords().size()) return -1;
            if (i1.getCoords().size() < i2.getCoords().size()) return +1;
            
            if (i1.getPublicId() < i2.getPublicId()) return -1;
            if (i1.getPublicId() > i2.getPublicId()) return +1;
            
            return 0;
        }
    
    }
    
    public class InselMittelpunktComparator implements Comparator<Insel> {

        @Override
        public int compare(Insel o1, Insel o2) {
            int diff = o1.getMittelpunkt().compareTo(o2.getMittelpunkt());
            if (diff != 0) return diff;
            
            if (o1.size() < o2.size()) return -1;
            if (o1.size() > o2.size()) return +1;
            
            return 0;
        }
        
    }
    
    public final class ParteiReportDaten {
        private final Partei p;

        /** alle bewohnten (oder aus anderen Gründen detailliert sichtbaren) Regionen für diese Partei */
        private final List<Region> regionen = new ArrayList<Region>();

        /** alle Nachbarregionen */
        private final List<Region> nachbarn = new ArrayList<Region>();
        
        /** alle früher einmal gesehenen Regionen, die diese Runde NICHT anderweitig sichtbar sind */
        private final List<RegionsSicht> historische = new ArrayList<RegionsSicht>();
        
        private List<Insel> bekannteInseln;
        /** alle direkt, benachbart oder historisch bekannten Regionen (werden im Konstruktor ermittelt) */
        private Map<Integer, List<Coords>> regionenAufInsel;
        private List<Coords> regionenOhneInsel;
        private Map<Integer, List<Coords>> nachbarnAufInsel;
        private List<Coords> nachbarnOhneInsel;

        public ParteiReportDaten(Partei p) {
            if (p == null) throw new NullPointerException("ParteiReportDaten sollen für Partei null erstellt werden...");
            this.p = p;
            
            // hier wird Partei.knownRegions aktualisiert:
            loadRegionen();
            loadNachbarn();
            
            // hier werden regions und nachbarn gesetzt:
            sichtbareRegionenBestimmen();
            
            Collections.sort(regionen, new RegionReportComparatorLNR());
            Collections.sort(nachbarn, new RegionReportComparatorLNR());
            
            // jetzt den Atlas auf seinen Mehrwert abklappern:
            for (Coords c : p.atlas.keySet()) {
                Region r = Region.Load(c);
                if (regionen.contains(r)) continue;
                if (nachbarn.contains(r)) continue;
                
                // aha, die sehen wir diese Runde nicht!
                historische.add(p.atlas.get(c));
            }
//            if (ZATMode.CurrentMode().isDebug()) {
//                if (!historische.isEmpty()) {
//                    new Debug(historische.size() + " historische Atlas-Einträge für Partei " + p + ": " + StringUtils.aufzaehlung(historische));
//                }
//            }
            
            InselVerwaltung iv = InselVerwaltung.getInstance();
            
            regionenAufInsel = new HashMap<Integer, List<Coords>>();
            regionenOhneInsel = new ArrayList<Coords>();
            
            Set<Coords> alleBekannten = new HashSet<Coords>();
            for (Region r : regionen) alleBekannten.add(r.getCoords());
            for (Region r : nachbarn) alleBekannten.add(r.getCoords());
            for (RegionsSicht rs : historische) alleBekannten.add(rs.getCoords());
            
            for(Coords c : alleBekannten) {
                Region r = Region.Load(c);
                if (r instanceof Ozean) { regionenOhneInsel.add(r.getCoords()); continue; }
                if (r instanceof Chaos) { regionenOhneInsel.add(r.getCoords()); continue; }

                int publicId = iv.getInselNummer(r.getCoords());
                if (!regionenAufInsel.containsKey(publicId)) regionenAufInsel.put(publicId, new ArrayList<Coords>());
                regionenAufInsel.get(publicId).add(r.getCoords());
            }

            // Wenig bekannte Inseln aussortieren:
            Set<Integer> loeschSet = new HashSet<Integer>(); // Set von publicIds, die dieser Partei doch unbekannt sind
            for (int publicId : regionenAufInsel.keySet()) {
                int knownN = regionenAufInsel.get(publicId).size();
                
                if (iv.getInsel(publicId) == null) throw new NullPointerException("Die globale Insel Nr. " + publicId + " ist nicht bekannt.");
                
                int inselGroesse = iv.getInsel(publicId).size();
                float ratio = (float)knownN / (float)inselGroesse;
                if ((knownN < 21) && (ratio < 0.5)) {
                    regionenOhneInsel.addAll(regionenAufInsel.get(publicId));
                    loeschSet.add(publicId);
                }
            }
            for (int publicId : loeschSet) regionenAufInsel.remove(publicId);
            

            nachbarnAufInsel = new HashMap<Integer, List<Coords>>();
            nachbarnOhneInsel = new ArrayList<Coords>();
            for(Region r : getNachbarn()) {
                if (r instanceof Ozean) { nachbarnOhneInsel.add(r.getCoords()); continue; }
                if (r instanceof Chaos) { nachbarnOhneInsel.add(r.getCoords()); continue; }

                int publicId = iv.getInselNummer(r.getCoords());
                if (!regionenAufInsel.containsKey(publicId)) {
                    // wenn die Insel nicht bekannt ist:
                    nachbarnOhneInsel.add(r.getCoords());
                } else {
                    // wenn die Insel bekannt ist:
                    if (!nachbarnAufInsel.containsKey(publicId)) {
                        // ggf. neue Liste erstellen
                        nachbarnAufInsel.put(publicId,  new ArrayList<Coords>());
                    }
                    nachbarnAufInsel.get(publicId).add(r.getCoords());
                }
            }
            
            // ...und noch die historischen zu den Nachbarn:
            for (RegionsSicht rs : historische) {
                if (rs.getTerrain().equalsIgnoreCase("Ozean")) { nachbarnOhneInsel.add(rs.getCoords()); continue; }
                if (rs.getTerrain().equalsIgnoreCase("Chaos")) { nachbarnOhneInsel.add(rs.getCoords()); continue; }
                int publicId = iv.getInselNummer(rs.getCoords());
                if (!regionenAufInsel.containsKey(publicId)) {
                    // wenn die Insel nicht bekannt ist:
                    nachbarnOhneInsel.add(rs.getCoords());
                } else {
                    // wenn die Insel bekannt ist:
                    if (!nachbarnAufInsel.containsKey(publicId)) nachbarnAufInsel.put(publicId,  new ArrayList<Coords>());
                    nachbarnAufInsel.get(publicId).add(rs.getCoords());
                }
            }


            bekannteInseln = new ArrayList<Insel>();
            for (int publicId : regionenAufInsel.keySet()) {
                Insel i = iv.getInsel(publicId);
                if (i == null) throw new NullPointerException("Die globale Insel Nr. " + publicId + " ist nicht bekannt.");
                bekannteInseln.add(i);
            }
            
            
            // bekannte Inseln mit einmaligen / permanenten Insel-IDs versehen
            ParteiInselAnker.Initialisieren(p);
            Set<Coords> anker = p.getInselAnker().keySet();
            
            for (Insel insel : bekannteInseln) {
                // privaten Namens- und ID-Eintrag suchen
                boolean gefunden = false;
                for (Coords c : insel.getCoords()) {
                    if (anker.contains(c)) {
                        // yepp!
                        ParteiInselAnker pia = p.getInselAnker().get(c);
                        insel.setPrivateNummer(p, pia.getPrivateInselId());
                        if (pia.getPrivaterInselname() != null) insel.setName(p, pia.getPrivaterInselname());
                        if (pia.getPrivateInselbeschreibung() != null) insel.setBeschreibung(p, pia.getPrivateInselbeschreibung());
                        
                        gefunden = true;
                        break;
                    }
                }
                if (gefunden) continue;
                
                // keiner vorhanden - erzeugen:
                Coords c = regionenAufInsel.get(insel.getPublicId()).get(0);
                ParteiInselAnker.FindOrCreateFor(p, c);
            }
            // jetzt haben alle bekannten Inseln neben der öffentlichen ID auch eine permanente private ID dieser Partei


            Collections.sort(bekannteInseln, iv.new InselMittelpunktComparator());
        }

        public List<Insel> getBekannteInseln() {
            return bekannteInseln;
        }

        public List<Coords> getNachbarnAufInsel(int publicInselId) {
            return nachbarnAufInsel.get(publicInselId);
        }

        public List<Coords> getNachbarnOhneInsel() {
            return nachbarnOhneInsel;
        }

        public List<Coords> getRegionenAufInsel(int publicInselId) {
            return regionenAufInsel.get(publicInselId);
        }

        public List<Coords> getRegionenOhneInsel() {
            return regionenOhneInsel;
        }
        
        
        
        private void sichtbareRegionenBestimmen() {
            if (p.getNummer() != 0) {
                // TODO: Die (versteckten) Nachbarn von bemannten Regionen sollten aber als Chaos auftauchen.
                for (RegionsSicht rs : p.getKnownRegions(true)) { // true = mit versteckten
                    Region r = Region.Load(rs.getCoords());
                    if (rs.hasDetails()) {
                        if (!p.canAccess(r)) {
							// TODO: da steht einer in einer eigentlich unsichtbaren Regionn - wat nu?
                        } else {
                            regionen.add(Region.Load(rs.getCoords()));
                        }
                    } else {
                        if (p.canAccess(r)) {
                            nachbarn.add(Region.Load(rs.getCoords()));
                        } else {
                            // dann isses Chaos oder so:
                            Region unsichtbar = null;
                            try {
                                unsichtbar = (Region)Class.forName("de.x8bit.Fantasya.Atlantis.Regions." + GameRules.TERRAIN_UNSICHTBARER_REGIONEN).newInstance();
                            } catch (InstantiationException ex) {
                                new SysErr(ex.getMessage());
                            } catch (IllegalAccessException ex) {
                                new SysErr(ex.getMessage());
                            } catch (ClassNotFoundException ex) {
                                new SysErr(ex.getMessage());
                            }
                            if (unsichtbar == null) throw new RuntimeException("Konnte keine Ersatzregion (unsichbar für " + p + ") instantiieren.");
                            unsichtbar.setCoords(r.getCoords());
                            nachbarn.add(unsichtbar);
                        }
                    }
                }
            } else {
                for (Region r : Region.CACHE.values()) {
                    p.addKnownRegion(r, true, Atlantis.class);
                    regionen.add(r);
                }
            }
        }
        
        /**
         * lädt alle Regionen für Regionen() vor
         */
        private void loadRegionen() {
            if (p.getNummer() == 0) {
                for (Region r : Region.CACHE.values()) {
                    p.addKnownRegion(r, true, Atlantis.class);
                }
                return;
            }

            for (Unit u : Unit.CACHE) {
                if (u.getOwner() == p.getNummer()) {
                    p.addKnownRegion(u.getCoords(), true, Unit.class);
                }
            }
        }

        /**
         * lädt alle Nachbarregionen - also solche, in denen keine Einheiten sind
         */
        private void loadNachbarn() {
            for (RegionsSicht rs : p.getKnownRegions(false)) { // false == ohne "Versteckte"
                Region r = Region.Load(rs.getCoords());
                if (rs.hasDetails() && !Unit.CACHE.getAll(r.getCoords(),p.getNummer()).isEmpty()) {
                    for (Coords nachbar : r.getCoords().getNachbarn()) {
                        p.addKnownRegion(nachbar, false, Unit.class);
                    }
                }
            }

            // new SysMsg(4, nachbarn.size() + " Nachbar-Regionen");
        }

        public List<Region> getNachbarn() {
            return nachbarn;
        }

        public List<Region> getRegionen() {
            return regionen;
        }

        public List<RegionsSicht> getHistorische() {
            return historische;
        }
        
        

        
        
    }
    
}
