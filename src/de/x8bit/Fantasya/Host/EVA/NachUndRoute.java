package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.MultiCoordsHint;
import de.x8bit.Fantasya.Host.EVA.util.Reisen;

public class NachUndRoute extends EVABase {
	public final static int BEFEHL_NACH_RICHTUNG = 1;
	public final static int BEFEHL_ROUTE_RICHTUNG = 11;
	public final static int BEFEHL_NACH_KOORDS = 21;
	public final static int BEFEHL_ROUTE_KOORDS = 31;

	public NachUndRoute()	{
		super("nach", "Bewegungen von Mann und Schiff");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

		StringBuilder richtungen = new StringBuilder();
		for (Richtung r : Richtung.values()) {
			if (richtungen.length() > 0) richtungen.append("|");
			richtungen.append(r.getShortcut().toLowerCase());
		}
		for (Richtung r : Richtung.values()) {
			if (richtungen.length() > 0) richtungen.append("|");
			richtungen.append(r.toString().toLowerCase());
		}

		// NACH
		bm = new BefehlsMuster(NachUndRoute.class, BEFEHL_NACH_RICHTUNG, "^nach( +(" + richtungen + "|pause))+([ ]+(\\/\\/).*)?", "n", Art.LANG);
        bm.setKeywords("nach", "pause");
        retval.add(bm);

        // ROUTE
        bm = new BefehlsMuster(NachUndRoute.class, BEFEHL_ROUTE_RICHTUNG, "^route( +(" + richtungen + "|pause))+([ ]+(\\/\\/).*)?", "r", Art.LANG);
        bm.setKeywords("route", "pause");
		retval.add(bm);

		// NACH (x-Koordinate y-Koordinate) ...
		bm = new BefehlsMuster(NachUndRoute.class, BEFEHL_NACH_KOORDS, "^nach(( \\([-+]?[0-9]+ [-+]?[0-9]+\\))|( pause))+([ ]+(\\/\\/).*)?", "n", Art.LANG);
		bm.addHint(new MultiCoordsHint());
        bm.setKeywords("nach", "pause");
		retval.add(bm);

		// ROUTE (x-Koordinate y-Koordinate) ...
		bm = new BefehlsMuster(NachUndRoute.class, BEFEHL_ROUTE_KOORDS, "^route(( \\([-+]?[0-9]+ [-+]?[0-9]+\\))|( pause))+([ ]+(\\/\\/).*)?", "r", Art.LANG);
		bm.addHint(new MultiCoordsHint());
        bm.setKeywords("route", "pause");
		retval.add(bm);

		return retval;
    }

	@Override
	public void PreAction()	{ }

    @Override
	public void DoAction(Einzelbefehl eb) {
        // new Debug("Bewegung: " + eb.toString());
        if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

		Reisen.Ausfuehren(eb);

        eb.setPerformed();
    }

	@Override
	public void PostAction() {
		// nach dem Wechsel von Regionen ist es ein guter Augenblick, um die Sortierung neu zu berechnen:
		for (Region r : Region.CACHE.values()) {
			Sortieren.Normalisieren(r);
		}
	}

	@Override
	public void DoAction(Region r, String befehl) {	}
	@Override
    public boolean DoAction(Unit u, String[] befehl) { return false; }
}
