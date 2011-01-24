/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.check;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.SensitiveInformationCheckable;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.types.StringToObjectMap;

public class MaliciousAttachmentSecurityCheck extends AbstractSecurityCheck
		implements SensitiveInformationCheckable {

	public static final String TYPE = "MaliciousAttachmentSecurityCheck";
	private static final int MINIMUM_STRING_DISTANCE = 50;

	public MaliciousAttachmentSecurityCheck(SecurityCheckConfig config,
			ModelItem parent, String icon) {
		super(config, parent, icon);
		if (config == null) {
			config = SecurityCheckConfig.Factory.newInstance();
			MaliciousAttachmentSecurityCheckConfig mascc = MaliciousAttachmentSecurityCheckConfig.Factory
					.newInstance();
			config.setConfig(mascc);
		}
		if (config.getConfig() == null) {
			MaliciousAttachmentSecurityCheckConfig mascc = MaliciousAttachmentSecurityCheckConfig.Factory
					.newInstance();
			config.setConfig(mascc);
		}
	}

	@Override
	public SecurityCheckRequestResult analyze(TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityCheckResult) {
		//TODO
		return null;

	}
	
	
	@Override
	protected SecurityCheckRequestResult execute(TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityChekResult) {
		WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner(
				(WsdlTestCase) testStep.getTestCase(), new StringToObjectMap());

		String originalResponse = getOriginalResult(testCaseRunner, testStep)
				.getResponse().getContentAsXml();

		// First, lets see what happens when we just attach a plain text file
		File textFile;
		try {
			textFile = File.createTempFile("test", ".txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(textFile));
			writer
					.write("This is just a text file, nothing to see here, just a harmless text file");
			writer.flush();
			Attachment attach = addAttachement(testStep, textFile, "text/plain");
			runCheck(testStep, context, securityTestLog, testCaseRunner, originalResponse, "Possible Malicious Attachment Vulnerability Detected");			
			getRequest(testStep).removeAttachment(attach);

			// Try with setting the wrong content type
			attach = addAttachement(testStep, textFile, "multipart/mixed");		
			runCheck(testStep, context, securityTestLog, testCaseRunner, originalResponse, "Possible Malicious Attachment Vulnerability Detected");
			getRequest(testStep).removeAttachment(attach);

		} catch (IOException e) {
			SoapUI.logError(e);
			return null;
		}
		//TODO 
		return null;

	}

	@Override
	public boolean acceptsTestStep(TestStep testStep) {
		return true;
	}

	@Override
	public void checkForSensitiveInformationExposure(TestStep testStep,
			SecurityTestRunContext context, SecurityTestLogModel securityTestLog) {
		InformationExposureCheck iec = new InformationExposureCheck(config,
				null, null);
		iec.analyze(testStep, context, securityTestLog, null);

	}


	@Override
	public String getType() {
		return TYPE;
	}



	private Attachment addAttachement(TestStep testStep, File file,
			String contentType) throws IOException {
		AbstractHttpRequest<?> request = getRequest(testStep);

		Attachment attach = request.attachFile(file, false);
		attach.setContentType(contentType);

		return attach;
	}

	@Override
	public SecurityCheckConfigPanel getComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean configure()
	{
		return false;
	}

}
