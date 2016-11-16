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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlHexBinary;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DataSource for XOP/MTOM attachments
 *
 * @author ole.matzura
 */

public final class XOPPartDataSource implements DataSource {
    private String content;
    private final String contentType;
    private final SchemaType schemaType;
    private File source;

    public XOPPartDataSource(String content, String contentType, SchemaType schemaType) {
        this.content = content;
        this.contentType = contentType;
        this.schemaType = schemaType;
    }

    public XOPPartDataSource(File source, String contentType, SchemaType schemaType) {
        this.source = source;
        this.contentType = contentType;
        this.schemaType = schemaType;
    }

    public String getContentType() {
        return StringUtils.isNullOrEmpty(contentType) ? ContentTypeHandler.DEFAULT_CONTENTTYPE : contentType;
    }

    public InputStream getInputStream() throws IOException {
        try {
            if (source != null) {
                return new FileInputStream(source);
            }
            if (SchemaUtils.isInstanceOf(schemaType, XmlHexBinary.type)) {
                return new ByteArrayInputStream(Hex.decodeHex(content.toCharArray()));
            } else if (SchemaUtils.isInstanceOf(schemaType, XmlBase64Binary.type)) {
                return new ByteArrayInputStream(Base64.decodeBase64(content.getBytes()));
            } else if (SchemaUtils.isAnyType(schemaType)) {
                return new ByteArrayInputStream(content.getBytes());
            } else {
                throw new IOException("Invalid type for XOPPartDataSource; " + schemaType.getName());
            }
        } catch (Exception e) {
            SoapUI.logError(e);
            throw new IOException(e.toString());
        }
    }

    public String getName() {
        return String.valueOf(schemaType.getName());
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
