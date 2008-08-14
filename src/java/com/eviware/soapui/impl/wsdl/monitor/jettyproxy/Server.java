package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import javax.servlet.ServletException;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.util.IO;

public class Server extends org.mortbay.jetty.Server
{

	@Override
	public void handle(final org.mortbay.jetty.HttpConnection connection) throws IOException, ServletException
	{
		final Request request = connection.getRequest();

		if (!request.getMethod().equals("CONNECT"))
		{
			super.handle(connection);
			return;
		}

		final String uri = request.getUri().toString();

		final int c = uri.indexOf(':');
		final String port = uri.substring(c + 1);
		final String host = uri.substring(0, c);

		final InetSocketAddress inetAddress = new InetSocketAddress(host, Integer.parseInt(port));

		final Socket clientSocket = connection.getEndPoint().getTransport() instanceof Socket ? (Socket) connection
				.getEndPoint().getTransport() : ((SocketChannel) connection.getEndPoint().getTransport()).socket();
		final InputStream in = clientSocket.getInputStream();
		final OutputStream out = clientSocket.getOutputStream();

		final Socket socket = new Socket(inetAddress.getAddress(), inetAddress.getPort());

		final Response response = connection.getResponse();
		response.setStatus(200);
//		response.setHeader("Connection", "close");
		response.flushBuffer();

		IO.copyThread(socket.getInputStream(), out);

		IO.copyThread(in, socket.getOutputStream());
		
	}

}
