/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.model.propertyexpansion.resolvers.providers;

import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ole on 27/05/14.
 */

public class CurrentStepRunIndexProviderTest
{
    @Test
    public void testProvider()
    {
        CurrentStepRunIndexProvider provider = new CurrentStepRunIndexProvider();

        TestStep mockStep1 = Mockito.mock( TestStep.class );
        Mockito.when( mockStep1.getId()).thenReturn( "1234" );

        TestStep mockStep2 = Mockito.mock( TestStep.class );
        Mockito.when( mockStep2.getId()).thenReturn( "2345" );

        TestStepResult mockResult1 = Mockito.mock( TestStepResult.class );
        Mockito.when( mockResult1.getTestStep()).thenReturn( mockStep1 );

        TestStepResult mockResult2 = Mockito.mock( TestStepResult.class );
        Mockito.when( mockResult2.getTestStep()).thenReturn( mockStep2 );

        List<TestStepResult> resultList = new ArrayList<TestStepResult>();
        resultList.add( mockResult1 );
        resultList.add( mockResult2 );
        resultList.add( mockResult2 );
        resultList.add( mockResult1 );
        resultList.add( mockResult2 );

        WsdlTestRunContext context = Mockito.mock(WsdlTestRunContext.class);
        AbstractTestCaseRunner runner = Mockito.mock(AbstractTestCaseRunner.class);
        Mockito.when( context.getTestRunner() ).thenReturn( runner );
        Mockito.when( runner.getResults()).thenReturn( resultList );

        Mockito.when( context.getCurrentStep()).thenReturn(mockStep1);
        Assert.assertEquals( "2", provider.getValue( context ));

        Mockito.when( context.getCurrentStep()).thenReturn(mockStep2);
        Assert.assertEquals( "3", provider.getValue( context ));
    }
}
