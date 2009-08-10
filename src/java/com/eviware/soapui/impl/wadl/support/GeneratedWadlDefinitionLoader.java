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

package com.eviware.soapui.impl.wadl.support;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.WadlGenerator;
import com.eviware.soapui.impl.rest.panels.request.inspectors.schema.InferredSchemaManager;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionLoader;

public class GeneratedWadlDefinitionLoader extends AbstractDefinitionLoader
{
	private RestService restService;

	public GeneratedWadlDefinitionLoader( RestService restService )
	{
		this.restService = restService;
	}

	public XmlObject loadXmlObject( String wsdlUrl, XmlOptions options ) throws Exception
	{
		if( wsdlUrl.toLowerCase().endsWith( ".xsd" ) )
			return XmlObject.Factory.parse( InferredSchemaManager.getInferredSchema( restService ).getXsdForNamespace(
					InferredSchemaManager.namespaceForFilename( wsdlUrl ) ) );
		return new WadlGenerator( restService ).generateWadl();
	}

	public String getBaseURI()
	{
		return restService.getName() + ".wadl";
	}

	public void setNewBaseURI( String uri )
	{
		// not implemented
	}

	public String getFirstNewURI()
	{
		return getBaseURI();
	}
}
