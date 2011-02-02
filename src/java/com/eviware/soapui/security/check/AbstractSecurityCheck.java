/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.check;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.XPathReference;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.support.XPathReferenceImpl;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityCheckResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.security.ui.SecurityCheckExecutionStrategyPanel;
import com.eviware.soapui.security.ui.SecurityParamsTable;
import com.eviware.soapui.support.SecurityCheckUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.jgoodies.forms.builder.ButtonBarBuilder;

public abstract class AbstractSecurityCheck extends AbstractWsdlModelItem<SecurityCheckConfig> implements
		XPathReferenceContainer
{
	public static final String SINGLE_REQUEST_STRATEGY = "A single request with all the parameters";
	public static final String SEPARATE_REQUEST_STRATEGY =  "Seperate request for each parameter";
	
	// configuration of specific request modification
	private static final int MINIMUM_STRING_DISTANCE = 50;
	protected SecurityCheckConfig config;
	protected String startupScript;
	protected String tearDownScript;
	protected SoapUIScriptEngine scriptEngine;
	private boolean disabled = false;
	protected JPanel panel;
	protected JDialog dialog;
	private boolean configureResult;
	protected Status status;
	SecurityCheckConfigPanel contentPanel;
	protected SecurityCheckResult securityCheckResult;
	protected SecurityCheckRequestResult securityCheckReqResult;
	private TestStep testStep;
	private SecurityParamsTable paramTable;

	private JTabbedPane tabs;

	// TODO check if should exist and what to do with securable
	public AbstractSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
		this.testStep = testStep;
		this.config = config;
		this.startupScript = config.getSetupScript() != null ? config.getSetupScript().getStringValue() : "";
		this.tearDownScript = config.getTearDownScript() != null ? config.getTearDownScript().getStringValue() : "";
		scriptEngine = SoapUIScriptEngineRegistry.create( this );
		if( config.getExecutionStrategy() == null )
			config.setExecutionStrategy( SEPARATE_REQUEST_STRATEGY );
		if( config.getRestParameters() == null )
			config.setRestParameters( RestParametersConfig.Factory.newInstance() );

	}

	abstract protected SecurityCheckRequestResult execute( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityChekResult );

	/*************************************
	 * START OF NEWLY REFACTORED
	 **************************************/
	/**
	 * Runs the test (internaly calls analyze)
	 * 
	 * @param testStep
	 *           The TestStep that the check will be applied to
	 * @param context
	 *           The context to run the test in
	 * @param securityTestLog
	 *           The security log to write to
	 */
	public SecurityCheckResult runNew( TestStep testStep, SecurityTestRunContext context )
	{
		securityCheckResult = new SecurityCheckResult( this );

		// setStatus( Status.INITIALIZED );
		runStartupScript( testStep );

		while( hasNext() )
		{
			securityCheckReqResult = new SecurityCheckRequestResult( this );
			executeNew( testStep, context );
			analyzeNew( testStep, context );
			// add to summary result
			securityCheckResult.addSecurityRequestResult( securityCheckReqResult );
		}
		// TODO refactor sensitiveInfoCheck to write to result directly and
		// uncomment the call
		// sensitiveInfoCheck( testStep, context, securityTestLog );

		runTearDownScript( testStep );

		return securityCheckResult;
	}

	/*
	 * should be implemented in every particular check it executes one request,
	 * modified by securityCheck if necessary and internally adds messages for
	 * logging to SecurityCheckRequestResult TODO needs to be abstract and
	 * implemented in every check
	 */
	protected void executeNew( TestStep testStep, SecurityTestRunContext context )
	{
	}

	/*
	 * should be implemented in every particular check it analyzes one executed
	 * request, modified by securityCheck and internally adds messages for
	 * logging to SecurityCheckRequestResult TODO needs to be abstract and
	 * implemented in every check
	 */
	protected void analyzeNew( TestStep testStep, SecurityTestRunContext context )

	{

	}

	/*
	 * checks if specific SecurityCheck still has modifications left TODO needs
	 * to be abstract and implemented in every check
	 */
	protected boolean hasNext()
	{
		return false;
	}

	// TODO to be extracted to specific securityCheck config for those that need
	// it
	// public void setParameters( RestParametersConfig parameters )
	// {
	// config.setRestParameters( parameters );
	// }

	public RestParamsPropertyHolder getParameters()
	{
		RestParamsPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder( this, config
				.getRestParameters() );
		return params;
	}

	/*************************************
	 * END OF NEWLY REFACTORED
	 **************************************/

	/**
	 * Analyses the specified TestStep
	 * 
	 * @param testStep
	 * @param context
	 * @param securityTestLog
	 * @param securityCheckResult
	 *           TODO
	 * @return TODO
	 */
	abstract public SecurityCheckRequestResult analyze( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityCheckResult );

	// protected Status getStatus()
	// {
	// return status;
	// }

	private void sensitiveInfoCheck( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog )
	{
		if( this instanceof SensitiveInformationCheckable )
		{
			( ( SensitiveInformationCheckable )this ).checkForSensitiveInformationExposure( testStep, context,
					securityTestLog );
		}
	}

	public boolean configure()
	{
//		if( dialog == null )
//		{
			buildDialog();
//		}
//
		UISupport.showDialog( dialog );
		return configureResult;
	}

	public boolean isConfigurable()
	{
		return true;
	}

	/**
	 * Gets desktop configuration for specific SecurityCheck
	 * 
	 * @param TestStep
	 *           the TestStep to create the config for, could be null for
	 *           HttpMonitor checks
	 * 
	 * @return
	 */
	public abstract SecurityCheckConfigPanel getComponent();

	/**
	 * The type of this check
	 * 
	 * @return
	 */
	public abstract String getType();

	/**
	 * Checks if this securityCheck is applicable to the specified TestStep
	 * 
	 * @param testStep
	 * @return
	 */
	public abstract boolean acceptsTestStep( TestStep testStep );

	/**
	 * Builds the configuration dialog
	 */
	protected void buildDialog()
	{
		dialog = new JDialog( UISupport.getMainFrame(), getTitle(), true );
		JPanel fullPanel = new JPanel( new BorderLayout() );
		contentPanel = getComponent();

		ButtonBarBuilder builder = new ButtonBarBuilder();

		ShowOnlineHelpAction showOnlineHelpAction = new ShowOnlineHelpAction( HelpUrls.XPATHASSERTIONEDITOR_HELP_URL );
		builder.addFixed( UISupport.createToolbarButton( showOnlineHelpAction ) );
		builder.addGlue();

		JButton okButton = new JButton( new OkAction() );
		builder.addFixed( okButton );
		builder.addRelatedGap();
		builder.addFixed( new JButton( new CancelAction() ) );

		builder.setBorder( BorderFactory.createEmptyBorder( 1, 5, 5, 5 ) );

		RestParamsPropertyHolder requestParams;

		if( getTestStep() instanceof WsdlTestRequestStep )
		{
			requestParams = SecurityCheckUtil.getSoapRequestParams( getRequest( getTestStep() ) );
		}
		else
		{
			requestParams = getRequest( getTestStep() ).getParams();
		}

		paramTable = new SecurityParamsTable( getParameters(), requestParams );
		paramTable.setPreferredSize( new Dimension( 300, 200 ) );

		JInspectorPanel parameter = JInspectorPanelFactory.build( paramTable, SwingConstants.BOTTOM );
		
		tabs = new JTabbedPane();
		
		tabs.addTab("Execution Strategy", new SecurityCheckExecutionStrategyPanel(getExecutionStrategy()));

		if( contentPanel != null )
		{
			fullPanel.setPreferredSize( new Dimension( 300, 400 ) );
			contentPanel.setPreferredSize( new Dimension( 300, 200 ) );
			contentPanel.add( builder.getPanel(), BorderLayout.SOUTH );
			JSplitPane topPane = UISupport.createVerticalSplit( new JScrollPane( parameter.getComponent() ),
					new JScrollPane( contentPanel ));
			JSplitPane splitPane = UISupport.createVerticalSplit( topPane,
					tabs );

			dialog.setContentPane( splitPane );
		}
		else
		{
			//fullPanel.setPreferredSize( new Dimension( 300, 400 ) );
			//fullPanel.add( builder.getPanel(), BorderLayout.SOUTH );
			paramTable.add( builder.getPanel(), BorderLayout.SOUTH );
			JSplitPane splitPane = UISupport.createVerticalSplit( paramTable,
					new JScrollPane( tabs ));
			//fullPanel.add( paramTable, BorderLayout.NORTH );
			dialog.setContentPane( splitPane );
		}

		dialog.setModal( true );
		dialog.pack();
		UISupport.initDialogActions( dialog, showOnlineHelpAction, okButton );

	}

	public TestStep getTestStep()
	{
		return testStep;
	}

	public void setTestStep( TestStep step )
	{
		testStep = step;
	}

	public class OkAction extends AbstractAction
	{
		public OkAction()
		{
			// TODO save the config
			super( "Save" );
			configureResult = true;
		}

		public void actionPerformed( ActionEvent arg0 )
		{
			if( contentPanel != null )
				contentPanel.save();

			dialog.setVisible( false );
		}

	}

	public class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super( "Cancel" );
			configureResult = false;
		}

		public void actionPerformed( ActionEvent arg0 )
		{
			dialog.setVisible( false );
		}
	}

	private void runTearDownScript( TestStep testStep )
	{
		scriptEngine.setScript( tearDownScript );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			scriptEngine.clearVariables();
		}

	}

	private void runStartupScript( TestStep testStep )
	{
		scriptEngine.setScript( startupScript );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		// scriptEngine.setVariable( "context", context );

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			scriptEngine.clearVariables();
		}
	}

	@Override
	public SecurityCheckConfig getConfig()
	{
		return config;
	}

//	@Override
//	public List<? extends ModelItem> getChildren()
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public String getDescription()
	{
		return config.getDescription();
	}

	@Override
	public ImageIcon getIcon()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId()
	{
		return config.getId();
	}

	@Override
	public String getName()
	{
		return config.getName();
	}

	@Override
	public void setName( String arg0 )
	{
		config.setName( arg0 );
	}

	/**
	 * Checks if the test is disabled
	 * 
	 * @return true if disabled
	 */
	public boolean isDisabled()
	{
		return disabled;
	}

	/**
	 * Disables or Enables the check
	 * 
	 * @param disabled
	 */
	public void setDisabled( boolean disabled )
	{
		this.disabled = disabled;

	}

	/**
	 * The title of this check
	 * 
	 * @return
	 */
	public String getTitle()
	{
		return "";
	}

	public static boolean isSecurable( TestStep testStep )
	{
		if( testStep != null && testStep instanceof Securable )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// protected void setStatus( Status s )
	// {
	// status = s;
	// }

	public String getExecutionStrategy()
	{
		return config.getExecutionStrategy();
	}

	public void setExecutionStrategy( String strategy )
	{
		config.setExecutionStrategy( strategy );
	}

	protected AbstractHttpRequest<?> getOriginalResult( WsdlTestCaseRunner testCaseRunner, TestStep testStep )
	{
		testStep.run( testCaseRunner, testCaseRunner.getRunContext() );

		return getRequest( testStep );
	}

	protected AbstractHttpRequest<?> getRequest( TestStep testStep )
	{
		if( testStep instanceof HttpTestRequestStep )
		{
			return ( ( HttpTestRequestStep )testStep ).getHttpRequest();
		}
		else if( testStep instanceof RestTestRequestStep )
		{
			return ( ( RestTestRequestStep )testStep ).getHttpRequest();
		}
		else if( testStep instanceof WsdlTestRequestStep )
		{
			return ( ( WsdlTestRequestStep )testStep ).getHttpRequest();
		}
		return null;
	}

	protected void runCheck( TestStep testStep, SecurityTestRunContext context, SecurityTestLogModel securityTestLog,
			WsdlTestCaseRunner testCaseRunner, String originalResponse, String message )
	{

		testStep.run( testCaseRunner, testCaseRunner.getRunContext() );
		AbstractHttpRequest<?> lastRequest = getRequest( testStep );

		if( lastRequest.getResponse().getStatusCode() == 200 )
		{
			if( StringUtils.getLevenshteinDistance( originalResponse, lastRequest.getResponse().getContentAsString() ) > MINIMUM_STRING_DISTANCE )
			{
				/*

				securityTestLog.addEntry( new SecurityTestLogMessageEntry( message, null
				 * new HttpResponseMessageExchange( lastRequest)
				 ) );*/
				// TODO implement this through SecurityCheckResult
				// setStatus( Status.FAILED );
			}
		}
		else
		{
			// TODO implement this through SecurityCheckResult
			// setStatus( Status.FAILED );
		}
		analyze( testStep, context, securityTestLog, null );
	}

	public XPathReference[] getXPathReferences()
	{
		List<XPathReference> result = new ArrayList<XPathReference>();

		for( TestProperty param : getParameters().getPropertyList() )
		{
			RestParamProperty restParam = (RestParamProperty)param;
			if( restParam != null )
				result.add( new XPathReferenceImpl( "SecurityCheck Parameter " + restParam.getName(), restParam, restParam, "path" ) );

		}

		return result.toArray( new XPathReference[result.size()] );
	}

}
