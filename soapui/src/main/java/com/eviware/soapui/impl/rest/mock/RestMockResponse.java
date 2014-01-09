package com.eviware.soapui.impl.rest.mock;


import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.MutableWsdlAttachmentContainer;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RestMockResponse extends AbstractWsdlModelItem<RESTMockResponseConfig> implements MockResponse,
		MutableWsdlAttachmentContainer, PropertyExpansionContainer, TestPropertyHolder
{

	String responseContent;

	protected RestMockResponse( RESTMockResponseConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
	}

	@Override
	public int getAttachmentCount()
	{
		return 0;
	}

	@Override
	public Attachment getAttachmentAt( int index )
	{
		return null;
	}

	@Override
	public Attachment[] getAttachmentsForPart( String partName )
	{
		return new Attachment[0];
	}

	@Override
	public MessagePart.AttachmentPart[] getDefinedAttachmentParts()
	{
		return new MessagePart.AttachmentPart[0];
	}

	@Override
	public MessagePart.AttachmentPart getAttachmentPart( String partName )
	{
		return null;
	}

	@Override
	public void addAttachmentsChangeListener( PropertyChangeListener listener )
	{

	}

	@Override
	public void removeAttachmentsChangeListener( PropertyChangeListener listener )
	{

	}

	@Override
	public boolean isMultipartEnabled()
	{
		return false;
	}

	@Override
	public String getResponseContent()
	{
		return null;
	}

	@Override
	public void setResponseContent( String responseContent )
	{

	}

	@Override
	public String getEncoding()
	{
		return null;
	}

	@Override
	public boolean isMtomEnabled()
	{
		return false;
	}

	@Override
	public boolean isInlineFilesEnabled()
	{
		return false;
	}

	@Override
	public boolean isEncodeAttachments()
	{
		return false;
	}

	@Override
	public Attachment.AttachmentEncoding getAttachmentEncoding( String partName )
	{
		return null;
	}

	@Override
	public Attachment[] getAttachments()
	{
		return new Attachment[0];
	}

	@Override
	public MockOperation getMockOperation()
	{
		return null;
	}

	@Override
	public StringToStringsMap getResponseHeaders()
	{
		return null;
	}

	@Override
	public MockResult getMockResult()
	{
		return null;
	}

	@Override
	public Attachment attachFile( File file, boolean cache ) throws IOException
	{
		return null;
	}

	@Override
	public void removeAttachment( Attachment attachment )
	{

	}

	@Override
	public PropertyExpansion[] getPropertyExpansions()
	{
		return new PropertyExpansion[0];
	}

	@Override
	public String[] getPropertyNames()
	{
		return new String[0];
	}

	@Override
	public void setPropertyValue( String name, String value )
	{

	}

	@Override
	public String getPropertyValue( String name )
	{
		return null;
	}

	@Override
	public TestProperty getProperty( String name )
	{
		return null;
	}

	@Override
	public Map<String, TestProperty> getProperties()
	{
		return null;
	}

	@Override
	public void addTestPropertyListener( TestPropertyListener listener )
	{

	}

	@Override
	public void removeTestPropertyListener( TestPropertyListener listener )
	{

	}

	@Override
	public boolean hasProperty( String name )
	{
		return false;
	}

	@Override
	public ModelItem getModelItem()
	{
		return null;
	}

	@Override
	public int getPropertyCount()
	{
		return 0;
	}

	@Override
	public List<TestProperty> getPropertyList()
	{
		return null;
	}

	@Override
	public TestProperty getPropertyAt( int index )
	{
		return null;
	}

	@Override
	public String getPropertiesLabel()
	{
		return null;
	}
}
