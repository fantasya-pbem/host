package de.x8bit.Fantasya.Host.GUI;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;

/**
 * das Fenster der Fenster ... hier wird die Welt grafisch Dargestellt
 * @author  mogel
 */
public class MainPanel extends JPanel implements MouseWheelListener, MouseListener, ComponentListener
{
	static final long serialVersionUID = 0;
	
	BufferedImage img_cursor = null;
	BufferedImage img_unit = null;
	BufferedImage img_ship = null;
	BufferedImage img_building = null;

	TerrainImages terrainPics = new TerrainImages();
	
	/**
	 * @uml.property  name="regionframe"
	 * @uml.associationEnd  
	 */
	private RegionFrame regionframe = null;
	
	public MainPanel()
	{
		super();
		addMouseWheelListener(this);
		addMouseListener(this);
		addComponentListener(this);
		
		// Größe der Bilder ermittlen
		BufferedImage img = terrainPics.getImage(new Chaos());
		ImageSize = new Point(img.getWidth(), img.getHeight());
		
		try
		{
			img_cursor = ImageIO.read(new File("gfx/details/rahmen.png"));
			img_unit = ImageIO.read(new File("gfx/details/einheit.png"));
			img_ship = ImageIO.read(new File("gfx/details/schiff.png"));
			img_building = ImageIO.read(new File("gfx/details/burg.png"));
		} catch (IOException e)
		{
			new BigError(e);
		}
		
	    regionframe = new RegionFrame();
	    regionframe.UpdateRegion(null);

	    // Regionen in den Proxy speichern
		RecalcRegions();	
		
		InitPopupMenu();
	}
	
	private JPopupMenu contextmenu;
	
	private void InitPopupMenu()
	{
		contextmenu = new JPopupMenu();
		
		contextmenu.add(new JMenuItem("Einheit erstellen"));
		contextmenu.add(new JMenuItem("Gebäude erstellen"));
		contextmenu.add(new JMenuItem("Schiff erstellen"));
		
		// Regionengenerator
		contextmenu.addSeparator();
		contextmenu.add(new JMenuItem("Region-Seed"));
		contextmenu.add(new JMenuItem("Region editieren"));

		// Abbrechen
		contextmenu.addSeparator();
		contextmenu.add(new JMenuItem("vergiß es ..."));
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		// Zoom einbauen
	}

	public void mouseReleased(MouseEvent e) 
	{ 
		int x, y;
		x = (e.getPoint().x / ImageSize.x) + minx;
		y = (e.getPoint().y / ImageSize.y) + miny;
		if (Cursor.x == x && Cursor.y == y)
		{
			contextmenu.show(e.getComponent(), e.getX(), e.getY());
		} else
		{
			Cursor = new Point(x,y);
			RecalcRegions();
			repaint();
		}
	}

	/**
	 * die Größe der Regionsbilder
	 */
	private Point ImageSize = new Point(0,0);
	
	/**
	 * der Cursor stellt die ausgewählte Region dar
	 */
	private Point Cursor = new Point(0,0);
	
	/** diese Welt wird angezeigt */
	private int Welt = 0;
	
	/** nicht benötigt */
	public void mousePressed(MouseEvent e) { }
	/** nicht benötigt */
	public void mouseClicked(MouseEvent e) { }
	/** nicht benötigt */
	public void mouseEntered(MouseEvent e) { }
	/** nicht benötigt */
	public void mouseExited(MouseEvent e) { }
	
	public void componentHidden(ComponentEvent e) { }
	public void componentMoved(ComponentEvent e)  { }
	public void componentResized(ComponentEvent e) { RecalcRegions(); this.repaint(); }
	public void componentShown(ComponentEvent e) { RecalcRegions(); this.repaint(); }
	
	protected void paintComponent(Graphics g)
	{
		super.paintComponents(g);
		
		int x, y;	// Koordinaten für das Bild
		
		// noch nix gefunden ^^
		if (proxy.size() < 1) return;
		
		// Regionen malen
		for(int i = 0; i < proxy.size(); i++)
		{
			Region r = proxy.get(i);
			x = (r.getCoords().getX() - minx) * ImageSize.x;
			y = (r.getCoords().getY() - miny) * ImageSize.y;
			g.drawImage(terrainPics.getImage(r), x, y, ImageSize.x, ImageSize.y, null);
			
			if (Cursor.x == r.getCoords().getX() && Cursor.y == r.getCoords().getY())
			{
				regionframe.UpdateRegion(r);
				g.drawImage(img_cursor, x, y, ImageSize.x, ImageSize.y, null);
			}
		}
	}
	
	/** Proxxy der die entsprechenden Region zwischen speichert */
	private ArrayList<Region> proxy;
	
	/** Dimensionierung des sichtbaren Teiles der Welt */
	private int minx;

	/** Dimensionierung des sichtbaren Teiles der Welt */
	private int miny;

	/** Dimensionierung des sichtbaren Teiles der Welt */
	private int maxx;

	/** Dimensionierung des sichtbaren Teiles der Welt */
	private int maxy;

	/**
	 * Berechnung der sichtbaren Regionen auf Grund des Cursors und der Fenstergröße
	 */
	private void RecalcRegions()
	{
		int sizex, sizey;			// Größe in Regionen

		// Größe berechnen
		sizex = this.getWidth() / ImageSize.x;
		sizey = this.getHeight() / ImageSize.y;
		//System.out.println("Size -> " + sizex + " / " + sizey);
		
		// Min & Max Regionen berechnen
		minx = Cursor.x - sizex / 2;
		miny = Cursor.y - sizey / 2;
		maxx = minx + sizex;
		maxy = miny + sizey;
		//System.out.println("Min -> " + minx + " / " + miny);
		//System.out.println("Max -> " + maxx + " / " + maxy);
		
		// die alten Regionen "löschen"
		proxy = new ArrayList<Region>();
				
		// alle Regionen in den Proxy laden
		for(int x = minx; x <= maxx; x++)
		{
			for(int y = miny; y <= maxy; y++)
			{
				proxy.add(Region.Load(x, y, Welt));
			}
		}
	}
}
