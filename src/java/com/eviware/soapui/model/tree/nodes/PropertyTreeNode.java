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

package com.eviware.soapui.model.tree.nodes;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

public class PropertyTreeNode extends AbstractModelItemTreeNode<PropertyTreeNode.PropertyModelItem>
{
	private boolean readOnly;
	private final TestProperty property;

	protected PropertyTreeNode( TestProperty property, ModelItem parent, TestPropertyHolder holder,
			SoapUITreeModel treeModel )
	{
		super( new PropertyModelItem( property, property.isReadOnly() ), parent, treeModel );
		this.property = property;
		readOnly = property.isReadOnly();
	}

	public static String buildName( TestProperty property )
	{
		String name = property.getName();
		String value = property.getValue();
		if( value == null )
			value = "";
		else
		{
			if( value.length() > 12 )
				value = value.substring( 0, 12 ) + "..";

			value = "'" + value + "'";
		}

		return name + " : " + value;
	}

	@Override
	public ActionList getActions()
	{
		if( !readOnly )
		{
			DefaultActionList actions = new DefaultActionList();
			SetPropertyValueAction setPropertyValueAction = new SetPropertyValueAction();
			actions.addAction( setPropertyValueAction );
			actions.setDefaultAction( setPropertyValueAction );
			return actions;
		}
		else
		{
			return super.getActions();
		}
	}

	public static class PropertyModelItem extends EmptyModelItem
	{
		private final TestProperty property;
		private String xpath;

		public PropertyModelItem( TestProperty property, boolean readOnly )
		{
			super( buildName( property ), readOnly ? UISupport.createImageIcon( "/bullet_black.gif" ) : UISupport
					.createImageIcon( "/bullet_green.gif" ) );

			this.property = property;
		}

		public TestProperty getProperty()
		{
			return property;
		}

		public String getXPath()
		{
			return xpath;
		}

		public void setXPath( String xpath )
		{
			this.xpath = xpath;
		}
	}

	private class SetPropertyValueAction extends AbstractAction
	{
		public SetPropertyValueAction()
		{
			super( "Set Value" );
			putValue( Action.SHORT_DESCRIPTION, "Prompts to set the value of this property" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String value = UISupport.prompt( "Specify property value", "Set Value", property.getValue() );
			if( StringUtils.hasContent( value ) )
			{
				property.setValue( value );
			}
		}
	}
}