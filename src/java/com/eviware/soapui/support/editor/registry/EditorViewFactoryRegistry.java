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

package com.eviware.soapui.support.editor.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eviware.soapui.impl.rest.panels.request.views.content.RestRequestContentViewFactory;
import com.eviware.soapui.impl.rest.panels.request.views.html.RestHtmlResponseViewFactory;
import com.eviware.soapui.impl.rest.panels.request.views.json.RestJsonResponseViewFactory;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorViewFactory;

/**
 * Registry of availabel XmlViews
 * 
 * @author ole.matzura
 */

public class EditorViewFactoryRegistry
{
	private static EditorViewFactoryRegistry instance;
	private List<EditorViewFactory> factories = new ArrayList<EditorViewFactory>();

	public EditorViewFactoryRegistry()
	{
		// this should obviously come from a configuration file..
		addFactory( new XmlSourceEditorViewFactory() );
		// addFactory( new RestRequestParamsViewFactory() );
		addFactory( new RestRequestContentViewFactory() );
		addFactory( new RestJsonResponseViewFactory() );
		addFactory( new RestHtmlResponseViewFactory() );
		addFactory( new RawXmlEditorFactory() );
	}

	public void addFactory( EditorViewFactory factory )
	{
		factories.add( factory );
	}

	public void setFactory( String viewId, EditorViewFactory factory )
	{
		for( int c = 0; c < factories.size(); c++ )
		{
			if( factories.get( c ).getViewId().equals( viewId ) )
			{
				factories.set( c, factory );
			}
		}
	}

	public static final EditorViewFactoryRegistry getInstance()
	{
		if( instance == null )
			instance = new EditorViewFactoryRegistry();

		return instance;
	}

	public EditorViewFactory[] getFactories()
	{
		return factories.toArray( new EditorViewFactory[factories.size()] );
	}

	public EditorViewFactory[] getFactoriesOfType( Class<?> type )
	{
		List<EditorViewFactory> result = new ArrayList<EditorViewFactory>();
		for( EditorViewFactory factory : factories )
		{
			if( Arrays.asList( factory.getClass().getInterfaces() ).contains( type ) )
				result.add( factory );
		}

		return result.toArray( new EditorViewFactory[result.size()] );
	}
}
