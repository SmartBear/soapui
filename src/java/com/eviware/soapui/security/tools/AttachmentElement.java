package com.eviware.soapui.security.tools;

import java.io.File;

public class AttachmentElement
{
	private File file;
	private String contentType;
	private Boolean cached;
	private Boolean enabled;

	AttachmentElement( File file, String contentType, Boolean enabled, Boolean cached )
	{
		this.file = file;
		this.enabled = enabled;
		this.cached = cached;
		this.contentType = contentType;
	}

	public File getFile()
	{
		return file;
	}

	public void setFile( File file )
	{
		this.file = file;
	}

	public Boolean getEnabled()
	{
		return enabled;
	}

	public void setEnabled( Boolean enabled )
	{
		this.enabled = enabled;
	}

	public Boolean getCached()
	{
		return cached;
	}

	public void setCached( Boolean cached )
	{
		this.cached = cached;
	}

	public long getSize()
	{
		return file.length();
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType( String contentType )
	{
		this.contentType = contentType;
	}
}
