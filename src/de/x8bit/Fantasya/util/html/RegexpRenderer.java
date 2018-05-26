package de.x8bit.Fantasya.util.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A class to read in a template from a file, and replace placeholders by
  * settable parameters.
  *
  * <p>
  * On construction, the renderer receives a text file with parameter clauses.
  * These clauses have the general form
  * </p>
  *
  * $opening $name = "$default" $closing
  *
  * <p>
  * Here, $opening and $closing mark the beginning and end of the clause, and
  * are set as regular expressions in the constructor (usually, start and end
  * of a comment in whatever file format the template uses). $name is the name
  * of the parameter, and $default its default value.
  * </p>
  *
  * <p>
  * The set of parameter names can be queried, and values for each parameter
  * name can be set. With this data, the template can be processed, which
  * returns a string where all clauses are replaced either by a user-set value,
  * or by the default value.
  * </p>
  */

public class RegexpRenderer {

	/** The pattern that is read in. */
	private String pattern;
	/** The opening clause. */
	private String opening;
	/** The closing clause. */
	private String closing;

	/** A mapping from parameter names to default values of the pattern. */
	private Map<String,String> parameters = new HashMap<String,String>();
	/** the parameters set by the user; null if none set. */
	private Map<String,String> userParams = new HashMap<String,String>();


	/** Creates a new renderer.
	  *
	  * This also reads the pattern from the input stream into an internal variable.
	  *
	  * @param input a Reader that defines the input source.
	  * @param opening a regular expression that signifies the start of a parameter token.
	  * @param closing a regular expression that signifies the end of a parameter token.
	  * @throws IllegalArgumentException if the input is null or one of opening
	  *     or closing is null or empty, or if the retrieved pattern is empty.
	  * @throws IOException if some error occured during reading from the stream.
	  */
	public RegexpRenderer(Reader input, String opening, String closing) throws IOException {
		// check the input parameters
		if (input == null) {
			throw new IllegalArgumentException("Input stream reader was null.");
		}
		if (opening == null || opening.length() == 0) {
			throw new IllegalArgumentException("Opening expression was empty.");
		}
		if (closing == null || closing.length() == 0) {
			throw new IllegalArgumentException("Closing expression was empty.");
		}

		this.opening = opening;
		this.closing = closing;

		// read in the pattern
		try {
			BufferedReader reader = new BufferedReader(input);
			pattern = reader.readLine();
			String line = reader.readLine();

			while (line != null) {
				pattern = "\n" + line;
				line = reader.readLine();
			}
		} catch (Exception e) {
			throw new IOException("Error reading in template.");
		}

		// if the pattern is empty, throw an exception
		if (pattern == null || pattern.length() == 0) {
			throw new IllegalArgumentException("Pattern is empty.");
		}

		// extract the parameters and default values for further use.
		analyzePattern();
	}

	/** Extracts the paramters from the template, and returns a list of them. */
	public Set<String> getParameters() {
		return parameters.keySet();
	}

	/** Sets the parameters to replace.
	  *
	  * Unset parameters are replaced by their default values.
	  *
	  * @param newParams the user-set parameters; overrides previous settings
	  * @throws InvalidParameterException if one or more parameters were not
	  * encountered in the pattern.
	  */
	public void setParameters(Map<String,String> newParams) throws InvalidParameterException {
		for (String key : newParams.keySet()) {
			if (!parameters.containsKey(key)) {
				throw new InvalidParameterException("Specified key " + key + " not found in pattern.");
			}
		}

		userParams = new HashMap<String,String>(newParams);
	}

	/** Processes the template and returns the processed String. */
	public String processTemplate() {
		String result = pattern;

		for (String key : parameters.keySet()) {
			String regexp = opening + "[ ]*" + key + "[ ]*=[ ]*\"" + parameters.get(key) + "\"[ ]*" + closing;

			if (userParams.containsKey(key)) {
				result = result.replaceAll(regexp, userParams.get(key));
			} else {
				result = result.replaceAll(regexp, parameters.get(key));
			}
		}

		return result;
	}

	/** Runs a over the pattern with regular expressions to obtain the
	  * parameters and default values.
	  */
	private void analyzePattern() {
		// the parentheses are used for accessing the groups (parameter name
		// and default) after the match.
		Matcher m = Pattern.compile(opening + "[ ]*(\\w+)[ ]*=[ ]*\"([^\"]+)\"[ ]*" + closing).matcher(pattern);

		// store values as long as the matcher finds something.
		while (m.find()) {
			parameters.put(m.group(1), m.group(2));
		}
	}
}
