package de.x8bit.Fantasya.Host.Reports.Writer;

import java.io.FileOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Reports.ReportWriter;

public final class XMLWriter extends ReportWriter
{

	private XMLStreamWriter xml;
	private XMLOutputFactory factory;
	
	
	public XMLWriter(Partei partei)
	{
		super(partei);
		new SysMsg(" - XML-Report");
		
		// passendes File öffnen
		OpenFile(".xml", "utf-8");
	}

	@Override
	protected void OpenFile(String extension, String codierung)
	{
		boolean disabled = !Datenbank.isEnabled();
        if (disabled) Datenbank.Enable();

        try
		{
			String f;
			
			if (partei.getNummer() != 0)
			{
				f = "reporte/" + GameRules.getRunde() + "-" + partei.getNummerBase36() + extension;
			} else
			{
				f = "world.xml";
			}

			factory = XMLOutputFactory.newInstance();
			
			xml = factory.createXMLStreamWriter(new FileOutputStream(f));
            //xml = factory.createXMLStreamWriter(new FileOutputStream(file));
			
	        xml.writeStartDocument();

	        // -- ist ungünstig zum reinen Anzeigen im Browser (Sicherheitsrichtlinienen verhindern den Download der XSL Dateien)
//	        xml.writeProcessingInstruction("xml-stylesheet type=\"text/xml\" href=\"http://www.fantasya-pbem.de/openapi/xmlreport/fantasya/fantasya.xsl\""); xml.writeCharacters("\n");
//	        xml.writeProcessingInstruction("xml-stylesheet type=\"text/xsl\" href=\"http://www.fantasya-pbem.de/openapi/xmlreport/fantasya/fantasya.xsl\""); xml.writeCharacters("\n\n");
			
			xml.writeStartElement("reports");
			xml.writeAttribute("game", "fantasya");

			boolean enabled = Datenbank.isEnabled();
			if (!enabled) Datenbank.Enable();
			xml.writeAttribute("version", Datenbank.Select("SELECT value FROM settings WHERE name = 'version'", "0.0.0"));
			if (!enabled) Datenbank.Disable();
			
			ElementStart("report");
			ElementAttribute("round", GameRules.getRunde());
		} catch(Exception ex)
		{
			new BigError(ex);
		}

        if (disabled) Datenbank.Disable();
	}

	@Override
	public void CloseFile()
	{
		// und die ganze Sache beenden
		try
		{
			ElementEnd();	// report
			
			xml.writeEndElement();	// reports
			xml.flush();
			xml.close();
		} catch(Exception ex) { new BigError(ex); }
	}

	public void ElementShort(String name, String data)
	{
		try
		{
			xml.writeStartElement(name);
			xml.writeCharacters(data);
			xml.writeEndElement();
			xml.writeCharacters("\n");
		} catch (XMLStreamException e)
		{
			new BigError(e);
		}
	}
	public void ElementShort(String name, int value)
	{
		ElementShort(name, "" + value + "");
	}	
	public void ElementShort(String name, boolean value)
	{
		ElementShort(name, "" + value + "");
	}

	
	public void ElementStart(String name)
	{
		try
		{
			xml.writeStartElement(name);
		} catch (XMLStreamException e)
		{
			new BigError(e);
		}
	}
	
	public void ElementAttribute(String attribute, String value)
	{
		try
		{
			xml.writeAttribute(attribute, value);
		} catch(Exception e)
		{
			new BigError(e);
		}
	}
	public void ElementAttribute(String attribute, int value)
	{
		ElementAttribute(attribute, "" + value + "");
	}

	public void ElementData(String data)
	{
		try
		{
			xml.writeCharacters(data);
		} catch (XMLStreamException e)
		{
			new BigError(e);
		}
	}
	public void ElementData(int anzahl)
	{
		ElementData("" + anzahl + "");
	}

	public void ElementEnd()
	{
		try
		{
			xml.writeEndElement();
			xml.writeCharacters("\n");
		} catch (XMLStreamException e)
		{
			new BigError(e);
		}
	}

}
