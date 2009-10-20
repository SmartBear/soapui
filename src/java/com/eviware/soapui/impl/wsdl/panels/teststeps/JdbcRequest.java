package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.JdbcRequestTestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;

public class JdbcRequest extends AbstractModelItem implements Request
{

	private final JdbcRequestTestStep testStep;
	private Set<SubmitListener> submitListeners = new HashSet<SubmitListener>();
	private final static Logger logger = Logger.getLogger( JdbcRequest.class );

	public JdbcRequest( JdbcRequestTestStep testStep )
	{
		this.testStep = testStep;
	}

	public void addSubmitListener( SubmitListener listener )
	{
		submitListeners.add( listener );
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return ModelSupport.dependsOn( testStep, modelItem );
	}

	public Attachment[] getAttachments()
	{
		return null;
	}

	public String getEncoding()
	{
		return null;
	}

	public String getEndpoint()
	{
		return null;
	}

	public Operation getOperation()
	{
		return null;
	}

	public String getRequestContent()
	{
		return ((JdbcRequestTestStepConfig)testStep.getConfig()).getQuery();
	}

	public MessagePart[] getRequestParts()
	{
		return null;
	}

	public MessagePart[] getResponseParts()
	{
		return null;
	}

	public String getTimeout()
	{
		return null;
	}

	public void removeSubmitListener( SubmitListener listener )
	{
		submitListeners.remove( listener );
	}

	public void setEncoding( String string )
	{
	}

	public void setEndpoint( String string )
	{
	}

	public Submit submit( SubmitContext submitContext, boolean async ) throws SubmitException
	{
		return new JdbcSubmit( submitContext, async );
	}

	public List<? extends ModelItem> getChildren()
	{
		return null;
	}

	public String getDescription()
	{
		return testStep.getDescription();
	}

	public ImageIcon getIcon()
	{
		return testStep.getIcon();
	}

	public String getId()
	{
		return testStep.getId();
	}

	public String getName()
	{
		return testStep.getName();
	}

	public ModelItem getParent()
	{
		return testStep.getParent();
	}

	public Settings getSettings()
	{
		return testStep.getSettings();
	}

	public class JdbcSubmit implements Submit, Runnable
	{
		private volatile Future<?> future;
		private SubmitContext submitContext;
		private Status status;
		private SubmitListener[] listeners;
		private Exception error;
		public JdbcSubmit( SubmitContext submitContext, boolean async )
		{
			this.submitContext = submitContext;

//			List<SubmitListener> regListeners = SoapUI.getListenerRegistry().getListeners( SubmitListener.class );
//
//			this.listeners = new SubmitListener[listeners.length + regListeners.size()];
//			for( int c = 0; c < listeners.length; c++ )
//				this.listeners[c] = listeners[c];
//
//			for( int c = 0; c < regListeners.size(); c++ )
//				this.listeners[listeners.length + c] = regListeners.get( c );

			error = null;
			status = Status.INITIALIZED;
			if( async && future != null )
				throw new RuntimeException( "Submit already running" );

			if( async )
				future = SoapUI.getThreadPool().submit( this );
			else
				run();
		}

		public void cancel()
		{
			if( status == Status.CANCELED )
				return;

			logger.info( "Canceling request.." );
			if( status == Status.RUNNING )
			{
				testStep.cancelQuery();
			}

			status = Status.CANCELED;

			for( int i = 0; i < listeners.length; i++ )
			{
				try
				{
					listeners[i].afterSubmit( this, submitContext );
				}
				catch( Throwable e )
				{
					SoapUI.logError( e );
				}
			}
		}

		public Exception getError()
		{
			return error;
		}

		public Request getRequest()
		{
			return JdbcRequest.this;
		}

		public Response getResponse()
		{
			return null;
		}

		public Status getStatus()
		{
			return status;
		}

		public Status waitUntilFinished()
		{
			if( future != null )
			{
				if( !future.isDone() )
				{
					try
					{
						future.get();
					}
					catch( Exception e )
					{
						SoapUI.logError( e );
					}
				}
			}
			else
				throw new RuntimeException( "cannot wait on null future" );

			return getStatus();
		}

		public void run()
		{
			try
			{
				testStep.runQuery();
			}
			catch (Exception e)
			{
				UISupport.showErrorMessage("There's been an error in executing query " + e.toString());
				error = e;
			}
			finally
			{
				if( status != Status.CANCELED )
				{
					for( int i = 0; i < listeners.length; i++ )
					{
						try
						{
							listeners[i].afterSubmit( this, submitContext );
						}
						catch( Throwable e )
						{
							SoapUI.logError( e );
						}
					}
				}
			}
		}
	}
}
