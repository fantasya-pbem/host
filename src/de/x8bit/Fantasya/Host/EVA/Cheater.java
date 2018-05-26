package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.Host.EVA.util.ItemHint;
import de.x8bit.Fantasya.Host.EVA.util.SkillHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.util.Codierung;

public class Cheater extends EVABase
{
	public Cheater() {
		super("set", "Cheater an die Wand");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    /**
     * hat (noch?) keine Keyword-Zuordnung - wird derzeit eh nur fürs Syntax-Highlighting benutzt,
     * und da sollen die Cheat-Befehle keins bekommen. (?) hapebe 2011-04-19
     * @return
     */
    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
		List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Cheater.class, 1, "^@?(set) (person)(en)? [0-9]+([ ]+(\\/\\/).*)?", "s", Art.KURZ);
		bm.addHint(new AnzahlHint(2));
		retval.add(bm);

        bm = new BefehlsMuster(Cheater.class, 2, "^@?(set) (item)(s)? [a-z]+ [0-9]+([ ]+(\\/\\/).*)?", "s", Art.KURZ);
		bm.addHint(new ItemHint(2));
		bm.addHint(new AnzahlHint(3));
		retval.add(bm);
		
        bm = new BefehlsMuster(Cheater.class, 3, "^@?(set) ((skill)|(talent)) [a-z]+ [0-9]+([ ]+(\\/\\/).*)?", "s", Art.KURZ);
		bm.addHint(new SkillHint(2));
		bm.addHint(new AnzahlHint(3));
		retval.add(bm);

        bm = new BefehlsMuster(Cheater.class, 4, "^@?(set) ((faction)|(party)|(volk)|(partei)) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "s", Art.KURZ);
		bm.addHint(new IDHint(2));
		retval.add(bm);

        bm = new BefehlsMuster(Cheater.class, 5, "^@?(set) ((ship)|(schiff)) [a-z]+([ ]+(\\/\\/).*)?", "s", Art.KURZ);
		retval.add(bm);

        bm = new BefehlsMuster(Cheater.class, 6, "^@?(set) ((building)|(gebaeude)) [a-z]+([ ]+(\\/\\/).*)?", "s", Art.KURZ);
		retval.add(bm);

        return retval;
    }
	
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			Partei p = Partei.getPartei(eb.getUnit().getOwner());
            Unit u = eb.getUnit();
            int variante = eb.getVariante();

            // im Beta-Spiel sind Cheats immer erlaubt
            if (Datenbank.isBetaGame()) p.setCheats(1);
            
            if (p.getCheats() <= 0) {
                eb.setError();
                new Fehler(u + " - Cheats sind für Dich nicht aktiviert!", u);
                continue;
            }

            // COMMAND SET PERSON <anzahl>
            if (variante == 1) {
                u.setPersonen(eb.getAnzahl());
            }

            // COMMAND SET ITEM <item> <anzahl>
            if (variante == 2) {
                String itemName = eb.getTokens()[2];
                Class<? extends Item> item = Item.getFor(itemName.toLowerCase());

                if (item == null) {
                    eb.setError();
                    new Fehler(u + " - Item '" + itemName + "' nicht gefunden.", u);
                    continue;
                }

                u.setItem(item, eb.getAnzahl());
            }

            // COMMAND SET SKILL <skill> <anzahl>
            if (variante == 3) {
                String skillName = eb.getTokens()[2];
                Class<? extends Skill> skill = Skill.getFor(skillName.toLowerCase());

                if (skill == null) {
                    eb.setError();
                    new Fehler(u + " - Talent '" + skillName + "' nicht gefunden.", u);
                    continue;
                }

                u.setSkill(skill, eb.getAnzahl() * u.getPersonen());
            }

            // COMMAND SET VOLK <nummer>
            if (variante == 4) {
                int nummer = -1;
                try { nummer = Codierung.fromBase36(eb.getTargetId()); } catch(Exception ex) { /* nüschts */ }
                if (nummer == -1) {
                    eb.setError();
                    new Fehler(u + " - ID '" + eb.getTargetId() + "' nicht erkannt.", u);
                    continue;
                }

                Partei newP = Partei.getPartei(nummer);
                if (newP == null) {
                    eb.setError();
                    new Fehler(u + " - das Volk '" + eb.getTargetId() + "' existiert nicht.", u);
                    continue;
                }

                u.setOwner(nummer);
                new Info("Du erhältst " + u + " durch Cheats von " + p + ".", newP);
            }

            // COMMAND SET SHIP <schiff>
            if (variante == 5) {
                Paket pak = Paket.FindShip(eb.getTokens()[2]);
                if (pak == null) {
                    eb.setError();
                    new Fehler(u + " - der Schiffstyp '" + eb.getTokens()[2] + "' ist unbekannt.", u);
                    continue;
                }

                Ship ship = Ship.Create(pak.Klasse.getClass().getSimpleName(), u.getCoords());
                ship.setGroesse(ship.getConstructionSize());
                ship.setFertig(true);
                if (u.getSchiff() == 0 && u.getGebaeude() == 0) u.Enter(ship);
            }

            // COMMAND SET BUILDING <gebäude>
            if (variante == 6) {
                Paket pak = Paket.FindBuilding(eb.getTokens()[2]);
                if (pak == null) {
                    eb.setError();
                    new Fehler(u + " - der Gebäudetyp '" + eb.getTokens()[2] + "' ist unbekannt.", u);
                    continue;
                }

                Building building = Building.Create(pak.Klasse.getClass().getSimpleName(), u.getCoords());
                building.setSize(20);	// auch wenn Steg & Co. kleiner sind
                if (u.getSchiff() == 0 && u.getGebaeude() == 0) u.Enter(building);
            }

            eb.setPerformed();
        }
    }


	public void PreAction() { }
	public void PostAction() {
		// Cheats pro Runde verwenden
		for (Partei p : Partei.PROXY) {
			if (p.getCheats() > 0) p.setCheats(p.getCheats() - 1);
		}
	}
	public boolean DoAction(Unit u, String[] befehl) { return false; }
    public void DoAction(Einzelbefehl eb) { }
	
}
