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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import org.apache.log4j.Logger;

public class HttpCompressionRequestFilter extends AbstractRequestFilter
{
   private final static Logger log = Logger.getLogger( HttpCompressionRequestFilter.class );

   @Override
   public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> httpRequest )
   {
      String requestContent = ( String ) context.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );
      if( requestContent == null )
      {
         log.warn( "Missing request content in context, skipping property expansion" );
      }
      else
      {
         Settings settings = httpRequest.getSettings();
         String compressionAlg = settings.getString( HttpSettings.REQUEST_COMPRESSION, "None" );
         if( !"None".equals( compressionAlg ) )
         {
            try
            {
               requestContent = new String( CompressionSupport.compress( compressionAlg, requestContent.getBytes() ) );
            }
            catch( Exception e )
            {
               e.printStackTrace();
            }
         }

         if( requestContent != null )
         {
            context.setProperty( BaseHttpRequestTransport.REQUEST_CONTENT, requestContent );
         }
      }

   }
}
