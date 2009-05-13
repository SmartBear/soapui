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

package com.eviware.soapui.support.components;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.SwingWorkerDelegator;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Dialog for creating progress-dialogs
 * 
 * @author Ole.Matzura
 */

public class ProgressDialog extends JDialog implements XProgressDialog, XProgressMonitor
{
	private JProgressBar progressBar;
	private JLabel progressLabel;
	private JButton cancelButton;
	private Worker worker;

	public ProgressDialog( String title, String label, int length, String initialValue, boolean allowCancel )
			throws HeadlessException
	{
		super( UISupport.getMainFrame(), title, true );

		setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );

		progressBar = new JProgressBar( 0, length );
		JPanel panel = UISupport.createProgressBarPanel( progressBar, 10, true );
		progressBar.setString( initialValue );

		getContentPane().setLayout( new BorderLayout() );
		progressLabel = new JLabel( label );
		progressLabel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 0, 10 ) );

		getContentPane().add( progressLabel, BorderLayout.NORTH );
		getContentPane().add( panel, BorderLayout.CENTER );

		if( allowCancel )
		{
			ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
			builder.addGlue();
			cancelButton = new JButton( new CancelAction() );
			builder.addFixed( cancelButton );
			builder.addGlue();
			builder.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
			getContentPane().add( builder.getPanel(), BorderLayout.SOUTH );
		}

		pack();
	}

	public void run( Worker worker )
	{
		this.worker = worker;
		SwingWorkerDelegator swingWorker = new SwingWorkerDelegator( this, this, worker )
		{
			@Override
			public void finished()
			{
				super.finished();
				ProgressDialog.this.worker = null;
			}
		};

		swingWorker.start();
		setVisible( true );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.support.components.XProgressMonitor#setProgress(int,
	 * java.lang.String)
	 */
	public void setProgress( int value, String string )
	{
		progressBar.setValue( value );
		progressBar.setString( string );

		pack();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.support.components.XProgressMonitor#setVisible(boolean)
	 */
	public void setVisible( boolean visible )
	{
		if( visible == true )
		{
			UISupport.centerDialog( this );
		}

		super.setVisible( visible );
	}

	private class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super( "Cancel" );
		}

		public void actionPerformed( ActionEvent e )
		{
			worker.onCancel();
		}
	}

	public void setCancelLabel( String label )
	{
		if( cancelButton != null )
			cancelButton.setText( label );
	}
}
