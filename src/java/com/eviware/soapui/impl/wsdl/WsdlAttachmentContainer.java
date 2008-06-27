package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.model.iface.Attachment.AttachmentEncoding;

public interface WsdlAttachmentContainer extends AttachmentContainer
{
	public boolean isMtomEnabled();
	
	public boolean isInlineFilesEnabled();
	
	public boolean isEncodeAttachments();
	
	AttachmentEncoding getAttachmentEncoding( String partName );
}
