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

package com.eviware.soapui.support.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import com.eviware.soapui.SoapUI;

public abstract class AbstractSoapUIDropTarget implements DropTargetListener
{
	public AbstractSoapUIDropTarget()
	{
	}

	public void dragEnter( DropTargetDragEvent dtde )
	{
		if( !isAcceptable( dtde.getTransferable(), dtde.getLocation() ) )
			dtde.rejectDrag();
	}

	public void dragExit( DropTargetEvent dtde )
	{
	}

	public void dragOver( DropTargetDragEvent dtde )
	{
		if( !isAcceptable( dtde.getTransferable(), dtde.getLocation() ) )
		{
			dtde.rejectDrag();
		}
		else
		{
			dtde.acceptDrag( dtde.getDropAction() );
		}
	}

	public void drop( DropTargetDropEvent dtde )
	{
		if( !isAcceptable( dtde.getTransferable(), dtde.getLocation() ) )
		{
			dtde.rejectDrop();
		}
		else
		{
			try
			{
				Object testCase = getTransferData( dtde.getTransferable() );
				if( testCase != null )
				{
					dtde.acceptDrop( dtde.getDropAction() );

					handleDrop( testCase, dtde.getLocation() );

					dtde.dropComplete( true );
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
	}

	protected abstract boolean handleDrop( Object target, Point point );

	protected abstract boolean isAcceptable( Object target, Point point );

	public void dropActionChanged( DropTargetDragEvent dtde )
	{
	}

	public boolean isAcceptable( Transferable transferable, Point point )
	{
		return isAcceptable( getTransferData( transferable ), point );
	}

	@SuppressWarnings( "unchecked" )
	private Object getTransferData( Transferable transferable )
	{
		DataFlavor[] flavors = transferable.getTransferDataFlavors();
		for( int i = 0; i < flavors.length; i++ )
		{
			DataFlavor flavor = flavors[i];
			if( flavor.isMimeTypeEqual( DataFlavor.javaJVMLocalObjectMimeType ) )
			{
				try
				{
					return transferable.getTransferData( flavor );
				}
				catch( Exception ex )
				{
					SoapUI.logError( ex );
				}
			}
		}

		return null;
	}

	public static void addDropTarget( Component component, AbstractSoapUIDropTarget target )
	{
		DropTarget dropTarget = new DropTarget( component, target );
		dropTarget.setDefaultActions( DnDConstants.ACTION_COPY_OR_MOVE );
	}
}