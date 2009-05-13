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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Adapter for DocumentListener implementations
 * 
 * @author Ole.Matzura
 */

public abstract class DocumentListenerAdapter implements DocumentListener
{
	public DocumentListenerAdapter()
	{
	}

	public void insertUpdate( DocumentEvent e )
	{
		update( e.getDocument() );
	}

	public abstract void update( Document document );

	public void removeUpdate( DocumentEvent e )
	{
		update( e.getDocument() );
	}

	public void changedUpdate( DocumentEvent e )
	{
	}

	public String getText( Document document )
	{
		try
		{
			return document.getText( 0, document.getLength() );
		}
		catch( BadLocationException e )
		{
			e.printStackTrace();
			return "";
		}
	}
}
