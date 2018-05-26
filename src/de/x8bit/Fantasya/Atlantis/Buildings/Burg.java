package de.x8bit.Fantasya.Atlantis.Buildings;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.Host.Reports.Writer.CRWriter;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.ComplexName;

public class Burg extends Building {

	public Burg() {
	}

	@Override
	public boolean hatFunktion() {
		return true;
	} // Burgen müssen nur anwesend sein

	/** 
	 * der Name der Burg ist abhängig von der Anzahl
	 * der verbauten Steine
	 */
	public String getBurgTyp() {
		if (getSize() < 2) {
			return "Baustelle";
		}
		if (getSize() < 10) {
			return "Befestigung";
		}
		if (getSize() < 50) {
			return "Turm";
		}
		if (getSize() < 250) {
			return "Schloss";
		}
		if (getSize() < 1250) {
			return "Festung";
		}
		return "Zitadelle";
	}
	
	/** 
	 * die Größe der Burg ist abhängig von der Anzahl
	 * der verbauten Steine
	 */
	public int getBurgSize() {
		if (getSize() < 2) {
			return 1;
		}
		if (getSize() < 10) {
			return 2;
		}
		if (getSize() < 50) {
			return 3;
		}
		if (getSize() < 250) {
			return 4;
		}
		if (getSize() < 1250) {
			return 5;
		}
		return 6;
	}
	

	/**
	 * @return der Bonus, der auf AV und DV der Insassen angewendet wird
	 */
	public int getKampfBonus() {
		if (getSize() < 2) {
			return 0;
		}
		if (getSize() < 10) {
			return 0;
		}
		if (getSize() < 50) {
			return 1;
		}
		if (getSize() < 250) {
			return 2;
		}
		if (getSize() < 1250) {
			return 3;
		}
		return 4;
	}

	/**
	 * Zerstörung des Gebäudes beginnen
	 */
	@Override
	public void Zerstoere(Unit u) {
		super.Zerstoere(u, new Item[]{new Stein(1)});
	}

	/**
	 * erstellt ein Gebäude bzw. baut daran weiter
	 * @param u - diese Einheit will daran bauen
	 */
	@Override
	public void Mache(Unit u) {
		// nochmal holen ... ist größer als Null
		int tw = u.Talentwert(Burgenbau.class);
		if (tw < NeededTalent()) {
			new Fehler(u + " hat nicht genügend Talent um an " + this + " zu bauen.", u, u.getCoords());
			return;
		}

		int build = 0;
		int count = tw * u.getPersonen();
		while (count >= NeededTalent()) {
			count -= NeededTalent();
			Item item = u.getItem(Stein.class);
			if (item.getAnzahl() > 0) {
				this.setSize(this.getSize() + 1);
				item.setAnzahl(item.getAnzahl() - 1);
				build++;
			} else {
				new Fehler("Keine Steine mehr vorhanden auf der Baustelle von " + this + ".", u, u.getCoords());
				break;
			}
		}
		new Info(u + " baut " + build + " Größenpunkte an " + this + " weiter.", u);
	}

	/** berechnet das nötige Talent zum Bauen der Burgen */
	private int NeededTalent() {
		if (getSize() < 2) {
			return 1;
		}
		if (getSize() < 10) {
			return 2;
		}
		if (getSize() < 50) {
			return 3;
		}
		if (getSize() < 250) {
			return 4;
		}
		if (getSize() < 1250) {
			return 5;
		}
		return 6;
	}

	/** berechnet den erreichbaren Lohn in der Region */
	public int getLohn() {
		if (getSize() < 2) {
			return 11;
		}
		if (getSize() < 10) {
			return 12;
		}
		if (getSize() < 50) {
			return 13;
		}
		if (getSize() < 250) {
			return 14;
		}
		if (getSize() < 1250) {
			return 15;
		}
		return 16;
	}

	/**
	 * speichert das Gebäude in den CR
	 * @param writer - passender ReportWriter
	 * @param partei - die Partei für diese Region
	 * @param kurz - TRUE wenn keine eigenen Einheiten hier
	 */
	@Override
	public void SaveCR(CRWriter writer, Partei partei, boolean kurz) {
		writer.wl("BURG " + getNummer());
		writer.wl(getBurgTyp(), "Typ");
		writer.wl(getName(), "Name");
		if (getBeschreibung().length() > 0) {
			writer.wl(getBeschreibung(), "Beschr");
		}
		writer.wl(getSize(), "Groesse");
		if (getOwner() != 0) {
			Unit u = Unit.Load(getOwner());
			if (u != null) {
				writer.wl(getOwner(), "Besitzer");
				writer.wl(u.getTarnPartei(), "Partei");
			} else {
				new SysErr("Owner [" + Codierung.toBase36(getOwner()) + "] existiert nicht mehr für Gebäude [" + this.getNummerBase36() + "].");
				setOwner(0);
			}
		}
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Burg", "Burgen", null);
	}
}
