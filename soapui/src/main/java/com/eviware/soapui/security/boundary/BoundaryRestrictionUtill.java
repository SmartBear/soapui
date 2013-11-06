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
package com.eviware.soapui.security.boundary;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlAnySimpleType;

import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

public class BoundaryRestrictionUtill
{
	public static String getRestrictionInfo( String label, String name, String xpath )
	{
		return BoundaryUtils.createCharacterArray( "ABCDEFG ", 100 );
	}

	public static List<String> getRestrictions( XmlTreeNode node, List<String> restrictionsList )
	{
		String baseType = null;

		for( int i = 0; i < node.getChildCount(); i++ )
		{
			XmlTreeNode mynode = node.getChild( i );

			if( "xsd:restriction".equals( mynode.getParent().getNodeName() ) )
			{
				if( mynode.getNodeName().equals( "@base" ) )
				{
					baseType = mynode.getNodeText();
					restrictionsList.add( "type = " + baseType );
				}
				else
				{
					String nodeName = mynode.getNodeName();
					String nodeValue = mynode.getChild( 0 ).getNodeText();
					restrictionsList.add( nodeName + " = " + nodeValue );
				}
			}
			getRestrictions( mynode, restrictionsList );
		}
		return restrictionsList;
	}

	public static List<String> getType( XmlTreeNode node, List<String> restrictionsList )
	{
		String baseType = null;

		for( int i = 0; i < node.getChildCount(); i++ )
		{
			XmlTreeNode mynode = node.getChild( i );

			if( mynode.getNodeName().equals( "@base" ) )
			{
				baseType = mynode.getNodeText();
				if( baseType.contains( ":" ) )
				{
					baseType = baseType.substring( baseType.indexOf( ":" ) + 1 );
				}
				restrictionsList.add( "type = " + baseType );
			}
			getType( mynode, restrictionsList );
		}
		return restrictionsList;
	}

	public static List<String> extractEnums( XmlTreeNode node )
	{
		List<String> restrictionsList = new ArrayList<String>();
		for( XmlAnySimpleType s : node.getSchemaType().getEnumerationValues() )
		{
			if( restrictionsList.isEmpty() )
			{
				restrictionsList.add( "For type enumeration values are: " );
			}
			restrictionsList.add( s.getStringValue() );
		}
		return restrictionsList;
	}

}
