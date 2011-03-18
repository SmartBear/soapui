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
package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.security.SecurityParametersTableModel;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.AbstractSecurityCheckWithProperties;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormMultiSelectList;
import com.eviware.x.impl.swing.JComboBoxFormField;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JTextFieldFormField;
import com.eviware.x.impl.swing.SwingXFormDialog;

public class SecurityCheckedParametersTablePanel extends JPanel
{

	static final String CHOOSE_TEST_PROPERTY = "Choose Test Property";
	private SecurityParametersTableModel model;
	private JXToolBar toolbar;
	private JXTable table;
	protected Map<String, TestProperty> properties;
	protected DefaultActionList actionList;
	private JUndoableTextArea pathPane;
	private XFormDialog dialog;
	private AbstractSecurityCheckWithProperties securityCheck;

	public SecurityCheckedParametersTablePanel( SecurityParametersTableModel model,
			Map<String, TestProperty> properties, AbstractSecurityCheckWithProperties securityCheck )
	{
		this.securityCheck = securityCheck;
		this.model = model;
		this.properties = properties;
		init();
	}

	private void init()
	{

		setLayout( new BorderLayout() );
		toolbar = UISupport.createToolbar();

		toolbar.add( UISupport.createToolbarButton( new AddNewParameterAction() ) );
		toolbar.add( UISupport.createToolbarButton( new RemoveParameterAction() ) );
		toolbar.add( UISupport.createToolbarButton( new CopyParameterAction() ) );
		toolbar.add( UISupport.createToolbarButton( new CloneParameterAction() ) );
		toolbar.addGlue();

		add( toolbar, BorderLayout.NORTH );
		table = new JXTable( model );
		add( new JScrollPane( table ), BorderLayout.CENTER );

		pathPane = new JUndoableTextArea();

	}

	public XFormDialog getDialog()
	{
		return dialog;
	}

	/*
	 * Creates dialog
	 */
	protected XFormDialog createAddParameterDialog()
	{
		actionList = new DefaultActionList();
		AddAction addAction = new AddAction();
		actionList.addAction( addAction, true );
		AddAndCopy addAndCopy = new AddAndCopy();
		actionList.addAction( addAndCopy );
		Close closeAction = new Close();
		actionList.addAction( closeAction );

		dialog = ADialogBuilder.buildDialog( AddParameterDialog.class, actionList, false );

		dialog.getFormField( AddParameterDialog.PATH ).setProperty( "component", buildPathSelector() );

		closeAction.setDialog( dialog );
		addAction.setDialog( dialog );
		addAndCopy.setDialog( dialog );

		final JTextFieldFormField labelField = ( JTextFieldFormField )dialog.getFormField( AddParameterDialog.LABEL );
		labelField.getComponent().setColumns( 30 );
		labelField.setEnabled( false );
		JComboBoxFormField nameField = ( JComboBoxFormField )dialog.getFormField( AddParameterDialog.NAME );
		enablePathField( false );
		nameField.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				if( !newValue.equals( CHOOSE_TEST_PROPERTY ) )
				{
					labelField.setEnabled( true );
					enablePathField( true );
				}
				else
				{
					labelField.setEnabled( false );
					enablePathField( false );
				}

			}
		} );
		ArrayList<String> options = new ArrayList<String>( properties.keySet() );
		options.set( 0, CHOOSE_TEST_PROPERTY );
		nameField.setOptions( options.toArray( new String[0] ) );

		( ( JFormDialog )dialog ).getDialog().setResizable( false );

		return dialog;
	}

	protected JPanel buildPathSelector()
	{
		JPanel sourcePanel = new JPanel( new BorderLayout() );
		sourcePanel.add( new JScrollPane( pathPane ), BorderLayout.CENTER );
		sourcePanel.setBorder( BorderFactory.createEmptyBorder( 0, 3, 3, 3 ) );
		return sourcePanel;
	}

	/**
	 * @param pathField
	 */
	protected void enablePathField( boolean enable )
	{
		pathPane.setEnabled( enable );
	}

	private class AddNewParameterAction extends AbstractAction
	{

		public AddNewParameterAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Adds a parameter to security check" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			XFormDialog dialog = createAddParameterDialog();
			dialog.show();
			model.fireTableDataChanged();
		}

	}

	private class RemoveParameterAction extends AbstractAction
	{

		public RemoveParameterAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Removes parameter from security check" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			model.removeRows( table.getSelectedRows() );
			model.fireTableDataChanged();
		}

	}

	public class AddAndCopy extends AbstractAction
	{

		private XFormDialog dialog;

		public AddAndCopy()
		{
			super( "Add&Copy" );
		}

		public void setDialog( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( dialog.getValue( AddParameterDialog.LABEL ) == null
					|| dialog.getValue( AddParameterDialog.LABEL ).trim().length() == 0 )
			{
				UISupport.showErrorMessage( "Label is required!" );
			}
			else
			{
				if( !model.addParameter( dialog.getValue( AddParameterDialog.LABEL ),
						dialog.getValue( AddParameterDialog.NAME ), pathPane.getText() ) )
					UISupport.showErrorMessage( "Label have to be unique!" );
			}
		}

	}

	private class Close extends AbstractAction
	{

		private XFormDialog dialog;

		public Close()
		{
			super( "Close" );
		}

		public void setDialog( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( dialog != null )
			{
				( ( SwingXFormDialog )dialog ).setReturnValue( XFormDialog.CANCEL_OPTION );

				JComboBoxFormField nameField = ( JComboBoxFormField )dialog.getFormField( AddParameterDialog.NAME );
				nameField.setSelectedOptions( new Object[] { nameField.getOptions()[0] } );
				dialog.setValue( AddParameterDialog.LABEL, "" );
				pathPane.setText( "" );

				dialog.setVisible( false );
			}

		}

	}

	private class CopyParameterAction extends AbstractAction
	{

		public CopyParameterAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/clone_request.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Copies parameter" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( table.getSelectedRow() > -1 )
			{
				XFormDialog dialog = createAddParameterDialog();

				int row = table.getSelectedRow();
				initDialogForCopy( dialog, row );

				dialog.show();
				model.fireTableDataChanged();
			}
		}

	}

	private void initDialogForCopy( XFormDialog dialog, int row )
	{
		dialog.setValue( AddParameterDialog.LABEL, ( String )model.getValueAt( row, 0 ) );
		dialog.setValue( AddParameterDialog.NAME, ( String )model.getValueAt( row, 1 ) );
		pathPane.setText( ( String )model.getValueAt( row, 2 ) );
	}

	private class CloneParameterAction extends AbstractAction
	{

		public CloneParameterAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/clone_request.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Clones parameter" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			XFormDialog dialog = createCloneParameterDialog();
			dialog.show();
			model.fireTableDataChanged();
		}

	}

	private String[] getSecurableTestStepsNames( TestCase testCase )
	{
		List<TestStep> testStepList = testCase.getTestStepList();
		List<String> namesList = new ArrayList<String>();
		for( TestStep testStep : testStepList )
		{
			if( AbstractSecurityCheck.isSecurable( testStep ) )
			{
				namesList.add( testStep.getName() );
			}
		}
		String[] names = new String[namesList.size()];
		for( int c = 0; c < namesList.size(); c++ )
		{
			names[c] = namesList.get( c );
		}
		return names;
	}

	public List<ModelItem> performClone( boolean showErrorMessage )
	{
		List<ModelItem> items = new ArrayList<ModelItem>();
		String targetTestSuiteName = dialog.getValue( CloneParameterDialog.TARGET_TESTSUITE );
		String targetTestCaseName = dialog.getValue( CloneParameterDialog.TARGET_TESTCASE );
		String targetSecurityTestName = dialog.getValue( CloneParameterDialog.TARGET_SECURITYTEST );
		String targetSecurityTestStepName = dialog.getValue( CloneParameterDialog.TARGET_TESTSTEP );
		String[] targetSecurityChecks = StringUtils.toStringArray( ( ( XFormMultiSelectList )dialog
				.getFormField( CloneParameterDialog.TARGET_SECURITYCHECK ) ).getSelectedOptions() );

		if( targetSecurityChecks.length == 0 )
		{
			if( showErrorMessage )
			{
				UISupport.showErrorMessage( "No SecurityChecks selected.." );
			}
			return items;
		}

		int[] indexes = ( ( XFormOptionsField )dialog.getFormField( CloneParameterDialog.PARAMETERS ) )
				.getSelectedIndexes();
		if( indexes.length == 0 )
		{
			if( showErrorMessage )
			{
				UISupport.showErrorMessage( "No Parameters selected.." );
			}
			return items;
		}

		Project project = securityCheck.getTestStep().getTestCase().getTestSuite().getProject();
		TestSuite targetTestSuite = project.getTestSuiteByName( targetTestSuiteName );
		TestCase targetTestCase = targetTestSuite.getTestCaseByName( targetTestCaseName );
		SecurityTest targetSecurityTest = targetTestCase.getSecurityTestByName( targetSecurityTestName );
		TestStep targetTestStep = targetTestCase.getTestStepByName( targetSecurityTestStepName );

		boolean overwrite = dialog.getBooleanValue( CloneParameterDialog.OVERWRITE );

		for( String checkName : targetSecurityChecks )
		{
			AbstractSecurityCheckWithProperties targetSecurityCheck = ( AbstractSecurityCheckWithProperties )targetSecurityTest
					.getTestStepSecurityCheckByName( targetTestStep.getId(), checkName );

			for( int i : indexes )
			{
				SecurityCheckedParameter checkParameter = securityCheck.getParameterAt( i );
				String newParameterLabel = checkParameter.getLabel();
				if( securityCheck.getParameterByLabel( checkParameter.getLabel() ) != null )
				{
					if( securityCheck.equals( targetSecurityCheck ) )
					{
						newParameterLabel = "Copy of " + checkParameter.getLabel();
					}
				}
				if( targetSecurityCheck.importParameter( checkParameter, overwrite, newParameterLabel )
						&& !items.contains( targetSecurityCheck ) )
				{
					items.add( targetSecurityCheck );
				}
			}
		}
		return items;
	}

	private class OkAction extends AbstractAction
	{

		private XFormDialog dialog;

		public OkAction()
		{
			super( "OK" );
		}

		public void setDialog( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			if( dialog != null )
			{
				( ( SwingXFormDialog )dialog ).setReturnValue( XFormDialog.OK_OPTION );

				List<ModelItem> items = performClone( false );

				if( items.size() > 0 )
				{
					UISupport.showInfoMessage( "Updated " + items.size() + " checks" );
				}
				dialog.setVisible( false );
			}
		}
	}

	private class CancelAction extends AbstractAction
	{

		private XFormDialog dialog;

		public CancelAction()
		{
			super( "Cancel" );
		}

		public void setDialog( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			if( dialog != null )
			{
				( ( SwingXFormDialog )dialog ).setReturnValue( XFormDialog.CANCEL_OPTION );
				dialog.setVisible( false );
			}
		}
	}

	private class ApplyAction extends AbstractAction
	{

		private XFormDialog dialog;

		public ApplyAction()
		{
			super( "Apply" );
		}

		public void setDialog( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			if( dialog != null )
			{
				List<ModelItem> items = performClone( true );
				if( items.size() > 0 )
				{
					UISupport.showInfoMessage( "Updated " + items.size() + " checks" );
				}

				( ( XFormMultiSelectList )dialog.getFormField( CloneParameterDialog.TARGET_SECURITYCHECK ) )
						.setSelectedOptions( new String[0] );
				( ( XFormMultiSelectList )dialog.getFormField( CloneParameterDialog.PARAMETERS ) )
						.setSelectedOptions( new String[0] );
			}
		}
	}

	protected XFormDialog createCloneParameterDialog()
	{
		actionList = new DefaultActionList();
		OkAction okAction = new OkAction();
		actionList.addAction( okAction, true );
		CancelAction cancelAction = new CancelAction();
		actionList.addAction( cancelAction );
		ApplyAction applyAction = new ApplyAction();
		actionList.addAction( applyAction );

		dialog = ADialogBuilder.buildDialog( CloneParameterDialog.class, actionList, false );

		okAction.setDialog( dialog );
		cancelAction.setDialog( dialog );
		applyAction.setDialog( dialog );

		final TestCase testCase = securityCheck.getTestStep().getTestCase();
		final Project project = testCase.getTestSuite().getProject();

		dialog.getFormField( CloneParameterDialog.TARGET_TESTSUITE ).addFormFieldListener( new XFormFieldListener()
		{
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				TestSuite testSuite = project.getTestSuiteByName( newValue );
				String[] testCaseNames = ModelSupport.getNames( testSuite.getTestCaseList() );
				dialog.setOptions( CloneParameterDialog.TARGET_TESTCASE, testCaseNames );

				if( testCaseNames.length > 0 )
				{
					dialog.setValue( CloneParameterDialog.TARGET_TESTCASE, testCaseNames[0] );
					TestCase testCase = testSuite.getTestCaseByName( testCaseNames[0] );

					String[] testStepNames = new String[0];
					String[] securityTestNames = ModelSupport.getNames( testCase.getSecurityTestList() );
					dialog.setOptions( CloneParameterDialog.TARGET_SECURITYTEST, securityTestNames );
					if( securityTestNames.length > 0 )
					{
						testStepNames = getSecurableTestStepsNames( testCase );
					}
					dialog.setOptions( CloneParameterDialog.TARGET_TESTSTEP, testStepNames );

					if( securityTestNames.length > 0 )
					{
						dialog.setValue( CloneParameterDialog.TARGET_SECURITYTEST, securityTestNames[0] );

						if( testStepNames.length > 0 )
						{
							dialog.setValue( CloneParameterDialog.TARGET_TESTSTEP, testStepNames[0] );
						}
						else
						{
							dialog.setOptions( CloneParameterDialog.TARGET_TESTSTEP, new String[0] );
						}

						String securityTestName = dialog.getValue( CloneParameterDialog.TARGET_SECURITYTEST );
						SecurityTest securityTest = testCase.getSecurityTestByName( securityTestName );
						String testStepName = dialog.getValue( CloneParameterDialog.TARGET_TESTSTEP );
						TestStep testStep = testCase.getTestStepByName( testStepName );
						String[] securityCheckNames = ModelSupport.getNames( securityTest.getTestStepSecurityCheckByType(
								testStep.getId(), AbstractSecurityCheckWithProperties.class ) );
						dialog.setOptions( CloneParameterDialog.TARGET_SECURITYCHECK, securityCheckNames );
					}
				}
				else
				{
					dialog.setOptions( CloneParameterDialog.TARGET_SECURITYTEST, new String[0] );
					dialog.setOptions( CloneParameterDialog.TARGET_TESTSTEP, new String[0] );
					dialog.setOptions( CloneParameterDialog.TARGET_SECURITYCHECK, new String[0] );
				}
			}
		} );
		dialog.getFormField( CloneParameterDialog.TARGET_TESTCASE ).addFormFieldListener( new XFormFieldListener()
		{
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				String testSuiteName = dialog.getValue( CloneParameterDialog.TARGET_TESTSUITE );
				TestSuite testSuite = project.getTestSuiteByName( testSuiteName );
				TestCase testCase = testSuite.getTestCaseByName( newValue );

				String[] testStepNames = new String[0];
				String[] securityTestNames = ModelSupport.getNames( testCase.getSecurityTestList() );
				dialog.setOptions( CloneParameterDialog.TARGET_SECURITYTEST, securityTestNames );
				if( securityTestNames.length > 0 )
				{
					testStepNames = getSecurableTestStepsNames( testCase );
				}
				dialog.setOptions( CloneParameterDialog.TARGET_TESTSTEP, testStepNames );

				if( securityTestNames.length > 0 )
				{
					dialog.setValue( CloneParameterDialog.TARGET_SECURITYTEST, securityTestNames[0] );
					if( testStepNames.length > 0 )
					{
						dialog.setValue( CloneParameterDialog.TARGET_TESTSTEP, testStepNames[0] );
					}
					else
					{
						dialog.setOptions( CloneParameterDialog.TARGET_TESTSTEP, new String[0] );
					}

					String securityTestName = dialog.getValue( CloneParameterDialog.TARGET_SECURITYTEST );
					SecurityTest securityTest = testCase.getSecurityTestByName( securityTestName );
					String testStepName = dialog.getValue( CloneParameterDialog.TARGET_TESTSTEP );
					TestStep testStep = testCase.getTestStepByName( testStepName );
					String[] securityCheckNames = ModelSupport.getNames( securityTest.getTestStepSecurityCheckByType(
							testStep.getId(), AbstractSecurityCheckWithProperties.class ) );
					dialog.setOptions( CloneParameterDialog.TARGET_SECURITYCHECK, securityCheckNames );
				}
				else
				{
					dialog.setOptions( CloneParameterDialog.TARGET_SECURITYTEST, new String[0] );
					dialog.setOptions( CloneParameterDialog.TARGET_TESTSTEP, new String[0] );
					dialog.setOptions( CloneParameterDialog.TARGET_SECURITYCHECK, new String[0] );
				}
			}
		} );
		dialog.getFormField( CloneParameterDialog.TARGET_TESTSTEP ).addFormFieldListener( new XFormFieldListener()
		{
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				String testSuiteName = dialog.getValue( CloneParameterDialog.TARGET_TESTSUITE );
				TestSuite testSuite = project.getTestSuiteByName( testSuiteName );
				String testCaseName = dialog.getValue( CloneParameterDialog.TARGET_TESTCASE );
				TestCase testCase = testSuite.getTestCaseByName( testCaseName );
				String securityTestName = dialog.getValue( CloneParameterDialog.TARGET_SECURITYTEST );
				SecurityTest securityTest = testCase.getSecurityTestByName( securityTestName );
				TestStep testStep = testCase.getTestStepByName( newValue );

				String[] securityCheckNames = ModelSupport.getNames( securityTest.getTestStepSecurityCheckByType(
						testStep.getId(), AbstractSecurityCheckWithProperties.class ) );
				dialog.setOptions( CloneParameterDialog.TARGET_SECURITYCHECK, securityCheckNames );
			}
		} );
		dialog.getFormField( CloneParameterDialog.TARGET_SECURITYTEST ).addFormFieldListener( new XFormFieldListener()
		{
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				String testSuiteName = dialog.getValue( CloneParameterDialog.TARGET_TESTSUITE );
				TestSuite testSuite = project.getTestSuiteByName( testSuiteName );
				String testCaseName = dialog.getValue( CloneParameterDialog.TARGET_TESTCASE );
				TestCase testCase = testSuite.getTestCaseByName( testCaseName );
				SecurityTest securityTest = testCase.getSecurityTestByName( newValue );
				String testStepName = dialog.getValue( CloneParameterDialog.TARGET_TESTSTEP );
				TestStep testStep = testCase.getTestStepByName( testStepName );

				String[] securityCheckNames = ModelSupport.getNames( securityTest.getTestStepSecurityCheckByType(
						testStep.getId(), AbstractSecurityCheckWithProperties.class ) );
				dialog.setOptions( CloneParameterDialog.TARGET_SECURITYCHECK, securityCheckNames );
			}
		} );

		WsdlTestCase wsdlTestCase = ( WsdlTestCase )securityCheck.getTestStep().getTestCase();

		dialog.setOptions( CloneParameterDialog.TARGET_TESTSUITE,
				ModelSupport.getNames( wsdlTestCase.getTestSuite().getProject().getTestSuiteList() ) );
		dialog.setValue( CloneParameterDialog.TARGET_TESTSUITE, wsdlTestCase.getTestSuite().getName() );

		List<TestCase> wsdlTestCaseList = wsdlTestCase.getTestSuite().getTestCaseList();
		dialog.setOptions( CloneParameterDialog.TARGET_TESTCASE, ModelSupport.getNames( wsdlTestCaseList ) );
		dialog.setValue( CloneParameterDialog.TARGET_TESTCASE, wsdlTestCase.getName() );

		dialog.setOptions( CloneParameterDialog.TARGET_TESTSTEP, getSecurableTestStepsNames( wsdlTestCase ) );
		dialog.setOptions( CloneParameterDialog.TARGET_SECURITYTEST,
				ModelSupport.getNames( wsdlTestCase.getSecurityTestList() ) );

		String securityTestName = dialog.getValue( CloneParameterDialog.TARGET_SECURITYTEST );
		SecurityTest securityTest = wsdlTestCase.getSecurityTestByName( securityTestName );
		String testStepName = dialog.getValue( CloneParameterDialog.TARGET_TESTSTEP );
		TestStep testStep = wsdlTestCase.getTestStepByName( testStepName );

		String[] securityCheckNames = ModelSupport.getNames( securityTest.getTestStepSecurityCheckByType(
				testStep.getId(), AbstractSecurityCheckWithProperties.class ) );
		dialog.setOptions( CloneParameterDialog.TARGET_SECURITYCHECK, securityCheckNames );

		dialog.setOptions( CloneParameterDialog.PARAMETERS, securityCheck.getParameterHolder().getParameterLabels() );

		( ( JFormDialog )dialog ).getDialog().setResizable( false );

		return dialog;
	}

	public JUndoableTextArea getPathPane()
	{
		return pathPane;
	}

	private class AddAction extends AbstractAction
	{

		private XFormDialog dialog;

		public AddAction()
		{
			super( "Add" );
		}

		public void setDialog( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			if( dialog.getValue( AddParameterDialog.LABEL ) == null
					|| dialog.getValue( AddParameterDialog.LABEL ).trim().length() == 0 )
			{
				UISupport.showErrorMessage( "Label is required!" );
			}
			else
			{
				if( model.addParameter( dialog.getValue( AddParameterDialog.LABEL ),
						dialog.getValue( AddParameterDialog.NAME ), pathPane.getText() ) )
				{
					JComboBoxFormField nameField = ( JComboBoxFormField )dialog.getFormField( AddParameterDialog.NAME );
					nameField.setSelectedOptions( new Object[] { nameField.getOptions()[0] } );
					dialog.setValue( AddParameterDialog.LABEL, "" );
					pathPane.setText( "" );
				}
				else
					UISupport.showErrorMessage( "Label have to be unique!" );
			}
		}

	}

	@AForm( description = "Add New Security Test Step Parameter", name = "Configure Security Test Step Parameters" )
	interface AddParameterDialog
	{
		@AField( description = "Parameter Name", name = "Parameter Name", type = AFieldType.ENUMERATION )
		static String NAME = "Parameter Name";

		@AField( description = "Parameter Label", name = "Parameter Label", type = AFieldType.STRING )
		static String LABEL = "Parameter Label";

		@AField( description = "Parameter XPath", name = "XPath", type = AFieldType.COMPONENT )
		static String PATH = "XPath";
	}

	@AForm( description = "Specify target TestSuite/TestCase/Security Test(s)/Security Check(s) and select Parameters to clone", name = "Clone Parameters", icon = UISupport.TOOL_ICON_PATH )
	public interface CloneParameterDialog
	{
		@AField( name = "Parameters", description = "The Parameters to clone", type = AFieldType.MULTILIST )
		public final static String PARAMETERS = "Parameters";

		@AField( name = "SecurityChecks", description = "The SecurityChecks to clone to", type = AFieldType.MULTILIST )
		public final static String TARGET_SECURITYCHECK = "SecurityChecks";

		@AField( name = "Target TestStep", description = "The target TestStep for the cloned Parameter(s)", type = AFieldType.ENUMERATION )
		public final static String TARGET_TESTSTEP = "Target TestStep";

		@AField( name = "Target SecurityTest", description = "The target SecurityTest for the cloned Parameter(s)", type = AFieldType.ENUMERATION )
		public final static String TARGET_SECURITYTEST = "Target SecurityTest";

		@AField( name = "Target TestCase", description = "The target TestCase for the cloned Parameter(s)", type = AFieldType.ENUMERATION )
		public final static String TARGET_TESTCASE = "Target TestCase";

		@AField( name = "Target TestSuite", description = "The target TestSuite for the cloned Parameter(s)", type = AFieldType.ENUMERATION )
		public final static String TARGET_TESTSUITE = "Target TestSuite";

		@AField( name = "Overwrite", description = "Overwrite existing parameters", type = AFieldType.BOOLEAN )
		public final static String OVERWRITE = "Overwrite";
	}

}
