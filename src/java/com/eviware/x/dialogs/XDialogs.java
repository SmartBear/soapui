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

package com.eviware.x.dialogs;

import java.awt.Dimension;

/**
 * @author Lars
 */

public interface XDialogs
{
	void showErrorMessage( String message );

	void showInfoMessage( String message );

	void showInfoMessage( String message, String title );

	void showExtendedInfo( String title, String description, String content, Dimension size );

	boolean confirm( String question, String title );

	Boolean confirmOrCancel( String question, String title );

	int yesYesToAllOrNo( String question, String title );

	String prompt( String question, String title, String value );

	String prompt( String question, String title );

	Object prompt( String question, String title, Object[] objects );

	Object prompt( String question, String title, Object[] objects, String value );

	char[] promptPassword( String question, String title );

	XProgressDialog createProgressDialog( String label, int length, String initialValue, boolean canCancel );

	boolean confirmExtendedInfo( String title, String description, String content, Dimension size );

	Boolean confirmOrCancleExtendedInfo( String title, String description, String content, Dimension size );

	String selectXPath( String title, String info, String xml, String xpath );
}
