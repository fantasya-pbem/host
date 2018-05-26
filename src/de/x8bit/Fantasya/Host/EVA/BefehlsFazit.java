package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.comparator.LangeZuerstBefehlsComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BefehlsFazit extends EVABase implements NotACommand
{
	public final static int PERMANENT = 0;
	public final static int VOID = 1;

	public BefehlsFazit()	{
		super("*", "So, ist jetzt auch alles wie befohlen erledigt worden?");
	}

    @Override
	public void PreAction() {
		// alle Strukturbefehle (PARTEI, EINHEIT, RUNDE, NÄCHSTER ...) als erledigt markieren:
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().getAll(BefehleEinlesen.class);
		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());
			eb.setPerformed();
		}
		
		// Lehrer werden bestätigt, wenn ALLE ihre Schüler schon bestätigt sind.
		BefehleBestaetigen.LehrerBestaetigen();
		
		
		if (ZATMode.CurrentMode().isDebug()) {
			for (Unit u : Unit.CACHE) {
				if (u.getTag("ejcOrdersConfirmed") != null) {
					// für Testzwecke: Gleichbedeutenden Kommentar einfügen:
					Einzelbefehl kommentar = new Einzelbefehl(u, ";bestaetigt");
					kommentar.setKeep(true);
					u.BefehleExperimental.add(kommentar);
					BefehlsSpeicher.getInstance().add(kommentar);
					kommentar.setPerformed();
				}
			}
		}
	}

    @Override
	public void PostAction() {
		ZATMode zatMode = ZATMode.CurrentMode();

		Set<Integer> ignorierteParteien = zatMode.getIgnorierteParteiNummern();
		int ignorierteBefehle = 0;

        for (Unit u : Unit.CACHE) {
            u.Befehle.clear();

			List<Einzelbefehl> loeschliste = new ArrayList<Einzelbefehl>();

			for (Einzelbefehl eb : u.BefehleExperimental) {
				// new Debug(u + ": " + eb.toString());

				if (!eb.isPerformed()) {
                    if (eb.getUnit().getCoords().getWelt() == 0) {
                        // ah, virtuelle Einheit!
                        continue;
                    }

					if (ignorierteParteien.contains(eb.getUnit().getOwner())) {
						// tja, die sollten auch nicht...
						ignorierteBefehle ++;
						// ... aber sie behalten die alten Befehle
						u.Befehle.add(eb.getBefehlCanonical());
						continue;
					}

					Partei p = Partei.getPartei(u.getOwner());
					if (!zatMode.isDebug()) {
						new BigError("Befehl '" + eb.getBefehl() + "' (" + u + " von " + p + ") ist nicht verarbeitet worden!");
					} else {
						new SysMsg("Befehl '" + eb.getBefehl() + "' (" + u + " von " + p + ") ist nicht verarbeitet worden!");
					}
                    u.Befehle.add(eb.getBefehlCanonical());
					continue;
				} else {
                    // für Reporte und Zugvorlagen

					if (!eb.isKeep()) {
						// new Debug("Befehl nicht übernehmen: " + eb.getBefehlCanonical());
						loeschliste.add(eb);
						continue;
					}

					u.Befehle.add(eb.getBefehlCanonical());
                }
			}

			u.BefehleExperimental.removeAll(loeschliste);


			/* jetzt die Befehle sortieren - der/die langen Befehle sollen am Anfang stehen
			 * http://www.fantasya-pbem.de/taverne/viewtopic.php?f=8&t=1067 @ 2011-03-06
			 */
			Collections.sort(u.BefehleExperimental, new LangeZuerstBefehlsComparator());

			for (int i=0; i<u.BefehleExperimental.size(); i++) u.BefehleExperimental.get(i).setSortRank(i);

		}

		if (ignorierteBefehle > 0) {
			new ZATMsg(ignorierteBefehle + " Befehle von ignorierten Parteien - ignoriert.");
		}

	}
	
    @Override
    public boolean DoAction(Unit u, String[] befehl) { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public void DoAction(Region r, String befehl) {
        // nothing to do - tralla!
    }
	
    @Override
	public void DoAction(Einzelbefehl eb) { }

}
