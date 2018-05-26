package de.x8bit.Fantasya.Host;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Effect;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.util.PackageLister;

/**
 * Verwaltet alle Packagedaten für die Atlantis-spezifischen Spiel-Objekte.
 * Die Daten werden per Reflection direkt aus dem Classpath / JAR geholt.
 *
 * @author  mogel
 */
public class Paket
{
	/** *psst* ... der bleibt geheim */
	private Paket() { }
	
	public static void Init() {
		// new SysMsg("Einlesen der Package-Daten...");

		LoadPackage("Items", Item.class, "de.x8bit.Fantasya.Atlantis.Items");
		LoadPackage("Skills", Skill.class, "de.x8bit.Fantasya.Atlantis.Skills");
		LoadPackage("Buildings", Building.class, "de.x8bit.Fantasya.Atlantis.Buildings");
		LoadPackage("Ships", Ship.class, "de.x8bit.Fantasya.Atlantis.Ships");
		LoadPackage("Regions", Region.class, "de.x8bit.Fantasya.Atlantis.Regions");
		LoadPackage("Units", Unit.class, "de.x8bit.Fantasya.Atlantis.Units");
		LoadPackage("Spells", Spell.class, "de.x8bit.Fantasya.Atlantis.Spells");
		LoadPackage("Effects", Effect.class, "de.x8bit.Fantasya.Atlantis.Effects");
	}

	private static Map<String, List<Paket>> packages = new HashMap<String, List<Paket>>();
	
	@SuppressWarnings("rawtypes")
	private static void LoadPackage(String name, Class baseClass, String basePackage) {
		StringBuilder sb = new StringBuilder();
		sb.append(" - lade ").append(name).append(" aus ").append(basePackage);

		List<Paket> alp = new ArrayList<Paket>();
		packages.put(name, alp);

		List<Class> classes = Paket.FindClasses(baseClass, basePackage);
		
		try {
			for (Class c : classes) {
				if (c.isInterface()) {
					continue;
				}
				Paket p = new Paket();
				p.ClassName = c.getSimpleName();
				//System.out.println(p.ClassName);
				p.Klasse = (Atlantis) c.newInstance();
				alp.add(p);
			}
			
			sb.append(" -> " + alp.size() + " Klassen.");
			// new SysMsg(sb.toString());
		} catch(Exception ex) {
			new SysErr("Fehler beim Laden des Klassen-Package '" + name + "'.");
			new BigError(ex);
		}
	}

    @SuppressWarnings("rawtypes")
	private static List<Class> FindClasses(Class baseClass, String basePackage) {
        List<Class> klassen = new ArrayList<Class>();

        try {
            klassen = PackageLister.getClasses(basePackage);

            for (Class c : klassen) {
                // es gibt eine ganze Reihe Klassen, die wir gar nicht erst als Spielobjekte in Betracht ziehen:
                if ((c.getModifiers() & Modifier.ABSTRACT) != 0) continue;
                if ((c.getModifiers() & Modifier.INTERFACE) != 0) continue;
                if (c.isMemberClass()) continue;

				Class[] ancestors = c.getClasses();
				boolean found = false;
				for (Class ancestor : ancestors) {
					if (ancestor.equals(baseClass)) {
						found = true;
						break;
					}
				}

				if (!found) continue;

				klassen.add(c);
            }

        } catch (URISyntaxException ex) {
            new BigError(ex);
        } catch (IOException ex) {
            new BigError(ex);
        } catch (ClassNotFoundException ex) {
            new BigError(ex);
        } catch (IllegalArgumentException ex) {
            new BigError(ex);
        } catch (SecurityException ex) {
            new BigError(ex);
        }

		return klassen;
    }


	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private static Paket FindClass(String paket, String name)
	{
		List<Paket> alp = packages.get(paket);
		name = name.toLowerCase();
		for(int i = 0; i < alp.size(); i++)
		{
			Paket p = alp.get(i);
			if (p.ClassName.toLowerCase().equals(name)) return p;
			if (p.Klasse instanceof Item)
			{
				// bei Items noch Einzahl & Mehrzahl testen
				Item item = (Item) p.Klasse;
				int anzahl = item.getAnzahl();
				// - Einzahl
				item.setAnzahl(1);
				if (item.getName().toLowerCase().equals(name)) return p;
				// - Mehrzahl
				item.setAnzahl(2);
				if (item.getName().toLowerCase().equals(name)) return p;
				// - Reset
				item.setAnzahl(anzahl);
			}
		}
		
		return null;
	}
	
	public static Paket FindItem(String name) { return FindClass("Items", name); }
	public static Paket FindSkill(String name) { return FindClass("Skills", name); }
	public static Paket FindBuilding(String name) { return FindClass("Buildings", name); }
	public static Paket FindShip(String name) { return FindClass("Ships", name); }
	public static Paket FindRegion(String name) { return FindClass("Regions", name); }
	public static Paket FindUnit(String name) { return FindClass("Units", name); }
	public static Paket FindSpell(String name) { return FindClass("Spells", name); }
		
	public static List<Paket> getPaket(String name) { return packages.get(name); }
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/** Name der Klasse (e.g. HelmDer7Winde) */
	public String ClassName;
	
	/** eine Instanz dieser Klasse (zum Zugriff auf alles öffentliche) */
	public Atlantis Klasse;

}
