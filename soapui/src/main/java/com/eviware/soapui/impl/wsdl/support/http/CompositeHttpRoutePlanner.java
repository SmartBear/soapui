package com.eviware.soapui.impl.wsdl.support.http;

import com.btr.proxy.search.ProxySearch;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.protocol.HttpContext;

import java.net.ProxySelector;

/**
 * @author Anders Jaensson
 */
public class CompositeHttpRoutePlanner implements HttpRoutePlanner
{

	private ProxySearch proxySearch;
	private HttpRoutePlanner defaultHttpRoutePlanner;
	private boolean autoProxyEnabled;
	private SchemeRegistry registry;
	private CachedProxySelector cachedProxySelector;

	public CompositeHttpRoutePlanner( SchemeRegistry registry )
	{
		this.registry = registry;
		this.defaultHttpRoutePlanner = new DefaultHttpRoutePlanner( registry );
		this.proxySearch = new ProxyVoleUtil().createAutoProxySearch();
	}

	public void setAutoProxyEnabled( boolean autoProxyEnabled )
	{
		this.autoProxyEnabled = autoProxyEnabled;
		cachedProxySelector = null;
	}

	@Override
	public HttpRoute determineRoute( HttpHost target, HttpRequest request, HttpContext context ) throws HttpException
	{
		Object manualProxy = request.getParams().getParameter( ConnRoutePNames.DEFAULT_PROXY );
		if( manualProxy == null && autoProxyEnabled )
		{
			CachedProxySelector proxySelector = getProxySelector();
			if( proxySelector != null )
			{
				return new ProxySelectorRoutePlanner( registry, proxySelector.getProxySelector() ).determineRoute( target, request, context );
			}
		}
		return defaultHttpRoutePlanner.determineRoute( target, request, context );
	}

	private CachedProxySelector getProxySelector()
	{
		if( cachedProxySelector == null )
		{
			ProxySelector proxySelector = proxySearch.getProxySelector();
			cachedProxySelector = new CachedProxySelector( proxySelector == null ? null
					: ProxyUtils.filterHttpHttpsProxy( proxySearch.getProxySelector() ) );
		}
		return cachedProxySelector;
	}

	private static class CachedProxySelector
	{
		private ProxySelector proxySelector;

		private CachedProxySelector( ProxySelector proxySelector )
		{
			this.proxySelector = proxySelector;
		}

		private ProxySelector getProxySelector()
		{
			return proxySelector;
		}
	}
}
