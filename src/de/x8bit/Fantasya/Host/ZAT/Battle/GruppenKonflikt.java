package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.util.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author hapebe
 */
public class GruppenKonflikt {
    final GruppenSet seiteA = new GruppenSet();
    final GruppenSet seiteB = new GruppenSet();
    final Set<GruppenPaarung> originalAngriffe = new HashSet<GruppenPaarung>();

    public GruppenKonflikt() {
   }

    public Set<Gruppe> getSeiteA() {
        return seiteA;
    }

    public Set<Gruppe> getSeiteB() {
        return seiteB;
    }

    public Set<GruppenPaarung> getOriginalAngriffe() {
        return originalAngriffe;
    }
    
    public Set<GruppenPaarung> getUnmoeglicheAngriffe() {
        Set<GruppenPaarung> retval = new HashSet<GruppenPaarung>();
        
        for (GruppenPaarung orig : getOriginalAngriffe()) {
            Gruppe a = orig.getA();
            Gruppe v = orig.getB();
            
            if (!(getSeiteA().contains(a) || getSeiteB().contains(a))) {
                continue; // dieses Paar gehört offenbar gar nicht zu diesem Konflikt.
            }
            
            if (
                    (getSeiteA().contains(a) && getSeiteA().contains(v))
                    ||
                    (getSeiteB().contains(a) && getSeiteB().contains(v))
            ) {
                retval.add(orig);
            }
        }
        
        return retval;
    }

    public void seitenTauschen() {
        GruppenSet temp = new GruppenSet();
        temp.addAll(seiteA);
        seiteA.clear();
        seiteA.addAll(seiteB);
        seiteB.clear();
        seiteB.addAll(temp);
        temp.clear();
        temp = null;
    }

    public int getGesamtPersonen() {
        return seiteA.getPersonen() + seiteB.getPersonen();
    }

    public Set<Partei> getParteienSeiteA() {
        Set<Partei> retval = new HashSet<Partei>();
        for (Gruppe g : getSeiteA()) {
            retval.add(Partei.getPartei(g.getParteiNr()));
        }
        return retval;
    }

    public Set<Partei> getParteienSeiteB() {
        Set<Partei> retval = new HashSet<Partei>();
        for (Gruppe g : getSeiteB()) {
            retval.add(Partei.getPartei(g.getParteiNr()));
        }
        return retval;
    }

    public Set<Partei> getInvolvierteParteien() {
        Set<Partei> retval = new HashSet<Partei>();
        retval.addAll(this.getParteienSeiteA());
        retval.addAll(this.getParteienSeiteB());
        return retval;
    }

    public String getBezeichnungUnbestimmtNominativ() {
        String b = getBezeichnung();
        if (b.equalsIgnoreCase("kleine meinungsverschiedenheit")) {
            return "eine kleine Meinungsverschiedenheit";
        }
        if (b.equalsIgnoreCase("handgemenge")) {
            return "ein Handgemenge";
        }
        if (b.equalsIgnoreCase("kampf")) {
            return "ein Kampf";
        }
        if (b.equalsIgnoreCase("schlacht")) {
            return "eine Schlacht";
        }
        if (b.equalsIgnoreCase("epische schlacht")) {
            return "eine epische Schlacht";
        }
        // default:
        return b;
    }

    public String getBezeichnungBestimmtAkkusativ() {
        String b = getBezeichnung();
        if (b.equalsIgnoreCase("kleine meinungsverschiedenheit")) {
            return "die kleine Meinungsverschiedenheit";
        }
        if (b.equalsIgnoreCase("handgemenge")) {
            return "das Handgemenge";
        }
        if (b.equalsIgnoreCase("kampf")) {
            return "den Kampf";
        }
        if (b.equalsIgnoreCase("schlacht")) {
            return "die Schlacht";
        }
        if (b.equalsIgnoreCase("epische schlacht")) {
            return "die epische Schlacht";
        }
        // default:
        return b;
    }

    public String getBezeichnung() {
        int minN = Math.min(seiteA.getPersonen(), seiteB.getPersonen());
        int maxN = Math.max(seiteA.getPersonen(), seiteB.getPersonen());
        int N = minN + maxN;
        if (maxN < 10) {
            return "kleine Meinungsverschiedenheit";
        }
        if ((N < 100) && (minN <= 10)) {
            return "Handgemenge";
        }
        if (N < 100) {
            return "Kampf"; // minN > 10
        }
        if ((N < 1000) && (minN < 50)) {
            return "Kampf";
        }
        if (N < 2000) {
            return "Schlacht";
        }
        if (minN < 1000) {
            return "Schlacht";
        }
        return "epische Schlacht"; // minN > 1000 und maxN > 1000
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.ucfirst(getBezeichnungUnbestimmtNominativ())).append(": ");
        if (getSeiteA().size() > 1) {
            List<String> beteiligte = new ArrayList<String>();
            for (Gruppe g : getSeiteA()) {
                beteiligte.add(g.beschreibeFuerPartei(Partei.getPartei(0)));
            }
            sb.append("Die Verb\u00fcndeten ").append(StringUtils.aufzaehlung(beteiligte));
            sb.append(" greifen ");
        } else {
            sb.append(getSeiteA().iterator().next().beschreibeFuerPartei(Partei.getPartei(0)));
            sb.append(" greift ");
        }
        if (getSeiteB().size() > 1) {
            List<String> beteiligte = new ArrayList<String>();
            for (Gruppe g : getSeiteB()) {
                beteiligte.add(g.beschreibeFuerPartei(Partei.getPartei(0)));
            }
            sb.append("die Verb\u00fcndeten ").append(StringUtils.aufzaehlung(beteiligte));
        } else {
            sb.append(getSeiteB().iterator().next().beschreibeFuerPartei(Partei.getPartei(0)));
        }
        sb.append(" an.");
        return sb.toString();
    }
    
}
