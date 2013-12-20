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
	private ProxySelector cachedProxySelector;

	public CompositeHttpRoutePlanner( SchemeRegistry registry )
	{
		this.registry = registry;
		this.defaultHttpRoutePlanner = new DefaultHttpRoutePlanner( registry );
		this.proxySearch = createProxySearch();
	}

	private ProxySearch createProxySearch()
	{
		ProxySearch proxySearch = new ProxySearch();
		proxySearch.addStrategy( ProxySearch.Strategy.JAVA );
		proxySearch.addStrategy( ProxySearch.Strategy.ENV_VAR );
		proxySearch.addStrategy( ProxySearch.Strategy.BROWSER );
		proxySearch.addStrategy( ProxySearch.Strategy.OS_DEFAULT );
		return proxySearch;
	}


	public void setAutoProxyEnabled( boolean autoProxyEnabled )
	{
		this.autoProxyEnabled = autoProxyEnabled;
		cachedProxySelector = null;
	}

	@Override
	public HttpRoute determineRoute( HttpHost target, HttpRequest request, HttpContext context ) throws HttpException
	{
		Object proxy = request.getParams().getParameter( ConnRoutePNames.DEFAULT_PROXY );
		if( proxy == null && autoProxyEnabled )
		{
			ProxySelector proxySelector = getProxySelector();
			if( proxySelector != null )
			{
				return new ProxySelectorRoutePlanner( registry, proxySelector ).determineRoute( target, request, context );
			}
		}
		return defaultHttpRoutePlanner.determineRoute( target, request, context );
	}

	private ProxySelector getProxySelector()
	{
		if( cachedProxySelector == null )
		{
			cachedProxySelector = proxySearch.getProxySelector();
		}
		return cachedProxySelector;
	}
}
