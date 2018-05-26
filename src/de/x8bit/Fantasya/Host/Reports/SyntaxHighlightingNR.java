package de.x8bit.Fantasya.Host.Reports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Allianz;
import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Steuer;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Buildings.Leuchtturm;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.Kampfzauber;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Mastodon;
import de.x8bit.Fantasya.Atlantis.Items.Resource;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.AnimalResource;
import de.x8bit.Fantasya.Atlantis.Items.LuxusGood;
import de.x8bit.Fantasya.Atlantis.Items.Wagen;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.Handelsmeldungen;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Atlantis.Regions.Lavastrom;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sandstrom;
import de.x8bit.Fantasya.Atlantis.Regions.Vulkan;
import de.x8bit.Fantasya.Atlantis.Regions.aktiverVulkan;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Host.EVA.Diebstahl;
import de.x8bit.Fantasya.Host.EVA.Environment;
import de.x8bit.Fantasya.Host.EVA.Handeln;
import de.x8bit.Fantasya.Host.EVA.Reporte;
import de.x8bit.Fantasya.Host.EVA.SteuernErpressen;
import de.x8bit.Fantasya.Host.EVA.Umwelt.BauernWanderung;
import de.x8bit.Fantasya.Host.EVA.Unterhalten;
import de.x8bit.Fantasya.Host.EVA.VolksZaehlung;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung.Insel;
import de.x8bit.Fantasya.Host.EVA.util.Soziologie;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.Reports.Writer.NRWriter;
import de.x8bit.Fantasya.Host.Reports.util.CoordComparatorLNR;
import de.x8bit.Fantasya.Host.Reports.util.EinflussKarte;
import de.x8bit.Fantasya.Host.Reports.util.GenericParteiComparator;
import de.x8bit.Fantasya.Host.Reports.util.ParteiPersonenComparator;
import de.x8bit.Fantasya.Host.Reports.util.ParteiSilberComparator;
import de.x8bit.Fantasya.Host.Reports.util.ParteiTalentComparator;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.StringUtils;
import de.x8bit.Fantasya.util.comparator.RegionsMachtComparator;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Einfache Textausgabe, ergänzt und geändert für "Syntax-Highlighting"
 * @author mogel - WO ?? :] - achso ReportNR
 * @author hapebe
 */
public class SyntaxHighlightingNR extends ReportNR {

	private NRWriter writer;
	
	/**
	 * diese Partei bekommt den NR
	 */
	private Partei partei;
	
	/**
	 * für Ganzzahlen
	 */
	private NumberFormat N;
	/**
	 * für rationale Zahlen
	 */
	private NumberFormat R;
	/**
	 * für Prozentwerte
	 */
	private NumberFormat P;
    
    

	public SyntaxHighlightingNR(Partei partei) {
		super(partei, false); // "fauler" Konstruktor
        this.partei = partei;
        
		writer = new NRWriter(this.partei, ".nr2", "UTF8");
		
		// Zahlenformate:
		N = NumberFormat.getIntegerInstance(Locale.GERMANY);
		N.setGroupingUsed(true);
		R = NumberFormat.getNumberInstance(Locale.GERMANY);
		R.setGroupingUsed(true);
		R.setMaximumFractionDigits(1);
		R.setMinimumFractionDigits(1);
		P = NumberFormat.getPercentInstance(Locale.GERMANY);
		P.setMaximumFractionDigits(1);
		P.setMinimumFractionDigits(1);		
		
		NRHeader();
		VolksZaehlung();
		if (partei.getNummer() == 0) Ranglisten();
        Inseln();
		AndereVoelker();
		Allianzen();
		Meldungen();
		Steuern();
		Regionen();
		
		writer.wl("");
		writer.wl("");
		
		writer.CloseFile();
	}
	
	private void writeSectionStart(String title, boolean zentriert) {
        if (zentriert) {
            writer.leftAndCenter(">>", title);
        } else {
            writer.wl(">> " + title);
        }
    }

    private void writeSectionEnd() {
		writer.wl("<<");
    }

    private void WriteTheLine() {
        WriteTheLine(true);
    }

    private void WriteTheLine(boolean spaced) {
		if (spaced) writer.wl("");
		writer.wl("-----------------------------------------------------------------------");
		if (spaced) writer.wl("");
	}
	
	/** das wichtigste von allen */
	private void NRHeader()	{

		writer.wl("");
		writer.wl("");
		writer.wl("");
		writer.center("Fantasya Auswertung");
		writer.center("~~~~~~~~~~~~~~~~~~~~~~~~~");
		writer.wl("");
		writer.center(GameDate());
		writer.center("(Runde " + GameRules.getRunde() + ")");
		writer.wl("");
		writer.wl("");
		writer.center("Der Name [und die Nummer] Deines Volkes lautet");
		writer.center("" + partei + ".");
		writer.wl("");
		writer.wl("");
	}

	private void VolksZaehlung() {
		int einkommen = 0;
		int vermoegen = 0;
		int headcount = 0;
		int unitcount = 0;
		Map<PersonenHaufen, PersonenHaufen> personen = new HashMap<PersonenHaufen, PersonenHaufen>();

		Collection<Unit> alle = null;
		if (partei.getNummer() != 0) {
			alle = Unit.CACHE.getAll(partei.getNummer());
		} else {
			alle = Unit.CACHE;
		}
		for (Unit u : alle) {

			einkommen += u.getEinkommen();
			vermoegen += u.getItem(Silber.class).getAnzahl();

			int tmp = u.getPersonen();
			u.setPersonen(1);
			String rasseSg = u.getRassenName();
			u.setPersonen(2);
			String rassePl = u.getRassenName();
			u.setPersonen(tmp);

			PersonenHaufen my = new PersonenHaufen(u.getPrefix(), rasseSg, rassePl);
			if (!personen.containsKey(my)) personen.put(my, my);

			personen.get(my).addAnzahl(u.getPersonen());

			headcount += u.getPersonen();
			unitcount ++;
		}
		List<PersonenHaufen> gruppen = new ArrayList<PersonenHaufen>();
		for (PersonenHaufen gruppe : personen.keySet()) gruppen.add(gruppe);
		Collections.sort(gruppen);
		Collections.reverse(gruppen);
		String personenBeschreibung = StringUtils.aufzaehlung(gruppen);

		int vermoegenProKopf = Math.round((float)vermoegen / (float)headcount);
		int einkommenProKopf = Math.round((float)einkommen / (float)headcount);
		

		writer.wl("Dein Volk zählt " + personenBeschreibung + " in " + N.format(unitcount) + " Einheiten.");
		writer.wl("");
		String msg = "Das Vermögen (Einkommen) deines Volkes liegt bei " + N.format(vermoegen)
				+ " (" + N.format(einkommen) + ") Silber, also ca. "
				+ N.format(vermoegenProKopf) + " (" + N.format(einkommenProKopf) + ") pro Kopf.";
		if (GameRules.isSkirmish()) {
			msg += " Du hast bisher " + partei.getIntegerProperty("punkte.krieg", 0) + " durch Kämpfe erhalten. Wäre jetzt "
				+ "das Spiel zu Ende, dann hättest Du " + partei.getIntegerProperty("punkte.ende", 0) + " Punkte zusätzlich und "
				+ "hättest insgesammt " + (partei.getIntegerProperty("punkte.krieg", 0) + partei.getIntegerProperty("punkte.ende", 0)) + " Punkte.";
		}
		writer.wl(msg);
		writer.wl("");

		if (partei.getNummer() == 0) {
			// Bauern-Statistik:
			int silberSumme = 0;
			int bauern = 0;
			int baeume = 0;
			int leer = 0;
			for (Region r : Region.CACHE.values()) {
				silberSumme += r.getSilber();
				bauern += r.getBauern();
				baeume += r.getBaum();
				if (r.istBetretbar(null) && r.getBauern() == 0) leer++;
			}
			float bauernProRegion = (float)bauern / (float)Region.CACHE.size();
			float baeumeProRegion = (float)baeume / (float)Region.CACHE.size();
			float silberProBauer = (float)silberSumme / (float)bauern;

			writer.wl(
					N.format(bauern) + " Bauern leben in " + N.format(Region.CACHE.size()) + " Regionen, "
					+ "das sind im Schnitt jeweils " + N.format(bauernProRegion) + ". "
					+ "Die Bauern besitzen insgesamt " + N.format(silberSumme) + " Silber, "
					+ R.format(silberProBauer) + "$ pro Kopf. "
					+ (leer>0?N.format(leer) + " Regionen sind unbewohnt. ":"")
					+ N.format(baeume) + " Bäume wachsen auf der Welt, "
					+ N.format(baeumeProRegion) + " pro Region."
			);
			writer.wl("");
            
            // Goblin- und Koboldeinkommen - wie geht es den lieben?
            int goblinEinkommen = 0; int goblinCnt = 0;
            int koboldEinkommen = 0; int koboldCnt = 0;
            for (Unit u : Unit.CACHE) {
                if (u.getRasse().equals("Kobold")) { koboldCnt += u.getPersonen(); koboldEinkommen += u.getEinkommen(); }
                if (u.getRasse().equals("Goblin")) { goblinCnt += u.getPersonen(); goblinEinkommen += u.getEinkommen(); }
            }
            writer.wl(
                    N.format(goblinCnt) + " Goblins konnten " + N.format(goblinEinkommen) + "$ verdienen, "
                    + N.format(koboldCnt) + " Kobolde haben " + N.format(koboldEinkommen) + "$ ergaunert."
            );
			writer.wl("");
		}

		writer.wl("");
		writer.center("Kennzahlen");
		writer.wl("");

		int p = partei.getNummer();

        VolksZaehlung.Datensatz t0 = null;
		VolksZaehlung.Datensatz t1;
        
		// t wie "Trend"
        boolean t = true;
        if (!Soziologie.Vz.containsKey(VolksZaehlung.MODUS_T0)) t = false;
        if (t) t = Soziologie.Vz.get(VolksZaehlung.MODUS_T0).getDaten().getEinheiten().containsKey(p);
        if (t) t0 = Soziologie.Vz.get(VolksZaehlung.MODUS_T0).getDaten();
        
        if (!Soziologie.Vz.containsKey(VolksZaehlung.MODUS_T1)) {
            new SysMsg("NR2: Es hat keine Volkszählung nach der Auswertung stattgefunden!");
            return;
        }
        t1 = Soziologie.Vz.get(VolksZaehlung.MODUS_T1).getDaten();

        if (!t) {
            // ohne Trend
        } else {
            // mit Trend
            writer.wl("                      :  Anfang des :    Ende des :     Ver-    ");
            writer.wl("                      :  Monats     :    Monats   :     änderung");
            writer.wl("::::::::::::::::::::::#:::::::::::::#:::::::::::::#::::::::::::::");

            float n0 = t0.getPersonen().get(p); String left = N.format(n0) + "  ";
            float n1 = t1.getPersonen().get(p); String right = N.format(n1) + "  ";
            String delta = ""; if (n0 > 0) delta = P.format(n1 / n0 - 1);
            new StatLine("Personen:", left, right, delta);

            n0 = t0.getEinheiten().get(p); left = N.format(n0) + "  ";
            n1 = t1.getEinheiten().get(p); right = N.format(n1) + "  ";
            delta = ""; if (n0 > 0) delta = P.format(n1 / n0 - 1);
            new StatLine("Einheiten:", left, right, delta);

            float ratio0 = (float)t0.getPersonen().get(p) / (float)t0.getEinheiten().get(p);
            float ratio1 = (float)t1.getPersonen().get(p) / (float)t1.getEinheiten().get(p);
            left = R.format(ratio0); right = R.format(ratio1);
            delta = ""; if (ratio0 > 0) delta = P.format(ratio1 / ratio0 - 1);
            new StatLine("Personen pro Einheit:", left, right, delta);

            n0 = t0.getVermoegen().get(p); left = "$" + N.format(n0) + "  ";
            n1 = t1.getVermoegen().get(p); right = "$" + N.format(n1) + "  ";
            delta = ""; if (n0 > 0) delta = P.format(n1 / n0 - 1);
            new StatLine("Vermoegen:", left, right, delta);

            ratio0 = n0 / (float)t0.getPersonen().get(p);
            ratio1 = n1 / (float)t1.getPersonen().get(p);
            left = R.format(ratio0); right = R.format(ratio1);
            delta = ""; if (ratio0 > 0) delta = P.format(ratio1 / ratio0 - 1);
            new StatLine("Vermoegen pro Person:", left, right, delta);

            n0 = t0.getEinkommen().get(p); left = "$" + N.format(n0) + "  ";
            n1 = t1.getEinkommen().get(p); right = "$" + N.format(n1) + "  ";
            delta = ""; if (n0 > 0) delta = P.format(n1 / n0 - 1);
            new StatLine("Einkommen:", left, right, delta);

            ratio0 = n0 / (float)t0.getPersonen().get(p);
            ratio1 = n1 / (float)t1.getPersonen().get(p);
            left = R.format(ratio0); right = R.format(ratio1);
            delta = ""; if (ratio0 > 0) delta = P.format(ratio1 / ratio0 - 1);
            new StatLine("Einkommen pro Person:", left, right, delta);

            n0 = t0.getTalentTage().get(p); left = N.format(n0) + "  ";
            n1 = t1.getTalentTage().get(p); right = N.format(n1) + "  ";
            delta = ""; if (n0 > 0) delta = P.format(n1 / n0 - 1);
            new StatLine("Lerntage:", left, right, delta);

            ratio0 = n0 / (float)t0.getPersonen().get(p);
            ratio1 = n1 / (float)t1.getPersonen().get(p);
            left = N.format(ratio0) + "  "; right = N.format(ratio1) + "  ";
            delta = ""; if (ratio0 > 0) delta = P.format(ratio1 / ratio0 - 1);
            new StatLine("Lerntage pro Person:", left, right, delta);

            float tw0 = Skill.Talentwert(ratio0);
            float tw1 = Skill.Talentwert(ratio1);
            left = R.format(tw0); right = R.format(tw1);
            delta = ""; if (ratio0 > 0) delta = P.format(tw1 / tw0 - 1);
            new StatLine("\"Volks-Talentwert\":", left, right, delta);
        }

        writer.wl("");

        Region schwerpunkt = Region.Load(t1.getSchwerpunkt(partei));
        StringBuilder sb = new StringBuilder();
        sb.append("Der Schwerpunkt der Bevölkerung deines Landes liegt rechnerisch ");
        if (partei.canAccess(schwerpunkt) && schwerpunkt.istBetretbar(null)) {
            sb.append("in " + schwerpunkt + " " + partei.getPrivateCoords(schwerpunkt.getCoords()) + ".");
        } else {
            sb.append("bei " + partei.getPrivateCoords(schwerpunkt.getCoords()).xy() + ".");
        }
        writer.wl(sb.toString());
        writer.wl("");
        
	}
	
	/**
	 * produziert einige Ranglisten, inspiriert von CIV I
	 */
	private void Ranglisten() {
		if (partei.getNummer() != 0) return;
        
        writeSectionStart("Ranglisten", true);

		writer.wl("Die größten Bevölkerungen:");
		writer.wl("==========================");
		List<Partei> meistePersonen = new ArrayList<Partei>();
		meistePersonen.addAll(Partei.PROXY);
		Collections.sort(meistePersonen, Collections.reverseOrder(new ParteiPersonenComparator()) );
		int nr = 1;
		for (Partei p : meistePersonen) {
			writer.wl(
					String.format("%5s", nr + ". ")
					+ String.format("%-51s", p.toString())
					+ String.format("%10s", N.format(p.getPersonen()))
			);
			nr ++;
		}
		writer.wl("");
		
		writer.wl("Der größte Reichtum:");
		writer.wl("====================");
		List<Partei> reichtum = new ArrayList<Partei>();
		reichtum.addAll(Partei.PROXY);
		Collections.sort(reichtum, Collections.reverseOrder(new ParteiSilberComparator()) );
		nr = 1;
		for (Partei p : reichtum) {
			int silber = 0;
			for (Unit u : p.getEinheiten()) silber += u.getItem(Silber.class).getAnzahl();
			writer.wl(
					String.format("%5s", nr + ". ")
					+ String.format("%-51s", p.toString())
					+ String.format("%10s", N.format(silber))
			);
			nr ++;
		}
		writer.wl("");
		
		writer.wl("Der höchste Bildungsstand:");
		writer.wl("==========================");
		List<Partei> bildung = new ArrayList<Partei>();
		List<Partei> ausgeschlossene = new ArrayList<Partei>();
		for (Partei p : Partei.PROXY) {
			if (p.getPersonen() >= 100) {
				bildung.add(p);
			} else {
				ausgeschlossene.add(p);
			}
		}
		Collections.sort(bildung, Collections.reverseOrder(new ParteiTalentComparator()) );
		nr = 1;
		for (Partei p : bildung) {
			int n1 = 0;
			for (Unit u : p.getEinheiten()) {
				for (Skill sk : u.getSkills()) {
					n1 += sk.getLerntage();
				}
			}
			int tageProPerson = Math.round((float)n1 / (float)p.getPersonen());
			
			writer.wl(
					String.format("%5s", nr + ". ")
					+ String.format("%-51s", p.toString())
					+ String.format("%10s", N.format(tageProPerson))
			);
			nr ++;
		}
		writer.wl("Die Parteien " + StringUtils.aufzaehlung(ausgeschlossene) + " wurden nicht berücksichtigt, das sie weniger als 100 Personen umfassen.");
		writer.wl("");
		
		writer.wl("Der größte Einfluss:");
		writer.wl("====================");
		
		EinflussKarte ek = new EinflussKarte();
		Map<Partei, Float> ranking = new HashMap<Partei, Float>();
		float rankingSumme = 0;
		for (Partei p : Partei.PROXY) {
			ranking.put(p, ek.getGlobalenEinfluss(p));
			rankingSumme += ranking.get(p);
		}
		
		List<Partei> einfluss = new ArrayList<Partei>();
		einfluss.addAll(Partei.PROXY);
		Collections.sort(einfluss, Collections.reverseOrder(new GenericParteiComparator(ranking)) );
		
		nr = 1;
		float erwarteterAnteil = rankingSumme / (float)einfluss.size();
		for (Partei p : einfluss) {
			float absolut = ek.getGlobalenEinfluss(p);
			float score = (float)(Math.log(absolut / erwarteterAnteil) / Math.log(2));
			float anteil = absolut / rankingSumme;
			writer.wl(
					String.format("%5s", nr + ". ")
					+ String.format("%-48s", p.toString())
					+ String.format("%5s", R.format(score))
					+ String.format("%8s", "(" + P.format(anteil) + ")")
			);
			nr ++;
		}
		writer.wl(
				"Die ersten Werte sind 2er-Logarithmen, d.h. ein um 1 größerer "
				+ "Wert bedeutet doppelt so großen Einfluss, ein um 10 größerer "
				+ "Wert etwa 1000mal so großen. 0 steht für den \"erwarteten\" "
				+ "Wert - falls alle Parteien gleich viel Einfluss hätten."
		);
		writer.wl("");
        
        writeSectionEnd();
	}

    
    private void Inseln() {
        InselVerwaltung iv = InselVerwaltung.getInstance();
        
        
        if (partei.getNummer() == 0) {
            WriteTheLine();
            if (partei.getNummer() == 0) {
                writeSectionStart("Inseln, Ozeane & Lavaseen", true);
            } else {
                // TODO
                writeSectionStart("Kontinente & Inseln", true);
            }
            writer.wl("");
            // Tabelle aller Kontinente / Inseln / Ozeane
            
            SortedSet<Insel> inseln = new TreeSet<Insel>(iv.new InselGroesseComparator());
            SortedSet<Insel> ozeane = new TreeSet<Insel>(iv.new InselGroesseComparator());
            SortedSet<Insel> lavastroeme = new TreeSet<Insel>(iv.new InselGroesseComparator());
            for (int insel : iv.getExistierendeInseln(InselVerwaltung.TYP_LAND)) inseln.add(iv.getInsel(insel));
            for (int insel : iv.getExistierendeInseln(InselVerwaltung.TYP_OZEAN)) ozeane.add(iv.getInsel(insel));
            for (int insel : iv.getExistierendeInseln(InselVerwaltung.TYP_LAVASTROM)) lavastroeme.add(iv.getInsel(insel));
            int summeLandGroesse = 0;
            int summeOzeanGroesse = 0;
            int summeLavastromGroesse = 0;
            for (Insel i : inseln) summeLandGroesse += i.getCoords().size();
            for (Insel i : ozeane) summeOzeanGroesse += i.getCoords().size();
            for (Insel i : lavastroeme) summeLavastromGroesse += i.getCoords().size();
            int anzahlRegionen = summeLandGroesse + summeOzeanGroesse + summeLavastromGroesse;
            
            float anteil = (float)summeLandGroesse / (float)anzahlRegionen;
            writer.wl("Kontinente & Inseln (" + P.format(anteil) + " der Welt)");
            writer.wl("");
            InselTabelle(inseln);
            writer.wl("");

            anteil = (float)summeOzeanGroesse / (float)anzahlRegionen;
            writer.wl("Ozeane & Seen (" + P.format(anteil) + " der Welt)");
            writer.wl("");
            InselTabelle(ozeane);
            writer.wl("");

            anteil = (float)summeLavastromGroesse / (float)anzahlRegionen;
            writer.wl("Lavaströme (" + P.format(anteil) + " der Welt)");
            writer.wl("");
            InselTabelle(lavastroeme);
            writer.wl("");
        }
        
//        for (int publicInselId : iv.getBekannteInselIds(partei)) {
//            int privateId = iv.getPrivateInselNummer(partei, publicInselId);
//            writer.wl("ISLAND " + privateId);
//            writer.wl(iv.getName(partei, publicInselId), "name");
//        }
        
        writeSectionEnd();
    }
    
    private void InselTabelle(Set<InselVerwaltung.Insel> inseln) {
        InselVerwaltung iv = InselVerwaltung.getInstance();

        int summeGroesse = 0;
        for (Insel i : inseln) summeGroesse += i.getCoords().size();
        
        //        >1 3  6   1         2         3         4         5         6
        writer.wl("  #  Name                   O/U Regions (%)    Hö kumul.% ");
		writer.wl("=======================================================================");
        
        float kumul = 0f;
        boolean line50 = false;
        boolean line90 = false;
        for (Insel i : inseln) {
            float anteil = (float)i.getCoords().size() / (float)summeGroesse;
            kumul += anteil;
            
            String anzahlHoehlen = "   ";
            int n = i.getHoehlen().size();
            if (n > 0) anzahlHoehlen = String.format("%3s", " " + n);
            
            String oberOderUnter = "O";
            if (i.istUnterwelt()) oberOderUnter = "U";
            
			writer.wl(
					String.format("%5s", i.getPublicId() + " ")
					+ String.format("%-23s", iv.getInselName(partei, i.getPublicId()))
					+ String.format("%2s", " " + oberOderUnter)
					+ String.format("%7s", N.format(i.getCoords().size()))
					+ String.format("%-9s", " (" + P.format(anteil) + ")")
                    + anzahlHoehlen
					+ " " + String.format("%-7s", "(" + P.format(kumul) + ")")
			);
            
            if ((!line50) && kumul > 0.5) {
                if ((!line90) && kumul > 0.9) { 
                    WriteTheLine(false);
                    line50 = true;
                    line90 = true; 
                } else {
                    WriteTheLine(false);
                    line50 = true;
                }
            }
            if ((!line90) && kumul > 0.9) { WriteTheLine(false); line90 = true; }
        }
        writer.wl("");
    }
    
	/** andere Völker auflisten */
	private void AndereVoelker() {
        // überhaupt irgendjemand?
        boolean irgendwer = false;
        for (Partei other : partei.getBekannteParteien()) {
            if (other.isMonster()) continue;

            WriteTheLine();
            writeSectionStart("Alle bekannten Völker", true);
            writer.wl("");
            irgendwer = true;
            break; // ja - aber den Titel nur einmal schreiben.
        }
        if (!irgendwer) return;

        for (Partei other : partei.getBekannteParteien()) {
            if (other.isMonster()) continue;

            String msg = other.getName() + " [" + other.getNummerBase36() + "], " + other.getEMail();
            writer.wl(msg);
        }

        writeSectionEnd();
	}
	
	
	/**
	 * alle Meldungen für den Spieler
	 */
	private void Meldungen() {
        // new Debug("Es gibt insgesamt " + Message.TotalCount() + " Meldungen.");

		// alle Meldungs-Kategorien
		List<String> kategorien = new ArrayList<String>();
		Map<String, List<Message>> sortiert = new HashMap<String, List<Message>>();
		for (Class<? extends Message> kategorie : Message.AlleArten()) {
            String kat = kategorie.getSimpleName();
			kategorien.add(kat);
			sortiert.put(kat, new ArrayList<Message>());
		}
		// Messages holen und einsortieren
		for (Message msg : Message.Retrieve(partei, (Coords)null, null)) {
			String kat = msg.getClass().getSimpleName();
			if (!sortiert.containsKey(kat)) continue; // Debug und so - wollen wir nicht im Report.
			sortiert.get(kat).add(msg);
		}

        // jetzt die Meldungen für die einzelnen Kategorien ausgeben
		for (Class<? extends Message> kategorie : Message.AlleArten()) {
            String kat = kategorie.getSimpleName();

			List<Message> messages = sortiert.get(kat);
            if (messages.isEmpty()) continue;

			WriteTheLine();

			// Header der Meldung
			writeSectionStart("- " + kat + " -", true);
			writer.wl("");
            
            if (kategorie.equals(Info.class)) Einnahmen();
            if (kategorie.equals(Handelsmeldungen.class)) Handelsbilanz();
			
			// Kategorie nie ins Log-File ausgeben ... darauf läßt sich der Rückschluss
			// Volk <-> Echse bilden !!
			// new SysMsg(" - - Kategorie '" + kat + "'");
			
            for (Message msg : messages) {
                 writer.wl(msg.getText());
            }
			
			// Footer der Meldungen
			writeSectionEnd();
		}

		WriteTheLine();
		writer.wl("");
		writer.wl("");
		writer.wl("");
	}
	
	/** Allianzen auflisten */
	private void Allianzen() {
		boolean header = false;
		
        for (int partnerNr : partei.getAllianzen().keySet()) {
            Allianz a = partei.getAllianz(partnerNr);
            Partei partner = Partei.getPartei(partnerNr);
            if (partner == null) {
                new SysErr("ReportNR: Allianz mit nicht-existenter Partei - " + partei.getNummerBase36() + " mit " + Codierung.toBase36(partnerNr));
                continue;
            }

            if (!a.isValid()) continue; // überspringt z.B. "leere" Allianzen, also solche ohne aktive Option.

            if (!a.isValid()) continue;

            if (!header) { WriteTheLine(); writeSectionStart("- Allianzen & Partner -", true); header = true; }
            String msg = partner + " -";
            for(AllianzOption ao: AllianzOption.values()) {
                if (ao.equals(AllianzOption.Alles)) continue;
                if (partei.hatAllianz(a.getPartner(), ao)) msg += " " + ao.name();
            }
            writer.wl(msg);
        }
        if (header) writeSectionEnd();
	}
	
	/** Steuern auflisten */
	private void Steuern() {
		WriteTheLine();
		writeSectionStart("Handels-Steuern", true);
		writer.wl("");
		
		boolean cont = false;
		String msg = "Die Steuer für alle Völker liegt bei " + partei.getDefaultsteuer() + "%";
		for(Steuer steuer : partei.getSteuern()) {
			Partei p = Partei.getPartei(steuer.getFaction());
			if (cont) msg += ", "; else { cont = true; msg += ", Sondervergünstigungen für: "; }
			msg += p + " (zahlt " + steuer.getRate() + "%)";
		}
		writer.wl(msg + ".");

        writeSectionEnd();
	}
    
    private void Einnahmen() {
        StringBuilder sb = new StringBuilder();
        if (SteuernErpressen.ParteiErtrag.containsKey(partei.getNummer())) {
            sb.append("Eure Steuereintreiber haben in diesem Monat ");
            sb.append(N.format(SteuernErpressen.ParteiErtrag.get(partei.getNummer())));
            sb.append("$ eingenommen. ");
        }
        
        if (Unterhalten.ParteiErtrag.containsKey(partei.getNummer())) {
            sb.append("Die Bauern haben ihren Applaus für eure Spaßmacher und Spielleute mit ");
            sb.append(N.format(Unterhalten.ParteiErtrag.get(partei.getNummer())));
            sb.append("$ ausgedrückt. ");
        }
        
        if (Diebstahl.ParteiErtrag.containsKey(partei.getNummer())) {
            sb.append("Lichtscheue Beobachter konnten ");
            sb.append(N.format(Diebstahl.ParteiErtrag.get(partei.getNummer())));
            sb.append("$ offensichtlich herrenloses Vermögen an sich nehmen. ");
        }
        
        if (sb.length() > 0) {
            writer.wl("");
            writer.wl(sb.toString());
            writer.wl("");
        }
    }
    
    private void Handelsbilanz() {
        StringBuilder sb = new StringBuilder();
        if (Handeln.parteiAusgaben.containsKey(partei.getNummer())) {
            sb.append("Für den Kauf von edlen Luxuswaren habt ihr insgesamt ");
            sb.append(N.format(Handeln.parteiAusgaben.get(partei.getNummer())));
            sb.append("$ ausgegeben. ");
        }
        if (Handeln.parteiEinnahmen.containsKey(partei.getNummer())) {
            sb.append("Der Verkauf von Waren an die Bauern hat alles in allem ");
            sb.append(N.format(Handeln.parteiEinnahmen.get(partei.getNummer())));
            sb.append("$ eingebracht. ");
        }
        if (sb.length() > 0) {
            writer.wl("");
            writer.wl(sb.toString());
            writer.wl("");
        }
    }
	
	/** alle Regionen ausgeben */
	private void Regionen() {
        InselVerwaltung iv = InselVerwaltung.getInstance();
        InselVerwaltung.ParteiReportDaten prd = iv.getParteiReportDaten(partei);
        Set<Coords> nurHistorische = new HashSet<Coords>();
        for (RegionsSicht rs : prd.getHistorische()) {
            nurHistorische.add(rs.getCoords());
        }
        
        for (Insel i : prd.getBekannteInseln()) {
            WriteTheLine();
            writer.wl("");
            
            String name = i.getName(partei);
            if (name == null) name = "Insel bei " + partei.getPrivateCoords(i.getMittelpunkt());
            writeSectionStart(name, true);
            
            String beschreibung = iv.getInselBeschreibung(partei, i.getPublicId());
            if (beschreibung != null) {
                writer.wl("");
                writer.wl(beschreibung);
                writer.wl("");
            }
            
            List<Coords> sortiert = new ArrayList<Coords>();
            sortiert.addAll(prd.getRegionenAufInsel(i.getPublicId()));
            Collections.sort(sortiert, new CoordComparatorLNR());

            // List<String> echo = new ArrayList<String>();
            for (Coords c : sortiert) {
                RegionsSicht rs = partei.getRegionsSicht(c);
                // echo.add(rs+"");
                
                if (rs != null) {
                    Region(Region.Load(c));
                    continue;
                }
                if (nurHistorische.contains(c)) {
                    // new Debug("Nur historische Sicht auf " + c + ".");
                    AtlasRegion(c);
                    continue;
                }
            }
            // new Debug("NR2/Regionssichten auf Insel " + i + ": " + StringUtils.aufzaehlung(echo));
            
            writeSectionEnd();
        }
        
        List<Coords> regionenOhneInsel = prd.getRegionenOhneInsel();
        if (!regionenOhneInsel.isEmpty()) {
            WriteTheLine();
            writer.wl("");
            writeSectionStart("Auf unbekannten Inseln", true);
            for (Coords c : regionenOhneInsel) {
                Region(Region.Load(c));
            }
            writeSectionEnd();
        }
	}
	
    private void AtlasRegion(Coords c) {
        RegionsSicht rs = partei.atlas.get(c);
        Coords privateCoords = partei.getPrivateCoords(c);
		String terrain = rs.getTerrain();
        String name = rs.getName();
        int sichtung = rs.getRunde();
        
		StringBuilder msg = new StringBuilder();

        msg.append(">> ");
        msg.append(terrain);
        if ((name != null) && (!"null".equals(name))) msg.append(" ").append(name);
        msg.append(" (").append(privateCoords.getX()).append(" ").append(privateCoords.getY()).append("), ");
        msg.append("zuletzt besucht in Runde ").append(sichtung).append(".");
        msg.append(" <<");
        
		writer.wl(msg.toString());
    }
    
    private void Region(Region r) {
//        if (ZATMode.CurrentMode().isDebug()) {
//            RegionsSicht rs = partei.getRegionsSicht(r.getCoords());
//            String code = "<null>";
//            if (rs != null) code = rs.toCode();
//            writer.wl("");
//            writer.wl(code);
//            writer.wl("");
//        }
        

        RegionsSicht rs = partei.getRegionsSicht(r.getCoords());
        boolean details = false;
        if (rs != null) details = rs.hasDetails();
        if (partei.getNummer() == 0) details = true;

        // WriteTheLine();
        Region_Header(r, details); // beinhaltet einen sectionStart


        if (details) {
            Region_Bewacher(r);
            Region_Strassen(r);
            Region_Handel(r);
            Region_Statistik(r);
            Region_Meldungen(r);
            Region_Einheiten(r);
            writeSectionEnd();
        } else {
            // Das SectionEnd wurde dann schon in Region_Header geschrieben.
        }
    }
    
	private void Region_Header(Region r, boolean details) {
		StringBuilder msg = new StringBuilder();

        Coords privateCoords = partei.getPrivateCoords(r.getCoords());
		String terrain = r.getClass().getSimpleName();
        if (!details) {
			if (!r.istBetretbar(null) || r instanceof Chaos) {
                // undetaillierte Ozeane etc.
                msg.append(">> ").append(terrain);
                msg.append(" (").append(privateCoords.getX()).append(" ").append(privateCoords.getY()).append("). ");
            } else {
                // undetaillierte Länder:
                msg.append(">> ").append(r.getName());
                msg.append(" (").append(privateCoords.getX()).append(" ").append(privateCoords.getY()).append("), ");
                msg.append(terrain).append(". ");
            }
        } else if ((!r.istBetretbar(null)) || (r instanceof Chaos)) {
			msg.append(
                    ">> ").append(terrain).append(
                    " (").append(privateCoords.getX()).append(" ").append(privateCoords.getY()).append("), ");
		} else {
			msg.append(">> ").append(r.getName()).append(
                    " (").append(privateCoords.getX()).append(" ").append(privateCoords.getY()).append("), ");
            msg.append(terrain).append(", ");
            if (r.getResource(Holz.class).getAnzahl() > 0) {
                msg.append(r.getResource(Holz.class).getAnzahl()).append(" Bäume, ");
            }
			msg.append(N.format(r.getBauern())).append(" Bauern, $").append(N.format(r.getSilber())).append(" Silber. ");
		}
        
        RegionsSicht rs = partei.getRegionsSicht(r.getCoords());
        if ((rs != null) && (rs.getQuelle() == Leuchtturm.class)) {
            msg.append("Den Bericht von hier liefert euch ein Leuchtturm. ");
        }
        
        // Wenn keine Details, dann war's das auch schon:
        if (!details) msg.append(" <<");

		if (details) {
            List<Item> tiere = new ArrayList<Item>();
            for (Item resource : r.getResourcen()) {
                if ((resource instanceof AnimalResource) && (resource.getAnzahl() > 0)) tiere.add(resource);
            }
            if (!tiere.isEmpty()) {
                msg.append("Hier können ").append(StringUtils.aufzaehlung(tiere)).append(" gefangen werden. ");
            }

            // Holz und Eisen
            List<Item> rohstoffe = new ArrayList<Item>();
            for (Item resource : r.getResourcen()) {
                if (resource.getAnzahl() == 0) continue;
                if (!(resource instanceof Resource)) continue;
                if (resource instanceof LuxusGood) continue;
                rohstoffe.add(resource);
            }
            if (!rohstoffe.isEmpty()) {
                msg.append(StringUtils.aufzaehlung(rohstoffe)).append(" können gewonnen werden. ");
            }
        
		
            // Nachbarregionen: Land
            List<String> landNachbarn = new ArrayList<String>();
            for(Richtung ri : Richtung.values()) {
                Region hr = Region.Load(r.getCoords().shift(ri));
                if (hr == null) {
                    landNachbarn.add("im " + ri.name() + " liegt Chaos");
                } else {
                    if (hr instanceof Ozean) {
                        continue;
                    } else if (hr instanceof Berge) {
                        landNachbarn.add("im " + ri.name() + " liegen die Berge von " + hr.getName());
                    } else if (hr instanceof Vulkan) {
                        landNachbarn.add("im " + ri.name() + " liegt der Vulkan " + hr.getName());
                    } else if (hr instanceof aktiverVulkan) {
                        landNachbarn.add("im " + ri.name() + " liegt der aktive Vulkan " + hr.getName());
                    } else if (hr instanceof Sandstrom) {
                        landNachbarn.add("im " + ri.name() + " liegt der Sandstrom von " + hr.getName());
                    } else if (hr instanceof Lavastrom) {
                        landNachbarn.add("im " + ri.name() + " fließt ein Lavastrom");
                    } else if (hr instanceof Chaos) {
                        landNachbarn.add("im " + ri.name() + " liegt Chaos");
                    } else {
                        landNachbarn.add("im " + ri.name() + " liegt " + hr.getArtikel() + (hr.getArtikel().length()>0?" ":"") + hr);
                    }
                }
            }
            if (!landNachbarn.isEmpty()) msg.append(StringUtils.ucfirst(StringUtils.aufzaehlung(landNachbarn))).append(". ");

            // Nachbarregionen: Wasser
            List<String> wasserNachbarn = new ArrayList<String>();
            for(Richtung ri : Richtung.values()) {
                Region hr = Region.Load(r.getCoords().shift(ri));
                if (hr == null)	continue;
                if (hr instanceof Ozean) {
                    wasserNachbarn.add(ri.toString() + " (Sturmwahrscheinlichkeit ist " + ((Ozean)hr).getSturmValue() + ")");
                }
            }
            if (!wasserNachbarn.isEmpty()) msg.append("Im ").append(StringUtils.aufzaehlung(wasserNachbarn)).append(" liegt die offene See. ");

            // Beschreibung
            if (r.getBeschreibung().length() > 0) {
                msg.append(r.getBeschreibung());
                if (!r.getBeschreibung().endsWith(".")) msg.append(".");
                msg.append(" ");
            }
        } // end if (details)
		
		writer.wl(msg.toString());
		if (details) writer.wl("");
	}
    
    private void Region_Bewacher(Region r) {
        // Bewacher?
		StringBuilder msg = new StringBuilder();
        Set<Partei> bewacherParteien = new HashSet<Partei>();
        for (Partei p : r.anwesendeParteien()) {
            for(Unit unit : r.getUnits()) {
                if (unit.getOwner() != p.getNummer()) continue;
                if (!unit.getBewacht()) continue;
                bewacherParteien.add(p);
            }
        }
        if (!bewacherParteien.isEmpty()) {
            List<String> parteiMeldungen = new ArrayList<String>();
            for (Partei p : bewacherParteien) {
                Set<Unit> wachen = new HashSet<Unit>();
                for(Unit unit : r.getUnits()) {
                    if (unit.getOwner() != p.getNummer()) continue;
                    if (!unit.getBewacht()) continue;
                    wachen.add(unit);
                }
                parteiMeldungen.add(p + " (" + StringUtils.aufzaehlung(wachen) + ")");
            }
            msg.append("Die Region wird von ").append(StringUtils.aufzaehlung(parteiMeldungen)).append(" bewacht. ");
        }
		if (msg.length() > 0) {
			writer.wl(msg.toString());
			writer.wl("");
		}
    }

	private void Region_Meldungen(Region r) {
		writer.NRFront += 3;

		List<Message> meldungen = Message.Retrieve(null, r.getCoords(), null);
		boolean omniszienz = partei.getNummer() == 0;
		boolean any = false;
		for (Message m : meldungen) {
			// nur anzeigen, wenn die Meldung nicht zu einer bestimmten Einheit gehört:
			if (m.getUnit() != null) continue;
			
			if ((m.getPartei() != null) && (!omniszienz)) {
				if (m.getPartei().getNummer() != partei.getNummer()) {
					continue; // fremde Meldungen überspringen
				}
			}

			any = true;
			writer.wl("@@@ " + m.getText());
		}
		if (any) writer.wl("");

		writer.NRFront -= 3;
	}

	private void Region_Strassen(Region r) {
		List<String> strassen = new ArrayList<String>();
        for(Richtung richtung : Richtung.values()) {
			if (r.getStrassensteine(richtung) > 0) {
				Region hr = Region.Load(r.getCoords().shift(richtung));
				String diese = hr + " ";
				diese += "(" + richtung.name();
				long prozent = Math.round( ((double) r.getStrassensteine(richtung) / (double) r.getSteineFuerStrasse()) * 100.0);
				if (prozent < 100) {
					diese += ", " + r.getStrassensteine(richtung) + " Pflastersteine = " + prozent + "% fertig)";
				} else diese += ")";
                strassen.add(diese);
			}
		}
		if (!strassen.isEmpty()) {
            String msg = "";
            if (strassen.size() == 1) {
                msg = "Es führt eine Strasse nach ";
            } else {
                msg = "Es führen Strassen nach ";
            }
            msg += StringUtils.aufzaehlung(strassen);
            msg += ".";
            writer.wl(msg);
            writer.wl("");
        }
	}
	
	private void Region_Handel(Region r) {
        if (!r.istBetretbar(null)) return;
		StringBuilder msg = new StringBuilder();
		
		if (r.hatGebaeude(Burg.class, 2, null))	{
			msg.append("Die Bauern kaufen hier: ");
		} else {
			msg.append("Mit einer Burg würden hier folgende Preise gelten: ");
		}
		
        List<String> ankauf = new ArrayList<String>();
        List<String> verkauf = new ArrayList<String>();
        for(int i = 0; i < r.getLuxus().size(); i++) {
            Nachfrage n = r.getLuxus().get(i);
            String warenName = n.getItem().getSimpleName();
            Item ware = (Item)Paket.FindItem(warenName).Klasse;
            if (n.getNachfrage() > 0) {
                int preis = (int) (n.getNachfrage() * ware.getPrice());
                ankauf.add(warenName + " " + preis + "$");
            } else {
                int preis = (int) ((0 - n.getNachfrage()) * ware.getPrice());
                verkauf.add(warenName + " (" + preis + "$)");
            }
        }

        msg.append(StringUtils.aufzaehlung(ankauf)).append(". ");
        msg.append("Die Bauern produzieren ").append(StringUtils.aufzaehlung(verkauf)).append(". ");

        writer.wl(msg.toString());
        writer.wl("");
	}
	
	
	private void Region_Statistik(Region r)	{
		NumberFormat N = NumberFormat.getIntegerInstance(Locale.GERMANY);
		N.setGroupingUsed(true);

		int vorhersageUnterhaltungSilber = (r.getSilber() / 20);
		if (r.freieArbeitsplaetze() > 0) {
			vorhersageUnterhaltungSilber += (r.getBauern() * r.getLohn()) / 20;
		} else {
			// r.freieArbeitsplaetze() ist dann 0 oder negativ,
			// es werden also weniger Bauern berücksichtigt, als vorhanden sind:
			int produktiveBauern = r.freieArbeitsplaetze() + r.getBauern();
			if (produktiveBauern < 0) produktiveBauern = 0;
			vorhersageUnterhaltungSilber += (produktiveBauern * r.getLohn()) / 20;
		}

		if (r.istBetretbar(null)) {
			int silberProBauer = 0;
			if (r.getBauern() > 0) {
				silberProBauer = Math.round((float)r.getSilber() / (float)r.getBauern());
			}

            StringBuffer msg = new StringBuffer();
			if (Environment.VerhungerteBauern().containsKey(r)) {
				if (Environment.VerhungerteBauern().get(r) > 0) {
					msg.append("In diesem Monat sind ").append(Environment.VerhungerteBauern().get(r)).append(" Bauern verhungert! ");
				}
			}

			if (Environment.NeugeboreneBauern.containsKey(r)) {
				int n = Environment.NeugeboreneBauern.get(r);
				if (n > 0) {
					msg.append(n).append(" Kinder sind auf die Welt gekommen. ");
				}
			}

			int arbeiter = r.freieArbeitsplaetze() + r.getBauern();
			
			if (r.getBauern() > 0) {
				float wohlstand = Reporte.SilberProBauerSerie().getQuantilForValue(silberProBauer);
				String wohlstandsAttribut = "bitterarmen";
				if (wohlstand > 0.1) wohlstandsAttribut = "sehr armen";
				if (wohlstand > 0.25) wohlstandsAttribut = "armen";
				if (wohlstand > 0.5) wohlstandsAttribut = "genügsamen";
				if (wohlstand > 0.75) wohlstandsAttribut = "wohlhabenden";
				if (wohlstand > 0.95) wohlstandsAttribut = "reichen";
				if (wohlstand > 0.99) wohlstandsAttribut = "dekadenten";

				msg.append("Die ").append(wohlstandsAttribut).append(" Bauern ");
				if (r.getSilber() > 0) {
					msg.append("besitzen im Durchschnitt $").append(silberProBauer).append(", ");
				} else { msg.append("haben nicht einen einzigen Notgroschen, "); }
				if (arbeiter < 0) {
					msg.append("würden theoretisch $").append(r.getLohn()).append(" Lohn bekommen ");
				} else {
					msg.append("erhalten $").append(r.getLohn()).append(" Lohn ");
				}
				if (vorhersageUnterhaltungSilber > 0) {
					msg.append("und können zusammen voraussichtlich $").append(
						N.format(vorhersageUnterhaltungSilber)).append(
						" Silber").append(" zu ihrer Unterhaltung ausgeben. ");
				} else {
					msg.append("und werden wohl nichts zu ihrer Unterhaltung ausgeben können. ");
				}
			}

			if (r.freieArbeitsplaetze() > 0) {
				msg.append("Es gibt Arbeit für ").append(N.format(r.freieArbeitsplaetze())).append(" neue Bauern. ");
			} else {
				if (arbeiter == 0) {
                    if (r.getBauern() > 0) {
                        msg.append("Alle Bauern sind bei der Arbeit, neue Bauern werden nicht gebraucht. ");
                    }
				} else if (arbeiter > 0) {
					if (arbeiter > 1) {
						msg.append("Nur ").append(N.format(arbeiter)).append(" Bauern finden einträgliche Arbeit. ");
					} else {
						msg.append("Nur ein einziger Bauern hat einträgliche Arbeit. ");
					}
				} else {
					msg.append("Kein einziger Bauer kann hier von seiner Arbeit leben! ");
				}
			}

			if (r.Rekruten() > 0) {
				if (r.Rekruten() > 1) {
					msg.append(r.Rekruten()).append(" Bauern wollen in Euren Dienst treten. ");
				} else {
					msg.append("1 Bauer will in Euren Dienst treten. ");
				}
			}

			if (msg.length() > 0) {
                writer.wl(msg.toString());
                writer.wl("");
            }


			// Migration / Bauernwanderung:
			msg = new StringBuffer();

			int gesamtBilanz = 0;
			List<String> descHer = new ArrayList<String>();
			List<String> descWeg = new ArrayList<String>();
			for (Region nachbar : r.getNachbarn()) {
				int weg = BauernWanderung.GetBauernWanderung(r, nachbar);
				int her = BauernWanderung.GetBauernWanderung(nachbar, r);

				int bilanz = her - weg;

				if (bilanz == 0) continue;

				String nachbarText = nachbar.toString();

				if (bilanz > 0) {
					descHer.add(bilanz + " aus " + nachbarText);
				} else {
					descWeg.add(-1 * bilanz + " nach " + nachbarText);
				}

				gesamtBilanz += bilanz;
			}

			int gesamtBewegungen = descWeg.size() + descHer.size();
			if (gesamtBewegungen > 0) {
				if (gesamtBewegungen > 1) {
					if (gesamtBilanz > 0) {
						if (gesamtBilanz > 1) {
							msg.append("In der Summe sind ").append(gesamtBilanz).append(" Bauern zugewandert: ");
						} else {
							msg.append("In der Summe ist ").append(gesamtBilanz).append(" Bauer zugewandert: ");
						}
						msg.append(StringUtils.aufzaehlung(descHer));
						if (!descWeg.isEmpty()) msg.append("; ausgewandert sind ").append(StringUtils.aufzaehlung(descWeg));
						msg.append(".");
					} else if (gesamtBilanz < 0) {
						if (gesamtBilanz < -1) {
							msg.append("In der Summe sind ").append(-1 * gesamtBilanz).append(" Bauern abgewandert: ");
						} else {
							msg.append("In der Summe ist ").append(-1 * gesamtBilanz).append(" Bauern abgewandert: ");
						}
						msg.append(StringUtils.aufzaehlung(descWeg));
						if (!descHer.isEmpty()) msg.append("; eingewandert sind ").append(StringUtils.aufzaehlung(descHer));
						msg.append(".");
					} else {
						msg.append("Ein- und Auswanderung sind ausgeglichen.");
					}
				} else {
					if (!descWeg.isEmpty()) {
						if (gesamtBilanz < -1) {
							msg.append(descWeg.get(0).replaceFirst("( nach)", " Bauern sind nach")).append(" ausgewandert");
						} else {
							msg.append(descWeg.get(0).replaceFirst("( nach)", " Bauer ist nach")).append(" ausgewandert");
						}
					}
					if (!descHer.isEmpty()) {
						if (gesamtBilanz > 1) {
							msg.append(descHer.get(0).replaceFirst("( aus)", " Bauern sind aus")).append(" eingewandert");
						} else {
							msg.append(descHer.get(0).replaceFirst("( aus)", " Bauer ist aus")).append(" eingewandert");
						}
					}
					msg.append(".");
				}
			}


			if (msg.length() > 0) {
                writer.wl(msg.toString());
                writer.wl("");
            }

        } // end if r.istBetretbar()

		int personen = 0;
		int einheiten = 0;
        int einkommen = 0;
		int silber = 0;

        Set<Unit> kandidaten;
		if (partei.getNummer() == 0) {
			kandidaten = r.getUnits(); // alle
		} else {
			kandidaten = r.getUnits(partei.getNummer()); // nur unsere
		}
		for(Unit u : kandidaten) {
			einheiten++;
			personen += u.getPersonen();
			einkommen += u.getEinkommen();
			silber += u.getItem(Silber.class).getAnzahl();
        }
		String personenS = N.format(personen);
		String einkommenS = N.format(einkommen);
		String silberS = N.format(silber);
		if (personen > 0) {
            StringBuilder msg = new StringBuilder();
            String einheitenPhrase = (einheiten != 1)?(einheiten + " Einheiten"):("einer Einheit");

            if (personen > 1) {
                msg.append(personenS).append(" Personen in ").append(einheitenPhrase);
                if (einkommen > 0) msg.append(" haben $").append(einkommenS).append(" verdient");
                msg.append(", gesamt $").append(silberS).append(" Silber.");
            } else {
                msg.append("Eine Person");
                if (einkommen > 0) {
					msg.append(" hat $" + einkommenS + " verdient").append(", gesamt $" + silberS + " Silber.");
				} else {
					if (silber > 0)	msg.append(" mit $" + silberS + " Silber.");
					if (silber <= 0) msg.append(", völlig pleite.");
				}
            }
			writer.wl(msg.toString());
			writer.wl("");
		}
	}
	
	
	private void Region_Einheiten(Region r)	{
		writer.NRFront = 3;

		// nach Macht und SORTIERE-Rang ordnen:
		final SortedSet<Unit> units = new TreeSet<Unit>(new RegionsMachtComparator(r));
		units.addAll(r.getUnits());

		
		// Gebäude
		for(Building building : r.getBuildings()) {
			Region_Building(building);
			writer.NRFront = 5;
			for(Unit u : building.getUnits()) Region_Einheit(u);
			writer.NRFront = 3;
            writer.wl(" <<"); // die Code Folding section schließen
		}
		
		// Schiffe
		for(Ship ship : r.getShips()) {
			Region_Ship(ship);
			writer.NRFront = 5;
			for(Unit u : ship.getUnits()) Region_Einheit(u);
			writer.NRFront = 3;
            writer.wl(" <<"); // die Code Folding section schließen
		}
		
		// übrigen Einheiten
		for(Unit u : units) {
			if ((u.getGebaeude() == 0) && (u.getSchiff() == 0)) Region_Einheit(u);
		}
		
		writer.NRFront = 1;
	}
	
	private void Region_Building(Building b) {
		Unit u = Unit.Load(b.getOwner());
		
		String msg = ">> " + b.getName() + " [" + b.getNummerBase36() + "], Größe " + b.getSize() + ", ";// + b.getTyp();
		if (b.getClass().equals(Burg.class)) msg += ((Burg)b).getBurgTyp(); else msg += b.getTyp();		
		if (u == null) {
			msg += ", das Gebäude hat keinen Besitzer";
		} else {
			msg += ", Besitzer ist " + u;
		}
		
		if (b.istBelagert() != null) {
            msg += ", wird belagert. ";
        } else {
            msg += ". ";
        }
		if (b.getBeschreibung().length() > 0) {
            msg += b.getBeschreibung();
            if (b.getBeschreibung().endsWith(".")) msg += ".";
        }
		
		writer.wl(msg.toString());
		writer.wl("");
	}

	private void Region_Ship(Ship s) {
		StringBuilder msg = new StringBuilder();
        msg.append(">> ").append(s.getName()).append(" [").append(s.getNummerBase36()).append("], ");

		long prozent = Math.round( ((double) s.getGroesse() / (double) s.getConstructionSize()) * 100.0);
		msg.append(s.istFertig() ? "fertig [" : "");
        msg.append(prozent).append("%");
        msg.append(s.istFertig() ? "]" : "");
        
		msg.append(", ").append(s.getTyp());
        
		Unit u = Unit.Load(s.getOwner());
		if (u == null) {
			msg.append(", das Schiff hat keinen Besitzer");
		} else if (!partei.cansee(u)) {
			msg.append(", wir können den Besitzer nicht erkennen");
		} else {
			msg.append(", Besitzer ist ").append(u);
			if (s.istFertig()) {
				if (u.getOwner() == partei.getNummer()) msg.append(", Kapazität: ").append(s.getKapazitaetFree() / 100).append(" GE frei");
			}
		}
		
		if (s.getKueste() != null) msg.append(", das Schiff ankert an der Küste im ").append(s.getKueste().name());
		
        msg.append(".");
		
		if (s.getBeschreibung().length() > 0) {
            msg.append(" ").append(s.getBeschreibung());
            if (!s.getBeschreibung().endsWith(".")) msg.append(".");
        }
		
        writer.wl(msg.toString());
		writer.wl("");
	}

	/**
	 * erstellt aus dem Prefix und der Rasse entsprechend die eigentliche Rasse
	 * @param prefix
	 * @param rasse
	 * @return
	 */
	private String preparePrefixRace(String prefix, String rasse) {
		if (prefix.length() > 0) {
			if (prefix.endsWith("-")) return prefix + rasse;
			return prefix + rasse.toLowerCase();
		}
		return rasse;
	}
	
	private void Region_Einheit(Unit u)	{
        boolean omniszienz = (partei.getNummer() == 0);
		if (!omniszienz) {
            // Mantis #324 - außerdem: 
			// "Im Schiff kann man sich an Land nicht verstecken, auf See dagegen schon!" (partei.cansee(Unit))
			// Sichtbarkeit von Einheiten mit Tarnung prüfen
			if (/* (u.getSchiff() == 0) && */ (u.getGebaeude() == 0)) {
				if (!partei.cansee(u)) return;
			}
        }
		
		Partei p = null;
		if (u.getTarnPartei() != 0) p = Partei.getPartei(u.getTarnPartei());

		StringBuilder msg = new StringBuilder();
        msg.append(u).append(", ");
		if (u.getOwner() == partei.getNummer()) {
			// eigene Einheit
			msg.insert(0, ">> * ");

			if (u.getSichtbarkeit() == 1) msg.append("versteckt sich, ");

			if (p == null) {
				msg.append("Partei ist getarnt, ");
			} else {
				if (u.getTarnPartei() != u.getOwner()) msg.append("Einheit ist getarnt als ").append(p).append(", ");
			}
		} else {
			if (p == null) {
				msg.insert(0, ">> - ");
                msg.append("Partei ist getarnt, ");
			} else {
				if (partei.hatAllianz(p.getNummer())) {
                    msg.insert(0, ">> + ");
                } else {
                    msg.insert(0, ">> - ");
                }
				msg.append(p.toString()).append(", ");
			}
		}
		if (omniszienz) {
			if ((p != null) && (p.getNummer() != u.getOwner())) {
				msg.append("wahre Partei ").append(p.toString()).append(", ");
			}
		}
		
		if (u.getBeschreibung().length() > 0) {
			msg.delete(msg.length()-2, msg.length()); // das letzte Komma und Leerzeichen entfernen

            String ersterBuchstabe = u.getBeschreibung().substring(0, 1);
            if (ersterBuchstabe.equals(ersterBuchstabe.toUpperCase())){
                msg.append(" - ");
            } else {
                msg.append(" ");
            }
			msg.append(u.getBeschreibung());
            if (u.getBeschreibung().endsWith(".")) msg.append(" "); else msg.append(", ");
        }
		
		// Personen mit Rasse
		if (Building.BewohnerCache().contains(u)) {
			if ((u.getOwner() != partei.getNummer()) && (!omniszienz)) {
				Building b = Building.getBuilding(u.getGebaeude());
				Unit owner = Unit.Load(b.getOwner());
				if (owner == null) {
					msg.append("1 ");
				} else {
					if (owner.getOwner() == partei.getNummer()) {
						// der Eigentümer der Burg erhält den Report ... daher die Personen
						// der Einheit anzeigen ... ist ja seine Burg
						msg.append(u.getPersonen()).append(" ");
					} else {
						msg.append("1 ");
					}
				}
			} else {
				msg.append(u.getPersonen()).append(" ");
			}
		} else {
			msg.append(u.getPersonen()).append(" ");
		}
		if (u.getTarnRasse().length() > 0) {
			msg.append(preparePrefixRace(u.getPrefix(), u.getTarnRasse()));
			if ((u.getOwner() == partei.getNummer()) || omniszienz) {
                msg.append(" (").append(u.getClass().getSimpleName()).append("), ");
            } else msg.append(", ");
		} else {
			msg.append(preparePrefixRace(u.getPrefix(), u.getRasse())).append(", ");
		}


        // Partei-spezifisches
		if ((u.getOwner() == partei.getNummer()) || omniszienz) {
			// Hunger und Verletzungen
			msg.append(u.strLebenspunkte()).append(", ");
			
			// Kampf und Belagerung
			msg.append("kämpft ").append(u.getKampfposition().name().toLowerCase()).append(", ");
            if (u.getBelagert() > 0) {
                Building b = Building.getBuilding(u.getBelagert());
                if (b != null) {
                    msg.append("belagert ").append(b).append(", ");
                } else {
					new SysErr("Einheit " + u + " wollte Gebäude " + Codierung.toBase36(u.getBelagert()) + " belagern; das gibt es aber nicht (mehr).");
                    u.setBelagert(0);
                }
            }

            
			// Talente
			List<String> talente = new ArrayList<String>();
			for(Skill skill : u.getSkills())	{
				if (skill.getLerntage() == 0) continue;
                int tageProPerson = skill.getLerntage() / u.getPersonen();
				talente.add(skill.getName() + " " + u.Talentwert(skill.getClass()) + " (" + tageProPerson + ")");
			}
            if (talente.isEmpty()) {
                msg.append("dumm wie Stroh");
            } else {
                msg.append("Talente: ").append(StringUtils.aufzaehlung(talente));
            }
            msg.append(". ");

			// magische Werte
			if (u.getSkill(Magie.class).getLerntage() > 0) {
				msg.append(u.getAura() + " Aura");
				if (u.getMana() > 0) {
					msg.append(" und " + u.getMana() + " Mana");
				}
				msg.append(". ");
			}
			
			// Zaubersprüche
			if (u.getSpells().size() > 0) {
                List<String> sprueche = new ArrayList<String>();
				for(Spell spell : u.getSpells()) {
                    sprueche.add("'" + spell.getName() + "' (Stufe " + spell.getStufe() + ")" );
                }
				msg.append("Zaubersprüche: " + StringUtils.aufzaehlung(sprueche) + ". ");
				
				// Kampfzauber
                msg.append("Aktive Kampfzauber - ");
				String as = u.getStringProperty(Kampfzauber.ATTACKSPELL, "");
				String ds = u.getStringProperty(Kampfzauber.DEFENCESPELL, "");
				String cs = u.getStringProperty(Kampfzauber.CONFUSIONSPELL, "");
				msg.append("Verwirrung: " + (cs.length() > 0 ? cs : "keiner") + ", ");
				msg.append("Angriff: " + (as.length() > 0 ? as : "keiner") + ", ");
				msg.append("Verteidigung: " + (ds.length() > 0 ? ds : "keiner") + ". "); 
			}

            // Reisemöglichkeiten:
            if ((u.getSchiff() == 0) || (Region.Load(u.getCoords()).istBetretbar(u))) {
                msg.append(getReiseBedingungen(u) + ". ");
            } else {
                Ship s = Ship.Load(u.getSchiff());
                if ((s != null) && (s.getOwner() == u.getNummer())) {
                    // ist Kapitän und befindet sich auf dem Meer:
                    if (u.getPersonen() == 1) {
                        msg.append(u.getName() + " gibt die Befehle für " + s.getTyp() + " " + s + ". ");
                    } else {
                        msg.append("Sie geben die Befehle für " + s.getTyp() + " " + s + ". ");
                    }
                } else {
                    // befindet sich an Bord eines Schiffes in einer nicht-betretbaren Region
                    // (oder das Schiff gibt es nicht...)
                }
            }
            

		} else {
            // Belagerung auch für fremde Einheiten angeben:
            if (u.getBelagert() > 0) {
                Building b = Building.getBuilding(u.getBelagert());
                if (b != null) {
                    msg.append(" belagert " + b + " ");
                } else {
					new SysErr(u + " wollte Gebäude " + Codierung.toBase36(u.getBelagert()) + " belagern; das gibt es aber nicht (mehr).");
                    u.setBelagert(0);
                }
            }
        }
		
		// Items
		if (partei.cansee(u)) {
			// wenigstens die Ausrüstung kann man mit Tarnung verbergen, auch wenn
			// eine Einheit auf einem Schiff oder in einem Gebäude ist.
            String verb = "Sie haben";
            if (u.getPersonen() == 1) verb = "Hat";


			List<String> items = new ArrayList<String>();
			for(Item item : u.getItems()) {
				if (item.getAnzahl() == 0) continue;
				if (item.getClass().equals(Silber.class)) {
					if ((u.getOwner() != partei.getNummer()) && (!omniszienz)) {
						items.add("einen Silberbeutel");
					} else {
						items.add(item.toString());
					}
				} else {
					if (omniszienz) {
						items.add(item.toString());
					} else if ( u.imGebaeude() && (u.getOwner() != partei.getNummer()) ) {
						int alt = item.getAnzahl();
						item.setAnzahl(1);
						items.add("1 " + item.getName());
						item.setAnzahl(alt);
					} else {
						items.add(item.toString());
					}
				}
			}

			if (items.isEmpty()) {
				msg.append(verb).append(" nichts");
			} else {
				msg.append(verb).append(" ").append(StringUtils.aufzaehlung(items));
			}

			if ((u.getOwner() == partei.getNummer()) || omniszienz) {
				msg.append("," + (items.isEmpty()?" ":" mit Sack und Pack ") + ((int)Math.ceil(u.getGewicht() / 100f) + " GE. "));
			} else {
				msg.append(". ");
			}
		} // endif partei.cansee(u)
		
		if ((u.getOwner() == partei.getNummer()) || omniszienz){
			// Befehle
            List<String> befehlsTexte = new ArrayList<String>();
            for (Einzelbefehl eb : u.BefehleExperimental) befehlsTexte.add(eb.getBefehlCanonical());
            if (befehlsTexte.isEmpty()) {
                msg.append("Keine Befehle. ");
            } else {
                msg.append("Befehle: ").append(StringUtils.aufzaehlung(befehlsTexte)).append(". ");
            }

			// Meldungen
			List<Message> unitMessages = Message.Retrieve(null, (Coords)null, u);
			if (!unitMessages.isEmpty()) {
				writer.wl(msg.toString());
				writer.NRFront += 3;
                List<String> texte = new ArrayList<String>();
				for (int i=0 ; i < unitMessages.size(); i++) {
                    String text = unitMessages.get(i).getText();
                    if (text.contains(u + " - ")) {
                        text = text.replace(u + " - ", "").trim();
                    }
                    if (text.contains(u + " ")) {
                        text = text.replace(u + " ", "").trim();
                    }
                    if (text.endsWith(".")) {
                        // erster Buchstabe klein, Punkt abtrennen:
                        text = text.substring(0, 1).toLowerCase() + text.substring(1, text.length() - 1);
                    }
                    texte.add(text);
				}
                String alles = StringUtils.aufzaehlung(texte);
                alles = alles.substring(0, 1).toUpperCase() + alles.substring(1) + ".";
                writer.wl(alles + " <<");
				writer.NRFront -= 3;
			} else {
				writer.wl(msg.toString() + "<<");
			}
		} else {
			// keine Details:
			writer.wl(msg.toString() + "<<");
		}

		writer.wl("");
	}

    private String getReiseBedingungen(Unit u) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> tags = u.getGewichtTags();

        String verb = "können";
        if (u.getPersonen() == 1) verb = "kann";

        int frei = -1;
        String freiText = null;
        if (tags.containsKey(Unit.TAG_FREIE_KAPAZITAET)) {
            frei = Integer.parseInt(tags.get(Unit.TAG_FREIE_KAPAZITAET));
            freiText = (int)Math.abs(frei) + " GE ";
            if (frei >= 0) {
                freiText = "(" + freiText + "frei)";
            } else {
                freiText = "(um " + freiText + "überladen)";
            }
        }

        int freiBeritten = -1;
        String freiBerittenText = null;
        if (tags.containsKey(Unit.TAG_FREIE_KAPAZITAET_BERITTEN)) {
            freiBeritten = Integer.parseInt(tags.get(Unit.TAG_FREIE_KAPAZITAET_BERITTEN));
            freiBerittenText = (int)Math.abs(freiBeritten) + " GE ";
            if (freiBeritten >= 0) {
                freiBerittenText = "(" + freiBerittenText + "frei)";
            } else {
                freiBerittenText = "(um " + freiBerittenText + "überladen)";
            }
        }

        // hat u genug Tiere (im Zweifelsfall reichen 0)?
        String zugtiereFehlenText = "";
        if (!u.hatGenugZugtiere()) {
            Set<String> zuviel = new HashSet<String>();
            if (u.hat(Wagen.class)) zuviel.add("Wagen");
            if (u.hat(Katapult.class)) zuviel.add("Katapulte");
            zugtiereFehlenText = verb + " nicht reisen, es fehlen Zugtiere für die " + StringUtils.aufzaehlung(zuviel) + " " + freiText;
        }


        if (u.canWalk()) {
            // hat genug Tiere und Talent:
            if (!u.hatTiere()) {
                // genau dann, wenn 0 Wagen und 0 Zugtiere...
                sb.append(verb + " reisen " + freiText);
            } else {
                // u hat Tiere und kann gehen:
                if (u.canRideAnimals()) {
                    sb.append(verb + " zu Fuß " + freiText + " oder beritten " + freiBerittenText + " reisen");
                } else {
                    // kann gehen, aber nicht reiten:
                    if (u.hat(Elefant.class) || u.hat(Mastodon.class)) {
                        Set<String> schnecken = new HashSet<String>();
                        if (u.hat(Elefant.class)) schnecken.add("Elefanten");
                        if (u.hat(Mastodon.class)) schnecken.add("Mastodonten");
                        sb.append(verb + " mit " + StringUtils.aufzaehlung(schnecken) + " nur zu Fuß reisen " + freiText);
                    } else {
                        sb.append(verb + " zu Fuß reisen " + freiText + ", zum Reiten mangelt es an Talent");
                    }
                }
            }
        } else {
            // kann nicht gehen (unabhängig vom Gepäck)
            if (u.hatTiere()) {
                // kann nicht gehen und hat Tiere:
                if (u.hatGenugZugtiere()) {
                    // hat genug Zugtiere, kann aber nicht gehen:
//                    sb.append(verb + " nicht reisen, es mangelt an Talent um die Tiere zu führen");
                    sb.append(verb + " nicht reisen, er ist überladen");
                } else {
                    sb.append(zugtiereFehlenText);
                }
            } else {
                // hat keine Tiere, kann nicht gehen:
                if (!u.hatGenugZugtiere()) {
                    sb.append(zugtiereFehlenText);
                } else {
                    // ist ganz einfach überladen:
                    sb.append(verb + " reisen " + freiText);
                }
            }
        }

        String retval = null;
        if (u.getPersonen() == 1) {
            retval = u.getName() + " " + sb;
        } else {
            retval = "Sie " + sb;
        }

        return retval;
    }

	private final class StatLine {
		final static String LabelFmt = "%-22s";
		final static String LeftFmt = "%14s";
		final static String RightFmt = "%14s";
		final static String DeltaFmt = "%14s";

		final String label;
		final String left;
		final String right;
		final String delta;

		/**
		 * Der Konstruktor für eine Zeile OHNE Vorher-Nachher-Vergleich
		 * @param right
		 * @param delta
		 */
		@SuppressWarnings("unused")
		public StatLine(String label, String left) {
			this.label = label;
			this.left = left;

			this.right = null;
			this.delta = null;

			writer.wl(this.toString());
		}

		/**
		 * Der Konstruktor für eine Zeile MIT Vorher-Nachher-Vergleich
		 * @param label
		 * @param left
		 * @param right
		 * @param delta
		 */
		public StatLine(String label, String left, String right, String delta) {
			this.label = label;
			this.left = left;
			this.right = right;
			if (delta.length() > 0) delta = (delta.substring(0,1).equals("-") ? delta : "+" + delta);
			this.delta = delta;

			writer.wl(this.toString());
		}

		@Override
		public String toString() {
			return
					String.format(LabelFmt, label)
					+ String.format(LeftFmt, left)
					+ (right != null ? String.format(RightFmt, right) : "")
					+ (delta != null ? String.format(DeltaFmt, delta) : "");
		}
	}
	
}
