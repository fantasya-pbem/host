package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.util.Codierung;

public class Betreten extends EVABase
{
	public Betreten()
	{
		super("betrete", "Betreten von Gebäuden, Schiffen und Höhlen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		bm = new BefehlsMuster(Betreten.class, 1, "^@?(betrete)[n]? ((gebaeude)|(gebäude)|(burg)) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "b", Art.KURZ);
		bm.addHint(new IDHint(2));
        bm.setKeywords("betrete", "betreten", "gebaeude", "gebäude", "burg");
        retval.add(bm);

		bm = new BefehlsMuster(Betreten.class, 2, "^@?(betrete)[n]? (schiff) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "b", Art.KURZ);
		bm.addHint(new IDHint(2));
        bm.setKeywords("betrete", "betreten", "schiff");
        retval.add(bm);

		bm = new BefehlsMuster(Betreten.class, 3, "^@?(betrete)[n]? ((hoehle)|(höhle)) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "b", Art.KURZ);
		bm.addHint(new IDHint(2));
        bm.setKeywords("betrete", "betreten", "hoehle", "höhle");
        retval.add(bm);

        return retval;
    }

	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			int variante = eb.getVariante();

			if (variante == 1) this.BetreteGebaeude(eb);
			if (variante == 2) this.BetreteSchiff(eb);
			if (variante == 3) this.BetreteHoehle(eb);

			eb.setPerformed();
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

	

	public void BetreteGebaeude(Einzelbefehl eb) {
		Unit u = eb.getUnit();

		int gebaeude = 0;
		try {
			gebaeude = Codierung.fromBase36(eb.getTargetId());
		} catch(Exception ex) { /* nüschts */ }

		if (gebaeude == 0) {
			new Fehler("Die Nummer für das Gebäude [" + eb.getTargetId() + "] wurde nicht erkannt.", u);
			eb.setError();
			return;
		}

		Building building = Building.getBuilding(gebaeude);
		if (building == null) {
			new Fehler(u + " - das Gebäude [" + Codierung.toBase36(gebaeude) + "] existiert nicht.", u);
			eb.setError();
			return;
		} else if (!building.getCoords().equals(u.getCoords())) {
			new Fehler(u + " - das Gebäude [" + Codierung.toBase36(gebaeude) + "] existiert nicht.", u);
			eb.setError();
			return;
		}
		
		if (u.getGebaeude() != building.getNummer()) {
			u.Enter(building);
		} else {
			new Info(u + " ist schon in " + building + ".", u);
		}
	}

	public void BetreteSchiff(Einzelbefehl eb) {
		Unit u = eb.getUnit();

		int schiff = 0;
		try { schiff = Codierung.fromBase36(eb.getTargetId()); } catch(Exception ex) { /* nüschts */ }

		if (schiff == 0) {
			new Fehler("Die Nummer für das Schiff '" + eb.getTargetId() + "' wurde nicht erkannt.", u, u.getCoords());
			eb.setError();
			return;
		}

		Ship ship = Ship.Load(schiff);
		if (ship == null) {
			new Fehler("Das Schiff '" + Codierung.toBase36(schiff) + "' ist nicht in dieser Region.", u, u.getCoords());
			eb.setError();
			return;
		} else if (!ship.getCoords().equals(u.getCoords())) {
			new Fehler("Das Schiff '" + Codierung.toBase36(schiff) + "' ist nicht in dieser Region.", u, u.getCoords());
			eb.setError();
			return;
		}

		if (u.getSchiff() != ship.getNummer()) {
			u.Enter(ship);
		} else {
			new Info(u + " ist schon auf " + ship + ".", u);
		}
	}

	public void BetreteHoehle(Einzelbefehl eb) {
		Unit u = eb.getUnit();

		int hoehle = 0;
		try {
			hoehle = Codierung.fromBase36(eb.getTargetId());
		} catch(Exception ex) { /* nüschts */ }

		if (hoehle == 0) {
			new Fehler("Die Nummer für die Höhle [" + eb.getTargetId() + "] wurde nicht erkannt.", u);
			eb.setError();
			return;
		}

		Building building = Building.getBuilding(hoehle);
		if (building == null) {
			new Fehler(u + " - die Höhle [" + Codierung.toBase36(hoehle) + "] existiert nicht.", u);
			eb.setError();
			return;
		} else if (!building.getCoords().equals(u.getCoords())) {
			new Fehler(u + " - die Höhle [" + Codierung.toBase36(hoehle) + "] existiert nicht.", u);
			eb.setError();
			return;
		}
		
		if (u.getGebaeude() != building.getNummer()) {
			u.Enter(building);
		} else {
			new Info(u + " ist schon in " + building + ".", u);
		}
	}
	
}
