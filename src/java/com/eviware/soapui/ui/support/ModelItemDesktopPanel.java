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

package com.eviware.soapui.ui.support;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * Base class for DesktopPanels..
 */

public abstract class ModelItemDesktopPanel<T extends ModelItem> extends JPanel implements DesktopPanel,
		PropertyChangeListener
{
	private final T modelItem;

	public ModelItemDesktopPanel( T modelItem )
	{
		super( new BorderLayout() );
		this.modelItem = modelItem;

		modelItem.addPropertyChangeListener( this );
	}

	protected boolean release()
	{
		modelItem.removePropertyChangeListener( this );
		return true;
	}

	public JComponent getComponent()
	{
		return this;
	}

	final public T getModelItem()
	{
		return modelItem;
	}

	public Icon getIcon()
	{
		return modelItem.getIcon();
	}

	abstract public boolean dependsOn( ModelItem modelItem );

	public String getTitle()
	{
		return modelItem.getName();
	}

	public final String getDescription()
	{
		TreePath treePath = SoapUI.getNavigator().getTreePath( modelItem );

		if( treePath == null )
		{
			return modelItem.getDescription();
		}
		else
		{
			String str = modelItem.getName() + " [";

			for( int c = 1; c < treePath.getPathCount(); c++ )
			{
				SoapUITreeNode comp = ( SoapUITreeNode )treePath.getPathComponent( c );
				if( comp.getModelItem() instanceof EmptyModelItem )
					continue;

				if( c > 1 )
					str += "/";

				str += comp.toString();
			}

			str += "]";

			return str;
		}
	}

	public static JButton createActionButton( Action action, boolean enabled )
	{
		JButton button = UISupport.createToolbarButton( action, enabled );
		action.putValue( Action.NAME, null );
		return button;
	}

	public void notifyPropertyChange( String propertyName, Object oldValue, Object newValue )
	{
		firePropertyChange( propertyName, oldValue, newValue );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( ModelItem.NAME_PROPERTY ) )
			notifyPropertyChange( DesktopPanel.TITLE_PROPERTY, null, getTitle() );

		if( evt.getPropertyName().equals( ModelItem.ICON_PROPERTY ) )
			notifyPropertyChange( DesktopPanel.ICON_PROPERTY, null, getIcon() );
	}
}
