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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class AttachmentUtilsTest {
    @Test
    public void actionIsNotWithinTypeElement() throws Exception {
        String contentType = AttachmentUtils.buildRootPartContentType("SendFile", SoapVersion.Soap12);

        String expectedContentType = "application/xop+xml; charset=UTF-8; type=\"application/soap+xml\"; action=\"SendFile\"";
        assertThat(contentType, is(expectedContentType));

    }

    @Test
    public void actionIsNotPartOfTypeElementInMTOMContentType() {
        String contentType = AttachmentUtils.buildMTOMContentType("boundary=\"----=_Part_10_7396679.1285664994648\"", "SendFile", SoapVersion.Soap12);
        String expectedContentType = "multipart/related; type=\"application/xop+xml\"; " +
                "start=\"<rootpart@soapui.org>\"; start-info=\"application/soap+xml\"; action=\"SendFile\"; " +
                "boundary=\"----=_Part_10_7396679.1285664994648\"";
        assertThat(contentType, is(expectedContentType));
    }
}
