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

package com.eviware.soapui.impl.rest.support.handlers;

import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

public class JsonMediaTypeHandler implements MediaTypeHandler
{
   public boolean canHandle( String contentType )
   {
      return couldBeJsonContent( contentType );
   }

   public static boolean couldBeJsonContent( String contentType )
   {
      return contentType != null && (contentType.contains( "javascript" ) || contentType.contains( "json"));
   }

   public String createXmlRepresentation( HttpResponse response )
   {
      try
      {
         String content = response.getContentAsString();
         if( !StringUtils.hasContent( content ) )
            return null;

         JSONObject json = JSONObject.fromObject( content );
         XMLSerializer serializer = new XMLSerializer();
         serializer.setTypeHintsEnabled( false );
         content = serializer.write( json );
         content = XmlUtils.prettyPrintXml( content );

         return content;
      }
      catch( Throwable e )
      {
         e.printStackTrace();
      }
      return null;
   }
}
