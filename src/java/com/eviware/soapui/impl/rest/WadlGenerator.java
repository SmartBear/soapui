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

package com.eviware.soapui.impl.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.RestParamProperty;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.StringUtils;
import com.sun.research.wadl.x2006.x10.ApplicationDocument;
import com.sun.research.wadl.x2006.x10.ParamStyle;
import com.sun.research.wadl.x2006.x10.RepresentationType;
import com.sun.research.wadl.x2006.x10.ApplicationDocument.Application;
import com.sun.research.wadl.x2006.x10.DocDocument.Doc;
import com.sun.research.wadl.x2006.x10.MethodDocument.Method;
import com.sun.research.wadl.x2006.x10.ParamDocument.Param;
import com.sun.research.wadl.x2006.x10.RequestDocument.Request;
import com.sun.research.wadl.x2006.x10.ResourceDocument.Resource;
import com.sun.research.wadl.x2006.x10.ResourcesDocument.Resources;
import com.sun.research.wadl.x2006.x10.ResponseDocument.Response;

public class WadlGenerator
{
	private RestService restService;

	public WadlGenerator( RestService restService )
	{
		this.restService = restService;
	}

	public ApplicationDocument generateWadl()
	{
		ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
		Application application = applicationDocument.addNewApplication();

		createDoc( application.addNewDoc(), restService );

		Resources resources = application.addNewResources();

		// use first endpoint for now -> this should be configurable
		String basePath = restService.getBasePath();
		String[] endpoints = restService.getEndpoints();
		if( endpoints.length > 0 )
			basePath = endpoints[0] + basePath;

		resources.setBase( basePath );

		for( int c = 0; c < restService.getOperationCount(); c++ )
		{
			resources.addNewResource().set( generateWadlResource( restService.getOperationAt( c ) ) );
		}

		return applicationDocument;
	}

	private XmlObject generateWadlResource( RestResource resource )
	{
		Resource resourceConfig = Resource.Factory.newInstance();
		createDoc( resourceConfig.addNewDoc(), resource );
		String path = resource.getPath();
		if( path.startsWith( "/" ) )
			path = path.length() > 1 ? path.substring( 1 ) : "";

		resourceConfig.setPath( path );
		resourceConfig.setId( resource.getName() );

		XmlBeansRestParamsTestPropertyHolder params = resource.getParams();
		for( int c = 0; c < params.size(); c++ )
		{
			generateParam( resourceConfig.addNewParam(), params.getPropertyAt( c ) );
		}

		for( int c = 0; c < resource.getChildResourceCount(); c++ )
		{
			resourceConfig.addNewResource().set( generateWadlResource( resource.getChildResourceAt( c ) ) );
		}

		for( int c = 0; c < resource.getRequestCount(); c++ )
		{
			RestRequest request = resource.getRequestAt( c );
			generateWadlMethod( resourceConfig, request );
		}

		return resourceConfig;
	}

	private void generateParam( Param paramConfig, RestParamProperty param )
	{
		paramConfig.setName( param.getName() );

		if( StringUtils.hasContent( param.getDefaultValue() ) )
			paramConfig.setDefault( param.getDefaultValue() );

		paramConfig.setType( param.getType() );
		paramConfig.setRequired( param.getRequired() );
		paramConfig.setDefault( param.getDefaultValue() );

		if( StringUtils.hasContent( param.getDescription() ) )
			createDoc( paramConfig.addNewDoc(), param.getName() + " Parameter", param.getDescription() );

		String[] options = param.getOptions();
		for( String option : options )
			paramConfig.addNewOption().setValue( option );

		ParamStyle.Enum style = ParamStyle.QUERY;
		switch( param.getStyle() )
		{
		case HEADER :
			style = ParamStyle.HEADER;
			break;
		case MATRIX :
			style = ParamStyle.MATRIX;
			break;
		case PLAIN :
			style = ParamStyle.PLAIN;
			break;
		case TEMPLATE :
			style = ParamStyle.TEMPLATE;
			break;
		}

		paramConfig.setStyle( style );
	}

	private void createDoc( Doc docConfig, ModelItem modelItem )
	{
		createDoc( docConfig, modelItem.getName(), modelItem.getDescription() );
	}

	private void createDoc( Doc docConfig, String name, String description )
	{
		docConfig.setLang( "en" );
		docConfig.setTitle( name );
		docConfig.getDomNode().appendChild( docConfig.getDomNode().getOwnerDocument().createTextNode( description ) );
	}

	private void generateWadlMethod( Resource resourceConfig, RestRequest request )
	{
		Method methodConfig = resourceConfig.addNewMethod();
		createDoc( methodConfig.addNewDoc(), request );
		methodConfig.setName( request.getMethod().toString() );
		methodConfig.setId( request.getName() );
		Request requestConfig = methodConfig.addNewRequest();

		Map<String, RestParamProperty> defaultParams = new HashMap<String, RestParamProperty>();
		for( RestParamProperty defaultParam : request.getResource().getDefaultParams() )
			defaultParams.put( defaultParam.getName(), defaultParam );

		XmlBeansRestParamsTestPropertyHolder params = request.getParams();
		for( int c = 0; c < params.size(); c++ )
		{
			RestParamProperty param = params.getPropertyAt( c );
			if( !defaultParams.containsKey( param.getName() ) || !param.equals( defaultParams.get( param.getName() ) ) )
				generateParam( requestConfig.addNewParam(), param );
		}

		if( request.hasRequestBody() )
		{
			for( RestRepresentation representation : request.getRepresentations( RestRepresentation.Type.REQUEST, null ) )
			{
				generateRepresentation( requestConfig.addNewRepresentation(), representation );
			}
		}

		Response responseConfig = methodConfig.addNewResponse();
		for( RestRepresentation representation : request.getRepresentations( RestRepresentation.Type.RESPONSE, null ) )
		{
			generateRepresentation( responseConfig.addNewRepresentation(), representation );
		}

		for( RestRepresentation representation : request.getRepresentations( RestRepresentation.Type.FAULT, null ) )
		{
			generateRepresentation( responseConfig.addNewFault(), representation );
		}
	}

	private void generateRepresentation( RepresentationType representationConfig, RestRepresentation representation )
	{
		representationConfig.setMediaType( representation.getMediaType() );

		if( StringUtils.hasContent( representation.getId() ) )
			representationConfig.setId( representation.getId() );

		List status = representation.getStatus();
		if( status != null && status.size() > 0 )
		{
			representationConfig.setStatus( status );
		}
	}

}
