/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.wsdl;

import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.InputSource;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionLoader;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.x.dialogs.XProgressMonitor;

/**
 * Abstract WSDLLocator for loading definitions from either URL or cache..
 * 
 * @author ole.matzura
 */

public abstract class AbstractWsdlDefinitionLoader extends AbstractDefinitionLoader implements WsdlDefinitionLoader
{
	private final String url;
	private String last;
	private String username;
	private String password;
	protected static final Logger log = Logger.getLogger( AbstractWsdlDefinitionLoader.class );
	private XProgressMonitor monitor;
	private int progressIndex;

	public AbstractWsdlDefinitionLoader( String url )
	{
		this.url = url;

		if( !PathUtils.isFilePath( url ) && !PathUtils.isRelativePath( url ) )
		{
			// check for username/password
			try
			{
				URL u = new URL( url );
				String authority = u.getAuthority();
				if( authority != null )
				{
					int ix1 = authority.indexOf( '@' );
					int ix2 = authority.indexOf( ':' );

					if( ix1 > ix2 && ix2 > 0 )
					{
						username = authority.substring( 0, ix2 );
						password = authority.substring( ix2 + 1, ix1 );
					}
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
	}

	public String getUrl()
	{
		return url;
	}

	public InputSource getBaseInputSource()
	{
		try
		{
			log.debug( "Returning baseInputSource [" + url + "]" );
			return new InputSource( load( url ) );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e.toString() );
		}
	}

	public abstract InputStream load( String url ) throws Exception;

	public XmlObject loadXmlObject( String url, XmlOptions options ) throws Exception
	{
		try
		{
			if( options == null )
			{
				options = new XmlOptions();
			}

			if( monitor != null )
				monitor.setProgress( progressIndex, "Loading [" + url + "]" );

			options.setLoadLineNumbers();
			// return XmlObject.Factory.parse( load( url ), options );
			return XmlUtils.createXmlObject( load( url ), options );
		}
		catch( Exception e )
		{
			log.error( "Failed to load url [" + url + "]" );
			throw e;
		}
	}

	public String getBaseURI()
	{
		// log.debug( "Returning baseURI [" + url + "]" );
		return url;
	}

	public InputSource getImportInputSource( String parent, String imp )
	{
		if( isAbsoluteUrl( imp ) )
			last = imp;
		else
			last = Tools.joinRelativeUrl( parent, imp );

		try
		{
			InputStream input = load( last );
			return input == null ? null : new InputSource( input );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e.toString() );
		}
	}

	protected boolean isAbsoluteUrl( String tempImp )
	{
		tempImp = tempImp.toUpperCase();
		return tempImp.startsWith( "HTTP:" ) || tempImp.startsWith( "HTTPS:" ) || tempImp.startsWith( "FILE:" );
	}

	public String getLatestImportURI()
	{
		String result = last == null ? url : last;
		log.debug( "Returning latest import URI [" + result + "]" );
		return result;
	}

	public boolean hasCredentials()
	{
		return !StringUtils.isNullOrEmpty( username ) && !StringUtils.isNullOrEmpty( password );
	}

	public String getPassword()
	{
		return password;
	}

	public String getUsername()
	{
		return username;
	}
}
