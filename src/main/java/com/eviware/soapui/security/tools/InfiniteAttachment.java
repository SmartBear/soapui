package com.eviware.soapui.security.tools;

import java.io.IOException;
import java.io.InputStream;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.support.RequestFileAttachment;
import com.eviware.soapui.model.iface.Attachment;

public class InfiniteAttachment extends RequestFileAttachment
{
	private long maxSize;

	public InfiniteAttachment( AttachmentConfig config, AbstractHttpRequestInterface<?> request, long maxSize )
	{
		super( config, request );
		this.maxSize = maxSize;
	}

	public InputStream getInputStream() throws IOException
	{
		return new InfiniteInputStream( maxSize );
	}

	public AttachmentType getAttachmentType()
	{
		return Attachment.AttachmentType.UNKNOWN;
	}

	@Override
	public String getContentType()
	{
		return "application/octet-stream";
	}
}
