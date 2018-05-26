package de.x8bit.Fantasya.Atlantis;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.util.Codierung;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author  mogel
 */
public class Allianz
{
	public Allianz() {
		// throw new RuntimeException("was, Allianzen??");
	}

	/**
	 * Die Partei, die die Allianz "erklärt"
	 */
	private int partei = 0;

	/**
	 * Die Partei, die die Allianz "erklärt"
	 */
	public void setPartei(int partei) {
		this.partei = partei;
	}

	/**
	 * der Allianz-Partner
	 * @uml.property  name="partner"
	 */
	private int partner = 0;
	/**
	 * @return
	 * @uml.property  name="partner"
	 */
	public int getPartner() { return partner; }
	/**
	 * @param value
	 * @uml.property  name="partner"
	 */
	public void setPartner(int value) { partner = value; }
	
	/**
	 * Optionen ... im Moment eigentlich nur Ja/Nein(/vieleicht ^^)
	 * @uml.property  name="optionen"
	 */
	private int optionen = 0;
	/**
	 * @return
	 * @uml.property  name="optionen"
	 */
	public int getOptionen() { return optionen; }
	/**
	 * @param value
	 * @uml.property  name="optionen"
	 */
	public void setOptionen(int value) { optionen = value; }
	public void setOption(AllianzOption ao, boolean yes)
	{
		if (ao == AllianzOption.Alles)
		{
			optionen = yes ? 0xffffffff : 0;
		} else
		{
			if (yes)
			{
				optionen |= (int) Math.pow((double) 2, (double) ao.ordinal());
			} else
			{
				optionen &= ~((int) Math.pow((double) 2, (double) ao.ordinal()));
			}
		}
	}
	
	public boolean getOption(AllianzOption ao)
	{
		int o = ((int) Math.pow((double) 2, (double) ao.ordinal()));
		return (o & optionen) == o;
	}

	public void readResultSet(ResultSet rs) {
		try {
			if (partei == 0) {
				setPartei(rs.getInt("partei"));
			} else {
				if (partei != rs.getInt("partei")) {
					new BigError("Allianz.readResultSet() - Versuch, eine AllianzOption zu einer fremden Allianz hinzuzufügen.");
				}
			}

			if (this.getPartner() == 0) {
				setPartner(rs.getInt("partner"));
			} else {
				if (this.getPartner() != rs.getInt("partner")) {
					new BigError("Allianz.readResultSet() - Versuch, eine AllianzOption zu einer falschen Allianz hinzuzufügen.");
				}
			}

			setOption(AllianzOption.ordinal(rs.getString("optionen")), true);
		} catch (SQLException ex) {
			new BigError(ex);
		}
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert für EINE AllianzOption dieser Allianz, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues(AllianzOption ao) {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("partei", partei);
		fields.put("partner", this.getPartner());
		fields.put("optionen", ao.name());
		
		return fields;
	}


	public boolean isValid() {
		Partei me = Partei.getPartei(partei);
		if (me == null) {
			new SysMsg("Allianz von [" + Codierung.toBase36(getPartner())+ "] ist ungültig: Partei " + Codierung.toBase36(this.partei) + " gibt es nicht (mehr)");
			return false;
		}
		Partei them = Partei.getPartei(this.getPartner());
		if (them == null) {
			new SysMsg("Allianz von " + me+  " ist ungültig: Partner " + Codierung.toBase36(this.getPartner()) + " gibt es nicht (mehr)");
			return false;
		}

		int cnt = 0;
		for (AllianzOption ao : AllianzOption.values()) {
			if (ao == AllianzOption.Alles) continue;
			if (this.getOption(ao)) cnt++;
		}
		if (cnt == 0) return false;

		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (AllianzOption ao : AllianzOption.values()) {
			if (ao == AllianzOption.Alles) continue;

			if (this.getOption(ao)) {
				if (sb.length() > 0) sb.append(", ");
				sb.append(ao.name());
			}
		}

		if (sb.length() == 0) sb.append(" --KEINE OPTIONEN--");

		return "Allianz von " + this.partei + " mit " + this.getPartner() + ": " + sb;
	}




	public static List<Allianz> getAlle() {
		List<Allianz> alle = new ArrayList<Allianz>();

		for (Partei p: Partei.PROXY) {
			for (Integer partnerNr : p.getAllianzen().keySet()) {
				alle.add(p.getAllianzen().get(partnerNr));
			}
		}

		return alle;
	}

	
	/**
	 * alle möglichen Allianzoptionen
	 * @author   mogel
	 */
	public enum AllianzOption
	{
		/**
		 * @uml.property  name="kaempfe"
		 * @uml.associationEnd  
		 */
		Kaempfe,
		/**
		 * @uml.property  name="gib"
		 * @uml.associationEnd  
		 */
		Gib,
		/**
		 * @uml.property  name="resourcen"
		 * @uml.associationEnd  
		 */
		Resourcen,
		/**
		 * @uml.property  name="treiben"
		 * @uml.associationEnd  
		 */
		Treiben,
		/**
		 * @uml.property  name="handel"
		 * @uml.associationEnd  
		 */
		Handel,
		/**
		 * @uml.property  name="unterhalte"
		 * @uml.associationEnd  
		 */
		Unterhalte,
		/**
		 * @uml.property  name="kontaktiere"
		 * @uml.associationEnd  
		 */
		Kontaktiere,
		/**
		 * @uml.property  name="steuern"
		 * @uml.associationEnd  
		 */
		Steuern,
		/**
		 * @uml.property  name="alles"
		 * @uml.associationEnd  
		 */
		Alles;
		
		/**
		 * liefert den Enum zum String zurück oder NULL falls nicht verfügbar
		 */
		public static AllianzOption ordinal(String option)
		{
			for(AllianzOption ao : AllianzOption.values())
			{
				if (ao.name().toLowerCase().equals(option.toLowerCase())) return ao;
			}
			new SysMsg("AllianzOption '" + option + "' ist unbekannt");
			return null;
		}
	}
}
