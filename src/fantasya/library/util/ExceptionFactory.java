package fantasya.library.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionFactory {
	public static String getExceptionDetails(Exception ex) {
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
