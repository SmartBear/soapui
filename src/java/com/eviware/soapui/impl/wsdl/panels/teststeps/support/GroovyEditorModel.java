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

/**
 * Model used by custom GrooyEditor
 * 
 * @author ole.matzura
 */

public interface GroovyEditorModel
{
	public String[] getKeywords();

	public String getScript();

	public void setScript( String text );

	public Action getRunAction();

	public Settings getSettings();

	public String getScriptName();
}
