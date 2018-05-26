package de.x8bit.Fantasya.Host;

import java.util.List;

/**
 * Kennzeichnet Klassen, die eigene erweiterte Optionen beim Aufruf von Main zur
 * Verfügung stellen (Option -X ...)
 * @author hapebe
 */
public interface CommandLineArg {

	/**
	 * Liste der verfügbaren Befehlszeilen-Aktionen.
	 * @return
	 */
	public List<String> getCommandLineOptions();

	/**
	 * Führt die Aktion aus
	 * @param commandLineOption auszuführende Aktion
	 */
	public void executeCommandLineOption(String commandLineOption, String arg);

}
