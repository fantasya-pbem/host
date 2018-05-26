package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.EVA.util.ParteiInselAnker;
import java.util.HashSet;
import java.util.Set;

public class Benennungen extends EVABase
{
    InselVerwaltung iv;

    
	
	public Benennungen()
	{
		super("benenne", "Namensvergabe");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
		BefehlsMuster bm = null;
		
        bm = new BefehlsMuster(Benennungen.class, 0, "^@?(benenne)[n]? ((einheit)|(region)|(gebäude)|(gebaeude)|(burg)|(schiff)|(volk)|(partei)|(insel)|(kontinent)) .+$", "b", Art.KURZ);
		Set<String> keywords = new HashSet<String>();
		keywords.add("benenne");
		keywords.add("benennen");
		keywords.add("einheit");
		keywords.add("region");
		keywords.add("gebäude");
		keywords.add("gebaeude");
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
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

            // Falls es verschiedene Teile gibt: Macht nix, wird alles der Name
            eb.combineTokens(2, eb.getTokens().length - 1);
            
            String bezug = eb.getTokens()[1].toLowerCase();
            String name = eb.getTokens()[2];

            if (bezug.equals("einheit")) {
                // COMMAND BENENNE EINHEIT "blabla"
                new Info(u + " umgetauft in " + name + ".", u);
                u.setName(name);
            } else if (bezug.equals("region")) {
                // COMMAND BENENNE REGION "blabla"
                Region region = Region.Load(u.getCoords());
                if (region.hatGebaeude(Burg.class, 1, u)) {
                    new Info("Der Name von " + region + " sei ab jetzt: " + name + ".", u);
                    region.setName(name);
                } else {
                    eb.setError();
                    new Fehler(u + " ist in keinem Gebäude.", u);
                }

            } else if (bezug.equals("gebaeude") || bezug.equals("burg") || bezug.equals("gebäude")) {
                // COMMAND BENENNE GEBÄUDE "blabla"
                Building building = Building.getBuilding(u.getGebaeude());
                if (building == null) {
                    u.setGebaeude(0);
                    eb.setError();
                    new Fehler(u + " ist in keinem Gebäude.", u);
                } else {
                    new Info("Der Name von " + building + " sei ab jetzt '" + name + "'.", u);
                    building.setName(name);
                }
            } else if (bezug.equals("schiff")) {
                // COMMAND BENENNE SCHIFF "blabla"
                Ship ship = Ship.Load(u.getSchiff());
                if (ship == null) {
                    u.setSchiff(0);
                    eb.setError();
                    new Fehler(u + " ist auf keinem Schiff.", u);
                } else {
                    new Info("Wir taufen " + ship + " auf den Namen '" + name + "'.", u);
                    ship.setName(name);
                }
            } else if (bezug.equals("volk") || bezug.equals("partei")) {
                // COMMAND BENENNE VOLK "blabla"
                // COMMAND BENENNE PARTEI "blabla" - Syntax F1
                Partei p = Partei.getPartei(u.getOwner());
                new Info("Unser Volk soll ab jetzt als " + name + " bekannt sein.", u);
                p.setName(name);
            } else if (bezug.equals("insel") || bezug.equals("kontinent")) {
                // BENENNE INSEL "blabla"
                Building building = Building.getBuilding(u.getGebaeude());
                if (building == null) {
                    u.setGebaeude(0);
                    eb.setError();
                    new Fehler(u + " wohnt hier nicht (kein Gebäude) und mag deswegen keinen Namen vergeben.", u);
                } else {
                    new Info("Diese Insel soll fortan " + name + " heißen.", u);
                    benenneInsel(u, name);
                }
            } else {
                eb.setError();
                new Fehler("'" + bezug + "' - sowas kann man nicht benennen.", u);
            }

            eb.setPerformed();
        } // nächster Befehl
    }

    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void DoAction(Einzelbefehl eb) { }
	@Override
	public void PostAction() { }
	@Override
	public void PreAction() {
        iv = InselVerwaltung.getInstance();
        iv.karteVerarbeiten();
    }
    

    private void benenneInsel(Unit u, String name) {
        ParteiInselAnker.BenenneInsel(u, name);
    }
    
	
}
