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

package com.eviware.soapui.support;

import java.awt.Dimension;

import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XProgressDialog;

public class ConsoleDialogs implements XDialogs
{
	public boolean confirm( String question, String title )
	{
		return false;
	}

	public Boolean confirmOrCancel( String question, String title )
	{
		return null;
	}

	public String prompt( String question, String title, String value )
	{
		return null;
	}

	public String prompt( String question, String title )
	{
		return null;
	}

	public String prompt( String question, String title, Object[] objects )
	{
		return null;
	}

	public String prompt( String question, String title, Object[] objects, String value )
	{
		return null;
	}

	public void showErrorMessage( String message )
	{
		System.err.println( message );
	}

	public void showInfoMessage( String message )
	{
		System.out.println( message );
	}

	public void showInfoMessage( String message, String title )
	{
		System.out.println( title + ": " + message );
	}

	public XProgressDialog createProgressDialog( String label, int length, String initialValue, boolean canCancel )
	{
		return new NullProgressDialog();
	}

	public void showExtendedInfo( String title, String description, String content, Dimension size )
	{
	}

	public boolean confirmExtendedInfo( String title, String description, String content, Dimension size )
	{
		return false;
	}

	public Boolean confirmOrCancleExtendedInfo( String title, String description, String content, Dimension size )
	{
		return null;
	}

	public String selectXPath( String title, String info, String xml, String xpath )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public char[] promptPassword( String question, String title )
	{
		// TODO Auto-generated method stub
		return null;
	}
}
