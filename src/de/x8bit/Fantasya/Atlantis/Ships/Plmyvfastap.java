package de.x8bit.Fantasya.Atlantis.Ships;

import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Schiffswerft;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Plmyvfastap extends Ship {

	public Plmyvfastap()
	{
		geschwindigkeit = 4;
		// kapazitaet = 200000;
		// matrosen = 120;
		// kapitaenTalent = 6;
		
		// Konstruktion des Schiffes
		setConstructionSize(Integer.MAX_VALUE);
		setConstructionItems( new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1), new ConstructionContainer(Eisen.class, 1), new ConstructionContainer(Stein.class, 1) } );
		setConstructionSkills( new ConstructionContainer [] { new ConstructionContainer(Schiffbau.class, 6) } );
		setConstructionCheats(new ConstructionCheats []
		                                              {
														new ConstructionCheats(Schiffswerft.class, new ConstructionContainer [] {
															new ConstructionContainer(Holz.class, 2), 	// alle
															new ConstructionContainer(Stein.class, 2),	// Plymvfastap
															new ConstructionContainer(Eisen.class, 2) 	// Plymvfastap
																																})
		                                              });
	}
	
	/** das Schiff ist eigentlich immer fertig ... aber an fertigen Schiffen wird nicht gebaut */
	@Override
	public void Mache(Unit unit)
	{
		setFertig(false);
//		super.Mache(unit);
//		setFertig(true);
	}
	
	/** Verhältnis berechnet auf Grund Tireme */
	@Override
	public int getKapazitaet() { return getGroesse() * 1000; }

	/** TW für Matrosen berechnen */
	@Override
	public int getMatrosen() { return getGroesse() / 2; }	// entspricht 100 TW bei Tireme

	/** TW des Kapitäns */
	@Override
	public int getKapitaenTalent() { return (getGroesse() / 40) - 1; }	// sollte TW 4 bei "Tireme" sein

	@Override
	public int getVerfall() {
		return 2;
	}

    @Override
    public void Zerstoere(Unit u) {
        super.Zerstoere(
                u,
                new Item [] {
                    new Holz(1),
                    new Stein(1),
                    new Eisen(1),
                }
        );
    }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Plmyvfastap", "Plmyvfastaps", null);
	}

}
