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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import javax.swing.Action;

import com.eviware.soapui.model.settings.Settings;

public abstract class AbstractGroovyEditorModel implements GroovyEditorModel
{
	private final String[] keywords;
	private final Settings settings;
	private Action runAction;
	private final String name;

	public AbstractGroovyEditorModel( String[] keywords, Settings settings, String name )
	{
		this.keywords = keywords;
		this.settings = settings;
		this.name = name;

		runAction = createRunAction();
	}

	public String[] getKeywords()
	{
		return keywords;
	}

	public Action getRunAction()
	{
		return runAction;
	}

	public Action createRunAction()
	{
		return null;
	}

	public abstract String getScript();

	public Settings getSettings()
	{
		return settings;
	}

	public abstract void setScript( String text );

	public String getScriptName()
	{
		return name;
	}
}
