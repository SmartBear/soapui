package com.eviware.soapui.security.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.config.MaliciousAttachmentConfig;
import com.eviware.soapui.security.tools.AttachmentElement;
import com.eviware.soapui.security.ui.MaliciousAttachmentMutationsPanel.MutationTables;
import com.eviware.x.impl.swing.JFormDialog;

public class MaliciousAttachmentListToTableHolder
{

	MaliciousAttachmentFilesListForm filesList;
	MaliciousAttachmentTableModel generateTableModel;
	MaliciousAttachmentTableModel replaceTableModel;
	JFormDialog tablesDialog;

	Map<String, List<MaliciousAttachmentConfig>> generateMap = new HashMap<String, List<MaliciousAttachmentConfig>>();
	Map<String, List<MaliciousAttachmentConfig>> replaceMap = new HashMap<String, List<MaliciousAttachmentConfig>>();
	Map<String, Boolean> removeMap = new HashMap<String, Boolean>();

	public JFormDialog getTablesDialog()
	{
		return tablesDialog;
	}

	public void setTablesDialog( JFormDialog tablesDialog )
	{
		this.tablesDialog = tablesDialog;
	}

	public MaliciousAttachmentFilesListForm getFilesList()
	{
		return filesList;
	}

	public void setFilesList( MaliciousAttachmentFilesListForm filesList )
	{
		this.filesList = filesList;
	}

	public MaliciousAttachmentTableModel getGenerateTableModel()
	{
		return generateTableModel;
	}

	public void setGenerateTableModel( MaliciousAttachmentTableModel generateTableModel )
	{
		this.generateTableModel = generateTableModel;
	}

	public MaliciousAttachmentTableModel getReplaceTableModel()
	{
		return replaceTableModel;
	}

	public void setReplaceTableModel( MaliciousAttachmentTableModel replaceTableModel )
	{
		this.replaceTableModel = replaceTableModel;
	}

	public void refresh()
	{
		if( filesList != null )
		{
			AttachmentElement item = filesList.getFirstItem();
			// String label = ( item != null ) ? item.getAttachment().getName() :
			// "";
			// tablesDialog.getFormField( MutationTables.LABEL ).setValue( label );
			if( item != null )
			{
				load( item );
			}
		}
	}

	public void refresh( AttachmentElement oldItem, AttachmentElement newItem )
	{
		if( oldItem != null )
		{
			save( oldItem );

			if( newItem != null )
			{
				load( newItem );
			}
		}
	}

	public void addResultToGenerateTable( MaliciousAttachmentConfig config )
	{
		generateTableModel.addResult( config );
	}

	public void addResultToReplaceTable( MaliciousAttachmentConfig config )
	{
		replaceTableModel.addResult( config );
	}

	private void save( AttachmentElement item )
	{
		List<MaliciousAttachmentConfig> generateList = new ArrayList<MaliciousAttachmentConfig>();
		List<MaliciousAttachmentConfig> replaceList = new ArrayList<MaliciousAttachmentConfig>();

		for( int i = 0; i < generateTableModel.getRowCount(); i++ )
		{
			generateList.add( generateTableModel.getRowValue( i ) );
		}

		for( int i = 0; i < replaceTableModel.getRowCount(); i++ )
		{
			replaceList.add( replaceTableModel.getRowValue( i ) );
		}

		Boolean remove = tablesDialog.getBooleanValue( MutationTables.REMOVE_FILE );

		generateMap.put( item.getId(), generateList );
		replaceMap.put( item.getId(), replaceList );
		removeMap.put( item.getId(), remove );
	}

	private void load( AttachmentElement item )
	{
		List<MaliciousAttachmentConfig> generateList = generateMap.get( item.getId() );
		List<MaliciousAttachmentConfig> replaceList = replaceMap.get( item.getId() );
		Boolean remove = removeMap.get( item.getId() );

		// tablesDialog.setValue( MutationTables.LABEL,
		// item.getAttachment().getName() );

		generateTableModel.clear();
		replaceTableModel.clear();
		tablesDialog.setBooleanValue( MutationTables.REMOVE_FILE, new Boolean( false ) );

		if( remove != null )
		{
			tablesDialog.setBooleanValue( MutationTables.REMOVE_FILE, remove );
		}

		if( generateList != null )
		{
			for( MaliciousAttachmentConfig element : generateList )
			{
				generateTableModel.addResult( element );
			}
		}

		if( replaceList != null )
		{
			for( MaliciousAttachmentConfig element : replaceList )
			{
				replaceTableModel.addResult( element );
			}
		}
	}
}
