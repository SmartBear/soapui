package com.eviware.soapui.support;

import com.eviware.soapui.impl.support.http.MediaType;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class MediaTypeComboBox extends JComboBox
{
	public MediaTypeComboBox( final MediaType model )
	{
		super( getMediaTypes() );

		setEditable( true );
		if( model.getMediaType() != null )
			setSelectedItem( model.getMediaType() );

		addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				model.setMediaType( String.valueOf( getSelectedItem() ) );
			}
		} );

	}

	public static Object[] getMediaTypes()
	{
		return new String[] { "application/json", "application/xml", "text/xml", "multipart/form-data", "multipart/mixed" };
	}

}
