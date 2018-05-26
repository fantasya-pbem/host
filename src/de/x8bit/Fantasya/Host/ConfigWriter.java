package de.x8bit.Fantasya.Host;

import java.io.FileOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;


import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Dingens;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Items.HerbalResource;
import de.x8bit.Fantasya.Atlantis.Items.Resource;
import de.x8bit.Fantasya.Atlantis.Items.AnimalResource;
import de.x8bit.Fantasya.Atlantis.Items.LuxusGood;
import de.x8bit.Fantasya.Atlantis.Items.MagicItem;
import de.x8bit.Fantasya.Atlantis.Items.Weapon;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;

public class ConfigWriter
{
	XMLStreamWriter xml;
	
	public ConfigWriter()
	{
		new SysMsg("schreibe XML-Konfiguration");
		
		try
		{
			xml = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileOutputStream("fantasya.xml"));
			xml.writeStartDocument();
			
			xml.writeStartElement("config");
			xml.writeAttribute("game", "fantasya");
			xml.writeAttribute("version", Datenbank.Select("SELECT value FROM settings WHERE name = 'version'", "0.0.0"));

			writeSkills();
			writeItems();
			writeBuildings();
			writeShips();
			writeRegions();
			writeRaces();
			writeSpells();
			
			xml.writeEndElement();
			xml.flush();
			xml.close();
		} catch (Exception e)
		{
			new BigError(e);
		}
	}
	
	private void writeData(String name, String data) throws Exception
	{
		xml.writeStartElement(name);
		xml.writeCharacters(data);
		xml.writeEndElement();
	}
	private void writeData(String name, int value) throws Exception
	{
		writeData(name, "" + value + "");
	}	
	private void writeData(String name, boolean value) throws Exception
	{
		writeData(name, "" + value + "");
	}
	private void addConstructionFragment(String fragname, String reference, String value) throws Exception
	{
		xml.writeStartElement(fragname);
		xml.writeAttribute("ref", reference);
		xml.writeCharacters(value);
		xml.writeEndElement();
	}
	private void addConstructionFragment(String fragname, String simpleName, int value) throws Exception {
		addConstructionFragment(fragname, simpleName, "" + value);
	}

	private void writeItems() throws Exception
	{
		xml.writeStartElement("items");
		
		for(Paket p : Paket.getPaket("Items"))
		{
			xml.writeStartElement("item");
			xml.writeAttribute("id", p.Klasse.getClass().getSimpleName());
		
			Item item = (Item) p.Klasse;
			item.setAnzahl(1);
			
			writeData("name", item.getName());
			writeData("weight", item.getGewicht());
			writeData("capacity", item.getKapazitaet());
			
			writeData("resource", item instanceof Resource);
			writeData("herb", item instanceof HerbalResource);
			writeData("merchandising", item instanceof LuxusGood);
			writeData("magic", item instanceof MagicItem);
			writeData("animal", item instanceof AnimalResource);
			writeData("weapon", item instanceof Weapon);
			
			writeData("costs", item.Unterhalt());
			writeData("price", item.getPrice());
			
			WriteConstructionPlan(item);
			
			xml.writeEndElement();
		}
		
		xml.writeEndElement();
	}
	
	private void writeSkills() throws Exception
	{
		xml.writeStartElement("skills");
		
		for(Paket p : Paket.getPaket("Skills"))
		{
			xml.writeStartElement("skill");
			xml.writeAttribute("id", p.Klasse.getClass().getSimpleName());
			
			writeData("name", p.Klasse.getName());
			
			xml.writeEndElement();
		}
		
		xml.writeEndElement();
	}

	private void writeBuildings() throws Exception
	{
		xml.writeStartElement("buildings");
		
		for(Paket p : Paket.getPaket("Buildings"))
		{
			xml.writeStartElement("building");
			xml.writeAttribute("id", p.Klasse.getClass().getSimpleName());
			
			Building building = (Building) p.Klasse;
			
			writeData("name", building.getName());
			writeData("costs-building", building.GebaeudeUnterhalt());
			writeData("costs-units", building.UnterhaltEinheit());
			
			// TODO Skills - bzw. Vorteile
			WriteConstructionPlan(building);
			
			xml.writeEndElement();
		}
		
		xml.writeEndElement();		
	}

	private void writeShips() throws Exception
	{
		xml.writeStartElement("ships");
		
		for(Paket p : Paket.getPaket("Ships"))
		{
			xml.writeStartElement("ship");
			xml.writeAttribute("id", p.Klasse.getClass().getSimpleName());
			
			Ship ship = (Ship) p.Klasse;
			
			writeData("name", ship.getName());
			
			writeData("speed", ship.getGeschwindigkeit());
			writeData("captain", ship.getKapitaenTalent());
			writeData("crew", ship.getMatrosen());
			writeData("capacity", ship.getKapazitaet());
			
			WriteConstructionPlan(ship);

			//if (ship.neededHarbour() != null) for(Class<? extends Ship> c : ship.neededHarbour()) addConstructionFragment("building", c.getSimpleName(), "");

			xml.writeEndElement();
		}
		
		xml.writeEndElement();		
	}

	private void WriteConstructionPlan(Dingens object) throws Exception {
		xml.writeStartElement("construction");
			if (object.getConstructionItems() != null) if (object.getConstructionItems().length > 0)
			{
				xml.writeStartElement("items");
				for(ConstructionContainer cc : object.getConstructionItems()) addConstructionFragment("item", cc.getClazz().getSimpleName(), cc.getValue());
				xml.writeEndElement();
			}
			if (object.getConstructionSkills() != null) if (object.getConstructionSkills().length > 0)
			{
				xml.writeStartElement("skills");
				for(ConstructionContainer cc : object.getConstructionSkills()) addConstructionFragment("skill", cc.getClazz().getSimpleName(), cc.getValue());
				xml.writeEndElement();
			}
			if (object.getConstructionBuildings() != null) if (object.getConstructionBuildings().length > 0)
			{
				xml.writeStartElement("buildings");
				for(ConstructionContainer cc : object.getConstructionBuildings()) addConstructionFragment("building", cc.getClazz().getSimpleName(), cc.getValue());
				xml.writeEndElement();
			}
			if (object.getConstructionCheats() != null) if (object.getConstructionCheats().length > 0)
			{
				xml.writeStartElement("cheats");
				for(ConstructionCheats cc : object.getConstructionCheats())
				{
					xml.writeStartElement("cheat");
					xml.writeAttribute("building", cc.getConstructionBuilding().getSimpleName());
						for(ConstructionContainer c : cc.getConstructionItems()) addConstructionFragment("item", c.getClazz().getSimpleName(), c.getValue());
					xml.writeEndElement();
				}
				xml.writeEndElement();
			}
		xml.writeEndElement();
	}

	private void writeRegions() throws Exception
	{
		xml.writeStartElement("regions");
		
		for(Paket p : Paket.getPaket("Regions"))
		{
			xml.writeStartElement("region");
			xml.writeAttribute("id", p.Klasse.getClass().getSimpleName());
			
			Region region = (Region) p.Klasse;
			writeData("name", region.getClass().getSimpleName()); // getName() ist eine überschriebene Methode !!
			writeData("street", region.getSteineFuerStrasse());
			// writeData("workplaces", region.freieArbeitsplaetze());
			
			xml.writeEndElement();
		}
		
		xml.writeEndElement();
	}

	@SuppressWarnings("unchecked")
	private void writeRaces() throws Exception
	{
		xml.writeStartElement("races");
		
		for(Paket p : Paket.getPaket("Units"))
		{
			xml.writeStartElement("race");
			xml.writeAttribute("id", p.Klasse.getClass().getSimpleName());
			
			Unit unit = (Unit) p.Klasse;
			writeData("name", unit.getClass().getSimpleName()); 	// getName() ist eine überschriebene Methode !!
			if (!(p.Klasse instanceof Monster)) {
				writeData("capacity", unit.getKapazitaet());			// Kapazität der Einheit
				writeData("weight", unit.getGewichtRaw());				// Gewicht der Einheit
				writeData("livepoints", unit.maxLebenspunkte());
				writeData("recruiting", unit.getRekrutierungsKosten());
				xml.writeStartElement("skillmodifications");
				unit.setPersonen(1);
				for(Paket paket : Paket.getPaket("Skills"))
				{
					Class<? extends Skill> c = (Class<? extends Skill>) paket.Klasse.getClass();
					unit.setSkill(c, 840);	// T7 bei Mensch bzw. Boni/Mali == 0 
					int modifikation = unit.Talentwert(c) - 7;
					if (modifikation != 0)
					{
						xml.writeStartElement("skill");
						xml.writeAttribute("ref", c.getSimpleName());
						xml.writeCharacters("" + modifikation);
						xml.writeEndElement();
					}
				}
				xml.writeEndElement();
			}
			
			xml.writeEndElement();
		}
		
		xml.writeEndElement();
	}

	private void writeSpells() throws Exception
	{
		xml.writeStartElement("spells");
		
		for(Paket p : Paket.getPaket("Spells"))
		{
			xml.writeStartElement("spell");
			xml.writeAttribute("id", p.Klasse.getClass().getSimpleName());
			
			Spell spell = (Spell) p.Klasse;
			
			writeData("name", spell.getName());
			writeData("level", spell.getStufe());
			writeData("battlespell", spell.isBattleSpell());
			writeData("firstspell", spell.isFirstSpell());
			writeData("attackspell", spell.isAttackSpell());
			writeData("defencespell", spell.isDefenceSpell());
			writeData("confusionspell", spell.isConfusionSpell());
			writeData("element", spell.getElementar().toString());
			writeData("say", spell.getSpruch());
			writeData((spell.isOrcus() ? " mana" : "aura"), spell.getStufe());
			writeData("description", spell.getBeschreibung());
			xml.writeEndElement();
		}
		
		xml.writeEndElement();		
	}
}
