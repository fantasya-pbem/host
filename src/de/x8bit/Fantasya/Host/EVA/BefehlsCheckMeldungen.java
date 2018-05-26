package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsCheck;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import java.util.List;

/**
 *
 * @author hb
 */
public class BefehlsCheckMeldungen extends EVABase implements NotACommand {

    public BefehlsCheckMeldungen() {
        super("Befehls-Check-Meldungen...");

        ZATMode zatMode = ZATMode.CurrentMode();

		if (zatMode.getSkip(this.getClass())) return;

        boolean dbEnabled = Datenbank.isEnabled();
        if (!dbEnabled) Datenbank.Enable();

        for (Partei p : BefehlsCheck.getInstance().getParteien()) {
            List<Message> meldungen = Message.Retrieve(p, (Coords)null, null);

            StringBuilder sb = new StringBuilder();
            sb.append("Befehle für Partei " + p + " wurden überprüft:\n\n");

            int fehlerCnt = 0;
            // TODO
            // int warningCnt = 0;
            for (Message m : meldungen) { if (m instanceof Fehler) fehlerCnt++; }

            if (fehlerCnt > 0) {
                sb.append("Es gab " + fehlerCnt + " Fehler:\n\n");
                for (Message m : meldungen) {
                    if (!(m instanceof Fehler)) continue;

					String prefix = "Fehler: ";
					if (m.getUnit() != null) prefix = m.getUnit() + ": ";
                    sb.append(prefix + m.getText() + "\n");
                }
            } else {
                sb.append("Es gab keine Fehler - die Befehle scheinen in Ordnung zu sein.\n");
            }
            
            new Debug(sb.toString());
        }

        if (!dbEnabled) Datenbank.Disable();
    }



    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void DoAction(Region r, String befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void DoAction(Einzelbefehl eb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void PreAction() {  }

    @Override
    public void PostAction() {  }

}