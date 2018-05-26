package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Host.EVA.VolksZaehlung;
import java.util.HashMap;

/**
 *
 * @param <K>
 * @param <V> 
 * @author hapebe
 */
public class Soziologie<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = 2495525957522416762L;

	public static final Soziologie<Integer, VolksZaehlung> Vz = new Soziologie<Integer, VolksZaehlung>();
}
