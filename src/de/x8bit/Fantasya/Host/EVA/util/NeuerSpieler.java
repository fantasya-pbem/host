package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.Paket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hb
 */
public class NeuerSpieler {
    public static final List<NeuerSpieler> PROXY = new ArrayList<NeuerSpieler>();

    private String description;
    private String name;
    private String password;
    private int userId;
    
    private String email;
    private Class<? extends Unit> rasse;
    private Class<? extends Unit> tarnung;
    private int holz;
    private int eisen;
    private int steine;
    private int insel;

    public NeuerSpieler() {
    }

	@SuppressWarnings("unchecked")
	public static NeuerSpieler fromResultSet(ResultSet rs) {
		NeuerSpieler n = null;

		try	{
            n = new NeuerSpieler();

			String tarnung = rs.getString("tarnung");

            n.setEmail(rs.getString("email"));
            n.setRasse((Class<? extends Unit>) Paket.FindUnit(rs.getString("rasse")).Klasse.getClass());
            if (tarnung.length() > 0) {
				n.setTarnung((Class<? extends Unit>) Paket.FindUnit(tarnung).Klasse.getClass());
			} else {
				n.setTarnung(null);
			}
            n.setHolz(rs.getInt("holz"));
            n.setEisen(rs.getInt("eisen"));
            n.setSteine(rs.getInt("steine"));
            n.setInsel(rs.getInt("insel"));
		} catch(Exception e) {
			new BigError("Fehler beim Laden eines neuen Spielers " + rs.toString() + ": " + e.getMessage());
		}

		return n;
    }

	public static Map<String, Object> getDBFields() {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("email", null);
		fields.put("rasse", null);
		fields.put("tarnung", null);
		fields.put("holz", null);
		fields.put("eisen", null);
		fields.put("steine", null);
		fields.put("insel", null);

		return fields;
	}

    public Map<String, Object> getDBValues() {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("email", this.getEmail());
		fields.put("rasse", this.getRasse().getSimpleName());
		fields.put("tarnung", this.getTarnung()!=null?this.getTarnung().getSimpleName():"");
		fields.put("holz", this.getHolz());
		fields.put("eisen", this.getEisen());
		fields.put("steine", this.getSteine());
		fields.put("insel", this.getInsel());

		return fields;
	}


    public int getEisen() {
        return eisen;
    }

    public void setEisen(int eisen) {
        this.eisen = eisen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getHolz() {
        return holz;
    }

    public void setHolz(int holz) {
        this.holz = holz;
    }

    public int getInsel() {
        return insel;
    }

    public void setInsel(int insel) {
        this.insel = insel;
    }

    public Class<? extends Unit> getRasse() {
        return rasse;
    }

    public void setRasse(Class<? extends Unit> rasse) {
        this.rasse = rasse;
    }

    public int getSteine() {
        return steine;
    }

    public void setSteine(int steine) {
        this.steine = steine;
    }

    public Class<? extends Unit> getTarnung() {
        return tarnung;
    }

    public void setTarnung(Class<? extends Unit> tarnung) {
        this.tarnung = tarnung;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public String getName() {
    	return name;
    }
    
    public void setDescription(String description) {
    	this.description = description;
    }
    
    public String getDescription() {
    	return description;
    }
    
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void setPassword(String password) {
    	this.password = password;
    }
    
    public String getPassword() {
    	return password;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.email != null ? this.email.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NeuerSpieler)) return false;

        NeuerSpieler other = (NeuerSpieler)o;

        if (!other.getEmail().equalsIgnoreCase(this.getEmail())) return false;

        return true;
    }
}
