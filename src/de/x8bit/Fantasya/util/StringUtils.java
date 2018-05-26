package de.x8bit.Fantasya.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @author hapebe
 */
@SuppressWarnings("rawtypes")
public final class StringUtils {

	/** Entfernt optionale Anfuehrungszeichen von einem String. */	
	public static String Trim(String string)
	{
		while(string.startsWith("\"")) string = string.substring(1);
		while(string.endsWith("\"")) string = string.substring(0, string.length() - 1);
		return string;
	}

	/**
	 * @param s array of Strings
	 * @param glue this should be added between each element of s
	 * @return a String containing all elements of s in sequence, separated by pieces of glue.
	 */
	public static String join(String[] s, String glue) {
        return StringUtils.join(Arrays.asList(s), glue);
	}

    public static String join(List<String> s, String glue) {
        if (s.isEmpty()) return null;
        StringBuilder out = new StringBuilder();
        out.append(s.get(0));
        for (int i=1; i<s.size(); i++) out.append(glue).append(s.get(i));
        return out.toString();
    }

	public static String aufzaehlung(List parts) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object part : parts) {
            if (i > 0) {
                if (i < parts.size() - 1) {
                    sb.append(", ");
                } else {
                    sb.append(" und ");
                }
            }
            sb.append(part.toString());

            i ++;
        }


        return sb.toString();
    }

    public static String aufzaehlungOder(List parts) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object part : parts) {
            if (i > 0) {
                if (i < parts.size() - 1) {
                    sb.append(", ");
                } else {
                    sb.append(" oder ");
                }
            }
            sb.append(part.toString());

            i ++;
        }


        return sb.toString();
    }


	@SuppressWarnings("unchecked")
	public static String aufzaehlung(Set parts) {
		List temp = new ArrayList();
		temp.addAll(parts);
		return StringUtils.aufzaehlung(temp);
	}

    public static String liste(List parts) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object part : parts) {
            sb.append((i+1)).append(". ").append(part);
            if (i < parts.size() - 1) sb.append("\n");
            i ++;
        }
        return sb.toString();
    }

	@SuppressWarnings("unchecked")
    public static String liste(Set parts) {
        List tmp = new ArrayList();
        tmp.addAll(parts);
        return StringUtils.liste(tmp);
    }

	/**
	 * @param str
	 * @return eine Kopie von str mit einem Großbuchstaben am Anfang (per String.toUpperCase())
	 */
	public static String ucfirst(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
	/**
	 * check for ' to get a valid sql-statement
     * @param value
     * @return
     */
	public static String checkValue(String value) {
		value = value.trim();
		if (value.indexOf("'") == -1) { return value; }
		
		StringBuilder build = new StringBuilder();
		
		for (int i = 0; i < value.length(); i++) {
			boolean add = true;
			char thisChar = value.charAt(i);
			if (thisChar == '\'') {
				build.append('\\');
			}
			else if (thisChar == '\\') {
				add = false;
				/*if (i + 1 == value.length()) {
					add = false;
				}
				else if (i + 1 < value.length()
					&& value.charAt(i + 1) == '\'') {
					add = false;
				}*/
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

	public static String only7bit(String s) {
	  StringBuffer r = new StringBuffer( s.length() );
	  r.setLength( s.length() );
	  int current = 0;
	  for (int i = 0; i < s.length(); i ++) {
		 char cur = s.charAt(i);
		 if (cur < 128) r.setCharAt( current++, cur );
	  }
	  r.setLength(current);
	  return r.toString();
	}

	public static String only8bit(String s) {
	  StringBuffer r = new StringBuffer( s.length() );
	  r.setLength( s.length() );
	  int current = 0;
	  for (int i = 0; i < s.length(); i ++) {
		 char cur = s.charAt(i);
		 if (cur < 256) r.setCharAt( current++, cur );
	  }
	  return r.toString();
	}

	public static String anriss(String txt, int soll) {
		int ist = txt.length();
		if (ist > soll) {
			return txt.substring(0, soll - 3) + "...";
		}
		return txt;
	}
    
    public static List<String> idsIn(String text) {
        List<String> retval = new ArrayList<String>();
        
        if (text.indexOf("[") != -1) {
            String[] parts = text.split("\\[");
            for (String part : parts) {
                String[] parts2 = part.split("\\]");
                if (parts2.length != 2) continue; // ???
                try {
                    String id = parts2[0];
                    Codierung.fromBase36(id);
                    retval.add(id);
                } catch (NumberFormatException ex) {
                    ; // NOP
                }
            }
        }
        
        return retval;
    }
    
    public static String normalize(String notNormal) {
    	notNormal = notNormal.replace("=C4", "\u00e4"); // -> ä
    	notNormal = notNormal.replace("=C3", "\u00fc"); // -> ü
		notNormal = notNormal.replace("\u00e4", "ae"); // ä
		notNormal = notNormal.replace("\u00f6", "oe"); // ö
		notNormal = notNormal.replace("\u00fc", "ue"); // ü
		notNormal = notNormal.replace("\u00c4", "AE"); // Ä
		notNormal = notNormal.replace("\u00d6", "OE"); // Ö
		notNormal = notNormal.replace("\u00dc", "UE"); // Ü
		notNormal = notNormal.replace("\u00df", "ss"); // ß
		
		return notNormal;
    }
}
