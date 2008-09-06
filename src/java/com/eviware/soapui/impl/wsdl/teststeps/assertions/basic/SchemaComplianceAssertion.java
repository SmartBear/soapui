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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.basic;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.DefinitionContext;
import com.eviware.soapui.impl.wadl.WadlDefinitionContext;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.RestMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlValidator;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import org.apache.xmlbeans.XmlObject;

/**
 * Asserts that a request or response message complies with its related
 * WSDL definition / XML Schema
 *
 * @author Ole.Matzura
 */

public class SchemaComplianceAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion
{
   public static final String ID = "Schema Compliance";
   public static final String LABEL = "Schema Compliance";

   private String definition;
   private DefinitionContext definitionContext;
   private String wsdlContextDef;

   public SchemaComplianceAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
   {
      super( assertionConfig, assertable, false, true, false, true );

      XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
      definition = reader.readString( "definition", null );
   }

   @Override
   public void prepare( TestRunner testRunner, TestRunContext testRunContext ) throws Exception
   {
      super.prepare( testRunner, testRunContext );

      definitionContext = null;
      wsdlContextDef = null;
   }

   protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context ) throws AssertionException
   {
      if( messageExchange instanceof WsdlMessageExchange )
      {
         return assertWsdlResponse( (WsdlMessageExchange) messageExchange, context );
      }
      else if( messageExchange instanceof RestMessageExchange )
      {
         return assertWadlResponse( (RestMessageExchange) messageExchange, context );
      }

      throw new AssertionException( new AssertionError( "Unknown MessageExchange type" ) );
   }

   private String assertWadlResponse( RestMessageExchange messageExchange, SubmitContext context ) throws AssertionException
   {
      WadlDefinitionContext wadlContext = null;
      try
      {
         definitionContext = getWadlContext( messageExchange, context );
      }
      catch( Exception e1 )
      {
         throw new AssertionException( new AssertionError( e1.getMessage() ) );
      }

      WadlValidator validator = new WadlValidator( wadlContext );

      try
      {
         AssertionError[] errors = validator.assertResponse( messageExchange );
         if( errors.length > 0 )
            throw new AssertionException( errors );
      }
      catch( AssertionException e )
      {
         throw e;
      }
      catch( Exception e )
      {
         throw new AssertionException( new AssertionError( e.getMessage() ) );
      }

      return "Schema compliance OK";
   }

   private String assertWsdlResponse( WsdlMessageExchange messageExchange, SubmitContext context )
           throws AssertionException
   {
      WsdlContext wsdlContext = null;
      try
      {
         wsdlContext = (WsdlContext) getWsdlContext( messageExchange, context );
      }
      catch( Exception e1 )
      {
         throw new AssertionException( new AssertionError( e1.getMessage() ) );
      }

      WsdlValidator validator = new WsdlValidator( wsdlContext );

      try
      {
         AssertionError[] errors = validator.assertResponse( messageExchange, false );
         if( errors.length > 0 )
            throw new AssertionException( errors );
      }
      catch( AssertionException e )
      {
         throw e;
      }
      catch( Exception e )
      {
         throw new AssertionException( new AssertionError( e.getMessage() ) );
      }

      return "Schema compliance OK";
   }

   private DefinitionContext getWsdlContext( WsdlMessageExchange messageExchange, SubmitContext context ) throws Exception
   {
      WsdlOperation operation = messageExchange.getOperation();
      WsdlInterface iface = (WsdlInterface) operation.getInterface();
      if( definition == null || definition.trim().length() == 0 || definition.equals(
              PathUtils.expandPath( iface.getDefinition(), iface, context ) ) )
      {
         definitionContext = (iface).getWsdlContext();
         ((WsdlContext) definitionContext).loadIfNecessary();
      }
      else
      {
         String def = PropertyExpansionUtils.expandProperties( context, definition );
         if( definitionContext == null || !def.equals( wsdlContextDef ) )
         {
            definitionContext = new WsdlContext( def, iface.getSoapVersion() );
            ((WsdlContext) definitionContext).load();
            ((WsdlContext) definitionContext).setInterface( iface );
            wsdlContextDef = def;
         }
      }

      return definitionContext;
   }

   private DefinitionContext getWadlContext( RestMessageExchange messageExchange, SubmitContext context ) throws Exception
   {
      RestResource operation = messageExchange.getResource();
      RestService service = operation.getService();
      if( definition == null || definition.trim().length() == 0 || definition.equals(
              PathUtils.expandPath( service.getDefinition(), service, context ) ) )
      {
         definitionContext = service.getWadlContext();
         ((WadlDefinitionContext) definitionContext).loadIfNecessary();
      }
      else
      {
         String def = PropertyExpansionUtils.expandProperties( context, definition );
         if( definitionContext == null || !def.equals( wsdlContextDef ) )
         {
            definitionContext = new WadlDefinitionContext( def );
            ((WadlDefinitionContext) definitionContext).load();
            ((WadlDefinitionContext) definitionContext).setInterface( service );
            wsdlContextDef = def;
         }
      }

      return definitionContext;
   }


   public boolean configure()
   {
      String value = definition;

      WsdlInterface iface = (WsdlInterface) getAssertable().getInterface();
      String orgDef = iface == null ? null : PathUtils.expandPath( iface.getDefinition(), iface );

      if( value == null || value.trim().length() == 0 )
      {
         value = orgDef;
      }

      value = UISupport.prompt( "Specify definition url to validate by", "Configure SchemaCompliance Assertion", value );

      if( value == null ) return false;

      if( value.trim().length() == 0 || value.equals( orgDef ) )
         definition = "";
      else
         definition = value;

      setConfiguration( createConfiguration() );
      return true;
   }

   protected XmlObject createConfiguration()
   {
      XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
      return builder.add( "definition", definition ).finish();
   }

   protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context ) throws AssertionException
   {
      WsdlContext wsdlContext = null;
      try
      {
         wsdlContext = (WsdlContext) getWsdlContext( (WsdlMessageExchange) messageExchange, context );
      }
      catch( Exception e1 )
      {
         throw new AssertionException( new AssertionError( e1.getMessage() ) );
      }
      WsdlValidator validator = new WsdlValidator( wsdlContext );

      try
      {
         AssertionError[] errors = validator.assertRequest( (WsdlMessageExchange) messageExchange, false );
         if( errors.length > 0 )
            throw new AssertionException( errors );
      }
      catch( AssertionException e )
      {
         throw e;
      }
      catch( Exception e )
      {
         throw new AssertionException( new AssertionError( e.getMessage() ) );
      }

      return "Schema compliance OK";
   }

   public static class Factory extends AbstractTestAssertionFactory
   {
      public Factory()
      {
         super( SchemaComplianceAssertion.ID, SchemaComplianceAssertion.LABEL, SchemaComplianceAssertion.class );
      }

      @Override
      public boolean canAssert( Assertable assertable )
      {
         return super.canAssert( assertable ) && assertable.getInterface() instanceof AbstractInterface &&
                 ((AbstractInterface) assertable.getInterface()).getDefinitionContext().hasSchemaTypes();
      }
   }
}
