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

package com.eviware.soapui.model.propertyexpansion.resolvers;

import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;

public class TestRunPropertyResolver implements PropertyResolver {
    public String resolveProperty(PropertyExpansionContext context, String propertyName, boolean globalOverride) {
        // if( !(context instanceof TestRunContext ))
        // return null;
        //
        // TestRunContext trc = ( TestRunContext ) context;
        //
        // TestCase testCase = trc.getTestCase();
        // if( testCase == null )
        // return null;
        // TestSuite testSuite = testCase.getTestSuite();
        //
        // if( propertyName.charAt( 0 ) == PropertyExpansion.SCOPE_PREFIX )
        // {
        // // explicit item reference?
        // String value = ResolverUtils.checkForExplicitReference( propertyName,
        // PropertyExpansion.PROJECT_REFERENCE, testSuite.getProject(), trc,
        // globalOverride );
        // if( value != null )
        // return value;
        //
        // value = ResolverUtils.checkForExplicitReference( propertyName,
        // PropertyExpansion.TESTSUITE_REFERENCE, testSuite, trc, globalOverride
        // );
        // if( value != null )
        // return value;
        //
        // value = ResolverUtils.checkForExplicitReference( propertyName,
        // PropertyExpansion.TESTCASE_REFERENCE, testCase, trc, globalOverride );
        // if( value != null )
        // return value;
        // }
        //
        // int sepIx = propertyName.indexOf( PropertyExpansion.PROPERTY_SEPARATOR
        // );
        // Object property = null;
        //
        // if( sepIx > 0 )
        // {
        // String step = propertyName.substring( 0, sepIx );
        // String name = propertyName.substring( sepIx+1 );
        //
        // sepIx = name.indexOf( PropertyExpansion.PROPERTY_SEPARATOR );
        // if( sepIx != -1 )
        // {
        // String xpath = name.substring( sepIx+1 );
        // name = name.substring( 0, sepIx );
        //
        // if( step.length() == 0 )
        // property = trc.getProperty( name);
        // else
        // property = trc.getProperty( step, name);
        //
        // if( property != null )
        // {
        // property = ResolverUtils.extractXPathPropertyValue( property,
        // PropertyExpansionUtils.expandProperties( trc, xpath ) );
        // }
        // }
        // else
        // {
        // if( step.length() == 0 )
        // property = trc.getProperty( name);
        // else
        // property = trc.getProperty( step, name);
        // }
        // }
        //
        // return property == null ? null : property.toString();

        return null;
    }
}
