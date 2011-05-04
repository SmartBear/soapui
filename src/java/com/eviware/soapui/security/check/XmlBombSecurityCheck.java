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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.DTDTypeConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.config.XmlBombSecurityCheckConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.security.ui.XmlBombSecurityCheckConfigPanel;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

public class XmlBombSecurityCheck extends AbstractSecurityCheckWithProperties
{

	public static final String TYPE = "XmlBombSecurityCheck";
	public static final String NAME = "XML Bomb";
	private static final String DEFAULT_PREFIX = "xmlbomb";

	private int currentIndex = 0;
	private XmlBombSecurityCheckConfig xmlBombConfig;
	private Map<SecurityCheckedParameter, ArrayList<DTDTypeConfig>> parameterMutations = new HashMap<SecurityCheckedParameter, ArrayList<DTDTypeConfig>>();
	private boolean mutation;

	public XmlBombSecurityCheck( SecurityCheckConfig config, ModelItem parent, String icon, TestStep testStep )
	{
		super( testStep, config, parent, icon );
		if( config.getConfig() == null || !( config.getConfig() instanceof XmlBombSecurityCheckConfig ) )
			initXmlBombConfig();
		else
			xmlBombConfig = ( XmlBombSecurityCheckConfig )config.getConfig();

	}

	private void initXmlBombConfig()
	{
		getConfig().setConfig( XmlBombSecurityCheckConfig.Factory.newInstance() );
		xmlBombConfig = ( XmlBombSecurityCheckConfig )getConfig().getConfig();

		xmlBombConfig.setAttachXmlBomb( false );
		xmlBombConfig.setXmlAttachmentPrefix( DEFAULT_PREFIX );

		xmlBombConfig.setUseExternalDTD( true );
		xmlBombConfig.setUseInternalDTD( true );
		initDefaultVectors();

	}

	public boolean useExternalDTD()
	{
		return xmlBombConfig.getUseExternalDTD();
	}

	public boolean useInternalDTD()
	{
		return xmlBombConfig.getUseInternalDTD();
	}

	public void setUseExternalDTD( boolean use )
	{
		xmlBombConfig.setUseExternalDTD( use );
	}

	public void setUseInternalDTD( boolean use )
	{
		xmlBombConfig.setUseInternalDTD( use );
	}

	private void initDefaultVectors()
	{
		try
		{
			InputStream in = SoapUI.class
					.getResourceAsStream( "/com/eviware/soapui/resources/security/xmlbomb/BillionLaughsAttack.xml.txt" );
			BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
			String strLine;
			StringBuffer value = new StringBuffer();
			while( ( strLine = br.readLine() ) != null )
			{
				value.append( strLine ).append( '\n' );
			}
			in.close();
			XmlString bomb = xmlBombConfig.addNewXmlBombs();
			bomb.setStringValue( value.toString() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		try
		{
			InputStream in = SoapUI.class
					.getResourceAsStream( "/com/eviware/soapui/resources/security/xmlbomb/QuadraticBlowup.xml.txt" );
			BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
			String strLine;
			StringBuffer value = new StringBuffer();
			while( ( strLine = br.readLine() ) != null )
			{
				value.append( strLine ).append( '\n' );
			}
			in.close();
			XmlString bomb = xmlBombConfig.addNewXmlBombs();
			bomb.setStringValue( value.toString() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		try
		{
			InputStream in = SoapUI.class
					.getResourceAsStream( "/com/eviware/soapui/resources/security/xmlbomb/BillionLaughsAttack.dtd.txt" );
			BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
			String strLine;
			StringBuffer value = new StringBuffer();
			while( ( strLine = br.readLine() ) != null )
			{
				value.append( strLine ).append( '\n' );
			}
			in.close();
			DTDTypeConfig bombDTD = xmlBombConfig.addNewInternalDTD();
			bombDTD.setValue( value.toString() );
			bombDTD.setReference( "lol10" );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		try
		{
			InputStream in = SoapUI.class
					.getResourceAsStream( "/com/eviware/soapui/resources/security/xmlbomb/QuadraticBlowup.dtd.txt" );
			BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
			String strLine;
			StringBuffer value = new StringBuffer();
			while( ( strLine = br.readLine() ) != null )
			{
				value.append( strLine ).append( '\n' );
			}
			in.close();
			DTDTypeConfig bombDTD = xmlBombConfig.addNewInternalDTD();
			bombDTD.setValue( value.toString() );
			bombDTD.setReference( "boom" );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		try
		{
			InputStream in = SoapUI.class
					.getResourceAsStream( "/com/eviware/soapui/resources/security/xmlbomb/ExternalEntity.dtd.txt" );
			BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
			String strLine;
			StringBuffer value = new StringBuffer();
			while( ( strLine = br.readLine() ) != null )
			{
				value.append( strLine ).append( '\n' );
			}
			in.close();
			DTDTypeConfig bombDTD = xmlBombConfig.addNewExternalDTD();
			bombDTD.setValue( value.toString() );
			bombDTD.setReference( "loadui" );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		try
		{
			StringToStringMap updatedParams = update( testStep, context );
			MessageExchange message = ( MessageExchange )testStep.run( ( TestCaseRunner )securityTestRunner, context );
			createMessageExchange( updatedParams, message );
		}
		catch( XmlException e )
		{
			SoapUI.logError( e, "[XmlBombSecurityScan]XPath seems to be invalid!" );
			reportSecurityCheckException( "Property value is not XML or XPath is wrong!" );
		}
		catch( Exception e )
		{
			SoapUI.logError( e, "[XmlBombSecurityScan]Property value is not valid xml!" );
			reportSecurityCheckException( "Property value is not XML or XPath is wrong!" );
		}
	}
	
	private StringToStringMap update( TestStep testStep, SecurityTestRunContext context ) throws XmlException, Exception
	{
		StringToStringMap params = new StringToStringMap();

		if( parameterMutations.size() == 0 )
			mutateParameters( testStep, context );

		if( getExecutionStrategy().getStrategy() == StrategyTypeConfig.ONE_BY_ONE )
		{
			/*
			 * Idea is to drain for each parameter mutations.
			 */
			for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
			{
				if( parameterMutations.containsKey( param ) )
					if( parameterMutations.get( param ).size() > 0 )
					{
						TestProperty property = getTestStep().getProperties().get( param.getName() );
						String value = context.expand( property.getValue() );
						if( param.getXpath() == null || param.getXpath().trim().length() == 0 )
						{
							DTDTypeConfig dtd = parameterMutations.get( param ).get( 0 );
							testStep.getProperties().get( param.getName() ).setValue( dtd.getValue() );
							params.put( param.getLabel(), dtd.getValue() );
							parameterMutations.get( param ).remove( 0 );
						}
						else
						{
							// no value, do nothing.
							if( value == null || value.trim().equals( "" ) )
								continue;
							if( XmlUtils.seemsToBeXml( value ) )
							{
								XmlObjectTreeModel model = new XmlObjectTreeModel( property.getSchemaType().getTypeSystem(),
										XmlObject.Factory.parse( value ) );
								XmlTreeNode[] nodes = model.selectTreeNodes( context.expand( param.getXpath() ) );
								DTDTypeConfig dtd = parameterMutations.get( param ).get( 0 );
								for( XmlTreeNode node : nodes )
									node.setValue( 1, "&"+dtd.getReference()+";" );
								params.put( param.getLabel(), "&"+dtd.getReference()+";" );
								parameterMutations.get( param ).remove( 0 );

								String xml = model.getXmlObject().toString().trim();
								//this is needed since xmlbeans translates & into &amp; and we do not want that. 
								xml = xml.replaceFirst( "&amp;"+dtd.getReference(), "&"+dtd.getReference() );
								if( xml.startsWith( "<?" ) )
								{
									if( xml.indexOf( "?>" ) > -1 )
									{
										StringBuffer buffer = new StringBuffer( xml );
										buffer.insert( xml.indexOf( "?>" ) + 2, dtd.getReference() );
										testStep.getProperties().get( param.getName() ).setValue( buffer.toString() );
									}
								}
								else
								{
									testStep.getProperties().get( param.getName() ).setValue( dtd.getValue() + "\n" + xml );
								}
							}
						}
						break;
					}
			}
		}

		return params;
	}

	private void mutateParameters( TestStep testStep, SecurityTestRunContext context ) throws XmlException, Exception
	{
		mutation = true;

		ArrayList<DTDTypeConfig> allDTDs = new ArrayList<DTDTypeConfig>();

		if( xmlBombConfig.getUseExternalDTD() )
			allDTDs.addAll( xmlBombConfig.getExternalDTDList() );
		if( xmlBombConfig.getUseInternalDTD() )
			allDTDs.addAll( xmlBombConfig.getInternalDTDList() );

		// for each parameter
		for( SecurityCheckedParameter parameter : getParameterHolder().getParameterList() )
		{

			if( parameter.isChecked() )
			{
				TestProperty property = testStep.getProperties().get( parameter.getName() );
				// check parameter does not have any xpath
				// than mutate whole parameter
				if( parameter.getXpath() == null || parameter.getXpath().trim().length() == 0 )
				{
					for( DTDTypeConfig dtd : allDTDs )
					{

						if( !parameterMutations.containsKey( parameter ) )
							parameterMutations.put( parameter, new ArrayList<DTDTypeConfig>() );
						parameterMutations.get( parameter ).add( dtd );

					}
				}
				else
				{
					// we have xpath but do we have xml which need to mutate
					// ignore if there is no value, since than we'll get exception
					if( property.getValue() == null && property.getDefaultValue() == null )
						continue;
					// get value of that property
					String value = context.expand( property.getValue() );

					// we have something that looks like xpath, or hope so.

					XmlObjectTreeModel model = null;

					model = new XmlObjectTreeModel( property.getSchemaType().getTypeSystem(), XmlObject.Factory
							.parse( value ) );

					XmlTreeNode[] nodes = model.selectTreeNodes( context.expand( parameter.getXpath() ) );

					// for each invalid type set all nodes

					for( DTDTypeConfig dtd : allDTDs )
					{

						if( nodes.length > 0 )
						{
							if( !parameterMutations.containsKey( parameter ) )
								parameterMutations.put( parameter, new ArrayList<DTDTypeConfig>() );
							parameterMutations.get( parameter ).add( dtd );
						}

					}

				}
			}
		}

	}

	// protected void execute2( SecurityTestRunner securityTestRunner, TestStep
	// testStep, SecurityTestRunContext context )
	// {
	// currentIndex = 0;
	//
	// String originalResponse = getOriginalResult( ( SecurityTestRunnerImpl
	// )securityTestRunner, testStep )
	// .getResponse().getContentAsXml();
	// String originalRequest = getRequest( testStep ).getRequestContent();
	// if( isAttachXmlBomb() )
	// {
	// while( currentIndex < getXmlBombList().size() + 1 )
	// {
	// Attachment attach = attachXmlBomb( testStep );
	// // runCheck(testStep, context, securityTestLog, testCaseRunner,
	// // originalResponse,
	// // "Possible XML Bomb Vulnerability Detected");
	// ( ( AbstractHttpRequest<?> )getRequest( testStep ) ).removeAttachment(
	// attach );
	// ( ( AbstractHttpRequest<?> )getRequest( testStep ) ).setRequestContent(
	// originalRequest );
	// }
	//
	// currentIndex = 0;
	// }
	//
	// if( getExecutionStrategy().getStrategy() == StrategyTypeConfig.ONE_BY_ONE
	// && getParameterHolder().getParameterList().size() > 0 )
	// {
	// for( SecurityCheckedParameter param :
	// getParameterHolder().getParameterList() )
	// {
	// if( param != null )
	// {
	// while( currentIndex < getXmlBombList().size() + 1 )
	// {
	// generateNextRequest( testStep, param.getName() );
	// // runCheck(testStep, context, securityTestLog,
	// // testCaseRunner, originalResponse,
	// // "Possible XML Bomb Vulnerability Detected");
	// ( ( AbstractHttpRequest<?> )getRequest( testStep ) ).setRequestContent(
	// originalRequest );
	// }
	// }
	// }
	// }
	// else if( getParameterHolder().getParameterList().size() > 0 )
	// {
	// while( currentIndex < getXmlBombList().size() + 1 )
	// {
	// generateNextRequest( testStep, getParameterHolder().getParameterList() );
	// // runCheck( testStep, context, securityTestLog, testCaseRunner,
	// // originalResponse,
	// // "Possible XML Bomb Vulnerability Detected" );
	// ( ( AbstractHttpRequest<?> )getRequest( testStep ) ).setRequestContent(
	// originalRequest );
	// }
	// }
	// // TODO
	//
	// }

	// private TestStep generateNextRequest( TestStep testStep,
	// List<SecurityCheckedParameter> list )
	// {
	// AbstractHttpRequest<?> request = ( AbstractHttpRequest<?> )getRequest(
	// testStep );
	// if( currentIndex < getXmlBombList().size() )
	// {
	// String bomb = getXmlBombList().get( currentIndex );
	//
	// String requestContent = request.getRequestContent();
	// String newRequestContent = requestContent;
	// if( testStep instanceof WsdlTestRequestStep )
	// {
	// for( SecurityCheckedParameter param : list )
	// {
	// newRequestContent = XmlUtils.setXPathContent( newRequestContent,
	// param.getName().substring(
	// param.getName().lastIndexOf( "\n" ) + 1 ), "&&payload&&" );
	// }
	// newRequestContent = newRequestContent.replaceAll(
	// "&amp;&amp;payload&amp;&amp;", "&payload" );
	// }
	//
	// newRequestContent = bomb + newRequestContent;
	//
	// request.setRequestContent( newRequestContent );
	//
	// currentIndex++ ;
	// }
	// else if( currentIndex == getXmlBombList().size() )
	// {
	// // request.setRequestContent( createQuadraticExpansionAttack(
	// // request.getRequestContent(), list ) );
	// }
	//
	// return testStep;
	//
	// }

	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		return null;
	}

	@Override
	public JComponent getAdvancedSettingsPanel()
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
		return xmlBombConfig.getAttachXmlBomb();
	}

	public void setAttachXmlBomb( boolean attach )
	{
		xmlBombConfig.setAttachXmlBomb( attach );
	}

	// private TestStep generateNextRequest( TestStep testStep, String param )
	// {
	// AbstractHttpRequest<?> request = ( AbstractHttpRequest<?> )getRequest(
	// testStep );
	//
	// if( currentIndex < getXmlBombList().size() )
	// {
	// String bomb = getXmlBombList().get( currentIndex );
	//
	// String requestContent = request.getRequestContent();
	// String newRequestContent = requestContent;
	// if( testStep instanceof WsdlTestRequestStep )
	// {
	// newRequestContent = XmlUtils.setXPathContent( request.getRequestContent(),
	// param.substring( param
	// .lastIndexOf( "\n" ) + 1 ), "&&payload&&" );
	// // We need to do this, since the parser we are using does not
	// // provide support for
	// // entity references (it throws a "Not Implemented" runtime
	// // exception when trying to create one
	// newRequestContent = newRequestContent.replaceAll(
	// "&amp;&amp;payload&amp;&amp;", "&payload;" );
	// }
	//
	// newRequestContent = bomb + newRequestContent;
	// // This is a bit of a hack, since the xpath functionality above
	// // strips the DTD if it is run
	// // after the DTD is added.
	// request.setRequestContent( newRequestContent );
	//
	// }
	// else if( currentIndex == getXmlBombList().size() )
	// {
	// List<RestParameterConfig> paramList = new
	// ArrayList<RestParameterConfig>();
	// RestParameterConfig restParam = RestParameterConfig.Factory.newInstance();
	// restParam.setName( param );
	// paramList.add( restParam );
	// // request.setRequestContent( createQuadraticExpansionAttack(
	// // request.getRequestContent(), paramList ) );
	// }
	//
	// currentIndex++ ;
	//
	// return testStep;
	// }

	private Attachment attachXmlBomb( TestStep testStep )
	{
		Attachment attach = null;
		if( isAttachXmlBomb() )
		{
			AbstractHttpRequest<?> request = ( AbstractHttpRequest<?> )getRequest( testStep );

			if( currentIndex < getXmlBombList().size() )
			{
				String bomb = getXmlBombList().get( currentIndex );
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
			else if( currentIndex == getXmlBombList().size() )
			{
				try
				{
					File bombFile = File.createTempFile( getAttachmentPrefix(), ".xml" );
					BufferedWriter writer = new BufferedWriter( new FileWriter( bombFile ) );
					// writer.write( createQuadraticExpansionAttack( null, null ) );
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

	public List<String> getXmlBombList()
	{
		return xmlBombConfig.getXmlBombsList();
	}

	protected void setBombList( List<String> bombList )
	{
		xmlBombConfig.setXmlBombsArray( bombList.toArray( new String[1] ) );
	}

	public List<DTDTypeConfig> getExternalDTDList()
	{
		return xmlBombConfig.getExternalDTDList();
	}

	public List<DTDTypeConfig> getInternalDTDList()
	{
		return xmlBombConfig.getInternalDTDList();
	}

	public String getAttachmentPrefix()
	{
		return xmlBombConfig.getXmlAttachmentPrefix();
	}

	public void setAttachmentPrefix( String prefix )
	{
		xmlBombConfig.setXmlAttachmentPrefix( prefix );
	}

	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		boolean hasNext = false;
		if( ( parameterMutations == null || parameterMutations.size() == 0 ) && !mutation )
		{
			if( getParameterHolder().getParameterList().size() > 0 )
				hasNext = true;
			else
				hasNext = false;
		}
		else
		{
			for( SecurityCheckedParameter param : parameterMutations.keySet() )
			{
				if( parameterMutations.get( param ).size() > 0 )
				{
					hasNext = true;
					break;
				}
			}
		}
		if( !hasNext )
		{
			parameterMutations.clear();
			mutation = false;
		}
		return hasNext;
	}

	@Override
	public String getConfigDescription()
	{
		return "Configures Xml bomb security scan";
	}

	@Override
	public String getConfigName()
	{
		return "XML Bomb Security Scan";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}
}
