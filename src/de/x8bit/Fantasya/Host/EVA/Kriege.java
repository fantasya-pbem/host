package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.Host.EVA.util.UnitHint;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht;
import de.x8bit.Fantasya.Host.ZAT.Battle.GruppenBuendnisfall;
import de.x8bit.Fantasya.Host.ZAT.Battle.GruppenKonflikt;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.EinheitenZielvorgabe;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.ReihenZielvorgabe;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.Zielvorgabe;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collections;

/**
 * @author  mogel
 */
public class Kriege extends EVABase {

    public final static int ANGRIFF_BEFEHL = 1;
    public final static int PARTEIANGRIFF_BEFEHL = 2;
    public final static int ZIELREIHE_BEFEHL = 10;
    public final static int ZIELEINHEIT_BEFEHL = 20;

    /**
     * enthält die Beute-Vorlieben aller Einheiten, soweit sie per 
     * SAMMEL BEUTE ... zum Ausdruck gebracht worden sind.
     */
    public final static Map<Unit, Integer> BEUTE_MODI = new HashMap<Unit, Integer>();

    public final static int BEUTE_NICHTS = 0;
    public final static int BEUTE_TRAGBAR = 50;
    public final static int BEUTE_ALLES = 100;


    public Kriege()	{
		super("attackiere", "Des Menschen liebstes Hobby: Krieg");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Kriege.class, ANGRIFF_BEFEHL, "^@?(attackiere)[n]?( [a-z0-9]{1,4})+([ ]+(\\/\\/).*)?", "a", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("attackiere", "attackieren");
        retval.add(bm);
        
        bm = new BefehlsMuster(Kriege.class, PARTEIANGRIFF_BEFEHL, "^@?(attackiere)[n]? (partei)( [a-z0-9]{1,4})+([ ]+(\\/\\/).*)?", "a", Art.KURZ);
        bm.addHint(new IDHint(2));
        bm.setKeywords("attackiere", "attackieren");
        retval.add(bm);

        bm = new BefehlsMuster(Kriege.class, ZIELREIHE_BEFEHL, "^@?(attackiere)[n]? ((vorne)|(hinten))([ ]+(\\/\\/).*)?", "a", Art.KURZ);
        bm.setKeywords("attackiere", "attackieren", "vorne", "hinten");
        retval.add(bm);

        bm = new BefehlsMuster(Kriege.class, ZIELEINHEIT_BEFEHL, "^@?(attackiere)[n]?( gezielt)( [a-z0-9]{1,4})+([ ]+(\\/\\/).*)?", "a", Art.KURZ);
        bm.setKeywords("attackiere", "attackieren", "gezielt");
        retval.add(bm);

        return retval;
    }
	
    @Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }

    @Override
	public void PostAction() { }

    @Override
	public void PreAction() { }
	
    @Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());
		
        Map<Unit, Set<Unit>> angriffe = new HashMap<Unit, Set<Unit>>();
        Map<Unit, Zielvorgabe> zielvorgaben = new HashMap<Unit, Zielvorgabe>();

        for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());
            Unit unit = eb.getUnit();

            if (eb.getVariante() == ANGRIFF_BEFEHL) {
                // Einzelangriffe auflisten:
                List<Unit> feinde = angreifen(eb, 1); // Einheiten-Nummern ab Token 1
                if (feinde != null) {
                    for (Unit feind : feinde) {
                        if (!angriffe.keySet().contains(unit)) angriffe.put(unit, new HashSet<Unit>());
                        angriffe.get(unit).add(feind);
                    }
                } else {
                	new Fehler("keine Temp-Einheiten für Kriege möglich", unit);
                }

                if (!eb.isError()) eb.setPerformed();
            }

            if (eb.getVariante() == PARTEIANGRIFF_BEFEHL) {
                // Einzelangriffe auflisten:
                List<Unit> feinde = parteiAngreifen(eb);
                for (Unit feind : feinde) {
                    if (!angriffe.keySet().contains(unit)) angriffe.put(unit, new HashSet<Unit>());
                    angriffe.get(unit).add(feind);
                }

                if (!eb.isError()) eb.setPerformed();
            }

            if (eb.getVariante() == ZIELREIHE_BEFEHL) {
                int reihe = -1;
                if (eb.getTokens()[1].equalsIgnoreCase(Kampfposition.Vorne.name())) reihe = 1;
                if (eb.getTokens()[1].equalsIgnoreCase(Kampfposition.Hinten.name())) reihe = 2;
                if (reihe == -1) {
                    eb.setError();
                    new Fehler(unit + " - Zielreihe nicht erkannt.", unit);
                } else {
                    zielvorgaben.put(unit, new ReihenZielvorgabe(reihe));
                    eb.setPerformed();
                }
            }

            if (eb.getVariante() == ZIELEINHEIT_BEFEHL) {
                // Einzelangriffe auflisten:
                List<Unit> feinde = angreifen(eb, 2); // Einheiten-Nummern ab Token 2 ( ATTACKIERE GEZIELT abc 123 mno pqr )
                for (Unit feind : feinde) {
                    // zielen reicht auch als "Ersatz" für das normale ATTACKIERE abc :
                    if (!angriffe.keySet().contains(unit)) angriffe.put(unit, new HashSet<Unit>());
                    angriffe.get(unit).add(feind);
                }

                if (!feinde.isEmpty()) {
                    Set<Unit> ziele = new HashSet<Unit>();
                    ziele.addAll(feinde);
                    zielvorgaben.put(unit, new EinheitenZielvorgabe(ziele));
                    eb.setPerformed();
                } else {
                    eb.setError();
                    eb.setPerformed();
                }
            }
        }
		
		if (angriffe.isEmpty()) {
			new Debug("Oh, doch nicht - es sind keine gültigen Kampf-Paarungen zustande gekommen.");
			return;
		}

        // dieser Bündnisfall enthält erstmal alle Angriffe zusammen:
        Set<GruppenKonflikt> konflikte = GruppenBuendnisfall.ausEinheitenAngriffen(angriffe);
        // new Debug("Alle Konflikte in " + r + " " + r.getCoords() + " auf Gruppen-Ebene:\n" + konflikte);

        // alle Einheiten heraussuchen, die via ATTACKIERE explizit angegriffen werden,
        // das kann verwirrenderweise auch implizit via ATTACKIERE PARTEI ... sein
        Set<Unit> explizitAngegriffene = new HashSet<Unit>();
        for (Unit att : angriffe.keySet()) {
            explizitAngegriffene.addAll(angriffe.get(att));
        }

        // alle unabhängigen Konflikte heraussuchen
        // konflikte = alles.vereinzeln();


		// alle Kämpfe durchlaufen
        for (GruppenKonflikt konflikt : konflikte) {
            new Debug("Konflikt:\n" + konflikt);

            // an einem normalen Regions-Kampf nehmen alle Einheiten der beteiligten Völker teil
            Gefecht gefecht = new Gefecht(r, konflikt, Collections.unmodifiableSet(explizitAngegriffene));
            gefecht.setZielVorgaben(zielvorgaben);

            gefecht.austragen();
        }

	}
	
    /**
     * @param eb Befehlsvariante ATTACKIERE PARTEI [id] ... ...
     * @return
     */
    private List<Unit> parteiAngreifen(Einzelbefehl eb) {
        if (eb.getVariante() != PARTEIANGRIFF_BEFEHL) throw new RuntimeException("Unerwarteter Befehl in parteiAngreifen(): " + eb.getBefehlCanonical());

        // Angaben über die "Akteure" selbst:
        Unit unit = eb.getUnit();
        Region r = Region.Load(unit.getCoords());
        Partei wir = Partei.getPartei(unit.getOwner());

        // TODO Temp-Einheiten werden nicht in den Kampf gezogen?
        if (unit.getTempNummer() > 0) return new ArrayList<Unit>();

        List<Unit> retval = new ArrayList<Unit>();

        for (int i = 2; i < eb.getTokens().length; i++) {
            String targetId = eb.getTokens()[i];

            if (targetId == null) {
                if (unit.getPersonen() == 1) new Fehler(unit + " weiß nicht, wen er/sie angreifen soll.", unit);
                if (unit.getPersonen() != 1) new Fehler(unit + " wissen nicht, wen sie angreifen sollen.", unit);
                continue;
            }

            if (targetId.startsWith("//")) break; // ab hier Zeilen-Kommentar

            // Nummer holen
            int nummer = -1;
            try { nummer = Codierung.fromBase36(targetId); } catch(Exception ex) { /* nüschts */ }
            if (nummer == -1) {
                new Fehler(unit + " - die Feind-Partei [" + targetId + "] wurde nicht erkannt.", unit);
                continue;
            }

            // Feinde holen
            Partei enemies = Partei.getPartei(nummer);
            if (enemies == null) {
                new Fehler(unit + " - die Feind-Partei [" + targetId + "] wurde nicht erkannt.", unit);
                continue;
            }

            // Angriff auf eigene Partei testen
            if (wir.equals(enemies)) {
                new Fehler(unit + " probt den Aufstand - aber: Is nich.", unit);
                continue;
            }


            // endlich: Die Einheiten auf die Zielliste setzen
            for (Unit enemy : r.getUnits()) {
                if (enemy.getOwner() == wir.getNummer()) continue;
                new Debug("Tarnpartei von " + enemy + " ist " + Partei.getPartei(enemy.getTarnPartei()) + ".");
                
                // die ParteiTARNUNG entscheidet:
                if (enemy.getTarnPartei() != enemies.getNummer()) continue;
                
                if (!wir.cansee(enemy)) continue;
                
                new Debug("Einheit " + enemy + " von " + Partei.getPartei(enemy.getOwner()) + " wird wegen Tarn-Parteizugehörigkeit zu " + Partei.getPartei(enemy.getTarnPartei()) + " auf die Liste der Ziele gesetzt.");
                retval.add(enemy);
            }
        } // nächste Partei-ID
        
        if (retval.isEmpty()) {
            new Fehler(unit + " hat keinen Gegner gefunden.", unit);
            eb.setError();
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("ATTACKIERE ");
            for (Unit feind : retval) sb.append(feind.getNummerBase36()).append(" ");
            sb.delete(sb.length() - 1, sb.length()); // letztes Leerzeichen entfernen
            new Info(eb.getBefehlCanonical() + " entsprach: " + sb, unit);
        }

        return retval;
    }
	
     /**
      * Kämpfe in der Region ausknobeln ... wer attackiert hier wen
      * @param eb Ein einzelner ATTACKIERE-Befehl (wird in die Listen-Strukturen aufgenommen)
      */
	private List<Unit> angreifen(Einzelbefehl eb, int startToken) {
        // Angaben über die "Akteure" selbst:
        Unit unit = eb.getUnit();
        Partei wir = Partei.getPartei(unit.getOwner());

        // TODO Temp-Einheiten werden nicht in den Kampf gezogen?
        if (unit.getTempNummer() > 0) return new ArrayList<Unit>();

        List<Unit> retval = new ArrayList<Unit>();
        
        for (int i = startToken; i < eb.getTokens().length; i++) {
            String targetId = eb.getTokens()[i];
            
            if (targetId == null) {
                if (unit.getPersonen() == 1) new Fehler(unit + " weiß nicht, wen er angreifen soll.", unit);
                if (unit.getPersonen() != 1) new Fehler(unit + " wissen nicht, wen sie angreifen sollen.", unit);
                continue;
            }

            if (targetId.startsWith("//")) break; // ab hier Zeilen-Kommentar

            // Nummer holen
            int nummer = -1;
            try { nummer = Codierung.fromBase36(targetId); } catch(Exception ex) { /* nüschts */ }
            if (nummer == -1) {
                new Fehler(unit + " - der Feind [" + targetId + "] wurde nicht erkannt.", unit);
                continue;
            }

            // Feind holen
            Unit enemy = Unit.Load(nummer);
            if (enemy == null) {
                new Fehler(unit + " - der Feind [" + targetId + "] ist nicht auffindbar, Paranoia?", unit);
                continue;
            } else if (!wir.cansee(enemy)) {
                new Fehler(unit + " - der Feind [" + targetId + "] ist nicht auffindbar, Paranoia?", unit);
                continue;
            } else if (!unit.getCoords().equals(enemy.getCoords())) {
                // nicht in der gleichen Region!
                new Fehler(unit + " - der Feind [" + targetId + "] ist nicht auffindbar, Paranoia?", unit);
                continue;
            } else if (enemy.getPersonen() <= 0) {
                new Fehler(unit + " - der Feind [" + targetId + "] hat sich bereits von selbst erledigt.", unit);
                continue;
			}


            // Angriff auf eigene Einheiten testen
            if (unit.getOwner() == enemy.getOwner()) {
                new Fehler(unit + " wollte " + enemy + " angreifen, der Amoklauf an einer eigenen Einheit konnte aber verhindert werden.", unit, unit.getCoords());
                continue;
            }

            // keine Temp-Einheiten
            if (unit.getTempNummer() > 0 || enemy.getTempNummer() > 0) {
                new Debug("TEMP-Kampf unterbunden: " + unit + "(" + unit.getTempNummer() + ") vs." + enemy + "(" + enemy.getTempNummer() + ").");
                continue;
            }

            // Allianzen werden nicht überprüft !! ... wozu auch :D

            if (!retval.contains(enemy)) retval.add(enemy);
        }

        if (retval.isEmpty()) {
            new Fehler(unit + " hat keinen Gegner gefunden.", unit);
            eb.setError();
        }
        
        return retval;
	}
	
	/**
	 * räumt den Angriff auf .. d.h. auf Seiten des Opfers werden alle Alliierte mit hinzu
	 * gezogen, außer sie stehen auf der Angriffsseite
	 * <br/><br/>
	 * wenn die Funktion fertig ist, dann steht in <i>defenders</i> und in <i>attackers</i>
	 * jeder für jede Seite drinnen wer an diesem Kampf beteiligt ist
	 * @param victim
	 * @param volk_attackers
	 */
//	private void ClearFight(Partei victim, ArrayList<Unit> attackerlist)
//	{
//		volk_defenders = new ArrayList<Partei>();
//		volk_attackers = new ArrayList<Partei>();
//
//		// als erste kommt natürlich die Partei des Opfers hinein
//		volk_defenders.add(victim);
//
//		// dann werden alle Alliierte in der Region gesucht
//		for(Unit unit : region.getUnits())
//		{
//			Partei p = Partei.Load(unit.getOwner());
//			if (victim.hatAllianz(p.getNummer(), AllianzOption.Kaempfe)) addPartei(p, volk_defenders);
//		}
//
//		// jetzt werden alle Angreifer gesucht ... dazu wird die attackerlist
//		// benötigt ... da nur wer angreift auch auf der Angreiferseite steht
//		// hier zählen Allianzen nicht
//		for(Unit unit : attackerlist)
//		{
//			Partei attacker = Partei.Load(unit.getOwner());
//
//			// erstmal schauen ob die Partei in der Verteiger-Liste ist ... also
//			// ob eigentlich eine Allianz besteht (so ein Fiesling ^^)
//			volk_defenders.remove(attacker);	// meckert nicht, wenn nicht vorhanden
//
//			// dann einfach der Liste der Angreifer hinzufügen
//			addPartei(attacker, volk_attackers);
//		}
//	}
	
	/**
	 * fügt eine Partei einer Liste hinzu
	 * @param partei - die Partei die hinzugefügt werden soll
	 * @param alp - die Liste, entwerder Angreifer oder Verteidiger
	 */
//	private void addPartei(Partei partei, ArrayList<Partei> alp) {
//		boolean found = false;
//		for(Partei p : alp)	{
//			if (p.getNummer() == partei.getNummer())
//			{
//				found = true;
//				break;
//			}
//		}
//		if (!found) alp.add(partei);
//	}
	
	
    @Override
	public void DoAction(Einzelbefehl eb) { }

}
