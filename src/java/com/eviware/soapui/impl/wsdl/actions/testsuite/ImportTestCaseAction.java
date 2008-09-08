package com.eviware.soapui.impl.wsdl.actions.testsuite;

import java.io.File;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class ImportTestCaseAction extends AbstractSoapUIAction<WsdlTestSuite>
{

	public ImportTestCaseAction()
	{
		super("Import Test Case", "Import test case in this test suite");
	}

	public void perform(WsdlTestSuite testSuite, Object param)
	{
		
		File file = UISupport.getFileDialogs().openXML(this, "Choose test case to import");

		if (file == null)
			return;

		String fileName = file.getAbsolutePath();
		if (fileName == null)
			return;

		testSuite.importTestCase(file);
		
	}

}
