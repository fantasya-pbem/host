package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.Battle;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Taktik;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Spell.ConfusionSpell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.Kampfzauber;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WWaffenlos;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.DuellAnalyse;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.EinheitenZielvorgabe;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.KampfSimulator;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.KampfreportXML;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.ReihenZielvorgabe;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.ZielChancenModell;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.Zielvorgabe;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;
import de.x8bit.Fantasya.util.comparator.UnitSortierungComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hb
 */
public final class Gefecht {
	
	public enum BattleTime
	{
		ROUND, ATTACK_ATTACKER, ATTACK_DEFENDER;
	}

	/** hier wird gespielt */
	private final Region region;

	/** Liste der Angreifer in diesem Kampf */
	private List<Partei> angreiferParteien = null;

	/** Liste der Verteidiger in diesem Kampf */
	private List<Partei> verteidigerParteien = null;

    private Set<Unit> explizitAngegriffene = null;

    /** Ausgangskonflikt dieses Gefechts - erstellt aus den ATTACKIERE-Befehlen */
    private GruppenKonflikt konflikt;
    
    /** Mögliche Angaben zu bevorzugten Zielen einzelner Einheiten - Varianten des ATTACKIERE-Befehls */
    Map<Unit, Zielvorgabe> zielvorgaben = null;

    /**
     * Liste der unbeteiligten Beobachter des Gefechts
     */
    private List<Partei> beobachterParteien = new ArrayList<Partei>();

    /**
     * diese Parteien bekommen detaillierte Infos zum Kampfgeschehen:
     */
    private List<Partei> detailInfos = new ArrayList<Partei>();

	/**
	 * Aufstellung der Angreifer
	 */
	private Side angreiferSeite = null;

	/**
	 * Aufstellung der Verteidiger
	 */
	private Side verteidigerSeite = null;

    private KampfrundenStatistik ks = null;

    /**
     * menschenlesbare Bezeichnung des Gefechts - das Handgemenge, die Schlacht, die epische Schlacht.....
     */
    private String wasIssesBestimmt;


    public Gefecht(Region region, GruppenKonflikt konflikt, Set<Unit> explizitAngegriffene) {
        this.region = region;
        this.konflikt = konflikt;

		// Parteien für die Meldungen sammeln
        beobachterParteien.add(Partei.getPartei(0));
        if (!KampfSimulator.AKTIV) {
            beobachterParteien.addAll(region.anwesendeParteien());
        }

        angreiferParteien = new ArrayList<Partei>();
        for (Partei p : konflikt.getParteienSeiteA()) {
            // angreifer enthält auch "theoretische" Alliierte; d.h. solche, die hier gar keine Einheiten haben.
            if (!region.getUnits(p).isEmpty()) angreiferParteien.add(p);
        }
        for (Partei p : angreiferParteien) beobachterParteien.remove(p);

        verteidigerParteien = new ArrayList<Partei>();
        for (Partei p : konflikt.getParteienSeiteB()) {
            // verteidiger enthält auch "theoretische" Alliierte; d.h. solche, die hier gar keine Einheiten haben.
            if (!region.getUnits(p).isEmpty()) verteidigerParteien.add(p);
        }
        for (Partei p : verteidigerParteien) beobachterParteien.remove(p);

        this.explizitAngegriffene = explizitAngegriffene;

		detailInfos.add(Partei.getPartei(0));
        if (!KampfSimulator.AKTIV) {
            detailInfos.addAll(this.angreiferParteien);
            detailInfos.addAll(this.verteidigerParteien);
        }

        // alle Einheiten der Parteien beteiligen sich - hierbei werden auch
        // die beiden "Sides" gesetzt.
        addAlleEinheiten(konflikt);
        
        wasIsses(); // Meldung: "Es gibt eine Schlacht ..."
        
        meldeAbgebrocheneAngriffe();
    }
    
    

    /**
     * übernimmt Zielvorgaben für dieses Gefecht. Zielvorgaben von "fremden" 
     * Einheiten werden ignoriert.
     * @param zielvorgaben
     */
    public void setZielVorgaben(Map<Unit, Zielvorgabe> zielvorgaben) {
        this.zielvorgaben = new HashMap<Unit, Zielvorgabe>();

        Set<Integer> parteien = new HashSet<Integer>();
        for(Partei p : this.getInvolvierte()) parteien.add(p.getNummer());

        for (Unit key : zielvorgaben.keySet()) {
            if (!parteien.contains(key.getOwner())) continue; // fremde Zielvorgaben interessieren nicht.

            this.zielvorgaben.put(key, zielvorgaben.get(key));
        }
    }
    
    /**
     * @return die aktuellen Zielvorgaben im Gefecht; sowohl der Angreifer als auch der Verteidiger
     */
    public Map<Unit, Zielvorgabe> getZielvorgaben() {
        return zielvorgaben;
    }

    public Set<Unit> getExplizitAngegriffene() {
        return explizitAngegriffene;
    }

    public void setExplizitAngegriffene(Set<Unit> explizitAngegriffene) {
        this.explizitAngegriffene = explizitAngegriffene;
    }


    public void wasIsses() {
//        String wasIssesUnbestimmt = "ein Handgemenge";
//        int n = Math.min(angreiferSeite.getPersonen(), verteidigerSeite.getPersonen());
//        if (n > 10) wasIssesUnbestimmt = "einen Kampf";
//        if (n > 100) wasIssesUnbestimmt = "eine Schlacht";
//        if (n > 10000) wasIssesUnbestimmt = "eine epische Schlacht";
//
//        wasIssesBestimmt = "das Handgemenge";
//        if (n > 10) wasIssesBestimmt = "den Kampf";
//        if (n > 100) wasIssesBestimmt = "die Schlacht";
//        if (n > 10000) wasIssesBestimmt = "die epische Schlacht";

        // alle Parteien werden über den Kampf informiert - "von Hand" wegen der privaten Koordinaten
		Set<Partei> zeugen = getRegion().anwesendeParteien();
		zeugen.add(Partei.getPartei(0));
        for (Partei p : zeugen) {
            String msg =
                    getRegion() + " " +
                    p.getPrivateCoords(getRegion().getCoords()) + ": " +
                    StringUtils.ucfirst(angreiferSeite.beschreibeTeilnehmer(p)) +
                    " greifen " + verteidigerSeite.beschreibeTeilnehmer(p) +
                    " an.\n";
            new Battle(">> " + msg, p); // Damit wird auch die NR2-Sektion eröffnet
            
            // Und nochmals als Info, für die normalen Report-Meldungen:
            if (p.getNummer() != 0) {
                Unit jaDu = getRegion().getUnits(p.getNummer()).first();
                if (jaDu != null) new Info(msg.trim(), jaDu);
            }

            if (getInvolvierte().contains(p)) {
                if (angreiferParteien.contains(p)) {
                    angreiferSeite.bericht(p).setKampfBeschreibung(msg);
                    angreiferSeite.bericht(p).setBeteiligte(angreiferSeite, verteidigerSeite);
                }
                if (verteidigerParteien.contains(p)) {
                    verteidigerSeite.bericht(p).setKampfBeschreibung(msg);
                    verteidigerSeite.bericht(p).setBeteiligte(angreiferSeite, verteidigerSeite);
                }
            }
        }
    }

    
    public void addAlleEinheiten(GruppenKonflikt konflikt) {
        // hier werden auch die Einheiten in Krieger aufgeteilt:
        angreiferSeite = new Side(konflikt.getSeiteA(), getRegion(), this, "Angreifer");
        verteidigerSeite = new Side(konflikt.getSeiteB(), getRegion(), this, "Verteidiger");

        // damit KriegerTyp.AusruestungsLegende() funktioniert:
        List<Krieger> alle = new ArrayList<Krieger>();
        alle.addAll(angreiferSeite.getKrieger());
        alle.addAll(verteidigerSeite.getKrieger());
        for (Krieger k : alle) {
            @SuppressWarnings("unused")
			KriegerTyp dummy = KriegerTyp.getInstance(k);
        }
    }

    /**
     * <p>Wird nur einmal vor Beginn eines Gefechts aufgerufen: </p>
     * <p>Wenn die Krieger mit Ausrüstung ausgestattet werden -
     * Angriffszauber sind so gestaltet, dass sie eine Angriffswaffe liefern.</p>
	 * @param side aktive Seite
	 */
    public void kampfMagie(Side side) {
        for (Partei p : side.getParteien()) {
            for (Unit mage : side.getUnits(p)) {
                if (mage.Talentwert(Magie.class) == 0) continue; // kein Magier

                // testen ob Angriffszauber gesetzt
                if (!mage.hasProperty(Kampfzauber.ATTACKSPELL)) {
                    new Debug("Magier " + mage + " hat keinen Kampfzauber gesetzt.");
                    continue;
                } // kein AS gesetzt
                
                String as = mage.getStringProperty(Kampfzauber.ATTACKSPELL);
                // handelt es sich um einen "nackten" Zaubernamen?
                Class<? extends Spell> sp = BefehlsMuster.SPELL_KEY.get(as.toLowerCase());
                if (sp != null) {
                    as = "ZAUBERE " + as;
                }


                for (Krieger k : side.getKrieger(mage)) {
                    String me = mage.toString();
                    if (mage.getPersonen() > 1) me = k.toString();
                    
                    // Spruch vorbereiten
                    Einzelbefehl eb = null;
                    try {
                        eb = new Einzelbefehl(mage, mage.getCoords(), as, 0);
                    } catch(IllegalArgumentException ex) {
                        try {
                            eb = new Einzelbefehl(mage, mage.getCoords(), "ZAUBERE " + as, 0);
                        } catch(IllegalArgumentException ex2) {
                            new Debug("IllegalArgumentException für AttackSpell '" + as + "'");
                            /* nop */
                        }
                    }

                    if (eb == null) {
                        new Fehler(me + ": '" + as + "' ist kein gültiger Kampfzauber-Befehl.", mage);
                        continue;
                    }

                    Spell spell = null;
                    try {
                        spell = eb.getSpell().newInstance();
                    } catch (Exception ex) {
                        new BigError(ex);
                    }

                    if (spell == null) {
                        new Fehler(me + " - Kampfzauber '" + as + "' ist irgendwie nicht recht spruchreif.", mage);
                        continue;
                    }
                    if (!spell.canUsedBy(mage)) {
                        new Fehler(me + " - Kampfzauber '" + as + "' ist zu kompliziert.", mage);
                        continue;
                    }
                    if (!(spell instanceof Spell.AttackSpell)) {
                        new Fehler(me + " - Kampfzauber '" + as + "' ist kein Angriffszauber.", mage);
                        continue;
                    }

                    // Zaubern
                    int stufe = ((Spell.AttackSpell) spell).ExecuteSpell(k, eb.getTokens());

                    /*
                    int a = spell.isOrcus() ? mage.getMana() : mage.getAura();
                    a -= stufe * spell.getStufe();
                    if (spell.isOrcus()) mage.setMana(a); else mage.setAura(a);
					*/
                    // String kosten = stufe * spell.getStufe() + (spell.isOrcus()?" Punkte Mana":" Punkte Aura");
                    // String neueEnergie = (spell.isOrcus()?mage.getMana():mage.getAura()) + (spell.isOrcus()?" Mana":" Aura");

                    // alle Informieren
                    geheimMeldung( p,
                            me + " bereitet " + spell.getName() + " auf Stufe " + stufe + " vor.", // für " + kosten + " vor und hat nun noch " + neueEnergie + ".", // eigene Partei
                            me + " bereitet " + spell.getName() + " vor.", // Freunde
                            null, // Feinde
                            null // Beobachter
                    );


                }
            }
        }
    }

    /**
     * @return Die aktiv an diesem Kampf beteiligten Parteien (ohne reine Beobachter).
     */
    public Set<Partei> getInvolvierte() {
        Set<Partei> retval = new HashSet<Partei>();
        retval.addAll(angreiferParteien);
        retval.addAll(verteidigerParteien);
        return retval;
    }

    private void ersteReihenAuffuellen() {
        int angreiferCnt = angreiferSeite.vorne.size();
        int verteidigerCnt = verteidigerSeite.vorne.size();

        if (angreiferCnt * 3 < verteidigerCnt) {
            KriegerCounter kc = angreiferSeite.frontVerstaerken((verteidigerCnt-1) / 3 + 1);
            if (kc.isEmpty()) {
                // meldung("Es ist niemand mehr da, um die erste Reihe der Angreifer zu verstärken.", false);
            } else {
                meldung(kc.getReportPhrase() + " verstärken die erste Reihe der " + angreiferSeite.getName() + ".", false);
                // Aufstellung wird eh am Ende der Kampfrunde komplett ausgegeben
                // meldung("Neue Aufstellung:\n" + TagDerWahrheit("Angreifer", angreiferSeite), false);
            }
        }

        if (verteidigerCnt * 3 < angreiferCnt) {
            KriegerCounter kc = verteidigerSeite.frontVerstaerken((angreiferCnt-1) / 3 + 1);
            if (kc.isEmpty()) {
                // meldung("Es ist niemand mehr da, um die erste Reihe der Verteidiger zu verstärken.", false);
            } else {
                meldung(kc.getReportPhrase() + " verstärken die erste Reihe der " + verteidigerSeite.getName() + ".", false);
                // Aufstellung wird eh am Ende der Kampfrunde komplett ausgegeben
                // meldung("Neue Aufstellung:\n" + TagDerWahrheit("Verteidiger", verteidigerSeite), false);
            }
        }
    }

    private void zivilistenAuffuellen() {
        // 3. Reihe ggf. an die Front ... Zivilisten, arme Schweine
		if (angreiferSeite.vorne.isEmpty() && angreiferSeite.hinten.isEmpty()) {
            if (!angreiferSeite.nicht.isEmpty()) {
                angreiferSeite.zivilVerteidigung();
                meldung(angreiferSeite.getName() + ": Es sind keine Kämpfer mehr zum Schutz der Bevölkerung vorhanden - die Zivilisten müssen jetzt kämpfen.", true);
            }
        }
		if (verteidigerSeite.vorne.isEmpty() && verteidigerSeite.hinten.isEmpty()) {
            if (!verteidigerSeite.nicht.isEmpty()) {
                verteidigerSeite.zivilVerteidigung();
                meldung(verteidigerSeite.getName() + ": Es sind keine Kämpfer mehr zum Schutz der Bevölkerung vorhanden - die Zivilisten müssen jetzt kämpfen.", true);
            }
        }
    }

	/**
     * <p>Allseitige Kampfrunde - Ansatz April 2011: Es gibt keine zeitliche Trennung von Angreifern und Verteidigern, alles gleichzeitig.</p>
     * <p>Die Methode sollte <strong>NICHT</strong> von außen aufgerufen werden; das wird nur für z.B. DuellAnalyse gebraucht!</p>
     * @param vorteil entweder null für eine normale Kampfrunde; oder die Seite, die eine "Taktikerrunde" bekommt, d.h. nur deren Krieger greifen an.
     */
    public void kampfrunde(Side vorteil) {
        if (ks == null) ks = new KampfrundenStatistik();

        meldung(">> -- Schlagabtausch -----------------------------------------------\n", true);

        if (!DuellAnalyse.AKTIV) {
            // also bei jedem echten Gefecht!
            ersteReihenAuffuellen();
            zivilistenAuffuellen();
        }

        List<Krieger> aktive = new ArrayList<Krieger>();
        
        if (vorteil != null) {
            aktive.addAll(vorteil.vorne);
            aktive.addAll(vorteil.hinten);
        } else {
            aktive.addAll(angreiferSeite.vorne);
            aktive.addAll(angreiferSeite.hinten);
            aktive.addAll(verteidigerSeite.vorne);
            aktive.addAll(verteidigerSeite.hinten);
        }

        // Ansatz im April 2011: Alle Einzelangriffe werden "gleichzeitig" ausgetragen, d.h. keine Trennung zwischen Angreifer und Verteidiger
        Collections.shuffle(aktive);


		// über alle Einheiten
        for (Krieger k : aktive) {
            if (k.istTot()) continue; // das kann ja durchaus passieren :-(

            // wer ist der Feind?
            Side feinde = null;
            if (verteidigerSeite.containsUnit(k.getUnit())) {
                feinde = angreiferSeite;
            } else if (angreiferSeite.containsUnit(k.getUnit())) {
                feinde = verteidigerSeite;
            }

            if (feinde == null) {
                throw new IllegalStateException("Krieger " + k + " ist weder auf der Seite der Angreifer noch der Verteidiger.");
            }
			if (!feinde.istLebendig()) break; // alle dooot.

            Zielvorgabe zielvorgabe = this.zielvorgaben.get(k.getUnit());

            int zielReihe = Krieger.REIHE_VORNE;
            if (zielvorgabe != null) {
                if (zielvorgabe instanceof ReihenZielvorgabe) zielReihe = zielvorgabe.getReihe();
            }
            if (feinde.vorne.isEmpty() && (zielReihe == Krieger.REIHE_VORNE)) zielReihe = Krieger.REIHE_HINTEN;

            // new Debug("Angriffswaffe wählen: " + k.detailedToString());
            // wenn (nur) Fernkampfwaffen vorhanden sind, wählt der Krieger selbst eine passende Zielreihe
            k.angriffsWaffeWaehlen(zielReihe); // hier wird usedWeapon gesetzt

            // wer hinten steht und keine Distanzwaffe hat - der steht da halt...
            if ((k.getReihe() == Krieger.REIHE_HINTEN) && (!(k.usedWeapon().istFernkampfTauglich()))) {
                new Debug("Krieger " + k + " steht ohne Distanzwaffe hinten:" + k.beschreibeAusruestung(true));
                continue;
            }

            int n = k.usedWeapon().numberOfAttacks();

            if (n == 0) {
                for (Partei p : detailInfos) new Battle(k.kurzCode() + " ist noch nicht bereit.", p);
                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(k.getUnit(), " die Waffe ist nicht einsatzbereit (Anzahl der Attacken = 0).");
                new Debug(k + " hat 0 Angriffe mit " + k.usedWeapon());
            }
            String meldungsPrefix = String.format("%-8s", k.kurzCode()) + " ";
            if (n > 1) {
                meldungsPrefix = "         ";
                for (Partei p : detailInfos) new Battle(k.kurzCode() + " \u00D7 " + n, p);

                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(k.getUnit(), "hat " + n + " Angriffe in einer Runde.");
                new Debug(k + " hat " + n + " Angriffe mit " + k.usedWeapon());
            }


            for (int i = 0; i < n; i++) {
                // Ziel suchen ..
                Krieger target = randomTarget(k, zielvorgabe);

                // wenn kein Target, weil die eigentlichen Ziele alle tot sind oder Katapulte eben
                // nicht mal die 1. Reihe angreifen (weil sie vorne sind), dann eben der nächste Kämpfer ... das
                // Ende des Kampfes wird bei Beginn einer Kampfrunde getestet
                if (target == null) break;

                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(k.getUnit(), "attackiert " + target + ".");


                // blabla an alle - jetzt direkt in _1on1_
                // meldung("\t" + k + " attackiert " + target + ".", false);

                // so ... (wer so sagt ist noch lange nicht fertig -.-) ... jetzt haben wir einen Angreifer
                // und einen Verteidiger ... der darf mal eben drauf losschlagen
                Einzelattacke resultat = Krieger._1on1_(k, target, ks);

                // Meldungen:
                // for (Partei p : detailInfos) new Battle(resultat.getBeschreibung(), p);
                for (Partei p : detailInfos) {
                    if (resultat.getVorwort().length() > 0) new Battle(resultat.getVorwort(), p);
					if (p.getNummer() == 0) {
						new Battle(meldungsPrefix + resultat.getKurzBeschreibung(p, true, true, true), p);
					} else {
						new Battle(meldungsPrefix + resultat.getKurzBeschreibung(p, false, false, false), p);
					}
                }

                for (Partei p : angreiferParteien) angreiferSeite.bericht(p).registriere(resultat);
                for (Partei p : verteidigerParteien) verteidigerSeite.bericht(p).registriere(resultat);

                // noch einige abschließende Dinge mit dem Opfer
                if (target.getLebenspunkte() >= target.getTrefferpunkte()) {
                    // ups ... der war einmal
                    heldenTod(feinde, target);
                    ks.recordTotschlag(k, target);
                    if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(k.getUnit(), " schlägt " + target + " tot.");
                    if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(target.getUnit(), " wird von " + k + " besiegt.");

                    // über Vernichtung informieren
                    Unit tUnit = target.getUnit();
                    if (tUnit.getPersonen() <= 0) { // hide a (possible) bug
                        meldung(k.getUnit() + " vernichtet " + tUnit + ".", true);
                        new Info(tUnit + " stirbt im Kampf.", tUnit);
                        tUnit.setPersonen(0);
                        for(Item item : tUnit.getItems()) if (item.getAnzahl() > 0) {
                            feinde.jackpot.add(item);
                        }
                        tUnit.clearItems();
                    }
                }

            } // nächster Angriff

            // Waffen für die nächste Runde vorbereiten (erhöht den Rundezähler bspw. für Katapulte):
            for (Weapon w : k.weapons) w.naechsteRunde();

        } // nächster getKrieger
        
        // Magische Effekte, die nach einer Kampfrunde abklingen, klingen hier ab.
        for (Krieger k : aktive) {
        	if (k.istTot()) continue; // das kann ja durchaus passieren :-(
        	
        	for(BattleEffects efx : k.getEffects()) {
            	efx.setEffectDownFor(Gefecht.BattleTime.ROUND);
            }
        }
        

        
        meldung("\n<<\n", true);
    }

	/**
	 * die Taktikerrunde
	 */
	private Side taktikerRunde() {
		// den besten von beiden Seite holen
		Unit aTaktiker = angreiferSeite.besterTaktiker();
		Unit dTaktiker = verteidigerSeite.besterTaktiker();

		// wenn eine Seite keinen Einheiten mehr, so wird null zurück geliefert !!!
		if (aTaktiker == null || dTaktiker == null) {
			meldung("Keine Taktikerrunde - kein Gegner mehr vorhanden.", true);
			return null;
		}
        
		if (aTaktiker.Talentwert(Taktik.class) == 0 && dTaktiker.Talentwert(Taktik.class) == 0) {
			meldung("Taktiker-Runde: Keine Seite hat einen taktischen Vorteil.", true);
			return null;
		}

		meldung("Taktiker-Runde:", true);
        ks = new KampfrundenStatistik();
        Side initiative = null;

		// Taktiker ausknobeln
        int twAngreifer = aTaktiker.Talentwert(Taktik.class);
        int twVerteidiger = dTaktiker.Talentwert(Taktik.class);
		int value = Random.rnd(1, twAngreifer + twVerteidiger + 1);
		if (value <= aTaktiker.Talentwert(Taktik.class)) {
			meldung(aTaktiker + " (T" + twAngreifer + ") erarbeitet einen taktischen Vorteil gegenüber " + dTaktiker + " (T" + twVerteidiger + ").", true);
            
            kampfrunde(angreiferSeite);

            initiative = angreiferSeite;
		} else {
			meldung(dTaktiker + " (T" + twVerteidiger + ") erarbeitet einen taktischen Vorteil gegenüber " + aTaktiker + " (T" + twAngreifer + ").", true);

            kampfrunde(verteidigerSeite);
            
            initiative = verteidigerSeite;
		}

        rundenFazit(-1);
        return initiative;
    }

    /**
	 * Wählt aus der Liste aller erreichbaren Ziele ein zufälliges Opfer aus.
     * @param k Angreifer
     * @param zielvorgabe Ziel-Wunsch von k, oder null für die Standardvariante
	 * @return ein Opfer oder null, wenn keins gefunden wurde (alle tot)
	 */
	private Krieger randomTarget(Krieger k, Zielvorgabe zielvorgabe) {
        if (k.istTot()) throw new IllegalStateException("Es soll für den toten Krieger " + k + " ein Ziel gesucht werden...");
        
		Krieger victim = null;

        // erstmal herausfinden: Bin ich Angreifer oder Verteidiger?
        Side feinde = null;
        if (verteidigerSeite.containsUnit(k.getUnit())) feinde = angreiferSeite;
        if (angreiferSeite.containsUnit(k.getUnit())) feinde = verteidigerSeite;
        
        // new Debug("Krieger auf Seiten der Feinde:" + feinde.describe());

        if (feinde == null) throw new IllegalStateException("Krieger " + k + " ist weder auf der Seite der Angreifer noch der Verteidiger.");

        if (feinde.vorne.isEmpty() && feinde.hinten.isEmpty()) return null; // koana da!

        // Krieger attackieren auch die zweite Reihe des Gegners, wenn sie können.
        int maxZufall = feinde.vorne.size();
        if (k.usedWeapon().AttackValue(k.getReihe(), Krieger.REIHE_HINTEN) > 0) {
            // kann auch die gegnerische hintere Reihe erreichen!
            maxZufall += feinde.hinten.size();
        }

        if (maxZufall == 0) {
            // vorn ist wohl keiner, und hinten kommen wir nicht ran...
            return null;
        }

        int idx = Random.rnd(0, maxZufall);
        if (idx < feinde.vorne.size()) {
            victim = feinde.vorne.get(idx);
        } else {
            victim = feinde.hinten.get(idx - feinde.vorne.size());
        }




        if (zielvorgabe != null) {
            // Reihenvorgabe - das kann jeder:
            if (zielvorgabe instanceof ReihenZielvorgabe) {
                if (zielvorgabe.getReihe() == Krieger.REIHE_VORNE) {
                    if (!feinde.vorne.isEmpty()) {
                        victim = feinde.vorne.get(Random.rnd(0, feinde.vorne.size()));
                    }
                }

                if (zielvorgabe.getReihe() == Krieger.REIHE_HINTEN) {
                    if (!feinde.hinten.isEmpty()) {
                        victim = feinde.hinten.get(Random.rnd(0, feinde.hinten.size()));
                    }
                }
            }

            // Einheitenzielvorgabe - das erfordert Taktik-Können!
            if (zielvorgabe instanceof EinheitenZielvorgabe) {
                Unit u = k.getUnit(); // Einheit des Kriegers selbst

                List<Krieger> kandidaten = new ArrayList<Krieger>();
                int maxTarnungTalent = 0;
                for (Unit wunsch : zielvorgabe.getOpfer()) {
                    kandidaten.addAll(feinde.getKrieger(wunsch));
                    // new Battle("Tarn-Talent von " + wunsch + ": " + wunsch.Talentwert(Tarnung.class), k.getUnit().getOwner());
                    if (wunsch.Talentwert(Tarnung.class) > maxTarnungTalent) maxTarnungTalent = wunsch.Talentwert(Tarnung.class);
                }
                if (!kandidaten.isEmpty()) {

                    // grundsätzlich geschafft - jetzt Zielen versuchen:
                    int nKandidaten = kandidaten.size();
                    int nAlle = feinde.vorne.size() + feinde.hinten.size();
                    // beide Ns sind garantiert nicht 0!
                    double ratio = (double)nKandidaten / (double)nAlle;

                    ZielChancenModell zcm = new ZielChancenModell();

                    // schafft der Krieger überhaupt einen Versuch? (Extra-"Kosten" für gezielte Angriffe sind mögliche Ausfälle)
                    int faehigkeit = u.Talentwert(Taktik.class);
                    if ((k.usedWeapon().neededSkill() != null) && (!k.usedWeapon().neededSkill().equals(Skill.class))) {
                        faehigkeit += u.Talentwert(k.usedWeapon().neededSkill());
                    }
                    faehigkeit -= maxTarnungTalent;
                    // new Battle("Angriffs-Fähigkeit: " + faehigkeit + " (max. TW Tarnung der Feinde: " + maxTarnungTalent + ")", k.getUnit().getOwner());
                    
                    double ausfallWahrscheinlichkeit = zcm.getAusfallChance(faehigkeit, ratio);
                    // for (Partei p : detailInfos) new Battle(k.kurzCode() + " Ausfall-P: " + ausfallWahrscheinlichkeit, p);
                    int w10000Chance = (int)Math.floor(ausfallWahrscheinlichkeit * 10000d);
                    if (w10000Chance > Random.W(10000)) {
                        for (Partei p : detailInfos) new Battle(k.kurzCode() + " zielt und zielt, und zielt...", p);
                        return null;
                    }


                    // ja, der Angriff gelingt! Treffen wir nun auch unser Ziel?
                    faehigkeit = u.Talentwert(Wahrnehmung.class);
                    if ((k.usedWeapon().neededSkill() != null) && (!k.usedWeapon().neededSkill().equals(Skill.class))) {
                        faehigkeit += u.Talentwert(k.usedWeapon().neededSkill());
                    }
                    // new Battle("Ziel-Fähigkeit: " + faehigkeit, k.getUnit().getOwner());

                    double trefferWahrscheinlichkeit = zcm.getZielChance(faehigkeit, ratio);
                    // for (Partei p : detailInfos) new Battle(k.kurzCode() + " Ziel-P: " + trefferWahrscheinlichkeit, p);
                    w10000Chance = (int)Math.floor(trefferWahrscheinlichkeit * 10000d);
                    if (w10000Chance > Random.W(10000)) {
                        // gotcha!
                        victim = kandidaten.get(Random.rnd(0, kandidaten.size()));
                        // TODO Meldung wohl eher wieder raus?
                        for (Partei p : detailInfos) new Battle(k.kurzCode() + " greift gezielt " + victim.kurzCode() + " an.", p);
                    } else {
                        // TODO Meldung wohl eher wieder raus?
                        for (Partei p : detailInfos) new Battle(k.kurzCode() + " greift an, aber nicht das Hauptziel...", p);
                    }

                } else {
                    // nop - wenn kein Ziel mehr existiert, dann eben den normalen Zufallsfeind angreifen.
                }
            }
        } // endif zielvorgabe != null
        
        if (victim != null) {
            if (victim.getUnit().getPersonen() <=0) throw new IllegalStateException("Die Einheit von " + victim + " hat gar keine Personen mehr!?");
        }
        
        return victim;
    }

    /**
     * realisiert die Konsequenzen für eine Einheit aus dem Tod eines ihrer getKrieger
     * @param seite
     * @param target
     */
    private void heldenTod(Side seite, Krieger target) {
        Unit tUnit = target.getUnit();

        // meldung(target.toString() + " stirbt.", true);
        // new Debug("Seite des Opfers:\n" + seite.describe());
        
        // if (seite.vorne.contains(target)) meldung(target + " stand in der vorderen Reihe.", true);
        // if (seite.hinten.contains(target)) meldung(target + " stand in der hinteren Reihe.", true);
        // if (seite.nicht.contains(target)) meldung(target + " stand in der NICHT-Reihe.", true);
        
        if (target.getReihe() == 1) seite.vorne.remove(target);
        if (target.getReihe() == 2) seite.hinten.remove(target);
        if (target.getReihe() == 3) seite.nicht.remove(target);
        
        // if (seite.vorne.contains(target)) meldung(target + " steht immer noch in der vorderen Reihe.", true);
        

        target.removeWeapons();

        for(Skill skill : target.getUnit().getSkills()) {
            int lt;
            if (tUnit.getPersonen() > 0) {
                lt = skill.getLerntage() / tUnit.getPersonen();
            } else {
                lt = 0;
            }
            lt = skill.getLerntage() - lt;
            if (lt < 0) lt = 0;
            skill.setLerntage(lt);
        }
        // Person jetzt aus der Einheit schmeißen - nur noch Ballast
        tUnit.setPersonen(tUnit.getPersonen() - 1);
    }


    /**
     * <p>Gibt verschiedene Versionen einer Meldung an die Beteiligten - abgestuft wird nach.</p>
     * <ol><li>eigener Partei,</li>
     * <li>Verbündeten,</li>
     * <li>Feinden,</li>
     * <li>unbeteiligten Beobachtern.</li></ol>
     * @param wir Partei, aus deren Perspektive berichtet wird.
     * @param textSelbst
     * @param textFreunde
     * @param textFeinde
     * @param textBeobachter
     */
    public void geheimMeldung(Partei wir, String textSelbst, String textFreunde, String textFeinde, String textBeobachter) {
        List<Partei> freunde = new ArrayList<Partei>();
        List<Partei> feinde = new ArrayList<Partei>();
        List<Partei> beobachter = new ArrayList<Partei>();
        
        if (angreiferParteien.contains(wir)) {
            freunde = angreiferParteien;
            feinde = verteidigerParteien;
        }
        if (verteidigerParteien.contains(wir)) {
            freunde = angreiferParteien;
            feinde = verteidigerParteien;
        }

        beobachter.addAll(beobachterParteien);
        if (freunde.size() + feinde.size() == 0) {
            beobachter.addAll(angreiferParteien);
            beobachter.addAll(verteidigerParteien);
        }


        for (Partei p : freunde) {
            if (p.getNummer() == wir.getNummer()) {
                new Battle(textSelbst, p);
            } else {
                new Battle(textFreunde, p);
            }
        }

        for (Partei p : feinde) {
            if (textFeinde != null) new Battle(textFeinde, p);
        }

        for (Partei p : beobachter) {
            if (textBeobachter != null) new Battle(textBeobachter, p);
        }

    }


    /**
     * Gibt eine Report-Meldung an alle (beteiligten?) Parteien aus.
     * @param text
     * @param anAlle wenn true, werden auch unbeteiligte anwesende Parteien informiert
     */
    public void meldung(String text, boolean anAlle) {
        new Battle(text, Partei.getPartei(0)); // auf jeden Fall Meldung an Partei 0
        if (KampfSimulator.AKTIV) return;
        
        this.meldungAnSeite(angreiferSeite, angreiferParteien, text);
        this.meldungAnSeite(verteidigerSeite, verteidigerParteien, text);
        
        /*
        for (Partei p : angreiferParteien) {
            KampfreportXML bericht = angreiferSeite.bericht(p);
            if (bericht == null) {
                String msg =
                        wasIssesBestimmt + " in " +
                        getRegion() + " [priv]" +
                        p.getPrivateCoords(getRegion().getCoords()) + ": " +
                        angreiferSeite.beschreibeTeilnehmer(p) +
                        " greifen " + verteidigerSeite.beschreibeTeilnehmer(p) +
                        " an - ";
                throw new RuntimeException(msg + "Kampfreport der Angreifer (" + p + ") in " + getRegion() + " " + getRegion().getCoords() + " ist null.");
            }
            bericht.meldung(text);
            new Battle(text, p);
        }
        for (Partei p : verteidigerParteien) {
            KampfreportXML bericht = null;
            if (verteidigerSeite != null) {
                // es kann sein, dass es schon während der Initialisierung Meldungen gibt - da besteht die verteidigerSeite evtl. noch gar nicht
                bericht = verteidigerSeite.bericht(p);
            }
            if (bericht != null) {
                bericht.meldung(text);
            }
            new Battle(text, p);
        }
        if (anAlle) {
            for (Partei p : beobachterParteien) {
                if (p.getNummer() == 0) continue;
                
                new Battle(text, p);
            }
        } */
        
    }
    
    private void meldungAnSeite(Side kampfSeite, List<Partei> beteiligteParteienDerSeite, String meldungsText)
    {
    	for (Partei p : beteiligteParteienDerSeite) {
        	KampfreportXML bericht = null;
        	if (kampfSeite != null)
        	{
        		bericht = kampfSeite.bericht(p);
        		if (bericht == null) {
                    String msg =
                            wasIssesBestimmt + " in " +
                            getRegion() + " [priv]" +
                            p.getPrivateCoords(getRegion().getCoords()) + ": " +
                            angreiferSeite.beschreibeTeilnehmer(p) +
                            " greifen " + verteidigerSeite.beschreibeTeilnehmer(p) +
                            " an - ";
                    throw new RuntimeException(msg + "Kampfreport der Angreifer (" + p + ") in " + getRegion() + " " + getRegion().getCoords() + " ist null.");
                }
                bericht.meldung(meldungsText);
        	}
        	new Battle(meldungsText, p);
        }
    }


    public Region getRegion() {
        return region;
    }

    public void austragen() {
        // alle direkt Beteiligten bekomme die Symbole des Verwundeten-Reports erklärt:
		meldung(verwundetenReportLegende(), false);

        meldung(
                "Abkürzungen für Waffen, Tiere und Ausrüstung:\n" +
                "(Zahlen direkt am Ende der Codes geben den dazugehörigen Talentwert an)\n" +
                KriegerTyp.AusruestungsLegende(),
                false);


		// Meldung über die Aufstellungen
        TagDerWahrheiten();

		// Zaubersprüche verarbeiten
        kampfMagie(angreiferSeite);
        kampfMagie(verteidigerSeite);
		ConfusionSpells();

        // nun wird ggf. die Taktikerrunde gespielt
		taktikerRunde();



		// jetzt pauschal 10 Runden prügeln
		for(int runde = 0; runde < 10; runde++)	{
			if (angreiferSeite.getPersonen() == 0) {
                // die Verteidiger erhalten die Gegenstände der Angreifer:
                // new Debug("Der Jackpot von " + verteidigerSeite.beschreibeTeilnehmer() + " bekommt alles von den Angreifern: " + StringUtils.aufzaehlung(angreiferSeite.jackpot) + ".");
                verteidigerSeite.jackpot.addAll(angreiferSeite.jackpot);
                angreiferSeite.jackpot.clear();
                siegmeldung(verteidigerSeite);

				// Der Jackpot wird weiter unten an die Verteidiger verteilt.
                break;
			}
			if (verteidigerSeite.getPersonen() == 0) {
                // die Angreifer erhalten die Gegenstände der Verteidiger:
                // new Debug("Der Jackpot von " + angreiferSeite.beschreibeTeilnehmer() + " bekommt alles von den Verteidigern: " + StringUtils.aufzaehlung(verteidigerSeite.jackpot) + ".");
                angreiferSeite.jackpot.addAll(verteidigerSeite.jackpot);
                verteidigerSeite.jackpot.clear();
                siegmeldung(angreiferSeite);

				// Der Jackpot wird weiter unten an die Angreifer verteilt.
				break;
			}

            // Meldungen ohne Angabe irgendeiner Zuordnung landen als
            // plain text im Knoten der entsprechenden Runde:
            KampfreportXML.setDefaultRunde(runde + 1);

			meldung(
                    "========== ========== ========== ========== ========== ========== ==\n" +
                    "======= >>  Runde " + (runde + 1) + "\n" +
                    "========== ========== ========== ========== ========== ========== ==\n" +
                    "========== ========== ========== ========== ========== ========== ==\n",
                    true
            );
            ks = new KampfrundenStatistik();


            // April 2011:
            kampfrunde(null); // normale Kampfrunde, null "Vorteil"

            rundenFazit(runde);

			meldung("\n" + "==========  (Ende Runde " + (runde + 1) + ") ===== ========== ========== ========== == <<\n", true);
        } // nächste Runde


        // Waffen aller überlebenden Krieger wieder einsammeln:
        for (Krieger k : angreiferSeite.getKrieger()) k.removeWeapons();
        for (Krieger k : verteidigerSeite.getKrieger()) k.removeWeapons();

        // Eigene verwaiste Ausrüstung und Beute einsammeln:
        angreiferSeite.CleanUp();
        verteidigerSeite.CleanUp();

        meldung("<<", true); // Diese Sektion wurde in wasIsses() geöffnet - umklammert das gesamte Gefecht.
        
        for (Partei p : angreiferParteien) {
            angreiferSeite.bericht(p).writeFile("reporte/" + p.getNummerBase36() + "-kampfreport.xml");
            angreiferSeite.bericht(p).writeHtml("reporte/" + p.getNummerBase36() + "-kampfreport.html");
        }
        for (Partei p : verteidigerParteien) {
            verteidigerSeite.bericht(p).writeFile("reporte/" + p.getNummerBase36() + "-kampfreport.xml");
            verteidigerSeite.bericht(p).writeHtml("reporte/" + p.getNummerBase36() + "-kampfreport.html");
        }

    }
    
    private void siegmeldung(Side gewinner) {
        new Battle(StringUtils.ucfirst(gewinner.beschreibeTeilnehmer(Partei.getPartei(0)) + " gewinnen " + wasIssesBestimmt + ".\n"), Partei.getPartei(0)); // auf jeden Fall für Partei 0
        for (Partei p : region.anwesendeParteien()) {
			new Battle(StringUtils.ucfirst(gewinner.beschreibeTeilnehmer(p) + " gewinnen " + wasIssesBestimmt + ".\n"), p);
        }
    }

    private void rundenFazit (int runde) {
		if (Message.IsMute()) return;
		
        final Set<Partei> empfaenger = new HashSet<Partei>();
        empfaenger.addAll(region.anwesendeParteien());
        empfaenger.add(Partei.getPartei(0));
        
        meldung(">> -- Zusammenfassung ----------------------------------------------\n", true);
        for (Partei beobachter : empfaenger) {
            // zusammengefasste Meldungen über Austeilen und Einstecken:
            StringBuffer zusammenfassung = new StringBuffer();
            for (Partei p : angreiferSeite.getTeilnehmer().keySet()) {
                for (Gruppe g : angreiferSeite.getTeilnehmer().get(p)) {
                    zusammenfassung.append("\n" + StringUtils.ucfirst(g.beschreibeFuerPartei(beobachter)) + ":\n");
                    zusammenfassung.append(ks.getBericht(angreiferSeite, g));
                }
            }
            new Battle("\nZusammenfassung für die Angreifer:\n\n" + zusammenfassung, beobachter);
            
            zusammenfassung = new StringBuffer();
            for (Partei p : verteidigerSeite.getTeilnehmer().keySet()) {
                for (Gruppe g : verteidigerSeite.getTeilnehmer().get(p)) {
                    zusammenfassung.append("\n" + StringUtils.ucfirst(g.beschreibeFuerPartei(beobachter)) + ":\n");
                    zusammenfassung.append(ks.getBericht(verteidigerSeite, g));
                }
            }
            new Battle("\nZusammenfassung für die Verteidiger:\n\n" + zusammenfassung, beobachter);
        }
        meldung("<<", true);

        // Meldung über die Aufstellungen
        meldung(">> -- Schlachtordnung ----------------------------------------------\n", true);
        if (runde >= 0) {
            // normale Kampfrunde:
            meldung("\n\nStatus nach der " + (runde + 1) + ". Runde:\n", true);
        } else {
            meldung("\n\nStatus nach der Taktikerrunde:\n", true);
        }
        for (Partei beobachter : empfaenger) {
            boolean ausfuehrlich = false;
            new Battle(TagDerWahrheit(beobachter, angreiferSeite, ausfuehrlich), beobachter);
            new Battle(TagDerWahrheit(beobachter, verteidigerSeite, ausfuehrlich), beobachter);
        }
        meldung("<<", true);

        // und noch die Details über Verwundungen:
        empfaenger.clear();
        empfaenger.addAll(angreiferParteien);
        empfaenger.addAll(verteidigerParteien);
        empfaenger.add(Partei.getPartei(0));
        for (Partei beobachter : empfaenger) {
            new Battle(">> -- Verwundungen -------------------------------------------------\n", beobachter);
            if (angreiferSeite.getPersonen() > 0) new Battle(verwundetenReport(angreiferSeite, beobachter), beobachter);
            if (verteidigerSeite.getPersonen() > 0) new Battle(verwundetenReport(verteidigerSeite, beobachter), beobachter);
            new Battle("\n-------------------------------------------------------------------- <<\n", beobachter);
        }

        // Beute wird erst am Ende verteilt - SAMMEL BEUTE TRAGBAR ist sonst wegen der möglichen, 
        // äh, Tragkraft-Verluste nicht möglich.
        // angreiferSeite.CleanUp();
        // verteidigerSeite.CleanUp();

        for (Partei p : angreiferParteien) angreiferSeite.bericht(p).beschreibeStatus(angreiferSeite, verteidigerSeite, ks);
        for (Partei p : verteidigerParteien) verteidigerSeite.bericht(p).beschreibeStatus(angreiferSeite, verteidigerSeite, ks);
    }

	/** führt alle PreKampfzauber aus */
	private void ConfusionSpells() {
		// Angriffsseite
		ConfusionSpellsSide(angreiferSeite, Krieger.Units(angreiferSeite.vorne), verteidigerSeite);
		ConfusionSpellsSide(angreiferSeite, Krieger.Units(angreiferSeite.hinten), verteidigerSeite);
		// Verteidiger
		ConfusionSpellsSide(verteidigerSeite, Krieger.Units(verteidigerSeite.vorne), angreiferSeite);
		ConfusionSpellsSide(verteidigerSeite, Krieger.Units(verteidigerSeite.hinten), angreiferSeite);
	}

	/**
	 *  führt pro Seite die Zaubersprüche aus
	 *  @param side - die eigene Seite im Kampf
	 *  @param others - die andere Seite des Kampfes
	 */
	private void ConfusionSpellsSide(Side side, Set<Unit> units, Side others)	{
		StringBuilder sb = new StringBuilder();
		sb.append("@Kriege.ConfusionSpellsSide(Side my, Set<Unit> units, Side others):\n");
		sb.append("Angreifer:\n" + side.describe());
		sb.append("Kandidaten:\n");
		for (Unit u : units) sb.append("\t" + u + "\n");
		sb.append("Verteidiger:\n" + others.describe());
		new Debug(sb.toString());

		for(Unit mage : units) {
			// Testen ob Magier
			if (mage.Talentwert(Magie.class) == 0) continue;

			// testen ob Verwirrungszauber gesetzt
			if (!mage.hasProperty(Kampfzauber.CONFUSIONSPELL)) {
				new Debug("Magier " + mage + " hat keinen Kampfzauber gesetzt.");
				continue;
			} // kein CS gesetzt

			String cs = mage.getStringProperty(Kampfzauber.CONFUSIONSPELL);
            // handelt es sich um einen "nackten" Zaubernamen?
            Class<? extends Spell> sp = BefehlsMuster.SPELL_KEY.get(cs.toLowerCase());
            if (sp != null) {
                cs = "ZAUBERE " + cs;
            }

			// Spruch vorbereiten
            Einzelbefehl eb = null;
            try {
                eb = new Einzelbefehl(mage, mage.getCoords(), cs, 0);
            } catch(IllegalArgumentException ex) {
                try {
                    eb = new Einzelbefehl(mage, mage.getCoords(), "ZAUBERE " + cs, 0);
                } catch(IllegalArgumentException ex2) {
                    new Debug("IllegalArgumentException für ConfusionSpell '" + cs + "'");
                    /* nop */
                }
            }
            
            

            if (eb == null) {
                new Fehler("'" + cs + "' ist kein gültiger Kampfzauber-Befehl.", mage, mage.getCoords());
                continue;
            }

            Spell spell = null;
			try {
                spell = eb.getSpell().newInstance();
            } catch (Exception ex) { 
                new BigError(ex);
            }

			if (spell == null) {
				new Fehler(mage + " - Kampfzauber '" + cs + "' ist irgendwie nicht recht spruchreif.", mage, mage.getCoords());
				continue;
			}
			if (!spell.canUsedBy(mage)) {
				new Fehler(mage + " - Kampfzauber '" + cs + "' ist zu kompliziert.", mage, mage.getCoords());
				continue;
			}
            if (!(spell instanceof ConfusionSpell)) {
				new Fehler(mage + " - Kampfzauber '" + cs + "' ist kein Verwirrungszauber.", mage, mage.getCoords());
				continue;
            }

			// Zaubern
			int stufe = ((ConfusionSpell) spell).ExecuteSpell(mage, side, others, eb.getTokens());

			int a = spell.isOrcus() ? mage.getMana() : mage.getAura();
			a -= stufe * spell.getStufe();
			if (spell.isOrcus()) mage.setMana(a); else mage.setAura(a);

            String kosten = stufe * spell.getStufe() + (spell.isOrcus()?" Punkte Mana":" Punkte Aura");
            String neueEnergie = (spell.isOrcus()?mage.getMana():mage.getAura()) + (spell.isOrcus()?" Mana":" Aura");

			// alle Informieren
			geheimMeldung( Partei.getPartei(mage.getOwner()),
                    mage + " zaubert " + spell.getName() + " auf Stufe " + stufe + " für " + kosten + " und hat nun noch " + neueEnergie + ".", // eigene Partei
                    mage + " zaubert " + spell.getName() + ".", // Freunde
                    null, // Feinde
                    null // Beobachter
            );
		}
	}


    /**
     * Gibt für alle anwesenden Parteien den jeweils angemessenen Detailgrad 
     * der Aufstellungen als Meldung aus.
     */
    private void TagDerWahrheiten() {
        List<Partei> leser = new ArrayList<Partei>();
		leser.addAll(region.anwesendeParteien());
		leser.add(Partei.getPartei(0));
		for (Partei p : leser) {
            boolean ausfuehrlich = false;
            if (ZATMode.CurrentMode().isDebug()) ausfuehrlich = true;
            
            new Battle(">> " + TagDerWahrheit(p, angreiferSeite, ausfuehrlich) + "\n<<", p);
            new Battle(">> " + TagDerWahrheit(p, verteidigerSeite, ausfuehrlich) + "\n<<", p);
        }

        if (ZATMode.CurrentMode().isDebug()) {
            // Meldung über die aktiven verschiedenen Krieger-Typen:
            meldung(beschreibeKriegerTypen(), false);
            for (KriegerTyp kt : this.getKriegerTypen()) {
                for (Partei p : angreiferParteien) angreiferSeite.bericht(p).registriere(kt);
                for (Partei p : verteidigerParteien) verteidigerSeite.bericht(p).registriere(kt);
            }
        }
    }


    /**
	 * listet alle Einheiten und Völker über den aktuellen Kampf
	 * @param beobachter - Aus deren Perspektive (und Aufklärungsvermögen) wird der Bericht erzeugt.
	 * @param side - diese Seite
	 * @return Aufstellung aller Einheiten
	 */
	private String TagDerWahrheit(Partei beobachter, Side side, boolean ausfuehrlich)	{
        StringBuilder sb = new StringBuilder();
		sb.append("Auf der Seite der " + side.getName()  + " stehen:\n");
        if (!side.vorne.isEmpty()) {
            sb.append("Vorne " + side.vorne.size() + " Personen: ");
            sb.append(Krieger.DienstgradeBeschreibung(side.vorne) + ".\n");
        }
        if (!side.hinten.isEmpty()) {
            sb.append("Hinten " + side.hinten.size() + " Personen: ");
            sb.append(Krieger.DienstgradeBeschreibung(side.hinten) + ".\n");
        }
        if (!side.nicht.isEmpty()) {
            sb.append("Abseits " + side.nicht.size() + " Personen: ");
            sb.append(Krieger.DienstgradeBeschreibung(side.nicht) + ".\n");
        }
        if (ausfuehrlich) {
            if (!side.immun.isEmpty()) {
                sb.append("Vom Kampf verschont werden: ");
                Set<Unit> immuneEinheiten = new HashSet<Unit>();
                for (Krieger k : side.immun) {
                    // nur sichtbare Einheiten!
                    if (beobachter.cansee(k.getUnit())) {
                        immuneEinheiten.add(k.getUnit());
                    }
                }
                sb.append(StringUtils.aufzaehlung(immuneEinheiten) + ".\n");
            }
        }

        // Nur Beteiligte bekommen die konkreten Aufstellungen zu Gesicht:
        if (getInvolvierte().contains(beobachter) || beobachter.getNummer() == 0) {
            for (Partei p : side.getTeilnehmer().keySet()) {
                for (Gruppe g : side.getTeilnehmer().get(p)) {
                    List<String> parts = new ArrayList<String>();
                    if (side.getPersonen(g, Kampfposition.Vorne) > 0) {
                        parts.add("Vorne kämpfen: " + TagDerWahrheitReihe(beobachter, side.vorne, g, ausfuehrlich));
                    }
                    if (side.getPersonen(g, Kampfposition.Hinten) > 0) {
                        parts.add("Hinten kämpfen: " + TagDerWahrheitReihe(beobachter, side.hinten, g, ausfuehrlich));
                    }
                    if (side.getPersonen(g, Kampfposition.Nicht) > 0) {
                        parts.add("Nicht kämpfen: " + TagDerWahrheitReihe(beobachter, side.nicht, g, ausfuehrlich));
                    }
    //                if (side.getPersonen(partei, Kampfposition.Immun) > 0) {
    //                    parts.add("Verschont werden: " + TagDerWahrheitReihe(beobachter, side.immun, partei));
    //                }

                    sb.append("\n" + StringUtils.ucfirst(g.beschreibeFuerPartei(beobachter)) + ":\n");
                    if (parts.isEmpty()) {
                        sb.append("Niemand!");
                    } else {
                        sb.append(StringUtils.join(parts, "\n"));
                    }
                    sb.append("\n\n");
                }
            }
        }
        sb.append("\n");
        return sb.toString();
	}

	/**
	 * listet alle Einheiten und und Völker einer Reihe des aktuellen Kampfes auf
	 * @param beobachter - aus Perspektive (Aufklärung?) dieser Partei
	 * @param reihe - diese Reihe
	 * @param gruppe - diese Gruppe
	 * @return Aufstellung dieser Reihe & Gruppe
	 */
	private String TagDerWahrheitReihe(Partei beobachter, List<Krieger> reihe, Gruppe gruppe, boolean ausfuehrlich) {
        StringBuilder sb = new StringBuilder();

        boolean talenteZeigen = false;
        // TODO: Hier auch Allianzen (HELFE) berücksichtigen?
        if (beobachter.getNummer() == gruppe.getParteiNr()) talenteZeigen = true;
		if (beobachter.getNummer() == 0) talenteZeigen = true;

		List<Krieger> eligible = new ArrayList<Krieger>();
        KriegerCounter kc = new KriegerCounter();
        for (Unit u : gruppe.getUnits()) {
            for (Krieger k : reihe) {
                if (k.getUnit().getNummer() == u.getNummer()) eligible.add(k);
            }
        }
        for (Krieger k : eligible) {
            kc.count(k);
        }

		List<String> parts = new ArrayList<String>();
        Map<Integer, String> units = kc.getReportTokens();
        Map<Integer, String> einheitenBeschreibung = this.beschreibeBewaffnung(eligible, talenteZeigen);
        for (int unitNr : units.keySet()) {
            Unit u = Unit.Load(unitNr);
            String desc = units.get(unitNr) + " (" + einheitenBeschreibung.get(unitNr);
            if (getZielvorgaben().get(u) != null) {
                desc += ", " + getZielvorgaben().get(u).beschreiben();
            }
            desc += ")";
            parts.add(desc);
        }

        sb.append(StringUtils.aufzaehlung(parts) + ".\n");

//        if (false) {
//            // TODO: Einzelauflistung der Krieger wieder aktivieren?
//            if (ausfuehrlich) {
//                // ab hier: Einzelauflistung der Krieger als Kurzcode(s) für sie selbst und ihre Ausrüstung:
//                for (Krieger k : eligible) sb.append(" * " + k.kurzCode() + " " +k.beschreibeAusruestung(talenteZeigen) + ",\n");
//                // sb.delete(sb.length()-2, sb.length()-1); // letztes Komma entfernen
//            }
//        }

        return sb.toString();
	}

    /**
     * @return alle (eindeutigen/verschiedenen) KriegerTypen auflisten, sowohl der Angreifer als auch der Verteidiger
     */
    private String beschreibeKriegerTypen() {
        // hier sollte nicht für alle das jeweilige Kampftalent sichtbar sein -
        // derzeit wird aber eh nur im Debug-Modus ausgegeben.

        StringBuilder sb = new StringBuilder();
        sb.append("DEBUG: Vorhandene Krieger-Typen:\n");
        for (KriegerTyp kt : this.getKriegerTypen()) {
            sb.append("\t* ").append(kt.toString()).append(",\n");
        }

        return sb.toString();
    }

	/**
     * Erstellt einen menschenlesbaren Text, der die Verwundungen der Einheiten einer Seite beschreibt.
     * @param side Diese Seite wird analysiert und berichtet
     * @param beobachter Aus Sicht dieser Partei wird formuliert (Partei-Tarnung und so...)
     * @return Text / Verwundetenbericht
     */
    private String verwundetenReport(Side side, Partei beobachter)	{
        StringBuilder sb = new StringBuilder();
		sb.append("Verwundungen auf der Seite der " + side.getName() + ":\n\n");
        for (Partei p : side.getTeilnehmer().keySet()) {
            for (Gruppe g : side.getTeilnehmer().get(p)) {
                sb.append(StringUtils.ucfirst(g.beschreibeFuerPartei(beobachter)) + ":\n\n");

                List<Unit> units = side.getUnits(g);
                Collections.sort(units, new UnitSortierungComparator());

                for (Unit u : units) {
                    sb.append(u.toString());
                    if (u.getPersonen() > 10) sb.append("\n");
                    sb.append(Krieger.HitpointSymbols(side.getKrieger(u)));
                    if (u.getPersonen() > 10) sb.append("\n");
                    sb.append("\n");
                }
                sb.append("\n");
            }
        }
		return sb.toString();
    }

    /**
     * @return Menschenlesbare Erklärung der in den Verwundeten-Reporten benutzten Symbole
     */
    private String verwundetenReportLegende() {
        StringBuilder sb = new StringBuilder();
        sb.append("In den Verwundeten-Reporten werden folgende Symbole verwendet (jedes steht für eine Person in der Einheit):\n");
        sb.append("\t.\tunversehrt\n");
        sb.append("\to\tleicht verletzt (bis zu einem Drittel des tödlichen)\n");
        sb.append("\tO\tschwer verletzt (zwischen 1 und 2 Drittel des tödlichen)\n");
        sb.append("\t@\tkritisch verletzt (mehr als 2 Drittel des tödlichen)\n");
		return sb.toString();
    }

    /**
     * liefert eine menschenlesbare Beschreibung der Bewaffnungen und anderen
     * Details in einer Liste von Kriegern, Unit-weise einzeln.
     * @param krieger
     * @param talenteZeigen wenn true, wird neben der Waffe auch der dazugehörige Talentwert erwähnt
     * @return Map UnitID => Beschreibung der Krieger dieser Einheit
     */
    public Map<Integer, String> beschreibeBewaffnung(List<Krieger> krieger, boolean talenteZeigen) {
        Map<Integer, Set<BewaffnungsRecord>> data = new HashMap<Integer, Set<BewaffnungsRecord>>();
        Map<Integer, String> retval = new HashMap<Integer, String>();

        for (Krieger k : krieger) {
            Unit u = k.getUnit();
            Weapon w = k.usedWeapon();
            
            int unitNr = u.getNummer();
            
            Class<? extends Skill> talent = null; int tw = 0;
            if (w != null) {
                if (talenteZeigen) {
                    talent = w.neededSkill();
                    // "neutrales" Talent nicht akzeptieren - nur Sub-Typen, also konkrete Skills
                    if (talent != Skill.class) tw = u.Talentwert(talent);
                }
            }

            Set<BewaffnungsRecord> unitRecords = data.get(unitNr);
            if (unitRecords == null) {
                unitRecords = new HashSet<BewaffnungsRecord>();
                data.put(unitNr, unitRecords);
            }
            BewaffnungsRecord br = new BewaffnungsRecord(w, talent, tw);
            if (!unitRecords.contains(br)) {
                unitRecords.add(br);
            }
            for (BewaffnungsRecord maybe : unitRecords) {
                if (maybe.equals(br)) maybe.addOne();
            }
        }

        for (int unitNr : data.keySet()) {
            Set<BewaffnungsRecord> unitRecords = data.get(unitNr);
            retval.put(unitNr, StringUtils.aufzaehlung(unitRecords));
        }

        return retval;
    }

    /**
     * @param krieger List von Kriegern
     * @return alle verschiedenen KriegerTypen, die in krieger enthalten sind
     */
    public Set<KriegerTyp> getKriegerTypen(List<Krieger> krieger) {
        Set<KriegerTyp> retval = new HashSet<KriegerTyp>();
        for (Krieger k : krieger) {
            retval.add(KriegerTyp.getInstance(k));
        }
        return retval;
    }

    /**
     * @return alle KriegerTypen, die in diesem Gefecht vertreten sind.
     */
    public Set<KriegerTyp> getKriegerTypen() {
        List<Krieger> eligible = new ArrayList<Krieger>();
        eligible.addAll(angreiferSeite.getKrieger());
        eligible.addAll(verteidigerSeite.getKrieger());
        return getKriegerTypen(eligible);
    }

    private void meldeAbgebrocheneAngriffe() {
        Set<Partei> beobachter = new HashSet<Partei>();
        beobachter.add(Partei.getPartei(0));
        beobachter.addAll(angreiferParteien);
        beobachter.addAll(verteidigerParteien);
        for (Partei p : beobachter) {
            Set<String> meldungA = new HashSet<String>();
            Set<String> meldungV = new HashSet<String>();
            for (GruppenPaarung gp : konflikt.getUnmoeglicheAngriffe()) {
                if (gp.getA().getParteiNr() == p.getNummer()) {
                    String s = "";
                    if (!gp.getA().istAuthentisch()) s+= "von " + gp.getA().beschreibeFuerPartei(p) + " ";
                    s += "auf " + gp.getB().beschreibeFuerPartei(p);
                    meldungA.add(s);
                }
                if (gp.getB().getParteiNr() == p.getNummer()) {
                    String s = "von " + gp.getA().beschreibeFuerPartei(p);
                    if (!gp.getB().istAuthentisch()) s += " auf " + gp.getB().beschreibeFuerPartei(p);
                    meldungV.add(s);
                }
            }
            if (!meldungA.isEmpty()) {
                if (meldungA.size() == 1) {
                    new Battle("Unser Angriff " + meldungA.iterator().next() + " fällt in den Wirren dieses Gefechts aus.", p);
                } else {
                    new Battle("Unsere Angriffe " + StringUtils.aufzaehlung(meldungA) + " fallen in den Wirren dieses Gefechts aus.", p);
                }
            }
            if (!meldungV.isEmpty()) {
                if (meldungV.size() == 1) {
                    new Battle("Der versuchte Angriff " + meldungV.iterator().next() + " fällt in den Wirren dieses Gefechts aus.", p);
                } else {
                    new Battle("Die versuchten Angriffe " + StringUtils.aufzaehlung(meldungV) + " fallen in den Wirren dieses Gefechts aus.", p);
                }
            }
        }
    }


    private class BewaffnungsRecord {
        final Weapon w;
        final Class<? extends Skill> talent;
        final int talentwert;
        int cnt;

        public BewaffnungsRecord(Weapon w, Class<? extends Skill> skill, int talentwert) {
            this.w = w;
            this.talent = skill;
            this.talentwert = talentwert;
            this.cnt = 0;
        }

        public void addOne() {
            cnt ++;
        }

        @SuppressWarnings("unused")
		public int getCnt() {
            return cnt;
        }

        public Class<? extends Skill> getTalent() {
            return talent;
        }

        public int getTalentwert() {
            return talentwert;
        }

        public Weapon getWeapon() {
            return w;
        }

        /**
         * @param o anderer BewaffnungsRecord
         * @return true dann, wenn o ein BewaffnungsRecord mit dem gleichen Talent, Talentwert und der gleichen Waffe ist. Oder: Wenn beide Records "null" als Waffe haben.
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof BewaffnungsRecord)) return false;
            BewaffnungsRecord other = (BewaffnungsRecord)o;

            // waffenlos ist immer gleich
            if ((w == null) && (other.getWeapon() == null)) return true;

            if (w.getClass() != other.getWeapon().getClass()) return false;
            if (talent != other.getTalent()) return false;
            if (talentwert != other.getTalentwert()) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.w != null ? this.w.getClass().hashCode() : 0);
            hash = 67 * hash + (this.talent != null ? this.talent.hashCode() : 0);
            hash = 67 * hash + this.talentwert;
            return hash;
        }

        @Override
        public String toString() {
            if (w == null) return cnt + " Unbewaffnete";
            if (talent == null) {
                if (w instanceof WWaffenlos) return cnt + " Unbewaffnete";
                return cnt + " mit " + w;
            }
            
            return cnt + " mit " + w + " T" + talentwert;
        }
    }

}
