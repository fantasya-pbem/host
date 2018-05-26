package de.x8bit.Fantasya.Host.Reports;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Allianz;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung.Insel;
import de.x8bit.Fantasya.Host.BuildInformation;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.Reports.Writer.CRWriter;
import de.x8bit.Fantasya.Host.Reports.util.CoordComparatorLNR;
import de.x8bit.Fantasya.util.StringUtils;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * die normale Ausgabe für Magellan ... Basis ist derzeit (10.06.2008) http://dose.0wnz.at/thewhitewolf/cr-format
 * @author  mogel
 */
public class ReportCR
{
	private static long date = new Date().getTime(); // Zeitpunkt für alle Spieler gleich
	
	/**
	 * @uml.property  name="writer"
	 * @uml.associationEnd  
	 */
	private CRWriter writer;
	/**
	 * @uml.property  name="partei"
	 * @uml.associationEnd  
	 */
	private Partei partei;
	
	/**
	 * erzeugen eines neuen CR's für Magellan
	 * @param partei - CR für diese Partei
	 */
	public ReportCR(Partei partei)
	{
		this.partei = partei;
		writer = new CRWriter(partei);
		
		// der Report
		CRHeader();
		Allianzen();
		Meldungen();
		AndereVoelker();
        SaveZauber(partei);
		
		SaveInseln();
        SaveRegionen();

		
		// Meldungstypen ausgeben
		writer.wl("MESSAGETYPE 3000");
		writer.wl("\"$rendered\"","text");
		writer.wl("Meldungen", "section");
		
		writer.wl("MESSAGETYPE 4000");
		writer.wl("\"$rendered\"","text");
		writer.wl("events", "section");
		
		// File schließen
		writer.CloseFile();
	}
	
	/**
	 * erzeugt den Standart-Header für die CR-Dateien
	 */
	private void CRHeader()
	{
		new SysMsg(3, " - - Header");
		
		// Basis-Header
		writer.wl("VERSION 64");
		writer.wl("utf-8", "charset");
		writer.wl("de", "locale");
		writer.wl("fantasya", "Spiel");
		writer.wl(0, "noskillpoints");					// auch wenn der Wert als Default gilt
		writer.wl(date, "date");
		writer.wl("Standart", "Konfiguration");
		writer.wl(BuildInformation.getBuild(), "buildid");
		writer.wl("Hex", "Koordinaten");
		writer.wl(36, "Basis");
		writer.wl(GameRules.getRunde(), "Runde");
		writer.wl(2, "Zeitalter");
		writer.wl("befehle@fantasya-pbem.de", "mailto");
		// writer.wl(Main.GameID + "@fantasya-pbem.de", "mailto");
		writer.wl("Fantasya Befehle", "mailcmd");
		
		// Partei-Header ... diese Partei erhält den Report
		writer.wl("PARTEI " + partei.getNummer());
		writer.wl("de", "locale");
		writer.wl(GameRules.getRunde() - partei.getAlter(), "age");
		writer.wl(31 + 512 + 4096, "Optionen");
			//AUSWERTUNG   *    1
			//COMPUTER     *    2
			//ZUGVORLAGE   *    4
			//SILBERPOOL   *    8
			//STATISTIK    *   16
			//DEBUG        *   32
			//ZIPPED       *   64
			//ZEITUNG      *  128
			//MATERIALPOOL *  256
			//ADRESSEN     *  512
			//BZIP2        * 1024
			//PUNKTE       * 2048
			//SHOWSKCHANGE * 4096		
		writer.wl(partei.getRasse(), "Typ");
		writer.wl( ((Unit)Paket.FindUnit(partei.getRasse()).Klasse).getRekrutierungsKosten(), "Rekrutierungskosten");
		int cnt = 0;
		if (partei.getNummer() != 0) {
			for (Unit u : Unit.CACHE.getAll(partei.getNummer())) {
				cnt += u.getPersonen();
			}
		} else {
			for (Unit u : Unit.CACHE) {
				cnt += u.getPersonen();
			}
		}
		writer.wl(cnt, "Anzahl Personen");
		writer.wl("Fantasya", "Magiegebiet");
		writer.wl(partei.getName(), "Parteiname");
		writer.wl(partei.getEMail(), "email");
		if (partei.getBeschreibung().length() > 0) writer.wl(partei.getBeschreibung(), "Banner");
	}
	
	private void Allianzen()
	{
		for (int partnerNr : partei.getAllianzen().keySet()) {
			Allianz a = partei.getAllianz(partnerNr);
			Partei partner = Partei.getPartei(a.getPartner());
			if (partner == null) continue; // hier hat es den Partner ins Jenseits getragen...
			
			writer.wl("ALLIANZ " + partner.getNummer());
			writer.wl(partner.getName(), "Parteiname");
			writer.wl(a.getOptionen(), "Status");
		}
	}
	
	/**
	 * listet die anderen, bekannten Völker auf
	 */
	private void AndereVoelker()
	{
        for (Partei other : partei.getBekannteParteien()) {
			if (other.getNummer() == partei.getNummer()) continue;
			
            writer.wl("PARTEI " + other.getNummer());
            writer.wl(other.getName(), "Parteiname");
            writer.wl(other.getEMail(), "email");
			writer.wl("0;Optionen");
        }
        writer.wl("PARTEI 0");
        writer.wl("Parteigetarnt", "Parteiname");
        writer.wl("foo@example.com", "email");
        writer.wl("0;Optionen");
	}
	
	/**
	 * alle Meldungen für diese Partei
	 */
	private void Meldungen()
	{
		int meldung = 0;
		
		new SysMsg(3, " - - Meldungen");
		
        List<Message> messages = new ArrayList<Message>();
		if (partei.getNummer() == 0) {
			messages = Message.Retrieve(null, (Coords)null, null);
		} else {
            // Regionsmeldungen (ohne Parteiangabe):
            Set<Coords> beobachtete = new HashSet<Coords>();
            for (RegionsSicht rs : partei.getKnownRegions(false)) {
                if (rs.hasDetails()) beobachtete.add(rs.getCoords());
            }
            for (Message regionsMsg : Message.Retrieve(null, (Coords)null, null)) {
                if (regionsMsg.getCoords() == null) continue;
                if (regionsMsg.getPartei() != null) continue;
                if (regionsMsg.getUnit() != null) continue;
                if (!beobachtete.contains(regionsMsg.getCoords())) continue;
                
                // gotcha:
                messages.add(regionsMsg);
            }
            
            for (Message m : messages) {
                writer.wl("MESSAGE " + ++meldung);
                writer.wl(cleanMessageText(m.getText()), "rendered");
                writer.wl(4000, "type");
                Coords c = partei.getPrivateCoords(m.getCoords());
                writer.wl(c.getX() + " " + c.getY() + " " + c.getWelt(), "region");
            }
            
            
            messages.clear();
			messages.addAll(Message.Retrieve(partei, (Coords)null, null));
            
			// Mantis #326:
			for (Message botschaft : Message.Retrieve(null, (Coords)null, null, "Botschaft")) {
				if (botschaft.getPartei() != null) continue; // entweder sind dann nicht wir gemeint, oder die Botschaft wurde schon mit den normalen Meldungen verarbeitet.
				if (botschaft.getCoords() == null) { messages.add(botschaft); continue; }
				if (Region.Load(botschaft.getCoords()).anwesendeParteien().contains(partei)) {
					messages.add(botschaft);
				}
			}
		}
		for (Message m : messages) {
            writer.wl("MESSAGE " + ++meldung);
            writer.wl(cleanMessageText(m.getText()), "rendered");
            writer.wl(3000, "type");
            if (m.getCoords() != null) {
                Coords c = partei.getPrivateCoords(m.getCoords());
                writer.wl(c.getX() + " " + c.getY() + " " + c.getWelt(), "region");
            }
            if (m.getUnit() != null) {
                writer.wl(m.getUnit().getNummer(), "unit");
            }
        }
		
		meldung++;
	}

	/**
	 * <p>Ersetzt alle \n-Zeichen in orig durch das Unicode-Zeichen 0x2028 (Format
	 * Characters: LINE SEPARATOR). Dieses Zeichen ist für so einen Fall 
	 * vorgesehen; es repräsentiert einen Zeilenumbruch als Text-Formatierung, 
	 * wenn das tatsächliche "Steuerzeichen" Zeilenumbruch nicht anwendbar, weil
	 * extern semantisch belegt ist.</p>
	 * <p>Freilich: Das nützt uns nur theoretisch etwas, denn Magellan kümmert sich
	 * nicht weiter um dieses Bedeutung.</p>
	 * <p>(Siehe Mantis #295)</p>
	 * @param orig
	 * @return
	 */
	private String cleanMessageText(String orig) {
		return orig.replaceAll("\\r", "").replaceAll("\\n", "\u2028");
	}
	
    /**
     * schreibt alle (sichtbaren) Inseln
     */
    private void SaveInseln() {
        InselVerwaltung iv = InselVerwaltung.getInstance();
        InselVerwaltung.ParteiReportDaten prd = iv.getParteiReportDaten(partei);
        for (Insel i : prd.getBekannteInseln()) {
            int privateId = i.getPrivateNummer(partei);
            writer.wl("ISLAND " + privateId);
            String name = i.getName(partei);
            if (name != null) {
                writer.wl(name, "name");
            } else {
                writer.wl("Insel bei " + partei.getPrivateCoords(i.getMittelpunkt()), "name");
            }
            
            // Beschreibung:
            if (partei.getNummer() != 0) {
                String beschr = i.getBeschreibung(partei);
                if (beschr != null) writer.wl(i.getBeschreibung(partei), "beschr");
            } else {
                StringBuilder beschreibung = new StringBuilder();
                
                beschreibung.append("Globale Insel-ID: " + i.getPublicId() + ". ");
                
                // Inselkennungen (diese werden beim Terraforming erzeugt):
                beschreibung.append("Inselkennungen: " + StringUtils.aufzaehlung(i.getInselKennungen()) + ". ");
                
                // Partei-Einflüsse in die Beschreibung:
                NumberFormat P = NumberFormat.getPercentInstance(Locale.GERMANY);
                P.setMaximumFractionDigits(2);
                P.setMinimumFractionDigits(2);
                
                SortedSet<InselVerwaltung.ParteiEinfluss> einfluesse = i.getParteiEinfluesse();
                float summeEinfluss = 0f;
                for (InselVerwaltung.ParteiEinfluss pe : einfluesse) summeEinfluss += pe.getEinfluss();
                List<String> meldung = new ArrayList<String>();
                for (InselVerwaltung.ParteiEinfluss pe : einfluesse) {
                    meldung.add(Partei.getPartei(pe.getPartei()) + " " + P.format(pe.getEinfluss() / summeEinfluss));
                }
                if (!meldung.isEmpty()) beschreibung.append("Einfluss: " + StringUtils.aufzaehlung(meldung) + ". ");
                
                writer.wl(beschreibung.toString(), "beschr");
            }
        }
    }

    /**
	 * speichert alle Regionen ...
	 */
	private void SaveRegionen() {
        InselVerwaltung iv = InselVerwaltung.getInstance();
        
        Map<Coords, Region> bekannteRegionen = new HashMap<Coords, Region>();
        SortedSet<Coords> bekannteCoords = new TreeSet<Coords>(new CoordComparatorLNR());
        
        for (Region r : iv.getParteiReportDaten(partei).getRegionen()) {
            bekannteCoords.add(r.getCoords());
            bekannteRegionen.put(r.getCoords(), r);
        }
        for (Region r : iv.getParteiReportDaten(partei).getNachbarn()) {
            bekannteCoords.add(r.getCoords());
            bekannteRegionen.put(r.getCoords(), r);
        }
        for (RegionsSicht rs : iv.getParteiReportDaten(partei).getHistorische()) {
            bekannteCoords.add(rs.getCoords());
        }
        
        for (Coords c : bekannteCoords) {
            if (bekannteRegionen.containsKey(c)) {
                Region r = bekannteRegionen.get(c);
                r.SaveCR(writer, partei, partei.getRegionsSicht(c));
            } else {
                // oha, historisch:
                partei.atlas.get(c).SaveCR(writer, partei);
            }
        }
	}
	

    private void SaveZauber(Partei p) {
        Set<String> alreadyWritten = new HashSet<String>();

        for (Unit u : Unit.CACHE) {
            if (u.getOwner() != p.getNummer()) continue;
            if (u.getSpells().isEmpty()) continue;

            for (Spell sp : u.getSpells()) {
                if ( alreadyWritten.contains(sp.getName()) ) continue;

                writer.wl(sp.getCREntry());

                alreadyWritten.add(sp.getName());
            }
        }
        
    }

}
