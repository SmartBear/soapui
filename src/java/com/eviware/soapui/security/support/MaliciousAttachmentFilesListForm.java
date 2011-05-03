package com.eviware.soapui.security.support;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.model.iface.Attachment;

public class MaliciousAttachmentFilesListForm extends JPanel
{
	private DefaultListModel listModel;
	private JList list;
	private Attachment oldSelection;
	private Attachment currentSelection;

	public MaliciousAttachmentFilesListForm( final MaliciousAttachmentListToTableHolder holder )
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
				currentSelection = ( Attachment )listModel.get( list.getSelectedIndex() );
				holder.refresh( oldSelection, currentSelection );
				oldSelection = currentSelection;
			}
		} );
	}

	public String getFirstItem()
	{
		if( list.getModel().getSize() != 0 )
		{
			list.setSelectedIndex( 0 );
			Attachment attachment = ( Attachment )list.getSelectedValue();
			return ( attachment != null ) ? attachment.getName() : "";
		}
		return "";
	}

	public JList getList()
	{
		return list;
	}

	public Attachment[] getData()
	{
		Attachment[] result = new Attachment[listModel.size()];
		for( int c = 0; c < result.length; c++ )
			result[c] = ( Attachment )listModel.get( c );

		return result;
	}

	public void setData( Attachment[] attachments )
	{
		Attachment[] oldData = getData();

		listModel.clear();
		if( attachments != null )
		{
			for( Attachment att : attachments )
			{
				listModel.addElement( att );
			}
		}

		firePropertyChange( "attachments", oldData, getData() );
	}
}