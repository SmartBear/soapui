package com.eviware.soapui.support.httpclient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.support.TestCaseWithJetty;

public class EchoServer
{

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( TestCaseWithJetty.class );
	}

	public static void main( String[] arstring )
	{
		try
		{
			SSLServerSocketFactory sslserversocketfactory = ( SSLServerSocketFactory )SSLServerSocketFactory.getDefault();
			SSLServerSocket sslserversocket = ( SSLServerSocket )sslserversocketfactory.createServerSocket( 9999 );
			SSLSocket sslsocket = ( SSLSocket )sslserversocket.accept();

			InputStream inputstream = sslsocket.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader( inputstream );
			BufferedReader bufferedreader = new BufferedReader( inputstreamreader );

			String string = null;
			while( ( string = bufferedreader.readLine() ) != null )
			{
				System.out.println( string );
				System.out.flush();
			}
		}
		catch( Exception exception )
		{
			exception.printStackTrace();
		}
	}

	@Test
	public void test()
	{
		Assert.assertTrue( true );
	}
}
