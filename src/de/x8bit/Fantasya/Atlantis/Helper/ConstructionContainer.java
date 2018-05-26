package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Atlantis;

/**
 * alles was gebaut werden kann besitzt einen Konstruktionsplan ... dieser Container
 * beinhaltet die Anzahl von dessen was benötigt wird ... und wieviel davon
 * @author mogel
 */
public class ConstructionContainer {

	private Class<? extends Atlantis> clazz;
	private int value;

	public void setValue(int value)							{ this.value = value; }
	public int getValue() 									{ return value; }
	public void setClazz(Class<? extends Atlantis> clazz)	{ this.clazz = clazz; }
	public Class<? extends Atlantis> getClazz() 			{ return clazz; }
	
	/**
	 * erzeugt einen neuen Konstruktions-Container
	 * @param clazz - dieser Skill/Item/Building/Ship/Region
	 * @param value - mind. dieses Talent
	 */
	public ConstructionContainer(Class<? extends Atlantis> clazz, int value)
	{
		setClazz(clazz);
		setValue(value);
	}
	
}
