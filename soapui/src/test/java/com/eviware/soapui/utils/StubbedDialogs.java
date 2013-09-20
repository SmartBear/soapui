package com.eviware.soapui.utils;

import com.eviware.soapui.support.NullProgressDialog;
import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XProgressDialog;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A stub of the Dialogs class, to be used in unit testing of GUIs.
 */
public class StubbedDialogs implements XDialogs
{

	private List<String> errorMessages = new ArrayList<String>();
	private List<String> infoMessages = new ArrayList<String>();

	@Override
	public void showErrorMessage( String message )
	{
		errorMessages.add( message );
	}

	@Override
	public void showInfoMessage( String message )
	{
		infoMessages.add( message );
	}

	@Override
	public void showInfoMessage( String message, String title )
	{
		infoMessages.add( message );
	}

	@Override
	public void showExtendedInfo( String title, String description, String content, Dimension size )
	{

	}

	@Override
	public boolean confirm( String question, String title )
	{
		return false;
	}

	@Override
	public Boolean confirmOrCancel( String question, String title )
	{
		return null;
	}

	@Override
	public int yesYesToAllOrNo( String question, String title )
	{
		return 0;
	}

	@Override
	public String prompt( String question, String title, String value )
	{
		return null;
	}

	@Override
	public String prompt( String question, String title )
	{
		return null;
	}

	@Override
	public Object prompt( String question, String title, Object[] objects )
	{
		return null;
	}

	@Override
	public Object prompt( String question, String title, Object[] objects, String value )
	{
		return null;
	}

	@Override
	public char[] promptPassword( String question, String title )
	{
		return new char[0];
	}

	@Override
	public XProgressDialog createProgressDialog( String label, int length, String initialValue, boolean canCancel )
	{
		return new NullProgressDialog();
	}

	@Override
	public boolean confirmExtendedInfo( String title, String description, String content, Dimension size )
	{
		return false;
	}

	@Override
	public Boolean confirmOrCancleExtendedInfo( String title, String description, String content, Dimension size )
	{
		return null;
	}

	@Override
	public String selectXPath( String title, String info, String xml, String xpath )
	{
		return null;
	}

	public List<String> getErrorMessages()
	{
		return errorMessages;
	}

	public List<String> getInfoMessages()
	{
		return infoMessages;
	}
}
