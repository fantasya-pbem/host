package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author hb
 */
public class GruppenBuendnisfall {

    /**
     * enthält die Gruppierungen, die sich aus den Tupeln von wahrer Partei und Tarnpartei ergeben
     */
    final Map<Integer, Map<Integer, Gruppe>> gruppen = new HashMap<Integer, Map<Integer, Gruppe>>();
    
    /**
     * Nimmt die anfänglich befohlenen Kämpfe auf, damit später eingeschätzt 
     * werden kann, wie gut mögliche Varianten zur Rollenverteilung A/V mit den 
     * anfänglichen Befehlen in Einklang zu bringen sind.
     */
    final Set<GruppenPaarung> befohleneAngriffe = new HashSet<GruppenPaarung>();
    
    /**
     * Nimmt *alle* sich ergebenen Gegnerschaften auf, d.h. mit Eingreifen von Verbündeten
     */
    final Set<GruppenPaarung> resultierendeAngriffe = new HashSet<GruppenPaarung>();

    private GruppenBuendnisfall() {
    }


    public Set<GruppenKnaeuel> zusammenfassen(Set<GruppenKnaeuel> rohstoff) {
        List<GruppenKnaeuel> einzeln = new ArrayList<GruppenKnaeuel>();
        einzeln.addAll(rohstoff);

        new Debug("Zusammenfassen #1:\n" + StringUtils.aufzaehlung(einzeln));

        // jetzt schauen, ob die einzelnen Fälle völlig disjunkt sind.
        boolean weitersuchen = true;
        while (weitersuchen) {
            weitersuchen = false;
            int kombi1 = -1; int kombi2 = -1;
            
            for (int i = 0; i < einzeln.size(); i++) {
                for (int j = i + 1; j < einzeln.size(); j++) {
                    if (!einzeln.get(i).getSchnittmenge(einzeln.get(j)).isEmpty()) {
                        // Überlappung gefunden - zusammenlegen!

                        new Debug("Werden zusammengefasst:\n" + einzeln.get(i) + "\n mit \n" + einzeln.get(j) + "(Ende vom Zusammenfassen)\n\n\n");

                        weitersuchen = true;
                        kombi1 = i;
                        kombi2 = j;

                        break;
                    }
                    if (weitersuchen) break;
                }
                if (weitersuchen) break;
            }

            // zwei "überlappende" Konflikte zusammenlegen:
            if (kombi1 > -1) {
                einzeln.get(kombi1).add(einzeln.get(kombi2));
                einzeln.remove(kombi2);

                new Debug("Zusammengelegt: " + einzeln.get(kombi1));
            }
        }
        

        if (ZATMode.CurrentMode().isDebug())
            new Debug("\n\n\nZusammenfassung abgeschlossen:\n" + StringUtils.liste(einzeln));


        Set<GruppenKnaeuel> retval = new HashSet<GruppenKnaeuel>();
        retval.addAll(einzeln);
        return retval;
    }

    /**
     * 
     * @param eAngriffe
     * @return
     */
    public static Set<GruppenKonflikt> ausEinheitenAngriffen(Map<Unit, Set<Unit>> eAngriffe) {
        GruppenBuendnisfall gbf = new GruppenBuendnisfall();
        
        // alle Einheiten in Gruppen einsortieren:
        Unit somebody = eAngriffe.keySet().iterator().next();
        Region r = Region.Load(somebody.getCoords());
        for (Unit u : r.getUnits()) {
            gbf.addToGruppe(u);
        }
        
        for (Unit attacker : eAngriffe.keySet()) {
            Gruppe a = gbf.getGruppe(attacker);
            a.setAngreifer(true);
            for (Unit defender : eAngriffe.get(attacker)) {
                Gruppe v = gbf.getGruppe(defender);
                v.setVerteidiger(true);
                GruppenPaarung gp = new GruppenPaarung(a, v);
                gbf.befohleneAngriffe.add(gp);
                gbf.resultierendeAngriffe.add(gp);
            }
        }
        
        if (ZATMode.CurrentMode().isDebug()) {
            new Debug("PRIMÄRE ANGRIFFE AUF GRUPPEN-EBENE:");
            Set<Gruppe> beteiligte = new HashSet<Gruppe>();
            Set<Gruppe> unbeteiligte = new HashSet<Gruppe>();
            
            for (GruppenPaarung gp : gbf.resultierendeAngriffe) {
                beteiligte.add(gp.getA());
                beteiligte.add(gp.getB());
            }
            for (Gruppe g : gbf.getAlleGruppen()) {
                if (!beteiligte.contains(g)) unbeteiligte.add(g);
            }
            new Debug("ausEinheitenAngriffen() -");
            new Debug("Beteiligte:\n" + beteiligte.toString());
            new Debug("Unbeteiligte:\n" + StringUtils.aufzaehlung(unbeteiligte));
            
            StringBuilder sb = new StringBuilder();
            sb.append("Paarungen:\n");
            for (GruppenPaarung gp : gbf.resultierendeAngriffe) {
                sb.append(gp.toString()).append("\n");
            }
            new Debug(sb.toString());
        }
        
        // jetzt stehen allen Verteidigern ihre Verbündeten bei:
        Set<GruppenPaarung> originalAngriffe = new HashSet<GruppenPaarung>();
        originalAngriffe.addAll(gbf.resultierendeAngriffe);
        for (GruppenPaarung gp : originalAngriffe) {
            Gruppe angreifer = gp.getA();
            Gruppe verteidiger = gp.getB();
            // Beistand gibt es nur, wenn sie von der wahren Partei angefordert wird,
            // und auch nur, wenn der Hilfesuchende seine Partei zu erkennen gibt.
            if (!verteidiger.istAuthentisch()) continue;
                
            for (Gruppe kandidat : gbf.getAlleGruppen()) {
                // unsere eigenen sollten vorher schon aufgenommen worden sein:
                if (kandidat.getParteiNr() == verteidiger.getParteiNr()) continue;

                // Alliierte geben ihre Parteitarnung NICHT zugunsten der Hilfesuchenden auf -
                // das Setzen einer Tarnpartei entzieht also Einheiten ihren Allianzpflichten.
                if (!kandidat.istAuthentisch()) continue;

                Partei maybe = Partei.getPartei(kandidat.getParteiNr());
                if (maybe.hatAllianz(verteidiger.getParteiNr(), AllianzOption.Kaempfe)) {
                    // Ausnahme, wenn der Verbündete den Hilfesuchenden selbst angreift:
                    boolean aktiverAngriffVonMaybe = false;
                    for (GruppenPaarung gp2 : gbf.befohleneAngriffe) {
                        if ((gp2.getA().getParteiNr() == maybe.getNummer()) && gp2.getA().istAuthentisch()) {
                            if (gp2.getB().getParteiNr() == verteidiger.getParteiNr()) {
                                aktiverAngriffVonMaybe = true; 
                                break;
                            } 
                        }
                    }
                    if (aktiverAngriffVonMaybe) {
                        new Debug(kandidat + " steht " + verteidiger + " NICHT als Verbündeter bei, weil sie ihn direkt angreifen.");
                        continue;
                    } // tja, war wohl nix mit den Allierten...
                    
                    kandidat.setVerbuendeter(true);
                    
                    // der Alliierte möchte jetzt ganz bestimmt den "Missetäter" angreifen:
                    GruppenPaarung entschluss = new GruppenPaarung(kandidat, angreifer);
                    gbf.befohleneAngriffe.add(entschluss);
                    
                    gbf.resultierendeAngriffe.add(new GruppenPaarung(angreifer, kandidat));
                }
            }
        }
        
        if (ZATMode.CurrentMode().isDebug()) {
            new Debug("SEKUNDÄRE ANGRIFFE AUF GRUPPEN-EBENE (mit Alliierten):");
            Set<Gruppe> beteiligte = new HashSet<Gruppe>();
            Set<Gruppe> unbeteiligte = new HashSet<Gruppe>();
            
            for (GruppenPaarung gp : gbf.resultierendeAngriffe) {
                beteiligte.add(gp.getA());
                beteiligte.add(gp.getB());
            }
            for (Gruppe g : gbf.getAlleGruppen()) {
                if (!beteiligte.contains(g)) unbeteiligte.add(g);
            }
            new Debug("ausEinheitenAngriffen() -");
            new Debug("Beteiligte:\n" + beteiligte.toString());
            new Debug("Unbeteiligte:\n" + StringUtils.aufzaehlung(unbeteiligte));
            
            StringBuilder sb = new StringBuilder();
            sb.append("Paarungen:\n");
            for (GruppenPaarung gp : gbf.resultierendeAngriffe) {
                sb.append(gp.toString()).append("\n");
            }
            new Debug(sb.toString());
        }
        
        // verbundene und unabhängige Konflikte finden:
        Set<GruppenKnaeuel> detailKonflikte = new HashSet<GruppenKnaeuel>();
        for (GruppenPaarung gp : gbf.resultierendeAngriffe) {
            GruppenKnaeuel gk = gbf.new GruppenKnaeuel();
            gk.addPaar(gp);
            detailKonflikte.add(gk);
        }
        
        Set<GruppenKnaeuel> konflikte = gbf.zusammenfassen(detailKonflikte);
        if (ZATMode.CurrentMode().isDebug()) {
            new Debug("\n\n\nPRIMÄRE KONFLIKTE AUF A/V-GRUPPEN-MENGEN-EBENE:");
            for (GruppenKnaeuel gk : konflikte) {
                new Debug("Konflikt: " + gk.toString());
                
                Set<Gruppe> unbeteiligte = new HashSet<Gruppe>();
                for (Gruppe g : gbf.getAlleGruppen()) {
                    if (!gk.getInvolvierte().contains(g)) unbeteiligte.add(g);
                }
                new Debug("Unbeteiligte in diesem Konflikt:\n\n\n\n" + StringUtils.aufzaehlung(unbeteiligte));
            }
        }
        
        int i = 1;
        Set<GruppenKonflikt> retval = new HashSet<GruppenKonflikt>();
        for (GruppenKnaeuel gk : konflikte) {
            // jetzt die jeweils optimale Seitenverteilen der Angreifer und Verteidiger suchen:
            SortedSet<Gruppe> involvierte = new TreeSet<Gruppe>();
            involvierte.addAll(gk.getInvolvierte());
            
            gbf.besteAVKombinationWert = Integer.MIN_VALUE;
            gbf.besteAVKombinationAngreifer = null;
            gbf.besteAVKombinationVerteidiger = null;
            
            gbf.avKombinationenProbieren(new TreeSet<Gruppe>(), involvierte);
            
            new Debug("Beste Konstellation:\n" + gbf.beschreibeKonstellation(gbf.besteAVKombinationAngreifer, gbf.besteAVKombinationVerteidiger, gbf.besteAVKombinationWert));
            
            GruppenKonflikt k = new GruppenKonflikt();
            for (Gruppe g : gbf.besteAVKombinationAngreifer) k.getSeiteA().add(g);
            for (Gruppe g : gbf.besteAVKombinationVerteidiger) k.getSeiteB().add(g);
            // wenn die A-Seite KEINE Angreifer aufweist, dann tauschen wir die Rollen - sonst isses egal:
            boolean angreiferAufSeiteA = false;
            for (Gruppe g : k.getSeiteA()) {
                if (g.istAngreifer()) angreiferAufSeiteA = true;
            }
            if (!angreiferAufSeiteA) k.seitenTauschen();
            
            for (GruppenPaarung gp : gbf.befohleneAngriffe) {
                if (k.getSeiteA().contains(gp.getA()) || k.getSeiteB().contains(gp.getA())) {
                    k.getOriginalAngriffe().add(gp);
                }
            }
            
            retval.add(k);
            new Debug(i + ". unabhängiger Konflikt:\n" + k.toString());
            i++;
        }
        
        return retval;
    }
    
    private void addToGruppe(Unit u) {
        int p = u.getOwner();
        int tp = u.getTarnPartei();
        
        if (!gruppen.containsKey(p)) gruppen.put(p, new HashMap<Integer, Gruppe>());
        if (!gruppen.get(p).containsKey(tp)) gruppen.get(p).put(tp, new Gruppe(p, tp));
        
        gruppen.get(p).get(tp).getUnits().add(u);
    }
    
    private Gruppe getGruppe(Unit u) {
        int p = u.getOwner();
        int tp = u.getTarnPartei();
        if (!gruppen.containsKey(p)) return null;
        if (!gruppen.get(p).containsKey(tp)) return null;
        return gruppen.get(p).get(tp);
    }
    
    private Set<Gruppe> getAlleGruppen() {
        Set<Gruppe> retval = new HashSet<Gruppe>();
        for (int angreifer : gruppen.keySet()) {
            for (int verteidiger : gruppen.get(angreifer).keySet()) {
                retval.add(gruppen.get(angreifer).get(verteidiger));
            }
        }
        return retval;
    }
    
    /**
     * @param angreifer
     * @param verteidiger
     * @return in etwa: wie viele Attackiere-Befehle / Personen / Hitpoints diese Kombination aus Angreifer und Verteidiger erfüllen kann.
     */
    private int bewerteAVKombination(Set<Gruppe> angreifer, Set<Gruppe> verteidiger) {
        int wert = Random.W(100) - 1; // tie breaker...
        
        for (GruppenPaarung gp : this.befohleneAngriffe) {
            Gruppe a = gp.getA();
            Gruppe v = gp.getB();
            // wird dieser Befehl hier "bedient"?
            boolean erfuellt = false;
            if (angreifer.contains(a) && verteidiger.contains(v)) erfuellt = true;
            if (verteidiger.contains(a) && angreifer.contains(v)) erfuellt = true;
            
            if (erfuellt) {
                wert += gp.getWichtigkeit() * 100;
            }
        }
        
        if (ZATMode.CurrentMode().isDebug()) new Debug(beschreibeKonstellation(angreifer, verteidiger, wert));
        
        return wert;
    }
    
    int besteAVKombinationWert = Integer.MIN_VALUE;
    Set<Gruppe> besteAVKombinationAngreifer = null;
    Set<Gruppe> besteAVKombinationVerteidiger = null;
    
    private void avKombinationenProbieren(SortedSet<Gruppe> angreifer, SortedSet<Gruppe> verteidiger) {
        if (angreifer.size() + verteidiger.size() < 2) throw new IllegalArgumentException("Es braucht mindestens zwei Gruppen für einen Kampf...");
        
        // Anfangs- ...
        if (angreifer.isEmpty()) {
            SortedSet<Gruppe> neueAngreifer = new TreeSet<Gruppe>();
            SortedSet<Gruppe> neueVerteidiger = new TreeSet<Gruppe>();
            neueAngreifer.add(verteidiger.first());
            neueVerteidiger.addAll(verteidiger);
            neueVerteidiger.remove(verteidiger.first());
            
            avKombinationenProbieren(neueAngreifer, neueVerteidiger);
            return;
        }
        // ... und Endzustand:
        if (verteidiger.isEmpty()) return;
        
        int v = bewerteAVKombination(angreifer, verteidiger);
        if (v > besteAVKombinationWert) {
            new Debug("Neue beste Konstellation: " + beschreibeKonstellation(angreifer, verteidiger, v));
            besteAVKombinationAngreifer = new TreeSet<Gruppe>(angreifer);
            besteAVKombinationVerteidiger = new TreeSet<Gruppe>(verteidiger);
            besteAVKombinationWert = v;
        }
        // new Debug("Aktuelle beste Konstellation: " + beschreibeKonstellation(besteAVKombinationAngreifer, besteAVKombinationVerteidiger, besteAVKombinationWert));
        
        // rekursieren:
        SortedSet<Gruppe> neueAngreifer = new TreeSet<Gruppe>();
        neueAngreifer.addAll(angreifer);
        SortedSet<Gruppe> neueVerteidiger = new TreeSet<Gruppe>();
        neueVerteidiger.addAll(verteidiger);
        
        // jede Gruppe aus V einmal den A zuordnen und dann rekursieren:
        for (Gruppe g : verteidiger) {
            if (g.compareTo(neueAngreifer.last()) < 0) continue;
            
            neueAngreifer.add(g);
            neueVerteidiger.remove(g);
            avKombinationenProbieren(neueAngreifer, neueVerteidiger);
            neueAngreifer.remove(g);
            neueVerteidiger.add(g);
            
            // {1} {2, 3, 4, 5, 6} -->
            //      {1, 2} {3, 4, 5, 6}
            //      {1, 3} {2, 4, 5, 6} -->
            //      {1, 4} {2, 3, 5, 6}
            //      {1, 5} {2, 3, 4, 6}
            //      {1, 6} {2, 3, 4, 5}
            
            // {1, 3} {2, 4, 5, 6} -->
            //      {1, 3, 4} {2, 5, 6} -->
            //      {1, 3, 5} {2, 4, 6}
            //      {1, 3, 6} {2, 5, 6}
            //      NICHT {1, 2, 3} {4, 5, 6}, weil Gruppe 2 < Gruppe 3.
            
            // {1, 3, 4} {2, 5, 6} -->
            //      {1, 3, 4, 5} {2, 6}
            //      {1, 3, 4, 6} {2, 5}
            //      NICHT: {1, 2, 3, 4} {5, 6}, weil Gruppe 2 < Gruppe 4.
        }
            
    }
    
    @SuppressWarnings("unused")
	private Gruppe gegenseitigeKlaeren(Set<GruppenPaarung> paare, Set<Gruppe> beidseits) {
        if (beidseits.size() == 1) {
            new Debug("Es gibt nur noch eine Gruppe, die auf beiden Seiten auftaucht - tja, dann nehmen wir wohl die heraus von den Angreifern.");
            return beidseits.iterator().next();
        }
        
        // Metriken:
        new Debug("Metriken von beidseitigen Gruppen:");
        for (Gruppe g : beidseits) {
            int nPaarungen = 0;
            int nAngreifer = 0;
            int nVerteidiger = 0;
            
            // Fälle, in denen g Angreifer ist und auf der anderen Seite nur 
            // Gruppen stehen, die nie ihrerseits als Angreifer auftauchen:
            int nKlarerAngreifer = 0;
            // dito aus der anderen Perspektive:
            int nKlarerVerteidiger = 0;
            
            for (GruppenPaarung gp : paare) {
                if (gp.getA().equals(g)) {
                    nPaarungen ++;
                    nAngreifer ++;
                    if (!gp.getB().istAngreifer()) nKlarerAngreifer ++;
                }
                if (gp.getB().equals(g)) {
                    nPaarungen ++;
                    nVerteidiger ++;
                    if (!gp.getA().istVerteidiger()) nKlarerVerteidiger ++;
                }
            }
            new Debug(g.shortDesc() + ": " + nPaarungen + " Paarungen (A" + nAngreifer + "/" + nKlarerAngreifer + ", V" + nVerteidiger + "/" + nKlarerVerteidiger + ")");
        }
        
        // Jede PAARUNG (egal in welcher Richtung) nur einmal berücksichtigen:
        Set<GruppenPaarung> befohleneAngriffeUnique = new HashSet<GruppenPaarung>();
        for (GruppenPaarung gp : befohleneAngriffe) {
            if (!befohleneAngriffeUnique.contains(gp.invers())) befohleneAngriffeUnique.add(gp);
        }
        
        int besterScore = -1;
        Gruppe besteGruppe = null;
        for (Gruppe a : beidseits) {
            for (Gruppe b : beidseits) {
                if (a.equals(b)) continue;
                if (a.compareTo(b) > 0) continue;

                if (paare.contains(new GruppenPaarung(a, b)) && paare.contains(new GruppenPaarung(b, a))) {
                    // greifen sich direkt gegenseitig an:
                    new Debug(a + " und " + b + " greifen sich gegenseitig an...");
                } else {
                    new Debug(a + " und " + b + " stehen auf beiden Seiten, greifen sich aber nicht gegenseitig an.");
                }
                
                // TODO stattdessen mit Personen rechnen, so dass große Konflikte mehr Gewicht haben.
                int nBefohlene = 0;
                for (GruppenPaarung gp : befohleneAngriffeUnique) {
                    nBefohlene += gp.getA().getPersonen() * gp.getB().getPersonen();
                }
                
                
                // wenn A von der Angreiferseite genommen wird:
                int erfuellte = 0;
                for (GruppenPaarung gp : paare) {
                    if (gp.getA().equals(a)) continue;
                    
                    if (befohleneAngriffeUnique.contains(gp) || befohleneAngriffeUnique.contains(gp.invers())) {
                        erfuellte += gp.getA().getPersonen() * gp.getB().getPersonen();
                    }
                }
                new Debug("Wenn " + a + " die Angreifer verlässt, sind noch " + erfuellte + " von " + nBefohlene + " befohlenen Angriffen vorhanden.");
                if (erfuellte > besterScore) {
                    besterScore = erfuellte;
                    besteGruppe = a;
                }
                
                
                // wenn B von der Angreiferseite genommen wird:
                erfuellte = 0;
                for (GruppenPaarung gp : paare) {
                    if (gp.getA().equals(b)) continue;
                    
                    if (befohleneAngriffeUnique.contains(gp) || befohleneAngriffeUnique.contains(gp.invers())) {
                        erfuellte += gp.getA().getPersonen() * gp.getB().getPersonen();
                    }
                }
                new Debug("Wenn " + b + " die Angreifer verlässt, sind noch " + erfuellte + " von " + nBefohlene + " befohlenen Angriffen vorhanden.");
                if (erfuellte > besterScore) {
                    besterScore = erfuellte;
                    besteGruppe = b;
                }
                
            }
        }
        
        return besteGruppe;
    }
    
    /**
     * Für die Debug-Ausgabe der potentiellen A/V-Seiten-Konstellationen
     * @param angreifer
     * @param verteidiger
     * @param wert
     * @return menschenlesbare (? ;-) ) Beschreibung
     */
    public String beschreibeKonstellation(Set<Gruppe> angreifer, Set<Gruppe> verteidiger, int wert) {
        StringBuilder sb = new StringBuilder();
        sb.append("Konstellation: A {");
        for (Gruppe g : angreifer) {
            sb.append(g.microDesc()).append(";");
        }
        if (!angreifer.isEmpty()) sb.delete(sb.length() - 1, sb.length());
        sb.append("} V {");
        for (Gruppe g : verteidiger) {
            sb.append(g.microDesc()).append(";");
        }
        if (!verteidiger.isEmpty()) sb.delete(sb.length() - 1, sb.length());
        sb.append("}");

        sb.append(" Wert = ").append(wert);
        return sb.toString();
    }

    public String shortDesc(Set<Gruppe> gruppen) {
        if (gruppen.isEmpty()) return "(Leere Gruppenmenge)";
        List<String> l = new ArrayList<String>();
        for (Gruppe g : gruppen) l.add(g.shortDesc());
        return "Gruppenmenge: " + StringUtils.aufzaehlung(l);
    }
    
    public class GruppenKnaeuel {
        final Set<GruppenPaarung> paare = new HashSet<GruppenPaarung>();
        
        public GruppenKnaeuel() {
            
        }
        
        public void addPaar(GruppenPaarung gp) {
            paare.add(gp);
        }

        public Set<GruppenPaarung> getPaare() {
            return paare;
        }
        
        public void add(GruppenKnaeuel gk) {
            for (GruppenPaarung gp : gk.getPaare()) this.addPaar(gp);
        }
        
        public Set<Gruppe> getInvolvierte() {
            Set<Gruppe> retval = new HashSet<Gruppe>();
            retval.addAll(getAGruppen());
            retval.addAll(getBGruppen());
            return retval;
        }
        
        public Set<Gruppe> getAGruppen() {
            Set<Gruppe> retval = new HashSet<Gruppe>();
            for (GruppenPaarung gp : paare) {
                retval.add(gp.getA());
            }
            return retval;
        }

        public Set<Gruppe> getBGruppen() {
            Set<Gruppe> retval = new HashSet<Gruppe>();
            for (GruppenPaarung gp : paare) {
                retval.add(gp.getB());
            }
            return retval;
        }
        
        public Set<Gruppe> getBeidseitigeGruppen() {
            Set<Gruppe> retval = new HashSet<Gruppe>();
            
            final Set<Gruppe> bGruppen = getBGruppen();
            
            for (Gruppe a : getAGruppen()) {
                if (bGruppen.contains(a)) retval.add(a);
            }

            return retval;
        }
        
        public Set<Gruppe> getEindeutigeGruppen() {
            Set<Gruppe> retval = new HashSet<Gruppe>();
            
            final Set<Gruppe> beidseitige = getBeidseitigeGruppen();
            
            for (Gruppe a : getAGruppen()) {
                if (!beidseitige.contains(a)) retval.add(a);
            }
            for (Gruppe b : getBGruppen()) {
                if (!beidseitige.contains(b)) retval.add(b);
            }

            return retval;
        }
        
        /**
         * @return true, wenn ein oder mehrere Gruppen auf beiden Seiten auftauchen
         */
        public boolean istWiderspruechlich() {
            if (!getBeidseitigeGruppen().isEmpty()) return true;
            return false;
        }

        public boolean contains(Gruppe g) {
            return this.getInvolvierte().contains(g);
        }
        
        public Set<Gruppe> getSchnittmenge(GruppenKnaeuel other) {
            if (other == null) throw new IllegalArgumentException("other ist <null>");
            
            Set<Gruppe> meine = this.getInvolvierte();
            Set<Gruppe> seine = other.getInvolvierte();
            
            Set<Gruppe> retval = new HashSet<Gruppe>();
            for (Gruppe g : meine) {
                if (seine.contains(g)) retval.add(g);
            }
            
            return retval;
        }

        @Override
        public String toString() {
            if (getPaare().size() == 1) {
                return this.getClass().getSimpleName() + " Simplex: " + getPaare().iterator().next();
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName()).append(" mit den Gruppen ");
            sb.append(StringUtils.aufzaehlung(this.getInvolvierte())).append("\n");
            sb.append("Paarungen:\n");
            for (GruppenPaarung gp : paare) sb.append("   ").append(gp.toString()).append("\n");
            sb.append("Auf der A-Seite:\n");
            for (Gruppe g : getAGruppen()) sb.append("   ").append(g.shortDesc()).append("\n");
            sb.append("Auf der B-Seite:\n");
            for (Gruppe g : getBGruppen()) sb.append("   ").append(g.shortDesc()).append("\n");
            if (this.istWiderspruechlich()) {
                sb.append("WIDERSPRÜCHLICHE Gruppen:\n");
                for (Gruppe g : getBeidseitigeGruppen()) sb.append("   ").append(g.shortDesc()).append("\n");
                sb.append("Eindeutige Gruppen:\n");
                for (Gruppe g : getEindeutigeGruppen()) sb.append("   ").append(g.shortDesc()).append("\n");
            }
            
            return sb.toString();
        }
        

    }

}
