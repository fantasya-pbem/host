package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.EVA.util.ParteiInselAnker;

public class Beschreibungen extends EVABase
{
    InselVerwaltung iv;
	
	public Beschreibungen()
	{
		super("benenne", "Ändere Beschreibungen von Objekten");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        BefehlsMuster bm = new BefehlsMuster(Beschreibungen.class, 0, "^@?(beschreibe)[n]? ((einheit)|(region)|(gebaeude)|(gebäude)|(burg)|(schiff)|(volk)|(partei)|(insel)|(kontinent)) .+$", "b", Art.KURZ);
        Set<String> keywords = new HashSet<String>();
        keywords.add("beschreibe");
        keywords.add("beschreiben");
        keywords.add("einheit");
        keywords.add("region");
        keywords.add("gebaeude");
        keywords.add("gebäude");
        keywords.add("burg");
        keywords.add("schiff");
        keywords.add("volk");
        keywords.add("partei");
        keywords.add("insel");
        keywords.add("kontinent");
        bm.setKeywords(keywords);
        retval.add(bm);

        return retval;
    }
	
	@Override
	public void PostAction() {
//		if (ZATMode.CurrentMode().isDebug()) {
//			if (false) {
//				new SysMsg("TODO: Region.PostAction() wieder entfernen!!!");
//				for (Region r: Region.CACHE.values()) {
//					String s = "Entstanden in " + r.getEnstandenIn() + ", Besuchsstatus " + r.getAlter();
//					for (Partei p : r.anwesendeParteien()) {
//						s += ", Partei " + p + " geboren in " + p.getAlter();
//					}
//
//					r.setBeschreibung(s);
//				}
//			}
//		}
	}
	
    @Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			// new TestMsg("Lerne: " + eb.toString());
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

            // Falls es verschiedene Teile gibt: Macht nix, wird alles der Name
            eb.combineTokens(2, eb.getTokens().length - 1);
            
            String bezug = eb.getTokens()[1].toLowerCase();
            String name = eb.getTokens()[2];

            if (bezug.equals("einheit")) {
                // COMMAND BENENNE EINHEIT "blabla"
                u.setBeschreibung(name);

            } else if (bezug.equals("region")) {
                // COMMAND BENENNE REGION "blabla"
                Region region = Region.Load(u.getCoords());
                if (region.hatGebaeude(Burg.class, 1, u)) region.setBeschreibung(name);

            } else if (bezug.equals("gebaeude") || bezug.equals("gebäude") || bezug.equals("burg")) {
                // COMMAND BENENNE GEBÄUDE "blabla"
                Building building = Building.getBuilding(u.getGebaeude());
                if (building == null) {
                    eb.setError();
                    new Fehler(u + " ist in keinem Gebäude.", u);
                } else {
                    building.setBeschreibung(name);
                }

            } else if (bezug.equals("schiff")) {
                // COMMAND BENENNE SCHIFF "blabla"
                Ship ship = Ship.Load(u.getSchiff());
                if (ship == null) {
                    eb.setError();
                    new Fehler(u + " ist auf keinem Schiff.", u);
                } else {
                    ship.setBeschreibung(name);
                }

            } else if (bezug.equals("volk") || bezug.equals("partei")) {
                // COMMAND BENENNE VOLK "blabla"
                // COMMAND BENENNE PARTEI "blabla" - Syntax F1
                Partei p = Partei.getPartei(u.getOwner());
                p.setBeschreibung(name);
                
            } else if (bezug.equals("insel") || bezug.equals("kontinent")) {
                // BESCHREIBE INSEL "blabla"
                Building building = Building.getBuilding(u.getGebaeude());
                if (building == null) {
                    u.setGebaeude(0);
                    eb.setError();
                    new Fehler(u + " wohnt hier nicht (kein Gebäude) und mag deswegen keine Beschreibung hinterlassen.", u);
                } else {
                    beschreibeInsel(u, name);
                }
                
            } else {
                eb.setError();
                new Fehler("'" + bezug + "' - das kann man nicht beschreiben.", u);
            }

            eb.setPerformed();
        } // nächster Befehl
    }
	
	@Override
	public void PreAction() {
        iv = InselVerwaltung.getInstance();
        iv.karteVerarbeiten();
    }

    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void DoAction(Einzelbefehl eb) { }
    
    
    private void beschreibeInsel(Unit u, String name) {
        ParteiInselAnker.BeschreibeInsel(u, name);
    }
    
	
    
}
