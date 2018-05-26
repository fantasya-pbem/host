package de.x8bit.Fantasya.Host.ZAT.Battle.util;

/**
 * Berechnung von Wahrscheinlichkeiten für den Erfolg gezielter Angriffe.
 * @author hb
 * @see de.x8bit.Fantasya.Host.EVA.Kriege
 */
public class ZielChancenModell {
    double ausfallAlpha = 0;
    double ausfallBeta = 5;

    double zielAlpha = 0;
    double zielBeta = 3;
    double zielSchwierigkeit = 10d;

    public double getAusfallSchwierigkeit(double ratio) {
        double skalar = Math.log(ratio);
        return -1 * Math.pow(Math.abs(skalar), 1.8);
    }

    /**
     * @param talentwert Summe der Talentwerte Wahrnehmung und der jeweiligen Waffe
     * @param ratio Verhältnis / Anteil:  Ziele div. Gesamtzahl der Feinde
     * @return
     */
    public double getAusfallChance(int talentwert, double ratio) {
        return logistischP((double)talentwert + getAusfallSchwierigkeit(ratio), getAusfallAlpha(), getAusfallBeta());
    }
    
    /**
     * @param talentwert Summe der Talentwerte Wahrnehmung und der jeweiligen Waffe
     * @param ratio Verhältnis / Anteil:  Ziele div. Gesamtzahl der Feinde
     * @return
     */
    public double getZielChance(int talentwert, double ratio) {
        double x = (double)talentwert + Math.log10(ratio) * getZielSchwierigkeit();

        return 1d - logistischP(x, getZielAlpha(), getZielBeta());
    }

    public double getAusfallAlpha() {
        return ausfallAlpha;
    }

    public void setAusfallAlpha(double ausfallAlpha) {
        this.ausfallAlpha = ausfallAlpha;
    }

    public double getAusfallBeta() {
        return ausfallBeta;
    }

    public void setAusfallBeta(double ausfallBeta) {
        this.ausfallBeta = ausfallBeta;
    }

    public double getZielAlpha() {
        return zielAlpha;
    }

    public void setZielAlpha(double zielAlpha) {
        this.zielAlpha = zielAlpha;
    }

    public double getZielBeta() {
        return zielBeta;
    }

    public void setZielBeta(double zielBeta) {
        this.zielBeta = zielBeta;
    }

    public double getZielSchwierigkeit() {
        return zielSchwierigkeit;
    }

    public void setZielSchwierigkeit(double zielSchwierigkeit) {
        this.zielSchwierigkeit = zielSchwierigkeit;
    }

    private double logistischP(double x, double alpha, double beta) {
        try {
            double exponent = -1 * (0 - x - alpha) / beta;
            double eAusdruck = 1 + Math.exp(exponent);
            double p = 1 / (eAusdruck * eAusdruck);

            return p;
        } catch (ArithmeticException ex) { }

        return 0d;
    }

}
