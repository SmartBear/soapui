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

package com.eviware.soapui.model.iface;

import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;

/**
 * A message part in a Request
 * 
 * @author ole.matzura
 */

public interface MessagePart
{
	public String getName();

	public String getDescription();

	public PartType getPartType();

	public enum PartType
	{
		HEADER, CONTENT, ATTACHMENT, FAULT, PARAMETER
	};

	public abstract static class ContentPart implements MessagePart
	{
		public abstract SchemaType getSchemaType();

		public abstract QName getPartElementName();

		public abstract SchemaGlobalElement getPartElement();

		public PartType getPartType()
		{
			return PartType.CONTENT;
		}
	}

	public abstract static class AttachmentPart implements MessagePart
	{
		public abstract String[] getContentTypes();

		public abstract boolean isAnonymous();

		public PartType getPartType()
		{
			return PartType.ATTACHMENT;
		}
	}

	public abstract static class HeaderPart extends ContentPart
	{
		public PartType getPartType()
		{
			return PartType.HEADER;
		}
	}

	public abstract static class ParameterPart extends ContentPart
	{
		public PartType getPartType()
		{
			return PartType.PARAMETER;
		}
	}

	public abstract static class FaultPart extends ContentPart
	{
		public PartType getPartType()
		{
			return PartType.FAULT;
		}

		public abstract Part[] getWsdlParts();
	}
}
