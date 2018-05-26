package de.x8bit.Fantasya.Host;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.util.PackageLister;

/**
 *
 * @author hapebe
 */
public class Xecutor {
	Map<String, Class<? extends CommandLineArg>> commands = new TreeMap<String, Class<? extends CommandLineArg>>();

	public Xecutor(String command, String arg) {
		this.findCommands();

		if (command != null) {
			try {
				Class<? extends CommandLineArg> executorClass = commands.get(command);
				if (executorClass == null) {
					System.err.println("Das Kommando '" + command + "' ist unbekannt.");
					System.exit(-1);
				}
				Object o = executorClass.newInstance();
				CommandLineArg executor = (CommandLineArg) o;

				executor.executeCommandLineOption(command, arg);
				System.exit(0);
				
			} catch (InstantiationException ex) {
				new BigError(ex);
			} catch (IllegalAccessException ex) {
				new BigError(ex);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("\n\nVerfügbare Aktionen für -X :\n");
			for (String option : commands.keySet()) {
				sb.append("   " + option + "\n");
			}
			if (commands.isEmpty()) sb.append("   <KEINE>\n");
			System.out.println(sb.toString());
			System.exit(0);
		}
	}

    @SuppressWarnings("rawtypes")
	private void findCommands() {
        List<Class> klassen;
        String basePackage = "de.x8bit.Fantasya";
		// if (true) return;
        try {
            klassen = PackageLister.getClasses(basePackage);

            for (Class c : klassen) {
                // es gibt eine ganze Reihe Klassen, von denen wir keine Befehlsverarbeitung erwarten:
                if ((c.getModifiers() & Modifier.ABSTRACT) != 0) continue;
                if ((c.getModifiers() & Modifier.INTERFACE) != 0) continue;
                if (c.isMemberClass()) continue;
				// spezielle Ausnahme, weil diese Klasse ebenfalls das JAR durchsuchen will...
				if (c.getName().equals("de.x8bit.Fantasya.Host.BefehlsSpeicher")) continue;
				if (c.getName().equals("de.x8bit.Fantasya.Host.Paket")) continue;

				Class[] interfaces = c.getInterfaces();
				boolean found = false;
				for (Class interFace : interfaces) {
					if (interFace.getName().equals("de.x8bit.Fantasya.Host.CommandLineArg")) {
						found = true;
						break;
					}
				}

				if (!found) continue;

				Object o = null;
				try {
					o = c.newInstance();
				} catch (InstantiationException ex) {
                    System.err.println("Xecutor: Kann eine Klasse nicht nach Optionen durchsuchen - " + c.getName() + " - " + ex);
                } catch (Exception ex) {
					throw new RuntimeException("Xecutor: " + ex);
				}
				
				if (o == null) continue;
				
				CommandLineArg arg = (CommandLineArg)o;
				for (String cmd : arg.getCommandLineOptions()) commands.put(cmd, arg.getClass());
            }

            // new SysMsg(importCnt + " Kommandozeilen-Parameter gefunden.");
        } catch (URISyntaxException ex) {
            new BigError(ex);
        } catch (IOException ex) {
            new BigError(ex);
        } catch (ClassNotFoundException ex) {
            new BigError(ex);
        } catch (IllegalArgumentException ex) {
            new BigError(ex);
        } catch (SecurityException ex) {
            new BigError(ex);
        }

    }

}
