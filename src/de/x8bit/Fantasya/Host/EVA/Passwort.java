package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

public class Passwort extends EVABase
{
	public Passwort()
	{
		super("passwort", "Ändere Passwort");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        // retval.add(new BefehlsMuster(Passwort.class, 0, "^@?(passwort) (\")?.+(\")?([ ]+(\\/\\/).*)?", "p", Art.KURZ));
        return retval;
    }
	
	public void PostAction() { }
	public void PreAction() { }
	
	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

//			Unit u = eb.getUnit();
//            Partei p = Partei.Load(u.getOwner());
//            // COMMAND PASSWORT "<neues_passwort>"
//            String passwort = Datenbank.CheckValue(eb.getTokens()[1], true);
//            p.setPassword(Atlantis.Trim(passwort));
//
//            new Info("Das Passwort wurde auf '" + passwort + "' geändert.", u, u.getCoords());
            eb.setPerformed();
        }
    }

    @Override
    public boolean DoAction(Unit u, String[] befehl) { return false; }
    @Override
    public void DoAction(Einzelbefehl eb) { }


}
