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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.model.support.AbstractResponse;
import com.eviware.soapui.support.xml.XmlUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcResponse extends AbstractResponse<JdbcRequest> {
    private String responseContent;
    private long timeTaken;
    private long timestamp;
    private final String rawSql;

    public JdbcResponse(JdbcRequest request, Statement statement, String rawSql) throws SQLException,
            ParserConfigurationException, TransformerConfigurationException, TransformerException {
        super(request);
        this.rawSql = rawSql;

        responseContent = XmlUtils.createJdbcXmlResultEx(statement, request.getTestStep().isConvertColumnNamesToUpperCase());
    }

    public String getContentAsString() {
        return responseContent;
    }

    public String getContentType() {
        return "text/xml";
    }

    @Override
    public byte[] getRawRequestData() {
        return rawSql.getBytes();
    }

    public String getRequestContent() {
        return getRequest().getTestStep().getQuery();
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setContentAsString(String xml) {
        responseContent = xml;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
