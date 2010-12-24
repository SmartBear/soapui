package com.eviware.soapui.security.check;

import javax.swing.JComponent;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.log.SecurityTestLogModel;

public class XmlBombSecurityCheck extends AbstractSecurityCheck {
	
	public static final String TYPE = "XmlBombSecurityCheck";

	public XmlBombSecurityCheck(SecurityCheckConfig config, ModelItem parent,
			String icon) {
		super(config, parent, icon);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void analyze(TestStep testStep, WsdlTestRunContext context,
			SecurityTestLogModel securityTestLog) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void execute(TestStep testStep, WsdlTestRunContext context,
			SecurityTestLogModel securityTestLog) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean acceptsTestStep(TestStep testStep) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JComponent getComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
