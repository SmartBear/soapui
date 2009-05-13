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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

public class XmlHolder implements Map<String, Object>
{
	private XmlObject xmlObject;
	private StringToStringMap declaredNamespaces;
	private PropertyExpansionContext context;
	private String propertyRef;

	public XmlHolder( String xml ) throws XmlException
	{
		xmlObject = XmlObject.Factory.parse( xml );
	}

	public XmlHolder( Node node ) throws XmlException
	{
		xmlObject = XmlObject.Factory.parse( node );
	}

	public XmlHolder( XmlObject xmlObject ) throws XmlException
	{
		this.xmlObject = xmlObject;
	}

	public XmlHolder( PropertyExpansionContext context, String propertyRef ) throws XmlException
	{
		this( context.getProperty( propertyRef ).toString() );

		this.context = context;
		this.propertyRef = propertyRef;
	}

	public void updateProperty()
	{
		updateProperty( false );
	}

	public void updateProperty( boolean prettyPrint )
	{
		if( context != null && propertyRef != null )
		{
			context.setProperty( propertyRef, prettyPrint ? getPrettyXml() : getXml() );
		}
	}

	public String getNodeValue( String xpath ) throws XmlException
	{
		xpath = initXPathNamespaces( xpath );

		return XmlUtils.selectFirstNodeValue( xmlObject, xpath );
	}

	public StringToStringMap getNamespaces()
	{
		if( declaredNamespaces == null )
			declaredNamespaces = new StringToStringMap();

		return declaredNamespaces;
	}

	public void declareNamespace( String prefix, String uri )
	{
		if( declaredNamespaces == null )
			declaredNamespaces = new StringToStringMap();

		declaredNamespaces.put( prefix, uri );
	}

	public String[] getNodeValues( String xpath ) throws XmlException
	{
		xpath = initXPathNamespaces( xpath );

		return XmlUtils.selectNodeValues( xmlObject, xpath );
	}

	private String initXPathNamespaces( String xpath )
	{
		if( declaredNamespaces != null && !declaredNamespaces.isEmpty() )
		{
			for( String prefix : declaredNamespaces.keySet() )
			{
				xpath = "declare namespace " + prefix + "='" + declaredNamespaces.get( prefix ) + "';\n" + xpath;
			}
		}
		else if( !xpath.trim().startsWith( "declare namespace" ) )
		{
			xpath = XmlUtils.declareXPathNamespaces( xmlObject ) + xpath;
		}
		return xpath;
	}

	public void setNodeValue( String xpath, Object value ) throws XmlException
	{
		xpath = initXPathNamespaces( xpath );

		XmlCursor cursor = xmlObject.newCursor();
		try
		{
			cursor.selectPath( xpath );

			if( cursor.toNextSelection() )
			{
				XmlUtils.setNodeValue( cursor.getDomNode(), value == null ? null : value.toString() );
			}
		}
		finally
		{
			cursor.dispose();
		}
	}

	public XmlObject getXmlObject()
	{
		return xmlObject;
	}

	public Node getDomNode( String xpath ) throws XmlException
	{
		xpath = initXPathNamespaces( xpath );
		return XmlUtils.selectFirstDomNode( xmlObject, xpath );
	}

	public Node[] getDomNodes( String xpath ) throws XmlException
	{
		xpath = initXPathNamespaces( xpath );
		return XmlUtils.selectDomNodes( xmlObject, xpath );
	}

	public void removeDomNodes( String xpath ) throws XmlException
	{
		xpath = initXPathNamespaces( xpath );
		Node[] nodes = getDomNodes( xpath );
		for( Node node : nodes )
		{
			node.getParentNode().removeChild( node );
		}
	}

	public String getXml()
	{
		return xmlObject.xmlText();
	}

	public String getPrettyXml()
	{
		return XmlUtils.prettyPrintXml( xmlObject );
	}

	public void clear()
	{
	}

	public boolean containsKey( Object key )
	{
		try
		{
			return getDomNode( key.toString() ) != null;
		}
		catch( XmlException e )
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean containsValue( Object value )
	{
		try
		{
			return getNodeValue( value.toString() ) != null;
		}
		catch( XmlException e )
		{
			e.printStackTrace();
			return false;
		}
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet()
	{
		return null;
	}

	public Object get( Object key )
	{
		try
		{
			String str = key.toString();
			if( str.equals( "prettyXml" ) )
				return getPrettyXml();
			else if( str.equals( "xmlObject" ) )
				return getXmlObject();
			else if( str.equals( "namespaces" ) )
				return getNamespaces();
			else if( str.equals( "xml" ) )
				return getXml();

			String[] nodeValues = getNodeValues( str );
			return nodeValues != null && nodeValues.length == 1 ? nodeValues[0] : nodeValues;
		}
		catch( XmlException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public boolean isEmpty()
	{
		return false;
	}

	public Set<String> keySet()
	{
		return null;
	}

	public String put( String key, Object value )
	{
		try
		{
			String result = getNodeValue( key );
			setNodeValue( key, value == null ? null : value.toString() );
			return result;
		}
		catch( XmlException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public void putAll( Map<? extends String, ? extends Object> t )
	{
		if( t.keySet() == null )
			return;

		for( String key : t.keySet() )
		{
			put( key, t.get( key ) );
		}
	}

	public Object remove( Object key )
	{
		try
		{
			Node node = getDomNode( key.toString() );
			if( node != null )
			{
				node.getParentNode().removeChild( node );
			}
		}
		catch( XmlException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	public int size()
	{
		return 0;
	}

	public Collection<Object> values()
	{
		return null;
	}
}
