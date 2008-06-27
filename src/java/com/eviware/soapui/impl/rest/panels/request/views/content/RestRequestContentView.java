package com.eviware.soapui.impl.rest.panels.request.views.content;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.Document;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.RestDocument;
import com.eviware.soapui.impl.rest.panels.request.RestRequestMessageEditor;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.support.AbstractEditorView;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.support.xml.XmlUtils;

public class RestRequestContentView extends AbstractEditorView<RestDocument> implements PropertyChangeListener
{
	private final RestRequest restRequest;
	private JPanel contentPanel;
	private JXEditTextArea contentEditor;
	private boolean updatingRequest;

	public RestRequestContentView(RestRequestMessageEditor restRequestMessageEditor, RestRequest restRequest)
	{
		super( "REST Content", restRequestMessageEditor, RestRequestContentViewFactory.VIEW_ID );
		this.restRequest = restRequest;
		
		restRequest.addPropertyChangeListener( this );
	}

	@Override
	public JComponent buildUI()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		
		panel.add( buildToolbar(), BorderLayout.NORTH );
		panel.add( buildContent(), BorderLayout.CENTER );
		panel.add( buildStatus(), BorderLayout.SOUTH );
		
		return panel;
	}

	@Override
	public void release()
	{
		super.release();
		
		restRequest.removePropertyChangeListener( this );
	}

	private Component buildStatus()
	{
		return new JPanel();
	}

	private Component buildContent()
	{
		contentPanel = new JPanel( new BorderLayout() );
		
		contentEditor = JXEditTextArea.createXmlEditor(true);
		contentEditor.setText( XmlUtils.prettyPrintXml( restRequest.getRequestContent() ) );
		
		contentEditor.getDocument().addDocumentListener(new DocumentListenerAdapter() {

			@Override
			public void update(Document document)
			{
				updatingRequest = true;
				restRequest.setRequestContent( contentEditor.getText() );
				updatingRequest = false;
			}} );
		
		contentPanel.add( new JScrollPane( contentEditor ));
		
		contentEditor.setEditable( restRequest.hasRequestBody() );
		
		return contentPanel;
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		
		return toolbar;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if( evt.getPropertyName().equals( "request" ) && !updatingRequest )
		{
			contentEditor.setText( (String)evt.getNewValue() );
		}
	}
}