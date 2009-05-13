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

package com.eviware.soapui.impl.wsdl;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart;

/**
 * Descriptor for attachments
 * 
 * @author Ole.Matzura
 */

public final class HttpAttachmentPart extends MessagePart.AttachmentPart
{
	public static final String ANONYMOUS_NAME = "<anonymous>";
	private String name;
	private List<String> contentTypes = new ArrayList<String>();
	private Attachment.AttachmentType type;
	private boolean anonymous;
	private SchemaType schemaType;

	public HttpAttachmentPart()
	{
		anonymous = true;
		name = ANONYMOUS_NAME;
		type = Attachment.AttachmentType.UNKNOWN;
	}

	public HttpAttachmentPart( String name, List<String> types )
	{
		super();
		this.name = name;

		if( types != null )
			contentTypes.addAll( types );
	}

	public HttpAttachmentPart( String name, String type )
	{
		this.name = name;
		if( type != null )
			contentTypes.add( type );
	}

	public String[] getContentTypes()
	{
		return contentTypes.toArray( new String[contentTypes.size()] );
	}

	public String getName()
	{
		return name;
	}

	public void addContentType( String contentType )
	{
		contentTypes.add( contentType );
	}

	public Attachment.AttachmentType getAttachmentType()
	{
		return type;
	}

	public void setType( Attachment.AttachmentType type )
	{
		this.type = type;
	}

	public String getDescription()
	{
		return name + " attachment; [" + getContentTypes() + "]";
	}

	public boolean isAnonymous()
	{
		return anonymous;
	}

	public SchemaType getSchemaType()
	{
		return schemaType;
	}

	public void setSchemaType( SchemaType schemaType )
	{
		this.schemaType = schemaType;
	}
}