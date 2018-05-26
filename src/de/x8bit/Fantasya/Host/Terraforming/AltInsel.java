package de.x8bit.Fantasya.Host.Terraforming;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Region;
import java.util.Map;

/**
 * Diese ProtoInsel wird verwendet, wenn die "echte" Welt bei der Schöpfung neuer
 * Kontinente (Inseln) einbezogen werden soll. Standardmäßig kopiert eine AltInsel
 * alle vorhandenen Regionen einer vorhandenen "Welt".
 *
 * @author hapebe
 */
public class AltInsel extends ProtoInsel {
	private static final long serialVersionUID = -7359667000667146462L;

	public final static String URSPRUNGS_MARKER = "URSPRUNGSREGION-ALTEWELT";

	public AltInsel(int welt) {
		this.setWelt(welt);

		boolean hatUrsprung = false;
		for (Region r : Region.CACHE.values()) {
			Coords c = r.getCoords();
			if (c.getWelt() == welt) {
				try {
					Region copy = r.getClass().newInstance();
					copy.setCoords(c);
					copy.setName(r.getName());
					copy.setAlter(r.getAlter());
					copy.setInselKennung(r.getInselKennung());
					copy.setEnstandenIn(r.getEnstandenIn());
					this.putRegion(copy);

					if ((c.getX() == 0) && (c.getY()==0)) {
						copy.setBeschreibung(URSPRUNGS_MARKER);
						hatUrsprung = true;
					}
				} catch (InstantiationException ex) {
					new BigError(ex);
				} catch (IllegalAccessException ex) {
					new BigError(ex);
				}
			}
		}
		if (!hatUrsprung) throw new RuntimeException("AltInsel enthält keine Region bei (0, 0)!");
	}

	@Override
	public void create() {
		throw new UnsupportedOperationException("Nicht anwendbar!");
	}

	@Override
	public Map<Class<? extends Atlantis>, Double> getTerrainProbabilities() {
		throw new UnsupportedOperationException("Nicht anwendbar!");
	}

	@Override
	protected int rndGroesse() {
		throw new UnsupportedOperationException("Nicht anwendbar!");
	}

	@Override
	protected int rndAbstand() {
		throw new UnsupportedOperationException("Not supported yet.");
	}


}
