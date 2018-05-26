package fantasya.library.io.order;

import java.io.BufferedReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.x8bit.Fantasya.util.StringUtils;
import fantasya.library.util.ExceptionFactory;

public class CleanOrderReader {
	
	private final Logger LOGGER = LoggerFactory.getLogger(CleanOrderReader.class);
	
	private BufferedReader in;
	private String comment = null;

	public CleanOrderReader(BufferedReader in) {
		this.in = in;
	}
	
	public String readOrder() {

		// Read until we hit either end of stream or the command is read in.
		// Commands may extend over multiple lines (in this case, they end with
		// "\").
		String order = nextLine();
		
		// delete a possible old comment
		comment = null;

		while (order != null && order.trim().endsWith("\\")) {
			order = order.substring(0, order.trim().length() - 1);
			String line = nextLine();
			if (line == null) {
				break;
			}
			order = order + line;
		}

		// On end of stream or in case of I/O errors or in is null, we return null.
		if (order == null) {
			return null;
		}
		
		// new line if order is forlage XY
		// here for multiple forlage
		if (order.trim().toLowerCase().startsWith("forlage")) {
			return readOrder();
		}
		
		// new line for complete temporary comments (';')
		// here for multiple temp-comments
		if (order.trim().charAt(0) == ';') {
			return readOrder();
		}
		
		// if the order is a permanent comment, return
		if (order.trim().startsWith("//")) {
			return order.trim();
		}
		
		// cut out or remove a possible comment
		order = cutOutCommentAfterOrder(order);
		
		// trim the order
		order = order.trim();
		
		// normalize order parts that is not quoted.
		order = normalize(order);
		
		// return trimmed order
		return (comment == null) ? order.trim() : order.trim() + " " + comment;
	}
	
	/**
	 * Getting string from BufferedReader.
	 * Testing, if string has other character than space and tab.
	 * Returns an empty string if BufferedReader is empty or throws an exception.
	 * 
	 * @return string that has not only tab and space and is not empty.
	 * If BufferedReader is empty or throws an exception returns 'null'
	 */
	
	private String nextLine() {
		// note: we jump out of this loop by returning or by exceptions
		// note: returning null means also cl
		String line;
		while (true) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.warn(ExceptionFactory.getExceptionDetails(e));
				return null;
			} catch (NullPointerException e) {
				e.printStackTrace();
				LOGGER.warn("BufferedReader in is 'NULL'. CleanOrderReader needs a BufferedReader.\n" + ExceptionFactory.getExceptionDetails(e));
				return null;
			}

			// If the end of stream is reached, return null.
			if (line == null) {
				return null;
			}
			
			// continue empty line
			if (line.trim().isEmpty()) {
				continue;
			}
			
			// continue if order is magellan tag 'REGION x,y,z ; region name'
			if (line.toLowerCase().startsWith("region")) {
				continue;
			}

			// return line
			return line;
		}
	}
	
	private String removeMultipleWhitespace(String order, int begin) {
		for (int i = 0; i < order.length(); i++) {
			if (i + 1 < order.length()) {
				char firstChar = order.charAt(i);
				char secondChar = order.charAt(i + 1);
				// IMPORTANT: order has no tabs -> see line 71
				// Because the order has no tabs, order has not substring(0, i-1) + " " + substring(i + 1); 
				if (firstChar == ' ' && secondChar == ' ') {
					order = removeMultipleWhitespace(order.substring(0, i) + order.substring(i + 1), i);
					break;
				}
			}
		}
		return order;
	}
	
	/**
	 * remove comment from string and save a permanent comment.
	 * 
	 * @param order to test
	 * @param begin begin searching for comment.
	 * 
	 *  @param return order without comment
	 */
	
	private String cutOutCommentAfterOrder(String orderString) {
		int permantentIndex = orderString.indexOf("//", 0);
		int temporaryIndex = orderString.indexOf(";", 0);
		
		// if there is no comment return orderString
		if (permantentIndex < 0 && temporaryIndex < 0) {
			return orderString;
		}
		
		int commentMark;
		
		// if there is only a permanent comment.
		if (temporaryIndex < 0 && permantentIndex >= 0) {
			commentMark = permantentIndex;
		}
		// if there is only a temporary comment
		else if (permantentIndex < 0 && temporaryIndex >= 0) {
			commentMark = temporaryIndex;
		}
		// if there are both, first comment rules
		else if (permantentIndex < temporaryIndex) {
			commentMark = permantentIndex;
		}
		else {
			commentMark = temporaryIndex;
		}
		
		// if ruling comment is permanent save comment
		if (commentMark == permantentIndex) {
			comment = orderString.substring(commentMark).trim();
		}
		
		// return order without comment
		return orderString.substring(0, commentMark);
	}
	
	public String normalize(String order) {
		if (order.indexOf('\"') < 0) {
			
			// replaces all '\t' with  ' '
			order = order.replace('\t', ' ');
			
			// delete all multiple whitespace
			order = removeMultipleWhitespace(order, 0);
			
			// normalize german special characters
			order = StringUtils.normalize(order);
			
			return order;
		}
		
		String[] orderArray = { null, null, null };
		
		int firstQuote = order.indexOf('\"');
		int lastQuote = order.lastIndexOf('\"');
		
		orderArray[0] = order.substring(0, firstQuote).trim();
		
		if (lastQuote <= firstQuote || lastQuote > order.length() - 2) {
			orderArray[1] = order.substring(firstQuote).trim();
		}
		else {
			orderArray[1] = order.substring(firstQuote, lastQuote + 1).trim();
			orderArray[2] = order.substring(lastQuote + 1).trim();
		}
		
		String orderPart;
		
		orderPart = orderArray[0];
					
		// replaces all '\t' with  ' '
		orderPart = orderPart.replace('\t', ' ');
					
		// delete all multiple whitespace
		orderPart = removeMultipleWhitespace(orderPart, 0);
					
		// normalize german special characters
		orderPart = StringUtils.normalize(orderPart);
		
		orderArray[0] = orderPart;
		
		
		orderPart = orderArray[2];
		
		if (orderPart != null && !orderPart.isEmpty()) {
						
			// replaces all '\t' with  ' '
			orderPart = orderPart.replace('\t', ' ');
						
			// delete all multiple whitespace
			orderPart = removeMultipleWhitespace(orderPart, 0);
						
			// normalize german special characters
			orderPart = StringUtils.normalize(orderPart);
			
			orderArray[2] = orderPart;
		}
		
		order = orderArray[0] + " " + orderArray[1];
		
		return (orderArray[2] == null || orderArray[2].isEmpty()) ? order : order + " " + orderArray[2];
	}
	
	public void close() {
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.warn(ExceptionFactory.getExceptionDetails(e));
			return;
		}
	}
}