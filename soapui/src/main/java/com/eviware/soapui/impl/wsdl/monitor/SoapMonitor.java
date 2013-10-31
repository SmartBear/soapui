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

package com.eviware.soapui.impl.wsdl.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.collections.list.TreeList;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.PatternFilter;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.LaunchForm;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeRequestMessageEditor;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeResponseMessageEditor;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.HttpRequestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.DateUtil;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.BrowserComponent;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * A SOAP Monitor..
 */

@SuppressWarnings( "serial" )
public class SoapMonitor extends JPanel
{
	private static final String ALL_FILTER_OPTION = "- all -";
	private JProgressBar progressBar;
	private JButton stopButton = null;

	private JXTable logTable = null;
	private MonitorLogTableModel tableModel = null;

	private String httpProxyHost = null;
	private int httpProxyPort = 80;

	private JButton startButton;
	private final WsdlProject project;
	private MessageExchangeRequestMessageEditor requestViewer;
	private MessageExchangeResponseMessageEditor responseViewer;

	private MessageExchangeModelItem requestModelItem;
	private JButton optionsButton;
	private int listenPort;
	private String targetEndpoint;
	private JButton clearButton;
	private int maxRows;
	private JButton addToTestCaseButton;
	// private JButton addToRestTestCaseButton;
	private JButton createRequestButton;
	private JButton addToMockServiceButton;
	private Stack<WsdlMonitorMessageExchange> messageExchangeStack = new Stack<WsdlMonitorMessageExchange>();
	private StackProcessor stackProcessor = new StackProcessor();
	private PatternFilter operationFilter;
	private PatternFilter interfaceFilter;
	private PatternFilter targetHostFilter;
	private JLabel infoLabel;
	private PatternFilter requestHostFilter;
	private JComboBox requestHostFilterCombo;
	private JComboBox targetHostFilterCombo;
	private JComboBox interfaceFilterCombo;
	private JComboBox operationFilterCombo;
	private DefaultComboBoxModel operationFilterModel;
	private DefaultComboBoxModel requestFilterModel;
	private DefaultComboBoxModel targetHostFilterModel;
	private JLabel rowCountLabel = new JLabel();
	private Map<AbstractInterface<?>, String> addedEndpoints;
	private JXToolBar toolbar;
	private String incomingRequestWss;
	private String incomingResponseWss;
	private boolean setAsProxy;
	private XFormDialog optionsDialog;
	private SoapMonitorEngine monitorEngine;
	private String oldProxyHost;
	private String oldProxyPort;
	private boolean oldProxyEnabled;
	private boolean oldProxyAuto;
	private String sslEndpoint;
	private JInspectorPanel inspectorPanel;
	private SoapMonitorListenerCallBack listenerCallBack;

	public SoapMonitor( WsdlProject project, int listenPort, String incomingRequestWss, String incomingResponseWss,
			JXToolBar mainToolbar, boolean setAsProxy, String sslEndpoint )
	{
		super( new BorderLayout() );
		this.project = project;
		this.listenPort = listenPort;
		this.incomingRequestWss = incomingRequestWss;
		this.incomingResponseWss = incomingResponseWss;
		this.setAsProxy = setAsProxy;
		this.maxRows = 100;
		this.sslEndpoint = sslEndpoint;

		// set the slow link to the passed down link

		this.setLayout( new BorderLayout() );

		add( buildToolbars( mainToolbar ), BorderLayout.NORTH );
		add( buildContent(), BorderLayout.CENTER );

		start();
	}

	public SoapMonitor( WsdlProject project, int sourcePort, String incomingRequestWss, String incomingResponseWss,
			JXToolBar toolbar, boolean setAsProxy )
	{
		this( project, sourcePort, incomingRequestWss, incomingResponseWss, toolbar, setAsProxy, null );
	}

	private JComponent buildContent()
	{
		inspectorPanel = JInspectorPanelFactory.build( buildLog() );

		JComponentInspector<JComponent> viewInspector = new JComponentInspector<JComponent>( buildViewer(),
				"Message Content", "Shows message content", true );
		inspectorPanel.addInspector( viewInspector );

		return inspectorPanel.getComponent();
	}

	private JComponent buildLog()
	{
		tableModel = new MonitorLogTableModel();
		logTable = new JXTable( 1, 2 );
		logTable.setColumnControlVisible( true );
		logTable.setModel( tableModel );
		logTable.setHorizontalScrollEnabled( true );
		logTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

		operationFilter = new PatternFilter( ".*", 0, 4 );
		operationFilter.setAcceptNull( true );
		interfaceFilter = new PatternFilter( ".*", 0, 3 );
		interfaceFilter.setAcceptNull( true );
		targetHostFilter = new PatternFilter( ".*", 0, 2 );
		targetHostFilter.setAcceptNull( true );
		requestHostFilter = new PatternFilter( ".*", 0, 1 );
		requestHostFilter.setAcceptNull( true );

		Filter[] filters = new Filter[] { requestHostFilter, targetHostFilter, interfaceFilter, operationFilter };

		FilterPipeline pipeline = new FilterPipeline( filters );
		logTable.setFilters( pipeline );

		ListSelectionModel sel = logTable.getSelectionModel();
		sel.addListSelectionListener( new ListSelectionListener()
		{
			public void valueChanged( ListSelectionEvent event )
			{
				int row = logTable.getSelectedRow();
				if( row == -1 )
				{
					// requestXmlDocument.setXml( null );
					// responseXmlDocument.setXml( null );
					requestModelItem.setMessageExchange( null );
				}
				else
				{
					WsdlMonitorMessageExchange exchange = tableModel.getMessageExchangeAt( row );
					requestModelItem.setMessageExchange( exchange );
					// responseModelItem.setMessageExchange( exchange );
					// requestXmlDocument.setXml( exchange.getRequestContent() );
					// responseXmlDocument.setXml( exchange.getResponseContent() );
				}

				addToMockServiceButton.setEnabled( row != -1 );
				addToTestCaseButton.setEnabled( row != -1 );
				// addToRestTestCaseButton.setEnabled( row != -1 );
				createRequestButton.setEnabled( row != -1 );
			}
		} );

		JPanel tablePane = new JPanel();
		tablePane.setLayout( new BorderLayout() );

		toolbar.addGlue();

		tablePane.add( buildFilterBar(), BorderLayout.NORTH );
		tablePane.add( new JScrollPane( logTable ), BorderLayout.CENTER );

		return tablePane;
	}

	private JPanel buildFilterBar()
	{
		requestFilterModel = new DefaultComboBoxModel( new String[] { ALL_FILTER_OPTION } );
		targetHostFilterModel = new DefaultComboBoxModel( new String[] { ALL_FILTER_OPTION } );
		Dimension comboBoxSize = new Dimension( 90, 18 );
		requestHostFilterCombo = UISupport.setFixedSize( new JComboBox( requestFilterModel ), comboBoxSize );

		// toolbar.addFixed( new JLabel( "<html><b>Filter:</b></html>"));
		// toolbar.addUnrelatedGap();

		ButtonBarBuilder toolbar = new ButtonBarBuilder();

		toolbar.addFixed( new JLabel( "Request Host" ) );
		toolbar.addRelatedGap();
		toolbar.addFixed( requestHostFilterCombo );
		toolbar.addUnrelatedGap();

		requestHostFilterCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				int ix = requestHostFilterCombo.getSelectedIndex();
				if( ix == -1 )
					return;

				requestHostFilter.setAcceptNull( ix == 0 );

				if( ix == 0 )
					requestHostFilter.setPattern( ".*", 0 );
				else
					requestHostFilter.setPattern( requestHostFilterCombo.getSelectedItem().toString(), 0 );

				updateRowCountLabel();
			}
		} );

		toolbar.addFixed( new JLabel( "Target Host" ) );
		toolbar.addRelatedGap();
		targetHostFilterCombo = UISupport.setFixedSize( new JComboBox( targetHostFilterModel ), comboBoxSize );
		toolbar.addFixed( targetHostFilterCombo );
		toolbar.addUnrelatedGap();

		targetHostFilterCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				int ix = targetHostFilterCombo.getSelectedIndex();
				if( ix == -1 )
					return;

				targetHostFilter.setAcceptNull( ix == 0 );

				if( ix == 0 )
					targetHostFilter.setPattern( ".*", 0 );
				else
					targetHostFilter.setPattern( targetHostFilterCombo.getSelectedItem().toString(), 0 );

				updateRowCountLabel();
			}
		} );

		String[] interfaceNames = ModelSupport.getNames( new String[] { ALL_FILTER_OPTION },
				ModelSupport.getChildren( getProject(), WsdlInterface.class ) );

		toolbar.addFixed( new JLabel( "Interface" ) );
		toolbar.addRelatedGap();
		interfaceFilterCombo = UISupport.setFixedSize( new JComboBox( interfaceNames ), comboBoxSize );
		toolbar.addFixed( interfaceFilterCombo );
		toolbar.addUnrelatedGap();

		operationFilterModel = new DefaultComboBoxModel( new String[] { ALL_FILTER_OPTION } );
		interfaceFilterCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				String item = ( String )interfaceFilterCombo.getSelectedItem();
				operationFilterModel.removeAllElements();

				if( item == null || getProject().getInterfaceByName( item ) == null )
				{
					operationFilterModel.addElement( ALL_FILTER_OPTION );
					interfaceFilter.setPattern( ".*", 0 );
				}
				else if( getProject().getInterfaceByName( item ) != null )
				{
					WsdlInterface iface = ( WsdlInterface )getProject().getInterfaceByName( item );
					String[] operationNames = ModelSupport.getNames( new String[] { ALL_FILTER_OPTION },
							iface.getOperationList() );
					for( String s : operationNames )
						operationFilterModel.addElement( s );

					interfaceFilter.setPattern( iface.getName(), 0 );
				}
			}
		} );

		toolbar.addFixed( new JLabel( "Operation" ) );
		toolbar.addRelatedGap();
		operationFilterCombo = UISupport.setFixedSize( new JComboBox( operationFilterModel ), comboBoxSize );
		toolbar.addFixed( operationFilterCombo );

		operationFilterCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				int ix = operationFilterCombo.getSelectedIndex();
				if( ix == -1 )
				{
					operationFilter.setPattern( ".*", 0 );
					updateRowCountLabel();
					return;
				}

				operationFilter.setAcceptNull( ix == 0 );

				if( ix == 0 )
					operationFilter.setPattern( ".*", 0 );
				else
					operationFilter.setPattern( operationFilterCombo.getSelectedItem().toString(), 0 );

				updateRowCountLabel();
			}
		} );

		toolbar.setBorder( BorderFactory.createEmptyBorder( 3, 2, 3, 0 ) );
		return toolbar.getPanel();
	}

	protected void updateRowCountLabel()
	{
		rowCountLabel.setText( logTable.getRowCount() + "/" + tableModel.getRowCount() + " entries" );
	}

	private JComponent buildViewer()
	{
		requestModelItem = new MessageExchangeModelItem( "monitor message exchange", null )
		{

			@Override
			public boolean hasRawData()
			{
				return true;
			}
		};

		requestViewer = new MessageExchangeRequestMessageEditor( requestModelItem );
		responseViewer = new MessageExchangeResponseMessageEditor( requestModelItem );

		return UISupport.createHorizontalSplit( requestViewer, responseViewer );
	}

	private JComponent buildToolbars( JXToolBar mainToolbar )
	{
		toolbar = UISupport.createSmallToolbar();
		mainToolbar.addFixed( startButton = UISupport.createToolbarButton( UISupport
				.createImageIcon( "/run_testcase.gif" ) ) );
		mainToolbar.addFixed( stopButton = UISupport.createToolbarButton( UISupport
				.createImageIcon( "/stop_testcase.gif" ) ) );
		mainToolbar.addFixed( optionsButton = UISupport.createToolbarButton( new SoapMonitorOptionsAction() ) );

		toolbar
				.addFixed( createRequestButton = UISupport.createToolbarButton( UISupport.createImageIcon( "/request.gif" ) ) );
		toolbar.addFixed( addToTestCaseButton = UISupport.createToolbarButton( UISupport
				.createImageIcon( "/testCase.gif" ) ) );
		// toolbar.addFixed( addToRestTestCaseButton =
		// UISupport.createToolbarButton( UISupport
		// .createImageIcon( "/testCase.gif" ) ) );
		toolbar.addFixed( addToMockServiceButton = UISupport.createToolbarButton( UISupport
				.createImageIcon( "/mockService.gif" ) ) );
		toolbar
				.addFixed( clearButton = UISupport.createToolbarButton( UISupport.createImageIcon( "/clear_loadtest.gif" ) ) );

		startButton.setToolTipText( "Starts the HTTP Monitor as configured" );
		stopButton.setToolTipText( "Stops the HTTP Monitor" );
		optionsButton.setToolTipText( "Sets Monitor Options" );
		clearButton.setToolTipText( "Clear all/selected messages from the log" );
		createRequestButton.setToolTipText( "Creates requests from selected messages" );
		addToTestCaseButton.setToolTipText( "Adds selected requests to a TestCase" );
		// addToRestTestCaseButton.setToolTipText(
		// "Adds selected REST requests to a TestCase" );
		addToMockServiceButton.setToolTipText( "Adds selected reponses to a MockService" );

		createRequestButton.setEnabled( false );
		addToMockServiceButton.setEnabled( false );
		addToTestCaseButton.setEnabled( false );
		// addToRestTestCaseButton.setEnabled( false );

		startButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				start();
			}
		} );

		stopButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				stop();
			}
		} );

		clearButton.addActionListener( new ClearAction() );
		createRequestButton.addActionListener( new CreateRequestsAction() );
		addToTestCaseButton.addActionListener( new AddToTestCaseAction() );
		// addToRestTestCaseButton.addActionListener( new
		// AddToRESTTestCaseAction() );
		addToMockServiceButton.addActionListener( new AddToMockServiceAction() );

		mainToolbar.addGlue();

		infoLabel = new JLabel();
		infoLabel.setPreferredSize( new Dimension( 150, 20 ) );
		infoLabel.setOpaque( false );
		mainToolbar.addFixed( infoLabel );

		progressBar = new JProgressBar();

		JPanel progressBarPanel = UISupport.createProgressBarPanel( progressBar, 2, false );
		progressBarPanel.setPreferredSize( new Dimension( 60, 20 ) );

		mainToolbar.addFixed( progressBarPanel );
		return toolbar;
	}

	/**
	 * Method start
	 */
	public void start()
	{
		int localPort = getLocalPort();
		listenerCallBack = new SoapMonitorListenerCallBack(){
			@Override
			public void fireAddMessageExchange( WsdlMonitorMessageExchange messageExchange )
			{
				addMessageExchange( messageExchange );
			}
		};
		monitorEngine = new SoapMonitorEngineImpl( sslEndpoint );
		monitorEngine.start( this.getProject(), localPort, listenerCallBack );

		if( monitorEngine.isRunning() )
		{
			stopButton.setEnabled( true );
			startButton.setEnabled( false );
			optionsButton.setEnabled( false );
			infoLabel.setText( ( monitorEngine.isProxy() ? "HTTP Proxy " : "SSL Tunnel " ) + "on port " + localPort );
			progressBar.setIndeterminate( true );

			if( setAsProxy )
			{
				oldProxyHost = SoapUI.getSettings().getString( ProxySettings.HOST, "" );
				oldProxyPort = SoapUI.getSettings().getString( ProxySettings.PORT, "" );
				oldProxyEnabled = SoapUI.getSettings().getBoolean( ProxySettings.ENABLE_PROXY );
				oldProxyAuto = SoapUI.getSettings().getBoolean( ProxySettings.AUTO_PROXY );

				SoapUI.getSettings().setString( ProxySettings.HOST, "127.0.0.1" );
				SoapUI.getSettings().setString( ProxySettings.PORT, String.valueOf( localPort ) );
				SoapUI.getSettings().setBoolean( ProxySettings.ENABLE_PROXY, true );
				SoapUI.getSettings().setBoolean( ProxySettings.AUTO_PROXY, false );
				SoapUI.updateProxyFromSettings();
			}

			SoapUI.log.info( "Started HTTP Monitor on local port " + localPort );
		}
		else
		{
			stopButton.setEnabled( false );
			startButton.setEnabled( true );
			optionsButton.setEnabled( true );
			infoLabel.setText( "Stopped" );
			progressBar.setIndeterminate( false );

			SoapUI.log.info( "Could not start HTTP Monitor on local port " + localPort );
		}
	}

	/**
	 * Method close
	 */
	public void close()
	{
		stop();
	}

	/**
	 * Method stop
	 */
	public void stop()
	{
		monitorEngine.stop();
		if( addedEndpoints != null )
		{
			for( Interface iface : addedEndpoints.keySet() )
				iface.removeEndpoint( addedEndpoints.get( iface ) );

			addedEndpoints.clear();
		}

		stopButton.setEnabled( false );
		startButton.setEnabled( true );
		optionsButton.setEnabled( true );
		progressBar.setIndeterminate( false );
		infoLabel.setText( "Stopped" );

		if( setAsProxy )
		{
			SoapUI.getSettings().setString( ProxySettings.HOST, oldProxyHost );
			SoapUI.getSettings().setString( ProxySettings.PORT, oldProxyPort );
			SoapUI.getSettings().setBoolean( ProxySettings.ENABLE_PROXY, oldProxyEnabled );
			SoapUI.getSettings().setBoolean( ProxySettings.AUTO_PROXY, oldProxyAuto );
			SoapUI.updateProxyFromSettings();
		}
	}

	@AForm( description = "Set options for adding selected requests to a MockService", name = "Add To MockService" )
	private final class AddToMockServiceAction implements ActionListener
	{
		private static final String CREATE_NEW_OPTION = "<Create New>";
		private XFormDialog dialog;

		@AField( name = "Target MockService", description = "The target TestSuite", type = AFieldType.ENUMERATION )
		public final static String MOCKSERVICE = "Target MockService";

		@AField( name = "Open Editor", description = "Open the created MockService", type = AFieldType.BOOLEAN )
		public final static String OPENEDITOR = "Open Editor";

		public void actionPerformed( ActionEvent e )
		{
			int[] rows = logTable.getSelectedRows();
			if( rows.length == 0 )
				return;

			if( dialog == null )
			{
				dialog = ADialogBuilder.buildDialog( this.getClass() );
			}

			String[] testSuiteNames = ModelSupport.getNames( new String[] { CREATE_NEW_OPTION }, getProject()
					.getMockServiceList() );
			dialog.setOptions( MOCKSERVICE, testSuiteNames );

			if( dialog.show() )
			{
				int withoutOperation = 0;
				for( int row : rows )
				{
					WsdlMonitorMessageExchange me = tableModel.getMessageExchangeAt( row );
					if( me.getOperation() == null )
						withoutOperation++ ;
				}
				if( withoutOperation == rows.length )
				{
					UISupport.showInfoMessage( "No SOAP requests selected!" );
					return;
				}
				String targetMockServiceName = dialog.getValue( MOCKSERVICE );

				WsdlMockService mockService = getProject().getMockServiceByName( targetMockServiceName );
				if( mockService == null )
				{
					targetMockServiceName = ModelSupport.promptForUniqueName( "MockService", getProject(), "" );
					if( targetMockServiceName == null )
						return;

					mockService = getProject().addNewMockService( targetMockServiceName );
					mockService.setIncomingWss( incomingResponseWss );
				}

				int cnt = 0;
				for( int row : rows )
				{
					WsdlMonitorMessageExchange me = tableModel.getMessageExchangeAt( row );
					if( me.getOperation() == null )
						continue;

					WsdlMockOperation mockOperation = mockService.getMockOperation( me.getOperation() );
					if( mockOperation == null )
						mockOperation = mockService.addNewMockOperation( me.getOperation() );

					WsdlMockResponse mockResponse = mockOperation
							.addNewMockResponse( "Monitor Response " + ( ++cnt ), false );
					mockResponse.setResponseContent( me.getResponseContent() );

					Attachment[] requestAttachments = me.getResponseAttachments();
					if( requestAttachments != null )
					{
						for( Attachment attachment : requestAttachments )
						{
							mockResponse.addAttachment( attachment );
						}
					}
				}

				if( cnt == 0 )
				{
					UISupport.showInfoMessage( "No response messages found" );
				}
				else
				{
					UISupport.showInfoMessage( "Added " + cnt + " MockResponses to MockService" );

					if( dialog.getBooleanValue( OPENEDITOR ) )
						UISupport.selectAndShow( mockService );
				}
			}
		}
	}

	@AForm( description = "Set options for adding selected requests to a TestCase", name = "Add To TestCase" )
	private final class AddToTestCaseAction implements ActionListener
	{
		private static final String CREATE_NEW_OPTION = "<Create New>";
		private XFormDialog dialog;

		@AField( name = "Target TestSuite", description = "The target TestSuite", type = AFieldType.ENUMERATION )
		public final static String TESTSUITE = "Target TestSuite";

		@AField( name = "Target TestCase", description = "The target TestCase for the requests", type = AFieldType.ENUMERATION )
		public final static String TESTCASE = "Target TestCase";

		@AField( name = "Open Editor", description = "Open the created TestCase", type = AFieldType.BOOLEAN )
		public final static String OPENEDITOR = "Open Editor";

		TestSuite testSuite;

		public void actionPerformed( ActionEvent e )
		{
			int[] rows = logTable.getSelectedRows();
			if( rows.length == 0 )
				return;

			if( dialog == null )
			{
				dialog = ADialogBuilder.buildDialog( this.getClass() );
				dialog.getFormField( TESTSUITE ).addFormFieldListener( new XFormFieldListener()
				{
					public void valueChanged( XFormField sourceField, String newValue, String oldValue )
					{
						if( newValue.equals( CREATE_NEW_OPTION ) )
						{
							dialog.setOptions( TESTCASE, new String[] { CREATE_NEW_OPTION } );
						}
						else
						{
							testSuite = getProject().getTestSuiteByName( newValue );
							dialog.setOptions(
									TESTCASE,
									testSuite == null ? new String[] { CREATE_NEW_OPTION } : ModelSupport.getNames(
											testSuite.getTestCaseList(), new String[] { CREATE_NEW_OPTION } ) );
						}
					}
				} );
			}

			String[] testSuiteNames = ModelSupport.getNames( new String[] { CREATE_NEW_OPTION }, getProject()
					.getTestSuiteList() );
			dialog.setOptions( TESTSUITE, testSuiteNames );
			testSuite = getProject().getTestSuiteByName( dialog.getValue( TESTSUITE ) );
			dialog.setOptions(
					TESTCASE,
					testSuite == null ? new String[] { CREATE_NEW_OPTION } : ModelSupport.getNames(
							testSuite.getTestCaseList(), new String[] { CREATE_NEW_OPTION } ) );

			if( dialog.show() )
			{
				String targetTestSuiteName = dialog.getValue( TESTSUITE );
				String targetTestCaseName = dialog.getValue( TESTCASE );

				WsdlTestSuite testSuite = getProject().getTestSuiteByName( targetTestSuiteName );
				if( testSuite == null )
				{
					targetTestSuiteName = ModelSupport.promptForUniqueName( "TestSuite", getProject(), "" );
					if( targetTestSuiteName == null )
						return;

					testSuite = getProject().addNewTestSuite( targetTestSuiteName );
				}

				WsdlTestCase testCase = testSuite.getTestCaseByName( targetTestCaseName );
				if( testCase == null )
				{
					targetTestCaseName = ModelSupport.promptForUniqueName( "TestCase", testSuite, "" );
					if( targetTestCaseName == null )
						return;

					testCase = testSuite.addNewTestCase( targetTestCaseName );
				}

				for( int row : rows )
				{
					WsdlMonitorMessageExchange me = tableModel.getMessageExchangeAt( row );
					if( me.getOperation() != null )
					{
						WsdlTestRequestStep test = ( WsdlTestRequestStep )testCase.insertTestStep(
								WsdlTestRequestStepFactory.createConfig( me.getOperation(), "Monitor Request " + ( row + 1 ) ),
								-1 );

						WsdlTestRequest request = test.getTestRequest();
						request.setRequestContent( me.getRequestContent() );
						request.setEndpoint( me.getTargetUrl().toString() );
						request.setIncomingWss( incomingRequestWss );

						Attachment[] requestAttachments = me.getRequestAttachments();
						if( requestAttachments != null )
						{
							for( Attachment attachment : requestAttachments )
							{
								request.importAttachment( attachment );
							}
						}
					}
					else
					{
						HttpRequestStepFactory httpRequestStepFactory = new HttpRequestStepFactory();
						HttpTestRequestStep test = ( HttpTestRequestStep )testCase.insertTestStep(
								httpRequestStepFactory.createConfig( me, "Monitor Request " + ( row + 1 ) ), -1 );

						test.getTestRequest().setRequestHeaders( excludeHeaders( me.getRequestHeaders() ) );

						HttpTestRequest request = ( HttpTestRequest )test.getHttpRequest();

						request.setEndpoint( me.getTargetUrl().toString() );
						// request.setIncomingWss( incomingRequestWss );
						String existingMediaType = me.getResponseHeaders().get( "Content-Type", "" );
						if( !StringUtils.isNullOrEmpty( existingMediaType ) )
						{
							request.setMediaType( existingMediaType );
						}
						if( "application/octet-stream".equals( existingMediaType )
								|| "application/x-amf".equals( existingMediaType ) )
						{
							request.attachBinaryData( me.getRequestContent().getBytes(), existingMediaType );
						}
						else
						{
							request.setRequestContent( me.getRequestContent() );
							test.getTestRequest().setRequestContent( me.getRequestContent() );
						}
						Attachment[] requestAttachments = me.getRequestAttachments();
						if( requestAttachments != null )
						{
							for( Attachment attachment : requestAttachments )
							{
								request.importAttachment( attachment );
							}
						}
						// }

					}
				}

				if( dialog.getBooleanValue( OPENEDITOR ) )
					UISupport.selectAndShow( testCase );
			}
		}
	}

	private final class CreateRequestsAction implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			int[] rows = logTable.getSelectedRows();
			if( rows.length == 0 )
				return;

			if( UISupport.confirm( "Create " + rows.length + " requests", "Create Request" ) )
			{
				int withoutOperation = 0;
				for( int row : rows )
				{
					WsdlMonitorMessageExchange me = tableModel.getMessageExchangeAt( row );
					if( me.getOperation() == null )
					{
						withoutOperation++ ;
						continue;
					}

					WsdlRequest request = me.getOperation().addNewRequest( "Monitor Request " + ( row + 1 ) );

					request.setRequestContent( me.getRequestContent() );
					request.setEndpoint( me.getTargetUrl().toString() );

					Attachment[] requestAttachments = me.getRequestAttachments();
					if( requestAttachments != null )
					{
						for( Attachment attachment : requestAttachments )
						{
							request.importAttachment( attachment );
						}
					}
				}
				if( withoutOperation > 0 )
				{
					UISupport.showInfoMessage( "For " + withoutOperation + "request(s) there are no operations",
							"Create Request" );
				}
			}
		}
	}

	private final class ClearAction implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			if( logTable.getRowCount() == 0 )
				return;

			int[] rows = logTable.getSelectedRows();

			if( rows.length == 0 )
			{
				if( UISupport.confirm( "Clear monitor log?", "Clear Log" ) )
					tableModel.clear();
			}
			else if( UISupport.confirm( "Clear " + rows.length + " rows from monitor log?", "Clear Log" ) )
			{
				tableModel.clearRows( rows );
			}
		}
	}

	@SuppressWarnings( "unchecked" )
	public class MonitorLogTableModel extends AbstractTableModel
	{
		private List<WsdlMonitorMessageExchange> exchanges = new TreeList();

		public MonitorLogTableModel()
		{
		}

		public synchronized void clear()
		{
			int sz = exchanges.size();
			while( exchanges.size() > 0 )
			{
				WsdlMonitorMessageExchange removed = exchanges.remove( 0 );
				removed.discard();
			}

			fireTableRowsDeleted( 0, sz );

			while( requestFilterModel.getSize() > 1 )
				requestFilterModel.removeElementAt( 1 );

			while( targetHostFilterModel.getSize() > 1 )
				targetHostFilterModel.removeElementAt( 1 );

			updateRowCountLabel();
		}

		public synchronized void clearRows( int[] indices )
		{
			for( int c = indices.length; c > 0; c-- )
			{
				int index = indices[c - 1];
				WsdlMonitorMessageExchange removed = exchanges.remove( logTable.convertRowIndexToModel( index ) );
				removed.discard();
				fireTableRowsDeleted( index, index );
				updateRowCountLabel();
			}
		}

		public int getColumnCount()
		{
			return 12;
		}

		public WsdlMonitorMessageExchange getMessageExchangeAt( int tableRow )
		{
			return exchanges.get( logTable.convertRowIndexToModel( tableRow ) );
		}

		@Override
		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "Count.";
			case 1 :
				return "Time";
			case 2 :
				return "Request Host";
			case 3 :
				return "Target Host";
			case 4 :
				return "Interface";
			case 5 :
				return "Operation";
			case 6 :
				return "Time Taken";
			case 7 :
				return "Req Sz";
			case 8 :
				return "Resp Sz";
			case 9 :
				return "Method";
			case 10 :
				return "Path";
			case 11 :
				return "Content-Type";
			}

			return null;
		}

		public int getRowCount()
		{
			return exchanges.size();
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			if( rowIndex < 0 || rowIndex >= exchanges.size() )
				return null;

			WsdlMonitorMessageExchange exchange = exchanges.get( rowIndex );
			if( exchange == null )
				return null;

			switch( columnIndex )
			{
			case 0 :
				return rowIndex;
			case 1 :
				return DateUtil.formatFull( new Date( exchange.getTimestamp() ) );
			case 2 :
				return exchange.getRequestHost();
			case 3 :
				return exchange.getTargetUrl().getHost();
			case 4 :
				return exchange.getOperation() == null ? "- unknown -" : exchange.getOperation().getInterface().getName();
			case 5 :
				return exchange.getOperation() == null ? "- unknown -" : exchange.getOperation().getName();
			case 6 :
				return String.valueOf( exchange.getTimeTaken() );
			case 7 :
				return String.valueOf( exchange.getRequestContentLength() );
			case 8 :
				return String.valueOf( exchange.getResponseContentLength() );
			case 9 :
				return String.valueOf( exchange.getRequestMethod() );
			case 10 :
				return exchange.getTargetUrl().getPath();
			case 11 :
				return String.valueOf( exchange.getResponseContentType() );
			}

			return null;
		}

		public synchronized void addMessageExchange( WsdlMonitorMessageExchange exchange )
		{
			exchanges.add( exchange );
			int size = exchanges.size();
			fireTableRowsInserted( size - 1, size );

			fitSizeToMaxRows();

			String requestHost = exchange.getRequestHost();
			if( requestFilterModel.getIndexOf( requestHost ) == -1 )
			{
				requestFilterModel.addElement( requestHost );
			}

			String host = exchange.getTargetUrl().getHost();
			if( targetHostFilterModel.getIndexOf( host ) == -1 )
			{
				targetHostFilterModel.addElement( host );
			}

			updateRowCountLabel();
		}

		public void fitSizeToMaxRows()
		{
			int removeCnt = 0;

			while( exchanges.size() > maxRows )
			{
				WsdlMonitorMessageExchange removed = exchanges.remove( 0 );
				removed.discard();
				removeCnt++ ;
			}

			if( removeCnt > 0 )
			{
				fireTableDataChanged();
				updateRowCountLabel();
			}
		}
	}

	protected String getHttpProxyHost()
	{
		return httpProxyHost;
	}

	/**
	 * excludes proxy headers as well as headers set for excluding in Global
	 * Preferences/WebRecordingSettings.EXCLUDED_HEADERS
	 * 
	 * @param requestHeaders
	 * @return
	 */
	private StringToStringsMap excludeHeaders( StringToStringsMap requestHeaders )
	{
		StringToStringsMap stsmap = new StringToStringsMap();
		for( String key : requestHeaders.getKeys() )
		{
			if( !( key.contains( "Proxy" ) || key.contains( "Content" ) ) && !BrowserComponent.isHeaderExcluded( key ) )
			{
				stsmap.add( key, requestHeaders.get( key, "" ) );
			}
		}
		return stsmap;
	}

	protected void setHttpProxyHost( String proxyHost )
	{
		httpProxyHost = proxyHost;
	}

	protected int getHttpProxyPort()
	{
		return httpProxyPort;
	}

	protected void setHttpProxyPort( int proxyPort )
	{
		httpProxyPort = proxyPort;
	}

	// protected SlowLinkSimulator getSlowLink()
	// {
	// return slowLink;
	// }

	public String getTargetHost()
	{
		String host = targetEndpoint;

		try
		{
			URL url = new URL( host );
			return url.getHost();
		}
		catch( MalformedURLException e )
		{
			return host;
		}
	}

	public String getTargetEndpoint()
	{
		return targetEndpoint;
	}

	public int getTargetPort()
	{
		try
		{
			URL url = new URL( targetEndpoint );
			return url.getPort() == -1 ? 80 : url.getPort();
		}
		catch( MalformedURLException e )
		{
			return 80;
		}
	}

	public int getLocalPort()
	{
		return listenPort;
	}

	public synchronized void addMessageExchange( WsdlMonitorMessageExchange messageExchange )
	{
		messageExchangeStack.push( messageExchange );

		if( !stackProcessor.isRunning() )
			new Thread( stackProcessor, "SoapMonitor StackProcessor for project [" + getProject().getName() + "]" )
					.start();
	}

	private class StackProcessor implements Runnable
	{
		private boolean canceled;
		private boolean running;

		public void run()
		{
			running = true;
			SoapUI.log.info( "Started stackprocessor for soapmonitor in project [" + getProject().getName() + "]" );
			while( !canceled && messageExchangeStack.size() > 0 )
			{
				WsdlMonitorMessageExchange messageExchange = messageExchangeStack.pop();
				if( messageExchange != null )
				{
					processMessage( messageExchange );
				}

				try
				{
					Thread.sleep( 100 );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
			running = false;
		}

		private synchronized void processMessage( WsdlMonitorMessageExchange messageExchange )
		{
			messageExchange.prepare( project.getWssContainer().getIncomingWssByName( incomingRequestWss ), project
					.getWssContainer().getIncomingWssByName( incomingResponseWss ) );

			tableModel.addMessageExchange( messageExchange );

			listenerCallBack.fireOnMessageExchange( messageExchange );
		}

		@SuppressWarnings( "unused" )
		public void cancel()
		{
			canceled = true;
		}

		@SuppressWarnings( "unused" )
		protected boolean isCanceled()
		{
			return canceled;
		}

		protected boolean isRunning()
		{
			return running;
		}
	}

	public MonitorLogTableModel getLogModel()
	{
		return tableModel;
	}

	public void addSoapMonitorListener( MonitorListener listener )
	{
		listenerCallBack.addSoapMonitorListener( listener );
	}

	public void removeSoapMonitorListener( MonitorListener listener )
	{
		listenerCallBack.removeSoapMonitorListener( listener );
	}

	public WsdlProject getProject()
	{
		return project;
	}

	public class SoapMonitorOptionsAction extends AbstractAction
	{

		public SoapMonitorOptionsAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/options.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( optionsDialog == null )
			{
				optionsDialog = ADialogBuilder.buildDialog( OptionsForm.class );
			}

			StringList endpoints = new StringList();
			endpoints.add( null );

			for( WsdlInterface iface : ModelSupport.getChildren( getProject(), WsdlInterface.class ) )
			{
				endpoints.addAll( iface.getEndpoints() );
			}

			optionsDialog.setIntValue( OptionsForm.PORT, listenPort );
			optionsDialog.setIntValue( OptionsForm.MAXROWS, maxRows );

			optionsDialog.setOptions( OptionsForm.REQUEST_WSS,
					StringUtils.merge( project.getWssContainer().getIncomingWssNames(), "<none>" ) );
			optionsDialog.setOptions( OptionsForm.RESPONSE_WSS,
					StringUtils.merge( project.getWssContainer().getIncomingWssNames(), "<none>" ) );

			optionsDialog.setValue( OptionsForm.REQUEST_WSS, incomingRequestWss );
			optionsDialog.setValue( OptionsForm.RESPONSE_WSS, incomingResponseWss );
			optionsDialog
					.setValue(
							LaunchForm.SET_CONTENT_TYPES,
							project.getSettings().getString( LaunchForm.SET_CONTENT_TYPES,
									SoapMonitorAction.defaultContentTypes() ) );

			if( optionsDialog.show() )
			{
				Settings settings = getProject().getSettings();

				settings.setLong( OptionsForm.PORT, listenPort = optionsDialog.getIntValue( OptionsForm.PORT, listenPort ) );
				settings.setLong( OptionsForm.MAXROWS, maxRows = optionsDialog.getIntValue( OptionsForm.MAXROWS, maxRows ) );
				settings.setString( LaunchForm.SET_CONTENT_TYPES, optionsDialog.getValue( LaunchForm.SET_CONTENT_TYPES ) );

				incomingRequestWss = optionsDialog.getValue( OptionsForm.REQUEST_WSS );
				incomingResponseWss = optionsDialog.getValue( OptionsForm.RESPONSE_WSS );

				tableModel.fitSizeToMaxRows();
			}
		}

		@AForm( name = "HTTP Monitor Options", description = "Set options for HTTP Monitor", helpUrl = HelpUrls.SOAPMONITOR_HELP_URL, icon = UISupport.OPTIONS_ICON_PATH )
		private class OptionsForm
		{
			@AField( description = "The local port to listen on", name = "Port", type = AFieldType.INT )
			public final static String PORT = "Port";

			@AField( description = "The maximum number of exchanges to log", name = "Max Log", type = AFieldType.INT )
			public final static String MAXROWS = "Max Log";

			@AField( description = "The Incoming WSS configuration to use for processing requests", name = "Incoming Request WSS", type = AFieldType.ENUMERATION )
			public final static String REQUEST_WSS = "Incoming Request WSS";

			@AField( description = "The Outgoing WSS configuration to use for processing responses", name = "Incoming Response WSS", type = AFieldType.ENUMERATION )
			public final static String RESPONSE_WSS = "Incoming Response WSS";

			@AField( description = "Content types to monitor", name = "Content types to monitor", type = AFieldType.STRINGAREA )
			public final static String SET_CONTENT_TYPES = "Content types to monitor";
		}
	}

	public void release()
	{
		requestViewer.release();
		responseViewer.release();

		if( optionsDialog != null )
		{
			optionsDialog.release();
			optionsDialog = null;
		}

		inspectorPanel.release();
	}

	public boolean isRunning()
	{
		return monitorEngine.isRunning();
	}



	public MessageExchangeModelItem getRequestModelItem()
	{
		return requestModelItem;
	}
}
