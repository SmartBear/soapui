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

package com.eviware.soapui.impl.rest.panels.request;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.Document;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequest.RequestMethod;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.panels.AbstractHttpRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;
import com.eviware.soapui.support.xml.XmlUtils;

public class RestRequestDesktopPanel extends AbstractHttpRequestDesktopPanel<RestRequest, RestRequest> implements
		PropertyChangeListener
{
	private boolean updatingRequest;
	private JComboBox methodCombo;
	private JComboBox mediaTypeCombo;
	private JUndoableTextField pathField;
	private JButton recreatePathButton;

	public RestRequestDesktopPanel(RestRequest modelItem)
	{
		super(modelItem, modelItem);
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals("method") && !updatingRequest)
		{
			methodCombo.setSelectedItem(evt.getNewValue());
		}
		else if (evt.getPropertyName().equals("mediaType") && !updatingRequest)
		{
			mediaTypeCombo.setSelectedItem((String) evt.getNewValue());
		}
	}

	@Override
	protected ModelItemXmlEditor<?, ?> buildRequestEditor()
	{
		return new RestRequestMessageEditor(getModelItem());
	}

	@Override
	protected ModelItemXmlEditor<?, ?> buildResponseEditor()
	{
		return new RestResponseMessageEditor(getModelItem());
	}

	@Override
	protected Submit doSubmit() throws SubmitException
	{
		return getRequest().submit(new WsdlSubmitContext(getModelItem()), true);
	}

	@Override
	protected String getHelpUrl()
	{
		return null;
	}

	@Override
	protected JComponent buildToolbar()
	{
		recreatePathButton = createActionButton(new RecreatePathAction(), true);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(super.buildToolbar(), BorderLayout.NORTH);

		JXToolBar toolbar = UISupport.createToolbar();
		toolbar.addSpace(3);
		methodCombo = new JComboBox(new Object[] { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
				RequestMethod.DELETE, RequestMethod.HEAD });

		methodCombo.setSelectedItem(getModelItem().getMethod());
		methodCombo.addItemListener(new ItemListener()
		{

			public void itemStateChanged(ItemEvent e)
			{
				updatingRequest = true;
				getModelItem().setMethod((RequestMethod) methodCombo.getSelectedItem());
				updatingRequest = false;
			}
		});

		toolbar.addLabeledFixed("Method", methodCombo);
		toolbar.addSeparator();

		pathField = new JUndoableTextField();
		pathField.setPreferredSize(new Dimension(250, 20));
		pathField.setText(getModelItem().getPath());
		pathField.getDocument().addDocumentListener(new DocumentListenerAdapter()
		{

			@Override
			public void update(Document document)
			{
				getModelItem().setPath(pathField.getText());
			}
		});

		toolbar.addLabeledFixed("Resource Path:", pathField);
		toolbar.add(recreatePathButton);

		toolbar.addSeparator();

		mediaTypeCombo = new JComboBox(new Object[] { getModelItem().getMediaType() });
		mediaTypeCombo.setEditable(true);
		mediaTypeCombo.addItemListener(new ItemListener()
		{

			public void itemStateChanged(ItemEvent e)
			{
				updatingRequest = true;
				getModelItem().setMediaType((String) mediaTypeCombo.getSelectedItem());
				updatingRequest = false;
			}
		});

		toolbar.addLabeledFixed("Media Type", mediaTypeCombo);
		toolbar.addSeparator();

		panel.add(toolbar, BorderLayout.SOUTH);
		return panel;
	}

	@Override
	protected void insertButtons(JXToolBar toolbar)
	{
	}

	public class RestRequestMessageEditor extends
			AbstractHttpRequestDesktopPanel<?, ?>.AbstractHttpRequestMessageEditor<RestRequestDocument>
	{
		public RestRequestMessageEditor(RestRequest modelItem)
		{
			super(new RestRequestDocument(modelItem));
		}
	}

	public class RestResponseMessageEditor extends
			AbstractHttpRequestDesktopPanel<?, ?>.AbstractHttpResponseMessageEditor<RestResponseDocument>
	{
		public RestResponseMessageEditor(RestRequest modelItem)
		{
			super(new RestResponseDocument(modelItem));
		}
	}

	public class RestRequestDocument extends AbstractXmlDocument
	{
		private final RestRequest modelItem;

		public RestRequestDocument(RestRequest modelItem)
		{
			this.modelItem = modelItem;
		}

		public RestRequest getRequest()
		{
			return modelItem;
		}

		public String getXml()
		{
			return getModelItem().getRequestContent();
		}

		public void setXml(String xml)
		{
			getModelItem().setRequestContent(xml);
		}
	}

	public class RestResponseDocument extends AbstractXmlDocument implements PropertyChangeListener
	{
		private final RestRequest modelItem;

		public RestResponseDocument(RestRequest modelItem)
		{
			this.modelItem = modelItem;

			modelItem.addPropertyChangeListener(RestRequest.RESPONSE_PROPERTY, this);
		}

		public RestRequest getRequest()
		{
			return modelItem;
		}

		public String getXml()
		{
			HttpResponse response = getModelItem().getResponse();
			if (response == null)
				return "";
			String content = response.getContentAsString();

			if (response.getContentType().equals("text/javascript"))
			{
				try
				{
					JSONObject json = JSONObject.fromObject(content);
					XMLSerializer serializer = new XMLSerializer();
					serializer.setTypeHintsEnabled(false);
					content = serializer.write(json);
					content = XmlUtils.prettyPrintXml(content);
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}
			
			return content;
		}

		public void setXml(String xml)
		{
			HttpResponse response = getModelItem().getResponse();
			if (response != null)
				response.setResponseContent(xml);
		}

		public void propertyChange(PropertyChangeEvent evt)
		{
			fireXmlChanged(evt.getOldValue() == null ? null : ((HttpResponse) evt.getOldValue()).getContentAsString(),
					getXml());
		}
	}

	private class RecreatePathAction extends AbstractAction
	{
		public RecreatePathAction()
		{
			super();
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/recreate_request.gif"));
		}

		public void actionPerformed(ActionEvent e)
		{
			getModelItem().setPath(getModelItem().getResource().getFullPath());
		}
	}
}
