package de.x8bit.Fantasya.Host;

import de.x8bit.Fantasya.Atlantis.Allianz;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;

public class TestCase
{
	private int fail = 0;

	public TestCase()
	{
		new SysMsg("TestCase Neustart");
		
		TestAllianzen();
		
		new SysMsg("TestCase Quit");
	}
		
	private void TestAllianzen()
	{
		new SysMsg("- teste Allianzen");
		
		Partei p = Partei.getPartei(1);
		if (p == null)
		{
			new SysMsg(" .. konnte Partei #1 nicht laden");
			return;
		}
		
		new SysMsg(" .. hole Allianz-Daten für Partei 1 und aktiviere Handel");
		Allianz a = p.getAllianz(2);
		a.setOption(AllianzOption.Handel, true);
		
		new SysMsg(" .. hole Partei erneut");
		p = Partei.getPartei(1);
		new SysMsg(" => Allianz für Partner 2 & Handel - " + p.hatAllianz(2, AllianzOption.Handel) + " [true]");
		new SysMsg(" => Allianz für Partner 2 & Treiben - " + p.hatAllianz(2, AllianzOption.Steuern) + " [false]");
		if (p.hatAllianz(2, AllianzOption.Handel) == false) fail++;
		if (p.hatAllianz(2, AllianzOption.Steuern) != false) fail++;
		
		new SysMsg(" .. setzte Option Alles");
		p.getAllianz(2).setOption(AllianzOption.Alles, true);
		new SysMsg(" => Allianz für Partner 2 & Handel - " + p.hatAllianz(2, AllianzOption.Handel) + " [true]");
		new SysMsg(" => Allianz für Partner 2 & Treiben - " + p.hatAllianz(2, AllianzOption.Steuern) + " [true]");
		if (p.hatAllianz(2, AllianzOption.Handel) == false) fail++;
		if (p.hatAllianz(2, AllianzOption.Steuern) == false) fail++;
		
		new SysMsg(" .. lösche Option Handel");
		p.getAllianz(2).setOption(AllianzOption.Handel, false);
		new SysMsg(" => Allianz für Partner 2 & Handel - " + p.hatAllianz(2, AllianzOption.Handel) + " [false]");
		new SysMsg(" => Allianz für Partner 2 & Treiben - " + p.hatAllianz(2, AllianzOption.Steuern) + " [true]");
		if (p.hatAllianz(2, AllianzOption.Handel) != false) fail++;
		if (p.hatAllianz(2, AllianzOption.Steuern) == false) fail++;
		
		new SysMsg(" .. lösche Option Alles");
		p.getAllianz(2).setOption(AllianzOption.Alles, false);
		new SysMsg(" => Allianz für Partner 2 & Handel - " + p.hatAllianz(2, AllianzOption.Handel) + " [false]");
		new SysMsg(" => Allianz für Partner 2 & Treiben - " + p.hatAllianz(2, AllianzOption.Steuern) + " [false]");
		if (p.hatAllianz(2, AllianzOption.Handel) != false) fail++;
		if (p.hatAllianz(2, AllianzOption.Steuern) != false) fail++;
		
		if (fail > 0) new BigError("TestCase für Allianzen ist schief gelaufen - " + fail + " Fehler");
	}
}
