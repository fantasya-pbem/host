package de.x8bit.Fantasya.Host.ZAT.Battle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Unit;

/**
 *
 * @author hb
 */
public class KampfrundenStatistik {

    Map<Integer, Integer> angriffsZahl = new HashMap<Integer, Integer>();
    Map<Integer, Integer> erfolgreicheAngriffsZahl = new HashMap<Integer, Integer>();
    Map<Integer, Integer> aktiverSchaden = new HashMap<Integer, Integer>();
    Map<Integer, Integer> killZahl = new HashMap<Integer, Integer>();

    Map<Integer, Integer> verteidigungsZahl = new HashMap<Integer, Integer>();
    Map<Integer, Integer> erfolgreicheVerteidigungsZahl = new HashMap<Integer, Integer>();
    Map<Integer, Integer> erlittenerSchaden = new HashMap<Integer, Integer>();
    Map<Integer, Integer> verlusteZahl = new HashMap<Integer, Integer>();

    public KampfrundenStatistik() { }

    /**
     * registriert den erfolgreichen Angriff von a auf d
     * @param a Angreifer
     * @param d Verteidiger
     * @param schaden erzielte Trefferpunkte
     */
    public void recordSchlag(Krieger a, Krieger d, int schaden) {
        int ida = a.getUnit().getNummer();
        int idd = d.getUnit().getNummer();
        
        if (!angriffsZahl.containsKey(ida)) angriffsZahl.put(ida, 0);
        if (!erfolgreicheAngriffsZahl.containsKey(ida)) erfolgreicheAngriffsZahl.put(ida, 0);
        if (!aktiverSchaden.containsKey(ida)) aktiverSchaden.put(ida, 0);

        if (!verteidigungsZahl.containsKey(idd)) verteidigungsZahl.put(idd, 0);
        if (!erlittenerSchaden.containsKey(idd)) erlittenerSchaden.put(idd, 0);

        angriffsZahl.put(ida, angriffsZahl.get(ida) + 1);
        erfolgreicheAngriffsZahl.put(ida, erfolgreicheAngriffsZahl.get(ida) + 1);
        aktiverSchaden.put(ida, aktiverSchaden.get(ida) + schaden);

        verteidigungsZahl.put(idd, verteidigungsZahl.get(idd) + 1);
        erlittenerSchaden.put(idd, erlittenerSchaden.get(idd) + schaden);
    }

    /**
     * registriert den geblockten Angriff von a auf d
     * @param a Angreifer
     * @param d Verteidiger
     */
    public void recordFehlschlag(Krieger a, Krieger d) {
        int ida = a.getUnit().getNummer();
        int idd = d.getUnit().getNummer();

        if (!angriffsZahl.containsKey(ida)) angriffsZahl.put(ida, 0);

        if (!verteidigungsZahl.containsKey(idd)) verteidigungsZahl.put(idd, 0);
        if (!erfolgreicheVerteidigungsZahl.containsKey(idd)) erfolgreicheVerteidigungsZahl.put(idd, 0);

        angriffsZahl.put(ida, angriffsZahl.get(ida) + 1);

        verteidigungsZahl.put(idd, verteidigungsZahl.get(idd) + 1);
        erfolgreicheVerteidigungsZahl.put(idd, erfolgreicheVerteidigungsZahl.get(idd) + 1);
    }

    /**
     * registriert die vollendete Ermordung von d durch a.
     * @param a Angreifer
     * @param d Verteidiger und Verflossener
     */
    public void recordTotschlag(Krieger a, Krieger d) {
        int ida = a.getUnit().getNummer();
        int idd = d.getUnit().getNummer();

        if(!killZahl.containsKey(ida)) killZahl.put(ida, 0);
        if(!verlusteZahl.containsKey(idd)) verlusteZahl.put(idd, 0);

        killZahl.put(ida, killZahl.get(ida) + 1);
        verlusteZahl.put(idd, verlusteZahl.get(idd) + 1);
    }

    public String getBericht(Unit u) {
        int id = u.getNummer();
        StringBuffer sb = new StringBuffer();

        sb.append(u);

        int aCnt = 0;
        int asCnt = 0;
        int aDmg = 0;
        int kill = 0;

        int dCnt = 0;
        int dsCnt = 0;
        int dDmg = 0;
        int victims = 0;

        try { aCnt = angriffsZahl.get(id); } catch (NullPointerException ex) {}
        try { asCnt = erfolgreicheAngriffsZahl.get(id); } catch (NullPointerException ex) {}
        try { aDmg = aktiverSchaden.get(id); } catch (NullPointerException ex) {}
        try { kill = killZahl.get(id); } catch (NullPointerException ex) {}

        try { dCnt = verteidigungsZahl.get(id); } catch (NullPointerException ex) {}
        try { dsCnt = erfolgreicheVerteidigungsZahl.get(id); } catch (NullPointerException ex) {}
        try { dDmg = erlittenerSchaden.get(id); } catch (NullPointerException ex) {}
        try { victims = verlusteZahl.get(id); } catch (NullPointerException ex) {}

        if (aCnt > 0) {
            if (asCnt > 0) {
                sb.append(" unternahm " + aCnt + " Angriffe, davon " + asCnt + " erfolgreich. " + aDmg + " Trefferpunkte wurden erzielt");
                if (kill > 0) {
                    sb.append(", " + kill + " Gegner wurden besiegt.");
                } else {
                    sb.append(".");
                }
            } else {
                sb.append(" versuchte " + aCnt + " mal anzugreifen.");
            }
        }
        if (dCnt > 0) {
            sb.append(" ");
            if (dsCnt > 0) {
                if (dsCnt < dCnt) {
                    sb.append("Von " + dCnt + " gegnerischen Angriffen wurden " + dsCnt + " vereitelt");
                    sb.append(", " + dDmg + " Trefferpunkte wurden erlitten");
                    if (victims > 0) {
                        sb.append(", " + victims + " Gefallene sind zu beklagen.");
                    } else {
                        sb.append(".");
                    }
                } else {
                    sb.append(dCnt + " gegnerische Angriffe wurden vereitelt.");
                }
            } else {
                sb.append(" " + dCnt + " gegnerische Angriffe waren erfolgreich, " + dDmg + " Trefferpunkte wurden erlitten");
                if (victims > 0) {
                    sb.append(", " + victims + " Gefallene sind zu beklagen.");
                } else {
                    sb.append(".");
                }
            }
        }

        return sb.toString();
    }

    public String getBericht(Side s, Gruppe g) {
        StringBuilder sb = new StringBuilder();

        Set<Integer> allUnits = new HashSet<Integer>();
        allUnits.addAll(angriffsZahl.keySet());
        allUnits.addAll(verteidigungsZahl.keySet());

        for (Unit u : g.getUnits()) {
            if (!allUnits.contains(u.getNummer())) continue; // die Einheit ist nicht in Erscheinung getreten...

            sb.append(getBericht(u)).append("\n");
        }

        return sb.toString();
    }

}
