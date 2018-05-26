package de.x8bit.Fantasya.Host.Reports;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Allianz;
import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Steuer;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Buildings.Leuchtturm;
import de.x8bit.Fantasya.Atlantis.Helper.Kampfzauber;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Flugdrache;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Atlantis.Regions.Lavastrom;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sandstrom;
import de.x8bit.Fantasya.Atlantis.Regions.Vulkan;
import de.x8bit.Fantasya.Atlantis.Regions.aktiverVulkan;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Host.BuildInformation;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.Environment;
import de.x8bit.Fantasya.Host.EVA.Reporte;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.Reports.Writer.NRWriter;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.StringUtils;

/**
 * einfache Textausgabe
 * @author  mogel
 */
public class ReportNR
{

	private NRWriter writer;
	
	/**
	 * diese Partei bekommt den NR
	 */
	private Partei partei;

	/**
	 * "fauler" Konstruktor NUR für abgeleitete Klassen.
	 */
	protected ReportNR(Partei partei, boolean dummy) { }

	public ReportNR(Partei partei)
	{
		this.partei = partei;
		writer = new NRWriter(this.partei);
		
		NRHeader();
		AndereVoelker();
		Allianzen();
		Meldungen();
		Steuern();
		Regionen();
		
		writer.wl("");
		writer.wl("");
		writer.wl("");
		
		writer.wl("Build-ID: " + BuildInformation.getBuild());
		
		writer.CloseFile();
	}
	
	private void WriteTheLine()
	{
		writer.wl("");
		writer.wl("-----------------------------------------------------------------------");
		writer.wl("");
	}
	
	/** das wichtigste von allen */
	private void NRHeader()	{
		int einkommen = 0;
		int vermoegen = 0;
		int headcount = 0;
		Map<PersonenHaufen, PersonenHaufen> personen = new HashMap<PersonenHaufen, PersonenHaufen>();
		
		for (Unit u : Unit.CACHE.getAll(partei.getNummer())) {
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
		}
		List<PersonenHaufen> gruppen = new ArrayList<PersonenHaufen>();
		for (PersonenHaufen gruppe : personen.keySet()) gruppen.add(gruppe);
		Collections.sort(gruppen);
		Collections.reverse(gruppen);
		String personenBeschreibung = StringUtils.aufzaehlung(gruppen);

		int vermoegenProKopf = Math.round((float)vermoegen / (float)headcount);
		int einkommenProKopf = Math.round((float)einkommen / (float)headcount);

		NumberFormat f = NumberFormat.getIntegerInstance(Locale.GERMANY);
		f.setGroupingUsed(true);

		writer.wl("");
		writer.wl("");
		writer.wl("");
		writer.center("Fantasya Auswertung");
		writer.center("~~~~~~~~~~~~~~~~~~~~~~~~~");
		writer.wl("");
		writer.center(GameDate() + " (Runde " + GameRules.getRunde() + ")");
		writer.wl("");
		writer.wl("");
		writer.center("der Name [und die Nummer] Deines Volkes lautet");
		writer.center("'" + partei + "'");
		writer.wl("");
		writer.wl("");
		writer.wl("Dein Volk zählt " + personenBeschreibung + ".");
		writer.wl("");
		String msg = "Das Vermögen (Einkommen) deines Volkes liegt bei " + f.format(vermoegen)
				+ " (" + f.format(einkommen) + ") Silber, also ca. "
				+ f.format(vermoegenProKopf) + " (" + f.format(einkommenProKopf) + ") pro Kopf.";
		if (GameRules.isSkirmish()) {
			msg += " Du hast bisher " + partei.getIntegerProperty("punkte.krieg", 0) + " durch Kämpfe erhalten. Wäre jetzt "
				+ "das Spiel zu Ende, dann hättest Du " + partei.getIntegerProperty("punkte.ende", 0) + " Punkte zusätzlich und "
				+ "hättest insgesammt " + (partei.getIntegerProperty("punkte.krieg", 0) + partei.getIntegerProperty("punkte.ende", 0)) + " Punkte.";
		}
		writer.wl(msg);
		writer.wl("");
	}
	
	/** andere Völker auflisten */
	private void AndereVoelker() {
		boolean header = false;
		
		for (Partei other : partei.getBekannteParteien()) {
			if (other.isMonster()) continue;
			
			if (!header) {
				WriteTheLine(); writer.center("Alle bekannten Völker"); writer.wl("");
				header = true;
			}
			
			String msg = other.getName() + " [" + other.getNummerBase36() + "], " + other.getEMail();
			writer.wl(msg);
		}
	}
	
	/**
	 * berechnet das Datum für diese Runde ... dabei werden aber nicht
	 * die verschiebungen der Jahreszeiten beachtet ... d.h. es kann auch
	 * für das Wachstum Winter sein ... obwohl lt. Runde Sommer wäre !! 
	 */
	protected String GameDate()
	{
		String monat = "";
		
		switch(GameRules.getRunde() % 12) {
			case 0:	monat = "gefrierendes Wasser";		break;
			case 1:	monat = "kalter Mond";				break;
			
			case 2:	monat = "klare Nächte";				break;
			case 3: monat = "zartes Grün";				break;
			case 4: monat = "fünfter Mond";				break;
			
			case 5:	monat = "lange Nächte";				break;
			case 6: monat = "lange Tage";				break;
			case 7:	monat = "heißer Schatten";			break;
			
			case 8:	monat = "reiche Ernte";				break;
			case 9: monat = "roter Abendhimmel";		break;
			case 10: monat = "fallendes Laub";			break;
			
			case 11: monat = "erster Schnee";			break;
		}
        
        String jz = "";
        if (GameRules.isSpring()) jz = "Frühling";
        if (GameRules.isSummer()) jz = "Sommer";
        if (GameRules.isAutumn()) jz = "Herbst";
        if (GameRules.isWinter()) jz = "Winter";
        
        if (jz.length() > 0) {
            jz = " Es ist " + jz + ".";
        } else {
            jz = "";
        }
		
		return "für " + monat + " des Jahres " + GameRules.getJahr() + " in der 2. Zeitrechnung." + jz;
	}
	
	/**
	 * alle Meldungen für den Spieler
	 */
	private void Meldungen()
	{

		boolean header = false;
		
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
		for(Class<? extends Message> clazz : Message.AlleArten()) {
            String kat = clazz.getSimpleName();

			List<Message> messages = sortiert.get(kat);
			if (messages.isEmpty()) continue;

			if (!header) { WriteTheLine(); header = true;  }

			// Header der Meldung
			writer.wl("");
			writer.w_raw("          - " + kat + " -");
			writer.wl("");
			
			// Kategorie nie ins Log-File ausgeben ... darauf läßt sich der Rückschluss
			// Volk <-> Echse bilden !!
			// new SysMsg(" - - Kategorie '" + kat + "'");
			
			for (Message msg : messages) {
                String text = msg.getText();
                Unit u = msg.getUnit();
                if (u != null) {
                    if (!text.contains("[" + u.getNummerBase36() + "]")) {
                        text = u + ": " + text;
                    }
                }
				writer.wl(text);
			}
			
			// Footer der Meldungen
			writer.wl("");
		}
	}
	
	/** Allianzen auflisten */
	private void Allianzen()
	{
		boolean header = false;
		
		for (int partnerNr : partei.getAllianzen().keySet()) {
			Allianz a = partei.getAllianz(partnerNr);
			Partei partner = Partei.getPartei(partnerNr);
			if (partner == null) {
				new SysErr("ReportNR: Allianz mit nicht-existenter Partei - " + partei.getNummerBase36() + " mit " + Codierung.toBase36(partnerNr));
				continue;
			}

			if (!a.isValid()) continue; // überspringt z.B. "leere" Allianzen, also solche ohne aktive Option.
            
			if (!header) { WriteTheLine(); writer.center("Allianzen & Partner"); header = true; }
			String msg = partner + " -";
			for(AllianzOption ao: AllianzOption.values()) {
				if (ao.equals(AllianzOption.Alles)) continue;
				if (partei.hatAllianz(a.getPartner(), ao)) msg += " " + ao.name();
			}
			writer.wl(msg);
		}
	}
	
	/** Steuern auflisten */
	private void Steuern()
	{
		WriteTheLine();
		writer.center("Handels-Steuern");
		writer.wl("");
		
		boolean cont = false;
		String msg = "Die Steuer für alle Völker liegt bei " + partei.getDefaultsteuer() + "%";
		for(Steuer steuer : partei.getSteuern())
		{
			Partei p = Partei.getPartei(steuer.getFaction());
			if (cont) msg += ", "; else { cont = true; msg += ", Sondervergünstigungen für: "; }
			msg += p + " (zahlt " + steuer.getRate() + "%)";
		}
		writer.wl(msg + ".");
	}
	
	/** alle Regionen ausgeben */
	private void Regionen()
	{
		for(Region r : writer.Regionen()) {
			if (!partei.canAccess(r)) continue;
			
			WriteTheLine();
			Region_Header(r);

			RegionsSicht rs = partei.getRegionsSicht(r.getCoords());
			boolean details = false;
			if (rs != null) details = rs.hasDetails();
			if (partei.getNummer() == 0) details = true;
			
			if (rs.getQuelle() == Leuchtturm.class) {
				writer.wl("Den Bericht von hier liefert euch ein Leuchtturm."); writer.wl("");
			}
			
			if (details) {
				Region_Strassen(r);
				Region_Handel(r);
				Region_Statistik(r);
				Region_Einheiten(r);
			}
		}
	}
	
	private void Region_Header(Region r)
	{
		Coords privateCoords = partei.getPrivateCoords(r.getCoords());
		Item resource = null;
		
		String msg = "";

		boolean hidden = !partei.canAccess(r);
		
		if (!hidden) {
			if (r instanceof Ozean) {
				msg = r.getClass().getSimpleName() +  " (" + privateCoords.getX() + ", " + privateCoords.getY() + "), " ;
			} else {
				msg = r.getName() + " (" + privateCoords.getX() + ", " + privateCoords.getY() + "), " + r.getClass().getSimpleName();
				msg += ", " + r.getResource(Holz.class).getAnzahl() + " Bäume, " + r.getBauern() + " Bauern, $" + r.getSilber() + " Silber. ";
			}
		} else {
			msg = r.getName() + " (" + privateCoords.getX() + ", " + privateCoords.getY() + "), " + GameRules.TERRAIN_UNSICHTBARER_REGIONEN;
		}

		if (!hidden) {
			clearListe();
			resource = r.getResource(Pferd.class); if (resource.getAnzahl() > 0) addContent(resource);
			resource = r.getResource(Kamel.class); if (resource.getAnzahl() > 0) addContent(resource);
			resource = r.getResource(Elefant.class); if (resource.getAnzahl() > 0) addContent(resource);
			resource = r.getResource(Flugdrache.class); if (resource.getAnzahl() > 0) addContent(resource);
			if (!liste.isEmpty()) {
				msg += "Hier können " + printListe() + " gefangen werden. ";
			}
		
			clearListe();
			resource = r.getResource(Eisen.class); if (resource.getAnzahl() > 0) { addContent(resource); }
			resource = r.getResource(Stein.class); if (resource.getAnzahl() > 0) { addContent(resource); }
			if (!liste.isEmpty()) {
				msg += "Es können " + printListe() + " abgebaut werden. ";
			}
		}
		
		// Nachbarregionen: Land
		List<String> parts = new ArrayList<String>();
		for(Richtung ri : Richtung.values()) {
			Region hr = Region.Load(r.getCoords().shift(ri));
			if ((hr == null) || (!partei.canAccess(hr))) {
				parts.add("im " + ri.name() + " liegt " + GameRules.TERRAIN_UNSICHTBARER_REGIONEN);
			} else {
				if (hr instanceof Ozean) {
					// nicht getestet wie das im NR aussieht
					// parts.add("Sturmwahrscheinlichkeit ist " + ((Ozean)hr).getSturmValue());
					continue;
				} else if (hr instanceof Berge) {
					parts.add("im " + ri.name() + " liegen die Berge von " + hr.getName());
				} else if (hr instanceof Vulkan) {
					parts.add("im " + ri.name() + " liegt der Vulkan " + hr.getName());
				} else if (hr instanceof aktiverVulkan) {
					parts.add("im " + ri.name() + " liegt der aktive Vulkan " + hr.getName());
				} else if (hr instanceof Sandstrom) {
					parts.add("im " + ri.name() + " liegt der Sandstrom von " + hr.getName());
				} else if (hr instanceof Lavastrom) {
					parts.add("im " + ri.name() + " fließt ein Lavastrom");
				} else if (hr instanceof Chaos) {
					parts.add("im " + ri.name() + " liegt Chaos");
				} else {
					parts.add("im " + ri.name() + " liegt " + hr.getArtikel() + (hr.getArtikel().length()>0?" ":"") + hr);
				}
			}
		}
		if (!parts.isEmpty()) msg += StringUtils.ucfirst(StringUtils.aufzaehlung(parts)) + ". ";

		// Nachbarregionen: Wasser
		parts = new ArrayList<String>();
		for(Richtung ri : Richtung.values()) {
			Region hr = Region.Load(r.getCoords().shift(ri));
			if (hr == null)	continue;
			if ((hr instanceof Ozean) && (partei.canAccess(hr))) parts.add(ri.toString());
		}
		if (!parts.isEmpty()) msg += "Im " + StringUtils.aufzaehlung(parts) + " liegt die offene See. ";
		
		if (!hidden) {
			// Beschreibung
			if (r.getBeschreibung().length() > 0) msg += r.getBeschreibung() + ". ";
		}
		
		writer.wl(msg);
		writer.wl("");

		if (hidden) return;

		
		// Bewacher?
		msg = null;
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
			msg = "Die Region wird von " + StringUtils.aufzaehlung(parteiMeldungen) + " bewacht. ";
		}
		if (msg != null) {
			writer.wl(msg);
			writer.wl("");
		}

	}
	
	private void Region_Strassen(Region r)
	{
		int count = 0;
		String msg = "";
		
		for(Richtung richtung : Richtung.values())
		{
			if (r.getStrassensteine(richtung) > 0)
			{
				Region hr = Region.Load(r.getCoords().shift(richtung));
				if (count++ != 0) msg += ", "; else msg = "Es führt eine Strasse nach ";
				msg += hr + " ";
				msg += "(" + richtung.name();
				long prozent = (long)Math.floor( ((double) r.getStrassensteine(richtung) / (double) r.getSteineFuerStrasse()) * 100.0);
				if (prozent < 100) {
					msg += ", " + r.getStrassensteine(richtung) + " Pflastersteine = " + prozent + "% fertig)";
				} else msg += ")";
			}
		}
		if (msg.length() > 0) { msg += "."; writer.wl(msg); writer.wl(""); }
	}
	
	private void Region_Handel(Region r)
	{
		if (r instanceof Ozean) return;
		if (r instanceof Lavastrom) return;
		if (r instanceof Sandstrom) return;
		
		String msg = "";
		String msg2 = "";
		
		if (r.hatGebaeude(Burg.class, 2, null))
		{
			msg = "Die Bauern kaufen hier: ";
		} else
		{
			msg = "Mit einer Burg würden hier folgende Preise gelten: ";
		}
		
		List<String> kaufen = new ArrayList<String>();
		for(Nachfrage n : r.getLuxus()) {
			String luxusName = n.getItem().getSimpleName();
			Paket p = Paket.FindItem(luxusName);
			if (n.getNachfrage() > 0) {
				kaufen.add(luxusName + " " + (int) (n.getNachfrage() * ((Item)p.Klasse).getPrice()) + "$" );
			} else {
				msg2 = "Die Bauern produzieren " + luxusName + " (" + (int) ((0 - n.getNachfrage()) * ((Item)p.Klasse).getPrice()) + "$).";
			}
		}
		
		msg += StringUtils.aufzaehlung(kaufen) + ". ";
		msg += msg2;
		writer.wl(msg);
		writer.wl("");
	}
	
	
	private void Region_Statistik(Region r)
    {
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
				msg.append("erhalten $").append(r.getLohn()).append(
					  " Lohn und können zusammen voraussichtlich $").append(vorhersageUnterhaltungSilber).append(
                      " Silber").append(" zu ihrer Unterhaltung ausgeben. ");
			}

			if (r.freieArbeitsplaetze() > 0) {
				msg.append("Es gibt Arbeit für ").append(r.freieArbeitsplaetze()).append(" neue Bauern. ");
			} else {
				int arbeiter = r.freieArbeitsplaetze() + r.getBauern();
				if (arbeiter == 0) {
                    if (r.getBauern() > 0) {
                        msg.append("Alle Bauern sind bei der Arbeit, neue Bauern werden nicht gebraucht. ");
                    }
				} else if (arbeiter > 0) {
					msg.append("Nur ").append(arbeiter).append(" Bauern finden einträgliche Arbeit. ");
				} else {
					msg.append("Kein einziger Bauer kann sich hier selbst versorgen! ");
				}
			}

			if (r.Rekruten() > 0) {
				msg.append(r.Rekruten()).append(" Bauern wollen in Euren Dienst treten. ");
			}

			if (msg.length() > 0) {
                writer.wl(msg.toString());
                writer.wl("");
            }
        }

		int personen = 0;
		int einheiten = 0;
        int einkommen = 0;
		int silber = 0;

        for(Unit u : r.getUnits()) {
            if ((u.getOwner() == partei.getNummer()) || (partei.getNummer() == 0)) {
                einheiten++;
                personen += u.getPersonen();
                einkommen += u.getEinkommen();
                silber += u.getItem(Silber.class).getAnzahl();
            }
        }
		if (personen > 0) {
            StringBuffer msg = new StringBuffer();
            String einheitenPhrase = (einheiten != 1)?(einheiten + " Einheiten"):("einer Einheit");

            if (personen > 1) {
                msg.append(personen + " Personen in ").append(einheitenPhrase);
                if (einkommen > 0) msg.append(" haben $" + einkommen + " verdient");
                msg.append(", gesamt $" + silber + " Silber.");
            } else {
                msg.append(personen + " Person in ").append(einheitenPhrase);
                if (einkommen > 0) msg.append(" hat $" + einkommen + " verdient");
                msg.append(", gesamt $" + silber + " Silber.");
            }
			writer.wl(msg.toString());
			writer.wl("");
		}
	}

	
	private void Region_Einheiten(Region r)
	{
		writer.NRFront = 3;
		
		// Gebäude
		for(Building b : r.getBuildings()) {
			Region_Building(b);
			writer.NRFront = 5;
			for(Unit u : b.getUnits()) {
				Region_Einheit(u);
			}
			writer.NRFront = 3;
		}
		
		// Schiffe
		for(Ship ship : r.getShips()) {
			Region_Ship(ship);
			writer.NRFront = 5;
			for(Unit u : ship.getUnits()) {
				if (partei.cansee(u)) Region_Einheit(u);
			}
			writer.NRFront = 3;
		}
		
		// übrigen Einheiten
		for(Unit u : r.getUnits()) {
			if ((u.getGebaeude() == 0) && (u.getSchiff() == 0)) Region_Einheit(u);
		}
		
		writer.NRFront = 1;
	}
	
	private void Region_Building(Building b)
	{
		Unit u = Unit.Load(b.getOwner());
		
		String msg = b.getName() + " [" + b.getNummerBase36() + "], Größe " + b.getSize() + ", ";// + b.getTyp();
		if (b.getClass().equals(Burg.class)) msg += ((Burg)b).getBurgTyp(); else msg += b.getTyp();		
		if (u == null)
		{
			msg += ", das Gebäude hat keinen Besitzer";
		} else
		{
			msg += ", Besitzer ist " + u;
		}
		
		if (b.getBeschreibung().length() > 0) msg += ", " + b.getBeschreibung();
		if (b.istBelagert() != null) msg += ", wird belagert";
		
		msg += ".";
		writer.wl(msg);
		writer.wl("");
	}

	private void Region_Ship(Ship s) {
		Unit u = Unit.Load(s.getOwner());
		
		String msg = s.getName() + " [" + s.getNummerBase36() + "], ";
		long prozent = Math.round( ((double) s.getGroesse() / (double) s.getConstructionSize()) * 100.0);
		msg += (s.istFertig() ? "fertig [" : "") + (prozent + "%") + (s.istFertig() ? "]" : "");
		msg += ", " + s.getTyp();
		if (u == null) {
			msg += ", das Schiff hat keinen Besitzer";
		} else if (!partei.cansee(u)) {
			msg += ", wir können den Besitzer nicht erkennen";
		} else {
			msg += ", Besitzer ist " + u;
			if (s.istFertig()) {
				if (u.getOwner() == partei.getNummer()) msg += ", Kapazität: " + (s.getKapazitaetFree() / 100) + " GE frei";
			}
		}
		
		if (s.getKueste() != null) msg += ", das Schiff ankert an der Küste im " + s.getKueste().name();
		
		if (s.getBeschreibung().length() > 0) msg += ", " + s.getBeschreibung();
		
		msg += ".";
		writer.wl(msg);
		writer.wl("");
	}

	/**
	 * erstellt aus dem Prefix und der Rasse entsprechend die eigentliche Rasse
	 * @param prefix
	 * @param rasseSg
	 * @return
	 */
	private String preparePrefixRace(String prefix, String rasse) {
		if (prefix.length() != 0) {
			if (prefix.endsWith("-")) return prefix + rasse;
			return prefix + rasse.toLowerCase();
		}
		return rasse;
	}
	
	private void Region_Einheit(Unit u)
	{
		Partei p = null;
		if (u.getTarnPartei() != 0) p = Partei.getPartei(u.getTarnPartei());
		
		// Sichtbarkeit von Einheiten mit Tarnung prüfen
		if ((u.getSchiff() == 0) && (u.getGebaeude() == 0)) {
			if (!partei.cansee(u)) return;
		}
		
		String msg = u + ", ";
		if (u.getOwner() == partei.getNummer())	{
			// eigene Einheit
			msg = "* " + msg;

			if (u.getSichtbarkeit() == 1) msg += "versteckt sich, ";

			if (p == null) {
				msg += "Partei ist getarnt, ";
			} else {
				if (u.getTarnPartei() != partei.getNummer()) msg += "Einheit ist getarnt als ";
				msg += p + ", ";
			}
		} else {
			if (p == null)
			{
				msg = "- " + msg + "Partei ist getarnt, ";
			} else {
				if (partei.hatAllianz(p.getNummer())) msg = "+ " + msg; else msg = "- " + msg;
				msg += p + ", ";
			}
		}
		
		if (u.getBeschreibung().length() > 0) msg += u.getBeschreibung() + ", ";
		
		// Personen mit Rasse
		if (Building.BewohnerCache().contains(u)) {
			if (u.getOwner() != partei.getNummer())	{
				Building b = Building.getBuilding(u.getGebaeude());
				Unit owner = Unit.Load(b.getOwner());
				if (owner == null) {
					msg += "1 ";				
				} else {
					if (owner.getOwner() == partei.getNummer())	{
						// der Eigentümer der Burg erhält den Report ... daher die Personen
						// der Einheit anzeigen ... ist ja seine Burg
						msg += u.getPersonen() + " ";				
					} else {
						msg += "1 ";
					}
				}
			} else {
				msg += u.getPersonen() + " ";
			}
		} else {
			msg += u.getPersonen() + " ";
		}
		if (u.getTarnRasse().length() > 0) {
			msg += preparePrefixRace(u.getPrefix(), u.getTarnRasse());
			if (u.getOwner() == partei.getNummer()) msg += " (" + u.getClass().getSimpleName() + "), "; else msg += ", ";
		} else {
			msg += preparePrefixRace(u.getPrefix(), u.getRasse()) + ", ";
		}
		// Partei-spezifisches
		if (u.getOwner() == partei.getNummer())
		{
			// Hunger
			msg += u.strLebenspunkte() + ", ";
			
			// Kampf
			msg += "kämpft " + u.getKampfposition().name() + ", ";
			// Talente
			msg += "Talente: ";
			clearListe();
            int summe = 0;
			for(Skill skill : u.getSkills())	{
				if (skill.getLerntage() == 0) continue;
                summe += skill.getLerntage() / u.getPersonen();
				addContent(skill.getName() + " " + u.Talentwert(skill.getClass()) + " [" + (skill.getLerntage() / u.getPersonen()) + "]");
			}
			msg += printListe();
			if (summe == 0) msg += "Dumm wie Stroh";

            msg += ". ";

			// magische Werte
			if (u.getSkill(Magie.class).getLerntage() > 0)
			{
				msg += u.getAura() + " Aura";
				if (u.getMana() > 0)
				{
					msg += " und ";
					msg += u.getMana() + " Mana"; 
				}
				msg += ". ";
			}
			
			// Zaubersprüche
			if (u.getSpells().size() > 0)
			{
				clearListe();
				msg += "Zaubersprüche: ";
				for(Spell spell : u.getSpells()) {
					addContent("\"" + spell.getName() + "\" (Stufe " + spell.getStufe() + ")" );
				}
				msg += printListe() + ". ";
				
				// Kampfzauber
				String as = u.getStringProperty(Kampfzauber.ATTACKSPELL, "");
				String ds = u.getStringProperty(Kampfzauber.DEFENCESPELL, "");
				String cs = u.getStringProperty(Kampfzauber.CONFUSIONSPELL, "");
				msg += "Verwirrung: '" + (cs.length() > 0 ? cs : "keiner") + "', "; 
				msg += "Angriff: '" + (as.length() > 0 ? as : "keiner") + "', "; 
				msg += "Verteidigung: '" + (ds.length() > 0 ? ds : "keiner") + "'. "; 
			}
			
            msg += "Kapazität: ";
            if (u.canRideAnimals()) {
                // Tragfähgikeit (false = zu Fuß)
                if (u.gesamteFreieKapazitaet(false) < 0) {
                    msg += "zu Fuß überladen";
                } else {
                    msg += "zu Fuß " + (u.gesamteFreieKapazitaet(false) / 100 + " frei");
                }
                msg += ", beritten ";
                // true = beritten
                if (u.gesamteFreieKapazitaet(true) < 0) {
                    msg += "überladen";
                } else {
                    msg += (u.gesamteFreieKapazitaet(true) / 100 + " frei");
                }
            } else {
                // kann nicht reiten
                // Tragfähgikeit (false = zu Fuß)
                if (u.gesamteFreieKapazitaet(false) < 0) {
                    msg += "überladen";
                } else {
                    msg += (u.gesamteFreieKapazitaet(false) / 100 + " frei");
                    if (!u.canWalkAnimals()) {
                        msg += " (kann aber mit den Tieren nicht reisen)";
                    }
                }
            }
			msg += ", ";
		}
		
		// Items
		if (partei.cansee(u)) {
			// wenigstens die Ausrüstung kann man mit Tarnung verbergen, auch wenn
			// eine Einheit auf einem Schiff oder in einem Gebäude ist.

			// new Debug(partei + " can see " + u + " von [" + Codierung.toBase36(u.getOwner()) + "]");
			clearListe();
			for(Item item : u.getItems()) {
				if (item.getAnzahl() == 0) continue;
				if (item.getClass().equals(Silber.class)) {
					if (u.getOwner() != partei.getNummer())	{
						addContent("einen Silberbeutel");
					} else {
						addContent(item);
					}
				} else {
					if (u.imGebaeude() && u.getOwner() != partei.getNummer() && (partei.getNummer() != 0)) {
						int alt = item.getAnzahl();
						item.setAnzahl(1);
						addContent("1 " + item.getName());
						item.setAnzahl(alt);
					} else {
						addContent(item);
					}
				}
			}

			if (liste.isEmpty()) {
				msg += "hat nichts";
			} else {
				msg += "hat " + StringUtils.aufzaehlung(liste);
			}

			if ((u.getOwner() == partei.getNummer()) || (partei.getNummer() == 0)) {
				msg += "," + (liste.isEmpty()?" ":" mit Sack und Pack ") + ((int)Math.ceil(u.getGewicht() / 100f) + " GE.");
			} else {
				msg += ".";
			}
		}

		
		if (u.getBelagert() > 0) {
			Building b = Building.getBuilding(u.getBelagert());
			if (b != null) {
				msg += " Belagert " + b + ". ";
			} else {
				new SysErr("Einheit " + u + " wollte Gebäude " + Codierung.toBase36(u.getBelagert()) + " belagern; das gibt es aber nicht (mehr).");
				u.setBelagert(0);
			}
		}
		
		// Befehle
		if ((u.getOwner() == partei.getNummer()) || (partei.getNummer() == 0)) {
			msg += " Befehle: ";
			List<String> befehlsTexte = new ArrayList<String>();
			for (Einzelbefehl eb : u.BefehleExperimental) befehlsTexte.add("\"" + eb.getBefehlCanonical() + "\"");
			if (befehlsTexte.isEmpty()) {
				msg += "Keine.";
			} else {
				msg += StringUtils.aufzaehlung(befehlsTexte);
			}
		}

		writer.wl(msg);
		writer.wl("");
	}
	
	/** eine Liste */
	private ArrayList<String> liste;

	/** löscht die letzte Liste */
	private void clearListe() { liste = new ArrayList<String>(); }

	/**
	 * fügt etwas der Liste hinzu
	 * @param content - was auch immer der Content sein möge
	 */
	private void addContent(String content) { liste.add(content); }
	
	/**
	 * fügt etwas der Liste hinzu
	 * @param atlantis - ein Atlantis-Objekt
	 * @see addContent(String content)
	 */
	private void addContent(Atlantis atlantis) { addContent(atlantis.toString()); }
	
	/**
	 * schreibt die Liste in der aktuellen Report
	 * @return die Liste
	 */
	private String printListe()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < liste.size(); i++)
		{
			// Komma oder "und"
			if (i > 0)
			{
				if (i < liste.size() - 1)
				{
					sb.append(", ");
				} else
				{
					sb.append(" und ");
				}
			}
			sb.append(liste.get(i));
		}
		
		return sb.toString();
	}
	

	protected class PersonenHaufen implements Comparable<PersonenHaufen> {
		final NumberFormat nf;

		int anzahl = 0;
		final String prefix;
		final String rasseSg;
		final String rassePl;

		public PersonenHaufen(String prefix, String rasseSg, String rassePl) {
			this.prefix = prefix;
			this.rasseSg = rasseSg;
			this.rassePl = rassePl;

			nf = NumberFormat.getIntegerInstance(Locale.GERMANY);
			nf.setGroupingUsed(true);
		}

		public int getAnzahl() {
			return anzahl;
		}

		/**
		 * @param anzahl
		 */
		public void addAnzahl(int anzahl) {
			setAnzahl(getAnzahl() + anzahl);
		}

		public void setAnzahl(int anzahl) {
			this.anzahl = anzahl;
		}

		public String getRassePl() {
			return rassePl;
		}

		public String getRasseSg() {
			return rasseSg;
		}

		public String getPrefix() {
			return prefix;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PersonenHaufen)) return false;

			PersonenHaufen other = (PersonenHaufen)obj;
			return (other.getPrefix().equals(this.getPrefix()) && other.getRasseSg().equals(this.getRasseSg()));
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 67 * hash + this.anzahl;
			hash = 67 * hash + (this.prefix != null ? this.prefix.hashCode() : 0);
			hash = 67 * hash + (this.rasseSg != null ? this.rasseSg.hashCode() : 0);
			return hash;
		}



		@Override
		public int compareTo(PersonenHaufen o) {
			if (this.getAnzahl() < o.getAnzahl()) return -1;
			if (this.getAnzahl() > o.getAnzahl()) return +1;
			return 0;
		}

		@Override
		public String toString() {
			if (getAnzahl() == 1) return getAnzahl() + " " + preparePrefixRace(this.getPrefix(), this.getRasseSg());
			return nf.format(getAnzahl()) + " " + preparePrefixRace(this.getPrefix(), this.getRassePl());
		}
	}

}
