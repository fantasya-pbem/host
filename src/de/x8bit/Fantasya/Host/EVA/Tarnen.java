package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.util.Codierung;

public class Tarnen extends EVABase
{
	public Tarnen()	{
		super("tarne", "Ohne und mit Tarnung wird gemauschelt");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Tarnen.class, 1, "^@?(tarne)[n]? (einheit)([ ]+(\\/\\/).*)?", "t", Art.KURZ);
        bm.setKeywords("tarne", "tarnen", "einheit");
        retval.add(bm);
        bm = new BefehlsMuster(Tarnen.class, 2, "^@?(tarne)[n]? (einheit) (nicht)([ ]+(\\/\\/).*)?", "t", Art.KURZ);
        bm.setKeywords("tarne", "tarnen", "einheit", "nicht");
        retval.add(bm);

        bm = new BefehlsMuster(Tarnen.class, 21, "^@?(tarne)[n]? ((volk)|(partei)) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "t", Art.KURZ);
		bm.addHint(new IDHint(2));
        bm.setKeywords("tarne", "tarnen", "volk", "partei");
		retval.add(bm);
        bm = new BefehlsMuster(Tarnen.class, 22, "^@?(tarne)[n]? ((volk)|(partei)) (nicht)([ ]+(\\/\\/).*)?", "t", Art.KURZ);
        bm.setKeywords("tarne", "tarnen", "volk", "partei", "nicht");
        retval.add(bm);
        bm = new BefehlsMuster(Tarnen.class, 25, "^@?(tarne)[n]? ((volk)|(partei))([ ]+(\\/\\/).*)?", "t", Art.KURZ);
        bm.setKeywords("tarne", "tarnen", "volk", "partei");
        retval.add(bm);

		// Rassen - alle Namen auflisten:
        List<String> rasseNamen = new ArrayList<String>();
        for (Paket p : Paket.getPaket("Units")) {
            rasseNamen.add(p.Klasse.getName().toLowerCase());
        }
        // ... und als RegEx formulieren:
        StringBuilder regEx = new StringBuilder();
        regEx.append("(");
        for (String name : rasseNamen) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(" + name + ")");
        }
        regEx.append(")");

        bm = new BefehlsMuster(Tarnen.class, 11, "^@?(tarne)[n]? (rasse) " + regEx + "([ ]+(\\/\\/).*)?", "t", Art.KURZ);
        bm.setKeywords("tarne", "tarnen", "rasse");
        retval.add(bm);
        bm = new BefehlsMuster(Tarnen.class, 12, "^@?(tarne)[n]? (rasse) (nicht)([ ]+(\\/\\/).*)?", "t", Art.KURZ);
        bm.setKeywords("tarne", "tarnen", "rasse", "nicht");
        retval.add(bm);

        bm = new BefehlsMuster(Tarnen.class, 42, "^@?(tarne)[n]? (nicht)([ ]+(\\/\\/).*)?", "t", Art.KURZ);
        bm.setKeywords("tarne", "tarnen", "nicht");
        retval.add(bm);

        return retval;
    }
	
	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			Unit u = eb.getUnit();

            if (eb.getVariante() == 42) {
                // COMMAND TARNE NICHT -> hebt alles für diese Einheit auf
                new Info(u + " tarnt sich nicht mehr.", u);
                u.setSichtbarkeit(0);
                u.setTarnPartei(u.getOwner());
                if (!u.getTarnRasse().equals(u.getRasse())) {
                    u.setTarnRasse(u.getRasse());
                }
                eb.setPerformed(); continue;
            }

            if (eb.getVariante() == 1) {
                // COMMAND TARNE EINHEIT
                u.setSichtbarkeit(1);
				if (u.Talentwert(Tarnung.class) > 0) {
					new Info(u + " versteckt sich vor anderen Einheiten.", u);
				} else {
					new Info(u + " versteckt sich künftig vor anderen Einheiten, aber derzeit fehlt noch das Talent dazu.", u);
				}
                eb.setPerformed(); continue;
            }

            if (eb.getVariante() == 2) {
                // COMMAND TARNE EINHEIT NICHT
                u.setSichtbarkeit(0);
                new Info(u + " zeigt sich wieder anderen Einheiten.", u);
                eb.setPerformed(); continue;
            }



            if (eb.getVariante() == 11) {
                // COMMAND TARNE RASSE <rasse>
                Unit hu = (Unit) Paket.FindUnit(eb.getTokens()[2]).Klasse;
                if (hu != null) {
                    u.setTarnRasse(hu.getClass().getSimpleName());
                    if (u.getTarnRasse().length() > 0) {
                        new Info(u + " tarnt sich als Rasse '" + u.getTarnRasse() + "'.", u);
                    } else {
                        // Fehlermeldung wird in Unit.setTarnRasse erzeugt.
                        eb.setError();
                    }
                } else {
                    new Fehler("Die Rasse '" + eb.getTokens()[2] + "' ist unbekannt.", u);
                    eb.setError();
                }
                eb.setPerformed(); continue;
            }
            if (eb.getVariante() == 12) {
                // COMMAND TARNE RASSE NICHT
                u.setTarnRasse("");
                new Info(u + " zeigt wieder seine Rasse.", u);
                eb.setPerformed(); continue;
            }


            if (eb.getVariante() == 21) {
                // COMMAND TARNE PARTEI <volk>
                // COMMAND TARNE VOLK <volk>
                int nr = 0;
                try { nr = Codierung.fromBase36(eb.getTargetId()); } catch(Exception ex) { };
                Partei partei = Partei.getPartei(nr);
                if (partei != null) {
                    u.setTarnPartei(nr);
                    new Info(u + " tarnt sich als Angehöriger von '" + partei + "'.", u);
                } else {
                    eb.setError();
                    new Fehler(u + " kann sich nicht als Partei '" + nr + "' tarnen.", u);
                }
                eb.setPerformed(); continue;
            }

            if (eb.getVariante() == 25) {
                // COMMAND TARNE PARTEI
                u.setTarnPartei(0);
                new Info(u + " versteckt seine Partei-Zugehörigkeit.", u);
                eb.setPerformed(); continue;
            }

            if (eb.getVariante() == 22) {
                // COMMAND TARNE PARTEI NICHT
                // COMMAND TARNE VOLK NICHT
                u.setTarnPartei(u.getOwner());
                new Info(u + " zeigt wieder seine wahre Zugehörigkeit.", u);
                eb.setPerformed(); continue;
            }
            
        } // nächster Einzelbefehl
    }

    @Override
    public void DoAction(Einzelbefehl eb) { }

   	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }

	@Override
	public void PostAction() { }

	@Override
	public void PreAction() { }


}
