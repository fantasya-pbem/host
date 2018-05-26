package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Battle;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;

public class Belagerung extends EVABase
{
	public Belagerung()
	{
		super("belagere", "Belagerungen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Belagerung.class, 0, "^(belager)[en]? [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "b", Art.LANG);
        bm.addHint(new IDHint(1));
        bm.setKeywords("belager", "belagere", "belagern");
        retval.add(bm);
        
        return retval;
    }
	
	/** kompletten Belagerungen löschen */
	public void PreAction() {
		for (Unit u : Unit.CACHE) {
            u.setBelagert(0);
        }
	}

    public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();
            String id = eb.getTargetId();

            int nummer = 0;
            try {
                nummer = Codierung.fromBase36(id);
            } catch(Exception ex) { }

            Building building = Building.getBuilding(nummer);
            if (building == null) {
                eb.setError();
                new Fehler("Kann das Gebäude [" + id + "] nicht finden.", u);
                continue;
            }

            if (!building.getCoords().equals(u.getCoords())) {
                eb.setError();
                new Fehler("Kann das Gebäude [" + id + "] nicht finden.", u);
                continue;
            }

            Unit owner = Unit.Load(building.getOwner());
            if (owner != null) new Battle(building + " wird von " + u + " belagert.", owner);
            
            new Battle(u + " belagert " + building + ".", u);
            u.setBelagert(building.getNummer());

            // Gebäude zerstören
            if ((u.getItem(Katapult.class).getAnzahl() > 0) && (u.Talentwert(Katapultbedienung.class) > 0)) {
                int treffer = 0;
                // TODO eine Mindestzahl Personen pro Katapult festlegen?
                for(int i = 0; i < u.getItem(Katapult.class).getAnzahl(); i++) {
                    int fail = Random.rnd(0, 50 - u.Talentwert(Katapultbedienung.class) * 3);
                    int kill = Random.rnd(0, 100);
                    if (kill > fail) treffer++;
                }
                if (treffer > building.getSize()) {
                    treffer = building.getSize();
                    new Battle(u + " zerstört " + building + ".", u);
                    if (owner != null) new Battle(u + " zerstört " + building + ".", owner);
                } else {
                    new Battle(u + " beschädigt " + building + " um " + treffer + " Größenpunkte.", u);
                    if (owner != null) new Battle(u + " beschädigt " + building + " um " + treffer + " Größenpunkte.", owner);
                }
                
                building.setSize(building.getSize() - treffer);

                // Burgen bestehen aus Steinen ... die können wieder verwendet werden
                int o = 0;	// Steine des Gebäude-Eigners
                int a = 0;	// Steine des Angreifers
                for(int i = 0; i < treffer; i++) {
                    switch(Random.rnd(0, 3)) {
                        case 0:	o++; break;	// Stein erhält der Eigner
                        case 1:	a++; break; // Stein erhält Angreifer
                        case 2: // Stein wird zerstört
                    }
                }
                if (owner != null) {
                    owner.setItem(Stein.class, owner.getItem(Stein.class).getAnzahl() + o);
                    new Battle(owner + " erhält " + o + " Steine vom Gebäude " + building + ".", owner);
                }
                u.setItem(Stein.class, u.getItem(Stein.class).getAnzahl() + a);
                new Battle(u + " erhält " + a + " Steine vom Gebäude.", u);

                eb.setPerformed();
            } else {
                // entweder keine Katapulte oder kein Talent
                eb.setError();
                new Fehler("Wir wissen nicht, wie wir " + building + " belagern sollen.", u, u.getCoords());
                continue;
            }

            if (building.getSize() == 0) {
                // alle Einheiten aus dem Gebäude rausschmeißen
                for(Unit hu : r.getUnits()) {
                    if (hu.getGebaeude() == building.getNummer()) {
                        hu.setGebaeude(0);
                    }
                }

                // das Gebäude selbst verschwinden lassen
                boolean success = Building.PROXY.remove(building);
				if (!success) throw new IllegalStateException("Gebäude " + building + " kann nach der Belagerung nicht aus dem Speicher entfernt werden.");
            }
            
        }


    }

    public void DoAction(Einzelbefehl eb) { }
	public boolean DoAction(Unit u, String[] befehl) { return false; }

	public void PostAction() { }
}
