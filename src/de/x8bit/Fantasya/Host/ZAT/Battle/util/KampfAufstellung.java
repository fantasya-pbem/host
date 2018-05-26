package de.x8bit.Fantasya.Host.ZAT.Battle.util;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.util.Codierung;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author hb
 */
public class KampfAufstellung {

    int einheitA = -1;
    int einheitB = -1;

    @SuppressWarnings("resource")
	public KampfAufstellung(String fileName) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Datei " + fileName + " nicht gefunden.", ex);
        }

        int aktuelleEinheit = -1;
        try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String hs = br.readLine();
			while(hs != null) {
				// TAB's durch Leerzeichen ersetzen
				hs = hs.replace("\t", " ");

				// Leerzeichen an den Enden killen
				hs = hs.trim();

				// Länge testen (ob überhaupt noch was da ist ^^)
				if (hs.length() == 0) {
					hs = br.readLine();
					continue;
				}

				// merfache Leerzeichen im String killen
				StringBuilder sb = new StringBuilder();
				sb.append(hs.substring(0, 1));
				for(int i = 1; i < hs.length(); i++) {
					if (hs.substring(i, i + 1).equals(" "))	{
						if (!hs.substring(i - 1, i).equals(" ")) sb.append(hs.substring(i, i + 1));
					} else {
						sb.append(hs.substring(i, i + 1));
					}
				}
				hs = sb.toString();

				// Deutsche-Umlaute ersetzen
                /*
				hs = hs.replace("=C4", "ä");
				hs = hs.replace("=C3", "ü");
				hs = hs.replace("ä", "ae");
				hs = hs.replace("ö", "oe");
				hs = hs.replace("ü", "ue");
				hs = hs.replace("Ä", "Ae");
				hs = hs.replace("Ö", "Oe");
				hs = hs.replace("Ü", "Ue");
				hs = hs.replace("ß", "ss"); */

                // Kommentar?
                if (hs.startsWith(";")) {
					hs = br.readLine(); continue;
                }

                // Einheiten-Bezug?
                if (hs.startsWith("[")) {
                    hs = hs.replaceAll("\\[", "");
                    hs = hs.replaceAll("\\]", "");
                    hs = hs.trim();

                    aktuelleEinheit = Codierung.fromBase36(hs);

                    if (einheitA == -1) {
                        einheitA = aktuelleEinheit;
                        // System.out.println("Einheit A: " + einheitA);
                    } else if (einheitB == -1) {
                        einheitB = aktuelleEinheit;
                        // System.out.println("Einheit B: " + einheitB);
                    }

					hs = br.readLine(); continue;
                }

                // tokenize:
                List<String> tokens = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(hs, " ");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    tokens.add(token);
                }

                // Rasse?

                if (tokens.size() == 1) {
                    if (aktuelleEinheit != -1) {
                        if (tokens.get(0).startsWith("!")) {
                            // property:
                            String[] pair = tokens.get(0).substring(1).trim().split("=");
                            if (pair.length != 2) {
                                throw new IllegalArgumentException("Ein 'Property' muss so aussehen: !name=wert . Leerzeichen mittendrin sind nicht erlaubt. (" + tokens.get(0) +")");
                            }
                            Unit.Load(aktuelleEinheit).setProperty(pair[0], pair[1]);
                            // System.out.println("Setze Property für [" + Codierung.toBase36(aktuelleEinheit) + "]: " + pair[0]  + "=" + pair[1]);

                        } else {
                            // Rasse:
                            Unit u = null;
                            try {
                                u = (Unit) Class.forName("de.x8bit.Fantasya.Atlantis.Units." + tokens.get(0)).newInstance();
                            } catch (InstantiationException ex) {
                                new BigError(new RuntimeException("Kann keine " + hs + "-Einheit erstellen.", ex));
                            } catch (IllegalAccessException ex) {
                                new BigError(new RuntimeException("Kann keine " + hs + "-Einheit erstellen.", ex));
                            } catch (ClassNotFoundException ex) {
                                new BigError(new RuntimeException("Kann keine " + hs + "-Einheit erstellen.", ex));
                            }

                            u.setNummer(aktuelleEinheit);
                            u.setName("Krieger " + Codierung.toBase36(aktuelleEinheit));
                            u.setCoords(new Coords(0, 0, 1));
                            u.setPersonen(1);
                            u.setKampfposition(Kampfposition.Vorne);

                            Unit.CACHE.add(u);

                            // System.out.println(u + " ist ein " + u.getRassenName() + ".");
                        }
                    }
                }

                if ((aktuelleEinheit == -1) || (Unit.Get(aktuelleEinheit) == null)) {
                    System.err.println("Vor allen anderen Angaben muss ein Krieger (als Base-36-Einheiten-Nummer) und dessen Rasse bestimmt werden, bspw.:");
                    System.err.println();
                    System.err.println("[a]");
                    System.err.println("Mensch");
                    System.err.println();
                    System.err.println("(Mindestens ein anderer Krieger muss später noch zusätzlich definiert werden, z.B. mit [b] eingeleitet.)");
                    System.exit(-1);
                }

                Unit u = Unit.Get(aktuelleEinheit);


                if (tokens.size() == 2) {
                    // ist das erste eine Zahl?
                    int n0 = Integer.MIN_VALUE;
                    // und das zweite?
                    int n1 = Integer.MIN_VALUE;

                    try { n0 = Integer.parseInt(tokens.get(0)); } catch (NumberFormatException ex) { }
                    try { n1 = Integer.parseInt(tokens.get(1)); } catch (NumberFormatException ex) { }

                    if (n0 != Integer.MIN_VALUE) {
                        if ("person".equalsIgnoreCase(tokens.get(1)) || "personen".equalsIgnoreCase(tokens.get(1))) {
                            u.setPersonen(n0);
                            // if (n0 != 1) message(u, "Hat " + n0 + " Personen.");
                        } else {
                            // Gegenstand:
                            Item it = null;
                            try {
                                it = (Item) Class.forName("de.x8bit.Fantasya.Atlantis.Items." + tokens.get(1)).newInstance();
                            } catch (InstantiationException ex) {
                                new BigError(new RuntimeException(hs + " ist kein Gegenstand.", ex));
                            } catch (IllegalAccessException ex) {
                                new BigError(new RuntimeException(hs + " ist kein Gegenstand.", ex));
                            } catch (ClassNotFoundException ex) {
                                new BigError(new RuntimeException(hs + " ist kein Gegenstand.", ex));
                            }

                            if (n0 <= 0) { System.err.println(hs + " - es muss mindestens ein Exemplar bestellt werden."); System.exit(-1); }

                            it.setAnzahl(n0);
                            u.setItem(it.getClass(), n0);
                            // message(u, "Hat " + it + ".");
                        }
                    } else if (n1 != Integer.MIN_VALUE) {
                        // Talent:
                        // Gegenstand:
                        Skill sk = null;
                        try {
                            sk = (Skill) Class.forName("de.x8bit.Fantasya.Atlantis.Skills." + tokens.get(0)).newInstance();
                        } catch (InstantiationException ex) {
                            new BigError(new RuntimeException(tokens.get(0) + " ist kein Talent.", ex));
                        } catch (IllegalAccessException ex) {
                            new BigError(new RuntimeException(tokens.get(0) + " ist kein Talent.", ex));
                        } catch (ClassNotFoundException ex) {
                            new BigError(new RuntimeException(tokens.get(0) + " ist kein Talent.", ex));
                        }

                        if (n1 <= 0) { System.err.println(hs + " - es muss mindestens ein Lerntag vorhanden sein."); System.exit(-1); }

                        u.setSkill(sk.getClass(), n1 * u.getPersonen());
                        // message(u, "Kann " + sk + ", Talentwert " + u.Talentwert(sk) + ".");
                    } else {
                        // dann nehmen wir beides als String:
                        if ("steht".equalsIgnoreCase(tokens.get(0)) || "kämpft".equalsIgnoreCase(tokens.get(0))) {
                            Kampfposition kp = Kampfposition.ordinal(tokens.get(1));
                            if (kp == null) { System.err.println(hs + " - die Kampfposition ist so nicht möglich."); System.exit(-1); }
                            // message(u, "Kampfposition: " + kp + ".");
                            u.setKampfposition(kp);
                        } else if ("partei".equalsIgnoreCase(tokens.get(0))) {
                            String token = tokens.get(1);
                            if (token.startsWith("[") && token.endsWith("]")) {
                                try {
                                    int id = Codierung.fromBase36(token.substring(1, token.length() - 1));
                                    Partei p = getOrCreatePartei(id);
                                    u.setOwner(p.getNummer());
                                    u.setTarnPartei(p.getNummer());
                                    if (p.getRasse() == null) p.setRasse(u.getRasse());
                                } catch (NumberFormatException ex) {
                                    throw new RuntimeException("Partei-ID nicht erkannt: " + token);
                                }
                            } else {
                                System.err.println("Partei muss als Base36-ID in eckigen Klammern angegeben werden - z.B. [abc]"); System.exit(-1);
                            }
                        } else if (
                                "heißt".equalsIgnoreCase(tokens.get(0))
                                || "heisst".equalsIgnoreCase(tokens.get(0))
                                || "Name".equalsIgnoreCase(tokens.get(0))
                        ) {
                            String token = tokens.get(1);
                            u.setName(token);
                        } else {
                            throw new RuntimeException("\"" + hs + "\" habe ich nicht verstanden.");
                        }
                    }

                }


				// nächste Zeile
				hs = br.readLine();
			}
			br.close();
		} catch(IOException ex) { new BigError(ex); }

        // beschreibeEinheit(Unit.Load(einheitA));
        // beschreibeEinheit(Unit.Load(einheitB));
    }


    private Partei getOrCreatePartei(int id) {
        Partei p = Partei.getPartei(id);
        if (p == null) {
            p = new Partei();
            p.setNummer(id);
            p.setName("Partei " + Codierung.toBase36(id));
            p.setEMail("noone@foo.bar");
            Partei.PROXY.add(p);
        }
        return p;
    }

    public int getEinheitA() {
        return einheitA;
    }

    public int getEinheitB() {
        return einheitB;
    }

    


}
