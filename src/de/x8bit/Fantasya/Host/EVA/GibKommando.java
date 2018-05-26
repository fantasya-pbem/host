package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
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
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.UnitHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.util.Codierung;

public class GibKommando extends EVABase
{
	public GibKommando()
	{
		super("gib", "Übergaben der Befehlsgewalt");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		bm = new BefehlsMuster(GibKommando.class, 0, "^@?(gib) [a-z0-9]{1,4} (kommando)([ ]+(\\/\\/).*)?", "g", Art.KURZ);
		bm.addHint(new UnitHint(1));
        retval.add(bm);

		bm = new BefehlsMuster(GibKommando.class, 0 + EVABase.TEMP, "^@?(gib) (temp) [a-z0-9]{1,4} (kommando)([ ]+(\\/\\/).*)?", "g", Art.KURZ);
		bm.addHint(new UnitHint(1));
        retval.add(bm);
        return retval;
    }

	// COMMAND GIB <einheit> KOMMANDO
	public void DoAction(Region r, String dummy) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

            // testen wovon wir die Befehlsgewalt für irgendwas haben
            Ship s = null;
            Building b = null;
            if (u.getSchiff() != 0) s = Ship.Load(u.getSchiff());
            if (u.getGebaeude() != 0) b = Building.getBuilding(u.getGebaeude());
            if (s == null && b == null) {
                eb.setError();
                new Fehler("Hmm, tja - wir haben weder für ein Schiff noch für ein Gebäude die Befehlsgewalt.", u);
                continue;
            }

            String empfId = eb.getTargetUnit();
            if (eb.getMuster().isTempMuster()) {
                empfId = Codierung.toBase36(Unit.getRealNummer(empfId, u));
            }
            int empf = Codierung.fromBase36(empfId);

            if (empf == 0) {
                eb.setError();
                new Fehler("Die Nummer " + eb.getTargetUnit() + " der Einheit wurde nicht erkannt.", u, u.getCoords());
                continue;
            }
            
			Unit other = Unit.Get(empf);
			if (other == null) {
                eb.setError();
				new Fehler(u + " konnte die Einheit nicht finden, die das Kommando erhalten sollte.", u, u.getCoords());
				continue;
			}
			if (!other.hatKontakt(u, AllianzOption.Gib)) {
                eb.setError();
				new Info("Die Einheit " + other + " hat uns nicht kontaktiert.", u, u.getCoords());
                continue;
			}

            // Tu es!
            if (s == null) {
                // Gebäude:
                if (other.getGebaeude() == b.getNummer()) {
					Building.PROXY.remove(b);

                    b.setOwner(other.getNummer());

					Building.PROXY.add(b);
                    new Info("Das Kommando für " + b + " wurde an " + other + " übergeben.", u, u.getCoords());
                    if (u.getOwner() != other.getOwner()) new Info(other + " hat das Kommando für " + b + " von " + u + " erhalten.", other, other.getCoords());
                    eb.setPerformed();
                } else {
                    eb.setError();
                    new Fehler(u + " kann nicht das Kommando übergeben, " + other + " ist nicht im Gebäude.", u, u.getCoords());
                }
            } else {
                // Schiff
                if (other.getSchiff() == s.getNummer())	{
                    eb.setPerformed();
                    s.setOwner(other.getNummer());
                    new Info("Das Kommando für '" + s + "' wurde an '" + other + "' übergeben.", u, u.getCoords());
                    if (u.getOwner() != other.getOwner()) new Info(other + " hat das Kommando für '" + s + "' von '" + u + "' erhalten.", other, other.getCoords());
                } else {
                    eb.setError();
                    new Fehler(u + " kann nicht das Kommando übergeben, " + other + " ist nicht auf dem Schiff.", u, u.getCoords());
                }
            }
            
		} // Nächster Einzelbefehl
    }
        
	public void PostAction() { }
	public void PreAction() { }
    public boolean DoAction(Unit u, String[] befehl) { return false; }
	public void DoAction(Einzelbefehl eb) { }

}
