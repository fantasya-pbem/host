package de.x8bit.Fantasya.Host.Reports;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.GameRules;

/**
 * Ob wirklich e-mails versendet werden, wird über
 * ZATMode.CurrentMode().isSendReportMails() bestimmt ... 
 * wird mit der Option -reporte automatisch unterdrückt ... wenn doch versendet
 * werden soll (weil änderung an was auch immer) dann muss die Option -email
 * mit angegeben werden.
 */
public class Zipping
{
	/** 
	 * das Salz in der Suppe ... <b>public</b>, damit bei dem einzelnen schreiben
	 * von Reporten das Salt korrekt gesetz werden kann
	 */
	public static String salz = "empty";
	
	/**
	 * 
	 * @param owner - ZIP für diese Partei
	 */
	public Zipping(Partei p)
	{
		CheckSalt();
		
		String files [] = new String [] {
											GameRules.getRunde() + "-" + p.getNummerBase36() + ".cr",
											GameRules.getRunde() + "-" + p.getNummerBase36() + ".zr",
											GameRules.getRunde() + "-" + p.getNummerBase36() + ".nr",
											GameRules.getRunde() + "-" + p.getNummerBase36() + ".nr2",
											GameRules.getRunde() + "-" + p.getNummerBase36() + ".xml"
										};
		
		try 
		{
			File f = new File("zip/" + GameRules.getRunde());
			if (!f.exists()) f.mkdirs();
			
			byte[] buf = new byte[65536];
			ZipOutputStream out = new ZipOutputStream( new BufferedOutputStream(new FileOutputStream("zip/" + GameRules.getRunde() + "/" + p.getNummerBase36() + ".zip")) );
			for (int i = 0; i < files.length; ++i) 
			{
				String fname = files[i];
				InputStream in = new BufferedInputStream(new FileInputStream("reporte/" + fname));
				out.putNextEntry(new ZipEntry(fname));
				int len;
				while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
				in.close();
			}
			if (p.getMonster() > 1)	{
				InputStream in = null;
				if (p.getMonster() == 2) in = new BufferedInputStream(new FileInputStream("world.xml")); // TODO geo.xml
				if (p.getMonster() == 3) in = new BufferedInputStream(new FileInputStream("world.xml"));
				out.putNextEntry(new ZipEntry("monster.xml"));
				int len;
				while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
				in.close();
			}
		   out.close();
		   
		   // löschen im ZIP-Verzeichnis
		   f = new File("zip/" + (GameRules.getRunde() - 10));
		   if (f.exists())
		   {
			   for(File d : f.listFiles()) d.delete();
			   f.delete();
		   }
		} catch (IOException e) 
		{
			new SysErr(e.toString());
		}
	}

	private void CheckSalt()
	{
		boolean enabled = Datenbank.isEnabled();
		if (!enabled) Datenbank.Enable();
		if (salz.equals("empty"))
		{
			salz = Long.toString(System.currentTimeMillis());
			Datenbank db = new Datenbank("salz speichern");
			db.SaveSettings("game.salt", salz);
			db.Close();
		}
		if (!enabled) Datenbank.Disable();
	}
}
