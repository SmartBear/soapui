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

package com.eviware.soapui.security;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;

/**
 * SecurityTestContext implementation for SecurityTests not active - just left
 * in case needed later
 *
 * @author SoapUI team
 */

public class SecurityTestContext extends DefaultPropertyExpansionContext
// implements SecurityTestRunContext
{

    public SecurityTestContext(ModelItem modelItem) {
        super(modelItem);
        // TODO Auto-generated constructor stub
    }
    // private final SecurityTestRunner runner;
    //
    // public SecurityTestContext( SecurityTestRunner runner )
    // {
    // super( runner.getSecurityTest().getTestCase() );
    // this.runner = runner;
    // }
    //
    // public SecurityTestRunner getSecurityTestRunner()
    // {
    // return runner;
    // }
    //
    // @Override
    // public Object get( Object key )
    // {
    // if( "securityTestRunner".equals( key ) )
    // return runner;
    //
    // return super.get( key );
    // }
    //
    // public Object getProperty( String testStep, String propertyName )
    // {
    // return null;
    // }
    //
    // public TestCaseRunner getTestRunner()
    // {
    // return null;
    // }
}
