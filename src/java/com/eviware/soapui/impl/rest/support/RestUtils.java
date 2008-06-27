package com.eviware.soapui.impl.rest.support;

import java.net.URL;
import java.util.List;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.RestParamProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringList;
import com.sun.research.wadl.x2006.x10.ApplicationDocument;
import com.sun.research.wadl.x2006.x10.ApplicationDocument.Application;
import com.sun.research.wadl.x2006.x10.DocDocument.Doc;
import com.sun.research.wadl.x2006.x10.MethodDocument.Method;
import com.sun.research.wadl.x2006.x10.ParamDocument.Param;
import com.sun.research.wadl.x2006.x10.ResourceDocument.Resource;
import com.sun.research.wadl.x2006.x10.ResourcesDocument.Resources;

public class RestUtils
{
	public static String [] extractTemplateParams( String path )
	{
		StringList result = new StringList();
		
		int ix = path.indexOf('{');
		while( ix != -1 )
		{
			int endIx = path.indexOf('}', ix);
			if( endIx == -1 )
				break;
			
			if( endIx > ix+1 )
				result.add( path.substring(ix+1, endIx));
			
			ix = path.indexOf('{', ix+1 );
		}
		
		return result.toStringArray();

	}
	
	public static void initFromWadl(RestService service, String wadlUrl)
	{
		try
		{
			ApplicationDocument applicationDocument = ApplicationDocument.Factory.parse( new URL( wadlUrl ));
			Application application = applicationDocument.getApplication();

			Resources resources = application.getResources();
			
			service.setBasePath(resources.getBase());
			service.setWadlUrl(wadlUrl);
			
			for( Resource resource : resources.getResourceList())
			{
				String name = getFirstTitle(resource.getDocList(), resource.getPath());
				String path = resource.getPath();
				
				RestResource newResource = service.addNewResource(name, path);
				initResourceFromWadlResource(newResource, resource, application );
				
				addSubResources( newResource, resource, application );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void addSubResources(RestResource newResource, Resource resource, Application application)
	{
		for( Resource res : resource.getResourceList() )
		{
			String name = getFirstTitle(resource.getDocList(), resource.getPath());
			String path = resource.getPath();
			
			RestResource newRes = newResource.addNewResource(name, path);
			initResourceFromWadlResource(newRes, res, application );
			
			addSubResources( newRes, res, application );
		}
	}

	private static String getFirstTitle(List<Doc> list, String defaultTitle)
	{
		for( Doc doc : list )
		{
			if( StringUtils.hasContent(doc.getTitle()))
			{
				return doc.getTitle();
			}
		}
		return defaultTitle;
	}

	private static void initResourceFromWadlResource(RestResource newResource, Resource resource, Application application)
	{
		String[] templateParams = RestUtils.extractTemplateParams(resource.getPath());
		for( String param : templateParams )
		{
			newResource.addProperty(param);
		}
		
		for( Param param : resource.getParamList())
		{
			String nm = param.getName();
			RestParamProperty prop = newResource.hasProperty(nm) ?
					newResource.getProperty(nm) : newResource.addProperty(nm);
					
			prop.setValue(param.getDefault());
			prop.setStyle( ParameterStyle.valueOf( param.getStyle().toString().toUpperCase() ));
		}
		
		for( Method method : resource.getMethodList())
		{
			method = resolveMethod( method, application );
			
			RestRequest request = newResource.addNewRequest( getFirstTitle(method.getDocList(), method.getName() + " - " + method.getId()));
			request.setMethod( RestRequest.RequestMethod.valueOf( method.getName() ));
			
			for( RestParamProperty prop : newResource.getDefaultParams())
			{
				RestParamProperty p = request.addProperty(prop.getName());
				p.setValue(prop.getValue());
				p.setStyle(prop.getStyle());
			}
			
			for( Param param : method.getRequest().getParamList())
			{
				RestParamProperty p = request.addProperty(param.getName());
				p.setValue(param.getDefault());
				p.setStyle( ParameterStyle.valueOf( param.getStyle().toString().toUpperCase() ));
			}
		}
	}

	private static Method resolveMethod(Method method, Application application)
	{
		String href = method.getHref();
		if( !StringUtils.hasContent(href))
			return method;
		
		for( Method m : application.getMethodList())
		{
			if( m.getId().equals(href.substring(1)))
				return m;
		}
		
		return method;
	}
}
