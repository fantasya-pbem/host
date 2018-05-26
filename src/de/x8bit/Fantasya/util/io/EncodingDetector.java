package de.x8bit.Fantasya.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;

/**
 *
 * @author hapebe
 */
public class EncodingDetector {

	/** Versucht, das Encoding einer Datei zu raten.
	 *
	 * Die Funktion probiert lediglich ein paar Standard-Encodings aus und
	 * schaut, ob es beim Einlesen Probleme gibt.
	 */
	public static Charset guess(File f) {
		// Lies die Datei komplett in den Array "bytes" ein.
		FileInputStream is = null;
		byte[] bytes = null;
		try {
			is = new FileInputStream(f);
			long length = f.length();
			if (length > Integer.MAX_VALUE) {
				throw new IOException(f.getName() + " ist zu groß (" + length + " Bytes).");
			}
			bytes = new byte[(int)length];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			if (offset < bytes.length) throw new IOException("Die Datei konnte nicht vollständig gelesen werden - " + f.getName());
			is.close();
		} catch (IOException ex) {
			new BigError(ex);
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
				new BigError(ex);
			}
		}

		// Erstelle eine handvoll encodings, und probiert sie durch.
		List<String> encodings = new ArrayList<String>();
		encodings.add("UTF-8");
		encodings.add("ISO-8859-1");
		encodings.add("US-ASCII");

		List<String> suggestions = new ArrayList<String>();

		for (String encoding : encodings) {
			CharsetDecoder decoder = Charset.forName(encoding).newDecoder();
			try {
				@SuppressWarnings("unused") // - Exception zur Flußsteuerung
				CharBuffer buffer = decoder.decode(ByteBuffer.wrap(bytes));
				// System.out.println(Arrays.toString(buffer.array()));
				suggestions.add(encoding);
				// new Debug(f.getName() + " könnte " + encoding + " sein...");
			} catch (CharacterCodingException ex) {
				// new Debug(f.getName() + " ist kein " + encoding + ": " + ex);
			}
		}

		if (suggestions.size() == 1) return Charset.forName(suggestions.get(0));
		if (suggestions.size() > 1) {
			// new SysMsg("Datei " + f.getName() + " könnte " + StringUtils.aufzaehlung(suggestions) + " sein... !");
			return Charset.forName(suggestions.get(0));
		}

		new SysMsg("Warnung: Konnte das Charset von " + f.getName() + " nicht bestimmen.");
		return null;
	}
}
