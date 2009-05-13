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
package com.eviware.x.form;

/**
 * 
 * @author lars
 */
public abstract class WizardPage
{
	private String name;
	private String description;

	public WizardPage( String name, String description )
	{
		this.name = name;
		this.description = description;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public boolean canGoBack()
	{
		return false;
	}

	/**
	 * Initialize the page. Note that this can be called multiple times if going
	 * Back and Next.
	 * 
	 * @return true if the page was initialized ok, false to end the wizard.
	 * @throws Exception
	 */
	public abstract boolean init() throws Exception;

	/**
	 * 
	 * @return true if the page finished ok, false to end the wizard.
	 * @throws Exception
	 */
	public abstract boolean run() throws Exception;
}
