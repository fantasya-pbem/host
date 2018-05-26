package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.UnitHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;

/**
 * hier werden neue Einheiten erzeugt ... die Methode Action wird nicht aufgerufen !!
 * @author mogel
 */
public class TempEinheiten extends EVABase
{
	public TempEinheiten()
	{
		super("Erstelle Temp-Einheiten");

		for(Unit unit : Unit.CACHE) {
			unit.setTempNummer(0);
		}
		createTempEVA();
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(TempEinheiten.class, 0 + EVABase.TEMP, "^@?(mache)[n]? (temp) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "m", Art.KURZ);
		bm.addHint(new UnitHint(1));
        bm.setKeywords("mache", "machen");
		retval.add(bm);

        bm = new BefehlsMuster(TempEinheiten.class, 1, "^@?(ende)([ ]+(\\/\\/).*)?", "e", Art.KURZ);
        bm.setKeywords("ende");
        retval.add(bm);

        return retval;
    }

	private void createTempEVA() {
		for(Einzelbefehl eb : BefehlsSpeicher.getInstance().getAll(this.getClass())) eb.getUnit().MacheTemp();
	}

	public void PostAction() { }
	public void PreAction() { }

	@Override
	public boolean DoAction(Unit u, String[] befehl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void DoAction(Region r, String befehl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public void DoAction(Einzelbefehl eb) { }

}
