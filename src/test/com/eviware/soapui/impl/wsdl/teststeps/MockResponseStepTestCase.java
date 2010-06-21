/*
 * soapUI Pro, copyright (C) 2007-2008 eviware software ab
 */

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlMockResponseStepFactory;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.support.TestCaseWithJetty;
import com.eviware.soapui.support.types.StringToObjectMap;

public class MockResponseStepTestCase extends TestCaseWithJetty
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   public void testDummy() throws Exception
   {
      assert true;
   }

   public void testMockResponseStep() throws Exception
   {
      // create empty project
      WsdlProject project = new WsdlProject();

      // import wsdl
      WsdlInterface iface = WsdlInterfaceFactory.importWsdl( project, "http://localhost:8082/test1/TestService.wsdl", true )[0];

      // create empty testcase
      WsdlTestCase testCase = project.addNewTestSuite( "TestSuite" ).addNewTestCase( "TestCase" );

      // add mockresponse step for
      WsdlRequest request = iface.getOperationAt( 0 ).getRequestAt( 0 );
      TestStepConfig config = WsdlMockResponseStepFactory.createConfig( request, false );
      WsdlMockResponseTestStep step = (WsdlMockResponseTestStep) testCase.addTestStep( config );

      WsdlMockResponse mockResponse = step.getMockResponse();
      mockResponse.setResponseContent( "<xml>Tjoho!!</xml>" );
      step.setPort( 8989 );
      step.setPath( "/tjohoo" );

      // run testcase
      TestCaseRunner runner = testCase.run( new StringToObjectMap(), true );
      Thread.sleep( 1000 );

      // submit request
      request.setEndpoint( "http://127.0.0.1:8989/tjohoo" );
      System.out.println( "Submitting first request" );
      Submit submit = request.submit( new WsdlSubmitContext( null ), false );

      // validate
      assertEquals( mockResponse.getResponseContent(), submit.getResponse().getContentAsString() );
      runner.waitUntilFinished();
      long timeTaken = runner.getResults().get( 0 ).getTimeTaken();
      assertTrue( timeTaken > 0 );
      assertEquals( Status.FINISHED, runner.getStatus() );
      assertEquals( mockResponse.getMockResult().getMockRequest().getRequestContent().trim(), request.getRequestContent().trim() );

      System.out.println( "Mock response took " + timeTaken + "ms" );

      // add copy
      testCase.cloneStep( step, "TestStep2" );

      assertEquals( testCase.getTestStepCount(), 2 );

      //	 run testcase again
      runner = testCase.run( new StringToObjectMap(), true );

      // wait for response step to start listening..
      Thread.sleep( 1000 );

      System.out.println( "Submitting first request again" );
      submit = request.submit( new WsdlSubmitContext( null ), false );
      assertEquals( mockResponse.getResponseContent(), submit.getResponse().getContentAsString() );
      assertEquals( Status.RUNNING, runner.getStatus() );
      assertEquals( mockResponse.getMockResult().getMockRequest().getRequestContent().trim(), request.getRequestContent().trim() );

      // wait for second response step to start listening
      Thread.sleep( 1000 );
      System.out.println( "Submitting second request" );
      submit = request.submit( new WsdlSubmitContext( null ), false );

      // validate
      assertEquals( mockResponse.getResponseContent(), submit.getResponse().getContentAsString() );

      // wait for runner to get result
      Thread.sleep( 1000 );

      timeTaken = runner.getResults().get( 1 ).getTimeTaken();
      assertTrue( timeTaken > 0 );
      assertEquals( Status.FINISHED, runner.getStatus() );
      assertEquals( mockResponse.getMockResult().getMockRequest().getRequestContent().trim(), request.getRequestContent().trim() );

      System.out.println( "Second Mock response took " + timeTaken + "ms" );
   }
}
