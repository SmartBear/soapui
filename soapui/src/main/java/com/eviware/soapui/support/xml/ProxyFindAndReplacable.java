/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.xml;

import javax.swing.JComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.eviware.soapui.support.actions.FindAndReplaceable;

public class ProxyFindAndReplacable implements FindAndReplaceable
{
	protected FindAndReplaceable proxytarget;
	protected boolean isReplaceAll = false;
	protected StringBuilder sbtartget;
	protected String oldValue;
	protected String newValue;
	protected int start;
	protected int end;

	public ProxyFindAndReplacable( FindAndReplaceable proxytarget )
	{
		this.proxytarget = proxytarget;
	}

	public void setSBTarget()
	{
		this.sbtartget = new StringBuilder();
		this.sbtartget.append( proxytarget.getText() );
	}

	public FindAndReplaceable getProxytarget()
	{
		return proxytarget;
	}

	public int getCaretPosition()
	{
		return proxytarget.getCaretPosition();
	}

	public String getSelectedText()
	{
		return proxytarget.getSelectedText();
	}

	public int getSelectionEnd()
	{
		return proxytarget.getSelectionEnd();
	}

	public int getSelectionStart()
	{
		return proxytarget.getSelectionStart();
	}

	public String getText()
	{
		if( isReplaceAll )
		{
			return sbtartget.toString();
		}
		else
			return proxytarget.getText();

	}

	public String getDialogText()
	{
		return proxytarget.getText();
	}

	public boolean isEditable()
	{
		return proxytarget.isEditable();
	}

	public void select( int start, int end )
	{
		if( isReplaceAll )
		{
			this.start = start;
			this.end = end;
		}
		else
			proxytarget.select( start, end );

	}

	public void setSelectedText( String txt )
	{
		if( isReplaceAll )
		{
			sbtartget.replace( this.start, this.end, newValue );
		}
		else
			proxytarget.setSelectedText( txt );

	}

	public boolean isReplaceAll()
	{
		return isReplaceAll;
	}

	public void setReplaceAll( boolean isReplaceAll )
	{
		if( proxytarget instanceof RSyntaxTextArea )
		{
			this.isReplaceAll = isReplaceAll;
		}
		else
		{
			this.isReplaceAll = false;
		}
	}

	public String getOldValue()
	{
		return oldValue;
	}

	public void setOldValue( String oldValue )
	{
		this.oldValue = oldValue;
	}

	public String getNewValue()
	{
		return newValue;
	}

	public void setNewValue( String newValue )
	{
		this.newValue = newValue;
	}

	public void flushSBText()
	{
		if( proxytarget instanceof RSyntaxTextArea )
		{
			( ( RSyntaxTextArea )proxytarget ).setText( sbtartget.toString() );
		}

	}

	public void setCarretPosition( boolean forward )
	{
		if( proxytarget instanceof RSyntaxTextArea )
		{
			( ( RSyntaxTextArea )proxytarget ).setCaretPosition( forward ? getEnd() : getStart() );
		}
	}

	public int getStart()
	{
		return start;
	}

	public int getEnd()
	{
		return end;
	}

	public JComponent getEditComponent()
	{
		return proxytarget.getEditComponent();
	}
}
