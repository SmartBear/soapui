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

package com.eviware.soapui.impl.support.panels;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

public abstract class AbstractHttpXmlRequestDesktopPanel<T extends ModelItem, T2 extends HttpRequestInterface<?>>
		extends AbstractHttpRequestDesktopPanel<T, T2>
{

	public AbstractHttpXmlRequestDesktopPanel( T modelItem, T2 requestItem )
	{
		super( modelItem, requestItem );
	}

	@Override
	protected ModelItemXmlEditor<?, ?> buildRequestEditor()
	{
		return new HttpRequestMessageEditor( getRequest() );
	}

	@Override
	protected ModelItemXmlEditor<?, ?> buildResponseEditor()
	{
		return new HttpResponseMessageEditor( getRequest() );
	}

	public class HttpRequestMessageEditor extends
			AbstractHttpRequestDesktopPanel<?, ?>.AbstractHttpRequestMessageEditor<HttpRequestDocument>
	{
		public HttpRequestMessageEditor( HttpRequestInterface<?> modelItem )
		{
			super( new HttpRequestDocument( modelItem ) );
		}
	}

	public class HttpResponseMessageEditor extends
			AbstractHttpRequestDesktopPanel<?, ?>.AbstractHttpResponseMessageEditor<HttpResponseDocument>
	{
		public HttpResponseMessageEditor( HttpRequestInterface<?> modelItem )
		{
			super( new HttpResponseDocument( modelItem ) );
		}
	}

	public class HttpRequestDocument extends AbstractXmlDocument implements PropertyChangeListener
	{
		private final HttpRequestInterface<?> modelItem;
		private boolean updating;

		public HttpRequestDocument( HttpRequestInterface<?> modelItem )
		{
			this.modelItem = modelItem;

			modelItem.addPropertyChangeListener( this );
		}

		public HttpRequestInterface<?> getRequest()
		{
			return modelItem;
		}

		public String getXml()
		{
			return getRequest().getRequestContent();
		}

		@Override
		public void release()
		{
			super.release();
			modelItem.removePropertyChangeListener( this );
		}

		public void setXml( String xml )
		{
			if( !updating )
			{
				updating = true;
				getRequest().setRequestContent( xml );
				updating = false;
			}
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( RestRequestInterface.REQUEST_PROPERTY ) && !updating )
			{
				updating = true;
				fireXmlChanged( ( String )evt.getOldValue(), ( String )evt.getNewValue() );
				updating = false;
			}
		}
	}

	public class HttpResponseDocument extends AbstractXmlDocument implements PropertyChangeListener
	{
		private final HttpRequestInterface<?> modelItem;

		public HttpResponseDocument( HttpRequestInterface<?> modelItem )
		{
			this.modelItem = modelItem;

			modelItem.addPropertyChangeListener( RestRequestInterface.RESPONSE_PROPERTY, this );
		}

		public HttpRequestInterface<?> getRequest()
		{
			return modelItem;
		}

		public String getXml()
		{
			return modelItem.getResponseContentAsXml();
		}

		public void setXml( String xml )
		{
			HttpResponse response = getRequest().getResponse();
			if( response != null )
				response.setResponseContent( xml );
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			fireXmlChanged( evt.getOldValue() == null ? null : ( ( HttpResponse )evt.getOldValue() ).getContentAsString(),
					getXml() );
		}

		public void release()
		{
			super.release();
			modelItem.removePropertyChangeListener( RestRequestInterface.RESPONSE_PROPERTY, this );
		}
	}
}
