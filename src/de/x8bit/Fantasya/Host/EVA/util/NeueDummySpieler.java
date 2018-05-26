package de.x8bit.Fantasya.Host.EVA.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Host.CommandLineArg;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.util.lang.NonsenseTexter;

/**
 *
 * @author hapebe
 */
public class NeueDummySpieler implements CommandLineArg {

	public List<String> getCommandLineOptions() {
		List<String> retval = new ArrayList<String>();

		retval.add("NeueDummySpieler.1");
		retval.add("NeueDummySpieler.10");

		return retval;
	}

	@SuppressWarnings("unchecked")
	public void executeCommandLineOption(String commandLineOption, String arg) {
		Datenbank db = new Datenbank("NeueDummySpieler");
		try {
			EVAFastLoader.loadNeueSpieler(db);
		} catch (SQLException ex) {
			new BigError(ex);
		}

		int nNeueSpieler = 1;
		if (commandLineOption.equals("NeueDummySpieler.10")) nNeueSpieler = 10;

		List<Class<? extends Unit>> rassen = new ArrayList<Class<? extends Unit>>();
		for (Paket p : Paket.getPaket("Units")) {
			if ( ((Unit)p.Klasse).istSpielerRasse() ) {
				rassen.add((Class<? extends Unit>)p.Klasse.getClass());
			}
		}

		for (int i = 0; i < nNeueSpieler; i++) {
			Collections.shuffle(rassen);

			NeuerSpieler n = new NeuerSpieler();
			n.setRasse(rassen.get(0));
            n.setEmail(NonsenseTexter.makeNonsenseWort(2) + "@fantasya-pbem.de");
			n.setTarnung(null);
            n.setHolz(15);
            n.setEisen(15);
            n.setSteine(15);
            n.setInsel(0);

			NeuerSpieler.PROXY.add(n);
		}

		EVAFastSaver.saveNeueSpieler(db);
	}

}
