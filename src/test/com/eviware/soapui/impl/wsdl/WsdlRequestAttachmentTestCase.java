/*
 * soapUI, copyright (C) 2004-2008 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.model.iface.Attachment;
import junit.framework.TestCase;

import java.io.File;

public class WsdlRequestAttachmentTestCase extends TestCase
{
   public void testAttachmentTypes() throws Exception
   {
      WsdlProject wsdlProject = new WsdlProject();
      File file = new File( "src\\test-resources\\MtomTest.wsdl" );
      WsdlInterface[] wsdlInterfaces = WsdlImporter.importWsdl( wsdlProject, file.toURI().toURL().toString() );
      WsdlOperation operation = (WsdlOperation) wsdlInterfaces[0].getOperationByName( "invoke" );

      WsdlRequest request = operation.addNewRequest( "request1" );
      request.setMtomEnabled( true );
      request.setRequestContent( "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://adobe.com/idp/services\">\n" +
              "   <soapenv:Header/>\n" +
              "   <soapenv:Body>\n" +
              "      <ser:invoke>\n" +
              "         <!--Optional:-->\n" +
              "         <ser:inDoc>\n" +
              "            <!--Optional:-->\n" +
              "            <ser:MTOM>cid:135595967796</ser:MTOM>\n" +
              "            <!--Optional:-->\n" +
              "         </ser:inDoc>\n" +
              "      </ser:invoke>\n" +
              "   </soapenv:Body>\n" +
              "</soapenv:Envelope>" );

      HttpAttachmentPart[] definedAttachmentParts = request.getDefinedAttachmentParts();
      assertTrue( definedAttachmentParts.length == 2  );
      assertEquals( definedAttachmentParts[0].getAttachmentType(), Attachment.AttachmentType.XOP );
      assertEquals( definedAttachmentParts[1].getAttachmentType(), Attachment.AttachmentType.UNKNOWN );
   }

}
