package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Reports.Writer.CRWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author hapebe
 */
public class RegionsSicht {
	protected final Coords c;
	protected final boolean details;
	protected final Class<? extends Atlantis> quelle;
    protected final int runde;
    
    /**
     * nur wichtig, falls es sich um eine RegionsSicht aus der Vergangenheit handelt.
     */
    protected String terrain;

    /**
     * nur wichtig, falls es sich um eine RegionsSicht aus der Vergangenheit handelt.
     */
    protected String name;

    /**
     * nur wichtig, falls es sich um eine RegionsSicht aus der Vergangenheit handelt.
     */
    protected final Set<Richtung> strassen = new HashSet<Richtung>();

	public Coords getCoords() {
		return c;
	}

	public boolean hasDetails() {
		return details;
	}

	public Class<? extends Atlantis> getQuelle() {
		return quelle;
	}

	public RegionsSicht(int runde, Coords c, boolean details, Class<? extends Atlantis> quelle) {
		this.runde = runde;
        this.c = c;
		this.details = details;
		this.quelle = quelle;
	}
    
    public String toCode() {
        Region r = Region.Load(c);
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(c.toString()).append("; ");
        sb.append(runde).append("; ");
        sb.append(r.getTyp());
        
        sb.append("; (");
        boolean first = true;
        for (Richtung strasse : strassen) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(strasse.getShortcut().toLowerCase());
        }
        sb.append("); ");
        
        if (name != null) name = name.replaceAll(";", ",");
        sb.append(name); // d.h. auch: "null" wenn name null ist.
        
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return toCode();
    }
    
    public static RegionsSicht FromCode(String code) {
        Coords c = null;
        int runde = -1;
        String name = null;
        String terrain = null;
        Set<Richtung> strassen = new HashSet<Richtung>();
        
        // überhaupt ansatzweise gültig?
        if ((!code.startsWith("{")) || (!code.endsWith("}"))) return null;
        
        // {(8,-10,1); 187; Hochland; (o,so,sw)}
        // oder (Update 2012-08-15):       {(8,-10,1); 187; Hochland; (o,so,sw); Namedeslandes)}
        
        code = code.substring(1, code.length() - 1);
        // (8,-10,1); 187; Hochland; (o,so,sw)
        // oder (Update 2012-08-15):       (8,-10,1); 187; Hochland; (o,so,sw); Namedeslandes)
        
        StringTokenizer st = new StringTokenizer(code, ";");
        for (int i=0; st.hasMoreTokens(); i++) {
            String t = st.nextToken().trim();
            if (i == 0) {
                // (8,-10,1)
                if ((!t.startsWith("(")) || (!t.endsWith(")"))) throw new IllegalArgumentException("Erwarte Koordinatenangabe in Klammern: " + t );
                t = t.replaceAll(",", " ");
                c = Coords.fromString(t); // new Debug(t + " -> " + c);
            } else if (i == 1) {
                // 187
                runde = Integer.parseInt(t);
            } else if (i == 2) {
                terrain = t;
            } else if (i == 3) {
                // (o,so,sw)
                if ((!t.startsWith("(")) || (!t.endsWith(")"))) throw new IllegalArgumentException("@" + c + ": Erwarte Liste von Straßenrichtungen in Klammern: " + t );
                t = t.substring(1, t.length() - 1);
                if (!t.isEmpty()) {
                    // o,so,sw
                    StringTokenizer st2 = new StringTokenizer(t, ",");
                    while (st2.hasMoreTokens()) {
                        strassen.add(Richtung.getRichtung(st2.nextToken().trim()));
                    }
                }
            } else if (i == 4) {
                // Namesdeslandes   oder null
                name = t;
            }
        }
        
        
        RegionsSicht rs = new RegionsSicht(runde, c, false, Partei.class);
        rs.setTerrain(terrain);
        if (!("null".equals(name))) rs.setName(name);
        rs.getStrassen().clear();
        rs.getStrassen().addAll(strassen);
        
        return rs;
    }

    public int getRunde() {
        return runde;
    }
    
    public Set<Richtung> getStrassen() {
        return strassen;
    }

    /**
     * die Straßen der realen Region in die Regionssicht kopieren (für historische Archivierung)
     * @param r die reale Region (sollte tunlichst mit den Koordinaten der RegionsSicht übereinstimmen)
     */
    public void copyStrassen(Region r) {
        if (!r.getCoords().equals(this.getCoords())) throw new IllegalStateException("RegionsSicht auf " + getCoords() + " soll die Straßen von " + r.getCoords() + " kopieren?");
        
        if (r.istBetretbar(null) && (r.getSteineFuerStrasse() > 0)) {
            strassen.clear();
            
            for(Richtung richtung : Richtung.values()) {
                if (r.getStrassensteine(richtung) >= r.getSteineFuerStrasse()) {
                    strassen.add(richtung);
                }
            }
        }
    }

    public String getTerrain() {
        return terrain;
    }

    public void setTerrain(String terrain) {
        this.terrain = terrain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
	/**
	 * speichert die (historische) RegionsSicht in den CR
	 * @param writer - passender ReportWriter
	 * @param partei - die Partei für diese Region
	 */
	public void SaveCR(CRWriter writer, Partei partei)	{
        Region gegenwart = Region.Load(getCoords());
        
		boolean hidden = !partei.canAccess(gegenwart); // wenn die Region zu jung ist, soll sie "versteckt" werden.
		int grenzid = 0;
		
		Coords my = partei.getPrivateCoords(getCoords());
        if (!hidden) {
            writer.wl("REGION " + my.getX() + " " + my.getY() + " " + my.getWelt() + " ");
            
            InselVerwaltung iv = InselVerwaltung.getInstance();
            InselVerwaltung.ParteiReportDaten prd = iv.getParteiReportDaten(partei);
            if (!prd.getNachbarnOhneInsel().contains(getCoords())) {
                int inselId = iv.getPrivateInselNummer(partei, getCoords());
                if (inselId != -1) writer.wl(inselId, "Insel");
            }
            
            if ((name != null) && (!"null".equals(name))) {
                writer.wl(name, "Name");
            } else {
                writer.wl("", "Name");
            }
			writer.wl(getTerrain(), "Terrain");
            writer.wl(getCoords().asRegionID(false), "id" );
            writer.wl(getRunde(), "Runde");
			writer.wl("historic", "visibility");
            
			// ** Grenzen (Straßen / Feuerwand / etc.)
			for(Richtung richtung : Richtung.values()) {
				if (getStrassen().contains(richtung)) {
					writer.wl("GRENZE " + grenzid++);
					writer.wl("Straße", "typ");
					writer.wl(richtung.ordinal(), "richtung");
					writer.wl(100, "prozent");
				}
			}
            
        } else {
            writer.wl("REGION " + my.getX() + " " + my.getY() + " " + my.getWelt() + " ");
            writer.wl(GameRules.TERRAIN_UNSICHTBARER_REGIONEN, "Terrain");
            writer.wl(this.getCoords().asRegionID(false), "id" );
        }
	}

}
