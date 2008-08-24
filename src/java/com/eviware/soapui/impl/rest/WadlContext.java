package com.eviware.soapui.impl.rest;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.config.DefintionPartConfig;
import com.eviware.soapui.impl.support.DefinitionContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.CachedWsdlLoader;
import com.eviware.soapui.support.StringUtils;

public class WadlContext implements DefinitionContext
{
	private final RestService restService;

	public WadlContext(RestService restService)
	{
		this.restService = restService;
	}

	public boolean hasSchemaTypes()
	{
		return false;
	}

	public Map<String, XmlObject> getDefinitionParts() throws Exception
	{
		Map<String,XmlObject> result = new HashMap<String,XmlObject>();
		
		if( !restService.getConfig().isSetDefinitionCache())
		{
			if(  StringUtils.hasContent(restService.getWadlUrl() ) )
				return WadlUtils.getDefinitionParts( restService.getWadlUrl() );
			else
				return WadlUtils.generateWadl( restService );
		}
		
		DefinitionCacheConfig cache = restService.getConfig().getDefinitionCache();
		List<DefintionPartConfig> partList = cache.getPartList();
		for( DefintionPartConfig part : partList )
		{
			result.put( part.getUrl(), CachedWsdlLoader.getPartContent( cache, part ) );
		}
		
		return result;
	}

	public boolean loadIfNecessary()
	{
		return true;
	}

	public String export(String path) throws Exception
	{
		File dir = new File( path );
		if( !dir.exists())
			dir.mkdirs();
		
		File outFile = new File( dir, "export.wadl");
	   FileWriter writer = new FileWriter( outFile) ;
	   writer.write( getDefinitionParts().values().iterator().next().toString() );
	   writer.close();

	   return outFile.toURI().toURL().toString();
	}

	public boolean isCached()
	{
		return true;
	}
}
