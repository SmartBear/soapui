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

package com.eviware.soapui.impl.wsdl.actions.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JButtonBar;

public abstract class SimpleDialog extends JDialog
{
	protected JButtonBar buttons = null;

	public SimpleDialog( String title, String description, String helpUrl, boolean okAndCancel )
	{
		super( UISupport.getMainFrame(), title, true );

		buttons = UISupport.initDialogActions( buildActions( helpUrl, okAndCancel ), this );
		buttons.setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );

		getContentPane().add(
				UISupport.buildDescription( title, description, UISupport.createImageIcon( UISupport.TOOL_ICON_PATH ) ),
				BorderLayout.NORTH );

		getContentPane().add( buildContent(), BorderLayout.CENTER );

		buttons.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createCompoundBorder( BorderFactory
				.createMatteBorder( 1, 0, 0, 0, Color.GRAY ), BorderFactory.createMatteBorder( 1, 0, 0, 0, Color.WHITE ) ),
				BorderFactory.createEmptyBorder( 3, 5, 3, 5 ) ) );

		getContentPane().add( buttons, BorderLayout.SOUTH );
		modifyButtons();

		pack();
	}

	/*
	 * overide this to change buttons at bottom of dialog. I did not make it
	 * abstrac because it would require refactoring when SimpleDialog is used.
	 * Robert.
	 */
	protected void modifyButtons()
	{
	};

	public SimpleDialog( String title, String description, String helpUrl )
	{
		this( title, description, helpUrl, true );
	}

	protected abstract Component buildContent();

	public ActionList buildActions( String url, boolean okAndCancel )
	{
		DefaultActionList actions = new DefaultActionList( "Actions" );
		if( url != null )
			actions.addAction( new HelpAction( url ) );

		OKAction okAction = new OKAction();
		actions.addAction( okAction );
		if( okAndCancel )
		{
			actions.addAction( new CancelAction() );
			actions.setDefaultAction( okAction );
		}
		return actions;
	}

	protected abstract boolean handleOk();

	@Override
	public void setVisible( boolean b )
	{
		if( b )
			beforeShow();
		else
			beforeHide();

		UISupport.centerDialog( this );
		super.setVisible( b );

		if( b )
			afterShow();
		else
			afterHide();
	}

	protected void afterHide()
	{
	}

	protected void afterShow()
	{
	}

	protected void beforeHide()
	{
	}

	protected void beforeShow()
	{
	}

	protected boolean handleCancel()
	{
		return true;
	}

	protected final class OKAction extends AbstractAction
	{
		public OKAction()
		{
			super( "OK" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( handleOk() )
			{
				setVisible( false );
			}
		}
	}

	protected final class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super( "Cancel" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( handleCancel() )
			{
				setVisible( false );
			}
		}
	}

	public final class HelpAction extends AbstractAction implements HelpActionMarker
	{
		private final String url;

		public HelpAction( String url )
		{
			this( "Online Help", url, UISupport.getKeyStroke( "F1" ) );
		}

		public HelpAction( String title, String url )
		{
			this( title, url, null );
		}

		public HelpAction( String title, String url, KeyStroke accelerator )
		{
			super( title );
			this.url = url;
			putValue( Action.SHORT_DESCRIPTION, "Show online help" );
			if( accelerator != null )
				putValue( Action.ACCELERATOR_KEY, accelerator );

			putValue( Action.SMALL_ICON, UISupport.HELP_ICON );
		}

		public void actionPerformed( ActionEvent e )
		{
			Tools.openURL( url );
		}
	}
}
