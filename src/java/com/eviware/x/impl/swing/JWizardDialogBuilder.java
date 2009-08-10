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
package com.eviware.x.impl.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

public class JWizardDialogBuilder extends SwingXFormDialogBuilder
{

	private SwingXFormDialog dialog;

	public JWizardDialogBuilder( String name )
	{
		super( name );
	}

	protected final class NextAction extends AbstractAction
	{
		public NextAction()
		{
			super( "Next" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( dialog != null )
			{
				// dialog.setReturnValue( XFormDialog.NEXT_OPTION );
				dialog.setVisible( false );
			}
		}
	}

	public ActionList buildprevNextCancelActions()
	{
		DefaultActionList actions = new DefaultActionList( "Actions" );
		actions.addAction( new NextAction() );
		actions.addAction( new CancelAction() );
		return actions;
	}
}
