package de.x8bit.Fantasya.Host.GUI;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * @author  mogel
 */
public class RegionTreeModel implements TreeModel
{
    /**
	 * @uml.property  name="root"
	 */
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Regionsinfos" );
    private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
   
    /**
	 * @return
	 * @uml.property  name="root"
	 */
    public Object getRoot() {
        return root;
    }

    public Object getChild( Object parent, int index ) {
        return ((TreeNode)parent).getChildAt( index );
    }

    public int getChildCount( Object parent ) {
        return ((TreeNode)parent).getChildCount();
    }

    public boolean isLeaf( Object node ) {
        return getChildCount( node ) == 0;
    }

    public int getIndexOfChild( Object parent, Object child ) {
        return ((TreeNode)parent).getIndex( (TreeNode)child );
    }

    public void addTreeModelListener( TreeModelListener listener ) {
        listeners.add( listener );
    }

    public void removeTreeModelListener( TreeModelListener listener ) {
        listeners.remove( listener );
    }
   
    // Fügt dem parent-Knoten (durch den TreePath gegeben) den
    // Child-Knoten hinzu
    public void add( TreePath parent, MutableTreeNode child ){
        // Den Knoten einbauen
        int index = getChildCount( parent.getLastPathComponent() );
        ((MutableTreeNode)parent.getLastPathComponent()).insert( child, index );
       
        // Die Listener unterrichten
        TreeModelEvent event = new TreeModelEvent(
                this,  // Quelle des Events
                parent, // Pfad zum Vater des veränderten Knoten
                new int[]{ index },  // Index des veränderten Knotens
                new Object[]{ child } ); // Der neue Knoten
       
        for( TreeModelListener listener : listeners )
            listener.treeNodesInserted( event );
    }

    // Entfernt den Knoten, der durch den TreePath angegeben ist.
    public void remove( TreePath path ){
        // Den Knoten entfernen
        Object parent = path.getParentPath().getLastPathComponent();
        Object child = path.getLastPathComponent();
       
        int index = getIndexOfChild( parent, child );
        ((MutableTreeNode)parent).remove( index );
       
        // Die Listener unterrichten
        TreeModelEvent event = new TreeModelEvent(
                this, // Quelle des Events
                path.getParentPath(),  // Pfad zum Vater des entfernten Knotens
                new int[]{index}, // Ehemaliger Index des Knotens
                new Object[]{child} ); // Der entfernte Knoten
       
        for( TreeModelListener listener : listeners )
            listener.treeNodesRemoved( event );
    }

    public void valueForPathChanged( TreePath path, Object newValue ) {
        // nicht beachten
    } 
 }
