package de.x8bit.Fantasya.util;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Host.CommandLineArg;
import de.x8bit.Fantasya.Host.Datenbank;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * @author hapebe
 */
public class MySQLTool implements CommandLineArg {
    
    @Override
	public List<String> getCommandLineOptions() {
		List<String> retval = new ArrayList<String>();

		retval.add("MySQLTool.export");
		retval.add("MySQLTool.import");

		return retval;
	}

    @Override
	public void executeCommandLineOption(String commandLineOption, String arg) {
		if (commandLineOption.equals("MySQLTool.export")) {
			this.exportDB();
			return;
		}
		if (commandLineOption.equals("MySQLTool.import")) {
			this.importDB();
			return;
		}

		throw new UnsupportedOperationException(commandLineOption + " - diese Aktion wird nicht unterstützt.");
	}

	public void exportDB() {
        Datenbank db = new Datenbank("MySQLTool.exportDB");

        String mysqldump = db.ReadSettings("mysqldump-executable", "C:/mysql51/bin/mysqldump.exe");
        File executable = new File(mysqldump);
        if (!executable.exists()) {
            System.out.println("\n\n\nPROBLEM:\n");
            System.out.println("mysqldump wurde unter '" + mysqldump + "' nicht " +
                    "gefunden - bitte passe ggf. in der Tabelle 'settings' den " +
                    "Wert von 'mysqldump-executable' an.");
            System.exit(-1);
        }

        String host = "localhost";
        int port = 3306;
        String[] tokens = Datenbank.GetServer().split(":");
        if (tokens.length == 1) {
            host = tokens[0];
        } else if (tokens.length == 2) {
            host = tokens[0];
            port = Integer.parseInt(tokens[1]);
        }

        String command =
                    mysqldump + " "
                    + Datenbank.GetDatenbank() + " "
                    + "-h " + host + " "
                    + "-P " + port + " "
                    + "-u" + Datenbank.GetBenutzer() + " "
                    + "-p" + Datenbank.GetPasswort() + " "
                    + "--delayed-insert "
                    + "--extended-insert "
                    + "--complete-insert";
        System.out.println("Führe natives Programm aus:\n");
        System.out.println(command + "\n");
        // System.exit(0);

        try {
            String line;
            Process p = Runtime.getRuntime().exec(command);

            File outFile = new File("temp/" + Datenbank.GetDatenbank() + ".sql");
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF8"));

            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            int cnt = 0;
            while ((line = input.readLine()) != null) {
                // System.out.println(line);
                out.append(line + "\n");
                cnt ++;
            }

            input.close();

            out.flush();
            out.close();

            System.out.println(cnt + " Zeilen SQL in " + outFile.getAbsolutePath() + " geschrieben.");
        } catch (UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Exception err) {
            err.printStackTrace();
        }

	}

    @SuppressWarnings("resource")
	private void importDB() {
        Datenbank db = new Datenbank("MySQLTool.importDB");

        String mysql = db.ReadSettings("mysql-executable", "C:/mysql51/bin/mysql.exe");
        File executable = new File(mysql);
        if (!executable.exists()) {
            System.out.println("\n\n\nPROBLEM:\n");
            System.out.println("mysql wurde unter '" + mysql + "' nicht " +
                    "gefunden - bitte passe ggf. in der Tabelle 'settings' den " +
                    "Wert von 'mysql-executable' an.");
            System.exit(-1);
        }

        // Datei zum Importieren suchen:
        List<File> candidates = new ArrayList<File>();
        File inFile = null;
        while (true) {
            File tempDir = new File("temp");
            if (!tempDir.exists()) break;
            if (!tempDir.isDirectory()) break;

            for (File maybe : tempDir.listFiles()) {
                if (maybe.getName().toLowerCase().endsWith(".sql")) candidates.add(maybe);
            }
            if (candidates.size() != 1) break;

            inFile = candidates.get(0);
            break;
        }
        if (inFile == null) {
            System.out.println("\n\n\nPROBLEM:\n");
            if (candidates.isEmpty()) {
                System.out.println("Es wurde keine SQL-Datei zum Import gefunden. Bitte lege eine solche Datei in der Ordner 'temp' im Fantasya-Verzeichnis ab.\n");
            } else {
                System.out.println("Es wurden mehrere mögliche SQL-Dateien zum Import gefunden: " +
                        StringUtils.aufzaehlung(candidates) +
                        ". Bitte lege nur genau eine SQL-Datei in 'temp' ab.\n");
            }
            System.exit(-1);
        }


        String host = "localhost";
        int port = 3306;
        String[] tokens = Datenbank.GetServer().split(":");
        if (tokens.length == 1) {
            host = tokens[0];
        } else if (tokens.length == 2) {
            host = tokens[0];
            port = Integer.parseInt(tokens[1]);
        }

        String command =
                    mysql + " "
                    + "-h" + host + " "
                    + "-P" + port + " "
                    + "-u" + Datenbank.GetBenutzer() + " "
                    + "-p" + Datenbank.GetPasswort() + " "
                    + Datenbank.GetDatenbank();
        System.out.println("Führe natives Programm aus:\n");
        System.out.println(command + "\n");
        // System.exit(0);

        try {
            String line;
            Process p = Runtime.getRuntime().exec(command);

            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF8"));

            BufferedWriter mysqlPipe = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

            BufferedReader mysqlMessages = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // MySQL den Dateiinhalt einflößen:
            int cnt = 0;
            while ((line = input.readLine()) != null) {
                mysqlPipe.append(line + "\n");
                cnt ++;
            }
            mysqlPipe.flush();
            mysqlPipe.close();
            System.out.println(cnt + " Zeilen SQL aus " + inFile.getAbsolutePath() + " eingelesen.");

            cnt = 0;
            while ((line = mysqlMessages.readLine()) != null) {
                System.out.println(line);
                cnt ++;
            }
            mysqlMessages.close();


        } catch (UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
	

}
