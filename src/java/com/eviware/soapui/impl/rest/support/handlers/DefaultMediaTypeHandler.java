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
import com.eviware.soapui.support.xml.XmlUtils;

public class DefaultMediaTypeHandler implements MediaTypeHandler
{
   public boolean canHandle( String contentType )
   {
      return true;
   }

   public String createXmlRepresentation( HttpResponse response )
   {
      String content = response.getContentAsString();
      if( XmlUtils.seemsToBeXml( content ) )
         return content;
      else
         return null; // "<data><![CDATA[" + content + "]]></data>";
   }
}
