package fantasya.library.io.order;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import static org.junit.Assert.*;

import org.junit.Test;

public class CleanOrderReaderTest {

	private CleanOrderReader generateReader(String s) {
		return new CleanOrderReader(new BufferedReader(new StringReader(s)));
	}

	// note: may be bad style to throw in the constructor (memory leak?), although
	// fixed
	// this should not be a serious issue for us
	@Test //(expected = NullPointerException.class)
	public void rejectNullStream() {
		CleanOrderReader reader = new CleanOrderReader(null);
		assertNull(reader.readOrder());
	}
	
	@Test
	public void joinLinesWithWhiteSpacesAfterBreakLine() {
		String command = "HELFE 4 KAEMPFE";
		String input = "HELFE 4\\ \n KAEMPFE";

		CleanOrderReader reader = generateReader(input);

		assertEquals(command, reader.readOrder());
	}

	@Test
	public void readMultipleCommandsSequentially() {
		String command1 = "MACHE 15 Schwert";
		String command2 = "BETRETE GEBAEUDE tx";
		String command3 = "NACH o w";
		CleanOrderReader reader = generateReader(command1 + "\n" + command2 + "\n" + command3);

		assertEquals(command1, reader.readOrder());
		assertEquals(command2, reader.readOrder());
		assertEquals(command3, reader.readOrder());
	}

	@Test
	public void skipEmptyLines() {
		String command = "Mache 18 Holz";
		CleanOrderReader reader = generateReader("\t \n" + command);

		assertEquals(command, reader.readOrder());
	}

	@Test
	public void returnNullOnEOF() {
		String command = "Do something";
		CleanOrderReader reader = generateReader(command);

		assertEquals(command, reader.readOrder());
		assertNull(reader.readOrder());
	}

	@Test
	public void joinLines() {
		String command = "HELFE 4 KAEMPFE";
		String input = "HELFE 4 \\\nKAEMPFE";

		CleanOrderReader reader = generateReader(input);

		assertEquals(command, reader.readOrder());
	}

	@Test
	public void handleEOFOnJoining() {
		String command = "HELFE someone";
		CleanOrderReader reader = generateReader(command + "\\");

		assertEquals(command, reader.readOrder());
	}

	@Test
	public void handleIOErrors() throws Exception {
		final Mockery context = new Mockery(){{ setImposteriser(ClassImposteriser.INSTANCE); }};
		final BufferedReader mockReader = context.mock(BufferedReader.class);

		context.checking(new Expectations() {{
			oneOf(mockReader).readLine();
				will(throwException(new IOException()));
		}});

		CleanOrderReader reader = new CleanOrderReader(mockReader);

		assertNull(reader.readOrder());
		context.assertIsSatisfied();
	}
	
	@Test
	public void trimmedLine() {
		String command = "HELFE 4 KAEMPFE";
		String input = " HELFE 4 KAEMPFE\t";

		CleanOrderReader reader = generateReader(input);

		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void removeRegionTagOfMagellan() {
		String command = "HELFE 4 KAEMPFE";
		String input = "REGION -40,36,1 ; Egafocodog\nHELFE 4 KAEMPFE";

		CleanOrderReader reader = generateReader(input);

		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void removeTemporaryCommentAfterOrder() {
		String command = "HELFE 4 KAEMPFE";
		String input = "HELFE 4 KAEMPFE\t; bla bla";

		CleanOrderReader reader = generateReader(input);

		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void removeTemporaryCommentAfterOrder2() {
		String command = "HELFE 4 KAEMPFE";
		String input = "HELFE 4 KAEMPFE; bla bla";

		CleanOrderReader reader = generateReader(input);

		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void removeTemporaryComment() {
		String command = "HELFE 4 KAEMPFE";
		String input = "; bla bla\nHELFE 4 KAEMPFE";

		CleanOrderReader reader = generateReader(input);

		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void removeMultipleWhitespace() {
		String command = "HELFE 4 KAEMPFE";
		String input = "HELFE\t 4  KAEMPFE";

		CleanOrderReader reader = generateReader(input);

		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void hasCommentAfterOrder() {
		String[] inputArray = {"HELFE 4 KAEMPFE\t// test\ttest ", "HELFE 4 KAEMPFE\t//test\ttest ", "HELFE 4 KAEMPFE\t//\ttest\ttest ", "HELFE 4 KAEMPFE// test\ttest "};
		String[] commandArray = {"HELFE 4 KAEMPFE // test\ttest", "HELFE 4 KAEMPFE //test\ttest", "HELFE 4 KAEMPFE //\ttest\ttest", "HELFE 4 KAEMPFE // test\ttest"};
		// String command = "HELFE 4 KAEMPFE // test\ttest";
		// String input = "HELFE 4 KAEMPFE\t// test\ttest ";
		
		if (inputArray.length != commandArray.length) {
			fail("Input array and command array has not same length.");
		}
		
		String input;
		String command;
		
		for (int i = 0; i < inputArray.length && i < commandArray.length; i++) {
			input = inputArray[i];
			command = commandArray[i];
			CleanOrderReader reader = generateReader(input);
			assertEquals(command, reader.readOrder());
		}
	}
	
	@Test
	public void replaceTabs() {
		String command = "HELFE 4 KAEMPFE";
		String input = "HELFE\t4\tKAEMPFE";

		CleanOrderReader reader = generateReader(input);
		
		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void getCommentsWithTabs() {
		String command = "// hilfe\ttest";
		String input = "// hilfe\ttest";

		CleanOrderReader reader = generateReader(input);
		
		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void normalizeSpecialGermanChars() {
		String command = "HELFE 4 KAEMPFE";
		String input = "HELFE 4 K" + "\u00c4" + "MPFE"; // KÄMPFE

		CleanOrderReader reader = generateReader(input);
		
		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void tempCommentsWithMultipleLines() {
		String command = "HELFE 4 KAEMPFE";
		String input = ";hilfe\ttest\\\n , \nHELFE 4 KAEMPFE";

		CleanOrderReader reader = generateReader(input);
		
		assertEquals(command, reader.readOrder());
	}
	
	@Test
	public void normalizeNotInQuotedText() {
		String[] command = { "BENENNE REGION \"Ünterwald\"" , "ZAUBERE \"Hain der tausend Eichen\" 1" };
		String input = "BENENNE REGION \"Ünterwald\"\nZAUBERE \"Hain der tausend Eichen\" 1";

		CleanOrderReader reader = generateReader(input);
		
		assertEquals(command[0], reader.readOrder());
		assertEquals(command[1], reader.readOrder());
	}
}
