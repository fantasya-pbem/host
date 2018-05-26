package de.x8bit.Fantasya.util.html;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RegexpRendererTest {

	/** The delimiter to use for opening a parameter with value "{{{" */
	private static String openDelimiter = "<!--@";
	/** The delimiter that is used for closing a parameter with value "}}}" */
	private static String closeDelimiter = "-->";

	/** A testing pattern. */
	private static String pattern = "<!--@greeting = \"Hello\"-->, <!--@ subject = \"World\" -->";
	/** The same pattern with all parameters replaced by their defaults. */
	private static String defaultPattern = "Hello, World";
	/** The parameters in the pattern. */
	private static String[] patternParameters = new String[] {"greeting", "subject"};

	private StringReader input;
	private RegexpRenderer renderer;


	@Before
	public void setup() throws Exception {
		input = new StringReader(pattern);
		renderer = new RegexpRenderer(input, openDelimiter, closeDelimiter);
	}


	// Tests for invalid input


	@Test(expected = IllegalArgumentException.class)
	public void rendererDoesNotAcceptNullStreams() throws Exception {
		new RegexpRenderer(null, openDelimiter, closeDelimiter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rendererDoesNotAcceptNullOpenings() throws Exception {
		new RegexpRenderer(input, null, closeDelimiter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rendererDoesNotAcceptEmptyOpenings() throws Exception {
		new RegexpRenderer(input, "", closeDelimiter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rendererDoesNotAcceptNullClosings() throws Exception {
		new RegexpRenderer(input, openDelimiter, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rendererDoesNotAcceptEmptyClosings() throws Exception {
		new RegexpRenderer(input, openDelimiter, "");
	}

	@Test(expected = IOException.class)
	public void invalidInputStreamGivesAnException() throws Exception {
		// simple invalidation: close the stream.
		input.close();
		new RegexpRenderer(input, openDelimiter, closeDelimiter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwAnErrorOnEmptyTemplate() throws Exception {
		input = new StringReader("");
		new RegexpRenderer(input, openDelimiter, closeDelimiter);
	}

	@Test
	public void validSetupWorks() throws Exception {
		input = new StringReader(pattern);
		new RegexpRenderer(input, openDelimiter, closeDelimiter);
	}


	// now tests of the functionality


	@Test
	public void noRenderingOfPlainMessages() throws Exception {
		input = new StringReader(defaultPattern);
		RegexpRenderer renderer  = new RegexpRenderer(input, openDelimiter, closeDelimiter);

		Assert.assertEquals("Renderer modified the input string.",
				defaultPattern, renderer.processTemplate());
	}

	@Test
	public void returnListOfParametersToModify() {
		Set<String> paramSet = renderer.getParameters();

		Assert.assertNotNull("Returned set was null", paramSet);
		Assert.assertEquals("Incorrect number of parameters.", patternParameters.length, paramSet.size());

		for (String s : patternParameters) {
			Assert.assertTrue("Parameter " + s + " not in returned parameters", paramSet.contains(s));
		}
	}

	@Test
	public void replaceParametersByDefaultValues() {
		Assert.assertEquals("Rendering with default values failed.",
				defaultPattern, renderer.processTemplate());
	}

	@Test
	public void setParametersAreReplaced() throws Exception {
		// ok, this test is hardcoded, otherwise I get too much clutter
		Map<String,String> params = new HashMap<String,String>();
		params.put("greeting", "Goodbye");

		renderer.setParameters(params);
		Assert.assertEquals("Rendering did not properly replace values",
				"Goodbye, World", renderer.processTemplate());
	}

	@Test
	public void checkThatParametersAreDeepCopied() throws Exception {
		Map<String,String> params = new HashMap<String,String>();

		params.put(patternParameters[0], "blah");
		renderer.setParameters(params);
		String orig = renderer.processTemplate();

		params.put(patternParameters[0], "blubb");
		Assert.assertEquals("Parameters were not deeply copied.",
				orig, renderer.processTemplate());
	}

	@Test(expected = InvalidParameterException.class)
	public void throwExceptionOnInvalidParameterSetting() throws Exception {
		Map<String,String> params = new HashMap<String,String>();
		params.put("probablyNotExistingParameter", "blah");

		renderer.setParameters(params);
	}
}
