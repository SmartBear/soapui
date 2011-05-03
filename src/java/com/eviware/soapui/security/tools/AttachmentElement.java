package com.eviware.soapui.security.tools;

import java.io.File;

import javax.activation.MimetypesFileTypeMap;

public class AttachmentElement
{

	File file;
	String contentType;
	Boolean enabled;
	Boolean cached;

	AttachmentElement( File file, String contentType, Boolean enabled, Boolean cached )
	{
		this.file = file;
		this.contentType = contentType;
		this.enabled = enabled;
		this.cached = cached;
	}

	public void setAttachment( File file )
	{
		this.file = file;
		this.contentType = new MimetypesFileTypeMap().getContentType( file );
	}

	public void setContentType( String contentType )
	{
		this.contentType = contentType;
	}

	public void setEnabled( Boolean enabled )
	{
		this.enabled = enabled;
	}

	public String getName()
	{
		return file.getName();
	}

	public String getPath()
	{
		return file.getPath();
	}

	public long getSize()
	{
		return file.length();
	}

	public File getAttachment()
	{
		return file;
	}

	public String getContentType()
	{
		return this.contentType;
	}

	public Boolean isEnabled()
	{
		return enabled;
	}

	public Boolean isCached()
	{
		return cached;
	}

}
