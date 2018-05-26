package de.x8bit.Fantasya.Atlantis.Regions;


import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Lavastrom extends Region {
	public Lavastrom() {
		setName("Lavastrom");
	}
	
	@Override
	public String getArtikel() { return "der".intern(); }

	@Override
	public void Init() {
		super.Init();
		setName("");
	}
	
	@Override
	public String toString() {
		return "Lavastrom";
	}
	
	// hier jetzt passend überschreiben
	@Override
	public boolean istBetretbar(Unit unit) { return false; }
	
	/**
	 * liefert die noch freien Arbeitsplätze
	 * @return freien Arbeitsplätze
	 */
	public int Arbeitsplaetze()	{
		return 0;
	}
	
	/**
	 * in einem Lavastrom wächst nichts bis gar nichts
	 */
	@Override
	public void Wachstum()
	{
		
	}
	
	@Override
	public boolean canNachfrage() { return false; }
}
