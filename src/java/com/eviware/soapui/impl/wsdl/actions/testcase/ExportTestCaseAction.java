/*
 *  soapUI Pro, copyright (C) 2007-2008 eviware software ab 
 */
package com.eviware.soapui.impl.wsdl.actions.testcase;

import java.io.File;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class ExportTestCaseAction extends AbstractSoapUIAction<WsdlTestCase>
{

	public ExportTestCaseAction()
	{
		super("Export", "Exports the test case");
	}

	public void perform(WsdlTestCase testCase, Object param)
	{
		testCase.beforeSave();
		String defaultFileName = System.getProperty("user.home", ".") + File.separator + testCase.getName() + ".xml";
		File file = UISupport.getFileDialogs().saveAs(this, "Select test case file", "xml", "XML", new File(defaultFileName));
		
		if( file == null ) return;
      
      String fileName = file.getAbsolutePath();
      if( fileName == null ) return;
       
      testCase.exportTestCase(file);
	}
}
