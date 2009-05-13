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

package com.eviware.soapui.support.editor.inspectors.attachments;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;

/**
 * Handles drop of files on the AttachmentPanel
 * 
 * @author emibre
 */

public class FileTransferHandler extends TransferHandler
{
	private DataFlavor fileFlavor;
	private AttachmentTableModel attachmentModel;

	/** Creates a new instance of FileTransferHandler */
	public FileTransferHandler( AttachmentTableModel attachmentModel )
	{
		fileFlavor = DataFlavor.javaFileListFlavor;
		this.attachmentModel = attachmentModel;
	}

	public boolean canImport( JComponent c, DataFlavor[] flavors )
	{
		return hasFileFlavor( flavors );
	}

	private boolean hasFileFlavor( DataFlavor[] flavors )
	{
		for( int i = 0; i < flavors.length; i++ )
		{
			if( fileFlavor.equals( flavors[i] ) )
			{
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings( "unchecked" )
	public boolean importData( JComponent c, Transferable t )
	{
		try
		{
			List<File> files = ( List<File> )t.getTransferData( fileFlavor );
			for( File f : files )
			{
				System.out.println( "Got a file: " + f.getName() );
				Boolean retval = UISupport.confirmOrCancel( "Cache attachment in request?", "Att Attachment" );
				if( retval == null )
					return false;

				attachmentModel.addFile( f, retval );
			}

		}
		catch( IOException ex )
		{
			SoapUI.logError( ex );
		}
		catch( UnsupportedFlavorException ex )
		{
			SoapUI.logError( ex );
		}
		return false;
	}

	public int getSourceActions( JComponent c )
	{
		return COPY_OR_MOVE;
	}
}
