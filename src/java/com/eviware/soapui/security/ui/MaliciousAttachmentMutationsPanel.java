package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.config.MaliciousAttachmentConfig;
import com.eviware.soapui.config.MaliciousAttachmentElementConfig;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.support.MaliciousAttachmentFilesListForm;
import com.eviware.soapui.security.support.MaliciousAttachmentGenerateTableModel;
import com.eviware.soapui.security.support.MaliciousAttachmentListToTableHolder;
import com.eviware.soapui.security.support.MaliciousAttachmentReplaceTableModel;
import com.eviware.soapui.security.support.MaliciousAttachmentTableModel;
import com.eviware.soapui.security.tools.RandomFile;
import com.eviware.soapui.settings.ProjectSettings;
import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JCheckBoxFormField;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JTextFieldFormField;

public class MaliciousAttachmentMutationsPanel
{
	private JFormDialog dialog;
	private MaliciousAttachmentSecurityCheckConfig config;
	private TestStep testStep;
	private JButton addGeneratedButton;
	private JButton removeGeneratedButton;
	private JButton addReplacementButton;
	private JButton removeReplacementButton;
	// private AttachmentContainer container;

	private MaliciousAttachmentListToTableHolder holder = new MaliciousAttachmentListToTableHolder();

	public MaliciousAttachmentMutationsPanel( MaliciousAttachmentSecurityCheckConfig config, TestStep testStep )
	{
		this.config = config;
		this.testStep = testStep;

		// if( testStep instanceof WsdlTestRequestStep )
		// {
		// WsdlTestRequestStep wsdlTestRequestStep = ( WsdlTestRequestStep
		// )testStep;
		//
		// if( wsdlTestRequestStep.getTestRequest() instanceof
		// MutableAttachmentContainer )
		// {
		// container = ( MutableAttachmentContainer
		// )wsdlTestRequestStep.getTestRequest();
		// }
		// }

		dialog = ( JFormDialog )ADialogBuilder.buildDialog( MutationSettings.class );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "component", createMutationsPanel() );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "dimension", new Dimension( 720, 320 ) );
	}

	private JComponent buildFilesList()
	{
		MaliciousAttachmentFilesListForm filesList = new MaliciousAttachmentFilesListForm( config, holder );
		holder.setFilesList( filesList );
		JScrollPane scrollPane = new JScrollPane( filesList );
		return scrollPane;
	}

	private JComponent buildTables()
	{
		JFormDialog tablesDialog = ( JFormDialog )ADialogBuilder.buildDialog( MutationTables.class );

		MaliciousAttachmentTableModel generateTableModel = new MaliciousAttachmentGenerateTableModel();
		tablesDialog.getFormField( MutationTables.GENERATE_FILE ).setProperty( "dimension", new Dimension( 410, 120 ) );
		tablesDialog.getFormField( MutationTables.GENERATE_FILE ).setProperty( "component",
				buildGenerateTable( generateTableModel ) );

		MaliciousAttachmentTableModel replaceTableModel = new MaliciousAttachmentReplaceTableModel();
		tablesDialog.getFormField( MutationTables.REPLACE_FILE ).setProperty( "dimension", new Dimension( 410, 120 ) );
		tablesDialog.getFormField( MutationTables.REPLACE_FILE ).setProperty( "component",
				buildReplacementTable( replaceTableModel ) );

		holder.setGenerateTableModel( generateTableModel );
		holder.setReplaceTableModel( replaceTableModel );
		holder.setTablesDialog( tablesDialog );

		JCheckBoxFormField remove = ( JCheckBoxFormField )tablesDialog.getFormField( MutationTables.REMOVE_FILE );
		remove.addFormFieldListener( new XFormFieldListener()
		{
			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				for( MaliciousAttachmentElementConfig element : config.getElementList() )
				{
					if( element.getKey().equals( holder.getTablesDialog().getValue( MutationTables.SELECTED_FILE ) ) )
					{
						element.setRemove( Boolean.parseBoolean( newValue ) );
						break;
					}
				}
			}
		} );

		return tablesDialog.getPanel();
	}

	protected JPanel buildGenerateTable( MaliciousAttachmentTableModel tableModel )
	{
		final JPanel panel = new JPanel( new BorderLayout() );
		final JXTable table = new JXTable( tableModel );
		setupTable( table );
		JScrollPane tableScrollPane = new JScrollPane( table );
		tableScrollPane.setBorder( BorderFactory.createEmptyBorder() );

		JXToolBar toolbar = UISupport.createToolbar();

		addGeneratedButton = UISupport.createToolbarButton( new GenerateFileAction() );
		toolbar.add( addGeneratedButton );

		removeGeneratedButton = UISupport.createToolbarButton( new RemoveGeneratedFileAction( tableModel, table ) );
		toolbar.add( removeGeneratedButton );
		removeGeneratedButton.setEnabled( false );

		toolbar.add( UISupport.createToolbarButton( new HelpAction( "www.soapui.org" ) ) );

		panel.add( toolbar, BorderLayout.PAGE_START );
		panel.add( tableScrollPane, BorderLayout.CENTER );

		table.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{
			public void valueChanged( ListSelectionEvent e )
			{
				if( addGeneratedButton != null )
				{
					addGeneratedButton.setEnabled( holder.getFilesList().getList().getModel().getSize() > 0 );
				}

				if( removeGeneratedButton != null )
				{
					removeGeneratedButton.setEnabled( table.getSelectedRowCount() > 0 );
				}
			}
		} );

		panel.setBorder( BorderFactory.createLineBorder( new Color( 0 ), 1 ) );

		return panel;
	}

	protected JPanel buildReplacementTable( MaliciousAttachmentTableModel tableModel )
	{
		final JPanel panel = new JPanel( new BorderLayout() );
		final JXTable table = new JXTable( tableModel );
		setupTable( table );
		JScrollPane tableScrollPane = new JScrollPane( table );
		tableScrollPane.setBorder( BorderFactory.createEmptyBorder() );

		JXToolBar toolbar = UISupport.createToolbar();

		addReplacementButton = UISupport.createToolbarButton( new AddFileAction() );
		toolbar.add( addReplacementButton );

		removeReplacementButton = UISupport.createToolbarButton( new RemoveReplacementFileAction( tableModel, table ) );
		toolbar.add( removeReplacementButton );
		removeReplacementButton.setEnabled( false );

		toolbar.add( UISupport.createToolbarButton( new HelpAction( "www.soapui.org" ) ) );

		panel.add( toolbar, BorderLayout.PAGE_START );
		panel.add( tableScrollPane, BorderLayout.CENTER );

		table.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{
			public void valueChanged( ListSelectionEvent e )
			{
				if( addReplacementButton != null )
				{
					addReplacementButton.setEnabled( holder.getFilesList().getList().getModel().getSize() > 0 );
				}

				if( removeReplacementButton != null )
				{
					removeReplacementButton.setEnabled( table.getSelectedRowCount() > 0 );
				}
			}
		} );

		panel.setBorder( BorderFactory.createLineBorder( new Color( 0 ), 1 ) );

		return panel;
	}

	protected void setupTable( JXTable table )
	{
		table.setPreferredScrollableViewportSize( new Dimension( 50, 90 ) );
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		table.getTableHeader().setReorderingAllowed( false );
		table.setDefaultEditor( String.class, getDefaultCellEditor() );
		table.setSortable( false );
	}

	private Object createMutationsPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		JSplitPane mainSplit = UISupport.createHorizontalSplit( buildFilesList(), buildTables() );
		mainSplit.setResizeWeight( 1 );
		panel.add( mainSplit, BorderLayout.CENTER );
		return panel;
	}

	public JComponent getPanel()
	{
		MaliciousAttachmentFilesListForm filesList = holder.getFilesList();

		Attachment[] data = new Attachment[] {};
		if( testStep instanceof WsdlTestRequestStep )
		{
			Attachment[] attachments = ( ( WsdlTestRequestStep )testStep ).getHttpRequest().getAttachments();
			data = new Attachment[attachments.length];
			for( int i = 0; i < attachments.length; i++ )
			{
				data[i] = ( ( WsdlTestRequestStep )testStep ).getTestRequest().getAttachmentAt( i );
			}
		}
		
		filesList.setData( data );
		holder.refresh();
		
		return dialog.getPanel();
	}

	@AForm( description = "Malicious Attachment Mutations", name = "Malicious Attachment Mutations" )
	public interface MutationSettings
	{
		@AField( description = "###Mutations panel", name = "###Mutations panel", type = AFieldType.COMPONENT )
		final static String MUTATIONS_PANEL = "###Mutations panel";
	}

	@AForm( description = "Malicious Attachment Mutation Tables", name = "Malicious Attachment Mutation Tables" )
	public interface MutationTables
	{
		@AField( description = "Selected", name = "Selected", type = AFieldType.LABEL )
		final static String SELECTED_FILE = "Selected";
		@AField( description = "Generate file", name = "Generate file", type = AFieldType.COMPONENT )
		final static String GENERATE_FILE = "Generate file";
		@AField( description = "Replace file", name = "Replace file", type = AFieldType.COMPONENT )
		final static String REPLACE_FILE = "Replace file";
		@AField( description = "Remove file", name = "Do not send attachment", type = AFieldType.BOOLEAN )
		final static String REMOVE_FILE = "Do not send attachment";
	}

	@AForm( description = "Generate File Mutation", name = "Generate File Mutation" )
	public interface GenerateFile
	{
		@AField( description = "Size(Mbyte)", name = "Size(Mbyte)", type = AFieldType.INT )
		final static String SIZE = "Size(Mbyte)";
		@AField( description = "Content type", name = "Content type", type = AFieldType.STRING )
		final static String CONTENT_TYPE = "Content type";
	}

	public class AddFileAction extends AbstractAction
	{
		private JFileChooser fileChooser;

		public AddFileAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Add file" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( fileChooser == null )
			{
				fileChooser = new JFileChooser();

			}

			// String root = PathUtils.getExpandedResourceRoot(
			// container.getModelItem() );
			String root = ProjectSettings.PROJECT_ROOT;

			if( StringUtils.hasContent( root ) )
			{
				fileChooser.setCurrentDirectory( new File( root ) );
			}

			int returnVal = fileChooser.showOpenDialog( UISupport.getMainFrame() );

			if( returnVal == JFileChooser.APPROVE_OPTION )
			{

				File file = fileChooser.getSelectedFile();
				Boolean retval = UISupport.confirmOrCancel( "Cache attachment in request?", "Add Attachment" );
				if( retval == null )
				{
					return;
				}

				String filename = file.getAbsolutePath();
				Long size = file.length();
				String contentType = new MimetypesFileTypeMap().getContentType( file );
				Boolean enabled = new Boolean( true );
				Boolean cached = retval;

				for( MaliciousAttachmentElementConfig element : config.getElementList() )
				{
					if( element.getKey().equals( holder.getTablesDialog().getValue( MutationTables.SELECTED_FILE ) ) )
					{
						MaliciousAttachmentConfig att = element.addNewReplaceAttachment();
						att.setFilename( filename );
						att.setSize( size );
						att.setContentType( contentType );
						att.setEnabled( enabled );
						att.setCached( cached );

						holder.addResultToReplaceTable( att );
					}
				}
			}
		}
	}

	public class GenerateFileAction extends AbstractAction
	{
		private XFormDialog dialog;

		public GenerateFileAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Generate file" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( dialog == null )
			{
				dialog = ADialogBuilder.buildDialog( GenerateFile.class );
				( ( JTextFieldFormField )dialog.getFormField( GenerateFile.CONTENT_TYPE ) ).setWidth( 30 );
			}

			dialog.show();

			if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
			{
				int newSizeInt = 0;
				String newSizeString = dialog.getValue( GenerateFile.SIZE );
				String contentType = dialog.getFormField( GenerateFile.CONTENT_TYPE ).getValue();

				try
				{
					newSizeInt = Integer.parseInt( newSizeString ) * 1024 * 1024;
				}
				catch( NumberFormatException nfe )
				{
					UISupport.showErrorMessage( "Size must be integer number" );
					return;
				}

				try
				{
					File file = new RandomFile( newSizeInt, "attachment", contentType ).next();

					String filename = file.getAbsolutePath();
					Long size = file.length();
					Boolean enabled = new Boolean( true );
					Boolean cached = new Boolean( true );

					for( MaliciousAttachmentElementConfig element : config.getElementList() )
					{
						if( element.getKey().equals( holder.getTablesDialog().getValue( MutationTables.SELECTED_FILE ) ) )
						{
							MaliciousAttachmentConfig att = element.addNewGenerateAttachment();
							att.setFilename( filename );
							att.setSize( size );
							att.setContentType( contentType );
							att.setEnabled( enabled );
							att.setCached( cached );

							holder.addResultToGenerateTable( att );
						}
					}
				}
				catch( Exception e1 )
				{
					UISupport.showErrorMessage( e1 );
				}
			}
		}
	}

	public class RemoveReplacementFileAction extends AbstractAction
	{
		private MaliciousAttachmentTableModel tableModel;
		private JXTable table;

		public RemoveReplacementFileAction( MaliciousAttachmentTableModel tableModel, JXTable table )
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Remove file" );

			this.tableModel = tableModel;
			this.table = table;
		}

		public void actionPerformed( ActionEvent e )
		{
			int row = table.getSelectedRow();
			if( row >= 0 )
			{
				tableModel.removeResult( row );

				for( int i = 0; i < config.getElementList().size(); i++ )
				{
					MaliciousAttachmentElementConfig element = config.getElementList().get( i );

					if( element.getKey().equals( holder.getTablesDialog().getValue( MutationTables.SELECTED_FILE ) ) )
					{
						element.getReplaceAttachmentList().remove( row );
					}
				}
			}
		}
	}

	public class RemoveGeneratedFileAction extends AbstractAction
	{
		private MaliciousAttachmentTableModel tableModel;
		private JXTable table;

		public RemoveGeneratedFileAction( MaliciousAttachmentTableModel tableModel, JXTable table )
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Remove file" );

			this.tableModel = tableModel;
			this.table = table;
		}

		public void actionPerformed( ActionEvent e )
		{
			int row = table.getSelectedRow();
			if( row >= 0 )
			{
				tableModel.removeResult( row );

				for( int i = 0; i < config.getElementList().size(); i++ )
				{
					MaliciousAttachmentElementConfig element = config.getElementList().get( i );

					if( element.getKey().equals( holder.getTablesDialog().getValue( MutationTables.SELECTED_FILE ) ) )
					{
						element.getGenerateAttachmentList().remove( row );
					}
				}
			}
		}
	}

	public class HelpAction extends AbstractAction implements HelpActionMarker
	{
		private final String url;

		public HelpAction( String url )
		{
			this( "Online Help", url, UISupport.getKeyStroke( "F1" ) );
		}

		public HelpAction( String title, String url )
		{
			this( title, url, null );
		}

		public HelpAction( String title, String url, KeyStroke accelerator )
		{
			super( title );
			this.url = url;
			putValue( Action.SHORT_DESCRIPTION, "Show online help" );
			if( accelerator != null )
				putValue( Action.ACCELERATOR_KEY, accelerator );

			putValue( Action.SMALL_ICON, UISupport.HELP_ICON );
		}

		public void actionPerformed( ActionEvent e )
		{
			Tools.openURL( url );
		}
	}

	protected TableCellEditor getDefaultCellEditor()
	{
		return new XPathCellRender();
	}

}
