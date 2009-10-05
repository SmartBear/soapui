package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.submit.AbstractMessageExchange;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.types.StringToStringMap;

public abstract class AbstractNonHttpMessageExchange<T extends ModelItem>
		extends AbstractMessageExchange<T> {

	public AbstractNonHttpMessageExchange(T modelItem) {
		super(modelItem);
	}

	@Override
	public Operation getOperation() {
		return null;
	}

	@Override
	public byte[] getRawRequestData() {
		return null;
	}

	@Override
	public byte[] getRawResponseData() {
		return null;
	}

	@Override
	public Attachment[] getRequestAttachments() {
		return null;
	}

	@Override
	public Attachment[] getRequestAttachmentsForPart(String partName) {
		return null;
	}
	
	@Override
	public StringToStringMap getResponseHeaders() {
		return null;
	}

	@Override
	public StringToStringMap getRequestHeaders() {
		return null;
	}

	@Override
	public Attachment[] getResponseAttachments() {
		return null;
	}

	@Override
	public Attachment[] getResponseAttachmentsForPart(String partName) {
		return null;
	}

	@Override
	public boolean hasRawData() {
		return false;
	}
}
