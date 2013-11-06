/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.request.inspectors.schema;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wadl.inference.ConflictHandler;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSResponse;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.log.JLogList;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * @author Dain.Nilsson
 */
public class InferredSchemaInspector extends AbstractXmlInspector implements SubmitListener
{
	private SchemaTabs tabs;
	private RestService service;
	private RestRequest request;
	private Handler handler;
	private Thread thread;

	protected InferredSchemaInspector( RestRequest request )
	{
		super( "Schema", "Inferred Schema", true, InferredSchemaInspectorFactory.INSPECTOR_ID );
		service = request.getResource().getService();
		this.request = request;

		request.addSubmitListener( this );
	}

	public JComponent getComponent()
	{
		if( tabs == null )
		{
			tabs = new SchemaTabs();
			InferredSchemaManager.addPropertyChangeListener( service, tabs );
		}

		return tabs;
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}

	public void afterSubmit( Submit submit, SubmitContext context )
	{
		if( submit.getResponse() == null )
			return;
		HttpResponse httpResponse = ( HttpResponse )submit.getResponse();
		String content = httpResponse.getContentAsXml();
		if( content == null || content.equals( "<xml/>" ) )
			return;
		XmlObject xml;
		try
		{
			URL url = httpResponse.getURL();
			String defaultNamespace = null;
			if( url != null )
			{
				defaultNamespace = url.getProtocol() + "://" + url.getHost();
			}
			else
			{
				if( httpResponse instanceof JMSResponse )
				{
					defaultNamespace = ( ( JMSResponse )httpResponse ).getEndpoint();
				}
			}
			XmlOptions options = new XmlOptions().setLoadSubstituteNamespaces( Collections.singletonMap( "",
					defaultNamespace ) );
			// xml = XmlObject.Factory.parse( content, options );
			xml = XmlUtils.createXmlObject( content, options );
		}
		catch( XmlException e )
		{
			e.printStackTrace();
			return;
		}
		if( !submit.getStatus().equals( Status.CANCELED )
				&& !InferredSchemaManager.getInferredSchema( service ).validate( xml ) )
		{
			setTitle( "Schema (conflicts)" );
			if( thread != null && thread.isAlive() )
			{
				handler.kill();
				try
				{
					thread.join();
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
			handler = new Handler( tabs, xml );
			thread = new Thread( handler );
			thread.start();
		}
	}

	public boolean beforeSubmit( Submit submit, SubmitContext context )
	{
		return true;
	}

	public void release()
	{
		super.release();

		request.removeSubmitListener( this );
		InferredSchemaManager.removePropertyChangeListener( service, tabs );

		if( thread != null && thread.isAlive() )
		{
			handler.kill();
		}
	}

	@SuppressWarnings( "serial" )
	private class SchemaTabs extends JTabbedPane implements ActionListener, PropertyChangeListener,
			ListSelectionListener
	{
		private JLogList log;
		private JPanel conflicts;
		private JButton resolveButton;
		private JCheckBox auto;
		private Handler handler;
		private RSyntaxTextArea xsd;
		private JList schemaList;
		public static final String AUTO_INFER_SCHEMAS = "AutoInferSchemas";
		public static final String NO_NAMESPACE = "<no namespace>";

		public SchemaTabs()
		{
			super();
			conflicts = new JPanel();
			conflicts.setLayout( new BorderLayout() );
			auto = new JCheckBox( "Auto-Resolve" );
			auto.setToolTipText( "Automatically modify inferred schema from received Responses" );
			auto.setOpaque( false );
			UISupport.setFixedSize( auto, 120, 20 );
			XmlBeansSettingsImpl settings = getRequest().getSettings();
			if( settings.isSet( AUTO_INFER_SCHEMAS ) )
			{
				auto.setSelected( settings.getBoolean( AUTO_INFER_SCHEMAS ) );
			}
			auto.addItemListener( new ItemListener()
			{
				public void itemStateChanged( ItemEvent e )
				{
					getRequest().getSettings().setBoolean( AUTO_INFER_SCHEMAS, auto.isSelected() );
				}
			} );
			resolveButton = new JButton( "Resolve conflicts" );
			resolveButton.setEnabled( false );
			resolveButton.setActionCommand( "resolve" );
			resolveButton.addActionListener( this );

			JXToolBar toolbar = UISupport.createToolbar();
			toolbar.addFixed( auto );
			toolbar.addFixed( resolveButton );

			log = new JLogList( "Schema log" );
			conflicts.add( toolbar, BorderLayout.NORTH );
			conflicts.add( log, BorderLayout.CENTER );
			addTab( "Conflicts", conflicts );

			schemaList = new JList( InferredSchemaManager.getInferredSchema( service ).getNamespaces() );
			schemaList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			schemaList.addListSelectionListener( this );

			toolbar = UISupport.createToolbar();
			toolbar.addFixed( UISupport.createToolbarButton( new RemoveNamespaceAction() ) );

			JPanel listPanel = new JPanel();
			listPanel.setLayout( new BorderLayout() );
			listPanel.add( toolbar, BorderLayout.NORTH );
			listPanel.add( new JScrollPane( schemaList ), BorderLayout.CENTER );
			xsd = SyntaxEditorUtil.createDefaultXmlSyntaxTextArea();
			xsd.setEditable( false );
			update();
			addTab( "Schemas", new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, listPanel, new JScrollPane( xsd ) ) );
		}

		public synchronized boolean awaitButton( Handler handler )
		{
			if( auto.isSelected() )
				return false;
			resolveButton.setEnabled( true );
			this.handler = handler;
			return true;
		}

		public synchronized void actionPerformed( ActionEvent e )
		{
			if( e.getActionCommand().equals( "resolve" ) )
			{
				resolveButton.setEnabled( false );
				handler.go();
			}
			else if( e.getActionCommand().equals( "save" ) )
			{
				InferredSchemaManager.save( service );
			}
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			update();
		}

		public void update()
		{
			String[] namespaces = InferredSchemaManager.getInferredSchema( service ).getNamespaces();
			for( int i = 0; i < namespaces.length; i++ )
				if( namespaces[i].equals( "" ) )
					namespaces[i] = NO_NAMESPACE;
			schemaList.setListData( namespaces );
			if( schemaList.isSelectionEmpty() )
			{
				xsd.setText( "" );
			}
			else
			{
				xsd.setText( XmlUtils.prettyPrintXml( InferredSchemaManager.getInferredSchema( service )
						.getXsdForNamespace( ( String )schemaList.getSelectedValue() ) ) );
				xsd.setCaretPosition( 0 );
				xsd.scrollRectToVisible( new Rectangle( 0, 0, ( int )( getSize().getWidth() ), ( int )( getSize()
						.getHeight() ) ) );
			}
		}

		public void logln( String line )
		{
			log.addLine( line );
		}

		public void valueChanged( ListSelectionEvent e )
		{
			if( e.getValueIsAdjusting() == false )
			{
				if( !schemaList.isSelectionEmpty() )
				{
					String namespace = ( String )schemaList.getSelectedValue();
					if( namespace.equals( NO_NAMESPACE ) )
						namespace = "";
					xsd.setText( XmlUtils.prettyPrintXml( InferredSchemaManager.getInferredSchema( service )
							.getXsdForNamespace( namespace ) ) );
					xsd.setCaretPosition( 0 );
					xsd.scrollRectToVisible( new Rectangle( 0, 0, ( int )( getSize().getWidth() ), ( int )( getSize()
							.getHeight() ) ) );
				}
			}
		}

		private class RemoveNamespaceAction extends AbstractAction
		{
			private RemoveNamespaceAction()
			{
				putValue( SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
				putValue( SHORT_DESCRIPTION, "Removes selected inferred namespace definition" );
			}

			public void actionPerformed( ActionEvent e )
			{
				if( !schemaList.isSelectionEmpty() )
				{
					String ns = ( String )schemaList.getSelectedValue();
					if( UISupport.confirm( "Remove inferred namespace '" + ns + "'?", "Remove namespace" ) )
					{
						if( ns.equals( NO_NAMESPACE ) )
							ns = "";
						InferredSchemaManager.deleteNamespace( service, ns );
					}
				}
			}
		}
	}

	public class Handler implements ConflictHandler, Runnable
	{
		private SchemaTabs panel;
		private XmlObject xml;
		private List<String> paths;
		private boolean yesToAll = false;
		private boolean kill = false;

		public Handler( SchemaTabs panel, XmlObject xml )
		{
			this.panel = panel;
			this.xml = xml;
			paths = new ArrayList<String>();
		}

		public synchronized void run()
		{
			try
			{
				if( panel.awaitButton( this ) )
				{
					try
					{
						wait();
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
				else
					yesToAll = true;
				if( kill )
					return;
				InferredSchemaManager.getInferredSchema( service ).learningValidate( xml, this );
				panel.update();
				setTitle( "Schema" );
				InferredSchemaManager.save( service );
			}
			catch( XmlException e )
			{
				setTitle( "Schema (invalid)" );
			}
		}

		public synchronized void go()
		{
			notifyAll();
		}

		public synchronized void kill()
		{
			kill = true;
			notifyAll();
		}

		public boolean callback( Event event, Type type, QName name, String path, String message )
		{

			// if(paths.contains(path)) return true;
			StringBuilder s = new StringBuilder( message ).append( " " );
			if( event == Event.CREATION )
			{
				paths.add( path );
				s.append( "Create " );
			}
			else if( event == Event.MODIFICATION )
			{
				paths.add( path );
				s.append( "Modify " );
			}
			if( type == Type.ELEMENT )
				s.append( "element '" );
			else if( type == Type.ATTRIBUTE )
				s.append( "attribute '" );
			else if( type == Type.TYPE )
				s.append( "type '" );
			s.append( name.getLocalPart() ).append( "' in namespace '" ).append( name.getNamespaceURI() )
					.append( "' at path " ).append( path ).append( "?" );
			if( !yesToAll )
			{
				int choice = UISupport.yesYesToAllOrNo( s.toString(), "Conflict" );
				if( choice == 2 )
				{
					panel.logln( s.append( " FAIL" ).toString() );
					return false;
				}
				else if( choice == 1 )
					yesToAll = true;
			}
			panel.logln( s.append( " OK" ).toString() );
			return true;
		}

	}

	public RestRequest getRequest()
	{
		return request;
	}
}
