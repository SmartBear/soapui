/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.wsdl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;
import org.apache.commons.httpclient.methods.GetMethod;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.SwingWorker;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * WsdlLoader for URLs
 * 
 * @author ole.matzura
 */

public class UrlWsdlLoader extends WsdlLoader implements DefinitionLoader
{
	private HttpState state;
	protected GetMethod getMethod;
	private boolean aborted;
	protected Map<String, byte[]> urlCache = new HashMap<String, byte[]>();
	protected boolean finished;
	private boolean useWorker;
	private ModelItem contextModelItem;

	public UrlWsdlLoader( String url )
	{
		this( url, null );
	}

	public UrlWsdlLoader( String url, ModelItem contextModelItem )
	{
		super( url );
		this.contextModelItem = contextModelItem;
		state = new HttpState();
	}

	public boolean isUseWorker()
	{
		return useWorker;
	}

	public void setUseWorker( boolean useWorker )
	{
		this.useWorker = useWorker;
	}

	public InputStream load() throws Exception
	{
		return load( getBaseURI() );
	}

	public synchronized InputStream load( String url ) throws Exception
	{
		if( !PathUtils.isHttpPath( url ) )
		{
			try
			{
				File file = new File( url.replace( '/', File.separatorChar ) );
				if( file.exists() )
					url = file.toURI().toURL().toString();
			}
			catch( Exception e )
			{
			}
		}

		if( urlCache.containsKey( url ) )
		{
			setNewBaseURI( url );
			return new ByteArrayInputStream( urlCache.get( url ) );
		}

		if( url.startsWith( "file:" ) )
		{
			return handleFile( url );
		}

		log.debug( "Getting wsdl component from [" + url + "]" );

		createGetMethod( url );

		if( aborted )
			return null;

		LoaderWorker worker = new LoaderWorker();
		if( useWorker )
			worker.start();
		else
			worker.construct();

		while( !aborted && !finished )
		{
			Thread.sleep( 200 );
		}

		// wait for method to catch up - required in unit tests..
		while( !aborted && getMethod.getResponseBody() == null )
		{
			Thread.sleep( 200 );
		}

		try
		{
			if( aborted )
			{
				throw new Exception( "Load of url [" + url + "] was aborted" );
			}
			else
			{
				byte[] content = getMethod.getResponseBody();
				if( content != null )
				{
					String compressionAlg = HttpClientSupport.getResponseCompressionType( getMethod );
					if( compressionAlg != null )
						content = CompressionSupport.decompress( compressionAlg, content );

					urlCache.put( url, content );
					String newUrl = getMethod.getURI().getURI();
					if( !url.equals( newUrl ) )
						log.info( "BaseURI was redirected to [" + newUrl + "]" );
					setNewBaseURI( newUrl );
					urlCache.put( newUrl, content );
					return new ByteArrayInputStream( content );
				}
				else
				{
					throw new Exception( "Failed to load url; " + getMethod.getStatusCode() + " - "
							+ getMethod.getStatusText() );
				}
			}
		}

		finally
		{
			getMethod.releaseConnection();
		}
	}

	protected InputStream handleFile( String url ) throws IOException
	{
		setNewBaseURI( url );
		return new URL( url ).openStream();
	}

	protected void createGetMethod( String url )
	{
		getMethod = new GetMethod( url );
		getMethod.setFollowRedirects( true );
		getMethod.setDoAuthentication( true );
		getMethod.getParams().setParameter( CredentialsProvider.PROVIDER, new WsdlCredentialsProvider() );

		if( SoapUI.getSettings().getBoolean( HttpSettings.AUTHENTICATE_PREEMPTIVELY ) )
		{
			HttpClientSupport.getHttpClient().getParams().setAuthenticationPreemptive( true );
		}
		else
		{
			HttpClientSupport.getHttpClient().getParams().setAuthenticationPreemptive( false );
		}
	}

	public final class LoaderWorker extends SwingWorker
	{
		public Object construct()
		{
			HttpClient httpClient = HttpClientSupport.getHttpClient();
			try
			{
				Settings soapuiSettings = SoapUI.getSettings();

				HttpClientSupport.applyHttpSettings( getMethod, soapuiSettings );
				HostConfiguration hostConfiguration = ProxyUtils.initProxySettings( soapuiSettings, state,
						new HostConfiguration(), getMethod.getURI().toString(), contextModelItem == null ? null
								: new DefaultPropertyExpansionContext( contextModelItem ) );

				httpClient.executeMethod( hostConfiguration, getMethod, state );
			}
			catch( Exception e )
			{
				return e;
			}
			finally
			{
				finished = true;
			}

			return null;
		}
	}

	public boolean abort()
	{
		if( getMethod != null )
			getMethod.abort();

		aborted = true;

		return true;
	}

	public boolean isAborted()
	{
		return aborted;
	}

	/**
	 * CredentialsProvider for providing login information during WSDL loading
	 * 
	 * @author ole.matzura
	 */

	public final class WsdlCredentialsProvider implements CredentialsProvider
	{
		private XFormDialog basicDialog;
		private XFormDialog ntDialog;

		public WsdlCredentialsProvider()
		{
		}

		public Credentials getCredentials( final AuthScheme authscheme, final String host, int port, boolean proxy )
				throws CredentialsNotAvailableException
		{
			if( authscheme == null )
			{
				return null;
			}
			try
			{
				String pw = getPassword();
				if( pw == null )
					pw = "";

				if( authscheme instanceof NTLMScheme )
				{
					if( hasCredentials() )
					{
						log.info( "Returning url credentials" );
						return new NTCredentials( getUsername(), pw, host, null );
					}

					log.info( host + ":" + port + " requires Windows authentication" );
					if( ntDialog == null )
					{
						buildNtDialog();
					}

					StringToStringMap values = new StringToStringMap();
					values.put( "Info", "Authentication required for [" + host + ":" + port + "]" );
					ntDialog.setValues( values );

					if( ntDialog.show() )
					{
						values = ntDialog.getValues();
						return new NTCredentials( values.get( "Username" ), values.get( "Password" ), host, values
								.get( "Domain" ) );
					}
					else
						throw new CredentialsNotAvailableException( "Operation cancelled" );
				}
				else if( authscheme instanceof RFC2617Scheme )
				{
					if( hasCredentials() )
					{
						log.info( "Returning url credentials" );
						return new UsernamePasswordCredentials( getUsername(), pw );
					}

					log.info( host + ":" + port + " requires authentication with the realm '" + authscheme.getRealm() + "'" );
					ShowDialog showDialog = new ShowDialog();
					showDialog.values.put( "Info", "Authentication required for [" + host + ":" + port + "]" );

					UISupport.getUIUtils().runInUIThreadIfSWT( showDialog );
					if( showDialog.result )
					{
						return new UsernamePasswordCredentials( showDialog.values.get( "Username" ), showDialog.values
								.get( "Password" ) );
					}
					else
						throw new CredentialsNotAvailableException( "Operation cancelled" );

				}
				else
				{
					throw new CredentialsNotAvailableException( "Unsupported authentication scheme: "
							+ authscheme.getSchemeName() );
				}
			}
			catch( IOException e )
			{
				throw new CredentialsNotAvailableException( e.getMessage(), e );
			}
		}

		private void buildBasicDialog()
		{
			XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Basic Authentication" );
			XForm mainForm = builder.createForm( "Basic" );
			mainForm.addLabel( "Info", "" );
			mainForm.addTextField( "Username", "Username for authentication", XForm.FieldType.TEXT );
			mainForm.addTextField( "Password", "Password for authentication", XForm.FieldType.PASSWORD );

			basicDialog = builder.buildDialog( builder.buildOkCancelActions(), "Specify Basic Authentication Credentials",
					UISupport.OPTIONS_ICON );
		}

		private void buildNtDialog()
		{
			XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "NT Authentication" );
			XForm mainForm = builder.createForm( "Basic" );
			mainForm.addLabel( "Info", "" );
			mainForm.addTextField( "Username", "Username for authentication", XForm.FieldType.TEXT );
			mainForm.addTextField( "Password", "Password for authentication", XForm.FieldType.PASSWORD );
			mainForm.addTextField( "Domain", "NT Domain for authentication", XForm.FieldType.TEXT );

			ntDialog = builder.buildDialog( builder.buildOkCancelActions(), "Specify NT Authentication Credentials",
					UISupport.OPTIONS_ICON );
		}

		private class ShowDialog implements Runnable
		{
			StringToStringMap values = new StringToStringMap();
			boolean result;

			public void run()
			{
				if( basicDialog == null )
					buildBasicDialog();

				basicDialog.setValues( values );

				result = basicDialog.show();
				if( result )
				{
					values = basicDialog.getValues();
				}
			}
		}
	}

	public void close()
	{
	}
}