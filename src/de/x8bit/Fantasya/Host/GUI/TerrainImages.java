package de.x8bit.Fantasya.Host.GUI;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Hochland;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sumpf;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/** Zeitweiser Hack, um Terrainbilder zu cachen.
 *
 * Das ist zwar haesslich, aber die vorherige Version mit dem Cachen der Bilder
 * in den Regionsklassen ist haesslich, doppelter Code und falsches Design. Insofern
 * ist das ein Fortschritt.
 */
public class TerrainImages {

	private Map<Class<? extends Region>, BufferedImage> imageCache
			= new HashMap<Class<? extends Region>, BufferedImage>();

	public TerrainImages() {
		try {
			imageCache.put(Wald.class, ImageIO.read(new File("gfx/terrains/wald.jpg")));
			imageCache.put(Berge.class, ImageIO.read(new File("gfx/terrains/berge.jpg")));
			imageCache.put(Sumpf.class, ImageIO.read(new File("gfx/terrains/sumpf.jpg")));
			imageCache.put(Hochland.class, ImageIO.read(new File("gfx/terrains/hochland.jpg")));
			imageCache.put(Gletscher.class, ImageIO.read(new File("gfx/terrains/gletscher.jpg")));
			imageCache.put(Ebene.class, ImageIO.read(new File("gfx/terrains/ebene.jpg")));
			imageCache.put(Chaos.class, ImageIO.read(new File("gfx/terrains/chaos.jpg")));
			imageCache.put(Wueste.class, ImageIO.read(new File("gfx/terrains/wueste.jpg")));
			imageCache.put(Ozean.class, ImageIO.read(new File("gfx/terrains/ozean.jpg")));
		} catch (Exception ex) {
			throw new RuntimeException("Fehler beim Laden der Terrainbilder.", ex);
		}
	}

	public BufferedImage getImage(Region r) {
		if (imageCache.containsKey(r.getClass())) {
			return imageCache.get(r.getClass());
		}

		return null;
	}
}
