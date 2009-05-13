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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import com.eviware.soapui.model.ModelItem;

/**
 * Runner for Tool-related utilities
 * 
 * @author ole.matzura
 */

public interface ToolRunner extends Runnable
{
	public void setContext( RunnerContext context );

	public boolean isRunning();

	public boolean canCancel();

	public boolean showLog();

	public void cancel();

	public String getName();

	public ModelItem getModelItem();

	public String getDescription();
}