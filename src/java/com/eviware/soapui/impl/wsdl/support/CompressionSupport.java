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

package com.eviware.soapui.impl.wsdl.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import com.eviware.soapui.support.Tools;

public class CompressionSupport
{
	public static final String ALG_GZIP = "gzip";
	public static final String ALG_DEFLATE = "deflate";
	private static final String[] algs = { ALG_GZIP, ALG_DEFLATE };

	public static String getAvailableAlgorithms( String separator )
	{
		StringBuffer buf = new StringBuffer();
		for( int i = 0; i < algs.length; ++i )
		{
			if( i > 0 )
				buf.append( separator );
			buf.append( algs[i] );
		}

		return buf.toString();
	}

	public static String getAvailableAlgorithm( String httpContentEncoding )
	{
		for( int i = 0; i < algs.length; ++i )
			if( httpContentEncoding.toLowerCase().endsWith( algs[i] ) )
				return algs[i];

		return null;
	}

	private static void checkAlg( String alg ) throws Exception
	{
		if( !ALG_GZIP.equals( alg ) && !ALG_DEFLATE.equals( alg ) )
			throw new Exception( "Compression algorithm not supported: " + alg );
	}

	public static byte[] compress( String alg, byte[] content ) throws Exception
	{
		checkAlg( alg );
		if( ALG_GZIP.equals( alg ) )
			return GZIPCompress( content );
		else if( ALG_DEFLATE.equals( alg ) )
			return DeflaterCompress( content );
		else
			return null;
	}

	public static byte[] decompress( String alg, byte[] content ) throws Exception
	{
		checkAlg( alg );
		if( ALG_GZIP.equals( alg ) )
			return GZIPDecompress( content );
		else if( ALG_DEFLATE.equals( alg ) )
			return DeflaterDecompress( content );
		else
			return null;
	}

	// createCompressionInputStream can be used in the future if
	// PipedInputStreams are used
	// for sending compressed data instead of creating compressed byte array
	// first and then sending
	public static InputStream createCompressionInputStream( String alg, byte[] content ) throws Exception
	{
		checkAlg( alg );
		ByteArrayInputStream bais = new ByteArrayInputStream( content );
		if( ALG_GZIP.equals( alg ) )
			return new GZIPInputStream( bais );
		else if( ALG_DEFLATE.equals( alg ) )
			return new InflaterInputStream( bais );
		else
			return null;
	}

	private static byte[] GZIPCompress( byte[] requestContent ) throws IOException
	{
		ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
		GZIPOutputStream gzipstream = new GZIPOutputStream( compressedContent );
		gzipstream.write( requestContent );
		gzipstream.finish();

		// get the compressed content
		return compressedContent.toByteArray();
	}

	private static byte[] GZIPDecompress( byte[] content ) throws IOException
	{
		GZIPInputStream zipin;
		InputStream in = new ByteArrayInputStream( content );
		zipin = new GZIPInputStream( in );
		ByteArrayOutputStream out = Tools.readAll( zipin, -1 );
		content = out.toByteArray();
		out.close();
		zipin.close();

		return content;
	}

	private static byte[] DeflaterCompress( byte[] requestContent ) throws IOException
	{
		ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
		DeflaterOutputStream defstream = new DeflaterOutputStream( compressedContent );
		defstream.write( requestContent );
		defstream.finish();

		// get the compressed content
		return compressedContent.toByteArray();
	}

	private static byte[] DeflaterDecompress( byte[] content ) throws IOException
	{
		InflaterInputStream zipin;
		InputStream in = new ByteArrayInputStream( content );
		zipin = new InflaterInputStream( in );
		ByteArrayOutputStream out = Tools.readAll( zipin, -1 );
		content = out.toByteArray();
		out.close();
		zipin.close();

		return content;
	}
}
