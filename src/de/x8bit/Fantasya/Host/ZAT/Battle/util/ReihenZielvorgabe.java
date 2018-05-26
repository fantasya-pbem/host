package de.x8bit.Fantasya.Host.ZAT.Battle.util;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import java.util.Set;

/**
 *
 * @author hb
 */
public class ReihenZielvorgabe implements Zielvorgabe {
    final private int reihe;

    public ReihenZielvorgabe(int reihe) {
        this.reihe = reihe;
    }

    @Override
    public int getReihe() {
        return reihe;
    }

    @Override
    public Set<Unit> getOpfer() {
        throw new UnsupportedOperationException("Eine ReihenZielvorgabe kennt kein getOpfer().");
    }

    @Override
    public String beschreiben() {
        if (getReihe() == Krieger.REIHE_VORNE) return "zielt auf die vordere Reihe der Feinde";
        if (getReihe() == Krieger.REIHE_HINTEN) return "zielt auf die hintere Reihe der Feinde";
        return "??? unbekannte Reihen-Zielvorgabe " + getReihe() + " ???";
    }

}
