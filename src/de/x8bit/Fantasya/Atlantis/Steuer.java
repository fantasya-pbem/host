package de.x8bit.Fantasya.Atlantis;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author  mogel
 */
public class Steuer
{
	/**
	 * legt eine neue Steuerrate fest
	 * @param owner - diese Partei legt die Rate fest
	 * @param rate - diese Steuerrate wird verlangt
	 * @param faction - diese Partei hat zu zahlen
	 */
	public Steuer(int owner, int rate, int faction)
	{
		this.owner = owner;
		this.rate = rate;
		this.faction = faction;
	}
	
	/**
	 * @uml.property  name="owner"
	 */
	private int owner = 0;
	/**
	 * @uml.property  name="rate"
	 */
	private int rate = 0;
	/**
	 * @uml.property  name="faction"
	 */
	private int faction = 0;
	
	/**
	 * @return diese Partei legt die Rate fest
	 * @uml.property  name="owner"
	 */
	public int getOwner() { return owner; };
	/**
	 * diese Partei legt die Rate fest
	 * @param owner
	 * @uml.property  name="owner"
	 */
	public void setOwner(int owner) { this.owner = owner; }
	
	/**
	 * @return diese Steuerrate wird verlangt
	 * @uml.property  name="rate"
	 */
	public int getRate() { return rate; }
	/**
	 * diese Steuerrate wird verlangt
	 * @param rate
	 * @uml.property  name="rate"
	 */
	public void setRate(int rate) { this.rate = rate; }
	
	/**
	 * @return diese Partei hat zu zahlen
	 * @uml.property  name="faction"
	 */
	public int getFaction() { return faction; }
	/**
	 * diese Partei hat zu zahlen
	 * @param faction
	 * @uml.property  name="faction"
	 */
	public void setFaction(int faction) { this.faction = faction; }

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues() {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("owner", this.getOwner());
		fields.put("rate", this.getRate());
		fields.put("partei", this.getFaction());

		return fields;
	}




	public static Steuer fromResultSet(ResultSet rs) {
		Steuer s = null;
		try {
			s = new Steuer(rs.getInt("owner"), rs.getInt("rate"), rs.getInt("partei"));
		} catch (SQLException ex) {
			new BigError(ex);
		}

		return s;
	}
}
