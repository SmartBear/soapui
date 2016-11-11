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

package com.eviware.soapui.impl.wsdl.support.soap;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.FaultDocument;

import javax.xml.namespace.QName;
import java.io.IOException;

/**
 * SoapVersion for SOAP 1.2
 *
 * @author ole.matzura
 */

public class SoapVersion12 extends AbstractSoapVersion {
    private final static QName envelopeQName = new QName(Constants.SOAP12_ENVELOPE_NS, "Envelope");
    private final static QName bodyQName = new QName(Constants.SOAP12_ENVELOPE_NS, "Body");
    private final static QName faultQName = new QName(Constants.SOAP11_ENVELOPE_NS, "Fault");
    private final static QName headerQName = new QName(Constants.SOAP12_ENVELOPE_NS, "Header");
    public final static SoapVersion12 instance = new SoapVersion12();

    private SchemaTypeLoader soapSchema;
    private XmlObject soapSchemaXml;
    private XmlObject soapEncodingXml;

    private SoapVersion12() {
        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

        try {
            // soapSchemaXml = XmlObject.Factory.parse( SoapUI.class
            // .getResource(
            // "/com/eviware/soapui/resources/xsds/soapEnvelope12.xsd" ) );
            soapSchemaXml = XmlUtils.createXmlObject(SoapUI.class
                    .getResource("/com/eviware/soapui/resources/xsds/soapEnvelope12.xsd"));
            soapSchema = XmlBeans.loadXsd(new XmlObject[]{soapSchemaXml});
            // soapEncodingXml = XmlObject.Factory.parse( SoapUI.class
            // .getResource(
            // "/com/eviware/soapui/resources/xsds/soapEncoding12.xsd" ) );
            soapEncodingXml = XmlUtils.createXmlObject(SoapUI.class
                    .getResource("/com/eviware/soapui/resources/xsds/soapEncoding12.xsd"));
        } catch (Exception e) {
            SoapUI.logError(e);
        } finally {
            state.restore();
        }
    }

    public String getEncodingNamespace() {
        return "http://www.w3.org/2003/05/soap-encoding";
    }

    public XmlObject getSoapEncodingSchema() throws XmlException, IOException {
        return soapEncodingXml;
    }

    public XmlObject getSoapEnvelopeSchema() throws XmlException, IOException {
        return soapSchemaXml;
    }

    public String getEnvelopeNamespace() {
        return Constants.SOAP12_ENVELOPE_NS;
    }

    public SchemaType getEnvelopeType() {
        return EnvelopeDocument.type;
    }

    public String toString() {
        return "SOAP 1.2";
    }

    public String getContentTypeHttpHeader(String encoding, String soapAction) {
        String result = getContentType();

        if (encoding != null && encoding.trim().length() > 0) {
            result += ";charset=" + encoding;
        }

        if (StringUtils.hasContent(soapAction)) {
            result += ";action=" + StringUtils.quote(soapAction);
        }

        return result;
    }

    public String getSoapActionHeader(String soapAction) {
        // SOAP 1.2 has this in the contenttype
        return null;
    }

    public String getContentType() {
        return "application/soap+xml";
    }

    public QName getBodyQName() {
        return bodyQName;
    }

    public QName getEnvelopeQName() {
        return envelopeQName;
    }

    public QName getHeaderQName() {
        return headerQName;
    }

    protected SchemaTypeLoader getSoapEnvelopeSchemaLoader() {
        return soapSchema;
    }

    public static QName getFaultQName() {
        return faultQName;
    }

    public SchemaType getFaultType() {
        return FaultDocument.type;
    }

    public String getName() {
        return "SOAP 1.2";
    }

    public String getFaultDetailNamespace() {
        return getEnvelopeNamespace();
    }
}
