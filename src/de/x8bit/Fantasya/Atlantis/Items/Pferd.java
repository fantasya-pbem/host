package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Hochland;
import de.x8bit.Fantasya.Atlantis.Regions.Sumpf;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import de.x8bit.Fantasya.Atlantis.Skills.Pferdedressur;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

public class Pferd extends Item implements AnimalResource {
    public final static String PROPERTY_ENTLASSEN = "environment.pferd.entlassen";
    public final static String PROPERTY_GEFANGEN = "environment.pferd.gefangen";

	public Pferd()
	{
		super(5000, 7000);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Pferdedressur.class, 1) } );
	}

	/**
	 * Konstruktor für verwandte Arten - Pegasi
	 * @param gewicht - Gewicht
	 * @param kapazitaet - Kapazität, muss das Gewicht beinhalten (das Item trägt sich selber!!)
	 */
	protected Pferd(int gewicht, int kapazitaet) {
		super(gewicht, kapazitaet);
	}

	@Override
	public String getName()	{
		if (anzahl != 1) return "Pferde";
		return "Pferd";
	}
	
	/**
	 * Pferde fangen via MACHE
	 * @param unit - diese Einheit will Pferde fangen
	 * @param anzahl - maximal so viele sollen gefangen werden
	 */
	@Override
	public void Mache(Unit unit, int anzahl)
	{
		// produzieren
		int punkte = super.GenericMake2(unit, anzahl);	// Produktionspunkte holen

		// Meldung
        if (punkte > 0) {
            Partei p = Partei.getPartei(unit.getOwner());
            
            int freigelassen = p.getIntegerProperty(Pferd.PROPERTY_ENTLASSEN, 0);
            int pferdeGefangen = p.getIntegerProperty(Pferd.PROPERTY_GEFANGEN, 0);

			double gluecksKoeffizient = 1d;
			if (pferdeGefangen > 0) {
				gluecksKoeffizient = 1d - (double)freigelassen / (double)pferdeGefangen;
			}
			// Wenn eine Partei genauso viele Pferde freigelassen wie gefangen hat (oder gar mehr),
			// dann sinken das Glück auf 0.
			//
			// Hat eine Partei gar keine Pferde entlassen, ist das Glück maximal - genau 1.

			// Chance kann 0..100 sein:
			int chance = (int)Math.round(gluecksKoeffizient * 100d);

            int davonPegasi = 0;
            if (this.getClass() == Pferd.class) {
                if (pferdeGefangen >= freigelassen) {
                    // es gibt die Chance auf Pegasi!
                    for (int i=0; i<punkte; i++) {
                        if (Random.rnd(0, 10000) <= chance) {
                            punkte --;
                            davonPegasi ++;
                        }
                    }
                }
            } else if (this.getClass() == Pegasus.class) {
                davonPegasi = punkte;
                punkte = 0;
            }

            setAnzahl(getAnzahl() + punkte);
            if (davonPegasi == 0) {
                new Info(unit + " fängt " + punkte + " " + this.getName() + ".", unit);
            } else {
                Item pegasi = unit.getItem(Pegasus.class);
                pegasi.setAnzahl(pegasi.getAnzahl() + davonPegasi);

                Item neuePegasi = new Pegasus();
                neuePegasi.setAnzahl(davonPegasi);

                if (punkte > 0) {
                    new Info(unit + " fängt " + (punkte + davonPegasi) + " " + this.getName()
                            + ". Dabei stellt sich heraus, dass " + davonPegasi + " " + neuePegasi.getName() + " darunter sind.",
                            unit);
                } else {
                    new Info(unit + " hat Glück und fängt " + davonPegasi + " " + neuePegasi.getName() + ".",
                            unit);
                }
            }

            p.setProperty(Pferd.PROPERTY_GEFANGEN, pferdeGefangen + punkte + davonPegasi);
        }
	}

//	/** das Pferd von der Gegend abziehen */
//	protected void Make(Unit u, int anzahl)
//	{
//		Region region = Region.Load(u.getCoords()); 
//		Item resource = region.getResource(this.getClass());
//		
//		// Sicherheitscheck
//		if (anzahl > resource.getAnzahl()) anzahl = resource.getAnzahl();
//		
//		// von der Gegend abziehen
//		resource.setAnzahl(resource.getAnzahl() - anzahl);
//		
//		// nun noch herstellen
//		super.Make(u, anzahl);
//		
//		// Pegasus fangen
//		if (anzahl > Atlantis.rnd(0, 100))
//		{
//			Item pegasus = u.getItem(Pegasus.class);
//			pegasus.setAnzahl(pegasus.getAnzahl() + (anzahl / 100) + 1); // für jedes 100. Pferd aber mind. 1 Pegasus
//			new Info(u + " kann ein Pegasus einfangen", u, u.getCoords());
//		}
//		
//		// Einhorn fangen ... etwas anders als in V1 ... aber nur max. 1
//		if (/* Einhorn-Chance */Atlantis.rnd(0, anzahl) > /* Total */Atlantis.rnd(0, 100) )
//		{
//			// *juhu* ... was ganz seltenes
//			Item einhorn = u.getItem(Einhorn.class);
//			einhorn.setAnzahl(einhorn.getAnzahl() + 1);
//			new Info(u + " kann ein Einhorn einfangen", u, u.getCoords());
//		}
//	}
	
	@Override
	public void actionWachstum(Region r)
	{
		// kein Wachstum in der Unterwelt:
		if (r.getCoords().getWelt() < 0) return;
		
		int rnd = Random.rnd(0, 1);
		int jz[] = new int[] { rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd };
		
		// idealen regionen
		if (r.getClass().equals(Ebene.class) || r.getClass().equals(Berge.class) ||
				r.getClass().equals(Hochland.class))
			jz = new int[] { 1, 2, 3, 4, 5, Random.rnd(4, 8), Random.rnd(4, 8), Random.rnd(4, 8), 5, 4, 3, 2 };
		
		if (r.getClass().equals(Sumpf.class) || r.getClass().equals(Wueste.class))
			jz = new int[] { 0, 0, 1, 2, 3, Random.rnd(2, 5), Random.rnd(2, 5), Random.rnd(2, 5), 3, 2, 1, 0 };

		int wachstum = jz[GameRules.getJahreszeit()];

		// das Wachstum
		int neu = anzahl > 4 ? (int) ((float) anzahl * wachstum / 100) + 1 : 0;
		
		if (r.freieArbeitsplaetze() < neu) return;	// nue wachsen wenn noch freie Abreitsplätze da sind
		
		//new Debug(this + " - " + anzahl + " Pferde - neu: " + neu, getCoords());
		anzahl += neu;
		
		if (anzahl < 0) anzahl = 0;
	}
	
    @Override
	public boolean surviveBattle() {
        if (Random.W(100) > 50) return true;
        return false;
    }

    @Override
	public boolean willWandern(Region r) {
		return (Random.W(10000) < 100); // 1%
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Pferd", "Pferde", null);
	}
}
