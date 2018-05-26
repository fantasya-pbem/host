package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author hapebe
 */
public class SchiffeTreiben extends EVABase implements NotACommand {

	public SchiffeTreiben()	{
		super(""); // Meldung kommt in PreAction()
	}

	@Override
	public void PreAction() {
		Set<Ship> leereSchiffe = new HashSet<Ship>();
		for (Ship s : Ship.PROXY) if (s.getOwner() == 0) leereSchiffe.add(s);

		int cnt = 0;
		for (Ship s : leereSchiffe) {
			Region r = Region.Load(s.getCoords());
			if (!(r instanceof Ozean)) continue;

			// okay, das Schiff ist leer und auf dem Wasser
			List<Region> kandidaten = new ArrayList<Region>();
			for (Region n : r.getNachbarn()) {
				if ((n instanceof Ozean) || n.istBetretbar(null)) kandidaten.add(n);
			}

			if (kandidaten.isEmpty()) {
				new SysErr("Treibendes Schiff " + s + " hat keinerlei Ausweg aus " + r + "?");
				continue;
			}

			Collections.shuffle(kandidaten);
			Region neu = kandidaten.get(0);

			// Küste:
			Richtung ri = neu.getCoords().getRichtungNach(r.getCoords());

			Ship.PROXY.remove(s);
			r.getShips().remove(s); // TODO evtl. auf neue PROXY-Implementierung anpassen.

			s.setCoords(neu.getCoords());
			if (neu.istBetretbar(null)) s.setKueste(ri);

			Ship.PROXY.add(s);
			neu.getShips().add(s); // TODO evtl. auf neue PROXY-Implementierung anpassen.

			cnt ++;
		}
		if (cnt > 0) new ZATMsg(cnt + " Schiffe treiben auf dem Wasser.");
	}

	@Override
	public void PostAction() {	}

	@Override
	public boolean DoAction(Unit u, String[] befehl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void DoAction(Region r, String befehl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void DoAction(Einzelbefehl eb) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
