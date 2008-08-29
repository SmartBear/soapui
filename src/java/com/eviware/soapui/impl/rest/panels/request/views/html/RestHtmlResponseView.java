/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.request.views.html;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestResponseDocument;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestResponseMessageEditor;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@SuppressWarnings("unchecked")
public class RestHtmlResponseView extends AbstractXmlEditorView<RestResponseDocument> implements PropertyChangeListener
{
	private final RestRequest restRequest;
	private JPanel contentPanel;
	private boolean updatingRequest;
	private JPanel panel;
//	private BrComponent browser;

	public RestHtmlResponseView(RestResponseMessageEditor restRequestMessageEditor, RestRequest restRequest)
	{
		super("HTML", restRequestMessageEditor, RestHtmlResponseViewFactory.VIEW_ID);
		this.restRequest = restRequest;

		restRequest.addPropertyChangeListener(this);
	}

	public JComponent getComponent()
	{
		if (panel == null)
		{
			panel = new JPanel(new BorderLayout());

			panel.add(buildToolbar(), BorderLayout.NORTH);
			panel.add(buildContent(), BorderLayout.CENTER);
			panel.add(buildStatus(), BorderLayout.SOUTH);
		}

		return panel;
	}

	@Override
	public void release()
	{
		super.release();

		restRequest.removePropertyChangeListener(this);
	}

	private Component buildStatus()
	{
		return new JPanel();
	}

	private Component buildContent()
	{
		contentPanel = new JPanel(new BorderLayout());

//		org.jdic.web.BrComponent.DESIGN_MODE = false;
//		browser = new BrComponent();

//		javax.swing.GroupLayout brMainLayout = new javax.swing.GroupLayout(browser);

//		browser.setLayout(brMainLayout);

//		brMainLayout.setHorizontalGroup(
//
//		brMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//
//		.addGap(0, 299, Short.MAX_VALUE)
//
//		);
//
//		brMainLayout.setVerticalGroup(
//
//		brMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//
//		.addGap(0, 503, Short.MAX_VALUE)
//
//		);

		HttpResponse response = restRequest.getResponse();
		if (response != null)
			setEditorContent(response);

//		contentPanel.add(new JScrollPane(browser));

		return contentPanel;
	}

	protected void setEditorContent(HttpResponse httpResponse)
	{
		if (httpResponse.getContentType() != null && httpResponse.getContentType().contains("html"))
		{
			try
			{
//				browser.open(httpResponse.getURL().toString());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
//			browser.setHTML("");
		}
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		return toolbar;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(AbstractHttpRequest.RESPONSE_PROPERTY) && !updatingRequest)
		{
			setEditorContent(((HttpResponse) evt.getNewValue()));
		}
	}

	@Override
	public void setXml(String xml)
	{
	}

	public boolean saveDocument(boolean validate)
	{
		return false;
	}

	public void setEditable(boolean enabled)
	{
	}
}