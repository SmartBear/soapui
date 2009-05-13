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

package com.eviware.soapui.support.action.swing;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.SoapUIMultiAction;

/**
 * Delegates a SwingAction to a SoapUIActionMapping containgin a
 * SoapUIMultiAction
 * 
 * @author ole.matzura
 */

public class SwingMultiActionDelegate extends AbstractAction implements PropertyChangeListener, SoapUIActionMarker
{
	private final SoapUIActionMapping<?> mapping;
	private ModelItem[] targets;

	public SwingMultiActionDelegate( SoapUIActionMapping<?> mapping, ModelItem[] targets )
	{
		super( mapping.getName() );
		this.mapping = mapping;
		this.targets = targets;

		if( mapping.getDescription() != null )
			putValue( Action.SHORT_DESCRIPTION, mapping.getDescription() );

		if( mapping.getIconPath() != null )
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( mapping.getIconPath() ) );

		if( mapping.getKeyStroke() != null )
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( mapping.getKeyStroke() ) );

		setEnabled( mapping.getAction().isEnabled() );

		String name = mapping.getName();
		int ix = name.indexOf( '&' );
		if( ix >= 0 )
		{
			putValue( Action.NAME, name.substring( 0, ix ) + name.substring( ix + 1 ) );
			// This doesn't seem to work in Java 5:
			// putValue( Action.DISPLAYED_MNEMONIC_INDEX_KEY, new Integer( ix ));
			putValue( Action.MNEMONIC_KEY, new Integer( name.charAt( ix + 1 ) ) );
		}
	}

	public SoapUIActionMapping<?> getMapping()
	{
		return mapping;
	}

	public void actionPerformed( ActionEvent e )
	{
		// required by IDE plugins
		if( SwingActionDelegate.switchClassloader )
		{
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader( SoapUI.class.getClassLoader() );

			try
			{
				( ( SoapUIMultiAction )mapping.getAction() ).perform( targets, mapping.getParam() );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
			finally
			{
				Thread.currentThread().setContextClassLoader( contextClassLoader );
			}
		}
		else
		{
			try
			{
				( ( SoapUIMultiAction )mapping.getAction() ).perform( targets, mapping.getParam() );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( SoapUIAction.ENABLED_PROPERTY ) )
			setEnabled( ( ( Boolean )evt.getNewValue() ).booleanValue() );
	}

	public ModelItem[] getTargets()
	{
		return targets;
	}

	public SoapUIAction<?> getSoapUIAction()
	{
		return mapping.getAction();
	}
}