package de.x8bit.Fantasya.Host;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.EVA.EVABase;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.PackageLister;
import de.x8bit.Fantasya.util.comparator.SortierungBefehlsComparator;

/**
 *
 * @author hb
 */
public class BefehlsSpeicher {
    List<BefehlsMuster> muster;
    Map<String, List<BefehlsMuster>> musterKey;

	/**
	 * Der "Hauptzwilling" der redundanten Index-Strukturen aller konkreten Befehle
	 */
    List<Einzelbefehl> befehle = new ArrayList<Einzelbefehl>();;

	/**
	 * Ein "Zwilling" - ermöglicht den schnellen Zugriff auf Befehle pro Phase / "Prozessor"
	 * und die Auflistung aller vorhandenen Prozessoren.
	 */
	private BefehleProPhase befehleProPhase = new BefehleProPhase();


    private static BefehlsSpeicher instance = new BefehlsSpeicher();

    public static BefehlsSpeicher getInstance() {
        return instance;
    }

    private BefehlsSpeicher() {
        muster = new ArrayList<BefehlsMuster>();
        prepareMuster();
    }

	/**
	 * Bei Änderungen dieser Methode bitte auch remove(eb) mit ändern.
	 * @param eb
	 * @return
	 */
	public boolean add(Einzelbefehl eb) {
		if (!befehle.add(eb)) return false;
		befehleProPhase.add(eb);
		return true;
	}

	/**
	 * entfernt einen Einzelbefehl aus allen Speichern hier - das ist z.B. nötig, 
	 * wenn TEMP-Einheiten mit Befehlen versorgt werden - in diesem Fall ändert sich 
	 * die ausführende Einheit, was konsequenterweise nur über die Entfernung des 
	 * Ursprungsbefehls und das Einfügen eines neuen erledigt werden kann.
	 * 
	 * Diese Methode muss alle Operationen von add(eb) umkehren!!!
	 * @param eb
	 * @return true, wenn dieser Einzelbefehl tatsächlich entfernt wurde; false, wenn er gar nicht vorhanden war (?)
	 */
	public boolean remove(Einzelbefehl eb) {
		if (!befehle.contains(eb)) return false;
		befehle.remove(eb);

		return befehleProPhase.get(eb.getProzessor()).removeDeep(eb);
	}

    public void clear() {
        befehle.clear();
        befehleProPhase.clear();
    }

	@SuppressWarnings("unchecked")
	public Set<Coords> getCoords(Class<? extends EVABase> prozessor) {
		BefehleProRegion bpr = befehleProPhase.get(prozessor);
		if (bpr != null) return bpr.keySet();

		// new SysMsg("BefehlsSpeicher: Es gibt keine Befehle für " + prozessor.getSimpleName());
		return Collections.EMPTY_SET;
	}

	/**
	 * Beste Methode, um die Befehle jeweils einer "Phase" für jeweils eine Region zu holen.
	 * Sollte hinsichtlich Finden der passenden Befehle so performant wie möglich sein.
	 * @param prozessor Klasse, die in der jeweiligen Phase die Befehle verarbeitet.
	 * @param c Koordinaten der Region
	 * @return Alle passenden Befehle
	 */
	public List<Einzelbefehl> get(Class<? extends EVABase> prozessor, Coords c) {
		List<Einzelbefehl> befehle = befehleProPhase.get(prozessor, c);
		if (befehle == null) {
			new SysErr("Liste der Einzelbefehle ist null für " + prozessor.getSimpleName() + " in " + c + ".");
			return new ArrayList<Einzelbefehl>();
		}

		// wenn es ignorierte Parteien gibt - aussortieren:
		if (ZATMode.CurrentMode().hatIgnorierteParteien()) {
			// erstmal kopieren, damit wir nichts im Speicher kaputt machen:
			List<Einzelbefehl> eigeneListe = new ArrayList<Einzelbefehl>();
			eigeneListe.addAll(befehle);
			befehle = eigeneListe;

			List<Einzelbefehl> loeschliste = new ArrayList<Einzelbefehl>();
			Set<Integer> ignore = ZATMode.CurrentMode().getIgnorierteParteiNummern();
			for (Einzelbefehl eb : befehle) {
				if (ignore.contains(eb.getUnit().getOwner())) loeschliste.add(eb);
			}

			befehle.removeAll(loeschliste);
		}

		Collections.sort(befehle, new SortierungBefehlsComparator());

		return befehle;
	}

    /**
     * (wird z.B. von Kommentare.PreAction() benutzt)
     * @param prozessor
     * @return ALLE Befehle zu einem Prozessor - ungeachtet von Region oder Einheit.
     */
    public List<Einzelbefehl> getAll(Class<? extends EVABase> prozessor) {
        List<Einzelbefehl> retval = new ArrayList<Einzelbefehl>();

        BefehleProRegion bpr = befehleProPhase.get(prozessor);
        if (bpr != null) {
            for (Coords c:bpr.keySet()) {
                retval.addAll(bpr.get(c));
            }
        }

        return retval;
    }

    /**
     * Gibt das passende BefehlsMuster zu einer BefehlsZeile zurück - oder null, wenn kein Muster passt.
     * @param text
     * @return das passende BefehlsMuster - oder null, wenn kein Muster passt
     */
    public BefehlsMuster parse(String text) {
        BefehlsMuster retval = null;

        text = text.trim();
        if (text.length() == 0) return null;
        String lcase = text.toLowerCase();

        String first = lcase.substring(0, 1);
        List<BefehlsMuster> candidates = musterKey.get(first);
        if (candidates == null) {
            new SysMsg("Kein Befehls-Muster-Kandidat für '" + first + "' gefunden?\n"
					+ "\tBefehlszeile: " + text);
            return null;
        }

        for (BefehlsMuster bm : candidates) {
            if (bm.getPattern().matcher(lcase).matches()) {
                // gotcha!
                retval = bm;
                break;
            }
        }

        return retval;
    }

    @SuppressWarnings("rawtypes")
	private void prepareMuster() {
        List<Class> befehlsKlassen;
        String befehlsPackage = "de.x8bit.Fantasya.Host.EVA";
        try {
            befehlsKlassen = PackageLister.getClasses(befehlsPackage);

            for (Class c : befehlsKlassen) {
                // es gibt eine ganze Reihe Klassen, von denen wir keine Befehlsverarbeitung erwarten:
                if (!c.getPackage().getName().equals(befehlsPackage)) continue;
                if ((c.getModifiers() & Modifier.ABSTRACT) != 0) continue;
                if ((c.getModifiers() & Modifier.INTERFACE) != 0) continue;
                if (c.isMemberClass()) continue;

                try {
					@SuppressWarnings("unchecked")
                    Method method = c.getMethod("getMuster", new Class[]{});
                    
					Object o = method.invoke(null, new Object[]{});

					@SuppressWarnings("unchecked")
                    List<BefehlsMuster> bms = (List<BefehlsMuster>)o;

                    // nun endlich: des Pudels Kern.
                    for (BefehlsMuster m : bms) {
                        muster.add(m);
                    }
                } catch (NoSuchMethodException ex) {
                    if (c.getPackage().getName().equals(befehlsPackage)) {
                        // wenn nicht als "NotACommand" deklariert, eine Warnung geben.
                        Class[] interfaces = c.getInterfaces();
                        boolean found = false;
                        for (Class i : interfaces) {
                            if (i.getSimpleName().equals("NotACommand")) found = true;
                            break;
                        }
                        if (!found) new SysMsg("BefehlsSpeicher.prepareMuster(): " + c.getSimpleName() + " enthält keine Befehls-Syntax. (?)");
                    }
                } catch (IllegalAccessException ex) {
                    new BigError(c.getSimpleName() + ": " +ex);
				} catch (InvocationTargetException ex) {
					new SysMsg("BefehlsSpeicher.prepareMuster(): InvocationTargetException - " + c.getSimpleName());
				}

            }

            // new SysMsg(importCnt + " Befehls-Klassen gefunden.");
        } catch (URISyntaxException ex) {
            new BigError(ex);
        } catch (IOException ex) {
            new BigError(ex);
        } catch (ClassNotFoundException ex) {
            new BigError(ex);
        } catch (IllegalArgumentException ex) {
            new BigError(ex);
        } catch (SecurityException ex) {
            new BigError(ex);
        } 

        // zum schnelleren Suchen in eine Map der Anfangs-Buchstaben/-Zeichen eintragen:
        musterKey = new TreeMap<String, List<BefehlsMuster>>();
        for (BefehlsMuster m : muster) {
            String first = m.getErstesZeichen();
            
            // wichtiges Verhalten, falls zu einem Befehlsmuster kein eindeutiges erstes Zeichen angegeben werden kann!
            if (first == null) first = "";

            if (musterKey.get(first) == null) musterKey.put(first, new ArrayList<BefehlsMuster>());

            musterKey.get(first).add(m);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Bekannte Muster:\n");
        sb.append(this.musterToString());

		sb.append("\n--------------------------------------------------------------------------------\n\n\n\n");
        sb.append("Befehle:\n");
		sb.append(this.befehleToString());

        return sb.toString();
    }

	public void toFile(String filename) {
        try {
            File file = new File(filename);

            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "UTF8"));

            out.append(this.toString()).append("\r\n");

            out.flush();
            out.close();

        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
	}

    /**
     * @return immutable Liste aller bekannten BefehlsMuster
     */
    public List<BefehlsMuster> getMuster() {
        return Collections.unmodifiableList(muster);
    }

    /**
     * @return menschenlesbare Ausgabe aller bekannten Befehls-Muster, geordnet nach ihrem Anfangsbuchstaben
     */
    public String musterToString() {
        StringBuilder sb = new StringBuilder();

        if (musterKey != null) {
            for (String first : musterKey.keySet()) {
                sb.append("----------\n\n[" + first.toUpperCase() + "]\n");
                for (BefehlsMuster m : musterKey.get(first)) {
                    sb.append("\t" + m + "\n");
                }
            }
        } else {
            sb.append("(Keine Muster!)\n");
        }

        return sb.toString();
    }

	public String befehleToString() {
        StringBuilder sb = new StringBuilder();
		
		for (Class<? extends EVABase> prozessor : befehleProPhase.getPhasen()) {
            String prozessorName = "<NULL>".intern();
            if (prozessor != null) prozessorName = prozessor.getSimpleName();
            sb.append("----------\n\n" + prozessorName + "\n");
			BefehleProRegion bpr = befehleProPhase.get(prozessor);
			for (Coords c : bpr.getCoords()) {
				Region r = Region.Load(c);
				sb.append("\t" + r + " " + c + ":\n");
				RegionsBefehle rb = bpr.get(c);
                if (rb == null) {
                    new SysMsg("RegionsBefehle sind leer in " + r + "?");
                    continue;
                }
				for (Integer parteiNummer : rb.getParteien()) {
					sb.append("\t\t" + Partei.getPartei(parteiNummer) +":\n");
					for (Einzelbefehl eb : rb) {
						if (eb.getUnit().getOwner() == parteiNummer) sb.append("\t\t\t" + eb + "\n");
					}
				}
			}
			sb.append("\n");
		}

        return sb.toString();
	}

	private class RegionsBefehle extends ArrayList<Einzelbefehl> {
		private static final long serialVersionUID = 3113003637305752254L;
		
		private final Set<Integer> parteien = new HashSet<Integer>();
		
		@Override
		public boolean add(Einzelbefehl eb) {
			if (super.add(eb)) {
				parteien.add(eb.getUnit().getOwner());
				return true;
			} else {
				return false;
			}
		}

		public boolean removeDeep(Einzelbefehl eb) {
			if (!super.remove(eb)) return false;

			parteien.clear();
			for (Einzelbefehl listenEintrag : this) {
				parteien.add(listenEintrag.getUnit().getOwner());
			}

			return true;
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("Das ist mit RegionsBefehle nicht zu machen! Bitte removeDeep() nutzen.");
		}

		@Override
		public void clear() {
			this.parteien.clear();
			super.clear();
		}

		public Set<Integer> getParteien() {
			return parteien;
		}
		
	}

	private class BefehleProRegion extends TreeMap<Coords, RegionsBefehle> {
		private static final long serialVersionUID = -8382486000344247639L;

		private final Set<Coords> coords = new TreeSet<Coords>();

		public void add(Einzelbefehl eb) {
			Coords c = eb.getUnit().getCoords();
			if (this.get(c) == null) this.put(c, new RegionsBefehle());
			this.get(c).add(eb);
			coords.add(c);
		}

		public boolean removeDeep(Einzelbefehl eb) {
			// TODO das funktioniert mit Bewegungsbefehlen nicht, weil sich die Region ändert.
			// TODO testen!
			RegionsBefehle rb = this.get(eb.getCoords());
			if (rb != null) return rb.removeDeep(eb);
			
			// rb ist null: in dieser Region gibt es keine Befehle...
			return false;
		}

		@SuppressWarnings("unused")
		public void clear(Coords c) {
			if (this.get(c) == null) this.put(c, new RegionsBefehle());
			this.get(c).clear();
			coords.remove(c);
		}

		@Override
		public void clear() {
			for (Coords c : this.keySet()) {
				this.get(c).clear();
			}
			coords.clear();
			super.clear();
		}

		public Set<Coords> getCoords() {
			return coords;
		}
	}

	private class BefehleProPhase extends HashMap<Class<? extends EVABase>, BefehleProRegion> {
		private static final long serialVersionUID = 4058843335946361555L;

		private final Set<Class<? extends EVABase>> phasen = new HashSet<Class<? extends EVABase>>();

		public void add(Einzelbefehl eb) {
			Class<? extends EVABase> prozessor = eb.getProzessor();
			if (this.get(prozessor) == null) this.put(prozessor, new BefehleProRegion());
			this.get(prozessor).add(eb);
			phasen.add(prozessor);
		}

		@SuppressWarnings("unused")
		public void clear(Class<? extends EVABase> prozessor) {
			if (this.get(prozessor) == null) this.put(prozessor, new BefehleProRegion());
			this.get(prozessor).clear();
			phasen.remove(prozessor);
		}

		@Override
		public void clear() {
			for (Class<? extends EVABase> prozessor : this.keySet()) {
				this.get(prozessor).clear();
			}
			phasen.clear();
			super.clear();
		}

		public Set<Class<? extends EVABase>> getPhasen() {
			return phasen;
		}

		public List<Einzelbefehl> get(Class<? extends EVABase> prozessor, Coords c) {
			BefehleProRegion bpr = this.get(prozessor);
			RegionsBefehle rb = bpr.get(c);
			return rb;
		}

		/**
		 * Gibt alle passenden Befehle zurück, die zu jedem der Parameter passen.
		 * Ist nur für Testzwecke gedacht und nicht bestmöglich performant!
		 */
		@SuppressWarnings("unused")
		public List<Einzelbefehl> get(Class<? extends EVABase> prozessor, Coords c, Unit u) {
			List<Einzelbefehl> retval = new ArrayList<Einzelbefehl>();

			for (Einzelbefehl eb : this.get(prozessor, c)) {
				if (eb.getUnit().getNummer() == u.getNummer()) retval.add(eb);
			}

			return retval;
		}
	}

}
