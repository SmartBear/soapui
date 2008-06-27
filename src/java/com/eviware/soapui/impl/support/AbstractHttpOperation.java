package com.eviware.soapui.impl.support;

import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Attachment.AttachmentEncoding;

public interface AbstractHttpOperation extends Operation
{
	AttachmentEncoding getAttachmentEncoding(String part, boolean isRequest );
	
	AbstractInterface<?> getInterface();
}
