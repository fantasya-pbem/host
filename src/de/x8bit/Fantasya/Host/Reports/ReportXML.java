package de.x8bit.Fantasya.Host.Reports;

import java.util.Date;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Host.BuildInformation;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Reports.Writer.XMLWriter;
import de.x8bit.Fantasya.util.Codierung;
import java.util.ArrayList;

/**
 * Report als XML-Ausgabe
 * @author mogel
 */
public class ReportXML
{
	private XMLWriter xml;
	
	private static long date = new Date().getTime(); // Zeitpunkt für alle Spieler gleich
	
	public ReportXML(Partei partei)
	{
		xml = new XMLWriter(partei);
		
		// der Report
		XMLHeader();
		Voelker(partei);
		//Allianzen(partei);
		Meldungen(partei);
		SaveRegionen(partei);
		
		// File schließen
		xml.CloseFile();
	}
	
	private void XMLHeader()
	{
		xml.ElementStart("header");
		xml.ElementShort("language", "de-de");
		xml.ElementShort("date", "" + date + "");
		xml.ElementShort("round", GameRules.getRunde());
		xml.ElementShort("buildid", BuildInformation.getBuild());

//		!! die Daten werden über die Rules-Set (vgl. gamedata.php) Datei in Kolumbus bereit gestellt !!
//	
//		xml.ElementShort("rules", "http://www.fantasya-pbem.de/download/fantasya.xml");
//		xml.ElementShort("commands", "http://www.fantasya-pbem.de/download/template.tpl");
//	
//		!! die Daten werden über die Rules-Set Datei in Kolumbus bereit gestellt !!
		
		xml.ElementEnd();
	}
	
	private void Voelker(Partei partei)
	{
		xml.ElementStart("factions");
		
//		if (!Main.getBFlag("EVA")) {
//			db.myQuery = "SELECT e.partei FROM einheiten e, (SELECT * FROM einheiten e WHERE partei = " + partei.getNummer() + " GROUP BY koordx, koordy, welt) k WHERE e.koordx = k.koordx AND e.koordy = k.koordy AND e.welt = k.welt GROUP BY e.partei";
//			ResultSet rs = db.Select();
//			try
//			{
//				while(rs.next())
//				{
//					Partei p = Partei.Load(rs.getInt("partei"));
//					if (p == null) continue;
//					if (p.isMonster()) continue;
//					p.SaveXML(xml, partei);
//				}
//			} catch(Exception ex) { new BigError(ex); }
//		} else {
			// EVA-Version:
			for (Partei p : partei.getBekannteParteien()) {
				// -- 03.10.2011 -- if (p.isMonster()) continue;
				p.SaveXML(xml, partei);
			}
//		}
		
		xml.ElementEnd();
	}
	
	private void Meldungen(Partei partei)
	{
		xml.ElementStart("messages");
	
		// alle Kategorien holen
		ArrayList<String> kategorien = new ArrayList<String>();	// alle Meldungs-Kategorien
		// TODO automatisierte EVA-Version
		kategorien.add("Greetings");
		kategorien.add("Fehler");
		kategorien.add("Battle");
		kategorien.add("Bewegung");
		kategorien.add("Magie");
		kategorien.add("Info");
		kategorien.add("Handelsmeldungen");
		kategorien.add("EchsenNews");
		kategorien.add("Zauberbuch");
		for (String kategorie : kategorien) {
			 Kategorie(partei, kategorie);
		}

		xml.ElementEnd();
	}
	
	private void Kategorie(Partei partei, String kategorie)
	{
		xml.ElementStart("categorie");
		xml.ElementAttribute("title", kategorie);

		// EVA-Version
		for (Message m : Message.Retrieve(partei, null, null, kategorie)) {
			int unit = 0;
			if (m.getUnit() != null) {
				unit = m.getUnit().getNummer();
			}
			int x = 0, y = 0, welt = 0;
			if (m.getCoords() != null) {
				x = m.getCoords().getX();
				y = m.getCoords().getY();
				welt = m.getCoords().getWelt();
			}

			xml.ElementStart("message");
			xml.ElementAttribute("id", m.getEvaId());

			xml.ElementAttribute("unit", Codierung.toBase36(unit));
			xml.ElementAttribute("x", x - partei.getUrsprung().getX());
			xml.ElementAttribute("y", y - partei.getUrsprung().getY());
			xml.ElementAttribute("welt", welt);

			xml.ElementData(m.getText());

			xml.ElementEnd();
		}
		
		xml.ElementEnd();
	}
	
	private void SaveRegionen(Partei partei)
	{
		xml.ElementStart("regions");
		
		SaveRegion(xml.Regionen(), partei, false);
		SaveRegion(xml.Nachbarn(), partei, true);
		
		xml.ElementEnd();
	}
	
	private void SaveRegion(List<Region> regions, Partei partei, boolean kurz)
	{
		for(Region region : regions) region.SaveXML(xml, partei, kurz);
	}
}
