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

package com.eviware.soapui.impl.wsdl.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CompressedStringConfig;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.Tools;

/**
 * Utility class for compressing/decompressing strings stored with
 * CompressedString
 * 
 * @author ole.matzura
 */

public class CompressedStringSupport
{
	public static String getString( CompressedStringConfig compressedStringConfig )
	{
		synchronized( compressedStringConfig )
		{
			String compression = compressedStringConfig.getCompression();
			if( "gzip".equals( compression ) )
			{
				try
				{
					byte[] bytes = Base64.decodeBase64( compressedStringConfig.getStringValue().getBytes() );
					GZIPInputStream in = new GZIPInputStream( new ByteArrayInputStream( bytes ) );
					return Tools.readAll( in, -1 ).toString();
				}
				catch( IOException e )
				{
					SoapUI.logError( e );
				}
			}

			return compressedStringConfig.getStringValue();
		}
	}

	public static void setString( CompressedStringConfig compressedStringConfig, String value )
	{
		synchronized( compressedStringConfig )
		{
			long limit = SoapUI.getSettings().getLong( WsdlSettings.COMPRESSION_LIMIT, 0 );
			if( limit > 0 && value.length() >= limit )
			{
				try
				{
					ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
					GZIPOutputStream out = new GZIPOutputStream( byteOut );
					out.write( value.getBytes() );
					out.finish();
					value = new String( Base64.encodeBase64( byteOut.toByteArray() ) );
					compressedStringConfig.setCompression( "gzip" );
				}
				catch( IOException e )
				{
					SoapUI.logError( e );
					compressedStringConfig.unsetCompression();
				}
			}
			else if( compressedStringConfig.isSetCompression() )
			{
				compressedStringConfig.unsetCompression();
			}

			compressedStringConfig.setStringValue( value );
		}
	}
}
