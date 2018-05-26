package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Waffenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Kriegsmastodon extends Kriegselefant
{
	public Kriegsmastodon() {
		super(); 
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Waffenbau.class, 5) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2), new ConstructionContainer(Mastodon.class, 1), new ConstructionContainer(Mastodonpanzer.class, 1) } );
	}
	
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Kriegsmastodon";
		return "Kriegsmastodon";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Kriegsmastodon", "Kriegsmastodons", null);
	}
}
