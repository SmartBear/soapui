package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.model.security.MaliciousAttachmentsTableModel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.StringListFormComponent;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JFormDialog;

public class MaliciousAttachmentMutationsPanel
{

	private JFormDialog dialog;
	private MaliciousAttachmentSecurityCheckConfig config;
	private MaliciousAttachmentsTableModel tableModel;

	public MaliciousAttachmentMutationsPanel( MaliciousAttachmentSecurityCheckConfig config )
	{
		this.config = config;
		dialog = ( JFormDialog )ADialogBuilder.buildDialog( MutationSettings.class );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "component", createMutationsPanel() );
		initDialog();
	}

	private JComponent buildFilesList()
	{
		StringListFormComponent filesList = new StringListFormComponent( null, true, true, "" );

		filesList.setPreferredSize( new Dimension( 50, 400 ) );

		filesList.setData( new String[] { "file1", "file2", "file3" } );

		return filesList;
	}

	private JComponent buildTables()
	{
		// JPanel panel = new JPanel( new BorderLayout() );
		// JXTable table = new JXTable( new TransfersTableModel() );
		//
		// //table.setColumnControlVisible( true );
		// table.setHorizontalScrollEnabled( true );
		// table.packAll();
		return new JPanel( new BorderLayout() );
	}

	private Object createMutationsPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		JComponent filesList = buildFilesList();
		JSplitPane mainSplit = UISupport.createHorizontalSplit( filesList, buildTables() );
		mainSplit.setResizeWeight( 0.4 );
		panel.add( mainSplit, BorderLayout.CENTER );

		panel.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );

		return panel;
	}

	private void initDialog()
	{

	}

	public JComponent getPanel()
	{
		return dialog.getPanel();
	}

	@AForm( description = "Malicious Attachment Mutations", name = "Malicious Attachment Mutations" )
	protected interface MutationSettings
	{
		@AField( description = "###Mutations panel", name = "###Mutations panel", type = AFieldType.COMPONENT )
		final static String MUTATIONS_PANEL = "###Mutations panel";
	}

}
