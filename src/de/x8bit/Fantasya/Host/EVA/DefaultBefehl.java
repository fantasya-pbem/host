package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.util.StringUtils;

public class DefaultBefehl extends EVABase
{
	public DefaultBefehl()
	{
		super("*", "Default-Befehl für Einheiten ohne Befehle");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(DefaultBefehl.class, 1, "^(faulenze)[n]?([ ]+(\\/\\/).*)?", "f", Art.LANG);
        bm.setKeywords("faulenze", "faulenzen");
        retval.add(bm);

        bm = new BefehlsMuster(DefaultBefehl.class, 2, "^@?(default) .+", "d", Art.KURZ);
		bm.setKeep(true);
        bm.setKeywords("default");
		retval.add(bm);
        return retval;
    }
	
	@Override
	public void PostAction() {
		// wer nichts langes getan hat - der soll halt faulenzen.
		for (Unit u : Unit.CACHE) {
            if ((!u.BefehleExperimental.containsLangenBefehl())
                    && (!u.BefehleExperimental.containsMultiLangenBefehl())
                    ) {
                u.BefehleExperimental.add(u, "FAULENZE");
                new Info(u + " faulenzt.", u);
                u.BefehleExperimental.last().setPerformed();
			}
		}
	}

	@Override
	public void PreAction() {
        // warnen, wenn mehrere DEFAULT-Befehle gegeben wurden:
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().getAll(this.getClass());
        Set<Unit> candidates = new HashSet<Unit>();
		for (Einzelbefehl eb : befehle) {
            if (eb.getVariante() == 2) candidates.add(eb.getUnit());
        }

        for (Unit u : candidates) {
            int cnt = 0;
            Einzelbefehl letzter = null;
            for (Einzelbefehl eb : u.BefehleExperimental) {
                if ((eb.getProzessor() == DefaultBefehl.class) && (eb.getVariante() == 2)) {
                    // wir wollen nur genau ein Token - den neuen Befehl
                    if (eb.getTokens().length > 2) {
                        eb.combineTokens(1, eb.getTokens().length - 1);
                    }
                    if (eb.getTokens().length < 2) {
                        continue;
                    }

                    Einzelbefehl inhalt = null;
					try {
						inhalt = new Einzelbefehl(u, u.getCoords(), eb.getTokens()[1], -1);
					} catch(IllegalArgumentException ex) {
						new Debug("DEFAULT-PreAction() - Fehler: " + ex.getMessage());
						continue;
					}
                    if (inhalt.getMuster() == null) continue;
                    if (inhalt.getArt().equals(Art.LANG)) {
                        cnt ++;
                        letzter = inhalt;
                    }
                }
            }

            if (cnt > 1) {
                new Fehler("Warnung: Die Einheit hat mehrere lange DEFAULT-Befehle bekommen, "
                       + "'" + letzter.getBefehlCanonical() + "' wird benutzt.", u);
            }
        }
    }

    @Override
    public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

            // FAULENZE ...
            if (eb.getVariante() == 1) {
                if (u.getCoords().getWelt() != 0) new Fehler(u + " faulenzt.", u);
                eb.setPerformed();
                continue;
            }
            
            // DEFAULT ...
            if (eb.getVariante() == 2) {

                // wir wollen nur genau ein Token - den neuen Befehl
                if (eb.getTokens().length > 2) {
                    eb.combineTokens(1, eb.getTokens().length - 1);
                }
                if (eb.getTokens().length < 2) {
                    eb.setError();
                    new Fehler(u + " - DEFAULT funktioniert nur, wenn ein gültiger Befehl angehängt wird.", u);
                    continue;
                }


				Einzelbefehl inhalt = null;
				try {
					inhalt = new Einzelbefehl(u, u.getCoords(), eb.getTokens()[1], -1);
				} catch(IllegalArgumentException ex) {
                    eb.setError();
					new Debug("DEFAULT-Verarbeitung-Fehler: " + ex.getMessage());
                    new Fehler(u + " - DEFAULT '" + eb.getTokens()[1] + "' ist kein gültiger Befehl.", u);
					continue;
				}
                if (inhalt.getMuster() == null) {
                    eb.setError();
                    new Fehler(u + " - DEFAULT '" + eb.getTokens()[1] + "' ist kein gültiger Befehl.", u);
                    continue;
                }

                if (inhalt.getMuster().getArt().equals(Art.LANG)) {
                    List<Einzelbefehl> loeschliste = new ArrayList<Einzelbefehl>();
                    List<String> neuliste = new ArrayList<String>();
                    // bestehenden langen Befehl auskommentieren
                    for (Einzelbefehl alt : u.BefehleExperimental) {
                        if (alt.getMuster().getArt().equals(Art.LANG)) {
                            loeschliste.add(alt);
							if ((alt.getTokens() != null) && (alt.getTokens().length > 0)) {
								String auskommentiert = "; Befehl war: " + StringUtils.join(alt.getTokens(), " ");
								neuliste.add(auskommentiert);
							}
                        }
                    }
                    for (Einzelbefehl alt : loeschliste) {
						u.BefehleExperimental.remove(alt);
						BefehlsSpeicher.getInstance().remove(alt);
					}
                    for (String auskommentiert : neuliste) {
                        u.BefehleExperimental.add(u, auskommentiert);
                        u.BefehleExperimental.last().setPerformed();  // ist ja jetzt eh ein Kommentar
						u.BefehleExperimental.last().setKeep(true); // behalten, obwohl es ein kurzer Kommentar ist
                    }
                }

                try {
                    u.BefehleExperimental.add(u, inhalt.getBefehlCanonical());
                } catch(IllegalArgumentException ex) {
                    new Fehler("DEFAULT-Befehl: " + ex.getMessage(), u, u.getCoords());
                }
				u.BefehleExperimental.last().setPerformed();  // Sollte diese Runde ja nicht abgearbeitet werden.
				u.BefehleExperimental.last().setKeep(true);  // Aber behalten wollen wir ihn.

				u.Befehle.clear();
				for (Einzelbefehl b : u.BefehleExperimental) u.Befehle.add(b.getBefehlCanonical());

                eb.setPerformed();
				if (!eb.isAlways()) eb.setKeep(false); // nur wenn @ vorangestellt war, den DEFAULT .....-Befehl selbst behalten

                new Info(u + " wird versuchen, im nächsten Monat '" + u.BefehleExperimental.last().getBefehlCanonical() + "' auszuführen, wenn keine anderen Befehle eintreffen.", u);
            }
        } // next Einzelbefehl
        
    }

    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
	public void DoAction(Einzelbefehl eb) { }
    
}
