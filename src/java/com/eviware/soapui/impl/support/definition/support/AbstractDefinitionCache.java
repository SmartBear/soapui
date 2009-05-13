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

package com.eviware.soapui.impl.support.definition.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.config.DefinitionCacheTypeConfig;
import com.eviware.soapui.config.DefintionPartConfig;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.support.xml.XmlUtils;

public abstract class AbstractDefinitionCache<T extends AbstractInterface<?>> implements DefinitionCache
{
	protected DefinitionCacheConfig definitionCache;
	private T container;
	private InterfaceDefinitionPart rootPart;
	private List<InterfaceDefinitionPart> parts;

	public AbstractDefinitionCache( DefinitionCacheConfig definitionCache, T container )
	{
		this.definitionCache = definitionCache;
		this.container = container;

		if( definitionCache == null )
			definitionCache = reinit( container );
	}

	protected abstract DefinitionCacheConfig reinit( T owner );

	public T getContainer()
	{
		return container;
	}

	public boolean validate()
	{
		if( definitionCache.getRootPart() == null )
			return false;

		if( definitionCache.sizeOfPartArray() == 0 )
			return false;

		return true;
	}

	public void importCache( DefinitionCache cache ) throws Exception
	{
		if( cache instanceof AbstractDefinitionCache )
		{
			definitionCache = reinit( container );
			definitionCache.set( ( ( AbstractDefinitionCache<?> )cache ).getConfig() );
			initParts();
		}
		else
		{
			update( new InterfaceCacheDefinitionLoader( cache ) );
		}
	}

	protected DefinitionCacheConfig getConfig()
	{
		return definitionCache;
	}

	public void update( DefinitionLoader loader ) throws Exception
	{
		definitionCache = reinit( container );

		definitionCache.setType( DefinitionCacheTypeConfig.TEXT );

		Map<String, XmlObject> urls = SchemaUtils.getDefinitionParts( loader );
		definitionCache.setRootPart( loader.getFirstNewURI() );

		for( Iterator<String> i = urls.keySet().iterator(); i.hasNext(); )
		{
			DefintionPartConfig definitionPart = definitionCache.addNewPart();
			String url = i.next();
			definitionPart.setUrl( url );
			XmlObject xmlObject = urls.get( url );
			Node domNode = xmlObject.getDomNode();

			if( domNode.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE )
			{
				Node node = ( ( DocumentFragment )domNode ).getFirstChild();
				if( node.getNodeType() == Node.TEXT_NODE )
				{
					domNode = XmlUtils.parseXml( node.getNodeValue() );
					xmlObject = XmlObject.Factory.parse( domNode );
				}
			}

			Element contentElement = ( ( Document )domNode ).getDocumentElement();

			Node newDomNode = definitionPart.addNewContent().getDomNode();
			newDomNode.appendChild( newDomNode.getOwnerDocument().createTextNode( xmlObject.toString() ) );
			definitionPart.setType( contentElement.getNamespaceURI() );
		}

		initParts();
	}

	public List<InterfaceDefinitionPart> getDefinitionParts() throws Exception
	{
		if( parts == null )
		{
			initParts();
		}

		return parts;
	}

	private void initParts()
	{
		parts = new ArrayList<InterfaceDefinitionPart>();

		List<DefintionPartConfig> partList = definitionCache.getPartList();
		for( DefintionPartConfig part : partList )
		{
			ConfigInterfaceDefinitionPart configInterfaceDefinitionPart = new ConfigInterfaceDefinitionPart( part, part
					.getUrl().equals( definitionCache.getRootPart() ), definitionCache.getType() );
			parts.add( configInterfaceDefinitionPart );

			if( configInterfaceDefinitionPart.isRootPart() )
				rootPart = configInterfaceDefinitionPart;
		}
	}

	public InterfaceDefinitionPart getRootPart()
	{
		if( parts == null )
			initParts();

		return rootPart;
	}

	public void clear()
	{
		definitionCache.setRootPart( null );

		while( definitionCache.sizeOfPartArray() > 0 )
			definitionCache.removePart( 0 );
	}
}
