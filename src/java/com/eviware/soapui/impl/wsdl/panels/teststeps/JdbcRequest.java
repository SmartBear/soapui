package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

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
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.support.ModelSupport;

public class JdbcRequest extends AbstractModelItem implements Request
{
	

	private final JdbcRequestTestStep testStep;
	private Set<SubmitListener> submitListeners = new HashSet<SubmitListener>();

	public JdbcRequest( JdbcRequestTestStep testStep )
	{
		this.testStep = testStep;
	}
	
	@Override
	public void addSubmitListener( SubmitListener listener )
	{
		submitListeners.add(listener);
	}

	@Override
	public boolean dependsOn( ModelItem modelItem )
	{
		return ModelSupport.dependsOn( testStep, modelItem );
	}

	@Override
	public Attachment[] getAttachments()
	{
		return null;
	}

	@Override
	public String getEncoding()
	{
		return null;
	}

	@Override
	public String getEndpoint()
	{
		return null;
	}

	@Override
	public Operation getOperation()
	{
		return null;
	}

	@Override
	public String getRequestContent()
	{
		return testStep.getQuery();
	}

	@Override
	public MessagePart[] getRequestParts()
	{
		return null;
	}

	@Override
	public MessagePart[] getResponseParts()
	{
		return null;
	}

	@Override
	public String getTimeout()
	{
		return testStep.getTimeout();
	}

	@Override
	public void removeSubmitListener( SubmitListener listener )
	{
		submitListeners.remove( listener );
	}

	@Override
	public void setEncoding( String string )
	{
	}

	@Override
	public void setEndpoint( String string )
	{
	}

	@Override
	public Submit submit( SubmitContext submitContext, boolean async ) throws SubmitException
	{
		return new JdbcSubmit( submitContext, async );
	}
	
	public List<? extends ModelItem> getChildren()
	{
		return null;
	}

	@Override
	public String getDescription()
	{
		return testStep.getDescription();
	}

	@Override
	public ImageIcon getIcon()
	{
		return testStep.getIcon();
	}

	@Override
	public String getId()
	{
		return testStep.getId();
	}

	@Override
	public String getName()
	{
		return testStep.getName();
	}

	@Override
	public ModelItem getParent()
	{
		return testStep.getParent();
	}

	@Override
	public Settings getSettings()
	{
		return testStep.getSettings();
	}
	
	public class JdbcSubmit implements Submit
	{
		public JdbcSubmit( SubmitContext submitContext, boolean async )
		{
		}

		@Override
		public void cancel()
		{
		}

		@Override
		public Exception getError()
		{
			return null;
		}

		@Override
		public Request getRequest()
		{
			return JdbcRequest.this;
		}

		@Override
		public Response getResponse()
		{
			return null;
		}

		@Override
		public Status getStatus()
		{
			return null;
		}

		@Override
		public Status waitUntilFinished()
		{
			return null;
		}
	}
}
