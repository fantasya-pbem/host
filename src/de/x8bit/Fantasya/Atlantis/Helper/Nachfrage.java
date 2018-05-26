package de.x8bit.Fantasya.Atlantis.Helper;

import java.util.HashMap;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;

public class Nachfrage
{
	private Class<? extends Item> item;
	private float nachfrage;
	
	public Class<? extends Item> getItem() { return item; }
	
	public void setNachfrage(float value) { nachfrage = value; }
	public float getNachfrage() { return nachfrage; }
	
	public Nachfrage(Class<? extends Item> clazz, float value)
	{
		item = clazz;
		nachfrage = value;

		if (nachfrage > 999.9f) {
			nachfrage /= 1000;
		}
	}

	@SuppressWarnings("unchecked")
	public Nachfrage(String string, float value)
	{
		try
		{
			item = (Class<? extends Item>) Class.forName("de.x8bit.Fantasya.Atlantis.Items." + string);
			nachfrage = value;
		} catch (ClassNotFoundException e)
		{
			new BigError(e);
		}
	}
	
	/**
	 * ändert den Preis des Produktes -> Handeln
	 */
	public void Handeln()
	{
		if (nachfrage < 0)
		{
			 nachfrage += 1.5;
			 if (nachfrage > -1.0) nachfrage = (float) -1.0;
		} else
		{
			nachfrage += 0.25;
			if (nachfrage > 100.0) nachfrage = (float) 100.0;
		}
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle (Regions-Resourcen) entspricht
	 */
	public Map<String, Object> getDBValues(Coords c) {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("koordx", c.getX());
		fields.put("koordy", c.getY());
		fields.put("welt", c.getWelt());
		fields.put("nachfrage", Math.round(this.getNachfrage() * 1000f));
		fields.put("luxus", this.getItem().getSimpleName());

		return fields;
	}


}
