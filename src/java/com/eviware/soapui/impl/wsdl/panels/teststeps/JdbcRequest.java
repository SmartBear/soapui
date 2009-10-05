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
		return testStep.getQuery();
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
		return testStep.getTimeout();
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

	public class JdbcSubmit implements Submit
	{
		public JdbcSubmit( SubmitContext submitContext, boolean async )
		{
		}

		public void cancel()
		{
		}

		public Exception getError()
		{
			return null;
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
			return null;
		}

		public Status waitUntilFinished()
		{
			return null;
		}
	}
}
