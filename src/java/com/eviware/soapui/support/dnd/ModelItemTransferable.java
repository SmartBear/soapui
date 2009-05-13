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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import com.eviware.soapui.model.ModelItem;

public class ModelItemTransferable implements Transferable
{
	public static final DataFlavor MODELITEM_DATAFLAVOR = new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType,
			"SoapUIModelItem" );

	private ModelItem modelItem;

	private DataFlavor[] _flavors = { MODELITEM_DATAFLAVOR };

	/**
	 * Constructs a transferrable tree path object for the specified path.
	 */
	public ModelItemTransferable( ModelItem path )
	{
		modelItem = path;
	}

	// Transferable interface methods...
	public DataFlavor[] getTransferDataFlavors()
	{
		return _flavors;
	}

	public ModelItem getModelItem()
	{
		return modelItem;
	}

	public boolean isDataFlavorSupported( DataFlavor flavor )
	{
		return java.util.Arrays.asList( _flavors ).contains( flavor );
	}

	public synchronized Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException
	{
		if( flavor.isMimeTypeEqual( MODELITEM_DATAFLAVOR.getMimeType() ) ) // DataFlavor.javaJVMLocalObjectMimeType))
			return modelItem;
		else
			throw new UnsupportedFlavorException( flavor );
	}
}
