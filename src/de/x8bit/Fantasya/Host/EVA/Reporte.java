package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Host.EVA.util.ParteiTalentTabelle;
import de.x8bit.Fantasya.Atlantis.Building;
import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.EVA.util.TopSkillCache;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Reports.ReportCR;
import de.x8bit.Fantasya.Host.Reports.ReportNR;
import de.x8bit.Fantasya.Host.Reports.ReportXML;
import de.x8bit.Fantasya.Host.Reports.ReportZR;
import de.x8bit.Fantasya.Host.Reports.SyntaxHighlightingNR;
import de.x8bit.Fantasya.Host.Reports.Zipping;
import de.x8bit.Fantasya.util.StatSerie;
import java.util.List;

public class Reporte extends EVABase implements NotACommand
{
	private List<Partei> spieler;

	/**
	 * für jede betretbare Region wird hier ein Wert in die Datenreihe aufgenommen: (Regions-)Silber pro (Bauern-)Kopf
	 */
	protected static StatSerie SilberProBauer = new StatSerie();


	public static TopSkillCache TopSkillCache = null;
	
	public Reporte() {
		super("erstelle Reporte");
	}
	
	@Override
	public void PreAction() {
        InselVerwaltung iv = InselVerwaltung.getInstance();
        iv.karteVerarbeiten();
        iv.parteiInseln();

		SilberProBauer = new StatSerie();

		for (Region r : Region.CACHE.values()) {
			if (r.istBetretbar(null)) {
				if (r.getBauern() > 0) {
					SilberProBauer.add(r.getSilber() / r.getBauern());
				}
			}
		}

		Building.ErneuereBewohnerCache();

		TopSkillCache = new TopSkillCache(Unit.CACHE);
		// new Debug(TopSkillCache.toString(Wahrnehmung.class));
		
		Region.USE_TOPTW_CACHE = true;
        

		// new VolksZaehlung(VolksZaehlung.MODUS_T0);
		// new VolksZaehlung(VolksZaehlung.MODUS_T1);
	}

	@Override
	public void PostAction() {
		spieler = new ArrayList<Partei>();
		spieler.addAll(Partei.PROXY);

        int nSpieler = 0;
        int nMonster = 0;
        for (Partei p : spieler) {
            if (p.getNummer() == 0) continue; // zähle gar nicht mit
            if (p.isMonster()) {
                nMonster ++;
            } else {
                nSpieler ++;
            }
        }

		new SysMsg(nSpieler + " Spieler- und " + nMonster + " NPC-Parteien gefunden.");

		for(Partei p : spieler) {
			new SysMsg("Reporte für " + p + ", " + GameRules.getRunde());

			new ReportCR(p);
			new ReportNR(p);
            new SyntaxHighlightingNR(p);
			new ReportZR(p);
			new ReportXML(p);

            boolean dbEnabled = Datenbank.isEnabled();
            if (!dbEnabled) Datenbank.Enable();
			new Zipping(p);
            if (!dbEnabled) Datenbank.Disable();
		}

        new ParteiTalentTabelle();

		Region.USE_TOPTW_CACHE = false;
		TopSkillCache = null;
	}

	public static StatSerie SilberProBauerSerie() {
		return SilberProBauer;
	}

	@Override
	public boolean DoAction(Unit u, String befehl[]) { return false; }
	@Override
	public void DoAction(Region r, String befehl) { }
	@Override
    public void DoAction(Einzelbefehl eb) { }

}
