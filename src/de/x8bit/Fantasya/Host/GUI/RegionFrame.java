package de.x8bit.Fantasya.Host.GUI;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Host.Datenbank;

/**
 * @author  mogel
 */
public class RegionFrame extends JFrame implements WindowListener
{
	static final long serialVersionUID = 0;
	
	/** der Baum zur Darstellung */
	private JTree myTree;
	/**
	 * @uml.property  name="myTreeModel"
	 * @uml.associationEnd  
	 */
	private RegionTreeModel myTreeModel;
	
	public RegionFrame()
	{
		super("Region");
		Datenbank db = new Datenbank("RegionFrame-Init");
		
		// Schließen
		setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
		addWindowListener(this);
		
		// Größe einlesen
	    setSize(db.ReadSettings("gui.regionframe.size.x", 200), db.ReadSettings("gui.regionframe.size.y", 600));
	    setLocation(db.ReadSettings("gui.regionframe.location.x", 800), db.ReadSettings("gui.regionframe.location.y", 0));
	    
		myTreeModel = new RegionTreeModel();
		myTree = new JTree(myTreeModel);
	    add(new JScrollPane(myTree));
	    
	    db.Close();
	}
	
	public void windowClosing(WindowEvent e)
	{
		Datenbank db = new Datenbank("RegionFrame - SaveSettings");
		db.SaveSettings("gui.regionframe.size.x", this.getSize().width);
		db.SaveSettings("gui.regionframe.size.y", this.getSize().height);
		db.SaveSettings("gui.regionframe.location.x", this.getLocation().x);
		db.SaveSettings("gui.regionframe.location.y", this.getLocation().y);
		db.Close();
	}

	/** nicht benötigt */
	public void windowActivated(WindowEvent e) { }
	/** nicht benötigt */
	public void windowClosed(WindowEvent e) { }
	/** nicht benötigt */
	public void windowDeactivated(WindowEvent e) { }
	/** nicht benötigt */
	public void windowDeiconified(WindowEvent e) { }
	/** nicht benötigt */
	public void windowIconified(WindowEvent e) { }
	/** nicht benötigt */
	public void windowOpened(WindowEvent e) { }
	
	public void UpdateRegion(Region r)
	{
		if (r == null) return;
		if (!this.isShowing()) this.setVisible(true);
		
		setTitle(r.getName() + " [" + r.getCoords().getX() + "/" + r.getCoords().getY() + "/" + r.getCoords().getWelt() + "]");
		
		ChangeRegionData((TreeNode) myTreeModel.getRoot(), r);
		ChangeEinheitData((TreeNode) myTreeModel.getRoot(), r);
		ChangeGebaeudeData((TreeNode) myTreeModel.getRoot(), r);
		ChangeSchiffData((TreeNode) myTreeModel.getRoot(), r);
	}
	
	/** Knoten für die Regionsinfos */
	private DefaultMutableTreeNode node_region = null;
	/** Knoten für die Einheiten */
	private DefaultMutableTreeNode node_einheiten = null;
	/** Knoten für die Schiffe */
	private DefaultMutableTreeNode node_schiffe = null;
	/** Knoten für die Gebäude */
	private DefaultMutableTreeNode node_gebaeude = null;
	
	/**
	 * Regionsinfos hinzufügen (Bauern / Bäume / ...)
	 * @param root - Wurzel des Baumes
	 * @param r - Region für die Infos
	 */
	private void ChangeRegionData(TreeNode root, Region r)
	{
		// alten Infos entfernen
		if (node_region != null) myTreeModel.remove(new TreePath(new Object[] { myTreeModel.getRoot(), node_region } ));
		
		// neuen Infos erstellen
		node_region = new DefaultMutableTreeNode(r.getName() + " (" + r.getClass().getSimpleName() + ")");
		node_region.add(new DefaultMutableTreeNode("Koordinaten [" + r.getCoords().getX() + "/" + r.getCoords().getY() + "/" + r.getCoords().getWelt() + "]"));
		node_region.add(new DefaultMutableTreeNode(r.getBauern() + " Bauern"));
		//node_region.add(new DefaultMutableTreeNode(r.getBaum() + " Bäume"));
		
		// und alles dem Baum übergeben
		myTreeModel.add(new TreePath(myTreeModel.getRoot()), node_region);
		myTree.expandPath(new TreePath(new Object[] { myTreeModel.getRoot(), node_region } ));
	}
	
	/**
	 * Einheiteninfos hinzufügen ( Völker -> Einheiten -> Items )
	 * @param root - Wurzel des Baumes
	 * @param r - Region für die Infos
	 */
	private void ChangeEinheitData(TreeNode root, Region r)
	{
		// alten Infos entfernen
		if (node_einheiten != null) myTreeModel.remove(new TreePath(new Object[] { myTreeModel.getRoot(), node_einheiten } ));

		// neuen Infos erstellen
		node_einheiten = new DefaultMutableTreeNode("232 Einheiten");
		
		// alle Völker in dieser Region, dann die Einheiten
		
		// und alles dem Baum übergeben
		myTreeModel.add(new TreePath(myTreeModel.getRoot()), node_einheiten);
		myTree.expandPath(new TreePath(new Object[] { myTreeModel.getRoot(), node_einheiten } ));
	}
	
	/**
	 * Gebäudeinfos hinzufügen
	 * @param root - Wurzel des Baumes
	 * @param r - Region für die Infos
	 */
	private void ChangeGebaeudeData(TreeNode root, Region r)
	{
		// alten Infos entfernen
		if (node_gebaeude != null) myTreeModel.remove(new TreePath(new Object[] { myTreeModel.getRoot(), node_gebaeude } ));

		// neuen Infos erstellen
		node_gebaeude = new DefaultMutableTreeNode("Gebäude");
		
		// alle Völker in dieser Region, dann die Einheiten
		
		// und alles dem Baum übergeben
		myTreeModel.add(new TreePath(myTreeModel.getRoot()), node_gebaeude);
		myTree.expandPath(new TreePath(new Object[] { myTreeModel.getRoot(), node_gebaeude } ));
	}

	/**
	 * Schiffinfos hinzufügen
	 * @param root - Wurzel des Baumes
	 * @param r - Region für die Infos
	 */
	private void ChangeSchiffData(TreeNode root, Region r)
	{
		// alten Infos entfernen
		if (node_schiffe != null) myTreeModel.remove(new TreePath(new Object[] { myTreeModel.getRoot(), node_schiffe } ));

		// neuen Infos erstellen
		node_schiffe = new DefaultMutableTreeNode("Schiffe");
		
		// alle Völker in dieser Region, dann die Einheiten
		
		// und alles dem Baum übergeben
		myTreeModel.add(new TreePath(myTreeModel.getRoot()), node_schiffe);
		myTree.expandPath(new TreePath(new Object[] { myTreeModel.getRoot(), node_schiffe } ));
	}
}
