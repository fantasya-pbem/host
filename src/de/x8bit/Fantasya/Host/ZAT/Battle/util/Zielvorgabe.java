package de.x8bit.Fantasya.Host.ZAT.Battle.util;

import de.x8bit.Fantasya.Atlantis.Unit;
import java.util.Set;

/**
 * <p>Entspricht den ATTACKIERE-Varianten:</p>
 * <ul><li>ATTACKIERE <vorne>|<hinten></li>
 * <li>ATTACKIERE GEZIELT <Einheiten-ID>[|<Einheiten-ID> ... ]</li>
 * </ul>
 * @author hb
 */
public interface Zielvorgabe {

    /**
     * @return Die Kampfreihe, die bevorzugt attackiert werden soll.
     */
    public int getReihe();

    /**
     * @return Die Einheiten, die bevorzugt attackiert werden sollen.
     */
    public Set<Unit> getOpfer();

    /**
     * @return menschenlesbare Beschreibung dieser Zielvorgabe.
     */
    public String beschreiben();
}
