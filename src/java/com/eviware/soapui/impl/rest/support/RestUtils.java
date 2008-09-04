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
import com.eviware.soapui.support.types.StringList;
import com.sun.research.wadl.x2006.x10.ApplicationDocument;
import com.sun.research.wadl.x2006.x10.ApplicationDocument.Application;
import com.sun.research.wadl.x2006.x10.DocDocument.Doc;
import com.sun.research.wadl.x2006.x10.MethodDocument.Method;
import com.sun.research.wadl.x2006.x10.ParamDocument.Param;
import com.sun.research.wadl.x2006.x10.RepresentationType;
import com.sun.research.wadl.x2006.x10.ResourceDocument.Resource;
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

public class RestUtils
{
   public static String[] extractTemplateParams( String path )
   {
      StringList result = new StringList();

      int ix = path.indexOf( '{' );
      while( ix != -1 )
      {
         int endIx = path.indexOf( '}', ix );
         if( endIx == -1 )
            break;

         if( endIx > ix + 1 )
            result.add( path.substring( ix + 1, endIx ) );

         ix = path.indexOf( '{', ix + 1 );
      }

      return result.toStringArray();

   }

   public static void initFromWadl( RestService service, String wadlUrl )
   {
      try
      {
         XmlObject xmlObject = XmlObject.Factory.parse( new URL( wadlUrl ) );

         Element element = ((Document) xmlObject.getDomNode()).getDocumentElement();

         if( element.getLocalName().equals( "application" ) && element.getNamespaceURI().startsWith( "http://research.sun.com/wadl" ) &&
             !element.getNamespaceURI().equals( Constants.WADL10_NS ))
         {
            String content = xmlObject.xmlText();
            content = content.replaceAll( "\"" + element.getNamespaceURI() + "\"", "\"" + Constants.WADL10_NS + "\"" );
            xmlObject = ApplicationDocument.Factory.parse( content );
         }
         else if( !element.getLocalName().equals( "application" ) || !element.getNamespaceURI().equals( Constants.WADL10_NS ) )
         {
            throw new Exception( "Document is not a WADL application with " + Constants.WADL10_NS + " namespace" );
         }

         ApplicationDocument applicationDocument = (ApplicationDocument) xmlObject.changeType( ApplicationDocument.type );
         Application application = applicationDocument.getApplication();

         Resources resources = application.getResources();
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
            initResourceFromWadlResource( newResource, resource, application );

            addSubResources( newResource, resource, application );
         }
      }
      catch( Exception e )
      {
         UISupport.showErrorMessage( e );
      }
   }

   private static void addSubResources( RestResource newResource, Resource resource, Application application )
   {
      for( Resource res : resource.getResourceList() )
      {
         String name = getFirstTitle( res.getDocList(), res.getPath() );
         String path = res.getPath();

         RestResource newRes = newResource.addNewResource( name, path );
         initResourceFromWadlResource( newRes, res, application );

         addSubResources( newRes, res, application );
      }
   }

   private static String getFirstTitle( List<Doc> list, String defaultTitle )
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

   private static void initResourceFromWadlResource( RestResource newResource, Resource resource, Application application )
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
         method = resolveMethod( method, application );

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
      }
   }

   private static void initParam( Param param, RestParamProperty prop )
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

   private static Method resolveMethod( Method method, Application application )
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
