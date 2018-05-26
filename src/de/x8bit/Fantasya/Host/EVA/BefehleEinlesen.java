package de.x8bit.Fantasya.Host.EVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsCheck;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.FalschesPasswortException;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.io.EncodingDetector;

import java.util.HashSet;
import java.util.Set;

/**
 * hier werden alle Befehle eingelesen und in die DB geschoben
 * @author  mogel
 */
public class BefehleEinlesen extends EVABase
{
	private static String inboxDirectory = "befehle-inbox";
	private static String befehleDirectory = "befehle";


	public BefehleEinlesen()
	{
		super("lese Befehle der Spieler ein");

		LoadBefehle();

				
		for (BefehlsMuster pattern : getMuster()) {
			addTemplate(pattern.getRegex());
		}
		
		
		// old code. new code temporary for game. new code testing. After test and ordercheck writing
		// delete these code.
        /* if (ZATMode.CurrentMode().isImapAbrufen()) {
            try {
                IMAPConnector conn = new IMAPConnector();
                int count = conn.befehleHolen();
                if (count > 0) {
                    new ZATMsg(count + " Befehls-Mails vom IMAP-Server geholt.");
                } else {
                    new ZATMsg("Keine Befehls-Mails vom IMAP-Server geholt.");
                }
            } catch (MessagingException ex) {
                new BigError(ex);
            } catch (IOException ex) {
                new BigError(ex);
            }
        }

        // Der Check wird so oder so durchgeführt - er bestimmt, welche Befehlsdateien verwendet werden.
		BefehlsCheck check = BefehlsCheck.getInstance();
		for (File f : this.getBefehlsInbox()) {
			check.addFile(f);
		}

		// check enthält jetzt nur die jeweils besten Dateien - d.h. die neuesten bzw. diejenige mit gültigem Passwort:
		for (Partei p : check.getParteien()) {
			if (check.hasValidBefehle(p)) {
				// okay, Datei kopieren:
				kopieren(check.getCleanBefehle(p).getAbsolutePath(), 
						befehleDirectory + "/" + p.getNummerBase36() + ".mail");
			} else {
				// Partei hat Befehle eingeschickt, aber keine gültigen:
				new Fehler("Falsches Passwort (oder falsche Parteinummer)!", p);
			}
		}

        InboxLeeren();
		
		// Wenn Befehls-Check-Modus, dann Partei ohne (neue) Befehle ignorieren:
		if (ZATMode.CurrentMode().isBefehlsCheck()) {
			if (check.getParteien().isEmpty()) {
				System.out.println("Es gibt keine neuen Befehlsdateien zum überprüfen.");
				System.exit(0);
			}

			List<Partei> ignoreList = new ArrayList<Partei>();
			ignoreList.addAll(Partei.PROXY);
			for (Partei p : check.getParteien()) {
				if (check.hasValidBefehle(p)) {
					ignoreList.remove(p);
				}
			}
			for (Partei p : ignoreList) {
				ZATMode.CurrentMode().setIgnore(p);
			}
			if (ZATMode.CurrentMode().hatIgnorierteParteien()) {
				for (int pNr : ZATMode.CurrentMode().getIgnorierteParteiNummern()) {
					new Debug("Ignoriere Partei " + Partei.getPartei(pNr));
				}
			}
		}

		
		LoadBefehle();

		
		for (BefehlsMuster pattern : getMuster()) {
			addTemplate(pattern.getRegex());
		} */
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		// Einstieg
        retval.add(new BefehlsMuster(BefehleEinlesen.class, 1, "^(fantasya) [a-z0-9]{1,4} (\")[a-zA-Z0-9]+(\")$", "f", Art.KURZ));
        retval.add(new BefehlsMuster(BefehleEinlesen.class, 1, "^(eressea) [a-z0-9]{1,4} (\")[a-zA-Z0-9]+(\")$", "e", Art.KURZ));
        retval.add(new BefehlsMuster(BefehleEinlesen.class, 1, "^(partei) [a-z0-9]{1,4} (\")[a-zA-Z0-9]+(\")$", "p", Art.KURZ));

		// diverses von Magellan
        retval.add(new BefehlsMuster(BefehleEinlesen.class, 2, "^(locale) [a-zA-Z0-9]+$", "l", Art.KURZ)); 	// welche Sprache auch immer verwendet wird
        retval.add(new BefehlsMuster(BefehleEinlesen.class, 3, "^(region) .+$", "r", Art.KURZ));

		// Rundenmarkierung - FCheck meldet dann entsprechend eine Warnung ... dadurch
		// können Spieler nicht unbedingt den entsprechenden falschen Rundenbefehl einschicken
		retval.add(new BefehlsMuster(BefehleEinlesen.class, 6, "^(runde) [0-9]{1,4}$", "r", Art.KURZ));

		// eine Einheit beginnt
        retval.add(new BefehlsMuster(BefehleEinlesen.class, 4, "^(einheit) [a-z0-9]{1,4}$", "e", Art.KURZ));

		// ... der nächste Bitte
        retval.add(new BefehlsMuster(BefehleEinlesen.class, 5, "^(naechster)$", "n", Art.KURZ));

		return retval;
    }

	
	/**
	 * Funktion wird nicht verwendet
	 */
	@Override
	public boolean DoAction(Unit u, String befehl[]) { return true;	}
	

	/**
	 * @return Alle Dateien, die sich im Ordner inboxDirectory befinden und die Endung .mail haben
	 */
	public List<File> getBefehlsInbox() {
        List<File> retval = new ArrayList<File>();

		File src = new File(inboxDirectory);
        if (!src.exists()) {
            new SysMsg("Warnung: Der Ordner " + inboxDirectory + " existiert nicht.");
            return retval;
        }

		File file[] = new File(inboxDirectory).listFiles();
		for(int i = 0; i < file.length; i++) {
//			if (file[i].toString().toLowerCase().endsWith(".mail")) {
				retval.add(file[i]);
//			}
		}
		return retval;
	}

	private void kopieren(String srcFile, String dstFile) {
		try {
			File dst = new File(dstFile);
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(new File(srcFile)), "UTF8"));
			Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dst), "UTF8"));

			String line;
			while ((line = r.readLine()) != null) {
				w.append(line + "\n");
			}

			w.flush();
			w.close();

			r.close();
		} catch (FileNotFoundException ex) {
			new BigError(ex);
		} catch (UnsupportedEncodingException ex) {
			new BigError(ex);
		} catch (IOException ex) {
			new BigError(ex);
		}
	}

	/**
     * <p>Löscht nach dem Einlesen der Befehle alle Quelldateien in inboxDirectory
     * NICHT in befehleDirectory, die Dateien dort bleiben also für eine mögliche Wiederhoung der AW
     * erhalten.</p>
     */
    private void InboxLeeren() {
        File src = new File(inboxDirectory);
        if (!src.exists()) {
            new SysMsg("Warnung: Der Ordner " + inboxDirectory + " existiert nicht.");
            return;
        }

        File file[] = new File(inboxDirectory).listFiles();
        for(int i = 0; i < file.length; i++) {
            boolean success = file[i].delete();
			if (!success) {
				new SysErr("Konnte Befehlsdatei " + file[i].getAbsolutePath() + " nicht löschen!");
			}
        }
	}


    /** liest alle Befehle ein */
	private void LoadBefehle() {
		FilenameFilter fileNameFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.trim().toLowerCase().endsWith(".order"));
			}
		};
		File fileArray[] = new File(befehleDirectory + "/" + GameRules.getRunde()).listFiles(fileNameFilter);
		if (fileArray == null) {
			new SysErr("keine Befehle für aktuelle Runde " + GameRules.getRunde() + " gefunden. (in " + befehleDirectory + "/" + GameRules.getRunde() + ")");
			fileArray = new File(befehleDirectory).listFiles(fileNameFilter);
			if (fileArray == null) {
				new SysErr("Auch in "+ befehleDirectory + " keine Dateien gefunden!");
				return;
			}
		}
		new SysMsg(4, " - " + fileArray.length + " Dateien gefunden");
		for(int i = 0; i < fileArray.length; i++) {
			readBefehle(fileArray[i].toString());
		}
	}
	
	/**
	 * &quot;Säubert&quot; eine Befehlsdatei von sämtlichen irrelevanten 
	 * Einträgen - ;-Kommentare, Magellan-speizifisches, ...
	 * @param file Dateiname der Quelldatei
	 * @param outFile Dateiname der Zieldatei
	 */
	public static void cleanBefehle(String file, String outFile) {
		try {
            StringBuilder out = new StringBuilder();

			Charset charset = EncodingDetector.guess(new File(file));
			if (charset == null) charset = Charset.forName("UTF-8");

			// TODO raus nehmen, wenn es reibungslos läuft
			// new SysMsg("Verwende " + charset.displayName() + " für " + file + " .");

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
			String hs = br.readLine();
			while(hs != null) {
				hs = hs.trim();
				
				// Zeilen zusammen fügen
				while(hs.endsWith("\\")) {
					hs = hs.substring(0, hs.length() - 1) + br.readLine();
					hs = hs.trim();
				}
				
				// Kommentare ( die mittels ;) löschen
				int idx = hs.indexOf(";");
				if (idx != -1) {
					if (hs.startsWith("//")) {
						// temporäre Kommentare hinter permanenten Kommentaren
						// werden nicht mehr gelöscht - Absprache mit ged
					} else {
						// Ansonsten werden die gelöscht
						hs = hs.substring(0, idx);
					}
				}
				
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
				hs = hs.replace("=C4", "ä");
				hs = hs.replace("=C3", "ü");
				hs = hs.replace("ä", "ae");
				hs = hs.replace("ö", "oe");
				hs = hs.replace("ü", "ue");
				hs = hs.replace("Ä", "AE");
				hs = hs.replace("Ö", "OE");
				hs = hs.replace("Ü", "UE");
				hs = hs.replace("ß", "ss");
				
				// Statements von Magellan löschen
				if (hs.toLowerCase().startsWith("region")) hs = "";

                if (hs.length() > 0) out.append(hs + "\n");
                hs = br.readLine();
            }

            // gesammelten Inhalt in outFile schreiben:
            Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF8"));
            w.append(out);
            w.flush();
            w.close();

			// Input-Datei schließen
			br.close();

        } catch(Exception ex) { new BigError(ex); }	
        
    }

	/**
	 * Findet die Partei, zu der eine Befehlsdatei gehört - es wird dafür auch das Passwort verlangt.
	 * @param cleanFile Befehlsdatei - muss vorher mit cleanFile() vorbehandelt sein!
	 * @return Die authentifizierte Partei oder null.
	 */
	public static Partei getPartei(String cleanFile) throws FalschesPasswortException {
		Partei partei = null;

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(cleanFile), "UTF8"));
			// Die Partei muss in der ersten Zeile definiert sein:
			String hs = br.readLine();
			if(hs != null) {
				// Partei
				if (hs.toLowerCase().startsWith("fantasya") || hs.toLowerCase().startsWith("eressea") || hs.toLowerCase().startsWith("partei")) {
					partei = holePartei(hs, cleanFile);
				}
			}
			br.close();
		} catch(IOException ex) { new BigError(ex); }

		return partei;
	}

    /**
	 * einlesen einer Befehlsdatei und schaufeln in die DB
	 * @param cleanFile - Name der Befehlsdatei (muss mit cleanBefehle vor-verarbeitet sein!)
	 */
	private void readBefehle(String cleanFile) {
		Set<Integer> erkannteEinheiten = new HashSet<Integer>();

		if (ZATMode.CurrentMode().isDebug()) new Debug(" - Befehls-Datei '" + cleanFile + "'");
		
		Partei partei = null;
		Unit unit = null;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(cleanFile), "UTF8"));
			String hs = br.readLine();
			while(hs != null) {
				// Zeilen zusammen fügen
				while(hs.endsWith("\\")) hs = hs.substring(0, hs.length() - 1) + br.readLine();
				
				// Kommentare ( die mittels ;) löschen
				int idx = hs.indexOf(";");
				if (idx != -1) {
					if (hs.startsWith("//")) {
						// temporäre Kommentare hinter permanenten Kommentaren
						// werden nicht mehr gelöscht - Absprache mit ged
					} else {
						// Ansonsten werden die gelöscht
						hs = hs.substring(0, idx);
					}
				}
				
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
				hs = hs.replace("=C4", "ä");
				hs = hs.replace("=C3", "ü");
				hs = hs.replace("ä", "ae");
				hs = hs.replace("ö", "oe");
				hs = hs.replace("ü", "ue");
				hs = hs.replace("Ä", "AE");
				hs = hs.replace("Ö", "OE");
				hs = hs.replace("Ü", "UE");
				hs = hs.replace("ß", "ss");
				
				// Statements von Magellan löschen
				if (hs.toLowerCase().startsWith("region")) hs = "";
				
				// Befehl in DB kopieren ... gut man könnte auch die Befehle hinzufügen und
				// dann die Einheit erneut speichern ... aber der Verwaltungsaufwand ist größer
				// Nicht mehr Notwendig, weil alles während der AW im Arbeitsspeicher verbleibt. 
				if ((partei != null) && (unit != null) && (hs.length() != 0) && (!hs.toLowerCase().startsWith("einheit")) && 
						(!hs.toLowerCase().startsWith("naechster")) && (!hs.toLowerCase().startsWith("faulenze")))
				{
					try {
						// unit.BefehleExperimental.add(unit, Datenbank.CheckValue(hs, true));
						unit.BefehleExperimental.add(unit, hs);
						// TODO: Das kann dann irgendwann raus (?)
						// unit.Befehle.add(Datenbank.CheckValue(hs, true));
						unit.Befehle.add(hs);
					} catch (IllegalArgumentException ex) {
						// ex.printStackTrace();
						new Debug(unit + ": " + ex.getMessage());
						new Fehler(ex.getMessage(), unit);
					}
				}
				
				// Partei
				if (hs.toLowerCase().startsWith("fantasya") || hs.toLowerCase().startsWith("eressea") || hs.toLowerCase().startsWith("partei")) {
					partei = null;
					try {
						partei = holePartei(hs, cleanFile);
					} catch (FalschesPasswortException ex) {
						// Partei hat Befehle eingeschickt, aber keine gültigen:
						new Fehler("Falsches Passwort (oder falsche Parteinummer)!", ex.getPartei());
					}
					if (partei != null) {
						new SysMsg(" - Partei " + partei + " erkannt.");
					}
				}
				
				// Einheit
				if (hs.toLowerCase().startsWith("einheit") && (partei != null)) {
					unit = holeEinheit(partei, hs);

					if (unit == null) {
						new Fehler("Einheit [" + hs + "] nicht gefunden.", partei);
					} else if (erkannteEinheiten.contains(unit.getNummer())) {
						new Fehler("Einheit " + unit + " hat mehrfach Befehle bekommen - die ersten werden verwendet.", partei);
					} else {
						erkannteEinheiten.add(unit.getNummer());
						// alte Befehle löschen
						unit.Befehle = new ArrayList<String>();
						unit.BefehleExperimental.clear();
					}
				}
				
				// Ende
				if (hs.toLowerCase().startsWith("naechster")) {
					unit = null;
					partei = null;
				}
				
				// nächste Zeile
				hs = br.readLine();
			}
			br.close();
		} catch(Exception ex) { new BigError(ex); }	
	}

	/**
	 * extrahiert die gültige Partei aus der Befehlsdatei
	 * <div class="warnung multiline">
	 * Passwort wird nicht mehr geprüft ... die Verwaltung erfolgt neu über die
	 * Webseite ... wenn ein Spieler Befehle einschickt und anschließend das Passwort
	 * ändert, werden die Befehle beim ZAT nicht mehr akzeptiert - da ja das Passwort
	 * inzwischen anders ist
	 * </div>
	 * @param line - Befehlszeile (like "Fantasya Partei Passwort")
	 * @param file - das Befehlsfile
	 */
	private static Partei holePartei(String line, String file) throws FalschesPasswortException {
		Partei retval = null;

		String befehl[] = line.split("\\ ");
		if (befehl.length != 3)	{
			new SysMsg("ungültige Befehlsdatei - '" + file + "'");
			return null;
		}
		
		// Partei holen
		try	{
			int nummer = Codierung.fromBase36(befehl[1]);
			retval = Partei.getPartei(nummer);
		} catch(Exception ex) { new SysMsg("fehlerhafte Befehlsdatei '" + file + "', '" + line + "' - " + ex.toString()); return null; }
		
		if (retval == null) { new SysMsg("Partei " + befehl[1] + " nicht gefunden"); return null; }
		
		// Passwort wird nicht mehr geprüft ... die Verwaltung erfolgt neu über die
		// Webseite ... wenn ein Spieler Befehle einschickt und anschließend das Passwort
		// ändert, werden die Befehle beim ZAT icht akzeptiert - da ja das Passort anders ist
		
//		if ((retval.getPassword()).equalsIgnoreCase(befehl[2].replace("\"", ""))) {
			// Spieler hat sich gemeldet
			retval.setNMR(GameRules.getRunde());

			return retval;
//		} else {
//			throw new FalschesPasswortException(retval);
//		}
	}
	
	/**
	 * extrahiert die gültige Einheit
	 * @param line - Befehlszeile (like "EINHEIT bla")
	 */
	private Unit holeEinheit(Partei partei, String line) {
		Unit retval;

		String befehl[] = line.split("\\ ");
		if (befehl.length < 2) return null; // still und leise
		
		try	{
			int nummer = Codierung.fromBase36(befehl[1]);
			retval = Unit.Load(nummer);
			if (retval == null) {
				new Fehler("Die Einheit [" + befehl[1] + "] gehört Dir nicht.", partei);
				return null;
			}
		} catch(Exception ex) { new SysErr("Einheit mit Nummer [" + befehl[1] + "] nicht gefunden"); return null; }
		
		if (retval.getOwner() != partei.getNummer()) {
			new Fehler("Die Einheit [" + befehl[1] + "] gehört Dir nicht.", partei);
			return null;
		}
		
		// new SysMsg(4, " - neue Einheit gefunden '" + unit + "'");		
		return retval;
	}

	@Override
	public void PostAction() { }
	@Override
	public void PreAction() { }
	@Override
	public void DoAction(Region r, String befehl) { }
	@Override
    public void DoAction(Einzelbefehl eb) { }
}
