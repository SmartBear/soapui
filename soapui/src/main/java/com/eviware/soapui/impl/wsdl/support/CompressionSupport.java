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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public class CompressionSupport {
    public static final String ALG_GZIP = "gzip";
    public static final String ALG_DEFLATE = "deflate";
    private static final String[] algs = {ALG_GZIP, ALG_DEFLATE};

    public static String getAvailableAlgorithms(String separator) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < algs.length; ++i) {
            if (i > 0) {
                buf.append(separator);
            }
            buf.append(algs[i]);
        }

        return buf.toString();
    }

    public static String getAvailableAlgorithm(String httpContentEncoding) {
        for (String alg : algs) {
            if (httpContentEncoding.toLowerCase().endsWith(alg)) {
                return alg;
            }
        }

        return null;
    }

    private static void checkAlg(String alg) throws Exception {
        if (!ALG_GZIP.equals(alg) && !ALG_DEFLATE.equals(alg)) {
            throw new Exception("Compression algorithm not supported: " + alg);
        }
    }

    public static byte[] compress(String alg, byte[] content) throws Exception {
        checkAlg(alg);
        if (ALG_GZIP.equals(alg)) {
            return GZIPCompress(content);
        } else if (ALG_DEFLATE.equals(alg)) {
            return DeflaterCompress(content);
        } else {
            return null;
        }
    }

    public static byte[] decompress(String alg, byte[] content) throws Exception {
        // Use the excellent content encoding handling that exists in HTTP Client
        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new HttpVersion(1, 0), 0, null));
        ByteArrayEntity entity = new ByteArrayEntity(content);
        entity.setContentEncoding(alg);
        response.setEntity(entity);
        new ResponseContentEncoding().process(response, null);
        return IOUtils.toByteArray(response.getEntity().getContent());
    }

    // createCompressionInputStream can be used in the future if
    // PipedInputStreams are used
    // for sending compressed data instead of creating compressed byte array
    // first and then sending
    public static InputStream createCompressionInputStream(String alg, byte[] content) throws Exception {
        checkAlg(alg);
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        if (ALG_GZIP.equals(alg)) {
            return new GZIPInputStream(bais);
        } else if (ALG_DEFLATE.equals(alg)) {
            return new InflaterInputStream(bais);
        } else {
            return null;
        }
    }

    private static byte[] GZIPCompress(byte[] requestContent) throws IOException {
        ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
        GZIPOutputStream gzipstream = new GZIPOutputStream(compressedContent);
        gzipstream.write(requestContent);
        gzipstream.finish();

        // get the compressed content
        return compressedContent.toByteArray();
    }

    private static byte[] DeflaterCompress(byte[] requestContent) throws IOException {
        ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
        DeflaterOutputStream defstream = new DeflaterOutputStream(compressedContent);
        defstream.write(requestContent);
        defstream.finish();

        // get the compressed content
        return compressedContent.toByteArray();
    }
}
