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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.config.DefintionPartConfig;
import com.eviware.soapui.impl.support.DefinitionContext;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaException;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;

/**
 * Holder for WSDL4J Definitions and related SchemaTypeLoader types
 * 
 * @author Ole.Matzura
 */

public class WsdlContext implements DefinitionContext
{
   private String url;
   private Definition definition;
   private SchemaTypeLoader schemaTypes;
   private boolean loaded;
	private SchemaException schemaException;
	
	private final static Logger log = Logger.getLogger( WsdlContext.class );
	private SoapVersion soapVersion;
	private DefinitionCacheConfig cache;
	private WsdlLoader currentLoader;
	private WsdlInterface iface;
	private WSDLFactory factory;
	private WSDLReader wsdlReader;
	
	private static Map<String,Definition> definitionCache = new HashMap<String, Definition>();
	private static Map<String,SchemaTypeLoader> schemaCache = new HashMap<String, SchemaTypeLoader>();
	private static Map<String,Integer> urlReferences = new HashMap<String, Integer>();
	
   public WsdlContext( String url, SoapVersion soapVersion, DefinitionCacheConfig cache, WsdlInterface iface )
   {
   	this.url = url;
		this.soapVersion = soapVersion;
		this.cache = cache;
		this.iface = iface;
   }
   
   public WsdlContext( String wsdlUrl, SoapVersion soapVersion )
	{
		this( wsdlUrl, soapVersion, null, null );
	}

	public WsdlInterface getInterface()
	{
		return iface;
	}

	public DefinitionCacheConfig getCacheConfig()
   {
   	return cache;
   }
   
   public Definition getDefinition() throws Exception
   {
      loadIfNecessary();
      return iface == null ? definition : definitionCache.get( url );
   }
   
   public boolean isLoaded()
   {
   	return loaded;
   }
   
   public synchronized boolean loadIfNecessary() throws Exception
   {
   	if( !loaded )
         load();
   	return loaded;
   }
   
   public synchronized void setDefinition( String url ) 
   {
   	if( !url.equals(this.url))
   	{
   		this.url = url;
   		this.cache = null;
   		loaded = iface != null && definitionCache.containsKey(url) && schemaCache.containsKey(url);
   	}
   }
     
   public synchronized boolean load() throws Exception
   {
		return load( null );
   }
   
   public synchronized boolean load( WsdlLoader wsdlLoader ) throws Exception
   {
   	if( !loaded && iface != null )
   		loaded = definitionCache.containsKey(url) && schemaCache.containsKey(url);
   	
   	if( loaded )
   		return true;
   	
   	// always use progressDialog since files can import http urls
      XProgressDialog progressDialog = UISupport.getDialogs().createProgressDialog(
               "Loading WSDL", 3, "Loading definition..", true );
      
   	Loader loader = new Loader( wsdlLoader );
      progressDialog.run( loader);
      
      // Get the value. It is the responsibility of the progressDialog to
      // wait for the other thread to finish.
      if( loader.hasError() )
      {
         if( loader.getError() instanceof SchemaException )
         {
         	schemaException = (SchemaException) loader.getError();
         	ArrayList<?> errorList = schemaException.getErrorList();
         	
         	log.error( "Error loading schema types from " + url + ", see log for details" );
         	
         	if( errorList != null )
         	{
	         	for( int c = 0; c < errorList.size(); c++ )
	         	{
	         		log.error( errorList.get( c ).toString() );
	         	}
         	}
         }
         else throw new Exception( loader.getError() );
      }
      else loaded = true;
      
      return loaded;
   }

   public SchemaTypeLoader getSchemaTypeLoader() throws Exception
   {
      loadIfNecessary();
      return iface != null ? schemaCache.get( url ) : schemaTypes;
   }
   
   public SchemaException getSchemaException()
   {
   	return schemaException;
   }

   private class Loader extends Worker.WorkerAdapter
   {
      private Throwable error;
      private WsdlLoader wsdlLoader;

      public Loader(WsdlLoader wsdlLoader) 
      {
          super();
          this.wsdlLoader = wsdlLoader;
      }

      private WsdlLoader getWsdlLoader() 
      {
          if(wsdlLoader != null) {
              return wsdlLoader;
          } else {
              return new UrlWsdlLoader( url );
          }
      }
      
      public boolean hasError()
		{
			return error != null;
		}

		public Object construct(XProgressMonitor monitor)
      {
      	try
      	{
				if( !validateCache( cache ) )
				{
				   monitor.setProgress( 1, "Caching definition from url [" + url + "]" ); 
				   
				   currentLoader = getWsdlLoader();
				   currentLoader.setProgressMonitor( monitor, 2 );
				   cache = iface == null ? WsdlUtils.cacheWsdl( currentLoader ) : 
				   		iface.cacheDefinition( currentLoader );
				   
				   if( currentLoader.isAborted() )
				   	throw new Exception( "Loading of WSDL from [" + url + "] was aborted" );
				}
      		 
            monitor.setProgress( 1, "Loading definition from " + (cache == null ? "url" : "cache") );
            
            log.debug( "Loading definition from " + (cache == null ? "url" : "cache") );
            currentLoader = cache == null ? getWsdlLoader() : new CachedWsdlLoader( cache );
				loadDefinitions( currentLoader);
            return null;
         }
         catch (Throwable e)
         {
         	log.error( "Loading of definition failed for [" + url + "]; " + e );
         	SoapUI.logError( e );
            this.error = e;
            return e;
         }
         finally
         {
            currentLoader = null;
         }
      }
      
      public Throwable getError()
      {
         return error;
      }
      
   	public boolean onCancel()
   	{
   		if( currentLoader == null )
   			return false;

   		return currentLoader.abort();
   	}
   }

   private void loadDefinitions( WsdlLoader loader ) throws Exception
   {
   	currentLoader = loader;
   	
   	if( factory == null )
   	{
	      factory = WSDLFactory.newInstance();
			wsdlReader = factory.newWSDLReader();
			wsdlReader.setFeature("javax.wsdl.verbose", true);
	      wsdlReader.setFeature("javax.wsdl.importDocuments", true);
   	}

   	definition = wsdlReader.readWSDL( loader );
      log.debug( "Loaded definition: " + (definition != null ? "ok" : "null") );

      if( iface != null )
      {
	      definitionCache.put( url, definition );
	      if( urlReferences.containsKey(url))
	      {
	      	urlReferences.put( url, urlReferences.get( url ) + 1 );
	      }
	      else
	      {
	      	urlReferences.put( url, 1 );
	      }
      }
      
      if( !currentLoader.isAborted() )
      {
      	schemaTypes = SchemaUtils.loadSchemaTypes(loader.getBaseURI(), soapVersion, loader);
      	if( iface != null )
      		schemaCache.put( url, schemaTypes);
      }
      else 
      	throw new Exception( "Loading of WSDL from [" + url + "] was aborted" );
      
      loaded = true;
   }
   
   public void release()
   {
   	if( iface != null && urlReferences.containsKey(url))
   	{
	   	Integer i = urlReferences.get(url);
	   	if( i.intValue() <= 1 )
	   	{
	   		urlReferences.remove(url);
	   		definitionCache.remove(url);
	   		schemaCache.remove( url );
	   	}
	   	else
	   	{
	   		urlReferences.put( url, i-1 );
	   	}
   	}
   }
   
	public boolean validateCache(DefinitionCacheConfig cache)
	{
		if( cache == null )
			return false;
		
		if( cache.getRootPart() == null )
			return false;
		
		if( cache.sizeOfPartArray() == 0 )
			return false;
		
		return true;
	}

	public SchemaTypeSystem getSchemaTypeSystem() throws Exception
	{
      if( !loaded )
         load();
		
		if( !schemaCache.containsKey(url) )
			return null;
		
		return schemaCache.get(url).findElement( soapVersion.getEnvelopeQName() ).getTypeSystem();
	}
   
   public boolean hasSchemaTypes() 
   {
      try
		{
			loadIfNecessary();
		}
		catch (Exception e)
		{
			SoapUI.logError(e);
			return false;
		}
      return schemaCache.containsKey( url );
   }

   public SchemaType findType(QName typeName) throws Exception
   {
      loadIfNecessary();
      return schemaCache.containsKey( url ) ? schemaCache.get( url ).findType( typeName ) : null;
   }

   public String getUrl()
   {
      return url;
   }

	public Collection<String> getDefinedNamespaces() throws Exception
	{
      loadIfNecessary();
      Set<String> namespaces = new HashSet<String>();
      
      SchemaTypeSystem schemaTypes = getSchemaTypeSystem();
		if( schemaTypes  != null )
      {
      	namespaces.addAll( SchemaUtils.extractNamespaces( getSchemaTypeSystem(), true ));
      }
      
		Definition definition = definitionCache.get( url );
      if( definition != null )
      	namespaces.add( definition.getTargetNamespace() );
      
      return namespaces;
	}

	public SoapVersion getSoapVersion()
	{
		return soapVersion;
	}

	public void setSoapVersion(SoapVersion soapVersion)
	{
		this.soapVersion = soapVersion;
	}

	public Map<String, XmlObject> getDefinitionParts() throws Exception
	{
		Map<String,XmlObject> result = new HashMap<String,XmlObject>();
		
		if( cache == null )
			return SchemaUtils.getDefinitionParts( new UrlWsdlLoader( url ));
		
		List<DefintionPartConfig> partList = cache.getPartList();
		for( DefintionPartConfig part : partList )
		{
			result.put( part.getUrl(), CachedWsdlLoader.getPartContent( cache, part ) );
		}
		
		return result;
	}

	public void setDefinitionCache(DefinitionCacheConfig definitionCache)
	{
		this.cache = definitionCache;
	}

	public void setInterface( WsdlInterface iface )
	{
		if( this.iface == null && iface != null )
		{
			if( definition != null )
				definitionCache.put(url, definition);
			else 
				loaded = false;
			
			if( schemaTypes != null )
				schemaCache.put( url, schemaTypes );
			else 
				loaded = false;
		}
		
		this.iface = iface;
	}

	public static void uncache(String url)
	{
		definitionCache.remove(url);
		schemaCache.remove(url);
		urlReferences.remove(url);
	}
}
