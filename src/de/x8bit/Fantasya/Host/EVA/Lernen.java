package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.Lehren.LehrenRecord;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.SkillHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;

public class Lernen extends EVABase
{
	public final static int VARIANTE_EINFACH = 0;
	public final static int VARIANTE_ZIEL_TW = 1;
	
	
    /**
     * Map schuelerId => LehrenRecord, ist also eine "geflippte" Map gegenüber der gleichnamigen in Lehren.java
     */
    public static Map<Integer, List<LehrenRecord>> Unterricht = new HashMap<Integer, List<LehrenRecord>>();

    public Lernen()
	{
		super("lerne", "Lernen von Talenten");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		// Skills - alle Namen auflisten:
        List<String> skillNames = new ArrayList<String>();
        for (Paket p : Paket.getPaket("Skills")) {
            // skillNames.add(p.Klasse.getName().toLowerCase());
            skillNames.addAll(EVABase.getNames(p)); // damit werden auch "ComplexNames" berücksichtigt, bspw. Varianten mit / ohne Umlaut
        }
        // ... und als RegEx formulieren:
        StringBuilder regEx = new StringBuilder();
        regEx.append("(");
        for (String name : skillNames) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(" + name + ")");
        }
        regEx.append(")");


        BefehlsMuster bm = new BefehlsMuster(Lernen.class, VARIANTE_EINFACH, "^(lerne)[n]? " + regEx + "([ ]+(\\/\\/).*)?", "l", Art.LANG);
        bm.addHint(new SkillHint(1));
        bm.setKeywords("lerne", "lernen");
        retval.add(bm);

        bm = new BefehlsMuster(Lernen.class, VARIANTE_ZIEL_TW, "^(lerne)[n]? " + regEx + " (t|tw)?[1-9]{1}[0-9]{0,2}([ ]+(\\/\\/).*)?", "l", Art.LANG);
        bm.addHint(new SkillHint(1));
        bm.setKeywords("lerne", "lernen");
        retval.add(bm);

        return retval;
    }

	// EVA
    public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			// new Debug("Lerne: " + eb.toString());
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

			Class<? extends Skill> skill = eb.getSkill();

            if (!r.istBetretbar(null)) {
                eb.setError();
				if (r instanceof Ozean) {
					new Fehler(u + " kann nicht auf dem Ozean lernen.", u);
				} else {
					new Fehler(u + " kann an diesem Ort nicht lernen.", u);
				}
                continue;
            }
			
			int zielwert = -1;
			if (eb.getVariante() == VARIANTE_ZIEL_TW) {
				String sZielwert = eb.getTokens()[2].toLowerCase();
				sZielwert = sZielwert.replaceAll("t", "");
				sZielwert = sZielwert.replaceAll("w", "");
				
				try {
					// new Debug("Lern-TW-Zielwert: " + sZielwert);
					zielwert = Integer.parseInt(sZielwert);
				} catch (NumberFormatException ex) { /*nop*/ }
				
				if (zielwert > 0) {
					int aktuell = u.Talentwert(skill);
					if (aktuell < zielwert) {
						u.setTag("ejcOrdersConfirmed", 1);
					} else {
						eb.setError();
						new Fehler(u + " - die befohlene Ausbildung ist schon abgeschlossen!", u);
					}
				}
			}
            
			
			u.Lernen(skill);
			
			
			if (zielwert > 0) {
				if (u.Talentwert(skill) == zielwert) {
					if (!eb.isError()) new Info(u + " erreicht Talentwert " + zielwert + " in " + skill.getSimpleName() + ".", u);
					u.clearTag("ejcOrdersConfirmed");
				}
			}
			
            eb.setPerformed();
        }
    }

	public void PostAction() {
		// TODO Noch irgendwelche Statistiken generieren?

		// Speicher wieder freigeben.
		// wird jetzt noch für die Befehls-Bestätigung gebraucht,
		// in BefehlsFazit:
		// Lehren.Unterricht.clear(); 
		Lernen.Unterricht.clear();
	}

	public void PreAction() {
        // Schüler-Lehrer-Datenbank löschen
        Lernen.Unterricht.clear();
        
        // alle "LehrenRecords" holen:
        for (int lehrer : Lehren.Unterricht.keySet()) {
            List<LehrenRecord> recs = Lehren.Unterricht.get(lehrer);
            for (LehrenRecord lr : recs) {
                int schueler = lr.getSchueler().getNummer();
                List<LehrenRecord> meine = Lernen.Unterricht.get(schueler);
                if (meine == null) {
                    meine = new ArrayList<LehrenRecord>();
                    Lernen.Unterricht.put(schueler, meine);
                }
                meine.add(lr);
            }
        }

    }

    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
	
    @Override
	public void DoAction(Einzelbefehl eb) { }

}
