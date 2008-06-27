/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.request;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequest.RequestMethod;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class RestRequestDesktopPanel extends ModelItemDesktopPanel<RestRequest> implements PropertyChangeListener
{
	private RestRequestMessageEditor requestEditor;
	private boolean updatingRequest;
	private JComboBox methodCombo;
	private JComboBox mediaTypeCombo;

	public RestRequestDesktopPanel(RestRequest modelItem)
	{
		super(modelItem);
		
		buildUI();
	}

	private void buildUI()
	{
		add( buildToolbar(), BorderLayout.NORTH );
		add( buildContent(), BorderLayout.CENTER );
	}

	private Component buildToolbar()
	{
		return new JXToolBar();
	}

	private Component buildRequestToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		methodCombo = new JComboBox( new Object[] { RequestMethod.GET, RequestMethod.POST, 
				RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.HEAD});
		
		methodCombo.setSelectedItem(getModelItem().getMethod());
		methodCombo.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e)
			{
				updatingRequest = true;
				getModelItem().setMethod((RequestMethod) methodCombo.getSelectedItem());
				updatingRequest = false;
			}});
		
		toolbar.addLabeledFixed( "Method", methodCombo );
		
		mediaTypeCombo = new JComboBox( new Object[]{ getModelItem().getMediaType()});
		mediaTypeCombo.setEditable(true);
		mediaTypeCombo.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e)
			{
				updatingRequest = true;
				getModelItem().setMediaType((String) mediaTypeCombo.getSelectedItem());
				updatingRequest = false;
			}} );
		
		toolbar.addLabeledFixed( "Media Type", mediaTypeCombo );
		return toolbar;
	}

	private Component buildContent()
	{
		return UISupport.createHorizontalSplit( buildRequestEditor(), buildResponseEditor() );
	}

	private Component buildRequestEditor()
	{
		requestEditor = new RestRequestMessageEditor( getModelItem() );
		
		JPanel panel = new JPanel( new BorderLayout() );
		panel.add( requestEditor, BorderLayout.CENTER );
		panel.add( buildRequestToolbar(), BorderLayout.NORTH );
		
		return panel;
	}

	private Component buildResponseEditor()
	{
		return new JTabbedPane();
	}

	@Override
	public boolean dependsOn(ModelItem modelItem)
	{
		return false;
	}
	
	public boolean onClose(boolean canCancel)
	{
		return true;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if( evt.getPropertyName().equals( "method" ) && !updatingRequest )
		{
			methodCombo.setSelectedItem(evt.getNewValue());
		}
		else if( evt.getPropertyName().equals( "mediaType" ) && !updatingRequest )
		{
			mediaTypeCombo.setSelectedItem( (String)evt.getNewValue() );
		}
	}
}
