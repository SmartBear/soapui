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

package com.eviware.soapui.support.components;

import javax.swing.Action;
import javax.swing.JButton;

import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.swing.JXButtonPanel;
import com.jgoodies.forms.builder.ButtonBarBuilder;

public class JButtonBar extends JXButtonPanel
{
	private ButtonBarBuilder builder;
	private JButton defaultButton;

	public JButtonBar()
	{
		builder = new ButtonBarBuilder( this );
	}

	public void addActions( ActionList actions )
	{
		for( int c = 0; c < actions.getActionCount(); c++ )
		{
			Action action = actions.getActionAt( c );

			if( !( action instanceof HelpActionMarker ) && c == 0 )
			{
				if( getComponentCount() == 0 )
					builder.addGlue();
				else
					builder.addUnrelatedGap();
			}

			if( action == ActionSupport.SEPARATOR_ACTION )
			{
				builder.addUnrelatedGap();
			}
			else
			{
				if( c > 0 )
					builder.addRelatedGap();

				JButton button = new JButton( action );
				if( c == 0 || actions.getDefaultAction() == action )
					defaultButton = button;

				if( action.getValue( Action.SMALL_ICON ) != null )
					button.setText( null );

				builder.addFixed( button );
			}

			if( action instanceof HelpActionMarker && c == 0 )
				builder.addGlue();
		}
	}

	public JButton getDefaultButton()
	{
		return defaultButton;
	}
}
