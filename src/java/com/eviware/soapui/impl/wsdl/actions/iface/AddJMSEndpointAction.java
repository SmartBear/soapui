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
package com.eviware.soapui.impl.wsdl.actions.iface;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesContext;
import hermes.JAXBHermesLoader;
import hermes.config.impl.DestinationConfigImpl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.editor.inspectors.jms.JMSUtil;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;

public class AddJMSEndpointAction extends AbstractSoapUIAction<WsdlInterface>
{
	public static final String SOAPUI_ACTION_ID = "AddJMSEndpointAction";
	private static final String SESSION = "Session";
	private static final String HERMES_CONFIG = "Hermes Config";
	private static final String SEND = "Send Queue/Topic";
	private static final String RECEIVE = "Receive Queue";
	private XForm mainForm;
	List<Destination> destinationNameList;
	List<Destination> queueNameList;

	public AddJMSEndpointAction()
	{
		super("Add JMS endpoint", "Wizard for creating JMS endpoint");
	}

	public void perform(WsdlInterface iface, Object param)
	{

		XFormDialog dialog = buildDialog(iface);

		initValues(iface);

		if (dialog.show())
		{
			String session = dialog.getValue(SESSION);
			int i = dialog.getValueIndex(SEND);
			if(i==-1){
				UISupport.showErrorMessage("Not supported endpoint");
				return;
			}
			String send = destinationNameList.get(i).getDestinationName();
			int j = dialog.getValueIndex(RECEIVE);
			if(j==-1){
				UISupport.showErrorMessage("Not supported endpoint");
				return;
			}
			String receive = queueNameList.get(j).getDestinationName();
			if ("-".equals(send) && "".equals(receive))
			{
				UISupport.showErrorMessage("Endpoint with blank send and receive field is discarded");
				return;
			}
			if (destinationNameList.get(i).getDomain().equals(Domain.TOPIC)
					&& queueNameList.get(j).getDomain().equals(Domain.QUEUE))
			{
				UISupport.showErrorMessage("Not supported endpoint");
				return;
			}

			iface.addEndpoint(createEndpointString(session, send, receive));
		}
	}

	private String createEndpointString(String session, String send, String receive)
	{
		StringBuilder sb = new StringBuilder(JMSUtil.JMS_ENDPIONT_PREFIX);
		sb.append(session + "/");
		sb.append(send);
		if (!"".equals(receive))
			sb.append("/" + receive);
		return sb.toString();
	}

	private String[] getSessionOptions(WsdlInterface iface, String hermesConfigPath)
	{

		List<Hermes> hermesList = new ArrayList<Hermes>();
		try
		{
			Context ctx = getHermesContext(iface, hermesConfigPath);
			JAXBHermesLoader loader = (JAXBHermesLoader) ctx.lookup(HermesContext.LOADER);
			hermesList = loader.load();
		}
		catch (Exception e)
		{
			SoapUI.log.info("no hermes context");
		}
		List<String> hermesSessionList = new ArrayList<String>();
		for (Hermes h : hermesList)
		{
			if (!h.getSessionConfig().getId().equals("<new>"))
				hermesSessionList.add(h.getSessionConfig().getId());
		}
		return hermesSessionList.toArray(new String[hermesSessionList.size()]);
	}

	

	private void initValues(WsdlInterface iface)
	{
		String hermesConfigPath = PropertyExpander.expandProperties(iface, iface.getProject().getHermesConfig());
		mainForm.getComponent(HERMES_CONFIG).setValue(hermesConfigPath);

		String[] sessionOptions = getSessionOptions(iface, hermesConfigPath);

		mainForm.setOptions(SESSION, sessionOptions);
		Context ctx = null;
		try
		{
			ctx = getHermesContext(iface, hermesConfigPath);
		}
		catch (Exception e)
		{
			SoapUI.log.info("no hermes context");
		}
		Hermes hermes = null;
		try
		{
			if (sessionOptions != null && sessionOptions.length > 0)
			{
				hermes = (Hermes) ctx.lookup(sessionOptions[0]);
			}

			if (hermes != null)
			{
				updateDestinations(hermes);
			}
		}
		catch (NamingException e)
		{
			SoapUI.logError(e);
		}

	}

	private void updateDestinations(Hermes hermes)
	{
		destinationNameList = new ArrayList<Destination>();
		queueNameList = new ArrayList<Destination>();
		destinationNameList.add(new Destination("-", Domain.UNKNOWN));
		queueNameList.add(new Destination("", Domain.UNKNOWN));
		extractDestinations(hermes, destinationNameList, queueNameList);
		mainForm.setOptions(SEND, destinationNameList.toArray());
		mainForm.setOptions(RECEIVE, queueNameList.toArray());
	}

	private Context getHermesContext(WsdlInterface iface, String hermesConfigPath) throws MalformedURLException,
			NamingException, IOException
	{
		WsdlProject project = iface.getProject();

		Context ctx = HermesUtils.hermesContext(project, hermesConfigPath);
		return ctx;

	}

	protected XFormDialog buildDialog(final WsdlInterface iface)
	{
		if (iface == null)
			return null;

		XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Add JMS endpoint");

		mainForm = builder.createForm("Basic");
		mainForm.addTextField(HERMES_CONFIG, "choose folder where hermes-config.xml is", XForm.FieldType.FOLDER)
				.addFormFieldListener(new XFormFieldListener()
				{
					public void valueChanged(XFormField sourceField, String newValue, String oldValue)
					{
						if (!"".equals(newValue))
						{
							Hermes hermes = null;
							try
							{
								Context ctx = HermesUtils.hermesContext(iface.getProject(), newValue);
								iface.getProject().setHermesConfig(newValue);
								String[] sessions = getSessionOptions(iface, newValue);
								mainForm.setOptions(SESSION, sessions);
								if (sessions != null && sessions.length > 0)
								{
									hermes = (Hermes) ctx.lookup(sessions[0]);
								}
							}
							catch (Exception e)
							{
								SoapUI.logError(e);
							}
							if (hermes != null)
							{
								updateDestinations(hermes);
							}
							else
							{
								mainForm.setOptions(SESSION, new String[] {});
								mainForm.setOptions(SEND, new String[] {});
								mainForm.setOptions(RECEIVE, new String[] {});
							}
						}
					}
				});
		mainForm.addComboBox(SESSION, new String[] {}, "Session name from HermesJMS").addFormFieldListener(
				new XFormFieldListener()
				{

					public void valueChanged(XFormField sourceField, String newValue, String oldValue)
					{
						String hermesConfigPath = mainForm.getComponent(HERMES_CONFIG).getValue();

						Hermes hermes = null;
						try
						{
							Context ctx = getHermesContext(iface, hermesConfigPath);
							hermes = (Hermes) ctx.lookup(newValue);
						}
						catch (Exception e)
						{
							SoapUI.logError(e);
						}
						if (hermes != null)
						{
							updateDestinations(hermes);
						}
						else
						{
							mainForm.setOptions(SEND, new String[] {});
							mainForm.setOptions(RECEIVE, new String[] {});
						}
					}

				});
		mainForm.addComboBox(SEND, new String[] {}, "Queue/Topic for sending/publishing");
		mainForm.addComboBox(RECEIVE, new String[] {}, "Queue for receiving");

		return builder
				.buildDialog(builder.buildOkCancelActions(), "create JMS endpoint by selecting proper values", null);
	}

	private void extractDestinations(Hermes hermes, List<Destination> destinationList, List<Destination> queueList)
	{
		Iterator<?> hermesDestionations = hermes.getDestinations();
		while (hermesDestionations.hasNext())
		{
			DestinationConfigImpl dest = (DestinationConfigImpl) hermesDestionations.next();
			Destination temp = new Destination(dest.getName(), Domain.getDomain(dest.getDomain()));
			destinationList.add(temp);

			if (Domain.QUEUE.getId() == dest.getDomain())
			{
				queueList.add(temp);
			}
		}
	}

	private class Destination
	{
		public Destination(String destinationName, Domain domain)
		{
			this.domain = domain;
			if (destinationName.equals("-") || destinationName.equals(""))
			{
				this.destinationName = destinationName;
			}
			else
			{
				if (domain.equals(Domain.QUEUE))
				{
					this.destinationName = "queue_" + destinationName;
				}
				else
				{
					this.destinationName = "topic_" + destinationName;
				}
			}

		}

		private String destinationName;
		private Domain domain;

		public String getDestinationName()
		{
			return destinationName;
		}

		@SuppressWarnings("unused")
		public Domain getDomain()
		{
			return domain;
		}

		public String toString()
		{
			return this.getDestinationName().replace("queue_", "").replace("topic_", "");
		}
	}
}
