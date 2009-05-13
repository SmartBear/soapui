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

import java.beans.PropertyChangeListener;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart.AttachmentPart;

/**
 * Behaviour for ModelItems that contain attachments (Requests and
 * MockResponses)
 * 
 * @author ole.matzura
 */

public interface AttachmentContainer
{
	public int getAttachmentCount();

	public Attachment getAttachmentAt( int index );

	public Attachment[] getAttachmentsForPart( String partName );

	public Attachment[] getAttachments();

	public AttachmentPart[] getDefinedAttachmentParts();

	public AttachmentPart getAttachmentPart( String partName );

	public static final String ATTACHMENTS_PROPERTY = WsdlRequest.class.getName() + "@attachments";

	public void addAttachmentsChangeListener( PropertyChangeListener listener );

	public void removeAttachmentsChangeListener( PropertyChangeListener listener );

	public boolean isMultipartEnabled();

	/**
	 * Returns ModelItem associated with this container
	 */

	public ModelItem getModelItem();
}