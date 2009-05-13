/*
 *  soapUI, copyright (C) 2004-2009 eviware.com
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.model.propertyexpansion.resolvers;

import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;

public class TestRunPropertyResolver implements PropertyResolver
{
	public String resolveProperty( PropertyExpansionContext context, String propertyName, boolean globalOverride )
	{
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
