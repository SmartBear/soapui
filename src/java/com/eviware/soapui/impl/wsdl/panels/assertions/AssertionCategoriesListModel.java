/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.panels.assertions;

import java.util.Set;

import javax.swing.DefaultListModel;

public class AssertionCategoriesListModel extends DefaultListModel
{
	Set<String> listEntriesSet;

	public AssertionCategoriesListModel( Set<String> keySet )
	{
		listEntriesSet = keySet;
	}

	@Override
	public int getSize()
	{
		return listEntriesSet.size();
	}

	@Override
	public Object getElementAt( int index )
	{
		return listEntriesSet.toArray()[index];
	}
}
