/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.RestParamProperty;
import com.eviware.soapui.support.StringUtils;
import com.sun.research.wadl.x2006.x10.ApplicationDocument;
import com.sun.research.wadl.x2006.x10.ParamStyle;
import com.sun.research.wadl.x2006.x10.ApplicationDocument.Application;
import com.sun.research.wadl.x2006.x10.DocDocument.Doc;
import com.sun.research.wadl.x2006.x10.MethodDocument.Method;
import com.sun.research.wadl.x2006.x10.ParamDocument.Param;
import com.sun.research.wadl.x2006.x10.RequestDocument.Request;
import com.sun.research.wadl.x2006.x10.ResourceDocument.Resource;
import com.sun.research.wadl.x2006.x10.ResourcesDocument.Resources;
import com.sun.research.wadl.x2006.x10.ResponseDocument.Response;

public class WadlUtils
{
	public static Map<String, XmlObject> getDefinitionParts(String wadlUrl)
	{
		Map<String, XmlObject> result = new HashMap<String, XmlObject>();

		try
		{
			URL url = new URL(wadlUrl);
			ApplicationDocument applicationDocument = ApplicationDocument.Factory.parse(url);
			result.put(url.getPath(), applicationDocument);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public static Map<String, XmlObject> generateWadl(RestService restService)
	{
		Map<String, XmlObject> result = new HashMap<String, XmlObject>();

		ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
		Application application = applicationDocument.addNewApplication();
		Resources resources = application.addNewResources();
		resources.setBase(restService.getBasePath());

		for (int c = 0; c < restService.getOperationCount(); c++)
		{
			resources.addNewResource().set(generateWadlResource(restService.getOperationAt(c)));
		}

		result.put("<generated>", applicationDocument);

		return result;
	}

	private static XmlObject generateWadlResource(RestResource resource)
	{
		Resource resourceConfig = Resource.Factory.newInstance();
		createDoc(resourceConfig.addNewDoc(), resource.getName(), resource.getDescription());
		String path = resource.getPath();
		if( path.startsWith("/"))
			path = path.length() > 1 ? path.substring(1) : "";
			
		resourceConfig.setPath(path);
		resourceConfig.setId(resource.getName());
		
		XmlBeansRestParamsTestPropertyHolder params = resource.getParams();
		for (int c = 0; c < params.size(); c++)
		{
			createParam(resourceConfig.addNewParam(), params.getPropertyAt(c));
		}

		for (int c = 0; c < resource.getResourceCount(); c++)
		{
			resourceConfig.addNewResource().set(generateWadlResource(resource.getResourceAt(c)));
		}

		for (int c = 0; c < resource.getRequestCount(); c++)
		{
			RestRequest request = resource.getRequestAt(c);
			addMethod(resourceConfig, request);
		}

		return resourceConfig;
	}

	private static void createParam(Param paramConfig, RestParamProperty propertyAt)
	{
		paramConfig.setName(propertyAt.getName());
		createDoc( paramConfig.addNewDoc(), propertyAt.getName(), propertyAt.getDescription() );
		
		if( StringUtils.hasContent(propertyAt.getValue()))
			paramConfig.setDefault( propertyAt.getValue() );
		
		switch( propertyAt.getType())
		{
			case STRING : paramConfig.setType(org.apache.xmlbeans.XmlString.type.getName());
		}
		
		ParamStyle.Enum style = ParamStyle.QUERY;
		switch (propertyAt.getStyle())
		{
		case HEADER:
			style = ParamStyle.HEADER;
			break;
		case MATRIX:
			style = ParamStyle.MATRIX;
			break;
		case PLAIN:
			style = ParamStyle.PLAIN;
			break;
		case TEMPLATE:
			style = ParamStyle.TEMPLATE;
			break;
		}

		paramConfig.setStyle(style);
	}

	private static void createDoc(Doc docConfig, String name, String description)
	{
		docConfig.setLang("en");
		docConfig.setTitle( StringUtils.hasContent(description) ? description : "[" + name + "]");
	}

	private static void addMethod(Resource resourceConfig, RestRequest request)
	{
		Method methodConfig = resourceConfig.addNewMethod();
		createDoc(methodConfig.addNewDoc(), request.getName(), request.getDescription());
		methodConfig.setName(request.getMethod().toString());
		methodConfig.setId(request.getName());
		Request requestConfig = methodConfig.addNewRequest();

		Set<String> defaultParamNames = new HashSet<String>();
		for( RestParamProperty defaultParam : request.getResource().getDefaultParams() )
			defaultParamNames.add(defaultParam.getName());
		
		XmlBeansRestParamsTestPropertyHolder params = request.getParams();
		for (int c = 0; c < params.size(); c++)
		{
			RestParamProperty param = params.getPropertyAt( c );
			if( !defaultParamNames.contains(param.getName()))
				createParam(requestConfig.addNewParam(), param );
		}
		
		Response responseConfig = methodConfig.addNewResponse();
		responseConfig.addNewRepresentation().setMediaType("application/xml");
	}

	public static String extractParams(URL param, XmlBeansRestParamsTestPropertyHolder params)
	{
		String path = param.getPath();
		String[] items = path.split("/");

		int templateParamCount = 0;
		StringBuffer resultPath = new StringBuffer();

		for (int i = 0; i < items.length; i++)
		{
			String item = items[i];
			try
			{
				String[] matrixParams = item.split(";");
				if (matrixParams.length > 0)
				{
					item = matrixParams[0];
					for (int c = 1; c < matrixParams.length; c++)
					{
						String matrixParam = matrixParams[c];

						int ix = matrixParam.indexOf('=');
						if (ix == -1)
						{
							params.addProperty(URLDecoder.decode(matrixParam, "Utf-8")).setStyle(ParameterStyle.MATRIX);
						}
						else
						{
							String name = matrixParam.substring(0, ix);
							RestParamProperty property = params.addProperty(URLDecoder.decode(name, "Utf-8"));
							property.setStyle(ParameterStyle.MATRIX);
							property.setValue(URLDecoder.decode(matrixParam.substring(ix + 1), "Utf-8"));
						}
					}
				}

				Integer.parseInt(item);
				RestParamProperty prop = params.addProperty("param" + templateParamCount++);
				prop.setStyle(ParameterStyle.TEMPLATE);
				prop.setValue(item);

				item = "{" + prop.getName() + "}";
			}
			catch (Exception e)
			{
			}

			if (StringUtils.hasContent(item))
				resultPath.append('/').append(item);
		}

		String query = ((URL) param).getQuery();
		if (StringUtils.hasContent(query))
		{
			items = query.split("&");
			for (String item : items)
			{
				try
				{
					int ix = item.indexOf('=');
					if (ix == -1)
					{
						params.addProperty(URLDecoder.decode(item, "Utf-8")).setStyle(ParameterStyle.QUERY);
					}
					else
					{
						String name = item.substring(0, ix);
						RestParamProperty property = params.addProperty(URLDecoder.decode(name, "Utf-8"));
						property.setStyle(ParameterStyle.QUERY);
						property.setValue(URLDecoder.decode(item.substring(ix + 1), "Utf-8"));
					}
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
		}

		return resultPath.toString();
	}
}
