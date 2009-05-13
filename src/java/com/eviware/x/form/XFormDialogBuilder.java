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

package com.eviware.x.form;

import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.eviware.soapui.support.action.swing.ActionList;

public abstract class XFormDialogBuilder
{
	private ArrayList<XForm> forms = new ArrayList<XForm>();

	public XFormDialogBuilder()
	{
	}

	protected void addForm( XForm form )
	{
		forms.add( form );
	}

	protected XForm[] getForms()
	{
		return forms.toArray( new XForm[forms.size()] );
	}

	public abstract XForm createForm( String name );

	public abstract XFormDialog buildDialog( ActionList actions, String description, ImageIcon icon );

	public abstract XFormDialog buildWizard( String description, ImageIcon icon, String helpURL );

	public abstract ActionList buildOkCancelActions();

	public abstract ActionList buildOkCancelHelpActions( String url );
}
