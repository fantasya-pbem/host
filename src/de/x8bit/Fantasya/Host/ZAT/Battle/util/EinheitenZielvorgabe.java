package de.x8bit.Fantasya.Host.ZAT.Battle.util;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.util.StringUtils;
import java.util.Set;

/**
 *
 * @author hb
 */
public class EinheitenZielvorgabe implements Zielvorgabe {
    private final Set<Unit> opfer;

    public EinheitenZielvorgabe(Set<Unit> opfer) {
        this.opfer = opfer;
    }

    @Override
    public int getReihe() {
        throw new UnsupportedOperationException("Eine EinheitenZielvorgabe kennt keine getReihe().");
    }

    @Override
    public Set<Unit> getOpfer() {
        return opfer;
    }

    @Override
    public String beschreiben() {
        return "zielt besonders auf: " + StringUtils.aufzaehlung(opfer);
    }

}
