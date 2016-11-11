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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CompressedStringConfig;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.Tools;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class for compressing/decompressing strings stored with
 * CompressedString
 *
 * @author ole.matzura
 */

public class CompressedStringSupport {
    public static String getString(CompressedStringConfig compressedStringConfig) {
        synchronized (compressedStringConfig) {
            String compression = compressedStringConfig.getCompression();
            if ("gzip".equals(compression)) {
                try {
                    byte[] bytes = Base64.decodeBase64(compressedStringConfig.getStringValue().getBytes());
                    GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes));
                    return Tools.readAll(in, -1).toString();
                } catch (IOException e) {
                    SoapUI.logError(e);
                }
            }

            return compressedStringConfig.getStringValue();
        }
    }

    public static void setString(CompressedStringConfig compressedStringConfig, String value) {
        synchronized (compressedStringConfig) {
            long limit = SoapUI.getSettings().getLong(WsdlSettings.COMPRESSION_LIMIT, 0);
            if (limit > 0 && value.length() >= limit) {
                try {
                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    GZIPOutputStream out = new GZIPOutputStream(byteOut);
                    out.write(value.getBytes());
                    out.finish();
                    value = new String(Base64.encodeBase64(byteOut.toByteArray()));
                    compressedStringConfig.setCompression("gzip");
                } catch (IOException e) {
                    SoapUI.logError(e);
                    compressedStringConfig.unsetCompression();
                }
            } else if (compressedStringConfig.isSetCompression()) {
                compressedStringConfig.unsetCompression();
            }

            compressedStringConfig.setStringValue(value);
        }
    }
}
