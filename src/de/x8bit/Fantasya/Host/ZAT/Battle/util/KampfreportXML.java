package de.x8bit.Fantasya.Host.ZAT.Battle.util;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Einzelattacke;
import de.x8bit.Fantasya.Host.ZAT.Battle.KampfrundenStatistik;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.KriegerTyp;
import de.x8bit.Fantasya.Host.ZAT.Battle.Side;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.comparator.UnitSortierungComparator;
import de.x8bit.Fantasya.util.html.RegexpRenderer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author hb
 */
public class KampfreportXML /* implements CommandLineArg */ {
    /**
     * Mit diesem globalen Flag lässt sich die Erzeugung von XML-Kampfreporten
     * generell "stumm" schalten.
     */
    public static boolean STUMM = true;

    private static final String HTML_TEMPLATE = "kampfbericht-template.html";

    /**
     * wenn unspezifische Meldungen aufgenommen werden, werden sie in diese Runde aufgenommen.
     */
    protected static int DefaultRunde = 0;

    protected final Partei partei;
    Document doc = null;

    /* public KampfreportXML() {
        this(null);
    } */

    public KampfreportXML(Partei partei) {
        this.partei = partei;
        if (STUMM) return;

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = dbfac.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException ex) {
            new BigError(ex);
        }

        //create the root element and add it to the document
        Element root = doc.createElement("kampfbericht");
        if (partei != null) root.setAttribute("partei", partei.getNummerBase36());
        doc.appendChild(root);
    }

    /**
     * @param runde Die fragliche Kampfrunde; muss größer oder gleich 0 sein.
     * @return Das entsprechende Element - garantiert nicht null.
     */
    public Node getRunde(int runde) {
        if (STUMM) return null;

        if (runde < 0) throw new IllegalArgumentException("Die Runde muss größer als 0 sein (" + runde +").");
        
        NodeList runden = doc.getElementsByTagName("runde");
        for (int i=0; i<runden.getLength(); i++) {
            Node n = runden.item(i);
            Node nr = n.getAttributes().getNamedItem("nr");
            if (nr != null) {
                if (nr.getNodeValue().equalsIgnoreCase(Integer.toString(runde))) return n;
            }
        }

        // okay, die Runde existiert (noch) nicht:
        Element e = doc.createElement("runde");
        e.setAttribute("nr", Integer.toString(runde));

        Element headline = doc.createElement("html");
        headline.appendChild(doc.createCDATASection("<h2>" + runde + ". Runde</h2>"));
        headline.setAttribute("trenner", Integer.toString(runde));
        e.appendChild(headline);

        Element ereignisse = doc.createElement("ereignisse");
        e.appendChild(ereignisse);
        
        Element resultat = doc.createElement("resultat");
        e.appendChild(resultat);

        Node root = doc.getFirstChild();
        if (runde > 0) {
            Node prvRunde = getRunde(runde - 1);
            Node after = prvRunde.getNextSibling();
            if (after != null) {
                root.insertBefore(e, prvRunde);
            } else {
                root.appendChild(e);
            }
        } else {
            root.appendChild(e);
        }

        return e;
    }

    public void setKampfBeschreibung(String s) {
        if (STUMM) return;

        Node n = doc.getElementsByTagName("beschreibung").item(0);
        if (n == null) {
            n = doc.createElement("beschreibung");
            doc.getFirstChild().appendChild(n);
        }

        Element html = doc.createElement("html");
        html.setAttribute("trenner", "ja");
        html.appendChild(doc.createCDATASection(s));
        html.appendChild(doc.createCDATASection("<h2>Ausgangslage</h2>\n"));

        n.appendChild(html);
    }

    public void beschreibeStatus(Side att, Side def, KampfrundenStatistik ks) {
        if (STUMM) return;
        
        Node runde = getRunde(DefaultRunde);
        
        NodeList children = runde.getChildNodes();
        Node resultat = null;
        for (int i=0; i<children.getLength(); i++) {
            Node maybe = children.item(i);
            if (maybe.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!maybe.getNodeName().equalsIgnoreCase("resultat")) continue;

            resultat = maybe; // gotcha!
        }

        Element side = beschreibeSeite(att, ks, true); // left side
        side.setAttribute("rolle", "angreifer");
        resultat.appendChild(side);

        side = beschreibeSeite(def, ks, false); // right side
        side.setAttribute("rolle", "verteidiger");
        resultat.appendChild(side);
    }

    /**
     * Schreibt einen &lt;beteiligte&gt;-Baum in das Dokument, in dem alle
     * Parteien, Einheiten und einzelnen Krieger dokumentiert werden.
     * @param att
     * @param def
     */
    public void setBeteiligte(Side att, Side def) {
        if (STUMM) return;
        
        Node top = doc.getElementsByTagName("beteiligte").item(0);
        if (top == null) {
            top = doc.createElement("beteiligte");
            doc.getFirstChild().appendChild(top);
        }

        Element side = beschreibeSeite(att, null, true); // left side
        side.setAttribute("rolle", "angreifer");
        top.appendChild(side);

        side = beschreibeSeite(def, null, false); // right side
        side.setAttribute("rolle", "verteidiger");
        top.appendChild(side);
    }

    private Element verwundetenReportHtml(Side side, KampfrundenStatistik ks)	{
        if (STUMM) return null;

        Element html = doc.createElement("html");

        StringBuffer sb = new StringBuffer();
        for (Partei p : side.getParteien()) {
            sb.append("<div class=\"partei\">" + p + "<br>\n");

            List<Unit> units = side.getUnits(p);
            Collections.sort(units, new UnitSortierungComparator());

            for (Unit u : units) {
                List<Krieger> krieger = side.getKrieger(u);

                sb.append("<div class=\"einheit\">");
                if (ks != null) {
                    sb.append(ks.getBericht(u)).append("<br>Status: ");
                }
                sb.append(u.toString());
                sb.append(", " + krieger.size() + " Kämpfer");
                sb.append("<br>\n");
                sb.append("<code>");
                sb.append(Krieger.HitpointSymbols(krieger, true)); // ture = HTML-Ausgabe
                sb.append("</code>\n");
                sb.append("</div>\n");
            }

            sb.append("</div>\n");
        }
        
        Node contents = doc.createCDATASection(sb.toString());
        html.appendChild(contents);
        return html;
    }

    protected Element beschreibeSeite(Side seite, KampfrundenStatistik ks, boolean leftSide) {
        if (STUMM) return null;
        
        Element el = doc.createElement("seite");

        for (Partei p : seite.getParteien()) {
            Element pe = doc.createElement("partei");
            pe.setAttribute("id", p.getNummerBase36());
            pe.setAttribute("name", p.getName());
            el.appendChild(pe);

            for (Unit u : seite.getUnits(p)) {
                Element ue = doc.createElement("einheit");
                ue.setAttribute("id", u.getNummerBase36());
                ue.setAttribute("rasse", u.getRasse());
                ue.setAttribute("personen", Integer.toString(u.getPersonen()));
                ue.setAttribute("kampfposition", u.getKampfposition().name());
                pe.appendChild(ue);

                if (ks != null) ue.appendChild(doc.createCDATASection(ks.getBericht(u)));

                for (Krieger k : seite.getKrieger(u)) {
                    Element ke = doc.createElement("krieger");
                    ke.setAttribute("id", u.getNummerBase36() + "." + k.getIndex());
                    ke.setAttribute("hp", Integer.toString(k.getLebenspunkte()));
                    ke.setAttribute("le", Integer.toString(k.getTrefferpunkte()));
                    ke.setAttribute("typ", KriegerTyp.getInstance(k).toString());
                    ue.appendChild(ke);
                }
            }
        }

        Element html = verwundetenReportHtml(seite, ks);
        if (leftSide) html.setAttribute("left", "1");
        if (!leftSide) html.setAttribute("right", "1");
        el.appendChild(html);

        return el;
    }

    protected Node getNode(Krieger k) {
        if (STUMM) return null;
        
        String attUnit = k.getUnit().getNummerBase36();
        String attPartei = Codierung.toBase36(k.getUnit().getOwner());

        Node runde = getRunde(DefaultRunde);

        // Ereignis-Struktur suchen:
        Node ereignisse = null;
        NodeList children = runde.getChildNodes();
        for (int i=0; i < children.getLength(); i++) {
            Node maybe = children.item(i);
            if (maybe.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!maybe.getNodeName().equalsIgnoreCase("ereignisse")) continue;
            ereignisse = maybe;
        }

        // Partei suchen oder anlegen:
        Node partei = null;
        children = ereignisse.getChildNodes();
        for (int i=0; i < children.getLength(); i++) {
            Node maybe = children.item(i);
            if (maybe.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!maybe.getNodeName().equalsIgnoreCase("partei")) continue;
            String id = ((Element)maybe).getAttribute("base36");
            if (!id.equals(attPartei)) continue;

            // gotcha!
            partei = maybe;
        }
        if (partei == null) {
            partei = doc.createElement("partei");
            ((Element)partei).setAttribute("base36", attPartei);
            ereignisse.appendChild(partei);
        }

        // Unit suchen oder anlegen:
        Node einheit = null;
        children = partei.getChildNodes();
        for (int i=0; i < children.getLength(); i++) {
            Node maybe = children.item(i);
            if (maybe.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!maybe.getNodeName().equalsIgnoreCase("einheit")) continue;
            String id = ((Element)maybe).getAttribute("base36");
            if (!id.equals(attUnit)) continue;

            // gotcha!
            einheit = maybe;
        }
        if (einheit == null) {
            einheit = doc.createElement("einheit");
            ((Element)einheit).setAttribute("base36", attUnit);
            partei.appendChild(einheit);
        }

        // Krieger suchen oder anlegen:
        Node kriegerNode = null;
        children = einheit.getChildNodes();
        for (int i=0; i < children.getLength(); i++) {
            Node maybe = children.item(i);
            if (maybe.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!maybe.getNodeName().equalsIgnoreCase("krieger")) continue;
            String id = ((Element)maybe).getAttribute("id");
            if (!id.equals(getKriegerId(k))) continue;

            // gotcha!
            kriegerNode = maybe;
        }
        if (kriegerNode == null) {
            kriegerNode = doc.createElement("krieger");
            ((Element)kriegerNode).setAttribute("id", getKriegerId(k));
            einheit.appendChild(kriegerNode);
        }
        
        return kriegerNode;
    }

    public void registriere(KriegerTyp kt) {
        if (STUMM) return;

        Node top = doc.getElementsByTagName("beteiligte").item(0);
        if (top == null) {
            top = doc.createElement("beteiligte");
            doc.getFirstChild().appendChild(top);
        }

        Node ktRootNode = null;
        NodeList children = top.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node maybe = children.item(i);
            if (maybe.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!maybe.getNodeName().equalsIgnoreCase("kriegerTypen")) continue;

            ktRootNode = maybe; // gotcha!
        }
        if (ktRootNode == null) {
            ktRootNode = doc.createElement("kriegerTypen");
            top.appendChild(ktRootNode);
        }


        // gibt es diesen Krieger-Typen schon?
        Node ktNode = null;
        children = ktRootNode.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node maybe = children.item(i);
            if (maybe.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!maybe.getNodeName().equalsIgnoreCase("kriegerTyp")) continue;
            String id = ((Element)maybe).getAttribute("name");
            if (!id.equals(kt.toString())) continue;

            ktNode = maybe; // gotcha!
        }
        if (ktNode == null) {
            ktNode = doc.createElement("kriegerTyp");
            ((Element)ktNode).setAttribute("name", kt.toString());
            ktRootNode.appendChild(ktNode);
        }
    }

    public void registriere(Einzelattacke ea) {
        if (STUMM) return;
        
        Node kriegerNode = getNode(ea.getAngreifer());

        Element angriff = doc.createElement("angriff");
        angriff.setAttribute("angreifer", getKriegerId(ea.getAngreifer()));
        angriff.setAttribute("verteidiger", getKriegerId(ea.getVerteidiger()));
        angriff.setAttribute("av", Integer.toString(ea.getAv()));
        angriff.setAttribute("dv", Integer.toString(ea.getDv()));
        angriff.setAttribute("erfolg", Boolean.toString(ea.isErfolgreich()));
        if (ea.isErfolgreich()) {
            angriff.setAttribute("dmg", Integer.toString(ea.getDamageValue()));
            angriff.setAttribute("blk", Integer.toString(ea.getBlockValue()));
        }
        angriff.setAttribute("seq", Integer.toString(ea.getLaufendeNummer()));
        // angriff.appendChild(doc.createCDATASection(ea.getBeschreibung()));
        kriegerNode.appendChild(angriff);


        kriegerNode = getNode(ea.getVerteidiger());
        Element verteidigung = doc.createElement("verteidigung");
        verteidigung.setAttribute("angreifer", getKriegerId(ea.getAngreifer()));
        verteidigung.setAttribute("verteidiger", getKriegerId(ea.getVerteidiger()));
        verteidigung.setAttribute("av", Integer.toString(ea.getAv()));
        verteidigung.setAttribute("dv", Integer.toString(ea.getDv()));
        verteidigung.setAttribute("erfolg", Boolean.toString(ea.isErfolgreich()));
        if (ea.isErfolgreich()) {
            verteidigung.setAttribute("dmg", Integer.toString(ea.getDamageValue()));
            verteidigung.setAttribute("blk", Integer.toString(ea.getBlockValue()));
        }
        verteidigung.setAttribute("seq", Integer.toString(ea.getLaufendeNummer()));
        // verteidigung.appendChild(doc.createCDATASection(ea.getBeschreibung()));
        kriegerNode.appendChild(verteidigung);
    }

    public String getKriegerId(Krieger k) {
        return  k.getUnit().getNummerBase36() + "." + k.getIndex();
    }

    public void meldung(String text) {
        if (STUMM) return;
        
        Node runde = getRunde(DefaultRunde);

        NodeList children = runde.getChildNodes();
        Node ereignisse = null;
        for (int i=0; i<children.getLength(); i++) {
            Node maybe = children.item(i);
            if (maybe.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!maybe.getNodeName().equalsIgnoreCase("ereignisse")) continue;

            ereignisse = maybe; // gotcha!
        }

        Element me = doc.createElement("meldung");
        ereignisse.appendChild(me);

        // CDATASection c = doc.createCDATASection((text.trim()+"\n").replaceAll("\n", "<br>\n"));
        CDATASection c = doc.createCDATASection(text.trim());
        me.appendChild(c);
    }

    public Document getDocument() {
        return doc;
    }

    public Partei getPartei() {
        return partei;
    }

    public void foobar() {
        Node root = doc.getFirstChild();

        //create a comment and put it in the root element
        Comment comment = doc.createComment("Just a thought");
        root.appendChild(comment);

        //create child element, add an attribute, and add to root
        Element child = doc.createElement("child");
        child.setAttribute("name", "value");
        root.appendChild(child);

        Element e2 = doc.createElement("grandchild");
        e2.setAttribute("name", "value");
        child.appendChild(e2);

        Element e3 = doc.createElement("grandchild");
        e3.setAttribute("name", "anotherValue");
        child.appendChild(e3);

        //add a text element to the child
        Text text = doc.createTextNode("Filler, ... I could have had a foo!");
        e3.appendChild(text);

        text = doc.createTextNode("Blah, blah");
        child.appendChild(text);

        setKampfBeschreibung("Ja, ja!\nja.");

//        Node r = getRunde(10);




        //print xml
        System.out.println("Here's the xml:\n\n" + this.toString());
    }

    public static void setDefaultRunde(int DefaultRunde) {
        KampfreportXML.DefaultRunde = DefaultRunde;
    }

    public void writeFile(String filename) {
        if (STUMM) return;
        
        stringToFile(this.toString(), filename);
    }

    public void writeHtml(String filename) {
        if (STUMM) return;

        String tableStart = "<table width=\"100%\"><tr>\n";
        String tableEnd = "</tr></table>\n";

        String cellStart = "<td width=\"50%\">";
        String cellEnd = "</td>";

        Map<String, String> params = new HashMap<String,String>();
        StringBuffer sb = new StringBuffer().append(tableStart);
        StringBuffer left = new StringBuffer().append(cellStart);
        StringBuffer right = new StringBuffer().append(cellStart);

        NodeList htmls = doc.getElementsByTagName("html");
        for (int i=0; i<htmls.getLength(); i++) {
            Element html = (Element)htmls.item(i);
            if (html.hasAttribute("trenner")) {
                left.append(cellEnd);
                right.append(cellEnd);
                sb.append(left).append(right).append(tableEnd);
                sb.append(html.getTextContent() + "\n");
                sb.append(tableStart);
                left = new StringBuffer().append(cellStart);
                right = new StringBuffer().append(cellStart);

                continue;
            }
            if (html.hasAttribute("left")) left.append(html.getTextContent() + "\n");
            if (html.hasAttribute("right")) right.append(html.getTextContent() + "\n");
        }

        left.append(cellEnd);
        right.append(cellEnd);
        sb.append(left).append(right).append(tableEnd);
        params.put("contents", sb.toString());

		try {
			InputStream file = new FileInputStream(KampfreportXML.HTML_TEMPLATE);
			Reader input = new InputStreamReader(file, "UTF8");
			RegexpRenderer tr = new RegexpRenderer(input, "<!--@", "-->");

			tr.setParameters(params);
			stringToFile(tr.processTemplate(), filename);
		} catch (Exception e) {
			// TODO: this is poor error handling; probably we want to
			// send the exception to some upper function
			e.printStackTrace();
		}
    }

    private void stringToFile(String string, String filename) {
        if (STUMM) return;
        
        try {
            File fileDir = new File(filename);

            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));

            out.append(string);
            out.flush();
            out.close();

        } catch (UnsupportedEncodingException ex) {
            new BigError(ex);
        } catch (IOException ex) {
            new BigError(ex);
        }
        
    }


    @Override
    public String toString() {
        if (STUMM) return null;
        
        /////////////////
        //Output the XML
        String xmlString = null;

        //set up a transformer
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = null;
            trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            //create string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            xmlString = sw.toString();
        } catch (TransformerConfigurationException ex) {
            new BigError(ex);
        } catch (TransformerException ex) {
            new BigError(ex);
        }

        return xmlString;
    }

	public List<String> getCommandLineOptions() {
		List<String> retval = new ArrayList<String>();

		retval.add("KampfreportXML.test");

		return retval;
	}

	public void executeCommandLineOption(String commandLineOption) {
		if (commandLineOption.equals("KampfreportXML.test")) {
			this.foobar();
            return;
		}

        throw new IllegalArgumentException("Unbekannte Befehlszeilenoption: " + commandLineOption);
    }

}
