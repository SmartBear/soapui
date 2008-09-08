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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.types.StringToStringMap;
import org.apache.commons.httpclient.Header;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

public abstract class BaseHttpResponse implements HttpResponse
{
   private StringToStringMap requestHeaders;
   private StringToStringMap responseHeaders;

   private long timeTaken;
   private long timestamp;
   private String contentType;
   private int statusCode;
   private SSLInfo sslInfo;
   private URL url;
   private WeakReference<AbstractHttpRequest<?>> httpRequest;
   private AbstractHttpRequest.RequestMethod method;
   private String version;
   private StringToStringMap properties;
   private ByteArrayOutputStream rawRequestData = new ByteArrayOutputStream();
   private ByteArrayOutputStream rawResponseData = new ByteArrayOutputStream();

   public BaseHttpResponse( ExtendedHttpMethod httpMethod, AbstractHttpRequest<?> httpRequest )
   {
      this.httpRequest = new WeakReference<AbstractHttpRequest<?>>( httpRequest );
      this.timeTaken = httpMethod.getTimeTaken();

      method = httpMethod.getMethod();
      version = httpMethod.getParams().getVersion().toString();

      Settings settings = httpRequest.getSettings();
      if( settings.getBoolean( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN ) )
      {
         try
         {
            httpMethod.getResponseBody();
         }
         catch( IOException e )
         {
            e.printStackTrace();
         }
         timeTaken += httpMethod.getResponseReadTime();
      }

      this.timestamp = System.currentTimeMillis();
      this.contentType = httpMethod.getResponseContentType();
      this.statusCode = httpMethod.getStatusCode();
      this.sslInfo = httpMethod.getSSLInfo();

      try
      {
         this.url = new URL( httpMethod.getURI().toString() );
      }
      catch( Exception e )
      {
         e.printStackTrace();
      }

      initHeaders( httpMethod );
   }

   protected void initHeaders( ExtendedHttpMethod httpMethod )
   {
      try
      {
         rawResponseData.write( httpMethod.getStatusLine().toString().getBytes() );
         rawResponseData.write( "\r\n".getBytes() );
         rawRequestData.write( ( method + " " + url.toString() + " " + version + "\r\n" ).getBytes() );

         requestHeaders = new StringToStringMap();
         Header[] headers = httpMethod.getRequestHeaders();
         for( Header header : headers )
         {
            requestHeaders.put( header.getName(), header.getValue() );
            rawRequestData.write( header.toExternalForm().getBytes() );
         }

         responseHeaders = new StringToStringMap();
         headers = httpMethod.getResponseHeaders();
         for( Header header : headers )
         {
            responseHeaders.put( header.getName(), header.getValue() );
            rawResponseData.write( header.toExternalForm().getBytes() );
         }

         responseHeaders.put( "#status#", httpMethod.getStatusLine().toString() );

         if( httpMethod.getRequestEntity() != null )
         {
            rawRequestData.write( "\r\n".getBytes() );
            if( httpMethod.getRequestEntity().isRepeatable() )
               httpMethod.getRequestEntity().writeRequest( rawRequestData );
            else
               rawRequestData.write( "<request data not available>".getBytes() );
         }

         rawResponseData.write( "\r\n".getBytes() );
         rawResponseData.write( httpMethod.getResponseBody() );
      }
      catch( Exception e )
      {
         e.printStackTrace();
      }
   }

   public StringToStringMap getRequestHeaders()
   {
      return requestHeaders;
   }

   public StringToStringMap getResponseHeaders()
   {
      return responseHeaders;
   }

   public long getTimeTaken()
   {
      return timeTaken;
   }

   public SSLInfo getSSLInfo()
   {
      return sslInfo;
   }

   public long getTimestamp()
   {
      return timestamp;
   }

   public String getContentType()
   {
      return contentType;
   }

   public URL getURL()
   {
      return url;
   }

   public AbstractHttpRequest<?> getRequest()
   {
      return httpRequest.get();
   }

   public int getStatusCode()
   {
      return statusCode;
   }

   public Attachment[] getAttachments()
   {
      return new Attachment[0];
   }

   public Attachment[] getAttachmentsForPart( String partName )
   {
      return new Attachment[0];
   }

   public byte[] getRawRequestData()
   {
      return rawRequestData.toByteArray();
   }

   public byte[] getRawResponseData()
   {
      return rawResponseData.toByteArray();
   }

   public AbstractHttpRequest.RequestMethod getMethod()
   {
      return method;
   }

   public String getHttpVersion()
   {
      return version;
   }

   public void setProperty( String name, String value )
   {
      if( properties == null )
         properties = new StringToStringMap();

      properties.put( name, value );
   }

   public String getProperty( String name )
   {
      return properties == null ? null : properties.get( name );
   }

   public String[] getPropertyNames()
   {
      return properties == null ? new String[0] : properties.getKeys();
   }
}
