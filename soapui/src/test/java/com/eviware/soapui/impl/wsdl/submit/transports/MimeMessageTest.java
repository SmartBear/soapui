/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.submit.transports;

import org.junit.Test;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class MimeMessageTest {

    @Test
    public void savedWithoutError() throws Exception {
        Session session = Session.getDefaultInstance(new Properties());
        // Instantiate a Multipart object
        MimeMultipart mp = new MimeMultipart();
        // create the first bodypart object
        MimeBodyPart b1 = new MimeBodyPart();
        // create textual content
        // and add it to the bodypart object
        b1.setContent("Spaceport Map", "text/plain");
        mp.addBodyPart(b1);
        // Multipart messages usually have more than
        // one body part. Create a second body part
        // object, add new text to it, and place it
        // into the multipart message as well. This
        // second object holds postscript data.
        MimeBodyPart b2 = new MimeBodyPart();
        b2.setDataHandler(new DataHandler(new FileDataSource("project.xml")));
        mp.addBodyPart(b2);
        // Create a new message object as described above,
        // and set its attributes. Add the multipart
        // object to this message and call saveChanges()
        // to write other message headers automatically.
        Message msg = new MimeMessage(session);
        // Set message attrubutes as in a singlepart
        // message.
        msg.setContent(mp); // add Multipart
        msg.saveChanges(); // save changes
    }
}
