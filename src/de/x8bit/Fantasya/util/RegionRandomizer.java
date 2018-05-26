package de.x8bit.Fantasya.util;

import de.x8bit.Fantasya.Host.Terraforming.KegelInsel;
import de.x8bit.Fantasya.Host.Terraforming.InselGenerator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Host.CommandLineArg;

/**
 * Test-Ballon - für eine gesteuerte Häufigkeitsverteilung von Terrains bei der Regionserstellung.
 * 2010-09-25
 * @author hapebe
 */
public class RegionRandomizer implements CommandLineArg {
	Map<Class<? extends Atlantis>, Double> logChancen = new HashMap<Class<? extends Atlantis>, Double>();

    @Override
	public List<String> getCommandLineOptions() {
		List<String> retval = new ArrayList<String>();

		retval.add("RegionRandomizer.neueInsel");
		retval.add("RegionRandomizer.testCalc");

		return retval;
	}

    @Override
	public void executeCommandLineOption(String commandLineOption, String arg) {
		if (commandLineOption.equals("RegionRandomizer.testCalc")) {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);

			KegelInsel pi = new KegelInsel(1, "Test-Insel");
			Map<Class<? extends Atlantis>, Double> ps = pi.getTerrainProbabilities();
			// und alle resultierenden Wahrscheinlichkeiten in % ausgeben:
			System.out.println("Wahrscheinlichkeit der Terrains:");
			for (Class<? extends Atlantis> typ : ps.keySet()) {
				double chance = ps.get(typ);
				System.out.println("  " + typ.getSimpleName() + " - " + nf.format(chance * 100) + "%");
			}

        } else if(commandLineOption.equals("RegionRandomizer.neueInsel")) {
            this.neueInsel();
		} else {
            throw new UnsupportedOperationException(commandLineOption + " - diese Aktion wird nicht unterstützt.");
        }
	}

    public void neueInsel() {
        InselGenerator ig = new InselGenerator();
        try {
            ig.make();
        } catch (InstantiationException ex) {
            new BigError(ex);
        } catch (IllegalAccessException ex) {
            new BigError(ex);
        }
    }

}
