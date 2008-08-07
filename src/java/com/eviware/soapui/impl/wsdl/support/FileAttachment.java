/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.ResolveContext;
import com.eviware.soapui.impl.wsdl.teststeps.BeanPathPropertySupport;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;

/**
 * Attachments cached locally for each request
 * 
 * @author Ole.Matzura
 */

public abstract class FileAttachment<T extends AbstractWsdlModelItem<?>> implements WsdlAttachment
{
	private AttachmentConfig config;
	private final static Logger log = Logger.getLogger(FileAttachment.class);
	private final T modelItem;
	private BeanPathPropertySupport urlProperty;

	public FileAttachment( T modelItem, AttachmentConfig config )
	{
		this.modelItem = modelItem;
		this.config = config;
		
		if( config.getTempFilename() != null )
		{
			try
			{
				log.info( "Moving locally cached file [" + config.getTempFilename() + "] to internal cache.." );
				File tempFile = new File( config.getTempFilename() );
				cacheFileLocally( tempFile);
			}
			catch (IOException e)
			{
				if( !config.isSetData() )
				{
					config.setData( new byte[0] );
					config.setSize( 0 );
				}
				
				SoapUI.logError( e );
			}
		}
		
		if( isCached() )
		{
			if( config.isSetTempFilename())
				config.unsetTempFilename();
			
			if( config.isSetUrl() )
				config.unsetUrl();
		}
		
		urlProperty = new BeanPathPropertySupport( modelItem, config, "url" );
	}
	
	public FileAttachment( T modelItem, File file, boolean cache, AttachmentConfig config ) throws IOException
	{
		this( modelItem, config );
		
		config.setName( file.getName() );
		config.setContentType( ContentTypeHandler.getContentTypeFromFilename( file.getName() ) );
		
		// cache locally if specified
		if( cache )
		{
			cacheFileLocally( file );
		}
		else
		{
			urlProperty.set( file.getPath(), false );
		}
	}
	
	public T getModelItem()
	{
		return modelItem;
	}

	private void cacheFileLocally(File file) throws FileNotFoundException, IOException
	{
		// write attachment-data to tempfile
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream( data );
		out.putNextEntry( new ZipEntry( config.getName() ));
		
		InputStream in = new FileInputStream( file );
		long sz = file.length();
		config.setSize( sz );
		
		Tools.writeAll( out, in );
		
		in.close();
		out.closeEntry();
		out.finish();
		out.close();
		data.close();
		
		config.setData( data.toByteArray() );
	}

	public String getContentType()
	{
		return config.getContentType();
	}

	public InputStream getInputStream() throws IOException
	{
		BufferedInputStream inputStream = null;
		
		if( isCached() )
		{
			ZipInputStream zipInputStream = new ZipInputStream( new ByteArrayInputStream( config.getData() ));
			zipInputStream.getNextEntry();
			inputStream = new BufferedInputStream( zipInputStream );
		}
		else
		{
			inputStream = new BufferedInputStream( new FileInputStream( urlProperty.expand() ));
		}
		
		AttachmentEncoding encoding = getEncoding();
		if( encoding == AttachmentEncoding.BASE64 )
		{
			ByteArrayOutputStream data = Tools.readAll( inputStream, Tools.READ_ALL );
			return new ByteArrayInputStream( Base64.encodeBase64( data.toByteArray() ));
		}
		else if( encoding == AttachmentEncoding.HEX )
		{
			ByteArrayOutputStream data = Tools.readAll( inputStream, Tools.READ_ALL );
			return new ByteArrayInputStream( new String( Hex.encodeHex( data.toByteArray() )).getBytes() );
		}
		
		return inputStream;
	}
	
	public String getName()
	{
		return config.getName();
	}

	public long getSize()
	{
		if( isCached() ) 
			return config.getSize();
		else
			return new File( urlProperty.expand() ).length();
	}

	public void release()
	{
		if( isCached() )
			new File( config.getTempFilename() ).delete();
	}

	public String getPart()
	{
		return config.getPart();
	}

	public void setContentType(String contentType)
	{
		config.setContentType( contentType );
	}

	public void setPart(String part)
	{
		config.setPart( part );
	}

	public String getUrl()
	{
		if( isCached() )
		{
			String name = config.getName();
			int ix = name.lastIndexOf( "." );
			
			try
			{
				File tempFile = File.createTempFile( "attachment-" + name.substring( 0, ix), name.substring(ix)  );
				FileOutputStream out = new FileOutputStream( tempFile );
				InputStream in = getInputStream();
				
				Tools.writeAll( out, in );
				
				out.close();
				in.close();
				
				return tempFile.getAbsoluteFile().toURI().toURL().toString();
			}
			catch (IOException e)
			{
				SoapUI.logError( e );
			}
		}
		else
		{
			return urlProperty.expand();
		}
		
		return null;
	}

	public boolean isCached()
	{
		return config.isSetData();
	}

	abstract public AttachmentType getAttachmentType();

	public void updateConfig(AttachmentConfig config)
	{
		this.config = config;
		urlProperty.setConfig(config);
	}

	public AttachmentConfig getConfig()
	{
		return config;
	}

	public void setContentID( String contentID )
	{
		if( (contentID == null || contentID.length() == 0) && config.isSetContentId() )
			config.unsetContentId();
		else
			config.setContentId( contentID );
	}

	public String getContentID()
	{
		return config.getContentId();
	}

	public void resolve(ResolveContext context)
	{
		if( isCached() )
			return;
		
		urlProperty.resolveFile( context, "Missing attachment [" + getName() + "]", null, null, false );
	}
}
