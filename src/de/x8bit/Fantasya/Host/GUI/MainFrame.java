package de.x8bit.Fantasya.Host.GUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Host.Datenbank;

/**
 * @author  mogel
 */
public class MainFrame extends JFrame implements WindowListener, MouseWheelListener, MouseListener, ComponentListener
{
	static final long serialVersionUID = 0;

	private TerrainImages terrainPics = new TerrainImages();
	
	/** behelfs Variable für das zeichnen */
	private boolean paintcount = false;
	
	public MainFrame()
	{
		super("Fantasya 2 - Das nächste Zeithalter");
		Datenbank db = new Datenbank("MainFrame-Init");
		
		// Schließen
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		addWindowListener(this);
		
		// Größe einlesen
	    setSize(db.ReadSettings("gui.mainframe.size.x", 800), db.ReadSettings("gui.mainframe.size.y", 600));
	    setLocation(db.ReadSettings("gui.mainframe.location.x", 0), db.ReadSettings("gui.mainframe.location.y", 0));
	    
	    // die Weltübersicht
	    //add(new MainPanel());
	    
	    // und anzeigen
	    setVisible( true );
	    
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
	    //regionframe.UpdateRegion(null);

	    // Regionen in den Proxy speichern
		RecalcRegions();	
		
		// Regionen freischalten & malen
		paintcount = true;
		this.repaint();
	}
	
	@Override
	public void windowClosing(WindowEvent e)
	{
		Datenbank db = new Datenbank("MainFrame - SaveSettings");
		db.SaveSettings("gui.mainframe.size.x", this.getSize().width);
		db.SaveSettings("gui.mainframe.size.y", this.getSize().height);
		db.SaveSettings("gui.mainframe.location.x", this.getLocation().x);
		db.SaveSettings("gui.mainframe.location.y", this.getLocation().y);
		db.Close();
	}

	/** nicht benötigt */
	@Override
	public void windowActivated(WindowEvent e) { this.repaint(); }
	/** nicht benötigt */
	@Override
	public void windowClosed(WindowEvent e) { }
	/** nicht benötigt */
	@Override
	public void windowDeactivated(WindowEvent e) { }
	/** nicht benötigt */
	@Override
	public void windowDeiconified(WindowEvent e) { }
	/** nicht benötigt */
	@Override
	public void windowIconified(WindowEvent e) { }
	/** nicht benötigt */
	@Override
	public void windowOpened(WindowEvent e) { }
	
	BufferedImage img_cursor = null;
	BufferedImage img_unit = null;
	BufferedImage img_ship = null;
	BufferedImage img_building = null;
	
	/**
	 * @uml.property  name="regionframe"
	 * @uml.associationEnd  
	 */
	private RegionFrame regionframe = null;
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		//  Zoom einbauen
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{ 
		int x, y;
		x = (e.getPoint().x / ImageSize.x) + minx;
		y = maxy - (e.getPoint().y / ImageSize.y);
		
		Cursor = new Point(x,y);
		RecalcRegions();
		repaint();
	}

	/**
	 * die Größe der Regionsbilder
	 */
	private Point ImageSize = new Point(64,64);
	
	/**
	 * der Cursor stellt die ausgewählte Region dar
	 */
	private Point Cursor = new Point(0,0);
	
	/** diese Welt wird angezeigt */
	private int Welt = 1;
	
	/** nicht benötigt */
	@Override
	public void mousePressed(MouseEvent e) { }
	/** nicht benötigt */
	@Override
	public void mouseClicked(MouseEvent e) { }
	/** nicht benötigt */
	@Override
	public void mouseEntered(MouseEvent e) { }
	/** nicht benötigt */
	@Override
	public void mouseExited(MouseEvent e) { }
	
	@Override
	public void componentHidden(ComponentEvent e) { }
	@Override
	public void componentMoved(ComponentEvent e)  { }
	@Override
	public void componentResized(ComponentEvent e) { RecalcRegions(); this.repaint(); }
	@Override
	public void componentShown(ComponentEvent e) { RecalcRegions(); this.repaint(); }
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		// Rest nur nach Initialisierung
		if (paintcount == false) return;
		
		// noch nix gefunden ^^
		if (proxy.size() < 1) return;
		
		int x, y;	// Koordinaten für das Bild
		
		// Regionen malen
		for(int i = 0; i < proxy.size(); i++)
		{
			// Koordinaten auf der Oberfläche berechnen
			Region r = proxy.get(i);
			x = (r.getCoords().getX() - minx) * ImageSize.x;
			y = (r.getCoords().getY() - miny) * ImageSize.y;
			
			// an X verschieben
			//x -= ((maxy - miny) / 2) - (r.getY() - miny) * ImageSize.x / 2;
			
			// an X spiegeln
			y = g.getClipBounds().height - y;
			
			// das Icon malen
			g.drawImage(terrainPics.getImage(r), x, y, ImageSize.x, ImageSize.y, null);
			
			if (Cursor.x == r.getCoords().getX() && Cursor.y == r.getCoords().getY())
			{
				regionframe.UpdateRegion(r);
				g.drawImage(img_cursor, x, y, ImageSize.x, ImageSize.y, null);
			}
			
			if (r.istBetretbar(null)) 
			{
				DrawShadowed(g, x, y + 10, r.getName());
				if ((r.getCoords().getX() % 2) == 0 && (r.getCoords().getY() % 2) == 0)
				{
					DrawShadowed(g, x, y + 22, "(" + r.getCoords().getX() + ", " + r.getCoords().getY() + ")");
				}
			}
		}
	}

	/**
	 * zeichent einen weißen Text mit schwarzem Schatten
	 * @param g - Graphics
	 * @param x - Position
	 * @param y - Position
	 * @param text - der Text
	 */
	private void DrawShadowed(Graphics g, int x, int y, String text)
	{
		g.setColor(new Color(0, 0, 0));
		g.drawString(text, x + 1, y + 1);
		g.setColor(new Color(255, 255, 255));
		g.drawString(text, x, y);
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
