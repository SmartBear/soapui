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

import javax.wsdl.BindingOperation;
import javax.wsdl.Part;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;

/**
 * Wrapper for WSDL parts
 * 
 * @author ole.matzura
 */

public class MessageXmlPart
{
	private XmlObject partXmlObject;
	private final XmlObject sourceXmlObject;
	private final Part part;
	private final BindingOperation bindingOperation;
	private final boolean isRequest;

	public MessageXmlPart( XmlObject sourceXmlObject, SchemaType type, Part part, BindingOperation bindingOperation,
			boolean isRequest )
	{
		this.sourceXmlObject = sourceXmlObject;
		this.part = part;
		this.bindingOperation = bindingOperation;
		this.isRequest = isRequest;
		partXmlObject = type == null ? sourceXmlObject.copy() : sourceXmlObject.copy().changeType( type );
	}

	public void update()
	{
		sourceXmlObject.set( partXmlObject );
	}

	public XmlCursor newCursor()
	{
		return partXmlObject.newCursor();
	}

	public boolean isAttachmentPart()
	{
		return isRequest ? WsdlUtils.isAttachmentInputPart( part, bindingOperation ) : WsdlUtils.isAttachmentOutputPart(
				part, bindingOperation );
	}

	public Part getPart()
	{
		return part;
	}
}
