/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.rest.panels.component;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import org.apache.commons.lang.mutable.MutableBoolean;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Text field for editing a rest resource. Pops up a separate dialog to edit parts of the resource separately if the
 * rest resource has parents or children.
 */
public class RestResourceEditor extends JTextField
{
	MouseListener mouseListener;

	private RestResource editingRestResource;
	private MutableBoolean updating;
	private int lastSelectedPosition;

	public RestResourceEditor( final RestResource editingRestResource, MutableBoolean updating )
	{
		super( editingRestResource.getFullPath() );
		this.editingRestResource = editingRestResource;
		this.updating = updating;

		if( isResourceLonely( editingRestResource ) )
		{
			getDocument().addDocumentListener( new LonelyDocumentListener() );
			addFocusListener( new FocusListener()
			{
				public void focusLost( FocusEvent e )
				{
					scanForTemplateParameters(editingRestResource);
				}

				public void focusGained( FocusEvent e )
				{
				}
			} );

		}
		else
		{
			Color originalBackground = getBackground();
			Border originalBorder = getBorder();
			setEditable( false );
			setBackground( originalBackground );
			setBorder( originalBorder );
			setCursor( Cursor.getPredefinedCursor( Cursor.TEXT_CURSOR ) );
			mouseListener = new MouseAdapter()
			{
				@Override
				public void mouseClicked( MouseEvent e )
				{
					final RestResource focusedResource = new RestResourceFinder( editingRestResource ).findResourceAt( lastSelectedPosition );
					SwingUtilities.invokeLater( new Runnable()
					{
						@Override
						public void run()
						{
							openPopup( focusedResource );
						}
					} );
				}
			};
			addMouseListener( mouseListener );
			addCaretListener( new CaretListener()
			{
				@Override
				public void caretUpdate( final CaretEvent e )
				{
					lastSelectedPosition = e.getDot();
				}

			} );
		}
	}

	static void scanForTemplateParameters(RestResource resource)
	{
		for( RestResource restResource : RestUtils.extractAncestorsParentFirst( resource ) )
		{
			for( String p : RestUtils.extractTemplateParams( restResource.getPath() ) )
			{
				if( !resourceOrParentHasProperty( restResource, p ) )
				{
					RestParamProperty property = restResource.addProperty( p );
					property.setStyle( RestParamsPropertyHolder.ParameterStyle.TEMPLATE );
					String value = UISupport.prompt( "Specify default value for parameter [" + p + "]",
							"Add Parameter", "" );
					if( value != null )
					{
						property.setDefaultValue( value );
						property.setValue( value );
					}
				}
			}
		}
	}

	private static boolean resourceOrParentHasProperty( RestResource restResource, String name )
	{
		for( RestResource r = restResource; r != null; r = r.getParentResource() )
		{
			if( r.hasProperty( name ) )
			{
				return true;
			}
		}
		return false;
	}

	private boolean isResourceLonely( RestResource restResource )
	{
		return restResource.getParentResource() == null && StringUtils.isNullOrEmpty( restResource.getInterface().getBasePath() );


	}

	public void openPopup( RestResource focusedResource )
	{
		RestResourceEditorPopupWindow popupWindow = new RestResourceEditorPopupWindow( editingRestResource, focusedResource );
		moveWindowBelowTextField( popupWindow );
		popupWindow.setVisible( true );
	}

	private class LonelyDocumentListener extends DocumentListenerAdapter
	{
		@Override
		public void update( Document document )
		{
			if( updating.booleanValue() )
			{
				return;
			}
			updating.setValue( true );
			editingRestResource.setPath( getText( document ).trim() );
			updating.setValue( false );
		}
	}

	private void moveWindowBelowTextField( RestResourceEditorPopupWindow popupWindow )
	{
		try
		{
			Point textFieldLocation = this.getLocationOnScreen();
			popupWindow.setLocation( textFieldLocation.x, textFieldLocation.y + this.getHeight() );
		}
		catch( IllegalComponentStateException ignore )
		{
			// this will happen when the desktop panel is being closed
		}
	}


}
