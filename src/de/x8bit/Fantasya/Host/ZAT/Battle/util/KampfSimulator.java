package de.x8bit.Fantasya.Host.ZAT.Battle.util;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.CommandLineArg;
import de.x8bit.Fantasya.Host.EVA.Kriege;
import de.x8bit.Fantasya.Host.EVA.util.BefehleKlassifizieren;
import de.x8bit.Fantasya.Host.Main;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author hb
 */
public class KampfSimulator implements CommandLineArg {

    public static boolean AKTIV = false;

    public final static  String GRUPPE_A = "GruppeA";
    public final static String GRUPPE_B = "GruppeB";
    public final static String UNENTSCHIEDEN = "unentschieden";
	
	/**
     * Datei mit der Definition der Ausgangslage
     */
    String filename;

    int wiederholungen;

    Partei pa;
    Partei pb;

    
    /**
     * enthält die NAMEN der Parteien, die es zu überwachen gilt.
     */
    List<String> watchlist = new ArrayList<String>();

    /**
     * enthält die Experimentaldaten der einzelnen "Runden"
     * Durchgang-Nr. => Name der Partei => Überlebende Personen
     */
    Map<Integer, Map<String, Integer>> survivors = new HashMap<Integer, Map<String,Integer>>();


    Map<Integer, UnitRecord> unitStats = new HashMap<Integer, UnitRecord>();

    int setupPersonenA = -1;
    int setupPersonenB = -1;

    public KampfSimulator() {
		wiederholungen = 200;
    }


	@Override
	public List<String> getCommandLineOptions() {
		List<String> retval = new ArrayList<String>();

		retval.add("KampfSimulator");
		retval.add("KampfSimulator.Chancen");
		retval.add("KampfSimulator.Gleichgewicht");

		return retval;
	}

	@Override
	public void executeCommandLineOption(String commandLineOption, String filename) {
        if (filename == null) {
            printUsage();
            System.exit(-1);
        }
        
		this.filename = filename;

        if (commandLineOption.equals("KampfSimulator")) {
            
            wiederholungen = 1;
            KampfSimulator.AKTIV = true;
			this.run(1d, true); // standalone
            KampfSimulator.AKTIV = false;

            return;
		}

        if (commandLineOption.equals("KampfSimulator.Chancen")) {
            Message.Mute();
            wiederholungen = 100;
            if (Main.getIFlag("KampfSimulator.N") != 0) wiederholungen = Main.getIFlag("KampfSimulator.N");
            
            KampfSimulator.AKTIV = true;
			this.run(25d, true); // standalone
            KampfSimulator.AKTIV = false;

            return;
		}

		if (commandLineOption.equals("KampfSimulator.Gleichgewicht")) {
            StringBuilder result = new StringBuilder();

            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            KampfSimulator.AKTIV = true;

            result.append("!*** " + filename + "\n");
            double coeffA50 = this.balance(KampfSimulator.GRUPPE_A, 0.5d, "-a5of10");
            double coeffB50 = this.balance(KampfSimulator.GRUPPE_B, 0.5d, "-b5of10");
            result.append( "!*** Coeff 50% Siege A: " + nf.format(coeffA50)  + "\n" );
            result.append( "!*** Coeff 50% Siege B: " + nf.format(coeffB50)  + "\n" );

            writeResult(filename, coeffA50, coeffB50);
            
            KampfSimulator.AKTIV = false;
            
			System.out.println(result.toString());
            
            return;
		}

		throw new UnsupportedOperationException(commandLineOption + " - diese Aktion wird nicht unterstützt.");
	}

    private void writeResult(String filename, double coeffA50, double coeffB50) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        
        try {
            // Create file (append mode)
            FileWriter fstream = new FileWriter("KampfSimulator-Ergebnisse.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(filename + "\t");
            out.write(nf.format(coeffA50) + "\t");
            out.write(nf.format(coeffB50) + "\n");
            out.close();
        } catch (Exception ex) {
            System.err.println("I/O Error: " + ex.getMessage());
        }
    }


    public double balance(String targetGroup, double targetPct, String fileSuffix) {
        Map<AnzahlPaar, WinnerMap> empiric = new TreeMap<AnzahlPaar,WinnerMap>();

        Message.Mute();

        NumberFormat rf = NumberFormat.getNumberInstance();
        rf.setMinimumFractionDigits(3);
        rf.setMaximumFractionDigits(3);

        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setMaximumFractionDigits(1);
        pf.setMinimumFractionDigits(1);

        if ((targetPct < 0) || (targetPct > 1)) {
            throw new IllegalArgumentException("targetPct muss einen Wert zwischen 0 und 1 haben (" + rf.format(targetPct) + ").");
        }

        double lowerLimitCoeff = -1d;
        double upperLimitCoeff = -1d;
        double currentCoeff = 1;

        int startIterationen = 50;
        int maxIterationen = 1000;
        if (Main.getIFlag("KampfSimulator.N") != 0) maxIterationen = Main.getIFlag("KampfSimulator.N");

        Partei pErspektive = null;

        System.out.println("Kampf-Gleichgewicht suchen - Aufstellung aus " + filename + " ...");


        // erstmal sehen, ob wir die groben coeff-Startwerte ändern müssen:
        for (boolean grobAbstimmung = true; grobAbstimmung; ) {
            KampfSimulator engine = new KampfSimulator();
            engine.filename = this.filename;
            engine.setup(currentCoeff);
            engine.setWiederholungen(startIterationen);

            if (targetGroup.equals(GRUPPE_A)) pErspektive = engine.pa;
            if (targetGroup.equals(GRUPPE_B)) pErspektive = engine.pb;
            if (pErspektive == null) {
                throw new IllegalStateException("Konnte die Partei (pErspektive) nicht bestimmen: " + targetGroup + "?");
            }

            // nur nochmal simulieren, wenn für diese Personenanzahl der Normtruppe noch nicht genug Simulationen vorliegen:
            int personenA = engine.getSetupPersonenA();
            int personenB = engine.getSetupPersonenB();
            AnzahlPaar paarAB = new AnzahlPaar(personenA, personenB);
            System.out.println(" ... " + personenA + " Personen A und " + personenB + " Personen B.");


            boolean runThis = false;
            if (empiric.get(paarAB) == null) {
                runThis = true;
            } else {
                if (empiric.get(paarAB).getSum() < maxIterationen) runThis = true;
            }

            WinnerMap outcomes = new WinnerMap();
            if (runThis) {
                System.out.println("Starte Simulation mit coeff=" + currentCoeff + " .");
                engine.run(currentCoeff, false); // nicht standalone
                outcomes = engine.evaluateAll();
                // System.out.println(outcomes);
            } else {
                System.out.println("Überspringe Simulation mit " + personenA + " Personen A und " + personenB + " Personen B, es gibt schon " + empiric.get(paarAB).getSum() + " Fälle.");
            }

            // das Ergebnis den empirischen Erfahrungen hinzufügen:
            if (!empiric.containsKey(paarAB)) empiric.put(paarAB, new WinnerMap());
            empiric.get(paarAB).addWinnerMap(outcomes);


            double outcomeWinAPct = engine.getWinProportion(empiric.get(paarAB), engine.pa);
            double outcomeWinBPct = engine.getWinProportion(empiric.get(paarAB), engine.pb);

            if (outcomeWinBPct > 0.9999d) {
                lowerLimitCoeff = currentCoeff;
                // wenn wir noch keine sicheren Siege haben, treten wir jetzt mit doppelt so vielen an:
                if (upperLimitCoeff < 0d) {
                    double newCoeff = currentCoeff * 2d;
                    System.out.println("Sichere Niederlage A bei coeff " + currentCoeff + ", suche sicheren Sieg A mit coeff " + newCoeff + " .");
                    currentCoeff = newCoeff;
                }
            } else if (outcomeWinAPct > 0.9999d) {
                upperLimitCoeff = currentCoeff;
                // wenn wir noch keine sicheren Niederlagen haben, treten wir jetzt mit halb so vielen an:
                if (lowerLimitCoeff < 0d) {
                    double newCoeff = currentCoeff / 2d;
                    System.out.println("Sicherer Sieg A bei coeff " + currentCoeff + ", suche sichere Niederlage A mit coeff " + newCoeff + " .");
                    currentCoeff = newCoeff;
                }
            } else {
                if (upperLimitCoeff > 0d) {
                    // sicher gewonnen hat A schon:
                    double newCoeff = currentCoeff / 2d;
                    System.out.println("Kein klares Ergebnis bei coeff " + currentCoeff + ", suche sichere Niederlage A mit coeff " + newCoeff + " .");
                    currentCoeff = newCoeff;
                } else {
                    double newCoeff = currentCoeff * 2d;
                    System.out.println("Kein klares Ergebnis bei coeff " + currentCoeff + ", suche sicheres Ergebnis mit coeff " + newCoeff + " .");
                    currentCoeff = newCoeff;
                }
            }

            /*
            if (targetGroup.equals(KampfSimulator.GRUPPE_A)) {
                // es soll der Anteil SIEGE der Normtruppe optimiert werden:
                double invProbability = 1 - targetPct;

                // das ist die entsprechende Siegwahrscheinlichkeit mit 10mal
                // geringerer Wahrscheinlichkeit eines Nicht-Siegs:
                double tenTimes = 1 - (invProbability / 10d);

                System.out.println("Sieganteil der Normtruppe bei coeff " + rf.format(upperLimitCoeff) + ": " + rf.format(outcomeWinPct));
                System.out.println("Optimierte Sieganteil der Normtruppe auf " + rf.format(targetPct) + "; inverser Anteil: " +  rf.format(invProbability) + "; 10mal wahrscheinlicherer Sieg: " + rf.format(tenTimes));

                if (outcomeWinPct < tenTimes) {
                    upperLimitCoeff *= 2d;
                } else {
                    upperLimitCoeff *= 1.5d; // sicherheitshalber
                    grobAbstimmung = false;
                    break;
                }

                System.out.println("upperLimitCoeff für Normtruppe auf " + rf.format(upperLimitCoeff) + " erhöht.");
            } else if (targetGroup.equals(KampfSimulator.GRUPPE_B)) {
                // es soll der Anteil Siege der Herausforderer optimiert werden:

                // das ist die entsprechende Siegwahrscheinlichkeit mit 10mal
                // höherer Wahrscheinlichkeit eines Nicht-Siegs:
                double tenTimes = targetPct / 10d;

                System.out.println("Sieganteil der Herausforderer bei coeff " + rf.format(upperLimitCoeff) + ": " + rf.format(outcomeWinPct));
                System.out.println("Optimiere Sieganteil der Herausforderer auf " + rf.format(targetPct) + "; 10mal unwahrscheinlicherer Sieg: " + rf.format(tenTimes));

                if (outcomeWinPct > tenTimes) {
                    upperLimitCoeff *= 2d;
                } else {
                    upperLimitCoeff *= 1.5d; // sicherheitshalber
                    grobAbstimmung = false;
                    break;
                }

                System.out.println("upperLimitCoeff für Normtruppe auf " + rf.format(upperLimitCoeff) + " erhöht.");
            } else {
                throw new RuntimeException("Welche Seite soll optimiert werden?!? (" + targetGroup + ")");
            }
            */

            // System.out.println("ll/ul: " + lowerLimitCoeff + " / " + upperLimitCoeff);
            if ((lowerLimitCoeff > 0d) && (upperLimitCoeff > 0d)) {
                grobAbstimmung = false;

                // Sicherheitsabstand dazu:
                // lowerLimitCoeff /= 1.25d;
                // upperLimitCoeff *= 1.25d;
            }


        }

        System.out.println("Startintervall für die Approximation: " + rf.format(lowerLimitCoeff) + " < coeff < " + rf.format(upperLimitCoeff));


        KampfSimulator result = null;
        AnzahlPaar letzteAufstellung = null;
        double coeff = -1d;
        double outcomeWinPct = -1d;
        
        for (boolean iterate = true; iterate; ) {
            coeff = Math.sqrt(lowerLimitCoeff * upperLimitCoeff);

            System.out.println(
                    "Starte Simulationlauf * " + startIterationen + " (" +
                    rf.format(lowerLimitCoeff) + " < " +
                    rf.format(coeff) + " < " +
                    rf.format(upperLimitCoeff) + ")"
                    );

            KampfSimulator engine = new KampfSimulator();
            engine.filename = this.filename;
            engine.setup(coeff);
            engine.setWiederholungen(startIterationen);

            // nur nochmal simulieren, wenn für diese Personenanzahl der Normtruppe noch nicht genug Simulationen vorliegen:
            int personenA = engine.getSetupPersonenA();
            int personenB = engine.getSetupPersonenB();
            AnzahlPaar paarung = new AnzahlPaar(personenA, personenB);
            boolean runThis = false;
            if (empiric.get(paarung) == null) {
                runThis = true;
            } else {
                if (empiric.get(paarung).getSum() < maxIterationen) runThis = true;
            }

            WinnerMap outcomes = new WinnerMap();
            if (runThis) {
                engine.run(coeff, false); // nicht standalone
                outcomes = engine.evaluateAll();
            } else {
                System.out.println("Überspringe Simulation mit " + personenA + "/" + personenB + " Personen A/B, es gibt schon " + empiric.get(paarung).getSum() + " Fälle.");
            }


            // das Ergebnis den empirischen Erfahrungen hinzufügen:
            if (!empiric.containsKey(paarung)) empiric.put(paarung, new WinnerMap());
            empiric.get(paarung).addWinnerMap(outcomes);

            outcomeWinPct = engine.getWinProportion(empiric.get(paarung), pErspektive);

            System.out.println(
                    "  Sieganteil " + targetGroup + " = " + pErspektive + ": " + rf.format(outcomeWinPct) +
                    " (Ziel: " + rf.format(targetPct) +
                    ") (" + personenA + " A-Krieger, " + personenB + " B-Krieger)");

            double span = upperLimitCoeff - lowerLimitCoeff;
            // System.out.println("span:" + upperLimitCoeff + " - " + lowerLimitCoeff + " = " + span);

            if (targetGroup.equals(KampfSimulator.GRUPPE_A)) {
                if (outcomeWinPct < targetPct) {
                    // die Gruppe A hat zu wenig gewonnen:
                    lowerLimitCoeff = lowerLimitCoeff + ((1d/3d) * span);
                    // System.out.println("new lower: " + (lowerLimitCoeff + ((1d/3d) * span)));
                } else {
                    // die Gruppe A hat zu viel gewonnen:
                    upperLimitCoeff = lowerLimitCoeff + ((2d/3d) * span);
                    // System.out.println("new upper: " + (lowerLimitCoeff + ((2d/3d) * span)));
                }
            } else if (targetGroup.equals(KampfSimulator.GRUPPE_B)) {
                if (outcomeWinPct > targetPct) {
                    // die Gruppe B hat zu viel gewonnen, mehr Gruppe A:
                    lowerLimitCoeff = lowerLimitCoeff + ((1d/3d) * span);
                    // System.out.println("new lower: " + (lowerLimitCoeff + ((1d/3d) * span)));
                } else {
                    // die Gruppe B hat zu wenig gewonnen, mehr Gruppe B:
                    upperLimitCoeff = lowerLimitCoeff + ((2d/3d) * span);
                    // System.out.println("new upper: " + (lowerLimitCoeff + ((2d/3d) * span)));
                }
            }

            if (paarung.equals(letzteAufstellung)) {
                if (empiric.get(paarung).getSum() < maxIterationen) {
                    // die Spannweite der Suche wird verfünffacht:
                    lowerLimitCoeff -= span * 2d;
                    upperLimitCoeff += span * 2d;
                    startIterationen *= 3;
                    startIterationen /= 2;
                    letzteAufstellung = null; // auf jeden Fall weitermachen nächste Runde
                } else {
                    // fertig!
                    result = engine;
                    iterate = false;
                    break;
                }
            } else {
                letzteAufstellung = paarung;
            }
        }
        System.out.println(
                "Ergebnis: Siegquote " + rf.format(outcomeWinPct) + " für " + targetGroup +
                " bei coeff " + rf.format(coeff) + ", " +
                result.getSetupPersonenA() + " Personen A, " + result.getSetupPersonenB() + " Personen B."
                );
        result.printEvaluateAll(fileSuffix);

        for (AnzahlPaar paarung: empiric.keySet()) {
            @SuppressWarnings("unused")
			WinnerMap wm = empiric.get(paarung);
            System.out.print(paarung.getAnzahlA() + "/" + paarung.getAnzahlB() + " von Gruppe A/B: ");
            double ratio = this.getWinProportion(empiric.get(paarung), pErspektive);
            System.out.print(pf.format(ratio));
            System.out.println(" (" + empiric.get(paarung).getSum() + " Kämpfe)" );
        }

        System.out.println("Ergebnis-Aufstellung: " + letzteAufstellung.getAnzahlA() + "/" + letzteAufstellung.getAnzahlB() + " von Gruppe A/B: ");
        
        // das "Ergebnis":
        return coeff;
    }

    /**
	 * @param coeff Multiplikator für das Einheiten-Setup
	 * @param standalone wenn true, werden Meldungen ausgegeben (falls weniger als 100 Wiederholungen bestellt sind)
	 */
	public void run(double coeff, boolean standalone) {
        survivors.clear();
        for (int i = 0; i < wiederholungen; i++) {
            if (standalone) System.out.println("Simulation-Nr. " + (i+1));

            setup(coeff);

            new BefehleKlassifizieren();
            
            boolean isMute = Message.IsMute();
            if (wiederholungen > 100) Message.Mute();
            new Kriege();
            if (!isMute) Message.Unmute();

            evaluateSingle(i);
        }

        evaluateAll();
        if (standalone) printEvaluateAll("");
    }

    /**
     * richtet das Kampf-Szenario ein, dabei werden je nach coeff die Personenanzahlen verschoben.
     * @param coeffA Personenzahlen der Einheiten von Partei A werden mit coeff multipliziert, bei Partei B dividiert
     */
    private void setup(double coeffA) {
        
        Unit.CACHE.clear();
        BefehlsSpeicher.getInstance().clear();
        Partei.PROXY.clear();
        Region.CACHE.clear();

        Partei p0 = new Partei();
        p0.setNummer(0);
        p0.setEMail("noone@foo.bar");
        p0.setMonster(1);
        p0.setRasse("Monster");
        Partei.PROXY.add(p0);


        Region r = new Ebene();
        r.setName("KampfSimulatorenland");
        r.setCoords(new Coords(0, 0, 1));
        r.Init();
        Region.CACHE.put(r.getCoords(), r);

        KampfAufstellung ka = new KampfAufstellung(filename); // arg ist hier dann ein Dateiname

        Unit ua = Unit.Load(ka.getEinheitA());
        Unit ub = Unit.Load(ka.getEinheitB());
        ua.BefehleExperimental.add(ua, "ATTACKIERE " + ub.getNummerBase36());
        ub.BefehleExperimental.add(ub, "ATTACKIERE " + ua.getNummerBase36());

        pa = Partei.getPartei(ua.getOwner());
        pb = Partei.getPartei(ub.getOwner());


        // den coeff anwenden:
        @SuppressWarnings("unused")
		double coeffB = 1d / coeffA;
        for (Unit u : Unit.CACHE) {
            // soll diese Einheit beim Skalieren ausgelassen werden?
            if (!u.hasProperty("personen")) {
                // System.out.println("lasse Einheit " + u + " beim Skalieren (coeff) aus.");
                continue;
            }

            if (u.getOwner() == pa.getNummer()) {
                skaliereEinheit(u, coeffA);
            }
            /*
            if (u.getOwner() == pb.getNummer()) {
                skaliereEinheit(u, coeffB);
            } */
        }

        // tatsächliche Personen zählen:
        setupPersonenA = 0; setupPersonenB = 0;
        for (Unit u : Unit.CACHE) {
            if (u.getOwner() == pa.getNummer()) setupPersonenA += u.getPersonen();
            if (u.getOwner() == pb.getNummer()) setupPersonenB += u.getPersonen();
        }


        watchlist.clear();
        watchlist.add(pa.getName());
        watchlist.add(pb.getName());

        for (Unit u : Unit.CACHE) {
            if (!unitStats.containsKey(u.getNummer())) {
                unitStats.put(u.getNummer(), new UnitRecord(u.getNummer(), u.getName(), u.getPersonen()));
            }
        }


        // ältere Methoden, um die Simulation einzurichten:
        // this.setupNormTruppe(norm, r, coeff);
        // this.setupHerausForderer(challenge, r);
        // this.setupEinzelkampf(norm, challenge, r);
    }

    /**
     * @param p Partei, zu der die neue Norm-Truppe gehören soll
     * @param r Region, in der die ... ... etc.
     * @param coeff Entspricht der Mann-Stärke der Norm-Truppe - 100 ist per 
     * Definition die "Normal-Norm".
     */
    public void setupNormTruppe(Partei p, Region r, double coeff) {
        // System.out.println("Setup der Norm-Truppe mit Koeffizient " + coeff);

		
//		Unit a = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
        Unit b = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
//        Unit c = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
//        Unit d = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());

        // Burgen-Test:
//		Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
//		burg.setName("Zitadelle");
//		burg.setSize(2500);
//        a.Enter(burg); b.Enter(burg); c.Enter(burg); d.Enter(burg);
		
//		a.setName("NormA");
        b.setName("NormB");
//        c.setName("NormC");
//        d.setName("NormD");

//        a.setPersonen(0);
        b.setPersonen(0);
//        c.setPersonen(0);
//        d.setPersonen(0);

        int fehlenNoch = (int) Math.round(coeff);

        this.setupPersonenA = fehlenNoch;

        while (fehlenNoch > 0) {
//            if (fehlenNoch > 0) {
//                a.setPersonen(a.getPersonen() + 1); fehlenNoch --;
//            }
            if (fehlenNoch > 0) {
                b.setPersonen(b.getPersonen() + 1); fehlenNoch --;
            }
//            if (fehlenNoch > 0) {
//                c.setPersonen(c.getPersonen() + 1); fehlenNoch --;
//            }
//            if (fehlenNoch > 0) {
//                d.setPersonen(d.getPersonen() + 1); fehlenNoch --;
//            }
        }
        
        // Pegasus vs. Pferd-Test
//        a.setItem(Schwert.class, a.getPersonen());
//        a.setItem(Pferd.class, a.getPersonen());
//        a.setSkill(Hiebwaffen.class, a.getPersonen() * Skill.LerntageFuerTW(8));
//        a.setSkill(Reiten.class, a.getPersonen() * Skill.LerntageFuerTW(8));
//        a.setKampfposition(Kampfposition.Vorne);
        


//        a.setItem(Speer.class, a.getPersonen());
//        a.setSkill(Speerkampf.class, a.getPersonen() * 1650);
//        a.setKampfposition(Kampfposition.Vorne);
        
        b.setItem(Schwert.class, b.getPersonen());
        b.setSkill(Hiebwaffen.class, b.getPersonen() * Skill.LerntageFuerTW(8));
        b.setKampfposition(Kampfposition.Vorne);

//        c.setItem(Bogen.class, c.getPersonen());
//        c.setSkill(Bogenschiessen.class, c.getPersonen() * 1650);
//        c.setKampfposition(Kampfposition.Hinten);

//        d.setItem(Armbrust.class, d.getPersonen());
//        d.setSkill(Armbrustschiessen.class, d.getPersonen() * 1650);
//        d.setKampfposition(Kampfposition.Hinten);
    }

    public void setupHerausForderer(Partei p, Region r) {
//        Unit b0 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
//        
//        b0.setName("B0");
//        b0.setPersonen((int)Math.round(100));
//        b0.setItem(Schwert.class, b0.getPersonen());
//        // b0.setItem(Kettenhemd.class, b0.getPersonen());
//        b0.setItem(Plattenpanzer.class, b0.getPersonen());
//        b0.setSkill(Hiebwaffen.class, b0.getPersonen() * Skill.LerntageFuerTW(8));
//        // b0.setSkill(Speerkampf.class, b0.getPersonen() * 1650);
//        b0.setSkill(Ausdauer.class, b0.getPersonen() * Skill.LerntageFuerTW(3));
//        b0.setKampfposition(Kampfposition.Vorne);

        Unit b1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
        
        b1.setName("B1");
        b1.setPersonen((int)Math.round(100));
        b1.setItem(Schwert.class, b1.getPersonen());
        b1.setItem(Pegasus.class, b1.getPersonen());
        b1.setSkill(Hiebwaffen.class, b1.getPersonen() * Skill.LerntageFuerTW(5));
        b1.setSkill(Reiten.class, b1.getPersonen() * Skill.LerntageFuerTW(6));
        // b1.setItem(Plattenpanzer.class, b1.getPersonen());
        // b1.setItem(Eisenschild.class, b1.getPersonen());
        b1.setKampfposition(Kampfposition.Vorne);

        // Taktiker:
//        Unit b1 = Unit.CreateUnit(challenge.getRasse(), challenge.getNummer(), r.getCoords());
//        b1.setName("B1");
//        b1.setPersonen(1); // skaliert nicht!
//        b1.setSkill(Taktik.class, b1.getPersonen() * 450);
//        b1.setKampfposition(Kampfposition.Hinten);

        // Schützen:
//        Unit b1 = Unit.CreateUnit(challenge.getRasse(), challenge.getNummer(), r.getCoords());
//        b1.setName("B1");
//        b1.setPersonen((int)Math.round(55 * coeff));
//        b1.setItem(Armbrust.class, b1.getPersonen());
//        b1.setSkill(Armbrustschiessen.class, b1.getPersonen() * 1650);
//        b1.setKampfposition(Kampfposition.Hinten);
		
		// Höllenhunde:
//        Unit b0 = Unit.CreateUnit("Hoellenhund", p.getNummer(), r.getCoords());
//        
//        b0.setName("B0");
//        b0.setPersonen((int)Math.round(10));
//		b0.setSkill(Monsterkampf.class, b0.getPersonen() * 30);
//        b0.setKampfposition(Kampfposition.Vorne);
    }


    public void setupEinzelkampf(Partei pa, Partei pb, Region r) {
        // System.out.println("Setup der Norm-Truppe mit Koeffizient " + coeff);

        /*
        Unit a = Unit.CreateUnit(pa.getRasse(), pa.getNummer(), r.getCoords());
        Unit b = Unit.CreateUnit(pb.getRasse(), pb.getNummer(), r.getCoords());

        a.setName("A-Kämpfer");
        b.setName("B-Kämpfer");

        a.setPersonen(5);
        b.setPersonen(5);

        a.setItem(Speer.class, a.getPersonen());
        // a.setItem(Plattenpanzer.class, a.getPersonen());
        a.setSkill(Speerkampf.class, a.getPersonen() * 1650);
        a.setKampfposition(Kampfposition.Vorne);

        b.setItem(Schwert.class, b.getPersonen());
        b.setSkill(Hiebwaffen.class, b.getPersonen() * 1650);
        b.setKampfposition(Kampfposition.Vorne);
        */

    }


    private void evaluateSingle(int runde) {
        for (String name : watchlist) {
            for (Partei p : Partei.PROXY) {
                if (!p.getName().equalsIgnoreCase(name)) continue;

                int cnt = 0;
                for (Unit u : Unit.CACHE) {
                    if (u.getOwner() != p.getNummer()) continue;

                    cnt += u.getPersonen();
                }

                if (!survivors.containsKey(runde)) {
					survivors.put(runde, new HashMap<String, Integer>());
				}

                survivors.get(runde).put(name, cnt);
            }
        }

        for (int id : unitStats.keySet()) {
            UnitRecord ur = unitStats.get(id);

            Unit u = Unit.Get(id);
            ur.registriereUeberlebende(u.getPersonen());
        }
    }

    private void printEvaluateAll(String filenameSuffix) {
        WinnerMap winnerCnt = this.evaluateAll();
        List<OneResult> results = this.evaluateResults();

        System.out.println("Gewinner-Statistik:");
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        for (String name : winnerCnt.keySet()) {
            float percentage = (float)winnerCnt.get(name) / (float)wiederholungen;
            System.out.println(name + ": " + nf.format(percentage));
            // System.out.println("(" + winnerCnt.get(name) + "/" + wiederholungen + ")");
        }

        System.out.println("Überlebens-Chancen der einzelnen Einheiten:");
        System.out.println("(alle / die Hälfte / wenigstens einer)");
        SortedSet<Integer> ids = new TreeSet<Integer>();
        ids.addAll(unitStats.keySet());
        for (int id : ids) {
            UnitRecord ur = unitStats.get(id);
            System.out.println(ur.toString(wiederholungen));
        }



        Collections.sort(results, new ResultComparator(watchlist.get(0), watchlist.get(1)));
        try {
            File file = new File("temp/kampf-simulator-statistik" + filenameSuffix + ".csv");

            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));

            for (OneResult or : results) {
                out.append(or.getSurvivors(watchlist.get(0)) + ";" + or.getSurvivors(watchlist.get(1)) + "\r\n");
            }

            out.flush();
            out.close();

        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private double getWinProportion(WinnerMap winnerCnt, Partei p) {
        // System.out.println(winnerCnt);
        double retval = (double)winnerCnt.get(p.getName()) / (double)winnerCnt.getSum();
        // System.out.println("getWinProportion(" + p.getName() + ") = " + winnerCnt.get(p.getName()) + "/" + winnerCnt.getSum() + "= " + retval);

        return retval;
    }

    private WinnerMap evaluateAll() {
        List<OneResult> results = this.evaluateResults();
        
        WinnerMap winnerCnt = new WinnerMap();

        for (OneResult or : results) {
            String winner = or.getWinner();
            if (winner == null) winner = KampfSimulator.UNENTSCHIEDEN;
            winnerCnt.recordWin(winner);
        }

        return winnerCnt;
    }

    private List<OneResult> evaluateResults() {
        List<OneResult> results = new ArrayList<OneResult>();

        for (int runde : survivors.keySet()) {
            Set<String> parteien = survivors.get(runde).keySet();
            OneResult result = new OneResult();

            for (String partei : parteien) {
                int cnt = survivors.get(runde).get(partei);

                result.record(partei, cnt);
            }
            results.add(result);
        }
        return results;
    }


    public int getWiederholungen() {
        return wiederholungen;
    }

    public void setWiederholungen(int wiederholungen) {
        this.wiederholungen = wiederholungen;
    }

    public int getSetupPersonenA() {
        return setupPersonenA;
    }

    public int getSetupPersonenB() {
        return setupPersonenB;
    }


    private void skaliereEinheit(Unit u, double coeff) {
        int neuePersonen = (int)Math.round((double)(u.getPersonen()) * coeff);
        double roundedCoeff = (double)neuePersonen / (double)(u.getPersonen());

        if (neuePersonen == u.getPersonen()) return;

        // Items:
        for (Item it : u.getItems()) {
            int neueAnzahl = (int)Math.round((double)(it.getAnzahl()) * roundedCoeff);
            it.setAnzahl(neueAnzahl);
        }

        // Talente:
        for (Skill sk : u.getSkills()) {
            int neueTage = (int)Math.round((double)(sk.getLerntage()) * roundedCoeff);
            sk.setLerntage(neueTage);
        }

        new Debug("Skaliere " + u + " von " + u.getPersonen() + " auf " + neuePersonen + " Personen.");
        u.setPersonen(neuePersonen);
    }

    private void printUsage() {
        System.out.println("Du musst einen Dateinamen (Datei enthält die Kampf-Konstellation) angeben, z.B.:");
        System.out.println();
        System.out.println("\tjava -jar fantasya.jar -X KampfSimulator MeinDuell.txt");
        System.out.println();
    }

    private class OneResult {
        Map<String, Integer> survivors = new TreeMap<String, Integer>();

        public OneResult() {

        }

        public void record(String partei, int survivorCnt) {
            survivors.put(partei, survivorCnt);
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (String partei : survivors.keySet()) {
                sb.append(partei + " " + survivors.get(partei) + " ");
            }
            String winner = this.getWinner();
            if (winner != null) {
                sb.append(winner);
            } else {
                sb.append("unentschieden");
            }
            return sb.toString();
        }

        public String getWinner() {
            String retval = null;

            int cnt = 0;
            for (String partei : survivors.keySet()) {
                if (survivors.get(partei) > 0) {
                    retval = partei;
                    cnt ++;
                }
                if (cnt > 1) return null; // unentschieden
            }

            return retval;
        }

        public int getSurvivors(String partei) {
            return survivors.get(partei);
        }
    }

    private class ResultComparator implements Comparator<OneResult> {
        final String norm;
        final String challenger;

        public ResultComparator(String norm, String challenger) {
            this.norm = norm;
            this.challenger = challenger;
        }

        @Override
        public int compare(OneResult r1, OneResult r2) {
            int delta1 = r1.getSurvivors(norm) - r1.getSurvivors(challenger);
            int survivors1 = r1.getSurvivors(norm) + r1.getSurvivors(challenger);
            boolean unentschieden1 = (survivors1 > Math.abs(delta1));
            
            int delta2 = r2.getSurvivors(norm) - r2.getSurvivors(challenger);
            int survivors2 = r2.getSurvivors(norm) + r2.getSurvivors(challenger);
            boolean unentschieden2 = (survivors2 > Math.abs(delta2));

            if (unentschieden1 && (!unentschieden2)) return 1; // unentschieden 1 nach hinten
            if (unentschieden2 && (!unentschieden1)) return -1; // unentschieden 2 nach hinten

            if (unentschieden1 && unentschieden2) {
                if (survivors1 > survivors2) return 1;
                if (survivors2 > survivors1) return -1;

                if (delta1 > delta2) return 1;
                if (delta1 < delta2) return -1;

                return 0;
            }

            // kein Unentschieden:
            if (delta1 > delta2) return 1; // hoher Sieg für Norm nach hinten
            if (delta1 < delta2) return -1; // dito - bzw. hoher Sieg für Challenger nach vorn

            return 0;
        }

    }

    @SuppressWarnings("serial")
	private class WinnerMap extends HashMap<String, Integer> {

        public void recordWin(String winner) {
            if (!this.containsKey(winner)) this.put(winner, 0);
            this.put(winner, this.get(winner) + 1);
        }

        @SuppressWarnings("unused")
		public void recordDraw() {
            this.recordWin(KampfSimulator.UNENTSCHIEDEN);
        }

        public void addWinnerMap(WinnerMap other) {
            for (String otherKey : other.keySet()) {
                if (!this.containsKey(otherKey)) this.put(otherKey, 0);
                this.put(otherKey, this.get(otherKey) + other.get(otherKey));
            }
        }

        public Integer get(String key) {
            if (!this.containsKey(key)) return 0;

            return super.get(key);
        }

        public int getSum() {
            int sum = 0;
            for (String key : this.keySet()) {
                sum += this.get(key);
            }
            return sum;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("WinnerMap:\n");
            for (String key : this.keySet()) {
                sb.append(key).append(": ").append(get(key)).append("\n");
            }

            return sb.substring(0, sb.length() - 1);
        }

    }

    @SuppressWarnings("unused")
    private class UnitRecord {
		final int id;
        final int ursprungsZahl;
        final String name;

        int alleUeberlebt = 0;
        int haelfteUeberlebt = 0;
        int einerUeberlebt = 0;
        boolean single = false;

        public UnitRecord(int id, String name, int ursprungsZahl) {
            this.id = id;
            this.ursprungsZahl = ursprungsZahl;
            this.name = name;

            if (ursprungsZahl <= 1) single = true;
        }

		public int getAlleUeberlebt() {
            return alleUeberlebt;
        }

        public void setAlleUeberlebt(int alleUeberlebt) {
            this.alleUeberlebt = alleUeberlebt;
        }

        public int getEinerUeberlebt() {
            return einerUeberlebt;
        }

        public void setEinerUeberlebt(int einerUeberlebt) {
            this.einerUeberlebt = einerUeberlebt;
        }

        public int getHaelfteUeberlebt() {
            return haelfteUeberlebt;
        }

        public void setHaelfteUeberlebt(int haelfteUeberlebt) {
            this.haelfteUeberlebt = haelfteUeberlebt;
        }

        public boolean isSingle() {
            return single;
        }

        public void setSingle(boolean single) {
            this.single = single;
        }

        public void registriereUeberlebende(int n) {
            if (n > 0) einerUeberlebt ++;
            if (n >= (ursprungsZahl / 2)) haelfteUeberlebt ++;
            if (n == ursprungsZahl) alleUeberlebt ++;
        }

        public String toString(int runden) {
            StringBuilder retval = new StringBuilder();
            NumberFormat f = NumberFormat.getPercentInstance(Locale.GERMAN);
            f.setMinimumFractionDigits(1);
            f.setMaximumFractionDigits(1);
            
            retval.append(name).append(" ");

            // retval.append("[").append(Codierung.toBase36(id)).append("] --> ");

            float q = ((float)alleUeberlebt) / ((float)runden);
            retval.append(f.format(q));

            if (!single) {
                q = ((float)haelfteUeberlebt) / ((float)runden);
                retval.append(" / ").append(f.format(q));

                q = ((float)einerUeberlebt) / ((float)runden);
                retval.append(" / ").append(f.format(q));
            }

            return retval.toString();
        }

    }

    private class AnzahlPaar implements Comparable<AnzahlPaar> {
        final int anzahlA;
        final int anzahlB;

        public AnzahlPaar(int anzahlA, int anzahlB) {
            this.anzahlA = anzahlA;
            this.anzahlB = anzahlB;
        }

        public int sortValue() {
            return anzahlA * anzahlA + anzahlB;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (!(other instanceof AnzahlPaar)) return false;
            AnzahlPaar partner = (AnzahlPaar)other;

            if (getAnzahlA() != partner.getAnzahlA()) return false;
            if (getAnzahlB() != partner.getAnzahlB()) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + this.anzahlA;
            hash = 29 * hash + this.anzahlB;
            return hash;
        }

        @Override
        public int compareTo(AnzahlPaar o) {
            if (sortValue() < o.sortValue()) return -1;
            if (sortValue() > o.sortValue()) return +1;
            return 0;
        }

        public int getAnzahlA() {
            return anzahlA;
        }

        public int getAnzahlB() {
            return anzahlB;
        }
    }

}

