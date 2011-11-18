/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.panels.assertions;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ResponseSLAAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleNotContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XQueryContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.jms.JMSStatusAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.jms.JMSTimeoutAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.recent.RecentAssertionHandler;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.NotSoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.WSAResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.WSSStatusAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.security.assertion.InvalidHttpStatusCodesAssertion;
import com.eviware.soapui.security.assertion.SensitiveInfoExposureAssertion;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;

public class AssertionCategoryMapping
{
	public final static String VALIDATE_RESPONSE_CONTENT_CATEGORY = "Validate Response Content";
	public final static String STATUS_CATEGORY = "Compliance, Status and Standards";
	public final static String SCRIPT_CATEGORY = "Script";
	public final static String SLA_CATEGORY = "SLA";
	public final static String JMS_CATEGORY = "JMS";
	public final static String SECURITY_CATEGORY = "Security";
	public final static String RECENTLY_USED = "Recently used";

	public static String[] getAssertionCategories()
	{
		return new String[] { VALIDATE_RESPONSE_CONTENT_CATEGORY, STATUS_CATEGORY, SCRIPT_CATEGORY, SLA_CATEGORY,
				JMS_CATEGORY, SECURITY_CATEGORY, RECENTLY_USED };
	}

	private static void addRecentlyAddedAssertions(
			LinkedHashMap<String, LinkedHashSet<AssertionListEntry>> categoriesAssertionsMap, Assertable assertable,
			RecentAssertionHandler recentAssertionHandler )
	{
		LinkedHashSet<AssertionListEntry> recentlyUsedSet = new LinkedHashSet<AssertionListEntry>();

		for( String name : recentAssertionHandler.get() )
		{
			String type = recentAssertionHandler.getAssertionTypeByName( name );

			if( type != null )
			{
				if( recentAssertionHandler.canAssert( type, assertable ) )
				{
					recentlyUsedSet.add( recentAssertionHandler.getAssertionListEntry( type ) );
				}
			}
		}

		if( recentlyUsedSet.size() > 0 )
			categoriesAssertionsMap.put( RECENTLY_USED, recentlyUsedSet );
	}

	public static LinkedHashMap<String, LinkedHashSet<AssertionListEntry>> getCategoriesAssertionsMap(
			Assertable assertable, RecentAssertionHandler recentAssertionHandler )
	{
		LinkedHashMap<String, LinkedHashSet<AssertionListEntry>> categoriesAssertionsMap = new LinkedHashMap<String, LinkedHashSet<AssertionListEntry>>();
		String[] assertions = TestAssertionRegistry.getInstance().getAvailableAssertionNames( assertable );

		addRecentlyAddedAssertions( categoriesAssertionsMap, assertable, recentAssertionHandler );

		LinkedHashSet<AssertionListEntry> validatingResponseAssertionsSet = new LinkedHashSet<AssertionListEntry>();
		for( String availableAssertion : assertions )
		{
			if( availableAssertion.equals( XPathContainsAssertion.LABEL ) )
			{
				validatingResponseAssertionsSet.add( new AssertionListEntry( XPathContainsAssertion.LABEL,
						XPathContainsAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( XQueryContainsAssertion.LABEL ) )
			{
				validatingResponseAssertionsSet.add( new AssertionListEntry( XQueryContainsAssertion.LABEL,
						XQueryContainsAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( SimpleContainsAssertion.LABEL ) )
			{
				validatingResponseAssertionsSet.add( new AssertionListEntry( SimpleContainsAssertion.LABEL,
						SimpleContainsAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( SimpleNotContainsAssertion.LABEL ) )
			{
				validatingResponseAssertionsSet.add( new AssertionListEntry( SimpleNotContainsAssertion.LABEL,
						SimpleNotContainsAssertion.DESCRIPTION ) );
			}
		}
		if( validatingResponseAssertionsSet.size() > 0 )
			categoriesAssertionsMap.put( VALIDATE_RESPONSE_CONTENT_CATEGORY, validatingResponseAssertionsSet );

		LinkedHashSet<AssertionListEntry> statusAssertionsSet = new LinkedHashSet<AssertionListEntry>();
		for( String availableAssertion : assertions )
		{
			if( availableAssertion.equals( InvalidHttpStatusCodesAssertion.LABEL ) )
			{
				statusAssertionsSet.add( new AssertionListEntry( InvalidHttpStatusCodesAssertion.LABEL,
						InvalidHttpStatusCodesAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( WSSStatusAssertion.LABEL ) )
			{
				statusAssertionsSet
						.add( new AssertionListEntry( WSSStatusAssertion.LABEL, WSSStatusAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( NotSoapFaultAssertion.LABEL ) )
			{
				statusAssertionsSet.add( new AssertionListEntry( NotSoapFaultAssertion.LABEL,
						NotSoapFaultAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( ValidHttpStatusCodesAssertion.LABEL ) )
			{
				statusAssertionsSet.add( new AssertionListEntry( ValidHttpStatusCodesAssertion.LABEL,
						ValidHttpStatusCodesAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( SoapResponseAssertion.LABEL ) )
			{
				statusAssertionsSet.add( new AssertionListEntry( SoapResponseAssertion.LABEL,
						SoapResponseAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( WSAResponseAssertion.LABEL ) )
			{
				statusAssertionsSet.add( new AssertionListEntry( WSAResponseAssertion.LABEL,
						WSAResponseAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( SchemaComplianceAssertion.LABEL ) )
			{
				statusAssertionsSet.add( new AssertionListEntry( SchemaComplianceAssertion.LABEL,
						SchemaComplianceAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( SoapFaultAssertion.LABEL ) )
			{
				statusAssertionsSet
						.add( new AssertionListEntry( SoapFaultAssertion.LABEL, SoapFaultAssertion.DESCRIPTION ) );
			}
		}
		if( statusAssertionsSet.size() > 0 )
			categoriesAssertionsMap.put( STATUS_CATEGORY, statusAssertionsSet );

		LinkedHashSet<AssertionListEntry> scriptAssertionsSet = new LinkedHashSet<AssertionListEntry>();
		for( String availableAssertion : assertions )
		{
			if( availableAssertion.equals( GroovyScriptAssertion.LABEL ) )
			{
				scriptAssertionsSet.add( new AssertionListEntry( GroovyScriptAssertion.LABEL,
						GroovyScriptAssertion.DESCRIPTION ) );
			}
		}
		if( scriptAssertionsSet.size() > 0 )
			categoriesAssertionsMap.put( SCRIPT_CATEGORY, scriptAssertionsSet );

		LinkedHashSet<AssertionListEntry> slaAssertionsSet = new LinkedHashSet<AssertionListEntry>();
		for( String availableAssertion : assertions )
		{
			if( availableAssertion.equals( ResponseSLAAssertion.LABEL ) )
			{
				slaAssertionsSet
						.add( new AssertionListEntry( ResponseSLAAssertion.LABEL, ResponseSLAAssertion.DESCRIPTION ) );
			}
		}
		if( slaAssertionsSet.size() > 0 )
			categoriesAssertionsMap.put( SLA_CATEGORY, slaAssertionsSet );

		LinkedHashSet<AssertionListEntry> jmsAssertionsSet = new LinkedHashSet<AssertionListEntry>();
		for( String availableAssertion : assertions )
		{
			if( availableAssertion.equals( JMSStatusAssertion.LABEL ) )
			{
				jmsAssertionsSet.add( new AssertionListEntry( JMSStatusAssertion.LABEL, JMSStatusAssertion.DESCRIPTION ) );
			}
			if( availableAssertion.equals( JMSTimeoutAssertion.LABEL ) )
			{
				jmsAssertionsSet.add( new AssertionListEntry( JMSTimeoutAssertion.LABEL, JMSTimeoutAssertion.DESCRIPTION ) );
			}
		}
		if( jmsAssertionsSet.size() > 0 )
			categoriesAssertionsMap.put( JMS_CATEGORY, jmsAssertionsSet );

		LinkedHashSet<AssertionListEntry> securityAssertionsSet = new LinkedHashSet<AssertionListEntry>();
		for( String availableAssertion : assertions )
		{
			if( availableAssertion.equals( SensitiveInfoExposureAssertion.LABEL ) )
			{
				securityAssertionsSet.add( new AssertionListEntry( SensitiveInfoExposureAssertion.LABEL,
						SensitiveInfoExposureAssertion.DESCRIPTION ) );
			}
		}
		if( securityAssertionsSet.size() > 0 )
			categoriesAssertionsMap.put( SECURITY_CATEGORY, securityAssertionsSet );

		return categoriesAssertionsMap;
	}
}
