package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Messages.Greetings;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

public class NMRs extends EVABase implements NotACommand {
	public NMRs() {
		super("Lösche Spieler, die sich nicht mehr melden.");
	}
	
	@Override
	public void PostAction() {
		for(Partei partei : Partei.PROXY) {
			if (partei.getNMR() > (GameRules.getRunde() - 3)) {
				if (partei.getNMR() == (GameRules.getRunde() - 2)) {
					new Greetings(
							partei,
							"ACHTUNG, du hast bereits zweimal keine Befehle für deine Partei \n" +
							"eingeschickt. Bitte mach das diesmal unbedingt, wenn du die \n" +
							"Löschung deiner Partei verhindern möchtest! \n"
					);
				}
				continue; // jedenfalls nicht löschen
			}
			
			new SysMsg(" - lösche Volk " + partei + ".");
			// einfach die Anzahl der Personen auf 0 setzen ... den Rest macht das System!
			for(Unit unit : Unit.CACHE.getAll(partei.getNummer())) {
				unit.setPersonen(0);
			}
		}
	}

	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	@Override
	public void DoAction(Region r, String befehl) { }
	@Override
	public void PreAction() { }
    @Override
    public void DoAction(Einzelbefehl eb) { }

}
