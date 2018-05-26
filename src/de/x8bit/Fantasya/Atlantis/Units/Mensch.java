package de.x8bit.Fantasya.Atlantis.Units;

import de.x8bit.Fantasya.Atlantis.Unit;

/**
 * 
 * @author mogel
 *
 * die die alles können ... nur nix so richtig gut
 * 
 */

public class Mensch extends Unit // SKILL
{
	public Mensch()
	{
		RekrutierungsKosten = 75;

		minHunger = 4;
		maxHunger = 8;
	}
    @Override
	public int Trefferpunkte() { return 20; }

	@Override
	public int maxMigranten(int personenDerPartei) {
		int moeglich = (int) (Math.log10(personenDerPartei / 50.0) * 20.0);
		if (moeglich <= 0) return 0;
		return moeglich;
	}

	@Override
	public boolean istSpielerRasse() { return true;	}

	@Override
	public String getRassenName() {
		if (this.getPersonen() == 1) return getRasse();
		return "Menschen";
	}

}
