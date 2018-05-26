package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.util.ComplexName;

public class Mastodonpanzer extends Elefantenpanzer
{
	public Mastodonpanzer() { super(); }
	
	public String getName()
	{
		return "Mastodonpanzer";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Mastodonpanzer", "Mastodonpanzer", null);
	}
	
	// Rest Elefantpanzer
}
