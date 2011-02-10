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

package com.eviware.soapui.security.log;

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
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.impl.wsdl.testcase.TestCaseLogItem;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityCheckResult;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityCheckStatus;
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
 * Panel for displaying SecurityChecks Results
 * 
 * @author dragica.soldo
 */

public class JSecurityTestRunLog extends JPanel
{
	private SecurityTestLogModel logListModel;
	private MessageExchangeModelItem requestModelItem;
	private JList testLogList;
	private boolean errorsOnly = false;
	private final Settings settings;
	private Set<String> boldTexts = new HashSet<String>();
	private boolean follow = true;
	protected int selectedIndex;
	private XFormDialog optionsDialog;
	private SoapMonitor soapMonitor;
	private JTabbedPane tabs;
	private SecurityTest securityTest;

	public JSecurityTestRunLog( SoapMonitor soapMonitor, JTabbedPane tabs )
	{
		super( new BorderLayout() );
		this.settings = SoapUI.getSettings();
		this.soapMonitor = soapMonitor;
		this.tabs = tabs;

		errorsOnly = settings.getBoolean( OptionsForm.class.getName() + "@errors_only" );

		buildUI();
	}

	public JSecurityTestRunLog( SecurityTest securityTest )
	{
		super( new BorderLayout() );
		this.securityTest = securityTest;
		this.settings = securityTest.getSettings();
		logListModel = securityTest.getSecurityTestLog();
		buildUI();
	}

	private void buildUI()
	{
		if( logListModel == null )
			logListModel = new SecurityTestLogModel();

		// logListModel = securityTest.getSecurityTestLog();
		logListModel.setMaxSize( ( int )settings.getLong( OptionsForm.class.getName() + "@max_rows", 1000 ) );

		testLogList = new JList( logListModel );
		testLogList.setCellRenderer( new SecurityTestLogCellRenderer() );
		// testLogList.setPrototypeCellValue( "Testing 123" );
		// testLogList.setFixedCellWidth( -1 );
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

	public boolean isErrorsOnly()
	{
		return errorsOnly;
	}

	public boolean isFollow()
	{
		return follow;
	}

	protected void addToolbarButtons( JXToolBar toolbar )
	{
		toolbar.addFixed( UISupport.createToolbarButton( new ClearLogAction() ) );
		toolbar.addFixed( UISupport.createToolbarButton( new SetLogOptionsAction() ) );
		toolbar.addFixed( UISupport.createToolbarButton( new ExportLogAction() ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLog#clear()
	 */
	public synchronized void clear()
	{
		logListModel.clear();
		boldTexts.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLog#addText(java.lang
	 * .String)
	 */
	// public synchronized void addEntry( SecurityTestLogMessageEntry
	// securityTestLogMessageEntry )
	// {
	// logListModel.addEntry( securityTestLogMessageEntry );
	// if( follow )
	// testLogList.ensureIndexIsVisible( logListModel.getSize() - 1 );
	// }

	/*
	 * 
	 */
	public synchronized void addSecurityCheckResult( SecurityCheckResult checkResult )
	{
		if( errorsOnly && checkResult.getStatus() != SecurityCheckRequestResult.SecurityCheckStatus.FAILED )
			return;

		logListModel.addSecurityCheckResult( checkResult );
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

	public SecurityTestLogModel getLogListModel()
	{
		return logListModel;
	}

	public void setLogListModel( SecurityTestLogModel logListModel )
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
					printLog( out );

					out.close();
				}
				catch( FileNotFoundException e1 )
				{
					UISupport.showErrorMessage( e1 );
				}
			}
		}
	}

	// public void setStepIndex( int i )
	// {
	// logListModel.setStepIndex( i );
	// }
	//
	// public synchronized void addBoldText( String string )
	// {
	// boldTexts.add( string );
	// addText( string );
	// }

	public void release()
	{
		if( optionsDialog != null )
		{
			optionsDialog.release();
			optionsDialog = null;
		}
	}

	public void printLog( PrintWriter out )
	{
		for( int c = 0; c < logListModel.getSize(); c++ )
		{
			Object value = logListModel.getElementAt( c );
			if( value instanceof String )
			{
				out.println( value.toString() );
			}
			// else if( value instanceof SecurityTestLogMessageEntry )
			// {
			// SecurityTestLogMessageEntry logItem = ( SecurityTestLogMessageEntry
			// )value;
			// String msg = logItem.getMessage();
			// if( StringUtils.hasContent( msg ) )
			// out.println( msg );
			// }
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
				SecurityCheckResult result = logListModel.getResultAt( index );
				// TODO see how this default action is implemented, maybe thats's
				// the way to implement opening
				// message exchange
				// if( result != null && result.getActions() != null )
				// result.getActions().performDefaultAction( new ActionEvent( this,
				// 0, null ) );
				if( result != null && !result.getSecurityRequestResultList().isEmpty() )
				{

					for( SecurityCheckRequestResult reqResult : result.getSecurityRequestResultList() )
					{
						ShowMessageExchangeAction showMessageExchangeAction = new ShowMessageExchangeAction( reqResult
								.getMessageExchange(), "SecurityCheck" );
						showMessageExchangeAction.actionPerformed( new ActionEvent( this, 0, null ) );
					}
				}
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

			SecurityTestLogMessageEntry result = ( SecurityTestLogMessageEntry )logListModel.getElementAt( row );
			if( result == null )
				return;

			ActionList actions = result.getActions();

			if( actions == null || actions.getActionCount() == 0 )
				return;

			JPopupMenu popup = ActionSupport.buildPopup( actions );
			UISupport.showPopup( popup, testLogList, e.getPoint() );
		}
	}

	public synchronized void addText( String string )
	{
		logListModel.addText( string );
		if( follow )
			testLogList.ensureIndexIsVisible( logListModel.getSize() - 1 );
	}

	private final class SecurityTestLogCellRenderer extends JLabel implements ListCellRenderer
	{
		private Font boldFont;
		private Font normalFont;
		private JHyperlinkLabel hyperlinkLabel = new JHyperlinkLabel( "" );

		public SecurityTestLogCellRenderer()
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

			SecurityCheckResult result = logListModel.getResultAt( index );
			if( result != null && !getText().startsWith( " ->" ) )
			{
				hyperlinkLabel.setText( getText() );
				hyperlinkLabel.setBackground( getBackground() );
				hyperlinkLabel.setEnabled( list.isEnabled() );

				if( result.getStatus() == SecurityCheckStatus.OK )
				{
					hyperlinkLabel.setIcon( UISupport.createImageIcon( "/valid_assertion.gif" ) );
				}
				else if( result.getStatus() == SecurityCheckStatus.FAILED )
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

}
