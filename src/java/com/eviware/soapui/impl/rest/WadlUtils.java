package com.eviware.soapui.impl.rest;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import com.sun.research.wadl.x2006.x10.ApplicationDocument;
import com.sun.research.wadl.x2006.x10.ApplicationDocument.Application;
import com.sun.research.wadl.x2006.x10.ResourceDocument.Resource;
import com.sun.research.wadl.x2006.x10.ResourcesDocument.Resources;

public class WadlUtils
{
	public static Map<String, XmlObject> getDefinitionParts(String wadlUrl)
	{
		Map<String,XmlObject> result = new HashMap<String, XmlObject>();
		
		try
		{
			URL url = new URL( wadlUrl );
			ApplicationDocument applicationDocument = ApplicationDocument.Factory.parse( url);
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
		Map<String,XmlObject> result = new HashMap<String, XmlObject>();
		
		ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
		Application application = applicationDocument.addNewApplication();
		Resources resources = application.addNewResources();
		
		for( int c = 0; c < restService.getOperationCount(); c++ )
		{
			resources.addNewResource().set( generateWadlResource( restService.getOperationAt(c)));
		}

		result.put( "<generated>", applicationDocument );
		
		return result;
	}

	private static XmlObject generateWadlResource(RestResource operationAt)
	{
		Resource resource = Resource.Factory.newInstance();
		resource.setPath(operationAt.getPath());
		
		
		
		return resource;
	}
}
