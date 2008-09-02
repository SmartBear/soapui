/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * Factory for WsdlTestRequestSteps
 *
 * @author Ole.Matzura
 */

public class HttpRequestStepFactory extends WsdlTestStepFactory
{
   public static final String HTTPREQUEST_TYPE = "httprequest";
   public static final String STEP_NAME = "Name";
   public static final String ENDPOINT = "Endpoint";
   public static final String METHOD = "Method";
   private XFormDialog dialog;
   private StringToStringMap dialogValues = new StringToStringMap();

   public HttpRequestStepFactory()
   {
      super(HTTPREQUEST_TYPE, "HTTP Test Request", "Submits a HTTP Request and validates its response", "/request.gif");
   }

   public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest)
   {
      return new RestTestRequestStep(testCase, config, forLoadTest);
   }

   public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name)
   {
      if (dialog == null)
         buildDialog();

      dialogValues.put(STEP_NAME, name);
      dialogValues = dialog.show(dialogValues);
      if (dialog.getReturnValue() != XFormDialog.OK_OPTION)
         return null;

      RestRequestStepConfig testStepConfig = RestRequestStepConfig.Factory.newInstance();
      RestMethodConfig requestConfig = testStepConfig.addNewRestRequest();
      requestConfig.setEndpoint(dialog.getValue(ENDPOINT));
      requestConfig.setMethod(dialog.getValue(METHOD));

      TestStepConfig testStep = TestStepConfig.Factory.newInstance();
      testStep.setType(HTTPREQUEST_TYPE);
      testStep.setConfig(testStepConfig);
      testStep.setName(name);

      return testStep;
   }

   public boolean canCreate()
   {
      return true;
   }

   private void buildDialog()
   {
      XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Add HTTP Request to TestCase");
      XForm mainForm = builder.createForm("Basic");

      mainForm.addTextField(STEP_NAME, "Name of TestStep", XForm.FieldType.URL).setWidth(30);
      mainForm.addTextField(ENDPOINT, "Endpoint", XForm.FieldType.URL).setWidth(30);
      mainForm.addTextField(METHOD, "Endpoint", XForm.FieldType.URL).setWidth(30);

      dialog = builder.buildDialog(builder.buildOkCancelActions(),
              "Specify options for adding a new HTTP Request to a TestCase", UISupport.OPTIONS_ICON);
   }
}