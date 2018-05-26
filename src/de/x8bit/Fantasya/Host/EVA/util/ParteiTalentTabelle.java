package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.Paket;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Erstellt eine CSV-Tabelle aller vorhandenen Parteien X aller vorhandenen Talente,
 * einmal die Gesamtlerntage, einmal die Lerntage pro Kopf.
 * @author hb
 */
public class ParteiTalentTabelle {

    final String delimiter = ";";

    final NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);

    @SuppressWarnings("unchecked")
	public ParteiTalentTabelle() {
        Map<Integer, Class<? extends Skill>> columns = new HashMap<Integer, Class<? extends Skill>>();
        Map<Integer, Partei> rows = new HashMap<Integer, Partei>();

        {
            int x = 0;
            for (Paket p : Paket.getPaket("Skills")) {
                columns.put(x, (Class<? extends Skill>)p.Klasse.getClass());
                x ++;
            }

            int y = 0;
            for (Partei p : Partei.PROXY) {
                rows.put(y, p);
                y ++;
            }
        }

        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(0);

        try {
            File file1 = new File("temp/partei-talente.csv");
            File file2 = new File("temp/partei-talente-pro-kopf.csv");

            Writer out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file1), "UTF8"));
            Writer out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2), "UTF8"));

            // erste Zeile: Spaltenüberschriften
            out1.write("\"\"" + delimiter);
            out2.write("\"\"" + delimiter);
            for (int x : columns.keySet()) {
                Class<? extends Skill> sk = columns.get(x);
                out1.write("\"" + sk.getSimpleName() + "\""+ delimiter);
                out2.write("\"" + sk.getSimpleName() + "\""+ delimiter);
            }
            out1.write("\r\n");
            out2.write("\r\n");

            // alle Parteien, zeilenweise:
            for (int y : rows.keySet()) {
                Partei p = rows.get(y);
                out1.write("\"" + p + "\""); // erstmal kein Delimiter!
                out2.write("\"" + p + "\""); // erstmal kein Delimiter!
                for (int x : columns.keySet()) {
                    Class<? extends Skill> sk = columns.get(x);

                    int total = 0;
                    int personen = 0;
                    for (Unit u : Unit.CACHE.getAll(p.getNummer())) {
                        total += u.getSkill(sk).getLerntage();
                        personen += u.getPersonen();
                    }

                    out1.write(delimiter + total);
                    if (personen > 0) {
                        out2.write(delimiter + nf.format((double)total / (double)personen));
                    } else {
                        out2.write(delimiter + "");
                    }

                }
                out1.write("\r\n");
                out2.write("\r\n");
            }

            out1.flush();
            out1.close();

            out2.flush();
            out2.close();

        } catch (UnsupportedEncodingException e) {
            new SysErr(e.getMessage());
        } catch (IOException e) {
            new SysErr(e.getMessage());
        }
        
    }

}
