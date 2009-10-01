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

package com.eviware.soapui.support.editor.inspectors.jms;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.support.jms.JMSContainer;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.jgoodies.binding.PresentationModel;

public abstract class AbstractJMSInspector extends AbstractXmlInspector
{
	private JPanel mainPanel;
	private SimpleBindingForm form;
	private final JMSContainer jmsContainer;

	protected AbstractJMSInspector(JMSContainer jmsContainer)
	{
		super("JMS Headers", "JMS header settings and properties", true, JMSInspectorFactory.INSPECTOR_ID);
		this.jmsContainer = jmsContainer;
	}

	public JComponent getComponent()
	{
		if (mainPanel == null)
		{
			mainPanel = new JPanel(new BorderLayout());
			form = new SimpleBindingForm(new PresentationModel<AbstractHttpRequest<?>>(jmsContainer.getJMSConfig()));
			buildContent(form);
			mainPanel.add(new JScrollPane(form.getPanel()), BorderLayout.CENTER);
		}
		return mainPanel;
	}

	public abstract void buildContent(SimpleBindingForm form);

	@Override
	public boolean isEnabledFor(EditorView<XmlDocument> view)
	{
		return !view.getViewId().equals(RawXmlEditorFactory.VIEW_ID);
	}

}