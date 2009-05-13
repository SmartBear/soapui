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

package com.eviware.soapui.impl.wsdl.actions.support;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Shows an online help page
 * 
 * @author Ole.Matzura
 */

public class ShowOnlineSoapUIHelp extends AbstractSoapUIAction<ModelItem> implements HelpActionMarker
{
	public static final String SOAPUI_ACTION_ID = "ShowOnlineSoapUIHelp";
	private String url;

	public ShowOnlineSoapUIHelp()
	{
		super( "Online Help", "Show Online Help" );
	}

	public ShowOnlineSoapUIHelp( String name, String url )
	{
		super( name, url );
		this.url = url;
	}

	public void perform( ModelItem target, Object param )
	{
		if( param == null && url == null )
		{
			UISupport.showErrorMessage( "Missing help URL" );
			return;
		}

		String url = param == null ? this.url : param.toString();
		if( !url.startsWith( "http://" ) )
			url = HelpUrls.HELP_URL_ROOT + url;

		Tools.openURL( url );
	}
}
