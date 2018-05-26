package de.x8bit.Fantasya.util;

public class DataBaseFactory {
	
	/**
	 * check for ' to get a valid sql-statement
     * @param value
     * @return
     */
	public static String checkValue(String value) {
		StringBuilder build = new StringBuilder();
		
		for (int i = 0; i < value.length(); i++) {
			boolean add = true;
			char thisChar = value.charAt(i);
			if (thisChar == '\'') {
				build.append('\\');
			}
			else if (thisChar == '\\' 
					&& i + 1 < value.length()
					&& value.charAt(i + 1) == '\'') {
					add = false;
			}
			/* if (!insert) {
                if (value.charAt(i) == '%') sb.append('\\');
                if (value.charAt(i) == '+') sb.append('\\');
            } */
			if (add) {
				build.append(thisChar);
			}
		}
		
		return build.toString();
	}
}
