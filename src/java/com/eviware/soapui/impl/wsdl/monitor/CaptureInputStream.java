/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.monitor;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CaptureInputStream extends FilterInputStream
{
	private final ByteArrayOutputStream capture = new ByteArrayOutputStream();
	private long maxData = 0;

	public CaptureInputStream( InputStream in, long maxData )
	{
		super( in );
		this.maxData = maxData;
	}

	public CaptureInputStream( InputStream in )
	{
		super( in );
	}

	@Override
	public int read() throws IOException
	{
		int i = super.read();
		if( maxData > 0 && capture.size() < maxData )
			capture.write( i );

		return i;
	}

	@Override
	public int read( byte[] b ) throws IOException
	{
		int i = super.read( b );

		if( i > 0 && maxData > 0 && capture.size() < maxData )
		{
			if( i + capture.size() < maxData )
				capture.write( b, 0, i );
			else
				capture.write( b, 0, ( int )( maxData - capture.size() ) );
		}

		return i;
	}

	@Override
	public int read( byte[] b, int off, int len ) throws IOException
	{
		int i = super.read( b, off, len );
		if( i > 0 && maxData > 0 && capture.size() < maxData )
		{
			if( i + capture.size() < maxData )
				capture.write( b, off, i );
			else
				capture.write( b, off, ( int )( maxData - capture.size() ) );
		}

		return i;
	}

	public byte[] getCapturedData()
	{
		return capture.toByteArray();
	}
}
