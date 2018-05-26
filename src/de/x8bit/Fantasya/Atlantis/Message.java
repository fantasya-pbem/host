package de.x8bit.Fantasya.Atlantis;

import de.x8bit.Fantasya.Atlantis.Helper.MessageCache;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastSaver;
import de.x8bit.Fantasya.util.comparator.MeldungsKategorieComparator;
import de.x8bit.Fantasya.util.PackageLister;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * das neue Message-System ... es werden für jeden Bereich eine entsprechende
 * Klasse angelegt ... die Klasse kümmert sich dann um die Zuweisung zur DB & so
 * 
 * Beachte: Messages werden anhand ihrer EvaId eindeutig identifiziert. Diese Id
 * wird zum Vergleichen mittels equals() und zum Sortieren verwendet, und sollte
 * daher fuer einen gegebenen ZAT eindeutig sein.
 * 
 * @author  mogel
 */
public class Message {
    private static SortedSet<Class<? extends Message>> Arten;
	protected static Map<String, Class<? extends Message>> ART_KEY;

	private Partei partei = null;
	private Unit unit = null;
	private String text = "";
	private Coords coords = null;
    private Date timestamp = null;
	private int evaId = -1;


    private static boolean Mute = false;

	private final static MessageCache Messages = new MessageCache();


	private static int AUTO_INCREMENT = 1;

	/**
	 * Flag - markiert, ob bereit ein Problem beim Parsen eines Datums aufgetreten ist - damit das nur einmal gemeldet wird.
	 */
	private static boolean PARSING_PROBLEM = false;

	
	public Message() {
        this.timestamp = new Date();
		this.evaId = AUTO_INCREMENT++;
    }

	
	@SuppressWarnings("unchecked")
    public static Set<Class<? extends Message>> AlleArten() {
        if (Arten == null) {
            Arten = new TreeSet<Class<? extends Message>>(new MeldungsKategorieComparator());
			ART_KEY = new HashMap<String, Class<? extends Message>>();
            try {
                List<Class> klassen = PackageLister.getClasses("de.x8bit.Fantasya.Atlantis.Messages");
                for (Class clazz : klassen) {
                    Arten.add(clazz);
					ART_KEY.put(clazz.getSimpleName(), clazz);
					ART_KEY.put(clazz.getSimpleName().toLowerCase(), clazz);
					ART_KEY.put(clazz.getSimpleName().toUpperCase(), clazz);

                }
                Arten = Collections.unmodifiableSortedSet(Arten);
				ART_KEY = Collections.unmodifiableMap(ART_KEY);

            } catch (ClassNotFoundException ex) {
                new BigError(ex);
            } catch (IOException ex) {
                new BigError(ex);
            } catch (URISyntaxException ex) {
                new BigError(ex);
            }
        }

        return Arten;
    }

    /**
     * schaltet alle Messages (auch abgeleitete Klassen) "stumm" - es werden 
     * weder Ausgaben nach stdio gemacht noch Objekte in Message.messages 
     * abgelegt.
     */
    public static void Mute() {
        Message.Mute = true;
    }

    /**
     * schaltet die Behandlung aller Messages auf normal - es werden Ausgaben
     * auf stdio gemacht und neue Message-Objekte werden in Message.messages
     * abgelegt.
     */
    public static void Unmute() {
        Message.Mute = false;
    }

    /**
     * @return true, wenn Message.Mute() aktiv ist - @see Mute() @see Unmute()
     */
    public static boolean IsMute() {
        return Message.Mute;
    }

	/**
	 * löscht alle Meldungen aus der Datenbank
	 */
	public static void DeleteAll()
	{
		System.out.println("Lösche alle Meldungen.");
		Messages.clear();
	}

	/** Liefert Messages ohne Beruecksichtigung der Kategorie zurueck. */
    public static List<Message> Retrieve(Partei p, Coords c, Unit u) {
		// Kategorie ignorieren
		return Retrieve(p, c, u, null);
    }

	public static List<Message> Retrieve(Partei p, Coords c, Unit u, String kategorie) {		
		Collection<Message> candidateMessages = Messages;
		
		if (p != null && c != null) {
			candidateMessages = Messages.getAll(c, p.getNummer());
		} else if (p != null) {
			candidateMessages = Messages.getAll(p.getNummer());
		} else if (c != null) {
			candidateMessages = Messages.getAll(c);
		}
		
		List<Message> messages = new ArrayList<Message>();
		for (Message msg : candidateMessages) {
			if (u != null && msg.getUnit() != u) {
				continue;
			}
			if (kategorie != null && !msg.getClass().getSimpleName().equals(kategorie)) {
				continue;
			}
			messages.add(msg);
		}
		
		return messages;
    }

	
	public static void SaveAll() {
		// Datenbank öffnen
        Datenbank db = new Datenbank("Meldungen.SaveAll");

		db.Truncate("meldungen");
		db.DisableKeys("meldungen");

		// Zeitformat holen
		// DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

		int cnt = 1;
		StringBuffer values = new StringBuffer();
		Message proto = null;
		if (Messages.size() > 0) {
			proto = Messages.iterator().next();
		}
		
		for (Message msg : Messages) {
			if (values.length() > 0) values.append(", ");
			values.append(Datenbank.MakeInsertValues(msg.getDBValues()));

			if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
				db.myQuery = EVAFastSaver.makeQuery("meldungen", proto.getDBValues(), values);
				int result = db.Update();

				values = new StringBuffer();
			}
            if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
				System.out.println("Message.SaveAll: " + cnt + " Meldungen...");
			}

			cnt++;
		}

		db.EnableKeys("meldungen");	db.Close();
		new SysMsg("Message.SaveAll: " + cnt + " Meldungen fertig.");
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues() {
		Map<String, Object> fields = new HashMap<String, Object>();

		Coords c = this.getCoords();
		fields.put("partei", getPartei() == null? 0 : getPartei().getNummer());
		fields.put("kategorie", getClass().getSimpleName());
		fields.put("text", getText());
		fields.put("debuglevel", 0);
		fields.put("zeit", new Timestamp(timestamp.getTime()).toString());
		fields.put("koordx", c == null ? 0 : c.getX());
		fields.put("koordy", c == null ? 0 : c.getY());
		fields.put("welt",  c == null ? 0 : c.getWelt());
		fields.put("einheit", getUnit() == null ? 0 : getUnit().getNummer());

		return fields;
	}


	public static Message fromResultSet(ResultSet rs) {
		Message retval = null;

		try {
            try {
                String kategorie = rs.getString("kategorie");
                Object o = Class.forName("de.x8bit.Fantasya.Atlantis.Messages." + kategorie).newInstance();
                if (o instanceof Message) {
                    retval = (Message) o;
                } else {
                    new BigError("Message.fromResultSet: Konnte keine Message erstellen aus " + kategorie);
                }
            } catch (ClassNotFoundException ex) {
                new BigError(ex);
            } catch (InstantiationException ex) {
                new BigError(ex);
            } catch (IllegalAccessException ex) {
                new BigError(ex);
            }

			retval.setText(rs.getString("text"));
			// TODO // retval.setDebugLevel(rs.getInt("debuglevel"));
            try {
                Timestamp mysqlDate = Timestamp.valueOf(rs.getString("zeit"));
                retval.setTimestamp(new Date(mysqlDate.getTime()));
            } catch (IllegalArgumentException ex) {
                if (!Message.PARSING_PROBLEM) {
					new SysErr("PROBLEM mit Timestamp '" + rs.getString("zeit") + "' bei den Meldungen - wenn das außerhalb des Mischbetriebs ZAT/EVA passiert, dann ist es ein Grund nachzuschauen! - " + ex);
					Message.PARSING_PROBLEM = true;
				}
                retval.setTimestamp(new Date(0));
            }

			int parteiNr = rs.getInt("partei");
			if (parteiNr != 0) retval.setPartei(Partei.getPartei(parteiNr));

			int unitNr = rs.getInt("einheit");
			if (unitNr != 0) retval.setUnit(Unit.Load(unitNr));

			int koordx = rs.getInt("koordx");
			int koordy = rs.getInt("koordy");
			int welt = rs.getInt("welt");
			if ((koordx != 0) || (koordy != 0) || (welt != 0)) {
				retval.setCoords(new Coords(koordx, koordy, welt));
			}

		} catch (SQLException ex) {
			new BigError(ex);
		}

		if (retval != null) Messages.add(retval);
		return retval;
	}
	
	/** letzte Meldung merken - für Fehler SMS */
	protected static String lastMessage;
 
	
	/** Initializes a new message and prints it to stdout.
	 * 
	 * @param level The debug level of the message. If it is above the threshold, the message is not printed.
	 * @param msg The message text.
	 * @param partei The player who the message is directed to.
	 * @param coords The coordinates of the region that the message refers to.
	 * @param unit The unit that the message refers to.
	 */
	protected void print(int level, String msg, Partei partei, Coords coords, Unit unit) {
		// first, some sanity checks.
		if (msg == null || msg.trim().isEmpty()) {
			throw new IllegalArgumentException("Messages must have a text!");
		}

		// print the message to stdout
		if (!Mute) {
			String kategorie = "[" + this.getClass().getSimpleName() + "] ";
		    if (kategorie.equals("[Battle] ")) kategorie = "";
			System.out.println(kategorie + msg);
		}
		
		this.text = msg;
		this.partei = partei;
		this.coords = coords;
		this.unit = unit;
        Messages.add(this);
	}
	
	/** Convenience function if only the party is set. */
	protected void print(int level, String msg, Partei partei) {
		this.print(level, msg, partei, null, null);
	}
	
	/** Convenience function if only the party and the coordinates are set. */
	protected void print(int level, String msg, Partei partei, Coords coords) {
		this.print(level, msg, partei, coords, null);
	}

	/** Convenience funtion if only the unit and the coordinates are set.
	 * 
	 * The party is taken from the unit's owner. The unit must not be null.
	 */
	protected void print(int level, String msg, Coords coords, Unit u) {
		this.print(level, msg, Partei.getPartei(u.getOwner()), coords, u);
	}
	
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + this.evaId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Message other = (Message) obj;
		if (this.evaId != other.evaId) {
			return false;
		}
		return true;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(" ");
		sb.append("id[").append(evaId).append("]");
		if (getPartei() != null) sb.append("P[").append(getPartei().getNummerBase36()).append("] ");
		if (getUnit() != null) sb.append("U[").append(getUnit().getNummerBase36()).append("] ");
		if (getCoords() != null) sb.append(getCoords()).append(" ");
		int cut = 40;
		if (getText().length() > cut) {
			sb.append("'").append(getText().substring(0, cut-1)).append("'");
		}
		
		return sb.toString();
	}



	public static int TotalCount() {
		return Messages.size();
	}

	
	public void setPartei(Partei partei) {
		this.partei = partei;
	}
	public Partei getPartei() {
		return partei;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public Unit getUnit() {
		return unit;
	}

	public void setText(String message) {
		this.text = message;
	}
	public String getText() {
		return text;
	}

	public void setCoords(Coords coords) {
		this.coords = coords;
	}
	public Coords getCoords() {
		return coords;
	}

	protected void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getEvaId() {
		return evaId;
	}

	public static MessageCache Cache() {
		return Messages;
	}
}
