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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by ole on 27/05/14.
 */

public class CurrentRunStepIndexProviderTest
{
    @Test
    public void testProvider()
    {
        CurrentRunStepIndexProvider provider = new CurrentRunStepIndexProvider();

        WsdlTestRunContext context = Mockito.mock(WsdlTestRunContext.class);
        AbstractTestCaseRunner runner = Mockito.mock(AbstractTestCaseRunner.class);
        Mockito.when( runner.getResultCount()).thenReturn( 3 );

        Mockito.when( context.getTestRunner()).thenReturn(runner);

        Assert.assertEquals( "3", provider.getValue( context ));
    }
}
