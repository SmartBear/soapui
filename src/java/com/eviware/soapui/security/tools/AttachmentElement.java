package com.eviware.soapui.security.tools;

import com.eviware.soapui.model.iface.Attachment;

public class AttachmentElement
{
	private Attachment attachment;
	private String id;

	public AttachmentElement( Attachment attachment, int id )
	{
		this.attachment = attachment;
		this.id = attachment.getName() + ":" + id;
	}

	public Attachment getAttachment()
	{
		return attachment;
	}

	public void setAttachment( Attachment attachment )
	{
		this.attachment = attachment;
	}

	public String getId()
	{
		return id;
	}

	public String toString()
	{
		return attachment.getName();
	}
}
