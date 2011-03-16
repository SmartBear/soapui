package com.eviware.soapui.security.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.components.ModelItemListDesktopPanel;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

public class CloneSecurityCheckParameterAction extends AbstractSoapUIAction<TestStep>
{
	public static final String SOAPUI_ACTION_ID = "CloneSecurityCheckParameterAction";
	private XFormDialog dialog;
	private TestCase testCase;
	private Project project;

	public CloneSecurityCheckParameterAction()
	{
		super( "Clone SecurityCheck Parameters", "Clones an arbitrary number of security check paramaters" );
	}

	public void perform( final TestStep testStep, Object param )
	{
		testCase = testStep.getTestCase();
		project = testCase.getTestSuite().getProject();

		final XFormDialog dialog = ADialogBuilder.buildDialog( CloneParameterDialog.class );

		dialog.getFormField( CloneParameterDialog.TESTSUITE ).addFormFieldListener( new XFormFieldListener()
		{
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				TestSuite testSuite = project.getTestSuiteByName( newValue );
				String[] testCaseNames = ModelSupport.getNames( testSuite.getTestCaseList() );
				dialog.setOptions( CloneParameterDialog.TESTCASE, testCaseNames );

				if( testCaseNames.length > 0 )
				{
					dialog.setValue( CloneParameterDialog.TESTCASE, testCaseNames[0] );
					TestCase testCase = testSuite.getTestCaseByName( testCaseNames[0] );

					String[] testStepNames = ModelSupport.getNames( testCase.getTestStepList() );
					dialog.setOptions( CloneParameterDialog.TESTSTEP, testStepNames );

					String[] securityTestNames = ModelSupport.getNames( testCase.getSecurityTestList() );
					dialog.setOptions( CloneParameterDialog.SECURITYTESTS, securityTestNames );

					if( testStepNames.length > 0 )
					{
						dialog.setValue( CloneParameterDialog.TESTSTEP, testStepNames[0] );
					}
					else
					{
						dialog.setOptions( CloneParameterDialog.TESTSTEP, new String[0] );
					}

					if( securityTestNames.length > 0 )
					{
						dialog.setValue( CloneParameterDialog.SECURITYTESTS, securityTestNames[0] );
					}
					else
					{
						dialog.setOptions( CloneParameterDialog.SECURITYTESTS, new String[0] );
					}
				}
				else
				{
					dialog.setOptions( CloneParameterDialog.SECURITYTESTS, new String[0] );
					dialog.setOptions( CloneParameterDialog.TESTSTEP, new String[0] );
				}
			}
		} );

		dialog.getFormField( CloneParameterDialog.TESTCASE ).addFormFieldListener( new XFormFieldListener()
		{
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				String testSuiteName = dialog.getValue( CloneParameterDialog.TESTSUITE );
				TestSuite testSuite = project.getTestSuiteByName( testSuiteName );
				TestCase testCase = testSuite.getTestCaseByName( newValue );
				String[] testStepNames = ModelSupport.getNames( testCase.getTestStepList() );
				dialog.setOptions( CloneParameterDialog.TESTSTEP, testStepNames );

				String[] securityTestNames = ModelSupport.getNames( testCase.getSecurityTestList() );
				dialog.setOptions( CloneParameterDialog.SECURITYTESTS, securityTestNames );

				if( testStepNames.length > 0 )
				{
					dialog.setValue( CloneParameterDialog.TESTSTEP, testStepNames[0] );
				}
				else
				{
					dialog.setOptions( CloneParameterDialog.TESTSTEP, new String[0] );
				}

				if( securityTestNames.length > 0 )
				{
					dialog.setValue( CloneParameterDialog.SECURITYTESTS, securityTestNames[0] );
				}
				else
				{
					dialog.setOptions( CloneParameterDialog.SECURITYTESTS, new String[0] );
				}
			}
		} );

		dialog.getFormField( CloneParameterDialog.TESTSTEP ).addFormFieldListener( new XFormFieldListener()
		{
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				String testSuiteName = dialog.getValue( CloneParameterDialog.TESTSUITE );
				TestSuite testSuite = project.getTestSuiteByName( testSuiteName );
				String testCaseName = dialog.getValue( CloneParameterDialog.TESTCASE );
				TestCase testCase = testSuite.getTestCaseByName( testCaseName );
				String securityTestName = dialog.getValue( CloneParameterDialog.SECURITYTESTS );
				SecurityTest securityTest = testCase.getSecurityTestByName( securityTestName );
				TestStep testStep = testCase.getTestStepByName( newValue );

				String[] securityCheckNames = ModelSupport.getNames( securityTest.getTestStepSecurityChecks( testStep
						.getId() ) );
				dialog.setOptions( CloneParameterDialog.SECURITYCHECKS, securityCheckNames );
			}
		} );

		dialog.getFormField( CloneParameterDialog.SECURITYTESTS ).addFormFieldListener( new XFormFieldListener()
		{
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				String testSuiteName = dialog.getValue( CloneParameterDialog.TESTSUITE );
				TestSuite testSuite = project.getTestSuiteByName( testSuiteName );
				String testCaseName = dialog.getValue( CloneParameterDialog.TESTCASE );
				TestCase testCase = testSuite.getTestCaseByName( testCaseName );
				SecurityTest securityTest = testCase.getSecurityTestByName( newValue );
				String testStepName = dialog.getValue( CloneParameterDialog.TESTSTEP );
				TestStep testStep = testCase.getTestStepByName( testStepName );

				String[] securityCheckNames = ModelSupport.getNames( securityTest.getTestStepSecurityChecks( testStep
						.getId() ) );
				dialog.setOptions( CloneParameterDialog.SECURITYCHECKS, securityCheckNames );
			}
		} );

		dialog.addAction( new ApplyAction() );

		WsdlTestCase testCase = ( WsdlTestCase )testStep.getTestCase();
		WorkspaceImpl workspace = testCase.getTestSuite().getProject().getWorkspace();

		dialog.setOptions( CloneParameterDialog.TESTSUITE,
				ModelSupport.getNames( testCase.getTestSuite().getProject().getTestSuiteList() ) );
		dialog.setValue( CloneParameterDialog.TESTSUITE, testCase.getTestSuite().getName() );

		List<TestCase> testCaseList = testCase.getTestSuite().getTestCaseList();
		dialog.setOptions( CloneParameterDialog.TESTCASE, ModelSupport.getNames( testCaseList ) );
		dialog.setValue( CloneParameterDialog.TESTCASE, testCase.getName() );

		if( dialog.show() )
		{
			List<ModelItem> items = performClone();

			if( items.size() > 0 )
			{
				UISupport.showDesktopPanel( new ModelItemListDesktopPanel( "Updated TestRequests",
						"The following TestRequests where updated with new assertions", items.toArray( new ModelItem[items
								.size()] ) ) );
			}
			else
			{
				UISupport.showInfoMessage( "Updated " + items.size() + " steps" );
			}
		}
	}

	public List<ModelItem> performClone()
	{
		// TODO fill placeholder
		return new ArrayList<ModelItem>();
	}

	@AForm( description = "Specify target TestSuite/TestCase/Security Test(s)/Security Check(s) and select Parameters to clone", name = "Clone Parameters", icon = UISupport.TOOL_ICON_PATH )
	interface CloneParameterDialog
	{
		@AField( name = "Parameters", description = "The Parameters to clone", type = AFieldType.MULTILIST )
		public final static String PARAMETERS = "Parameters";

		@AField( name = "SecurityChecks", description = "The SecurityChecks to clone to", type = AFieldType.MULTILIST )
		public final static String SECURITYCHECKS = "Parameters";

		@AField( name = "Target TestSuite", description = "The target TestSuite for the cloned Parameter(s)", type = AFieldType.ENUMERATION )
		public final static String TESTSUITE = "Target TestSuite";

		@AField( name = "Target TestCase", description = "The target TestCase for the cloned Parameter(s)", type = AFieldType.ENUMERATION )
		public final static String TESTCASE = "Target TestCase";

		@AField( name = "Target TestStep", description = "The target TestStep for the cloned Parameter(s)", type = AFieldType.ENUMERATION )
		public final static String TESTSTEP = "Target TestStep";

		@AField( name = "Target SecurityTest", description = "The target SecurityTest for the cloned Parameter(s)", type = AFieldType.ENUMERATION )
		public final static String SECURITYTESTS = "Target SecurityTest";

		@AField( name = "Overwrite", description = "Overwrite existing parameters", type = AFieldType.BOOLEAN )
		public final static String OVERWRITE = "Overwrite";
	}

	private class ApplyAction extends AbstractAction
	{
		private ApplyAction()
		{
			super( "Apply" );

			putValue( SHORT_DESCRIPTION, "Applies current configuration" );
		}

		public void actionPerformed( ActionEvent e )
		{
			List<ModelItem> items = performClone();
			UISupport.showInfoMessage( "Updated  " + items.size() + " items" );
		}
	}

}
