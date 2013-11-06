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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Dialog for creating progress-dialogs
 * 
 * @author Ole.Matzura
 */

public class ProcessDialog extends JDialog implements RunnerContext
{
	private JProgressBar progressBar;
	private JLabel progressLabel;
	private JButton cancelButton;
	private JTextArea logArea;
	private JButton closeButton;
	private ToolRunner runner;
	private RunnerStatus status;
	private final static Logger log = Logger.getLogger( "toolLogger" );

	public ProcessDialog( String title, String description, boolean showLog, boolean allowCancel )
			throws HeadlessException
	{
		super( UISupport.getMainFrame() );
		setTitle( title );
		setModal( true );

		setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{

			public void windowClosing( WindowEvent e )
			{
				if( runner != null && !runner.isRunning() )
					dispose();
				else
					UISupport.showErrorMessage( "Cannot close while task is running.." );
			}
		} );

		progressBar = new JProgressBar( 0, 1 );
		progressBar.setValue( 0 );
		progressBar.setIndeterminate( false );

		getContentPane().setLayout( new BorderLayout() );

		if( description != null )
		{
			progressBar.setBorder( BorderFactory.createEmptyBorder( 10, 0, 0, 0 ) );

			JPanel p = new JPanel( new BorderLayout() );
			p.add( new JLabel( description ), BorderLayout.NORTH );
			p.add( progressBar, BorderLayout.CENTER );
			p.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			getContentPane().add( p, BorderLayout.NORTH );
		}
		else
		{
			progressBar.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			getContentPane().add( progressBar, BorderLayout.NORTH );
		}

		if( showLog )
			getContentPane().add( buildLog(), BorderLayout.CENTER );

		if( allowCancel )
		{
			ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
			builder.addGlue();
			cancelButton = new JButton( new CancelAction() );
			builder.addFixed( cancelButton );
			builder.addUnrelatedGap();

			if( showLog )
			{
				closeButton = new JButton( new CloseAction() );
				builder.addFixed( closeButton );
			}

			builder.addGlue();

			builder.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
			getContentPane().add( builder.getPanel(), BorderLayout.SOUTH );
		}
		else if( showLog )
		{
			ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
			builder.addGlue();

			closeButton = new JButton( new CloseAction() );
			builder.addFixed( closeButton );
			builder.addGlue();

			builder.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
			getContentPane().add( builder.getPanel(), BorderLayout.SOUTH );
		}

		pack();
	}

	private Component buildLog()
	{
		logArea = new JTextArea();
		logArea.setEditable( false );
		logArea.setBackground( Color.WHITE );
		JScrollPane scrollPane = new JScrollPane( logArea );
		scrollPane.setPreferredSize( new Dimension( 500, 300 ) );

		return UISupport.wrapInEmptyPanel( scrollPane, BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
	}

	public void setProgress( String string )
	{
		progressBar.setString( string );
	}

	public void run( ToolRunner toolRunner )
	{
		if( !SoapUI.getLogMonitor().hasLogArea( "toolLogger" ) )
			SoapUI.getLogMonitor().addLogArea( "tools", "toolLogger", false );

		this.runner = toolRunner;
		runner.setContext( this );
		Thread thread = new Thread( runner, toolRunner.getName() );
		thread.start();

		UISupport.centerDialog( this );
		setVisible( true );
	}

	private class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super( "Cancel" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( runner.isRunning() )
				runner.cancel();
		}
	}

	private final class CloseAction extends AbstractAction
	{
		public CloseAction()
		{
			super( "Close" );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			setVisible( false );
		}
	}

	public void setCancelLabel( String label )
	{
		if( cancelButton != null )
			cancelButton.setText( label );
	}

	public void setStatus( RunnerStatus status )
	{
		this.status = status;

		if( status == RunnerStatus.RUNNING )
		{
			progressBar.setIndeterminate( true );
			if( cancelButton != null )
				cancelButton.setEnabled( true );

			if( closeButton != null )
				closeButton.setEnabled( false );
		}
		else if( status == RunnerStatus.ERROR )
		{
			if( logArea == null )
			{
				setVisible( false );
				return;
			}

			progressBar.setIndeterminate( false );
			progressBar.setValue( 0 );
			if( cancelButton != null )
				cancelButton.setEnabled( false );

			if( closeButton != null )
				closeButton.setEnabled( true );
		}
		else if( status == RunnerStatus.FINISHED )
		{
			if( logArea == null )
			{
				setVisible( false );
				return;
			}

			progressBar.setIndeterminate( false );
			progressBar.setValue( 1 );
			if( cancelButton != null )
				cancelButton.setEnabled( false );

			if( closeButton != null )
				closeButton.setEnabled( true );
		}
	}

	public void disposeContext()
	{
	}

	public void log( String msg )
	{
		if( logArea == null )
			return;

		logArea.insert( msg, logArea.getText().length() );
		log.info( msg );
		try
		{
			logArea.setCaretPosition( logArea.getLineStartOffset( logArea.getLineCount() - 1 ) );
		}
		catch( BadLocationException e )
		{
			SoapUI.logError( e );
			log.error( e.toString() );
		}
	}

	public void logError( String msg )
	{
		log( msg );
	}

	public RunnerStatus getStatus()
	{
		return status;
	}
}
