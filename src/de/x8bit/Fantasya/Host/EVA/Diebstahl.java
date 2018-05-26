package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.UnitHint;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;
import java.util.HashMap;
import java.util.Map;

public class Diebstahl extends EVABase {

    public static final Map<Integer, Integer> ParteiErtrag = new HashMap<Integer, Integer>();
    
    public Diebstahl() {
		super("beklaue", "Diebstahl am Feind");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
		List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		bm = new BefehlsMuster(Diebstahl.class, 0, "^(beklaue)[n]? [a-z0-9]+([ ]+(\\/\\/).*)?", "b", Art.LANG);
		bm.addHint(new UnitHint(1));

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

			// Opfer laden
			Unit victim = Unit.Load(Codierung.fromBase36(eb.getTargetUnit()));
			if (victim == null) {
				new Fehler(u + " - die Einheit '" + eb.getTargetUnit() + "' ist nicht zu finden.", u);
				eb.setError();
				continue;
			}
		
			// Koordinaten überprüfen
			if (!victim.getCoords().equals(u.getCoords())) {
				new Fehler(u + " - die Einheit '" + eb.getTargetUnit() + "' ist nicht zu finden.", u);
				eb.setError();
				continue;
			}
		
			// Wahrnehmung testen
			int wahrnehmung = r.topTW(Wahrnehmung.class, victim.getOwner());
			int tarnung = u.Talentwert(de.x8bit.Fantasya.Atlantis.Skills.Tarnung.class); // muss nicht verwendet werden (also Tarnung gesetzt, für unsichtbar)
			if (tarnung <= wahrnehmung) {
				// erwischt ... beide Informieren
				new Info(u + " wurde beim Diebstahl von " + victim + " erwischt.", u);
				new Info(victim + " erwischt " + u + " beim versuchten Diebstahl.", victim);
				eb.setPerformed();
			} else {
				// erfolgreich
				int silber = victim.getItem(Silber.class).getAnzahl();
				if (silber != 0) {
					int possible = tarnung * 50 * u.getPersonen();
					if (possible < silber) {
						silber -= possible;
					} else {
						possible = silber;
						silber = 0;
					}

					// Silber wechselt den Besitzer
					victim.getItem(Silber.class).setAnzahl(silber);
					u.getItem(Silber.class).setAnzahl(u.getItem(Silber.class).getAnzahl() + possible);

					// beide Informieren
					new Info(u + " kann " + possible + " Silber von " + victim + " stehlen.", u);
					// keine Info !!! new Info(victim + " wurde um " + possible + " Silber erleichtert", victim, victim.getCoords());
					
					u.setEinkommen(u.getEinkommen() + possible);
					victim.setEinkommen(victim.getEinkommen() - possible); // hihi...
                    
                    // Ertrag durch Klauen aufzeichnen (kommt später ggf. in den Report)
                    if (!ParteiErtrag.containsKey(u.getOwner())) ParteiErtrag.put(u.getOwner(), 0);
                    ParteiErtrag.put(u.getOwner(), ParteiErtrag.get(u.getOwner()) + possible);

					eb.setPerformed();
				} else {
					if (victim.getItems().size() > 0)
					{
						new Info("Das Opfer des Diebstahls (" + victim + ") hat überhaupt nichts, armer Bettler", u);
					} else {
						// kein Silber vorhanden ... also irgend was klauen
						int possible = tarnung * u.getPersonen();
						Item item = victim.getItems().get(Random.rnd(0, victim.getItems().size()));
						int avail = item.getAnzahl();
						if (avail < possible) possible = avail;
						item.setAnzahl(item.getAnzahl() - possible);
						new Info(u + " kann " + possible + " " + item.getName() + " von " + victim + " stehlen.", u);
						item = u.getItem(item.getClass());
						item.setAnzahl(item.getAnzahl() + possible);
					}
					eb.setPerformed();
				}
			}
		}
		
	}



	@Override
	public void PostAction() { }
	@Override
	public void PreAction() { }
	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	@Override
	public void DoAction(Einzelbefehl eb) { }

}
