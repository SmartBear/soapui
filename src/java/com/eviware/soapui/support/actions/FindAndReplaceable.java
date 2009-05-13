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

package com.eviware.soapui.support.actions;

/**
 * Behaviour for any component that supports Find-and-Replace
 * 
 * @author ole.matzura
 */

public interface FindAndReplaceable
{
	public boolean isEditable();

	public int getCaretPosition();

	public String getText();

	public void select( int start, int end );

	public int getSelectionStart();

	public int getSelectionEnd();

	public void setSelectedText( String txt );

	public String getSelectedText();
}
