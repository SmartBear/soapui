package com.eviware.soapui.impl.rest.discovery;

import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;

/**
 * Utility class for decoding content in an HTTP request or response based on the Content-Encoding header.
 */
public class HttpContentDecoder {
    public static byte[] decode(byte[] bytes, StringToStringsMap headers) {
        if (bytes == null) {
            return null;
        }
        // Use the excellent content encoding handling that exists in HTTP Client
        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new HttpVersion(1, 0), 0, null));
        ByteArrayEntity entity = new ByteArrayEntity(bytes);
        String contentEncoding = null;
        if (headers != null) {
            contentEncoding = headers.getCaseInsensitive("Content-Encoding", null);
        }
        entity.setContentEncoding(contentEncoding);
        response.setEntity(entity);
        try {
            new ResponseContentEncoding().process(response, null);
            return IOUtils.toByteArray(response.getEntity().getContent());
        } catch (HttpException ignore) {
        } catch (IOException ignore) {
        }
        return bytes;
    }
}
