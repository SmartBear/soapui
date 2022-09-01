package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.Socket;

public class SoapUIPlainSocketFactory extends PlainConnectionSocketFactory {
    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        // Open source cannot work with socks proxy.
        return super.createSocket(context);
    }
}