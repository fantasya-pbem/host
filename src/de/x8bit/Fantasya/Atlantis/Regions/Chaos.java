package de.x8bit.Fantasya.Atlantis.Regions;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Unit;

import java.awt.image.BufferedImage;


import de.x8bit.Fantasya.Atlantis.Region;

public class Chaos extends Region
{
	public Chaos()
	{
		setName("Chaos");
	}
	
	public Chaos(int x, int y, int welt)
	{
		this();
        this.setCoords(new Coords(x, y, welt));
	}
	
	@Override
	public void Init()
	{
		// nix ... Chaos ist halt Choas
	}
	
	@Override
	public String getArtikel() { return "das"; }
    

	// hier jetzt passend überschreiben ... Chaos ist alles anders ^^
	@Override
	public boolean istBetretbar(Unit unit) { return false; }
	
	@Override
	public boolean canNachfrage() { return false; }
	
	private static BufferedImage image = null;
	
}
