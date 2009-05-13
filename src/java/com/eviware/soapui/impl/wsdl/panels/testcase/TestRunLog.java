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

package com.eviware.soapui.impl.wsdl.panels.testcase;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.TestCaseLogItem;
import com.eviware.soapui.impl.wsdl.testcase.TestCaseLogModel;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.components.JHyperlinkLabel;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

/**
 * Panel for displaying TestStepResults
 * 
 * @author Ole.Matzura
 */

public class TestRunLog extends JPanel
{
	private TestCaseLogModel logListModel;
	private JList testLogList;
	private boolean errorsOnly = false;
	private final Settings settings;
	private Set<String> boldTexts = new HashSet<String>();
	private boolean follow = true;
	protected int selectedIndex;
	private XFormDialog optionsDialog;

	public TestRunLog( Settings settings )
	{
		super( new BorderLayout() );
		this.settings = settings;

		errorsOnly = settings.getBoolean( OptionsForm.class.getName() + "@errors_only" );

		buildUI();
	}

	private void buildUI()
	{
		logListModel = new TestCaseLogModel();
		logListModel.setMaxSize( ( int )settings.getLong( OptionsForm.class.getName() + "@max_rows", 1000 ) );

		testLogList = new JList( logListModel );
		testLogList.setCellRenderer( new TestLogCellRenderer() );
		testLogList.setPrototypeCellValue( "Testing 123" );
		testLogList.setFixedCellWidth( -1 );
		testLogList.addMouseListener( new LogListMouseListener() );

		JScrollPane scrollPane = new JScrollPane( testLogList );
		add( scrollPane, BorderLayout.CENTER );
		add( buildToolbar(), BorderLayout.NORTH );
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		addToolbarButtons( toolbar );

		return toolbar;
	}

	protected JList getTestLogList()
	{
		return testLogList;
	}

	protected void addToolbarButtons( JXToolBar toolbar )
	{
		toolbar.addFixed( UISupport.createToolbarButton( new ClearLogAction() ) );
		toolbar.addFixed( UISupport.createToolbarButton( new SetLogOptionsAction() ) );
		toolbar.addFixed( UISupport.createToolbarButton( new ExportLogAction() ) );
	}

	private final class TestLogCellRenderer extends JLabel implements ListCellRenderer
	{
		private Font boldFont;
		private Font normalFont;
		private JHyperlinkLabel hyperlinkLabel = new JHyperlinkLabel( "" );

		public TestLogCellRenderer()
		{
			setOpaque( true );
			setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
			setIcon( null );
			boldFont = getFont().deriveFont( Font.BOLD );
			normalFont = getFont();

			hyperlinkLabel.setOpaque( true );
			hyperlinkLabel.setForeground( Color.BLUE.darker().darker().darker() );
			hyperlinkLabel.setUnderlineColor( Color.GRAY );
			hyperlinkLabel.setBorder( BorderFactory.createEmptyBorder( 0, 4, 3, 3 ) );
		}

		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			if( isSelected )
			{
				setBackground( list.getSelectionBackground() );
				setForeground( list.getSelectionForeground() );
			}
			else
			{
				setBackground( list.getBackground() );
				setForeground( list.getForeground() );
			}

			if( value instanceof String )
			{
				setText( value.toString() );
			}
			else if( value instanceof TestCaseLogItem )
			{
				TestCaseLogItem logItem = ( TestCaseLogItem )value;
				String msg = logItem.getMsg();
				setText( msg == null ? "" : msg );
			}

			TestStepResult result = logListModel.getResultAt( index );
			if( result != null && !result.isDiscarded() && result.getActions() != null && !getText().startsWith( " ->" ) )
			{
				hyperlinkLabel.setText( getText() );
				hyperlinkLabel.setBackground( getBackground() );
				hyperlinkLabel.setEnabled( list.isEnabled() );

				if( result.getStatus() == TestStepStatus.OK )
				{
					hyperlinkLabel.setIcon( UISupport.createImageIcon( "/valid_assertion.gif" ) );
				}
				else if( result.getStatus() == TestStepStatus.FAILED )
				{
					hyperlinkLabel.setIcon( UISupport.createImageIcon( "/failed_assertion.gif" ) );
				}
				else
				{
					hyperlinkLabel.setIcon( UISupport.createImageIcon( "/unknown_assertion.gif" ) );
				}

				return hyperlinkLabel;
			}

			setEnabled( list.isEnabled() );

			if( boldTexts.contains( getText() ) )
			{
				setFont( boldFont );
			}
			else
			{
				setFont( normalFont );
			}

			return this;
		}
	}

	/**
	 * Mouse Listener for triggering default action and showing popup for log
	 * list items
	 * 
	 * @author Ole.Matzura
	 */

	private final class LogListMouseListener extends MouseAdapter
	{
		public void mouseClicked( MouseEvent e )
		{
			int index = testLogList.getSelectedIndex();
			if( index != -1 && ( index == selectedIndex || e.getClickCount() > 1 ) )
			{
				TestStepResult result = logListModel.getResultAt( index );
				if( result != null && result.getActions() != null )
					result.getActions().performDefaultAction( new ActionEvent( this, 0, null ) );
			}

			selectedIndex = index;
		}

		public void mousePressed( MouseEvent e )
		{
			if( e.isPopupTrigger() )
				showPopup( e );
		}

		public void mouseReleased( MouseEvent e )
		{
			if( e.isPopupTrigger() )
				showPopup( e );
		}

		public void showPopup( MouseEvent e )
		{
			int row = testLogList.locationToIndex( e.getPoint() );
			if( row == -1 )
				return;

			if( testLogList.getSelectedIndex() != row )
			{
				testLogList.setSelectedIndex( row );
			}

			TestStepResult result = logListModel.getResultAt( row );
			if( result == null )
				return;

			ActionList actions = result.getActions();

			if( actions == null || actions.getActionCount() == 0 )
				return;

			JPopupMenu popup = ActionSupport.buildPopup( actions );
			UISupport.showPopup( popup, testLogList, e.getPoint() );
		}
	}

	public synchronized void clear()
	{
		logListModel.clear();
		boldTexts.clear();
	}

	public synchronized void addText( String string )
	{
		logListModel.addText( string );
		if( follow )
			testLogList.ensureIndexIsVisible( logListModel.getSize() - 1 );
	}

	public synchronized void addTestStepResult( TestStepResult stepResult )
	{
		if( errorsOnly && stepResult.getStatus() != TestStepResult.TestStepStatus.FAILED )
			return;

		logListModel.addTestStepResult( stepResult );
		if( follow )
		{
			try
			{
				testLogList.ensureIndexIsVisible( logListModel.getSize() - 1 );
			}
			catch( RuntimeException e )
			{
			}
		}
	}

	public TestCaseLogModel getLogListModel()
	{
		return logListModel;
	}

	public void setLogListModel( TestCaseLogModel logListModel )
	{
		this.logListModel = logListModel;
		testLogList.setModel( logListModel );
	}

	private class SetLogOptionsAction extends AbstractAction
	{
		public SetLogOptionsAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/options.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Sets TestCase Log Options" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( optionsDialog == null )
				optionsDialog = ADialogBuilder.buildDialog( OptionsForm.class );

			optionsDialog.setIntValue( OptionsForm.MAXROWS, ( int )settings.getLong( OptionsForm.class.getName()
					+ "@max_rows", 1000 ) );
			optionsDialog.setBooleanValue( OptionsForm.ERRORSONLY, settings.getBoolean( OptionsForm.class.getName()
					+ "@errors_only" ) );
			optionsDialog.setBooleanValue( OptionsForm.FOLLOW, follow );

			if( optionsDialog.show() )
			{
				int maxRows = optionsDialog.getIntValue( OptionsForm.MAXROWS, 1000 );
				logListModel.setMaxSize( maxRows );
				settings.setLong( OptionsForm.class.getName() + "@max_rows", maxRows );
				errorsOnly = optionsDialog.getBooleanValue( OptionsForm.ERRORSONLY );
				settings.setBoolean( OptionsForm.class.getName() + "@errors_only", errorsOnly );

				follow = optionsDialog.getBooleanValue( OptionsForm.FOLLOW );
			}
		}
	}

	@AForm( name = "Log Options", description = "Set options for the run log below" )
	private static interface OptionsForm
	{
		@AField( name = "Max Rows", description = "Sets the maximum number of rows to keep in the log", type = AFieldType.INT )
		public static final String MAXROWS = "Max Rows";

		@AField( name = "Errors Only", description = "Logs only TestStep errors in the log", type = AFieldType.BOOLEAN )
		public static final String ERRORSONLY = "Errors Only";

		@AField( name = "Follow", description = "Follow log content", type = AFieldType.BOOLEAN )
		public static final String FOLLOW = "Follow";
	}

	private class ClearLogAction extends AbstractAction
	{
		public ClearLogAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/clear_loadtest.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Clears the log" );
		}

		public void actionPerformed( ActionEvent e )
		{
			logListModel.clear();
		}
	}

	private class ExportLogAction extends AbstractAction
	{
		public ExportLogAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/export.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Exports this log to a file" );
		}

		public void actionPerformed( ActionEvent e )
		{
			File file = UISupport.getFileDialogs().saveAs( this, "Save Log" );
			if( file != null )
			{
				try
				{
					PrintWriter out = new PrintWriter( file );
					for( int c = 0; c < logListModel.getSize(); c++ )
					{
						Object value = logListModel.getElementAt( c );
						if( value instanceof String )
						{
							out.println( value.toString() );
						}
						else if( value instanceof TestCaseLogItem )
						{
							TestCaseLogItem logItem = ( TestCaseLogItem )value;
							String msg = logItem.getMsg();
							if( StringUtils.hasContent( msg ) )
								out.println( msg );
						}
					}

					out.close();
				}
				catch( FileNotFoundException e1 )
				{
					UISupport.showErrorMessage( e1 );
				}
			}
		}
	}

	public static class TestRunLogTestRunListener extends TestRunListenerAdapter
	{
		private SimpleDateFormat dateFormat;
		private final TestRunLog runLog;
		private final boolean clearOnRun;

		public TestRunLogTestRunListener( TestRunLog runLog, boolean clearOnRun )
		{
			this.runLog = runLog;
			this.clearOnRun = clearOnRun;
			dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
		}

		public void beforeRun( TestRunner testRunner, TestRunContext runContext )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( testRunner.getTestCase() ) )
				return;

			if( clearOnRun )
				runLog.clear();

			String testCaseName = testRunner.getTestCase().getName();
			runLog.addBoldText( "TestCase [" + testCaseName + "] started at " + dateFormat.format( new Date() ) );
			runLog.setStepIndex( 0 );
		}

		public void afterRun( TestRunner testRunner, TestRunContext runContext )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( testRunner.getTestCase() ) )
				return;

			WsdlTestCaseRunner wsdlRunner = ( WsdlTestCaseRunner )testRunner;

			String testCaseName = testRunner.getTestCase().getName();
			if( testRunner.getStatus() == TestRunner.Status.CANCELED )
				runLog.addText( "TestCase [" + testCaseName + "] canceled [" + testRunner.getReason() + "], time taken = "
						+ wsdlRunner.getTimeTaken() );
			else if( testRunner.getStatus() == TestRunner.Status.FAILED )
			{
				String msg = wsdlRunner.getReason();
				if( wsdlRunner.getError() != null )
				{
					if( msg != null )
						msg += ":";

					msg += wsdlRunner.getError();
				}

				runLog.addText( "TestCase [" + testCaseName + "] failed [" + msg + "], time taken = "
						+ wsdlRunner.getTimeTaken() );
			}
			else
				runLog.addText( "TestCase [" + testCaseName + "] finished with status [" + testRunner.getStatus()
						+ "], time taken = " + wsdlRunner.getTimeTaken() );
		}

		public void afterStep( TestRunner testRunner, TestRunContext runContext, TestStepResult stepResult )
		{
			if( SoapUI.getTestMonitor().hasRunningLoadTest( testRunner.getTestCase() ) )
				return;

			runLog.addTestStepResult( stepResult );
		}
	}

	public void setStepIndex( int i )
	{
		logListModel.setStepIndex( i );
	}

	public synchronized void addBoldText( String string )
	{
		boldTexts.add( string );
		addText( string );
	}

	public void release()
	{
		if( optionsDialog != null )
		{
			optionsDialog.release();
			optionsDialog = null;
		}
	}
}
