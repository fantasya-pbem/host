package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hb
 */
public class Buendnisfall {

    final Map<Partei, Set<Partei>> angriffe = new HashMap<Partei, Set<Partei>>();
    
    private Buendnisfall() {
    }

    public Set<Partei> getInvolvierte() {
        Set<Partei> retval = new HashSet<Partei>();
        retval.addAll(getAngreifer());
        retval.addAll(getVerteidiger());
        return retval;
    }

    public Set<Partei> getAngreifer() {
        Set<Partei> retval = new HashSet<Partei>();
        retval.addAll(angriffe.keySet());
        return retval;
    }

    public Set<Partei> getVerteidiger() {
        Set<Partei> retval = new HashSet<Partei>();
        for (Partei p : angriffe.keySet()) {
            retval.addAll(angriffe.get(p));
        }
        return retval;
    }

    /**
     * die Angriffe "flippen", so dass eine Auflistung der Feinde entsteht, gegen die sich jede Partei verteidigen muss
     * @return Partei [A] -> Set aller Parteien, die [A] angreifen
     */
    public Map<Partei, Set<Partei>> getVerteidigungen() {
        Map<Partei, Set<Partei>> verteidigungen = new HashMap<Partei, Set<Partei>>();
        for (Partei p : getVerteidiger()) {
            for (Partei angreifer : angriffe.keySet()) {
                if (angriffe.get(angreifer).contains(p)) {
                    if (!verteidigungen.keySet().contains(p)) {
						verteidigungen.put(p, new HashSet<Partei>());
					}
                    verteidigungen.get(p).add(angreifer);
                }
            }
        }
        return verteidigungen;
    }


    public Set<Buendnisfall> vereinzeln() {
        List<Buendnisfall> einzeln = new ArrayList<Buendnisfall>();

        // erstmal alle Angriffe einzeln "herstellen":
        for (Partei p : this.angriffe.keySet()) {
            Buendnisfall b = new Buendnisfall();
            Set<Partei> gegner = this.angriffe.get(p);
            b.angriffe.put(p, gegner);

            einzeln.add(b);
        }

        new Debug("Vereinzeln #1:\n" + StringUtils.aufzaehlung(einzeln));

        // jetzt schauen, ob die einzelnen Fälle völlig disjunkt sind.
        boolean weitersuchen = true;
        while (weitersuchen) {
            weitersuchen = false;
            int kombi1 = -1; int kombi2 = -1;
            for (int i = 0; i < einzeln.size(); i++) {
                for (int j = i + 1; j < einzeln.size(); j++) {
                    Set<Partei> setI = einzeln.get(i).getInvolvierte();
                    Set<Partei> setJ = einzeln.get(j).getInvolvierte();

                    for (Partei p : setI) {
                        if (setJ.contains(p)) {
                            // Überlappung gefunden - zusammenlegen!

                            new Debug(einzeln.get(i) + " mit " + einzeln.get(j) + "zusammenlegen.");

                            weitersuchen = true;
                            kombi1 = i;
                            kombi2 = j;

                            break;
                        }
                    }

                    if (weitersuchen) break;
                }
                if (weitersuchen) break;
            }

            // zwei "überlappende" Konflikte zusammenlegen:
            if (kombi1 > -1) {
                einzeln.get(kombi1).angriffe.putAll(einzeln.get(kombi2).angriffe);
                einzeln.remove(kombi2);

                new Debug("Zusammengelegt: " + einzeln.get(kombi1));
            }
        }

        new Debug("Vereinzeln #2:\n" + StringUtils.liste(einzeln));


        // jetzt kann es noch sein, dass die gleiche Partei auf beiden Seiten eines komplexen Angriffs auftaucht,
        // bspw: A -> B, B -> C, C -> A
        for (Buendnisfall b : einzeln) {
            List<Partei> alle = new ArrayList<Partei>();
            alle.addAll(b.getInvolvierte());

            Collections.shuffle(alle);
            for (Partei sagMirWoDuStehst : alle) {
                Set<Partei> a = b.getAngreifer();
                Set<Partei> d = b.getVerteidiger();
                if (a.contains(sagMirWoDuStehst) && d.contains(sagMirWoDuStehst)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(sagMirWoDuStehst).append(" sind etwas verwirrt - sie entscheiden sich für die Seite der ");
                    if (Math.random() < 0.5) {
                        // von den Angreifern entfernen:
                        b.angriffe.remove(sagMirWoDuStehst);
                        sb.append("Verteidiger.");
                        // bei den Verteidigern hinzufügen:
                        for (Partei angreifer : b.angriffe.keySet()) {
                            b.angriffe.get(angreifer).add(sagMirWoDuStehst);
                        }
                    } else {
                        // von den Verteidigern entfernen:
                        for (Partei angreifer : b.angriffe.keySet()) {
                            b.angriffe.get(angreifer).remove(sagMirWoDuStehst);
                        }
                        // zu den Angreifern hinzufügen:
                        b.angriffe.put(sagMirWoDuStehst, b.getVerteidiger());
                        sb.append("Angreifer.");
                    }
                    new Debug(sb.toString());

                    // jetzt mögliche "leere" Angriffe herausziehen:
                    Set<Partei> loeschset = new HashSet<Partei>();
                    for (Partei p : b.angriffe.keySet()) {
                        if (b.angriffe.get(p).isEmpty()) loeschset.add(p);
                    }
                    for (Partei p : loeschset) b.angriffe.remove(p);
                    
                    new Debug("Neuer Stand:\n" + b);
                }
            }

        }




        Set<Buendnisfall> retval = new HashSet<Buendnisfall>();
        retval.addAll(einzeln);
        return retval;
    }

    /**
     * 
     * @param eAngriffe
     * @return
     */
    public static Buendnisfall ausEinheitenAngriffen(Map<Unit, Set<Unit>> eAngriffe) {
        Buendnisfall retval = new Buendnisfall();
        
        // Parteiangriffe auflisten:
        for (Unit attacker : eAngriffe.keySet()) {
            Partei a = Partei.getPartei(attacker.getOwner());
            for (Unit defender : eAngriffe.get(attacker)) {
                Partei d = Partei.getPartei(defender.getOwner()); // Tarnpartei statt Owner?
                if (!retval.angriffe.keySet().contains(a)) retval.angriffe.put(a, new HashSet<Partei>());
                retval.angriffe.get(a).add(d);
            }
        }

        // Gegenseitige Angriffe finden und entscheiden, wer wen angreift:
        retval.klaereGegenseitigeAngriffe();

        // den Angegriffenen stehen ihre Alliierten bei:
        for (Partei a : retval.angriffe.keySet()) {
            Set<Partei> helfer = new HashSet<Partei>();
            for (Partei b : retval.angriffe.get(a)) {
                Set<Partei> alliierteTheoretisch = new HashSet<Partei>();
                for (Partei maybe : Partei.PROXY) {
                    if (maybe.hatAllianz(b.getNummer(), AllianzOption.Kaempfe)) alliierteTheoretisch.add(maybe);
                }

                Set<Partei> alliiertePraktisch = new HashSet<Partei>();
                for (Partei maybe : alliierteTheoretisch) {
                    // falls der Allierte uns direkt angreift, wird das nix.
                    if (retval.angriffe.containsKey(maybe)) {
                        if (retval.angriffe.get(maybe).contains(b)) {
                            // tja...
                            continue;
                        }
                    }
                    new Debug(maybe  + " steht " + b + " gegen den Angriff von " + a + " zur Seite.");
                    alliiertePraktisch.add(maybe);
                }

                helfer.addAll(alliiertePraktisch);
            }

            retval.angriffe.get(a).addAll(helfer);
        }

        // und nochmal: Gegenseitige Angriffe finden und entscheiden, wer wen angreift:
        retval.klaereGegenseitigeAngriffe();

        return retval;
    }
    
    private void klaereGegenseitigeAngriffe() {
        boolean weitersuchen = true;
        while (weitersuchen) {
            weitersuchen = false;
            Partei gegenseitig1 = null;
            Partei gegenseitig2 = null;
            for (Partei a : angriffe.keySet()) {
                for (Partei b : angriffe.get(a)) {
                    if (!angriffe.containsKey(b)) continue; // b greift niemanden an.

                    if (angriffe.get(b).contains(a)) {
                        // die greifen sich gegenseitig an!
                        gegenseitig1 = a;
                        gegenseitig2 = b;
                        weitersuchen = true;
                        break;
                    }
                }
                if (weitersuchen) break;
            }

            // Keine gefunden?
            if ((gegenseitig1 == null) || (gegenseitig2 == null)) break;
            
            if (Math.random() < 0.5) {
                // tauschen:
                Partei tmp = gegenseitig1;
                gegenseitig1 = gegenseitig2;
                gegenseitig2 = tmp;
            }
            new Debug(gegenseitig1 + " und " + gegenseitig2 + " versuchen sich gegenseitig anzugreifen, " + gegenseitig1 + " ist schneller entschlossen.");

            // den Angriff von B auf A streichen:
            angriffe.get(gegenseitig2).remove(gegenseitig1);

            // weitersuchen, falls diesmal ein "Paar" gefunden wurde.
        }

        // jetzt mögliche "leere" Angriffe herausziehen:
        Set<Partei> loeschset = new HashSet<Partei>();
        for (Partei p : angriffe.keySet()) {
            if (angriffe.get(p).isEmpty()) loeschset.add(p);
        }
        for (Partei p : loeschset) angriffe.remove(p);

    }

    /**
     * Die Ausgabe ist nicht für Spieler geeignet (Partei-Tarnung...).
     * @return Beschreibung dieser Kampfkonstellation
     */
    @Override
    public String toString() {
		if (this.angriffe.keySet().isEmpty()) return "Kein Bündnisfall";
		
        StringBuilder sb = new StringBuilder();
        for (Partei angreifer : this.angriffe.keySet()) {
            sb.append(angreifer).append(" attackiert ");
            sb.append(StringUtils.aufzaehlung(this.angriffe.get(angreifer)));
            sb.append("\n");
        }
        sb.delete(sb.length() - 1, sb.length()); // letzten LF entfernen
        return sb.toString();
    }

}
