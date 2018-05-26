package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Partei;

/**
 *
 * @author hb
 */
public class Einzelattacke {

    protected static int LaufendeNummer = 0;

    final int laufendeNummer;

    protected final Krieger angreifer;
    protected final Krieger verteidiger;
	Weapon angreiferWaffe;
	Weapon verteidigerWaffe;
    int av;
    int dv;
    int damageValue;
    int blockValue;

    int wuerfelSeiten;
	int gewuerfelt;
    boolean erfolgreich;
    int schaden;
	boolean tot;

    /**
     * Das Vorwort sollte als einzelne Meldung VOR Beschreibung oder Kurzbeschreibung ausgegeben werden, wenn es != null und länger als 0 Zeichen ist.
     */
    protected String vorwort = "";
    protected String beschreibung;
    protected String kurzBeschreibung;

    public Einzelattacke(Krieger angreifer, Krieger verteidiger) {
        this.angreifer = angreifer;
        this.verteidiger = verteidiger;

        this.laufendeNummer = LaufendeNummer ++;
    }

    public Krieger getAngreifer() {
        return angreifer;
    }

    public Krieger getVerteidiger() {
        return verteidiger;
    }

    public int getLaufendeNummer() {
        return laufendeNummer;
    }

    public int getAv() {
        return av;
    }

    public void setAv(int av) {
        this.av = av;
    }

    /**
     * 
     * @return Das Vorwort sollte als einzelne Meldung VOR Beschreibung oder Kurzbeschreibung ausgegeben werden, wenn es != null und länger als 0 Zeichen ist.
     */
    public String getVorwort() {
        return vorwort;
    }

    /**
     * Das Vorwort sollte als einzelne Meldung VOR Beschreibung oder Kurzbeschreibung ausgegeben werden, wenn es != null und länger als 0 Zeichen ist.
     */
    public void setVorwort(String vorwort) {
        if (vorwort == null) {
            this.vorwort = "";
            return;
        }
        this.vorwort = vorwort;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getKurzBeschreibung() {
        return kurzBeschreibung;
    }

    public void setKurzBeschreibung(String kurzBeschreibung) {
        this.kurzBeschreibung = kurzBeschreibung;
    }

	public String getKurzBeschreibung(Partei wir, boolean talenteZeigen, boolean avDvZeigen, boolean hitpointsZeigen) {
        StringBuilder shorty = new StringBuilder();

        shorty.append(String.format("%-8s", verteidiger.kurzCode()));

        boolean angreiferTalentZeigen = false;
		boolean verteidigerTalentZeigen = false;
		boolean avZeigen = false;
		boolean dvZeigen = false;
		
		if (angreifer.getUnit().getOwner() == wir.getNummer()) {
			angreiferTalentZeigen = true;
			avZeigen = true;
		}
		if (verteidiger.getUnit().getOwner() == wir.getNummer()) {
			verteidigerTalentZeigen = true;
			dvZeigen = true;
		}
		if (talenteZeigen) { angreiferTalentZeigen = true; verteidigerTalentZeigen = true; }
		if (avDvZeigen) { avZeigen = true; dvZeigen = true; }

        shorty.append( String.format("%-5s", angreiferWaffe.beschreibeKurz(angreiferTalentZeigen)) );
		if (avZeigen) {
			shorty.append("=").append(String.format("%2s", getAv()));
		} else {
			shorty.append("   ");
		}
        shorty.append(" -> ");
        shorty.append( String.format("%-5s", verteidigerWaffe.beschreibeKurz(verteidigerTalentZeigen)) );
		if (dvZeigen) {
			shorty.append("=").append(String.format("%2s", getDv()));
		} else {
			shorty.append("   ");
		}

        String s = String.format("%3s", getGewuerfelt() + (isErfolgreich()?"+":" "));
		if (!avDvZeigen) s = (isErfolgreich()?"+  ":"   ");
        shorty.append(" ").append(s).append(" ");

		s = "      ";
		if ((getDamageValue() > 0) && (getBlockValue() > 0)) {
			s = String.format("%6s", getDamageValue() + "-" + getBlockValue());
		}
		shorty.append(s);

        if (getSchaden() > 0) {
			shorty.append(String.format("%4s", getSchaden())).append("D");
		} else {
            shorty.append("     ");
		}

        shorty.append(" ");

        // Lebenspunkte dazuzählen ... da 0 Lebenspunkte super sind !!
		// quasi - je mehr Lebenspunkte um so toter ist der Krieger
		if (hitpointsZeigen || (verteidiger.getUnit().getOwner() == wir.getNummer())) {
			if (getSchaden() > 0) {
				int lpVorher = verteidiger.getLebenspunkte() - getSchaden();
				shorty.append(String.format("%-9s", lpVorher + ">" + verteidiger.getLebenspunkte() + ">" + verteidiger.getTrefferpunkte()));

			} else if (getDamageValue() > 0) {
				shorty.append(String.format("%-9s", "(" + verteidiger.getLebenspunkte() + ">" + verteidiger.getTrefferpunkte() + ")"));
			}
		}
		if (isTot()) shorty.append(" -- ").append(verteidiger.kurzCode()).append("t"); // + "†");

		return shorty.toString();
	}

    public int getDv() {
        return dv;
    }

    public void setDv(int dv) {
        this.dv = dv;
    }

    public int getBlockValue() {
        return blockValue;
    }

    public void setBlockValue(int blockValue) {
        this.blockValue = blockValue;
    }

    public int getDamageValue() {
        return damageValue;
    }

    public void setDamageValue(int damageValue) {
        this.damageValue = damageValue;
    }

    public boolean isErfolgreich() {
        return erfolgreich;
    }

    public void setErfolgreich(boolean erfolgreich) {
        this.erfolgreich = erfolgreich;
    }

    public int getGewuerfelt() {
        return gewuerfelt;
    }

    public void setGewuerfelt(int gewuerfelt) {
        this.gewuerfelt = gewuerfelt;
    }

    public int getSchaden() {
        return schaden;
    }

    public void setSchaden(int schaden) {
        this.schaden = schaden;
    }

	public Weapon getAngreiferWaffe() {
		return angreiferWaffe;
	}

	public void setAngreiferWaffe(Weapon angreiferWaffe) {
		this.angreiferWaffe = angreiferWaffe;
	}

	public Weapon getVerteidigerWaffe() {
		return verteidigerWaffe;
	}

	public void setVerteidigerWaffe(Weapon verteidigerWaffe) {
		this.verteidigerWaffe = verteidigerWaffe;
	}

	public int getWuerfelSeiten() {
		return wuerfelSeiten;
	}

	public void setWuerfelSeiten(int wuerfelSeiten) {
		this.wuerfelSeiten = wuerfelSeiten;
	}

	public boolean isTot() {
		return tot;
	}

	public void setTot(boolean tot) {
		this.tot = tot;
	}

}
