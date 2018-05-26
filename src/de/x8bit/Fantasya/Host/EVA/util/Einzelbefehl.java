package de.x8bit.Fantasya.Host.EVA.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.EVABase;
import de.x8bit.Fantasya.Host.EVA.Kommentare;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import java.util.Collections;

/**
 * Kapselt eine einzelne Befehlszeile einer Einheit - d.h. jeder Einheit
 * können 0 bis N dieser Objekte zugeordnet sein.
 * @see BefehlsListe
 * @author hapebe
 */
public class Einzelbefehl {
    final Coords coords;
	final Unit unit;
	final String befehl;
	int sortRank;

	boolean parsed = false;
	boolean performed = false;
	boolean error = false;

	/** dieses Flag bestimmt, ob ein Befehl in die neue Befehlsvorlage übernommen wird ("langer Befehl") */
	Boolean keep = null;
	/** true genau dann, wenn der Befehl mit "@" gekennzeichnet worden ist / werden soll */
	boolean always = false;

    BefehlsMuster muster;
	protected String[] tokens;
	/**
	 * zusätzlich zu den "aktiven" Befehls-Tokens kann ein //-Kommentar enthalten sein, der hier einfach am Stück gespeichert wird
	 */
	protected String comment;

	String targetUnit;
	String targetId;
	int anzahl = -1;
	Class<? extends Item> item;
	Class<? extends Spell> spell;
    Class<? extends Skill> skill;
	/**
	 * Falls der Befehl zu einer Bewegung in andere Regionen geführt hat, soll diese Liste alle Stationen der Reise enthalten - AUSSER der letzten, aktuellen Region.
	 */
    protected List<Region> reise = null;

	public Einzelbefehl(Unit u, String befehl) {
        this(u, u.getCoords(), befehl, u.BefehleExperimental.size());
    }

    public Einzelbefehl(Unit u, String befehl, int sortRank) {
        this(u, u.getCoords(), befehl, sortRank);
    }

	public Einzelbefehl(Unit u, Coords c, String befehl, int sortRank) {
		if (befehl.trim().isEmpty()) {
			new SysErr("Leerer String als Befehl für " + u + " in " + c + "?");
		}

		// globale Befehls-Modifikatoren:
		this.recreateTokens(befehl.split("\\ "));
		boolean modified = false;

		if (tokens.length > 0) {
			// "@"
			if (getTokens()[0].startsWith("@")) {
				modified = true;
				this.setAlways(true);
				tokens[0] = tokens[0].substring(1).trim();
			}
		}

		// "//"
		setComment(null);
		for (int i = 1; i < tokens.length; i++) {
			if (tokens[i].startsWith("//")) {
				modified = true;
				combineTokens(i, tokens.length - 1); // damit werden die tokens neu gesetzt, i ist dann das letzte Token.
				setComment(tokens[i].substring(2).trim());
				tokens[i] = ""; // aus dem regulären Befehl entfernen
			}
		}

		if (modified) {
			recreateTokens(tokens);
			befehl = recreateBefehl(tokens);
		}


		this.muster = BefehlsSpeicher.getInstance().parse(befehl);
        if (this.muster == null) throw new IllegalArgumentException("'" + befehl + "' ist kein gültiger Befehl.");

		this.unit = u;
        this.coords = c;
		this.befehl = befehl;
		this.sortRank = sortRank;
		
		// this.recreateTokens(befehl.split("\\ "));

		this.parseHints();
	}

	public String getBefehl() {
		return befehl;
	}

	public void setTokens(String[] tokens) {
		this.tokens = tokens;
	}

	/**
	 * @return alle semantischen Einheiten des Befehls - bei Index 0 das Befehls-Verb, ab 1 dann ggf. die Parameter.
	 */
	public String[] getTokens() {
		if (!parsed) this.parseHints();
		return tokens;
	}

	/**
	 * @return ein inline-Kommentar ohne "//" - oder null, wenn es keinen gibt
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment ein inline-Kommentar ohne "//"
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return Gibt den Befehl so zurück, wie ihn der Host verstanden hat und als "korrektestmöglich" ansieht.
	 */
	public String getBefehlCanonical() {
		if (!parsed) this.parseHints();

        StringBuilder sb = new StringBuilder();
		if (isAlways()) sb.append("@");

		sb.append(recreateBefehl(tokens));

		if (getComment() != null) {
			sb.append(" //").append(getComment());
		}

		return sb.toString();
	}

	public Unit getUnit() {
		if (!parsed) this.parseHints();
		return unit;
	}

	public Coords getCoords() {
		return coords;
	}

	public void setSortRank(int sortRank) {
		this.sortRank = sortRank;
	}

	public int getSortRank() {
		return sortRank;
	}

    public BefehlsMuster getMuster() {
        return muster;
    }

    public void setMuster(BefehlsMuster muster) {
        this.muster = muster;
    }

    public Class<? extends EVABase> getProzessor() {
		if (this.getMuster() == null) return null;
		
        return this.getMuster().getProzessor();
    }

    public int getVariante() {
        return this.getMuster().getVariante();
    }

    /**
     * @return KURZ, LANG oder MULTI-LANG (z.B. HANDEL)
     */
    public Art getArt() {
        return this.getMuster().getArt();
    }

	public boolean isPerformed() {
		return performed;
	}

	public void setPerformed() {
		this.performed = true;
	}

	/**
	 * setzt gleichzeitig den Status auf 'performed' - bezieht sich also nur auf Fehler,
	 * die bei der Abarbeitung aufgetreten sind!!!
	 */
	public void setError() {
		this.setPerformed();
		this.error = true;
	}

    public boolean isError() {
        return error;
    }

	/**
	 * <p>Berücksichtigt auch das "always"-Flag (@....)</p>
	 * @return true, wenn der Befehl in die neue Befehlsvorlage aufgenommen werden soll.
	 */
	public boolean isKeep() {
		if (isAlways()) return true; // @-Befehle immer übernehmen

		if (keep == null) {
			if (getMuster() != null) {
				keep = getMuster().isKeep();
			} else {
				return false;
			}
		}
		return keep.booleanValue();
	}

	/**
	 * @param keep wenn true, wird der Befehl in die neue Befehlsvorlage aufgenommen.
	 */
	public void setKeep(boolean keep) {
		this.keep = keep;
	}

	/**
	 * @return true genau dann, wenn der Befehl mit "@" gekennzeichnet worden ist / werden soll
	 */
	public boolean isAlways() {
		return always;
	}

	/**
	 * @param always true genau dann, wenn der Befehl mit "@" gekennzeichnet worden ist / werden soll
	 */
	public void setAlways(boolean always) {
		this.always = always;
	}

	/**
	 * Dient dazu, TEMP-Nummern durch die permanente Nummer zu ersetzen.
	 * @param newUnit Neue, permanente Unit-ID (Base36)
	 */
	public void setTargetUnit(String newUnit) {
		if (!parsed) this.parseHints();
		if (this.getTargetUnit() == null) throw new RuntimeException("In diesem Befehl gibt es gar keine Ziel-Einheit - " + this);

		targetUnit = newUnit;

		for (ParamHint h : this.getMuster().getHints()) {
			if (h.getType() == Unit.class) {
				tokens[h.getPosition()] = newUnit;
				break;
			}
		}
	}

	public String getTargetUnit() {
		if (!parsed) this.parseHints();
		return targetUnit;
	}

	public String getTargetId() {
		if (!parsed) this.parseHints();
		return targetId;
	}

	public int getAnzahl() {
		if (!parsed) this.parseHints();
		return anzahl;
	}

	public Class<? extends Item> getItem() {
		if (!parsed) this.parseHints();
		return item;
	}

	public Class<? extends Spell> getSpell() {
		if (!parsed) this.parseHints();
		return spell;
	}

	public Class<? extends Skill> getSkill() {
		if (!parsed) this.parseHints();
		return skill;
	}

	public List<Region> getReise() {
		if (reise == null) return null;
		return Collections.unmodifiableList(reise);
	}

	public void setReise(List<Region> reise) {
		this.reise = reise;
	}



	/**
	 * <p>Kann auch von außen benutzt werden, um die Interpretation in "Tokens" zu ändern
	 * (bspw. Item- und Zaubernamen, die Leerzeichen enthalten!) - siehe parse() und getBefehlCanonical()</p>
	 * <p>Wandelt den Text-Befehl nach dem Standard-Verfahren in logische "Tokens"
	 * um - d.h. Bestandteile innerhalb von &quot;&quot; werden als zusammenhängend
	 * behandelt.</p>
	 *
	 * @param parts an Leerzeichen gesplitteter Text
	 */
	public void recreateTokens(String[] parts) {
		boolean quotation = false;
		StringBuilder current = null;
		List<String> als = new ArrayList<String>();

		for(int i = 0; i < parts.length; i++) {
			parts[i] = parts[i].trim();

			// wenn das Token leer ist:
			if (parts[i].isEmpty()) continue;

			// wenn das Token Leerzeichen enthält, aber nicht in " eingeschlossen ist,
			// dann erledigen wir das jetzt:
			if ( (parts[i].contains(" ")) && (!parts[i].startsWith("\"")) && (!parts[i].toUpperCase().startsWith("TEMP")) )
                parts[i] = "\"" + parts[i] + "\"";
			if (current == null) {
				current = new StringBuilder(parts[i]);
			} else {
				current = current.append(" ").append(parts[i]);
			}
			if (parts[i].startsWith("\"")) quotation = true;
			if (quotation) {
				if (parts[i].endsWith("\"")) {
					als.add(current.toString().replace("\"", ""));
					current = null;
					quotation = false;
				} else {
					// ein kleines Zwischenstück ... zb. "der" oder "tausend" bei "Hain der tausend Eichen"
					// braucht nix gemacht werden
				}
			} else {
				als.add(current.toString());
				current = null;
			}
		}
		if (quotation) {
			// nicht geschlossenes " !
			als.add(current.toString().replace("\"", ""));
			quotation = false; // Ordnung muss sein. (?)
			current = null;
		}

		this.tokens = new String[als.size()];
		for(int i = 0; i < als.size(); i++) tokens[i] = als.get(i);
	}

	public String recreateBefehl(String[] tokens) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < tokens.length; i++) {
			// TODO statt des Ursprungsbefehls die "kanonische" Variante zurückgeben, bspw. 'LERNE' statt 'Lernen'

			if (sb.length() > 0) sb.append(" ");
			
            // wenn das Token Leerzeichen enthält, aber nicht in " eingeschlossen ist,
			// dann erledigen wir das jetzt:
			boolean einfassen = tokens[i].contains(" ");
            if (einfassen && tokens[i].toUpperCase().startsWith("TEMP")) tokens[i] = tokens[i].toUpperCase(); // TEMP groß schreiben
			if (tokens[i].startsWith("\"")) einfassen = false; // ist schon angeführt!
			if (tokens[i].startsWith("(")) einfassen = false; // Koordinaten
			if (tokens[i].toUpperCase().startsWith("TEMP")) einfassen = false;
			if (einfassen) {
                sb.append("\"").append(tokens[i]).append("\"");
            } else {
				if (i == 0) {
					// Befehle immer in Großbuchstaben, ...
					if ( 
						(this.getProzessor() == null) 
						|| (this.getProzessor() != Kommentare.class)
					) {
						sb.append(tokens[i].toUpperCase());
					} else {
						// ... außer Kommentare!
						sb.append(tokens[i]);
					}
				} else {
					sb.append(tokens[i]);
				}
            }
        }
		return sb.toString();
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues(Unit u) {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("nummer", u.getNummer());
		fields.put("sortierung", this.getSortRank());
		fields.put("befehl", this.getBefehlCanonical());

		return fields;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		StringBuilder tokensTxt = new StringBuilder();
		int i = 0;
		for (String token : this.getTokens()) {
			if (i > 0) tokensTxt.append("|");
			if ((i == 0) && isAlways()) tokensTxt.append("@");
			tokensTxt.append(token);
			i ++;
		}
		if (getComment() != null) tokensTxt.append("//" + getComment());

		sb.append("'" + tokensTxt + "', ");

		if (this.getTargetUnit() != null) sb.append("u>"+this.getTargetUnit() + ", ");
		if (this.getTargetId() != null) sb.append("ID>"+this.getTargetId() + ", ");
		if (this.getItem() != null) sb.append("it>"+this.getItem().getSimpleName() + ", ");
		if (this.getSpell() != null) sb.append("Spell>"+this.getSpell().getSimpleName() + ", ");
		if (this.getAnzahl() >= 0) sb.append("*"+this.getAnzahl() + ", ");

		sb.append(this.getUnit() + "#" + this.getSortRank() + ", ");
		if (this.getProzessor() != null) {
            sb.append(this.getProzessor().getSimpleName());
            if (this.getVariante() != 0) sb.append("#" + this.getVariante());
			sb.append(" - ");
        }
		sb.append(this.getArt());

        if (this.error) {
            sb.append(", ERROR!!!");
        } else {
            if (this.isPerformed()) sb.append(", done");
        }

		if (this.isKeep()) {
			sb.append(", keep.");
		} else {
			sb.append(", dont keep.");
		}

		return sb.toString();
	}

	private void parseHints() {
		// TODO: What's wrong here?
		if (this.getMuster() == null) return;

		// System.out.println(this.getBefehl() + "#" + this.getVariante() + " - " + this.tokens.length);

		for (ParamHint h : this.getMuster().getHints()) {
			@SuppressWarnings("rawtypes")
			Class scope = h.getType();

			if (scope == null) {
				// das nehmen wir mal als Kennzeichen von "anzahl":
				int position = h.getPosition();
				if (position == Integer.MAX_VALUE) position = tokens.length - 1;
                try {
                    this.anzahl = Integer.parseInt(tokens[position]);
                } catch (NumberFormatException ex) {
                    throw new NumberFormatException(this.getBefehl() + " / " + position + ".'" + tokens[position] + "' - Die Exception sagt: " + ex.getMessage());
                }
				continue;
			}

			if (scope == Atlantis.class) {
				// das bedeutet: Eine ID, egal wofür.
				int position = h.getPosition();
				if (position == Integer.MAX_VALUE) position = tokens.length - 1;

				this.targetId = tokens[position];
				continue;
			}

			if (scope == Unit.class) {
				int position = h.getPosition();
				if (position == Integer.MAX_VALUE) {
                    position = tokens.length - 1;
                    if (this.getMuster().isTempMuster()) position --;
                }

				this.targetUnit = tokens[position];
				if (this.getMuster().isTempMuster()) {
                    this.targetUnit += " " + tokens[position + 1];
                    this.combineTokens(position, position + 1);
                }
				continue;
			}

			if (scope == Skill.class) {
				int position = h.getPosition();
				if (position == Integer.MAX_VALUE) {
                    position = tokens.length - 1;
                    if (this.getMuster().isTempMuster()) position --;
                }

                this.skill = Skill.getFor(tokens[position].toLowerCase());
				if (this.skill == null) throw new IllegalArgumentException("Skill in '" + this.getBefehl() + "' nicht erkannt.");

				continue;
			}

			if (scope == Item.class) {
				int position = h.getPosition();
				if (position == Integer.MAX_VALUE) position = tokens.length - 1;

				boolean found = false;
				String myItem = "";
				for (int i = position; i < tokens.length; i++) {
					myItem = myItem + (myItem.length() > 0?" ":"") + tokens[i].toLowerCase();
					if (Item.getFor(myItem) != null) {
						found = true;

						this.item = Item.getFor(myItem);

						if (i > position) this.combineTokens(position, i);
						break;
					}
				}

                if ((!found) && (h.getPosition()) == Integer.MAX_VALUE) {
                    // arrrgh - von hinten nach vorn nach dem Item suchen.
                    myItem = "";
                    for (int i = tokens.length - 1; i >= 0; i--) {
                        myItem = tokens[i].toLowerCase() + (myItem.length() > 0?" ":"") + myItem;
                        if (Item.getFor(myItem) != null) {
                            found = true;

                            this.item = Item.getFor(myItem);

                            if (i < (tokens.length - 1)) this.combineTokens(i, tokens.length - 1);
                            break;
                        }
                    }
                }

				if (!found) throw new IllegalArgumentException("Item in '" + this.getBefehl() + "' nicht erkannt.");
				continue;
			}

			if (scope == Spell.class) {
				int position = h.getPosition();
				if (position == Integer.MAX_VALUE) position = tokens.length - 1;

				boolean found = false;
				String mySpell = "";
				for (int i = position; i < tokens.length; i++) {
					mySpell = mySpell + (mySpell.length() > 0?" ":"") + tokens[i].toLowerCase();
					if (BefehlsMuster.SPELL_KEY.containsKey(mySpell)) {
						found = true;

						this.spell = BefehlsMuster.SPELL_KEY.get(mySpell);

						if (i > position) this.combineTokens(position, i);
						break;
					}
				}
				if (!found) throw new IllegalArgumentException("Spell in '" + this.getBefehl() + "' nicht erkannt.");
			}

			if (h instanceof MultiCoordsHint) {
				// betrifft bis jetzt: NACH (x0 y0) (x1 y1) PAUSE (x2 y2) - und das gleich mit ROUTE
				// Koordinaten in Klammern jeweils paarweise zu einem Token zusammenfassen:
				List<String> neu = new ArrayList<String>();
				neu.add(tokens[0]); // der Befehl / das Verb
				for (int i=1; i<tokens.length; i++) {
					String token = tokens[i];
					if (token.startsWith("(")) {
						// Dass Y (also [i+1]) vorhanden ist, wird per RegEx des BefehlsMusters geprüft - sonst nicht.
						neu.add(tokens[i] + " " + tokens[i+1]);
						i++; // Die Y-Koordinate wird jeweils übersprungen.
					} else if (token.equalsIgnoreCase("pause")) {
						neu.add("pause");
					}
				}
				
				String[] neueTokens = new String[neu.size()];
				for (int i=0; i<neu.size(); i++) neueTokens[i] = neu.get(i);

				this.setTokens(neueTokens);
			}
		}
		parsed = true;
	}

	/**
	 * Kann benutzt werden, um z.B. Item-Namen wie "Amulett der Heilung" oder TEMP-Nummern "TEMP abc" zusammenzufassen,
	 * wenn sie denn logisch zusammen gehören.
	 * @param from
	 * @param to
	 */
	public void combineTokens(int from, int to) {
        if (to > tokens.length - 1) to = tokens.length - 1;

		int shortenBy = to - from;
		String[] newTokens = new String[tokens.length - shortenBy];

		// Tokens davor:
		for (int i=0; i<from; i++) newTokens[i] = tokens[i];

		// Das Kombi-Token:
		StringBuilder combined = new StringBuilder();
		for (int i=from; i <= to; i++) {
			if (i > from) combined.append(" ");
			combined.append(tokens[i]);
		}
		newTokens[from] = combined.toString();

		// Tokens danach:
		for (int i=to + 1; i<tokens.length; i++) {
			int idxNew = i - shortenBy;
			newTokens[idxNew] = tokens[i];
		}

		this.tokens = newTokens;
	}

}
