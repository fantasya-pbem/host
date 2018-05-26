package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

/**
 * markiert Waffen, die pro Runde / Angriff noch Zusatzeffekte (Kosten) haben,
 * wie z.B. Feuerball und Feuerwalze.
 * @author hb
 */
public interface ITeureWaffe {

    /**
     * @return Anzahl der Angriffe diese Runde - aber ohne die dazugehörigen Kosten abzuziehen.
     */
    public int numberOfAttacksNurZurInfo();

}
