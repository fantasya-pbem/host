package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Skills.Spionage;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;

public class Spionieren extends EVABase {

	public Spionieren()	{
		super("spioniere", "Spionage");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Spionieren.class, 0, "^@?(spioniere)[n]? [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "s", Art.LANG);
        bm.setKeywords("spioniere", "spionieren");
        retval.add(bm);

        return retval;
    }

	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }

	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			boolean successful = true;
			
			Unit agent = eb.getUnit();
			
			if (agent.Talentwert(Spionage.class) == 0) {
				new Fehler("ich sollte schon wenigstens das Talent zum Agenten haben", agent);
				eb.setError();
				continue;
			}
			
			Unit victim = null;
			
			// Well, both unit in one region?
			for (Unit possibleVictim : r.getUnits()) {
				if (possibleVictim.getNummer() == Codierung.fromBase36(eb.getTokens()[1])) {
					victim = possibleVictim;
				}
			}
			
			if (victim == null) {
				new Fehler("konnte niemanden finden den ich Ausspionieren kann", agent);
				eb.setError();
				continue;
			}
			
			Unit aufpasser = r.TopSkill(Wahrnehmung.class, victim.getOwner());
			
			if (aufpasser == null) {
				successful = true;
			}
			
			if (agent.Talentwert(Spionage.class) > aufpasser.Talentwert(Wahrnehmung.class)) {
				successful = true;
			}
			else if (agent.Talentwert(Spionage.class) == aufpasser.Talentwert(Wahrnehmung.class)) {
				successful = (Random.rnd(0, 100) < 50);
			}
			else {
				successful = false;
			}
				
			if (successful) {
				spioniere(agent, victim);
			} else {
				new Fehler("ich wurden beim Spionieren erwischt", agent);
			}	
				
			eb.setPerformed();				
        } // nächster Einzelbefehl
	}

	protected enum SpionageItem {
		DETAILS,
		SILVER,
		TALENTS,
//		HERB,
//		DEWPOND,
	}
	
	private void spioniere(Unit agent, Unit victim) {
		List<SpionageItem> spionage = new ArrayList<SpionageItem>();
		spionage.add(SpionageItem.DETAILS);
		if (victim.getItem(Silber.class).getAnzahl() > 0) spionage.add(SpionageItem.SILVER);
		if (victim.getSkills().size() > 0) spionage.add(SpionageItem.TALENTS);
		SpionageItem si = spionage.get(Random.rnd(0, spionage.size()));
		if (si.equals(SpionageItem.DETAILS)) spionageDetails(agent, victim);
		if (si.equals(SpionageItem.SILVER)) spionageSilver(agent, victim);
		if (si.equals(SpionageItem.TALENTS)) spionageTalents(agent, victim);
	}

	private void spionageTalents(Unit agent, Unit victim) {
		StringBuilder msg = new StringBuilder(victim.toString());
		msg.append(" hat folgende Fähigkeiten: ");
		for(int i = 0; i < victim.getSkills().size(); i++) {
			Skill s = victim.getSkills().get(i);
			if (s.getLerntage() == 0) continue;
			if (i != 0) msg.append(", ");
			msg.append(s.getName());
			msg.append(" [");
			msg.append(victim.Talentwert(s));
			msg.append("]");
		}
		msg.append(".");
		new de.x8bit.Fantasya.Atlantis.Messages.Spionage(msg.toString(), agent);
	}

	private void spionageSilver(Unit agent, Unit victim) {
		String msg = victim + " besitzt " + victim.getItem(Silber.class).getAnzahl() + " Silber";
		new de.x8bit.Fantasya.Atlantis.Messages.Spionage(msg, agent);
	}

	private void spionageDetails(Unit agent, Unit victim) {
		List<String> something = new ArrayList<String>();
		something.add("Gesundheit ist " + victim.strLebenspunkte());
		something.add("Kampfposition ist " + Kampfposition.PositionCR(victim.getKampfposition()));
		something.add("Rasse ist " + victim.getRasse());
		StringBuilder msg = new StringBuilder("Über " + victim + " ließ sich folgendes herausfinden: ");
		for(int i = 0; i < something.size(); i++) {
			if (i != 0) msg.append(", ");
			msg.append(something.get(i));
		}
		msg.append(".");
		new de.x8bit.Fantasya.Atlantis.Messages.Spionage(msg.toString(), agent);
	}

	@Override
	public void DoAction(Einzelbefehl eb) { }

	@Override
	public void PreAction() { }

	@Override
	public void PostAction() { }

}
