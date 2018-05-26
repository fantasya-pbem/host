package de.x8bit.Fantasya.Atlantis.Regions;

import java.awt.image.BufferedImage;


import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;

/**
 * ein Ozean
 * 
 * @author mogel
 *
 */
public class Ozean extends Region
{
	public Ozean()
	{
		setName("Ozean");
	}
	
	@Override
	public void Init()
	{
		super.Init();
		setName("");
	}

	@Override
	public String getArtikel() { return "".intern(); }
	/** liefert die Wahrscheinlichkeit für einen Sturm (bzw. das das Schiff abgetrieben wird) */
	public int getSturmValue()
	{
		int v = getIntegerProperty("sturmvalue", -1); // -1 weil nicht gesetzt
		return v;
	}
	
	/** setzt die Wahrscheinlichkeit */
	public void setSturmValue(int v)
	{
		setProperty("sturmvalue", v);
	}

	@Override
	public String toString()
	{
		return "Ozean";
	}
	
	// hier jetzt passend überschreiben ... Ozean ist alles anders ^^
	@Override
	public boolean istBetretbar(Unit unit) {
		if (unit != null) if (unit.canSwim()) return true; 
		return false; 
	}
	
	private static BufferedImage image = null;
	
	/**
	 * liefert die noch freien Arbeitsplätze
	 * @return freien Arbeitsplätze
	 */
	public int Arbeitsplaetze() { return 0; }
	
	/** in einem Ozean wächst nichts (erstmal) */
	@Override
	public void Wachstum() { }
	
	/**
	 * berechnet den Sturmwert für alle Ozeane ... dazu erhalten alle Ozeane mit Küste eine 0
	 * alle Ozeane die an 0 grenzen eine 1 ... der Rest der Ozeane eine 2
	 */
	public static void calcSturmValue() {
		new SysMsg("berechne SturmValue");
		calcSturmValue0();
		calcSturmValue1();
		calcSturmValue2();
	}

	/** berechnet den Sturmwert für 0 - Küstenregionen */
	private static void calcSturmValue0() {
		for(Region r : Region.CACHE.values())
		{
			if (!(r instanceof Ozean)) continue; 	// nur Ozeane
			Ozean o = (Ozean) r;
			if (o.getSturmValue() != -1) continue;	// Wert schon gesetzt
			int count = 0;
			for(Coords c : o.getCoords().getNachbarn()) count += (Region.Load(c).istBetretbar(null) ? 1 : 0);
			if (count > 0) o.setSturmValue(0);
		}
	}

	/** berechnet den Sturmwert für 1 - Nachbarn von 0 */
	private static void calcSturmValue1() {
		for(Region r : Region.CACHE.values())
		{
			if (!(r instanceof Ozean)) continue; 	// nur Ozeane
			Ozean o = (Ozean) r;
			if (o.getSturmValue() != -1) 
				continue;	// Wert schon gesetzt
			int count = 0;
			for(Coords c : o.getCoords().getNachbarn())
			{
				Region region = Region.Load(c);
				if (!(region instanceof Ozean)) continue;
				Ozean or = (Ozean) region;
				count += ((or.getSturmValue() == 0) ? 1 : 0);
			}
			if (count > 0) o.setSturmValue(1);
		}
	}

	/** berechnet den Sturmwert für 2 - übrigen */
	private static void calcSturmValue2() {
		for(Region r : Region.CACHE.values())
		{
			if (!(r instanceof Ozean)) continue; 	// nur Ozeane
			Ozean o = (Ozean) r;
			if (o.getSturmValue() != -1) continue;	// Wert schon gesetzt
			o.setSturmValue(2);
		}
	}
	
	@Override
	public boolean canNachfrage() { return false; }
}
