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

import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.config.RestServiceConfig;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.DefinitionContext;
import com.eviware.soapui.impl.wadl.WadlDefinitionContext;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WSDL implementation of Interface, maps to a WSDL Binding
 *
 * @author Ole.Matzura
 */

public class RestService extends AbstractInterface<RestServiceConfig> implements RestResourceContainer
{
   private List<RestResource> resources = new ArrayList<RestResource>();
   private WadlDefinitionContext wadlContext;

   public RestService( WsdlProject project, RestServiceConfig serviceConfig )
   {
      super( serviceConfig, project, "/rest_service.gif" );

      List<RestResourceConfig> resourceConfigs = serviceConfig.getResourceList();
      for( int i = 0; i < resourceConfigs.size(); i++ )
      {
         resources.add( new RestResource( this, resourceConfigs.get( i ) ) );
      }
   }

   public String getInterfaceType()
   {
      return RestServiceFactory.REST_TYPE;
   }

   public RestResource getOperationAt( int index )
   {
      return resources.get( index );
   }

   public RestResource getOperationByName( String name )
   {
      return (RestResource) getWsdlModelItemByName( resources, name );
   }

   public int getOperationCount()
   {
      return resources.size();
   }

   public List<Operation> getOperationList()
   {
      return new ArrayList<Operation>( resources );
   }

   public String getBasePath()
   {
      return getConfig().isSetBasePath() ? getConfig().getBasePath() : "";
   }

   public void setBasePath( String basePath )
   {
      String old = getBasePath();
      getConfig().setBasePath( basePath );

      notifyPropertyChanged( "basePath", old, basePath );
   }

   public boolean isGenerated()
   {
      return StringUtils.isNullOrEmpty( getConfig().getDefinitionUrl() );
   }

   public String getWadlUrl()
   {
      return isGenerated() ? generateWadlUrl() : getConfig().getDefinitionUrl();
   }

   public String generateWadlUrl()
   {
      return getName() + ".wadl";
   }

   public void setWadlUrl( String wadlUrl )
   {
      String old = getWadlUrl();
      getConfig().setDefinitionUrl( wadlUrl );

      notifyPropertyChanged( "wadlUrl", old, wadlUrl );
   }

   public String getTechnicalId()
   {
      return getConfig().getBasePath();
   }

   public RestResource addNewResource( String name, String path )
   {
      RestResourceConfig resourceConfig = getConfig().addNewResource();
      resourceConfig.setName( name );
      resourceConfig.setPath( path );

      RestResource resource = new RestResource( this, resourceConfig );
      resources.add( resource );

      fireOperationAdded( resource );
      return resource;
   }

   public RestResource cloneResource( RestResource resource, String name )
   {
      RestResourceConfig resourceConfig = (RestResourceConfig) getConfig().addNewResource().set( resource.getConfig() );
      resourceConfig.setName( name );

      RestResource newResource = new RestResource( this, resourceConfig );
      resources.add( newResource );

      fireOperationAdded( newResource );
      return newResource;
   }

   public void deleteResource( RestResource resource )
   {
      int ix = resources.indexOf( resource );
      if( !resources.remove( resource ) )
         return;

      fireOperationRemoved( resource );

      getConfig().removeResource( ix );
      resource.release();
   }

   public List<RestResource> getAllResources()
   {
      List<RestResource> result = new ArrayList<RestResource>();
      for( RestResource resource : resources )
      {
         addResourcesToResult( resource, result );
      }

      return result;
   }

    public Map<String, RestResource> getResources()
   {
      Map<String,RestResource> result = new HashMap<String,RestResource>();

      for( RestResource resource : getAllResources() )
      {
         result.put( resource.getFullPath( false ), resource );
      }

      return result;
   }

   private void addResourcesToResult( RestResource resource, List<RestResource> result )
   {
      result.add( resource );

      for( RestResource res : resource.getChildResourceList() )
      {
         addResourcesToResult( res, result );
      }
   }

   public RestResource getResourceByPath( String resourcePath )
   {
      for( RestResource resource : getAllResources() )
      {
         if( resource.getPath().equals( resourcePath ) )
            return resource;
      }

      return null;
   }

   @Override
   public DefinitionContext getDefinitionContext()
   {
      return getWadlContext();
   }

   public WadlDefinitionContext getWadlContext()
   {
      if( wadlContext == null )
         wadlContext = new WadlDefinitionContext( getWadlUrl(), this );

      return wadlContext;
   }

   @Override
   public String getDefinition()
   {
      return getWadlUrl();
   }

   public String getType()
   {
      return RestServiceFactory.REST_TYPE;
   }

   public boolean isDefinitionShareble()
   {
      return !isGenerated();
   }

   public void beforeSave()
   {
      if( isGenerated() && wadlContext != null )
      {
         try
         {
            wadlContext.getDefinitionCache().clear();
         }
         catch( Exception e )
         {
            e.printStackTrace();  
         }
      }

   }
}
