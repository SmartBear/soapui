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

package com.eviware.soapui.support.uri;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.util.ArrayList;

public class HttpParser {

    public static String readLine(InputStream inputStream, String charset) throws IOException {
        return IOUtils.toString(inputStream, charset);
    }

    public static Header[] parseHeaders(InputStream is, String charset) throws IOException, HttpException {
        ArrayList<Header> headers = new ArrayList<Header>();
        String name = null;
        StringBuffer value = null;
        for (; ; ) {
            String line = HttpParser.readLine(is, charset);
            if ((line == null) || (line.trim().length() < 1)) {
                break;
            }

            if ((line.charAt(0) == ' ') || (line.charAt(0) == '\t')) {
                if (value != null) {
                    value.append(' ');
                    value.append(line.trim());
                }
            } else {
                if (name != null) {
                    headers.add(new BasicHeader(name, value.toString()));
                }

                int colon = line.indexOf(":");
                if (colon < 0) {
                    throw new ProtocolException("Unable to parse header: " + line);
                }
                name = line.substring(0, colon).trim();
                value = new StringBuffer(line.substring(colon + 1).trim());
            }

        }

        if (name != null) {
            headers.add(new BasicHeader(name, value.toString()));
        }

        return (Header[]) headers.toArray(new Header[headers.size()]);
    }

}
