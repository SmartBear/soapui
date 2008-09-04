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

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.RestParamProperty;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.impl.wsdl.support.UrlSchemaLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.sun.research.wadl.x2006.x10.ApplicationDocument;
import com.sun.research.wadl.x2006.x10.ApplicationDocument.Application;
import com.sun.research.wadl.x2006.x10.DocDocument.Doc;
import com.sun.research.wadl.x2006.x10.MethodDocument.Method;
import com.sun.research.wadl.x2006.x10.ParamDocument.Param;
import com.sun.research.wadl.x2006.x10.RepresentationType;
import com.sun.research.wadl.x2006.x10.ResourceDocument.Resource;
import com.sun.research.wadl.x2006.x10.ResourceTypeDocument;
import com.sun.research.wadl.x2006.x10.ResourcesDocument.Resources;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WadlImporter
{
   private RestService service;
   private Application application;
   private Resources resources;

   public WadlImporter( RestService service )
   {
      this.service = service;
   }

   public void initFromWadl( String wadlUrl )
   {
      try
      {
         XmlObject xmlObject = XmlObject.Factory.parse( new URL( wadlUrl ) );

         Element element = ((Document) xmlObject.getDomNode()).getDocumentElement();

         if( element.getLocalName().equals( "application" ) && element.getNamespaceURI().startsWith( "http://research.sun.com/wadl" ) &&
                 !element.getNamespaceURI().equals( Constants.WADL10_NS ) )
         {
            String content = xmlObject.xmlText();
            content = content.replaceAll( "\"" + element.getNamespaceURI() + "\"", "\"" + Constants.WADL10_NS + "\"" );
            xmlObject = ApplicationDocument.Factory.parse( content );
         }
         else
         if( !element.getLocalName().equals( "application" ) || !element.getNamespaceURI().equals( Constants.WADL10_NS ) )
         {
            throw new Exception( "Document is not a WADL application with " + Constants.WADL10_NS + " namespace" );
         }

         ApplicationDocument applicationDocument = (ApplicationDocument) xmlObject.changeType( ApplicationDocument.type );
         application = applicationDocument.getApplication();

         resources = application.getResources();
         String base = resources.getBase();

         try
         {
            URL baseUrl = new URL( base );
            service.setBasePath( baseUrl.getPath() );

            service.addEndpoint( Tools.getEndpointFromUrl( baseUrl ) );
         }
         catch( Exception e )
         {
            service.setBasePath( base );
         }

         service.setWadlUrl( wadlUrl );

         for( Resource resource : resources.getResourceList() )
         {
            String name = getFirstTitle( resource.getDocList(), resource.getPath() );
            String path = resource.getPath();

            RestResource newResource = service.addNewResource( name, path );
            initResourceFromWadlResource( newResource, resource );

            addSubResources( newResource, resource );
         }
      }
      catch( Exception e )
      {
         UISupport.showErrorMessage( e );
      }
   }

   private void addSubResources( RestResource newResource, Resource resource )
   {
      for( Resource res : resource.getResourceList() )
      {
         String name = getFirstTitle( res.getDocList(), res.getPath() );
         String path = res.getPath();

         RestResource newRes = newResource.addNewResource( name, path );
         initResourceFromWadlResource( newRes, res );

         addSubResources( newRes, res );
      }
   }

   private String getFirstTitle( List<Doc> list, String defaultTitle )
   {
      for( Doc doc : list )
      {
         if( StringUtils.hasContent( doc.getTitle() ) )
         {
            return doc.getTitle();
         }
      }
      return defaultTitle;
   }

   private void initResourceFromWadlResource( RestResource newResource, Resource resource )
   {
      for( Param param : resource.getParamList() )
      {
         String nm = param.getName();
         RestParamProperty prop = newResource.hasProperty( nm ) ?
                 newResource.getProperty( nm ) : newResource.addProperty( nm );

         initParam( param, prop );
      }

      for( Method method : resource.getMethodList() )
      {
         method = resolveMethod( method );

         initMethod( newResource, method );
      }

      List types = resource.getType();
      if( types != null && types.size() > 0 )
      {
         for( Object obj : types )
         {
            ResourceTypeDocument.ResourceType type = resolveResource( obj.toString() );
            if( type != null )
            {
               for( Method method : type.getMethodList() )
               {
                  RestRequest restRequest = initMethod( newResource, method );

                  for( Param param : type.getParamList() )
                  {
                     String nm = param.getName();
                     RestParamProperty prop = restRequest.hasProperty( nm ) ?
                             restRequest.getProperty( nm ) : restRequest.addProperty( nm );

                     initParam( param, prop );
                  }
               }
            }
         }
      }
   }

   private RestRequest initMethod( RestResource newResource, Method method )
   {
      String name = method.getName();
      if( StringUtils.hasContent( method.getId() ) )
         name += " - " + method.getId();

      RestRequest request = newResource.addNewRequest( getFirstTitle( method.getDocList(), name ) );
      request.setMethod( RestRequest.RequestMethod.valueOf( method.getName() ) );

      if( method.getRequest() != null )
      {
         for( Param param : method.getRequest().getParamList() )
         {
            RestParamProperty p = request.addProperty( param.getName() );
            initParam( param, p );
         }
      }

      if( method.getResponse() != null )
      {
         for( RepresentationType representationType : method.getResponse().getRepresentationList() )
         {
            RestRepresentation restRepresentation = request.addNewRepresentation( RestRepresentation.Type.RESPONSE );
            restRepresentation.setMediaType( representationType.getMediaType() );
            restRepresentation.setStatus( representationType.getStatus() );
         }

         for( RepresentationType representationType : method.getResponse().getFaultList() )
         {
            RestRepresentation restRepresentation = request.addNewRepresentation( RestRepresentation.Type.FAULT );
            restRepresentation.setMediaType( representationType.getMediaType() );
            restRepresentation.setStatus( representationType.getStatus() );
         }
      }

      return request;
   }

   private void initParam( Param param, RestParamProperty prop )
   {
      prop.setDefaultValue( param.getDefault() );
      prop.setValue( param.getDefault() );
      prop.setStyle( ParameterStyle.valueOf( param.getStyle().toString().toUpperCase() ) );
      prop.setRequired( param.getRequired() );
      prop.setType( param.getType() );

      String[] options = new String[param.sizeOfOptionArray()];
      for( int c = 0; c < options.length; c++ )
         options[c] = param.getOptionArray( c ).getValue();

      if( options.length > 0 )
         prop.setOptions( options );
   }

   private Method resolveMethod( Method method )
   {
      String href = method.getHref();
      if( !StringUtils.hasContent( href ) )
         return method;

      for( Method m : application.getMethodList() )
      {
         if( m.getId().equals( href.substring( 1 ) ) )
            return m;
      }

      return method;
   }

   private RepresentationType resolveRepresentation( RepresentationType representation )
   {
      String href = representation.getHref();
      if( !StringUtils.hasContent( href ) )
         return representation;

      for( RepresentationType m : application.getRepresentationList() )
      {
         if( m.getId().equals( href.substring( 1 ) ) )
            return m;
      }

      return representation;
   }

   private ResourceTypeDocument.ResourceType resolveResource( String id )
   {
      for( ResourceTypeDocument.ResourceType resourceType : application.getResourceTypeList() )
      {
         if( resourceType.getId().equals( id ) )
            return resourceType;
      }

      return null;
   }

   public static Map<String, XmlObject> getDefinitionParts( String wadlUrl )
   {
      Map<String, XmlObject> result = new HashMap<String, XmlObject>();

      try
      {
         return SchemaUtils.getSchemas( wadlUrl, new UrlSchemaLoader( wadlUrl ) );

//         URL url = new URL(wadlUrl);
//			ApplicationDocument applicationDocument = ApplicationDocument.Factory.parse(url);
//			result.put(url.getPath(), applicationDocument);
      }
      catch( Exception e )
      {
         e.printStackTrace();
      }

      return result;
   }

   public static String extractParams( URL param, XmlBeansRestParamsTestPropertyHolder params )
   {
      String path = param.getPath();
      String[] items = path.split( "/" );

      int templateParamCount = 0;
      StringBuffer resultPath = new StringBuffer();

      for( int i = 0; i < items.length; i++ )
      {
         String item = items[i];
         try
         {
            String[] matrixParams = item.split( ";" );
            if( matrixParams.length > 0 )
            {
               item = matrixParams[0];
               for( int c = 1; c < matrixParams.length; c++ )
               {
                  String matrixParam = matrixParams[c];

                  int ix = matrixParam.indexOf( '=' );
                  if( ix == -1 )
                  {
                     params.addProperty( URLDecoder.decode( matrixParam, "Utf-8" ) ).setStyle( ParameterStyle.MATRIX );
                  }
                  else
                  {
                     String name = matrixParam.substring( 0, ix );
                     RestParamProperty property = params.addProperty( URLDecoder.decode( name, "Utf-8" ) );
                     property.setStyle( ParameterStyle.MATRIX );
                     property.setValue( URLDecoder.decode( matrixParam.substring( ix + 1 ), "Utf-8" ) );
                  }
               }
            }

            Integer.parseInt( item );
            RestParamProperty prop = params.addProperty( "param" + templateParamCount++ );
            prop.setStyle( ParameterStyle.TEMPLATE );
            prop.setValue( item );

            item = "{" + prop.getName() + "}";
         }
         catch( Exception e )
         {
         }

         if( StringUtils.hasContent( item ) )
            resultPath.append( '/' ).append( item );
      }

      String query = ((URL) param).getQuery();
      if( StringUtils.hasContent( query ) )
      {
         items = query.split( "&" );
         for( String item : items )
         {
            try
            {
               int ix = item.indexOf( '=' );
               if( ix == -1 )
               {
                  params.addProperty( URLDecoder.decode( item, "Utf-8" ) ).setStyle( ParameterStyle.QUERY );
               }
               else
               {
                  String name = item.substring( 0, ix );
                  RestParamProperty property = params.addProperty( URLDecoder.decode( name, "Utf-8" ) );
                  property.setStyle( ParameterStyle.QUERY );
                  property.setValue( URLDecoder.decode( item.substring( ix + 1 ), "Utf-8" ) );
               }
            }
            catch( UnsupportedEncodingException e )
            {
               e.printStackTrace();
            }
         }
      }

      return resultPath.toString();
   }
}