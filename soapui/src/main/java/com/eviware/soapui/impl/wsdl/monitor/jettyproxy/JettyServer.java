/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import com.eviware.soapui.SoapUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.util.IO;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class JettyServer extends org.mortbay.jetty.Server {
    private Logger log = LogManager.getLogger(JettyServer.class);

    public JettyServer() {
        super();
        if (SoapUI.getLogMonitor() == null || SoapUI.getLogMonitor().getLogArea("jetty log") == null) {
            return;
        }
        SoapUI.getLogMonitor().getLogArea("jetty log").addLogger(log.getName(), true);
    }

    @Override
    public void handle(final org.mortbay.jetty.HttpConnection connection) throws IOException, ServletException {
        final Request request = connection.getRequest();

        if (request.getMethod().equals("CONNECT")) {
            final String uri = request.getUri().toString();

            final int c = uri.indexOf(':');
            final String port = uri.substring(c + 1);
            final String host = uri.substring(0, c);

            final InetSocketAddress inetAddress = new InetSocketAddress(host, Integer.parseInt(port));

            final Socket clientSocket = connection.getEndPoint().getTransport() instanceof Socket ? (Socket) connection
                    .getEndPoint().getTransport() : ((SocketChannel) connection.getEndPoint().getTransport()).socket();
            final InputStream in = clientSocket.getInputStream();
            final OutputStream out = clientSocket.getOutputStream();

            final SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(inetAddress.getAddress(),
                    inetAddress.getPort());

            final Response response = connection.getResponse();
            response.setStatus(200);
            // response.setHeader("Connection", "close");
            response.flushBuffer();

            IO.copyThread(socket.getInputStream(), out);

            IO.copyThread(in, socket.getOutputStream());
        } else {
            super.handle(connection);
        }
    }

}
