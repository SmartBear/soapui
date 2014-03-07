package com.eviware.soapui.impl.wsdl.monitor;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.apache.http.HttpRequest;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Prakash
 */
public class SoapMonitorListenerCallBack
{
	private SoapUIListenerSupport<MonitorListener> listeners = new SoapUIListenerSupport<MonitorListener>(
			MonitorListener.class );

	public void fireAddMessageExchange( WsdlMonitorMessageExchange messageExchange )
	{
		fireOnMessageExchange( messageExchange );
	}

	public void fireOnMessageExchange( WsdlMonitorMessageExchange messageExchange )
	{
		for( MonitorListener listener : listeners.get() )
		{
			try
			{
				listener.onMessageExchange( messageExchange );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
	}

	public void fireOnRequest( WsdlProject project, ServletRequest request, ServletResponse response )
	{
		for( MonitorListener listener : listeners.get() )
		{
			try
			{
				listener.onRequest( project, request, response );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
	}

	public void fireBeforeProxy( WsdlProject project, ServletRequest request, ServletResponse response, HttpRequest httpRequest )
	{
		for( MonitorListener listener : listeners.get() )
		{
			try
			{
				listener.beforeProxy( project, request, response, httpRequest );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
	}

	public void fireAfterProxy( WsdlProject project, ServletRequest request, ServletResponse response, HttpRequest httpRequest,
										 WsdlMonitorMessageExchange capturedData )
	{
		for( MonitorListener listener : listeners.get() )
		{
			try
			{
				listener.afterProxy( project, request, response, httpRequest, capturedData );
			}
			catch( Throwable t )
			{
				SoapUI.logError( t );
			}
		}
	}

	public void addSoapMonitorListener( MonitorListener listener )
	{
		listeners.add( listener );
	}

	public void removeSoapMonitorListener( MonitorListener listener )
	{
		listeners.remove( listener );
	}

	public static class SoapUIListenerSupport<T>
	{
		private Set<T> listeners = new HashSet<T>();
		@SuppressWarnings( "unused" )
		private final Class<T> listenerClass;

		public SoapUIListenerSupport( Class<T> listenerClass )
		{
			this.listenerClass = listenerClass;
			listeners.addAll( SoapUI.getListenerRegistry().getListeners( listenerClass ) );
		}

		public void add( T listener )
		{
			listeners.add( listener );
		}

		public void remove( T listener )
		{
			listeners.remove( listener );
		}

		public Collection<T> get()
		{
			return listeners;
		}
	}
}
