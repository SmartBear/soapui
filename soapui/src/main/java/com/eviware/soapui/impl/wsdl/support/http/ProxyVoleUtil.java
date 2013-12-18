/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.support.http;

import com.btr.proxy.search.ProxySearch;

/**
 * @author Joel
 */
public class ProxyVoleUtil
{
	public ProxySearch createAutoProxySearch()
	{
		ProxySearch proxySearch = new ProxySearch();
		proxySearch.addStrategy( ProxySearch.Strategy.JAVA );
		proxySearch.addStrategy( ProxySearch.Strategy.ENV_VAR );
		proxySearch.addStrategy( ProxySearch.Strategy.BROWSER );
		proxySearch.addStrategy( ProxySearch.Strategy.OS_DEFAULT );
		return proxySearch;
	}
}
