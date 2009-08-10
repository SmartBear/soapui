/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;

public class MimeMessageTestCase extends TestCase
{
   public void testMimeMessage() throws Exception
   {
   	Session session = Session.getDefaultInstance( new Properties() );
//    Instantiate a Multipart object
   	MimeMultipart mp = new MimeMultipart();
//   	 create the first bodypart object
   	MimeBodyPart b1 = new MimeBodyPart();
//   	 create textual content
//   	 and add it to the bodypart object
   	b1.setContent("Spaceport Map","text/plain");
   	mp.addBodyPart(b1);
//   	 Multipart messages usually have more than
//   	 one body part. Create a second body part
//   	 object, add new text to it, and place it
//   	 into the multipart message as well. This
//   	 second object holds postscript data.
   	MimeBodyPart b2 = new MimeBodyPart(); 
   	b2.setDataHandler( new DataHandler( new FileDataSource( "project.xml")) );
   	mp.addBodyPart(b2);
//   	 Create a new message object as described above,
//   	 and set its attributes. Add the multipart
//   	 object to this message and call saveChanges()
//   	 to write other message headers automatically.
   	Message msg = new MimeMessage(session);
//   	 Set message attrubutes as in a singlepart
//   	 message.
   	msg.setContent(mp); // add Multipart
   	msg.saveChanges(); // save changes
   }
}
