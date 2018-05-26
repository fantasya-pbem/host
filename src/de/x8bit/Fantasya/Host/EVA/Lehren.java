package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.util.Codierung;

public class Lehren extends EVABase
{
    /**
     * Map lehrerId => LehrenRecord, ist also eine "geflippte" Map gegenüber der gleichnamigen in Lernen.java
     */
    public static Map<Integer, List<LehrenRecord>> Unterricht = new HashMap<Integer, List<LehrenRecord>>();

    public Lehren()
	{
		super("lehre", "Lehren von Einheiten");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        BefehlsMuster bm = new BefehlsMuster(Lehren.class, 0, "^(lehre)[n]? .+([ ]+(\\/\\/).*)?", "l", Art.LANG);
        bm.setKeywords("lehre", "lehren");
        retval.add(bm);

        return retval;
    }
	
	public void DoAction(Region r, String dummy) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

            // COMMAND LEHRE <einheit> <einheit> <einheit>
            u.setLehrtage(u.getPersonen() * 300);	// jeder Lehrer hat 300 Lehrtage

            String[] befehl = eb.getTokens();
			List<String> neuerBefehl = new ArrayList<String>();
			neuerBefehl.add("LEHRE");

            for(int i = 1; i < befehl.length; i++) {
                int nummer = 0;
                if (befehl[i].equalsIgnoreCase("temp")) {
                    i++;
                    // test auf überlauf
                    if (i >= befehl.length) {
                        eb.setError();
                        new Fehler(u + " - welche Temp-Einheit soll gelehrt werden?", u);
                        continue;
                    }
                    nummer = Unit.getRealNummer(befehl[i], u);

                    if (nummer == 0) {
                        eb.setError();
                        new Fehler(u + " - kann Temp-Einheit '" + befehl[i]+ "' nicht finden.", u);
                        continue;
                    }

                } else {
                    // keine TEMP-Einheit:
                    try { 
                        nummer = Codierung.fromBase36(befehl[i]);
                    } catch(Exception ex) {
                        eb.setError();
                        new Fehler(u + " - kann Einheit " + befehl[i] + " nicht finden.", u);
                        continue;
                    }

                    if ((nummer <= 0) || (nummer > Codierung.fromBase36("zzzz"))) {
                        eb.setError();
                        new Fehler(u + " - ungültige Einheiten-Nummer beim Lehren: " + befehl[i] + ".", u);
                        continue;
                    }
                }

                if (nummer == 0) new BigError("Nanu, Nummer ist 0 beim Lehren?");

                Unit schueler = null;
                for(Unit s: r.getUnits()) if (s.getNummer() == nummer) schueler = s;
                if (schueler == null) {
                    new Fehler("Die Einheit [" + befehl[i] + "] ist nicht in dieser Region.", u);
                    eb.setError();
                    continue;
                }
				// KONTAKT soll nicht nötig sein (Mantis #302 - http://www.fantasya-pbem.de/mantis/view.php?id=302 )
//                if (!schueler.hatKontakt(u, AllianzOption.Kontaktiere)) {
//                    new Fehler(schueler + " hat keinen Kontakt zu uns.", u, u.getCoords());
//                    eb.setError();
//                    continue;
//                }

                LehrenRecord ausbildung = new LehrenRecord(u, schueler);

                List<LehrenRecord> meineKlasse = Lehren.Unterricht.get(u.getNummer());
                if (meineKlasse == null) {
                    meineKlasse = new ArrayList<LehrenRecord>();
                    Lehren.Unterricht.put(u.getNummer(), meineKlasse);
                }
                if (meineKlasse.contains(ausbildung)) {
                    eb.setError();
                    new Fehler(u + " lehrt den Schüler " + schueler + " bereits.", u);
                    continue;
                }

                meineKlasse.add(ausbildung);
				neuerBefehl.add(schueler.getNummerBase36());
                
            } // next Einheiten-ID (Schüler)

            // bereinigten Befehl setzen (TEMP-IDs ersetzt, ungültige gestrichen)
			eb.setTokens( neuerBefehl.toArray( new String[]{} ) );

			eb.setPerformed();
        }
    }
	public void PostAction() { }
	public void PreAction()	{
        // Lehrer-Schüler-Datenbank löschen
        Lehren.Unterricht.clear();
	}

    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public class LehrenRecord {
        final protected Unit lehrer;
        final protected Unit schueler;

        public LehrenRecord(Unit lehrer, Unit schueler) {
            this.lehrer = lehrer;
            this.schueler = schueler;
        }

        public Unit getLehrer() {
            return lehrer;
        }

        public Unit getSchueler() {
            return schueler;
        }



        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + (this.lehrer != null ? this.lehrer.hashCode() : 0);
            hash = 73 * hash + (this.schueler != null ? this.schueler.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof LehrenRecord)) return false;

            LehrenRecord other = (LehrenRecord) o;
            if (
                    (lehrer.getNummer() == other.getLehrer().getNummer())
                    && (schueler.getNummer() == other.getSchueler().getNummer())
            ) return true;

            return false;
        }

    }
	
    @Override
	public void DoAction(Einzelbefehl eb) { }

}
