package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Building;

/**
 * enthält alle Infos um bei der Produktion entsprechend
 * Vorteile zu erhalten (Schiffe & Schiffbau & Werft & so)
 * @author mogel
 */
public class ConstructionCheats {

	/** in diesem Gebäude werden Resourcen gespart */
	private Class<? extends Building> constructionBuilding = null;

	/** 
	 * diese Items werden gespart ... unter Value steht jedes wievielte Item gespart wird
	 * eine 1 bedeutet eins verwendet eins gespart ... 2 bedeutet zwei verwendet eins gespart
	 */
	private ConstructionContainer constructionItems [] = null;

	public void setConstructionItems(ConstructionContainer constructionItems[])	{ this.constructionItems = constructionItems; }
	public ConstructionContainer[] getConstructionItems() 						{ return constructionItems; }
	public void setConstructionBuilding(Class<? extends Building> building) 	{ this.constructionBuilding = building; }
	public Class<? extends Building> getConstructionBuilding() 					{ return constructionBuilding; }

	public ConstructionCheats(Class<? extends Building> buildings, ConstructionContainer[] items)
	{
		setConstructionBuilding(buildings);
		setConstructionItems(items);
	}
	
}
