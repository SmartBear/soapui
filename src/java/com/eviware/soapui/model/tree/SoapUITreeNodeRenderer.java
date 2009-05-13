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

package com.eviware.soapui.model.tree;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.Tools;

/**
 * TreeCellRenderer for SoapUITreeNodes
 * 
 * @author Ole.Matzura
 */

public class SoapUITreeNodeRenderer extends DefaultTreeCellRenderer
{
	public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus )
	{
		ModelItem modelItem = ( ( SoapUITreeNode )value ).getModelItem();
		if( modelItem instanceof Project )
		{
			Project project = ( Project )modelItem;
			if( !project.isOpen() && !project.isDisabled() )
			{
				leaf = false;
				expanded = false;
			}
		}

		super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );

		ImageIcon icon = modelItem.getIcon();
		setIcon( icon );

		if( modelItem instanceof TestStep && ( ( TestStep )modelItem ).isDisabled() )
		{
			setEnabled( false );
		}
		else if( modelItem instanceof TestCase && ( ( TestCase )modelItem ).isDisabled() )
		{
			setEnabled( false );
		}
		else if( modelItem instanceof TestSuite && ( ( TestSuite )modelItem ).isDisabled() )
		{
			setEnabled( false );
		}
		else
		{
			setEnabled( true );
		}

		String toolTipText = tree.getToolTipText();
		if( toolTipText == null )
		{
			String description = modelItem.getDescription();
			if( description == null || description.trim().length() == 0 )
				description = modelItem.getName();

			if( description != null && description.trim().indexOf( '\n' ) > 0 )
				description = Tools.convertToHtml( description );

			setToolTipText( description );
		}
		else
			setToolTipText( toolTipText.length() > 0 ? toolTipText : null );

		return this;
	}
}
