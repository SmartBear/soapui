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

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JTable;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionImpl;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.tree.nodes.PropertyTreeNode.PropertyModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;

public class JTableTestPropertyDropTarget implements DropTargetListener
{
	private final JTable table;
	private final ModelItem modelItem;

	public JTableTestPropertyDropTarget( ModelItem modelItem, JTable table )
	{
		this.modelItem = modelItem;
		this.table = table;
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
				Transferable transferable = dtde.getTransferable();
				Object transferData = transferable.getTransferData( transferable.getTransferDataFlavors()[0] );
				if( transferData instanceof PropertyModelItem )
				{
					dtde.acceptDrop( dtde.getDropAction() );
					PropertyModelItem modelItem = ( PropertyModelItem )transferData;

					String xpath = modelItem.getXPath();
					if( xpath == null && XmlUtils.seemsToBeXml( modelItem.getProperty().getValue() ) )
					{
						xpath = UISupport.selectXPath( "Create PropertyExpansion", "Select XPath below", modelItem
								.getProperty().getValue(), null );

						if( xpath != null )
							xpath = XmlUtils.removeXPathNamespaceDeclarations( xpath );
					}

					PropertyExpansion propertyExpansion = new PropertyExpansionImpl( modelItem.getProperty(), xpath );

					Point point = dtde.getLocation();
					int column = table.columnAtPoint( point );
					int row = table.rowAtPoint( point );
					table.setValueAt( propertyExpansion.toString(), row, column );

					dtde.dropComplete( true );
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
	}

	public void dropActionChanged( DropTargetDragEvent dtde )
	{
	}

	public boolean isAcceptable( Transferable transferable, Point point )
	{
		int column = table.columnAtPoint( point );
		int row = table.rowAtPoint( point );
		if( !table.isCellEditable( row, column ) )
			return false;

		DataFlavor[] flavors = transferable.getTransferDataFlavors();
		for( int i = 0; i < flavors.length; i++ )
		{
			DataFlavor flavor = flavors[i];
			if( flavor.isMimeTypeEqual( DataFlavor.javaJVMLocalObjectMimeType ) )
			{
				try
				{
					Object modelItem = transferable.getTransferData( flavor );
					if( modelItem instanceof PropertyModelItem )
					{
						return PropertyExpansionUtils.canExpandProperty( this.modelItem, ( ( PropertyModelItem )modelItem )
								.getProperty() );
					}
				}
				catch( Exception ex )
				{
					SoapUI.logError( ex );
				}
			}
		}

		return false;
	}
}
