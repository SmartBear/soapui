package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.text.Document;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.settings.ProjectSettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JFormComponent;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.types.StringList;
import com.eviware.x.form.XForm.FieldType;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormTextField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.AbstractSwingXFormField;
import com.eviware.x.impl.swing.JFormDialog;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.l2fprod.common.swing.JDirectoryChooser;

public class MaliciousAttachmentMutationsPanel
{

	private JFormDialog dialog;
	private MaliciousAttachmentSecurityCheckConfig config;
	private MutationTableModel generateTableModel;
	private MutationTableModel replaceTableModel;

	private Map<String, Object> tableMap = new HashMap<String, Object>();

	public MaliciousAttachmentMutationsPanel( MaliciousAttachmentSecurityCheckConfig config )
	{
		this.config = config;
		dialog = ( JFormDialog )ADialogBuilder.buildDialog( MutationSettings.class );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "component", createMutationsPanel() );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "dimension", new Dimension( 720, 320 ) );
		initDialog();
	}

	private JComponent buildFilesList()
	{
		FileListFormComponent filesList = new FileListFormComponent();
		filesList.setData( new String[] { "workspace/soapUI-core-DEV/copyrightInXsd.groovy", "file2", "file3" } );
		JScrollPane scrollPane = new JScrollPane( filesList );
		return scrollPane;
	}

	protected void update( String key )
	{

	}

	private JComponent buildTables()
	{
		JFormDialog dialog = ( JFormDialog )ADialogBuilder.buildDialog( MutationTables.class );

		generateTableModel = new GenerateMutationTableModel();
		dialog.getFormField( MutationTables.GENERATE_FILE ).setProperty( "dimension", new Dimension( 410, 120 ) );
		dialog.getFormField( MutationTables.GENERATE_FILE ).setProperty( "component",
				buildTable( generateTableModel, false ) );

		replaceTableModel = new ReplaceMutationTableModel();
		dialog.getFormField( MutationTables.REPLACE_FILE ).setProperty( "dimension", new Dimension( 410, 120 ) );
		dialog.getFormField( MutationTables.REPLACE_FILE )
				.setProperty( "component", buildTable( replaceTableModel, true ) );

		return dialog.getPanel();
	}

	protected JPanel buildTable( MutationTableModel tableModel, boolean add )
	{
		JPanel panel = new JPanel( new BorderLayout() );
		JXTable table = new JXTable( tableModel );
		setupTable( table );
		JScrollPane tableScrollPane = new JScrollPane( table );
		tableScrollPane.setBorder( BorderFactory.createEmptyBorder() );

		JXToolBar toolbar = UISupport.createToolbar();

		if( add )
		{
			toolbar.add( UISupport.createToolbarButton( new AddFileAction() ) );
		}
		else
		{
			// generate
			toolbar.add( UISupport.createToolbarButton( new GenerateFileAction() ) );
		}

		toolbar.add( UISupport.createToolbarButton( new RemoveFileAction() ) );
		toolbar.add( UISupport.createToolbarButton( new HelpAction( "www.soapui.org" ) ) );

		panel.add( toolbar, BorderLayout.PAGE_START );
		panel.add( tableScrollPane, BorderLayout.CENTER );

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

		InternalCellRenderer internalCellRenderer = new InternalCellRenderer();

		for( int c = 0; c < table.getColumnCount(); c++ )
		{
			table.getColumn( c ).setCellRenderer( internalCellRenderer );

			if( c == table.getColumnCount() - 1 )
			{
				table.getColumn( c ).setPreferredWidth( 30 );
			}
		}
	}

	// private void initColumnSizes(JXTable table, Malic) {
	// TableColumn column = null;
	// Component comp = null;
	// int headerWidth = 0;
	// int cellWidth = 0;
	// Object[] longValues = model.longValues;
	// TableCellRenderer headerRenderer =
	// table.getTableHeader().getDefaultRenderer();
	//
	// for (int i = 0; i < 5; i++) {
	// column = table.getColumnModel().getColumn(i);
	//
	// comp = headerRenderer.getTableCellRendererComponent(
	// null, column.getHeaderValue(),
	// false, false, 0, 0);
	// headerWidth = comp.getPreferredSize().width;
	//
	// comp = table.getDefaultRenderer(model.getColumnClass(i)).
	// getTableCellRendererComponent(
	// table, longValues[i],
	// false, false, 0, i);
	// cellWidth = comp.getPreferredSize().width;
	//
	// if (DEBUG) {
	// System.out.println("Initializing width of column "
	// + i + ". "
	// + "headerWidth = " + headerWidth
	// + "; cellWidth = " + cellWidth);
	// }
	//
	// column.setPreferredWidth(Math.max(headerWidth, cellWidth));
	// }
	// }

	private Object createMutationsPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		JSplitPane mainSplit = UISupport.createHorizontalSplit( buildFilesList(), buildTables() );
		mainSplit.setResizeWeight( 1 );
		panel.add( mainSplit, BorderLayout.CENTER );

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

	@AForm( description = "Malicious Attachment Mutation Tables", name = "Malicious Attachment Mutation Tables" )
	protected interface MutationTables
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
	protected interface GenerateFile
	{
		@AField( description = "Size", name = "Size", type = AFieldType.INT )
		final static String SIZE = "Size";
		@AField( description = "Content type", name = "Content type", type = AFieldType.STRING )
		final static String CONTENT_TYPE = "Content type";
	}

	class FileListFormComponent extends JPanel implements JFormComponent, ActionListener
	{
		private DefaultListModel listModel;
		private JList list;

		public FileListFormComponent()
		{
			super( new BorderLayout() );

			listModel = new DefaultListModel();
			list = new JList( listModel );
			list.setToolTipText( "Choose file" );
			JScrollPane scrollPane = new JScrollPane( list );
			scrollPane.setPreferredSize( new Dimension( 30, 50 ) );
			add( scrollPane, BorderLayout.CENTER );

			list.addListSelectionListener( new ListSelectionListener()
			{

				public void valueChanged( ListSelectionEvent e )
				{
					String key = null;
					MaliciousAttachmentMutationsPanel.this.update( key );
				}
			} );
		}

		public void setValue( String value )
		{
			String[] oldData = getData();
			listModel.clear();

			try
			{
				StringList stringList = StringList.fromXml( value );

				String[] files = stringList.toStringArray();
				for( String file : files )
					if( file.trim().length() > 0 )
						listModel.addElement( file );

				firePropertyChange( "data", oldData, getData() );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}

		public String getValue()
		{
			StringList result = new StringList( listModel.toArray() );
			return result.toXml();
		}

		public JList getList()
		{
			return list;
		}

		public void actionPerformed( ActionEvent e )
		{
			String[] oldData = getData();

			int selectedIndex = list.getSelectedIndex();

			String elm = ( String )listModel.getElementAt( selectedIndex );
			String value = UISupport.prompt( "Specify value", "Edit..", elm );

			if( value != null )
			{
				listModel.setElementAt( value, selectedIndex );
				firePropertyChange( "options", oldData, getData() );
			}
		}

		public String[] getData()
		{
			String[] result = new String[listModel.size()];
			for( int c = 0; c < result.length; c++ )
				result[c] = ( String )listModel.get( c );

			return result;
		}

		public void setData( String[] strings )
		{
			String[] oldData = getData();

			listModel.clear();
			if( strings != null )
			{
				for( String str : strings )
				{
					listModel.addElement( str );
				}
			}

			firePropertyChange( "options", oldData, getData() );
		}

		public String[] getOptions()
		{
			return getData();
		}

		public void setOptions( String[] options )
		{
			setData( options );
		}

		public void addItem( String valueOf )
		{
			listModel.addElement( valueOf );
		}
	}

	class FileElement
	{
		File file;
		Boolean enabled;

		FileElement( File file, Boolean enabled )
		{
			this.file = file;
			this.enabled = enabled;
		}
	}

	private abstract class MutationTableModel extends AbstractTableModel
	{
		protected List<FileElement> files = new ArrayList<FileElement>();

		public synchronized int getRowCount()
		{
			return files.size();
		}

		public void addResult( FileElement element )
		{
			int rowCount;
			synchronized( this )
			{
				rowCount = getRowCount();
				files.add( element );
			}

			fireTableRowsInserted( rowCount, rowCount++ );
		}

		public void removeResult( FileElement element )
		{
			int rowCount;
			synchronized( this )
			{
				rowCount = getRowCount();
				files.remove( element );
			}

			fireTableRowsDeleted( rowCount, rowCount-- );
		}

		public abstract int getColumnCount();

		public abstract String getColumnName( int column );

		public abstract Object getValueAt( int rowIndex, int columnIndex );

		public Class getColumnClass( int c )
		{
			return getValueAt( 0, c ).getClass();
		}
	}

	private class GenerateMutationTableModel extends MutationTableModel
	{
		public boolean isCellEditable( int row, int col )
		{
			if( col > 0 )
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public int getColumnCount()
		{
			return 3;
		}

		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "Size";
			case 1 :
				return "Content type";
			case 2 :
				return "Enable";
			}

			return null;
		}

		public synchronized Object getValueAt( int rowIndex, int columnIndex )
		{
			FileElement element = files.get( rowIndex );

			if( element != null )
			{
				switch( columnIndex )
				{
				case 0 :
					return element.file.length();
				case 1 :
					return new MimetypesFileTypeMap().getContentType( element.file );
				case 2 :
					return element.enabled;
				}
			}

			return null;
		}
	}

	private class ReplaceMutationTableModel extends MutationTableModel
	{

		public boolean isCellEditable( int row, int col )
		{
			if( col == 2 || col == 3 )
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public int getColumnCount()
		{
			return 4;
		}

		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "With";
			case 1 :
				return "Size";
			case 2 :
				return "Content type";
			case 3 :
				return "Enable";
			}

			return null;
		}

		public synchronized Object getValueAt( int rowIndex, int columnIndex )
		{
			FileElement element = files.get( rowIndex );

			if( element != null )
			{
				switch( columnIndex )
				{
				case 0 :
					return "file";
				case 1 :
					return element.file.length();
				case 2 :
					return new MimetypesFileTypeMap().getContentType( element.file );
				case 3 :
					return element.enabled;
				}
			}

			return null;
		}
	}

	// private void updateValue( String value )
	// {
	// if( value != null && projectRoot != null && value.startsWith( projectRoot
	// ) )
	// {
	// if( value.equals( projectRoot ) )
	// value = "";
	// else if( value.length() > projectRoot.length() + 1 )
	// value = value.substring( projectRoot.length() + 1 );
	// }
	//
	// textField.setText( value );
	// }

	public class AddFileAction extends AbstractAction
	{
		private JFileChooser fileChooser;
		private String projectRoot = ProjectSettings.PROJECT_ROOT;

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

			fileChooser.setCurrentDirectory( new File( projectRoot ) );

			int returnVal = fileChooser.showOpenDialog( UISupport.getMainFrame() );
			if( returnVal == JFileChooser.APPROVE_OPTION )
			{
				// updateValue( fileChooser.getSelectedFile().getAbsolutePath() );
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
			}

			dialog.show();

			if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
			{
				Integer size = dialog.getIntValue( GenerateFile.SIZE, 0 );
				String contentType = dialog.getFormField( GenerateFile.CONTENT_TYPE ).getValue();

				generateTableModel.addResult( new FileElement( new File( "fake_file" ), true ) );
			}
		}
	}

	public class RemoveFileAction extends AbstractAction
	{
		public RemoveFileAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Remove file" );
		}

		public void actionPerformed( ActionEvent e )
		{

			// sensitivInformationTableModel.removeRows(
			// tokenTable.getSelectedRows() );

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

	class FileFormField extends AbstractSwingXFormField<JPanel> implements XFormTextField
	{
		private JTextField textField;
		private final FieldType type;
		private JButton selectDirectoryButton;
		private String projectRoot;

		private boolean updating;
		private String oldValue;
		private String currentDirectory;

		public FileFormField( String tooltip, FieldType type )
		{
			super( new JPanel() );
			this.type = type;

			ButtonBarBuilder builder = new ButtonBarBuilder( getComponent() );
			textField = new JUndoableTextField( 30 );
			textField.setToolTipText( tooltip );
			builder.addGriddedGrowing( textField );
			builder.addRelatedGap();
			selectDirectoryButton = new JButton( new SelectDirectoryAction() );
			builder.addFixed( selectDirectoryButton );

			textField.getDocument().addDocumentListener( new DocumentListenerAdapter()
			{

				@Override
				public void update( Document document )
				{
					String text = textField.getText();

					if( !updating )
						fireValueChanged( text, oldValue );

					oldValue = text;
				}
			} );
		}

		public void setValue( String value )
		{
			updating = true;
			oldValue = null;
			updateValue( value );
			updating = false;
		}

		private void updateValue( String value )
		{
			if( value != null && projectRoot != null && value.startsWith( projectRoot ) )
			{
				if( value.equals( projectRoot ) )
					value = "";
				else if( value.length() > projectRoot.length() + 1 )
					value = value.substring( projectRoot.length() + 1 );
			}

			textField.setText( value );
		}

		public String getValue()
		{
			String text = textField.getText().trim();

			if( projectRoot != null && text.length() > 0 )
			{
				String tempName = projectRoot + File.separatorChar + text;
				if( new File( tempName ).exists() )
				{
					text = tempName;
				}
			}

			return text;
		}

		public void setEnabled( boolean enabled )
		{
			textField.setEnabled( enabled );
			selectDirectoryButton.setEnabled( enabled );
		}

		@Override
		public boolean isEnabled()
		{
			return textField.isEnabled();
		}

		public void setCurrentDirectory( String currentDirectory )
		{
			this.currentDirectory = currentDirectory;
		}

		public class SelectDirectoryAction extends AbstractAction
		{
			private JFileChooser fileChooser;

			public SelectDirectoryAction()
			{
				super( "Browse..." );
			}

			public void actionPerformed( ActionEvent e )
			{
				if( fileChooser == null )
				{
					if( type == FieldType.FILE_OR_FOLDER )
					{
						fileChooser = new JFileChooser();
						fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
					}
					else if( type == FieldType.FOLDER || type == FieldType.PROJECT_FOLDER )
						fileChooser = new JDirectoryChooser();
					else
						fileChooser = new JFileChooser();

				}

				String value = FileFormField.this.getValue();
				if( value.length() > 0 )
				{
					fileChooser.setSelectedFile( new File( value ) );
				}
				else if( currentDirectory != null )
				{
					fileChooser.setCurrentDirectory( new File( currentDirectory ) );
				}
				else if( projectRoot != null )
				{
					fileChooser.setCurrentDirectory( new File( projectRoot ) );
				}

				int returnVal = fileChooser.showOpenDialog( UISupport.getMainFrame() );
				if( returnVal == JFileChooser.APPROVE_OPTION )
				{
					updateValue( fileChooser.getSelectedFile().getAbsolutePath() );
				}
			}
		}

		public void setProperty( String name, Object value )
		{
			super.setProperty( name, value );

			if( name.equals( ProjectSettings.PROJECT_ROOT ) && type == FieldType.PROJECT_FOLDER )
			{
				projectRoot = ( String )value;
			}
			else if( name.equals( CURRENT_DIRECTORY ) )
			{
				currentDirectory = ( String )value;
			}
		}

		public void setWidth( int columns )
		{
			textField.setColumns( columns );
		}

		public String getCurrentDirectory()
		{
			return currentDirectory;
		}
	}

	protected TableCellEditor getDefaultCellEditor()
	{
		return new XPathCellRender();
	}

	private class InternalCellRenderer extends DefaultTableCellRenderer
	{
		public InternalCellRenderer()
		{
			super();

			setHorizontalAlignment( SwingConstants.CENTER );
		}
	}

}
