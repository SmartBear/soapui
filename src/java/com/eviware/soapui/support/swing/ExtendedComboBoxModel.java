/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.swing;

import java.io.Serializable;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

public class ExtendedComboBoxModel extends AbstractListModel implements MutableComboBoxModel, Serializable
{
	Vector objects;
	Object selectedObject;

	/**
	 * Constructs an empty DefaultComboBoxModel object.
	 */
	public ExtendedComboBoxModel()
	{
		objects = new Vector();
	}

	/**
	 * Constructs a DefaultComboBoxModel object initialized with an array of
	 * objects.
	 * 
	 * @param items
	 *           an array of Object objects
	 */
	public ExtendedComboBoxModel( final Object items[] )
	{
		objects = new Vector();
		objects.ensureCapacity( items.length );

		int i, c;
		for( i = 0, c = items.length; i < c; i++ )
			objects.addElement( items[i] );

		if( getSize() > 0 )
		{
			selectedObject = getElementAt( 0 );
		}
	}

	/**
	 * Constructs a DefaultComboBoxModel object initialized with a vector.
	 * 
	 * @param v
	 *           a Vector object ...
	 */
	public ExtendedComboBoxModel( Vector<?> v )
	{
		objects = v;

		if( getSize() > 0 )
		{
			selectedObject = getElementAt( 0 );
		}
	}

	// implements javax.swing.ComboBoxModel
	/**
	 * Set the value of the selected item. The selected item may be null.
	 * <p>
	 * 
	 * @param anObject
	 *           The combo box value or null for no selection.
	 */
	public void setSelectedItem( Object anObject )
	{
		if( ( selectedObject != null && !selectedObject.equals( anObject ) ) || selectedObject == null
				&& anObject != null )
		{
			selectedObject = anObject;
			fireContentsChanged( this, -1, -1 );
		}
	}

	// implements javax.swing.ComboBoxModel
	public Object getSelectedItem()
	{
		return selectedObject;
	}

	// implements javax.swing.ListModel
	public int getSize()
	{
		return objects.size();
	}

	// implements javax.swing.ListModel
	public Object getElementAt( int index )
	{
		if( index >= 0 && index < objects.size() )
			return objects.elementAt( index );
		else
			return null;
	}

	/**
	 * Returns the index-position of the specified object in the list.
	 * 
	 * @param anObject
	 * @return an int representing the index position, where 0 is the first
	 *         position
	 */
	public int getIndexOf( Object anObject )
	{
		return objects.indexOf( anObject );
	}

	// implements javax.swing.MutableComboBoxModel
	public void addElement( Object anObject )
	{
		objects.addElement( anObject );
		fireIntervalAdded( this, objects.size() - 1, objects.size() - 1 );
		if( objects.size() == 1 && selectedObject == null && anObject != null )
		{
			setSelectedItem( anObject );
		}
	}

	// implements javax.swing.MutableComboBoxModel
	public void insertElementAt( Object anObject, int index )
	{
		objects.insertElementAt( anObject, index );
		fireIntervalAdded( this, index, index );
	}

	// implements javax.swing.MutableComboBoxModel
	public void removeElementAt( int index )
	{
		if( getElementAt( index ) == selectedObject )
		{
			if( index == 0 )
			{
				setSelectedItem( getSize() == 1 ? null : getElementAt( index + 1 ) );
			}
			else
			{
				setSelectedItem( getElementAt( index - 1 ) );
			}
		}

		objects.removeElementAt( index );

		fireIntervalRemoved( this, index, index );
	}

	// implements javax.swing.MutableComboBoxModel
	public void removeElement( Object anObject )
	{
		int index = objects.indexOf( anObject );
		if( index != -1 )
		{
			removeElementAt( index );
		}
	}

	/**
	 * Empties the list.
	 */
	public void removeAllElements()
	{
		if( objects.size() > 0 )
		{
			int firstIndex = 0;
			int lastIndex = objects.size() - 1;
			objects.removeAllElements();
			selectedObject = null;
			fireIntervalRemoved( this, firstIndex, lastIndex );
		}
		else
		{
			selectedObject = null;
		}
	}

	public void setElementAt( Object obj, int index )
	{
		objects.setElementAt( obj, index );
		fireContentsChanged( this, index, index );
	}
}
