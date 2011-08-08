///*
// *  soapUI, copyright (C) 2004-2011 eviware.com 
// *
// *  soapUI is free software; you can redistribute it and/or modify it under the 
// *  terms of version 2.1 of the GNU Lesser General Public License as published by 
// *  the Free Software Foundation.
// *
// *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
// *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// *  See the GNU Lesser General Public License for more details at gnu.org.
// */
//
//package com.eviware.soapui.impl.wsdl.support.http;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
//
///**
// * @deprecated Replaced by {@link SoapUISSLSocketFactory}
// */
//public class SoapUIEasySSLProtocolSocketFactory extends EasySSLProtocolSocketFactory
//{
//	private Map<String, EasySSLProtocolSocketFactory> factoryMap = new HashMap<String, EasySSLProtocolSocketFactory>();
//
//	public SoapUIEasySSLProtocolSocketFactory() throws Exception
//	{
//		super();
//	}
//
//		@Override
//		public Socket createSocket( String host, int port, InetAddress localAddress, int localPort,
//				HttpConnectionParams params ) throws IOException
//		{
//			String sslConfig = ( String )params.getParameter( SoapUIHostConfiguration.SOAPUI_SSL_CONFIG );
//	
//			if( StringUtils.isNullOrEmpty( sslConfig ) )
//			{
//				return enableSocket( ( SSLSocket )super.createSocket( host, port, localAddress, localPort, params ) );
//			}
//	
//			EasySSLProtocolSocketFactory factory = factoryMap.get( sslConfig );
//			if( factory != null )
//			{
//				return enableSocket( ( SSLSocket )factory.createSocket( host, port, localAddress, localPort, params ) );
//			}
//			try
//			{
//				// try to create new factory for specified config
//				factory = new EasySSLProtocolSocketFactory();
//	
//				int ix = sslConfig.lastIndexOf( ' ' );
//				String keyStore = sslConfig.substring( 0, ix );
//				String pwd = sslConfig.substring( ix + 1 );
//	
//				factory.setKeyMaterial( new KeyMaterial( keyStore, pwd.toCharArray() ) );
//				factoryMap.put( sslConfig, factory );
//	
//				return enableSocket( ( SSLSocket )factory.createSocket( host, port, localAddress, localPort, params ) );
//			}
//			catch( Exception gse )
//			{
//				SoapUI.logError( gse );
//				return enableSocket( ( SSLSocket )super.createSocket( host, port, localAddress, localPort, params ) );
//			}
//		}
//
//		private synchronized Socket enableSocket( SSLSocket socket )
//		{
//			socket.getSession().invalidate();
//	
//			String protocols = System.getProperty( "soapui.https.protocols" );
//			String ciphers = System.getProperty( "soapui.https.ciphers" );
//	
//			if( StringUtils.hasContent( protocols ) )
//			{
//				socket.setEnabledProtocols( protocols.split( "," ) );
//			}
//			else if( socket.getSupportedProtocols() != null )
//			{
//				socket.setEnabledProtocols( socket.getSupportedProtocols() );
//			}
//	
//			if( StringUtils.hasContent( ciphers ) )
//			{
//				socket.setEnabledCipherSuites( ciphers.split( "," ) );
//			}
//			else if( socket.getSupportedCipherSuites() != null )
//			{
//				socket.setEnabledCipherSuites( socket.getSupportedCipherSuites() );
//			}
//			return socket;
//		}
//}
