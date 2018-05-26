package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.AnimalResource;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Battle;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.ItemHint;
import de.x8bit.Fantasya.Host.EVA.util.SpellHint;
import de.x8bit.Fantasya.Host.EVA.util.UnitHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.util.Codierung;

public class Gib extends EVABase {
	public Gib() {
		super("gib", "Übergaben von allem möglichen was eine Einheit so hat - GIB");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		// Items - alle Namen auflisten:
        List<String> itemNames = new ArrayList<String>();
        // Tiere - alle Namen auflisten:
        List<String> animalNames = new ArrayList<String>();

		for (Paket p : Paket.getPaket("Items")) {
            for (String name : getNames(p)) {
                itemNames.add(name);
                if ( p.Klasse instanceof AnimalResource ) {
                    animalNames.add(name);
                }
            }
        }

		// ... und als RegEx formulieren:
        StringBuilder regEx = new StringBuilder();
        regEx.append("(");
        for (String name : itemNames) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(" + name + ")");
        }
        regEx.append(")");

        // Variante 1 - Item ohne Anzahl
		bm = new BefehlsMuster(Gib.class, 1,
				"^@?(gib) [a-z0-9]{1,4} (\")?" + regEx + "(\")?([ ]+(\\/\\/).*)?",
				"g", Art.KURZ); // GIB <einheit> <item>
		bm.addHint(new UnitHint(1));
		bm.addHint(ItemHint.LAST);
        bm.setKeywords("gib");
		retval.add(bm);
		bm = new BefehlsMuster(Gib.class, 1,
				"^@?(liefere) [a-z0-9]{1,4} (\")?" + regEx + "(\")?([ ]+(\\/\\/).*)?",
				"l", Art.KURZ); // LIEFERE <einheit> <item>
		bm.addHint(new UnitHint(1));
		bm.addHint(ItemHint.LAST);
        bm.setKeywords("liefere");
		bm.setKeep(true);
		retval.add(bm);

        bm = new BefehlsMuster(Gib.class, 1 + EVABase.TEMP,
				"^@?(gib) (temp) [a-z0-9]{1,4} (\")?" + regEx + "(\")?([ ]+(\\/\\/).*)?",
				"g", Art.KURZ); // GIB <einheit> <item>
		bm.addHint(new UnitHint(1));
		bm.addHint(ItemHint.LAST);
        bm.setKeywords("gib");
		retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 1 + EVABase.TEMP,
				"^@?(liefere) (temp) [a-z0-9]{1,4} (\")?" + regEx + "(\")?([ ]+(\\/\\/).*)?",
				"l", Art.KURZ); // LIEFERE <einheit> <item>
		bm.addHint(new UnitHint(1));
		bm.addHint(ItemHint.LAST);
        bm.setKeywords("liefere");
		bm.setKeep(true);
		retval.add(bm);

        // Variante 2 - Item mit Anzahl
		bm = new BefehlsMuster(Gib.class, 2,
				"^@?(gib) [a-z0-9]{1,4} [0-9]+ (\")?" + regEx + "(\")?( permanent)?([ ]+(\\/\\/).*)?",
				"g", Art.KURZ); // GIB <einheit> <anzahl> <item>
		bm.addHint(new UnitHint(1));
		bm.addHint(new AnzahlHint(2));
		bm.addHint(new ItemHint(3));
        bm.setKeywords("gib", "permanent");
		retval.add(bm);
		bm = new BefehlsMuster(Gib.class, 2,
				"^@?(liefere) [a-z0-9]{1,4} [0-9]+ (\")?" + regEx + "(\")?([ ]+(\\/\\/).*)?",
				"l", Art.KURZ); // LIEFERE <einheit> <anzahl> <item>
		bm.addHint(new UnitHint(1));
		bm.addHint(new AnzahlHint(2));
		bm.addHint(new ItemHint(3));
        bm.setKeywords("liefere");
		bm.setKeep(true);
		retval.add(bm);
		
		bm= new BefehlsMuster(Gib.class, 2 + EVABase.TEMP,
				"^@?(gib) (temp) [a-z0-9]{1,4} [0-9]+ (\")?" + regEx + "(\")?( permanent)?([ ]+(\\/\\/).*)?",
				"g", Art.KURZ); // GIB <einheit> <anzahl> <item>
		bm.addHint(new UnitHint(1));
		bm.addHint(new AnzahlHint(2));
		bm.addHint(new ItemHint(3));
        bm.setKeywords("gib", "permanent");
		retval.add(bm);
		bm= new BefehlsMuster(Gib.class, 2 + EVABase.TEMP,
				"^@?(liefere) (temp) [a-z0-9]{1,4} [0-9]+ (\")?" + regEx + "(\")?([ ]+(\\/\\/).*)?",
				"l", Art.KURZ); // LIEFERE <einheit> <anzahl> <item>
		bm.addHint(new UnitHint(1));
		bm.addHint(new AnzahlHint(2));
		bm.addHint(new ItemHint(3));
        bm.setKeywords("liefere");
		bm.setKeep(true);
		retval.add(bm);



		// ... Tiere als RegEx formulieren (diese können auch den Bauern (zurück-)gegeben werden)
        regEx = new StringBuilder();
        regEx.append("(");
        for (String name : animalNames) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(" + name + ")");
        }
        regEx.append(")");

        // Variante 3 - Tiere ohne Anzahl an die Bauern
		bm = new BefehlsMuster(Gib.class, 3,
				"^@?(gib) (bauern) " + regEx + "([ ]+(\\/\\/).*)?",
				"g", Art.KURZ); // GIB bauern <Item.isAnimal()>
        bm.addHint(new ItemHint(2));
        bm.setKeywords("gib", "bauern");
        retval.add(bm);
		bm = new BefehlsMuster(Gib.class, 3,
				"^@?(liefere) (bauern) " + regEx + "([ ]+(\\/\\/).*)?",
				"l", Art.KURZ); // GIB bauern <Item.isAnimal()>
        bm.addHint(new ItemHint(2));
        bm.setKeywords("liefere", "bauern");
		bm.setKeep(true);
        retval.add(bm);

        // Variante 4 - Tiere an die Bauern
		bm = new BefehlsMuster(Gib.class, 4,
				"^@?(gib) (bauern) [0-9]+ " + regEx + "([ ]+(\\/\\/).*)?",
				"g", Art.KURZ); // GIB bauern <Anzahl> <Item.isAnimal()>
        bm.addHint(new AnzahlHint(2));
        bm.addHint(new ItemHint(3));
        bm.setKeywords("gib", "bauern");
        retval.add(bm);
		bm = new BefehlsMuster(Gib.class, 4,
				"^@?(liefere) (bauern) [0-9]+ " + regEx + "([ ]+(\\/\\/).*)?",
				"l", Art.KURZ); // GIB bauern <Anzahl> <Item.isAnimal()>
        bm.addHint(new AnzahlHint(2));
        bm.addHint(new ItemHint(3));
        bm.setKeywords("liefere", "bauern");
		bm.setKeep(true);
        retval.add(bm);

        // Variante 5: ALLES
        bm = new BefehlsMuster(Gib.class, 5, "^@?(gib) [a-z0-9]{1,4} (alles)([ ]+(\\/\\/).*)?", "g", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("gib", "alles");
        retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 5, "^@?(liefere) [a-z0-9]{1,4} (alles)([ ]+(\\/\\/).*)?", "l", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("liefere", "alles");
		bm.setKeep(true);
        retval.add(bm);

        bm = new BefehlsMuster(Gib.class, 5 + EVABase.TEMP, "^@?(gib) (temp) [a-z0-9]{1,4} (alles)([ ]+(\\/\\/).*)?", "g", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("gib", "alles");
        retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 5 + EVABase.TEMP, "^@?(liefere) (temp) [a-z0-9]{1,4} (alles)([ ]+(\\/\\/).*)?", "l", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("liefere", "alles");
		bm.setKeep(true);
        retval.add(bm);

		
        // Variante 11: Personen
        //  GIB <einheit> <anzahl> PERSON
        bm = new BefehlsMuster(Gib.class, 11,
				"^@?(gib) [a-z0-9]{1,4} [0-9]+ (person)(en)?([ ]+(\\/\\/).*)?",
				"g", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.addHint(new AnzahlHint(2));
        bm.setKeywords("gib", "person", "personen");
        retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 11,
				"^@?(liefere) [a-z0-9]{1,4} [0-9]+ (person)(en)?([ ]+(\\/\\/).*)?",
				"l", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.addHint(new AnzahlHint(2));
        bm.setKeywords("liefere", "person", "personen");
		bm.setKeep(true);
        retval.add(bm);

        bm = new BefehlsMuster(Gib.class, 11 + EVABase.TEMP,
				"^@?(gib) (temp) [a-z0-9]{1,4} [0-9]+ (person)(en)?([ ]+(\\/\\/).*)?",
				"g", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.addHint(new AnzahlHint(2));
        bm.setKeywords("gib", "person", "personen");
        retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 11 + EVABase.TEMP,
				"^@?(liefere) (temp) [a-z0-9]{1,4} [0-9]+ (person)(en)?([ ]+(\\/\\/).*)?",
				"l", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.addHint(new AnzahlHint(2));
        bm.setKeywords("liefere", "person", "personen");
		bm.setKeep(true);
        retval.add(bm);


        // Variante 13: Komplette Entlassung - Personen an die Bauern
        bm = new BefehlsMuster(Gib.class, 13, "^@?(gib) (bauern) (person)(en)?([ ]+(\\/\\/).*)?", "g", Art.KURZ);
        bm.setKeywords("gib", "bauern", "person", "personen");
        retval.add(bm);
		bm = new BefehlsMuster(Gib.class, 13, "^@?(liefere) (bauern) (person)(en)?([ ]+(\\/\\/).*)?", "l", Art.KURZ);
        bm.setKeywords("liefere", "bauern", "person", "personen");
		bm.setKeep(true);
        retval.add(bm);

        // Variante 14: Teilentlassung - XX Personen an die Bauern
        bm = new BefehlsMuster(Gib.class, 14, "^@?(gib) (bauern) [0-9]+ (person)(en)?([ ]+(\\/\\/).*)?", "g", Art.KURZ);
        bm.addHint(new AnzahlHint(2));
        bm.setKeywords("gib", "bauern", "person", "personen");
        retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 14, "^@?(liefere) (bauern) [0-9]+ (person)(en)?([ ]+(\\/\\/).*)?", "l", Art.KURZ);
        bm.addHint(new AnzahlHint(2));
        bm.setKeywords("liefere", "bauern", "person", "personen");
		bm.setKeep(true);
        retval.add(bm);



		// Variante 21: Übergabe von Einheiten
        bm = new BefehlsMuster(Gib.class, 21, "^@?(gib) [a-z0-9]{1,4} einheit([ ]+(\\/\\/).*)?", "g", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("gib", "einheit");
        retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 21, "^@?(liefere) [a-z0-9]{1,4} einheit([ ]+(\\/\\/).*)?", "l", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("liefere", "einheit");
		bm.setKeep(true);
        retval.add(bm);

		// Variante 22: Übergabe von Einheiten an die Bauern
        bm = new BefehlsMuster(Gib.class, 22, "^@?(gib) (bauern) einheit([ ]+(\\/\\/).*)?", "g", Art.KURZ);
        bm.setKeywords("gib", "bauern", "einheit");
        retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 22, "^@?(liefere) (bauern) einheit([ ]+(\\/\\/).*)?", "l", Art.KURZ);
        bm.setKeywords("liefere", "bauern", "einheit");
		bm.setKeep(true);
        retval.add(bm);




		// Variante 31: Komplettes Zauberbuch
        bm = new BefehlsMuster(Gib.class, 31, "^@?(gib) [a-z0-9]{1,4} (zauberbuch)([ ]+(\\/\\/).*)?", "g", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("gib", "zauberbuch");
        retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 31, "^@?(liefere) [a-z0-9]{1,4} (zauberbuch)([ ]+(\\/\\/).*)?", "l", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("liefere", "zauberbuch");
		bm.setKeep(true);
        retval.add(bm);

        bm = new BefehlsMuster(Gib.class, 31 + EVABase.TEMP, "^@?(gib) (temp) [a-z0-9]{1,4} (zauberbuch)([ ]+(\\/\\/).*)?", "g", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("gib", "zauberbuch");
        retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 31 + EVABase.TEMP, "^@?(liefere) (temp) [a-z0-9]{1,4} (zauberbuch)([ ]+(\\/\\/).*)?", "l", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("liefere", "zauberbuch");
		bm.setKeep(true);
        retval.add(bm);


        // einzelne Sprüche übergeben
        // ... als RegEx formulieren:
        regEx = new StringBuilder();
        regEx.append("(");
        for (Paket p:Paket.getPaket("Spells")) {
            String name = p.Klasse.getName().toLowerCase();
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(" + name + ")");
        }
        regEx.append(")");

        // Variante 32: Einzelner Spruch
        bm = new BefehlsMuster(Gib.class, 32,
				"^@?(gib) [a-z0-9]{1,4}( zauberbuch)? (\")?" + regEx +"(\")?([ ]+(\\/\\/).*)?",
				"g", Art.KURZ);
		bm.addHint(new UnitHint(1));
		bm.addHint(SpellHint.LAST);
        bm.setKeywords("gib", "zauberbuch");
		retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 32,
				"^@?(liefere) [a-z0-9]{1,4}( zauberbuch)? (\")?" + regEx +"(\")?([ ]+(\\/\\/).*)?",
				"l", Art.KURZ);
		bm.addHint(new UnitHint(1));
		bm.addHint(SpellHint.LAST);
        bm.setKeywords("liefere", "zauberbuch");
		bm.setKeep(true);
		retval.add(bm);

        bm = new BefehlsMuster(Gib.class, 32 + EVABase.TEMP,
				"^@?(gib) (temp) [a-z0-9]{1,4}( zauberbuch)? (\")?" + regEx +"(\")?([ ]+(\\/\\/).*)?",
				"g", Art.KURZ);
		bm.addHint(new UnitHint(1));
		bm.addHint(SpellHint.LAST);
        bm.setKeywords("gib", "zauberbuch");
		retval.add(bm);
        bm = new BefehlsMuster(Gib.class, 32 + EVABase.TEMP,
				"^@?(liefere) (temp) [a-z0-9]{1,4}( zauberbuch)? (\")?" + regEx +"(\")?([ ]+(\\/\\/).*)?",
				"l", Art.KURZ);
		bm.addHint(new UnitHint(1));
		bm.addHint(SpellHint.LAST);
        bm.setKeywords("liefere", "zauberbuch");
		bm.setKeep(true);
		retval.add(bm);


        return retval;
    }
	
	@Override
	public void PostAction() { }
	@Override
	public void PreAction() { }
	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	
//	private void GibAlles(Unit u, Unit partner)
//	{
//		// COMMAND GIB <einheit> ALLES
//		// hier wird jetzt halt alles übergeben ... außer Personen
//		for(Item it : u.getItems()) {
//			it.Gib(u, partner, it.getAnzahl());
//		}
//	}

	private boolean GibZauber(Unit mage, Unit partner, List<Class<? extends Spell>> spells) {
		if (!mage.hatKontakt(partner, AllianzOption.Gib)) {
			new Fehler(mage + " hat keinen Kontakt zu " + partner + ".", mage);
			return false;
		}

		if (mage.Talentwert(Magie.class) == 0) {
			new Fehler(mage + " hat keine Ahnung wie man zaubert!", mage);
			return false;
		}
		if (partner.Talentwert(Magie.class) == 0) {
			new Fehler(partner + " hat keine Ahnung wie man zaubert!", partner);
			return false;
		}

		for(Class<? extends Spell> spellClass : spells) {
			try {
				Spell spell = spellClass.newInstance();
                // new Debug("GIB ZAUBER " + spell.getName() + " " + mage + " > " + partner);
				if (!spell.canUsedBy(mage))	{
					new Fehler(mage + " kann mit dem Zauberspruch '" + spell + "' nichts anfangen.", mage, mage.getCoords());
					continue;
				}
				if (spell.getStufe() > partner.Talentwert(Magie.class)) {
					new Fehler(partner + " kann mit dem Zauberspruch '" + spell + "' nichts anfangen.", partner, partner.getCoords());
					continue;
				}

				Spell s = partner.getSpell(spellClass);
				if (s == null) {
					partner.setSpell(spellClass.newInstance());
					new Info(partner + " erhält den Spruch '" + spell + "'.", partner, partner.getCoords());
				} else {
					new Info(partner + " kennt den Spruch '" + spell + "' bereits.", partner, partner.getCoords());
				}
			} catch (Exception ex) {
				new BigError("Kann Zauberspruch " + spellClass.getSimpleName() + " nicht instantiieren?");
			}
		}

		return true;
	}

	private boolean GibEinheit(Unit u, Unit partner) {
		Partei empfaenger = Partei.getPartei(partner.getOwner());

		// Magier involviert?
		if (u.getSkill(Magie.class).getLerntage() > 0) {
			int vorhanden = empfaenger.getMagier().getPersonen();
			if (vorhanden + u.getPersonen() > empfaenger.getMaxMagier()) {
				if (u.getPersonen() > 1) {
					new Fehler("Spüren, dass " + empfaenger + " ihnen als Magier keine Heimat bieten kann.", u);
				} else {
					new Fehler("Spürt, dass " + empfaenger + " einem Magier keine Heimat bieten kann.", u);
				}
				return false;
			}
		}
		
		if (partner.getRasse().equals(u.getRasse())) {
			new Info(u + " wird an " + empfaenger + " übergeben.", Partei.getPartei(u.getOwner()));

            Unit.CACHE.remove(u);
            u.setOwner(partner.getOwner());
            u.setTarnPartei(partner.getTarnPartei());
            Unit.CACHE.add(u);
            
			new Info(partner + " hat die Einheit " + u + " erhalten.", partner);
			return true;
		}
		
		if (!empfaenger.getRasse().equals("Mensch")) {
			new Fehler(empfaenger + " ist kein Menschen-Volk.", u);
			return false;
		} else {
			// berechnen, ob die Empfänger die Migranten aufnehmen können:
			int personenGesamt = empfaenger.getPersonen();
			int gaeste = empfaenger.getMigranten().getPersonen();
			int moeglich = empfaenger.getMaxMigranten();

			new Debug(
					"GIB EINHEIT: " + empfaenger +
					" hat " + personenGesamt + " Personen, davon vorher "
					+ gaeste + " Migranten; insgesamt "
					+ moeglich + " Einwanderer sind möglich. "
					+ "Der Einwanderungsbewerber hat " + u.getPersonen() + " Personen."
			);

			if (gaeste + u.getPersonen() > moeglich) {
				if (moeglich > 0) {
					new Fehler(empfaenger + " kann derzeit keine zusätzlichen Fremden aufnehmen.", u);
					// für den Empfänger ist es kein Fehler - er kann ja nix dafür:
					new Info(
							partner + " - wir konnten " + u
							+ " nicht aufnehmen: Zu viele Fremde bei uns, "
							+ "derzeit " + gaeste + " von " + moeglich + " möglichen.",
							partner
					);
				} else {
					new Fehler(empfaenger + " kann derzeit keine Fremden aufnehmen.", u);
				}
				return false;
			} else {
				new Info(u + " wird an " + empfaenger + " übergeben.", Partei.getPartei(u.getOwner()));

				// int altePartei = u.getOwner();
                
				Unit.CACHE.remove(u);
				u.setOwner(partner.getOwner());
				u.setTarnPartei(partner.getTarnPartei());
				Unit.CACHE.add(u);
                
                // new Debug("Geo: " + Unit.CACHE.bei(u.getCoords()).toString());
                // new Debug("Alte Partei: " + Unit.CACHE.von(Partei.getPartei(altePartei)).toString());
                // new Debug("Neue Partei: " + Unit.CACHE.von(Partei.getPartei(u.getOwner())).toString());


				new Info(partner + " hat die Einheit " + u + " erhalten.", Partei.getPartei(u.getOwner()));
				return true;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void GibPerson(Unit u, Unit partner, int anzahl) {
		Partei empfaenger = Partei.getPartei(partner.getOwner());

		if (!partner.getRasse().equals(u.getRasse())) {
			new Fehler(partner + " kann keinen " + u.getRasse() + " aufnehmen.", u);
			new Info("Personen-Übergabe ist gescheitert: Wir können keinen " + u.getRasse() + " aufnehmen.", partner);
			return;
		}

		// Monster können beliebig viele verschiedene "Migranten" haben
		if (!empfaenger.isMonster()) {
			
			// Migranten unter sich?
			if (!partner.getRasse().equals(empfaenger.getRasse())) {
				if (empfaenger.getMaxMigranten() > 0) {
					int moeglich = empfaenger.getMaxMigranten() - empfaenger.getMigranten().getPersonen();
					if (moeglich <= 0) {
						anzahl = 0;
						new Fehler(empfaenger + " kann keine neuen Fremden aufnehmen.", u);
					} else if (anzahl > moeglich) {
						anzahl = moeglich;
						new Fehler(empfaenger + " kann nur " + anzahl + " weitere Fremde aufnehmen.", u);
					}
				} else {
					new Fehler(empfaenger + " kann keinen " + u.getRasse() + " aufnehmen.", u);
					return;
				}
			}
		}
		
		if (u.getPersonen() < anzahl) {
			new Fehler(u + " kann nur " + u.getPersonen() + " statt " + anzahl + " Personen übergeben.", u);
			anzahl = u.getPersonen();
		}

		// würde die Übergabe das Magier-Limit sprengen?
		if (istMagier(u) || istMagier(partner)) {
			int alteMagier = 0;
			// weitere Magier in der Empfänger-Partei?
			for (Unit maybe : Unit.CACHE.getAll(partner.getOwner())) {
				if (istMagier(maybe)) {
					alteMagier += maybe.getPersonen();
				}
			}

			int neueMagier = 0;
			// Uebergabe innerhalb einer Partei
			if (istMagier(u) && !istMagier(partner)) {
				neueMagier = partner.getPersonen();
			}
			if (!istMagier(u) && istMagier(partner)) {
				neueMagier = u.getPersonen();
			}
			// Uebergabe zwischen unterschiedlichen Parteien
			if (u.getOwner() != partner.getOwner() ) {
				neueMagier = u.getPersonen();
			}
			
			if (alteMagier + neueMagier > partner.maxMagier()) {
				anzahl = partner.maxMagier() - alteMagier;
				if (anzahl <= 0) anzahl = 0;

				if (anzahl > 0) {
					new Info(u + " kann nur " + anzahl + " Personen übergeben - es wären sonst zu viele Magier bei " + Partei.getPartei(partner.getOwner()) + ".", u);
				} else {
					new Fehler(u + " kann keine Personen übergeben - " + Partei.getPartei(partner.getOwner()) + " hätte dann zu viele Magier.", u);
				}
			}
		}

		// TODO topTW
		
		if (anzahl > 0) {
			// die Skills wechseln und Trefferpunkte geben:
			List<Paket> alp = Paket.getPaket("Skills");
			for(Paket p : alp) {
				// Skill bzw. Lerntage für die alte Einheit holen
				Skill s = u.getSkill((Class<? extends Skill>) p.Klasse.getClass());
				
				// Lerntage pro Person ausrechnen
				int lerntage = s.getLerntage() / u.getPersonen();
				
				// neue Lerntage setzen (abzgl. der "verlorenen" Personen) 
				s.setLerntage((u.getPersonen() - anzahl) * lerntage);
				
				// neuen Lernentage setzen
				Skill o = partner.getSkill(s.getClass());	// Skill holen
				int lt = lerntage * anzahl;					// Lerntage berechnen
				lt += o.getLerntage();						// bereits vorhandene Lerntage dazu addieren
				o.setLerntage(lt);							// Lerntage speichern
			}
			
			// Lebenspunkte umsetzen
            int transfer = (int) (((float) u.getLebenspunkte() / (float) u.getPersonen()) * (float) anzahl);
			partner.setLebenspunkte( partner.getLebenspunkte() + transfer);
			u.setLebenspunkte(u.getLebenspunkte() - transfer);
            if (partner.getLebenspunkte() < 0) {
                throw new RuntimeException("Bei GIB PERSON ist etwas mit den Lebenspunkten schiefgegangen (empfänger hat <0 LP) - " + u + " gibt " + anzahl + " Personen an " + partner + ".");
            }
            if (u.getLebenspunkte() < 0) {
                throw new RuntimeException("Bei GIB PERSON ist etwas mit den Lebenspunkten schiefgegangen (geber hat <0 LP) - " + u + " gibt " + anzahl + " Personen an " + partner + ".");
            }
		}
		
		// die Personen wechseln
		u.setPersonen(u.getPersonen() - anzahl);
		partner.setPersonen(partner.getPersonen() + anzahl);
		
		new Info(u + " gibt " + anzahl + " Personen an " + partner + ".", u);
		new Info(partner + " erhält " + anzahl + " Personen von " + u + ".", partner);
	}
	
	/**
	 * EVA gibt etwas den Bauern
	 * @param region
	 * @param u Die gebende Einheit
	 * @param eb
	 */
	private void GibBauern(Region region, Unit u, Einzelbefehl eb, Class<? extends Item> item) {
		int anzahl = eb.getAnzahl();

		// Varianten 3 + 4: Tiere an die Bauern.
		if ((eb.getVariante() == 3) || (eb.getVariante() == 4)) {
			if (!region.istBetretbar(null)) {
				new Fehler("'" + eb.getBefehl() + "'?!?", u, u.getCoords());
				eb.setError();
				return;
			}
			// Tiere - sind Items
			int maxAnzahl = u.getItem(item).getAnzahl();
			if (eb.getVariante() == 3) anzahl = maxAnzahl;
			if (anzahl > maxAnzahl)	anzahl = u.getItem(item).getAnzahl();
			if (anzahl > 0) {
				try {
					Item it = item.newInstance();
					it.setAnzahl(anzahl);
					new Info(u + " wildert " + anzahl + " " + it.getName() + " aus.", u, u.getCoords());
					u.setItem(item, maxAnzahl - anzahl);
					region.getResource(item).setAnzahl(region.getResource(item).getAnzahl() + anzahl);

                    if ( (item == Pferd.class) || (item == Pegasus.class) ) {
                        this.logAuswildern(u, anzahl);
                    }
				} catch (Exception ex) {
					new BigError("Kann Item " + item.getSimpleName() + " nicht instantiieren?");
				}
			}
			eb.setPerformed();
			return;
		}

		
		// TODO topTW
		// Variante 13: Komplette Entlassung - Personen an die Bauern
		if (eb.getVariante() == 13) anzahl = u.getPersonen();
		// Variante 22: Ebenfalls komplette Entlassung, per GIB BAUERN EINHEIT
		if (eb.getVariante() == 22) anzahl = u.getPersonen();


		// Variante 14: Teilentlassung - XX Personen an die Bauern
		if (!region.istBetretbar(null)) {
			new Fehler("Die Leute von " + u + " weigern sich hier zu bleiben.", u, u.getCoords());
			eb.setError();
			return;
		}
		if (anzahl > u.getPersonen()) anzahl = u.getPersonen();
		if (anzahl > 0) {
			if (anzahl < u.getPersonen()) {
				new Info(anzahl + " Personen von " + u + " freuen sich auf ihr neues Zuhause in " + region.getName() + ".", u, u.getCoords());
			} else {
				new Info(u + " widmet sich ganz dem einfachen Landleben in " + region.getName() + ".", Partei.getPartei(u.getOwner()));
			}
			region.setBauern(region.getBauern() + anzahl);

			// die Skills anpassen
			List<Paket> alp = Paket.getPaket("Skills");
			for(Paket p : alp)
			{
				// Skill bzw. Lerntage für die alte Einheit holen
				@SuppressWarnings("unchecked")
				Skill s = u.getSkill((Class<? extends Skill>) p.Klasse.getClass());
				// Lerntage pro Person ausrechnen
				int lerntage = s.getLerntage() / u.getPersonen();
				// neue Lerntage setzen (abzgl. der "verlorenen" Personen) 
				s.setLerntage((u.getPersonen() - anzahl) * lerntage);
			}
			
			// Lebenspunkte umsetzen
			int lp = u.getLebenspunkte() / u.getPersonen();
			u.setLebenspunkte(lp * (u.getPersonen() - anzahl));
			
			// neuen Personen setzen
			u.setPersonen(u.getPersonen() - anzahl);
		}
		eb.setPerformed();
	}


	/**
	 * EVA - Kollektiver Selbstmord
	 * @param u - Die aktive Einheit
	 * @param region
	 * @param eb
	 * @param item
	 */
	private void GibSuicide(Region region, Unit u, Einzelbefehl eb, Class<? extends Item> item) {
		int variante = eb.getVariante();
		int anzahl = eb.getAnzahl();

		// Items - 1=ohne Anzahl, 2=mit Anzahl, 5=ALLES
		if ((variante == 1) || (variante == 2) || (variante == 5)) {
			List<Class<? extends Item>> items = new ArrayList<Class<? extends Item>>();
			
			// nur 1 bestimmtes Item:
			if ((variante == 1) || (variante == 2)) items.add(item);

			// ALLES was da ist:
			if (variante == 5) {
				for (Item it : u.getItems()) {
					items.add(it.getClass());
				}
			}

			for (Class<? extends Item> current : items)	{
                int maxAnzahl = u.getItem(current).getAnzahl();
				if ((variante == 1) || (variante == 5)) anzahl = maxAnzahl;
				if (anzahl > maxAnzahl) {
					anzahl = u.getItem(current).getAnzahl();
				}
				if (anzahl > 0) {
					try {
						Item it = current.newInstance();
						it.setAnzahl(anzahl);
						if (!(it instanceof AnimalResource)) {
							new Info(u + " wirft " + anzahl + " " + it.getName() + " weg.", u);
						} else {
							new Info(u + " entlässt " + anzahl + " " + it.getName() + ".", u);
						}
						u.setItem(current, maxAnzahl - anzahl);
					} catch (Exception ex) {
						new BigError("Kann Item " + current.getSimpleName() + " nicht instantiieren?");
					}
				}
			}
			eb.setPerformed();
		}

		// 21: GIB 0 EINHEIT
		if (variante == 21) anzahl = u.getPersonen();
		// 11: GIB 0 xxx PERSONEN
		if ((variante == 11) || (variante == 21)) {
			// Personen
			if (anzahl > u.getPersonen()) anzahl = u.getPersonen();
			if (anzahl > 0) {
				if (anzahl < u.getPersonen()) {
					new Info(anzahl + " Personen von " + u + " begehen Selbstmord.", u, u.getCoords());
				} else {
					new Info(u + " begeht Selbstmord in " + region.getName() + ".", Partei.getPartei(u.getOwner()));
				}
				u.setPersonen(u.getPersonen() - anzahl);
			}
			eb.setPerformed();
		}
        
	}


	// EVA:
	@Override
	public void DoAction(Region region, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), region.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();
//			Partei p = Partei.getPartei(u.getOwner());

			if ("liefere".equalsIgnoreCase(eb.getTokens()[0])) eb.setKeep(true);

			int variante = eb.getVariante();
			Unit targetUnit = null;
			Class<? extends Item> item = eb.getItem();
			Class<? extends Spell> spell = eb.getSpell();
			int anzahl = eb.getAnzahl();

			// ist eine TEMP-Einheit im Spiel?
			if ((eb.getVariante() & EVABase.TEMP) != 0) {
				String tempId = eb.getTargetUnit().toLowerCase();
				if (tempId.startsWith("temp ")) tempId = tempId.substring(5);

				int tempnummer = Unit.getRealNummer(tempId, u);

				if (tempnummer == 0) {
					eb.setError();
                    new Fehler(u + " - Temp-Einheit " + eb.getTargetUnit() + " nicht gefunden.", u, u.getCoords());
					continue;
				}
				eb.setTargetUnit(Codierung.toBase36(tempnummer));
				variante -= EVABase.TEMP;
			}
			if (eb.getTargetUnit() != null) {
				targetUnit = Unit.Load(Codierung.fromBase36(eb.getTargetUnit()));
			}


			// GIB bauern ...
			if ((variante == 3) || (variante == 4) || (variante == 13) || (variante == 14) || (variante==22)) {
				this.GibBauern(region, u, eb, item);
				continue;
			}

			// GIB 0 ...
			if (eb.getTargetUnit().equals("0")) {
				this.GibSuicide(region, u, eb, item);
				continue;
			}



			// okay, "normales" Übergeben an eine Einheit
			if (targetUnit == null) {
				new Fehler(u + " - kann Einheit " + eb.getTargetUnit() + " nicht finden.", u, u.getCoords());
				eb.setError();
				continue;
			}
			
			if (!targetUnit.getCoords().equals(u.getCoords())) {
				new Fehler(u + " - kann Einheit " + eb.getTargetUnit() + " nicht finden.", u, u.getCoords());
				eb.setError();
				continue;
			}

			if (!u.cansee(targetUnit)) {
				new Fehler(u + " - kann Einheit " + eb.getTargetUnit() + " nicht finden.", u, u.getCoords());
				eb.setError();
				continue;
			}

			// haben wir Kontakt?
			if (!u.hatKontakt(targetUnit, AllianzOption.Gib)) {
				new Fehler(u + " hat keinen Kontakt zu " + targetUnit + ".", u, u.getCoords());
				new Fehler(u + " hat versucht, uns etwas zu geben, aber wir haben nichts angenommen.", targetUnit, targetUnit.getCoords());
				eb.setError();
				continue;
			}

			// Belagerung checken ... wenn, dann nur innerhalb des Gebäudes erlauben
			if (u.getGebaeude() != targetUnit.getGebaeude()) {
				// ist mein Gebäude belagert?
				Building building = Building.getBuilding(u.getGebaeude());
				if (building != null) {
					Unit belagerer = building.istBelagert();
					if (belagerer != null) {
						new Battle(u + " ist in einem belagertem Gebäude.", u);
						eb.setError();
						continue;
					}
				}
				// oder das der Zieleinheit?
				building = Building.getBuilding(targetUnit.getGebaeude());
				if (building != null) {
					Unit belagerer = building.istBelagert();
					if (belagerer != null) {
						new Battle(targetUnit + " ist in einem belagertem Gebäude.", u);
						eb.setError();
						continue;
					}
				}
			}

			
			// GIB xxx Item / GIB xxx 10 Item
			if ((variante == 1) || (variante == 2)) {
				Item it = u.getItem(item);
				if (variante == 1) anzahl = it.getAnzahl();
				if (anzahl > 0) it.Gib(u, targetUnit, anzahl);
				eb.setPerformed();
				continue;
			}

			// GIB xxx ALLES
			if (variante == 5) {
				for(Item it : u.getItems()) {
					if (it.getAnzahl() > 0) {
						it.Gib(u, targetUnit, it.getAnzahl());
					}
				}
				eb.setPerformed();
				continue;
			}

			// GIB xxx 10 PERSONEN
			if (variante == 11) {
				GibPerson(u, targetUnit, anzahl);
				eb.setPerformed();
				continue;
			}

			// GIB xxx EINHEIT
			if (variante == 21) {
				if (GibEinheit(u, targetUnit)) {
					eb.setPerformed();
				} else {
					eb.setError();
				}
				continue;
			}

			// GIB xxx ZAUBERBUCH / GIB xxx "Hain der Trallala"
			if ((variante == 31) || (variante == 32)) {
				List<Class<? extends Spell>> spells = new ArrayList<Class<? extends Spell>>();

				// nur 1 bestimmtes Item:
				if (variante == 32) spells.add(spell);

				// ALLES was da ist:
				if (variante == 31) {
					for (Spell sp : u.getSpells()) {
						spells.add(sp.getClass());
					}
				}

				if (!GibZauber(u, targetUnit, spells)) {
					eb.setError();
				} else {
					eb.setPerformed();
				}
				continue;
			}
			
		} // next Einzelbefehl


		// testen auf PERMANENTen Befehl
		// for(int i = 0; i < befehl.length; i++) if (befehl[i].toLowerCase().equals("permanent")) return true;

		return;
	}

    @Override
    public void DoAction(Einzelbefehl eb) { }

    /**
     * wir merken uns, wie viele Pferde eine Partei ausgewildert hat, damit
     * sie nicht auf diesem Weg ihren Pegasus-Bestand vergrößern kann.
     * @param u
     * @param anzahl
     */
    private void logAuswildern(Unit u, int anzahl) {
        Partei p = Partei.getPartei(u.getOwner());
        int bisherAusgewildert = p.getIntegerProperty(Pferd.PROPERTY_ENTLASSEN, 0);
        p.setProperty(Pferd.PROPERTY_ENTLASSEN, bisherAusgewildert + anzahl);
    }

	/** Prueft ob eine Einheit ein Magier ist. */
	private boolean istMagier(Unit u) {
		return (u.getSkill(Magie.class).getLerntage() > 0);
	}
}
