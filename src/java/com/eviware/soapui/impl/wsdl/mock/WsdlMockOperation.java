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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.DispatchStyleConfig;
import com.eviware.soapui.config.DispatchStyleConfig.Enum;
import com.eviware.soapui.config.MockOperationConfig;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockRunContext;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.scripting.ScriptEnginePool;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A WsdlMockOperation in a WsdlMockService
 *
 * @author ole.matzura
 */

public class WsdlMockOperation extends AbstractWsdlModelItem<MockOperationConfig> implements MockOperation, PropertyChangeListener
{
   private final static Logger log = Logger.getLogger( WsdlMockOperation.class );

   public final static String DISPATCH_STYLE_PROPERTY = WsdlMockOperation.class.getName() + "@dispatchstyle";
   public final static String DEFAULT_RESPONSE_PROPERTY = WsdlMockOperation.class.getName() + "@defaultresponse";
   public final static String DISPATCH_PATH_PROPERTY = WsdlMockOperation.class.getName() + "@dispatchpath";
   public final static String OPERATION_PROPERTY = WsdlMockOperation.class.getName() + "@operation";

   private WsdlOperation operation;
   private List<WsdlMockResponse> responses = new ArrayList<WsdlMockResponse>();
   private int currentDispatchIndex;
   private ScriptEnginePool scriptEnginePool;
   private InternalInterfaceListener interfaceListener = new InternalInterfaceListener();
   private InternalProjectListener projectListener = new InternalProjectListener();
   private ImageIcon oneWayIcon;
   private ImageIcon notificationIcon;
   private ImageIcon solicitResponseIcon;

   private AtomicLong counter;
   private Map<QueryValuePair, WsdlMockResponse> responseMap;

   public WsdlMockOperation( WsdlMockService mockService, MockOperationConfig config )
   {
      super( config, mockService, "/mockOperation.gif" );

      Interface iface = mockService.getProject().getInterfaceByName( config.getInterface() );
      if( iface == null )
      {
         SoapUI.log.warn( "Missing interface [" + config.getInterface() +
                 "] for MockOperation in project" );
      }
      else
      {
         operation = (WsdlOperation) iface.getOperationByName( config.getOperation() );
      }

      List<MockResponseConfig> responseConfigs = config.getResponseList();
      for( MockResponseConfig responseConfig : responseConfigs )
      {
         WsdlMockResponse wsdlMockResponse = new WsdlMockResponse( this, responseConfig );
         wsdlMockResponse.addPropertyChangeListener( this );
         responses.add( wsdlMockResponse );
      }

      initData( config );

      counter = new AtomicLong();
      responseMap = new ConcurrentHashMap<QueryValuePair, WsdlMockResponse>();
   }

   private void initData( MockOperationConfig config )
   {
      if( !config.isSetName() )
         config.setName( operation == null ? "<missing operation>" : operation.getName() );

      if( !config.isSetDispatchStyle() )
         config.setDispatchStyle( DispatchStyleConfig.SEQUENCE );

      if( !config.isSetDefaultResponse() && responses.size() > 0 )
         setDefaultResponse( responses.get( 0 ).getName() );

      scriptEnginePool = new ScriptEnginePool( this );
      scriptEnginePool.setScript( getDispatchPath() );

      if( operation != null )
      {
         operation.getInterface().getProject().addProjectListener( projectListener );
         operation.getInterface().addInterfaceListener( interfaceListener );
         operation.getInterface().addPropertyChangeListener( WsdlInterface.NAME_PROPERTY, this );
      }

      oneWayIcon = UISupport.createImageIcon( "/onewaymockoperation.gif" );
      notificationIcon = UISupport.createImageIcon( "/mocknotificationoperation.gif" );
      solicitResponseIcon = UISupport.createImageIcon( "/mocksolicitresponseoperation.gif" );
   }

   public WsdlMockOperation( WsdlMockService mockService, MockOperationConfig config, WsdlOperation operation )
   {
      super( config, mockService, "/mockOperation.gif" );
      this.operation = operation;

      config.setInterface( operation.getInterface().getName() );
      config.setOperation( operation.getName() );

      initData( config );
      interfaceListener = new InternalInterfaceListener();

      counter = new AtomicLong();
      responseMap = new ConcurrentHashMap<QueryValuePair, WsdlMockResponse>();
   }

   @Override
   public ImageIcon getIcon()
   {
      if( operation != null )
      {
         if( isOneWay() )
         {
            return oneWayIcon;
         }
         else if( isNotification() )
         {
            return notificationIcon;
         }
         else if( isSolicitResponse() )
         {
            return solicitResponseIcon;
         }
      }

      return super.getIcon();
   }

   public WsdlMockService getMockService()
   {
      return (WsdlMockService) getParent();
   }

   public WsdlMockResponse getMockResponseAt( int index )
   {
      return responses.get( index );
   }

   public WsdlOperation getOperation()
   {
      return operation;
   }

   public WsdlMockResponse getMockResponseByName( String name )
   {
      return (WsdlMockResponse) getWsdlModelItemByName( responses, name );
   }

   public int getMockResponseCount()
   {
      return responses.size();
   }

   public WsdlMockResponse addNewMockResponse( MockResponseConfig responseConfig )
   {
      WsdlMockResponse mockResponse = new WsdlMockResponse( this, responseConfig );

      responses.add( mockResponse );
      if( responses.size() == 1 )
         setDefaultResponse( mockResponse.getName() );

      ( getMockService() ).fireMockResponseAdded( mockResponse );

      //add ws-a action
      WsdlUtils.setDefaultWsaAction( mockResponse.getWsaConfig(), true );

//   	String [] attrs = WsdlUtils.getExentsibilityAttributes(getOperation().getBindingOperation().getOperation().getOutput(), 
//   			new QName("http://www.w3.org/2006/05/addressing/wsdl", "Action") );
//   	if (attrs.length > 0)
//		{
//   		mockResponse.getWsaConfig().setAction(attrs[0]);
//		} else {
//			WsdlUtils.createDefaultWsaAction(mockResponse);
//		}

      return mockResponse;
   }

   public WsdlMockResponse addNewMockResponse( String name, boolean createResponse )
   {
      MockResponseConfig responseConfig = getConfig().addNewResponse();
      responseConfig.setName( name );
      responseConfig.addNewResponseContent();

      if( createResponse && getOperation() != null && getOperation().isBidirectional() )
      {
         boolean createOptional = SoapUI.getSettings().getBoolean( WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS );
         CompressedStringSupport.setString( responseConfig.getResponseContent(), getOperation().createResponse( createOptional ) );
      }

      return addNewMockResponse( responseConfig );
   }

   public WsdlMockResponse addNewMockResponse( String requestQuery, String matchingValue )
   {
      // Create a unique name.
      String name = String.valueOf( counter.addAndGet( 1 ) );

      // Create the mock response and store it for later retrieval.
      WsdlMockResponse mockResponse = addNewMockResponse( name, false );
      responseMap.put( new QueryValuePair( requestQuery, matchingValue ), mockResponse );
      return mockResponse;
   }

   public void removeMockResponse( WsdlMockResponse mockResponse )
   {
      int ix = responses.indexOf( mockResponse );
      responses.remove( ix );
      mockResponse.removePropertyChangeListener( this );

      try
      {
         ( getMockService() ).fireMockResponseRemoved( mockResponse );
      }
      finally
      {
         mockResponse.release();
         getConfig().removeResponse( ix );
      }
   }

   public WsdlMockResult dispatchRequest( WsdlMockRequest request ) throws DispatchException
   {
      try
      {
         request.setOperation( getOperation() );
         WsdlMockResult result = new WsdlMockResult( request );

         if( getMockResponseCount() == 0 )
            throw new DispatchException( "Missing MockResponse(s) in MockOperation [" + getName() + "]" );

         Enum dispatchStyle = getDispatchStyle();
         if( dispatchStyle == DispatchStyleConfig.XPATH )
         {
            dispatchRequestXPath( request, result );
         }
         else if( dispatchStyle == DispatchStyleConfig.SCRIPT )
         {
            dispatchRequestScript( request, result );
         }
         // I added DispatchStyleConfig.QUERY_MATCH to handle async response test step. /Lars
         else if( dispatchStyle == DispatchStyleConfig.QUERY_MATCH )
         {
            dispatchRequestQueryMatch( request, result );
         }
         else
         {
            dispatchRequestSequence( request, result );
         }

         return result;
      }
      catch( Throwable e )
      {
         throw new DispatchException( e );
      }
   }

   private void dispatchRequestQueryMatch( WsdlMockRequest request, WsdlMockResult result ) throws DispatchException
   {
      WsdlMockResponse mockResponse = getMatchingMockResponse( request );
      if( mockResponse != null )
      {
         result.setMockResponse( mockResponse );
         mockResponse.execute( request, result );
      }
      else
      {
         log.error( "Unable to find a response for the request. Dropping it!" );

// TODO Ericsson: Throw exception if no response was found?
//         throw new DispatchException("Unable to find a response for the request.");
      }
   }

   private void dispatchRequestXPath( WsdlMockRequest request, WsdlMockResult result )
           throws XmlException, DispatchException
   {
      XmlObject[] items = evaluateDispatchXPath( request );
      for( XmlObject item : items )
      {
         WsdlMockResponse mockResponse = getMockResponseByName( XmlUtils.getNodeValue( item.getDomNode() ) );

         if( mockResponse == null )
            mockResponse = getMockResponseByName( getDefaultResponse() );

         if( mockResponse != null )
         {
            result.setMockResponse( mockResponse );
            mockResponse.execute( request, result );
            return;
         }
      }

      throw new DispatchException( "Missing matching response message" );
   }

   private void dispatchRequestScript( WsdlMockRequest request, WsdlMockResult result )
           throws DispatchException
   {
      Object retVal = evaluateDispatchScript( request );

      WsdlMockResponse mockResponse = retVal == null ? getMockResponseByName( getDefaultResponse() )
              : getMockResponseByName( retVal.toString() );

      if( mockResponse == null )
         mockResponse = getMockResponseByName( getDefaultResponse() );

      if( mockResponse != null )
      {
         result.setMockResponse( mockResponse );
         mockResponse.execute( request, result );
         return;
      }
      else
      {
         throw new DispatchException( "Missing matching response message [" + retVal + "]" );
      }
   }

   private void dispatchRequestSequence( WsdlMockRequest request, WsdlMockResult result )
           throws DispatchException
   {
      WsdlMockResponse mockResponse = null;
      synchronized( this )
      {
         if( getDispatchStyle() == DispatchStyleConfig.RANDOM )
         {
            currentDispatchIndex = (int) ( ( Math.random() * getMockResponseCount() ) + 0.5F );
         }

         if( currentDispatchIndex >= getMockResponseCount() )
            currentDispatchIndex = 0;

         mockResponse = getMockResponseAt( currentDispatchIndex );
         result.setMockResponse( mockResponse );

         currentDispatchIndex++;
      }

      mockResponse.execute( request, result );
   }

   @Override
   public void release()
   {
      super.release();

      for( WsdlMockResponse response : responses )
      {
         response.removePropertyChangeListener( this );
         response.release();
      }

      scriptEnginePool.release();

      if( operation != null )
      {
         operation.getInterface().getProject().removeProjectListener( projectListener );
         operation.getInterface().removeInterfaceListener( interfaceListener );
         operation.getInterface().removePropertyChangeListener( WsdlInterface.NAME_PROPERTY, this );
      }
   }

   public XmlObject[] evaluateDispatchXPath( WsdlMockRequest request ) throws XmlException
   {
      XmlObject xmlObject = request.getRequestXmlObject();
      String path = getDispatchPath();
      if( StringUtils.isNullOrEmpty( path ) )
         throw new XmlException( "Missing dispatch XPath expression" );

      XmlObject[] items = xmlObject.selectPath( path );
      return items;
   }

   public Object evaluateDispatchScript( WsdlMockRequest request ) throws DispatchException
   {
      String dispatchPath = getDispatchPath();
      if( dispatchPath == null || dispatchPath.trim().length() == 0 )
      {
         throw new DispatchException( "Dispatch Script is empty" );
      }

      SoapUIScriptEngine scriptEngine = scriptEnginePool.getScriptEngine();

      try
      {
         WsdlMockService mockService = getMockService();
         WsdlMockRunner mockRunner = mockService.getMockRunner();
         MockRunContext context = mockRunner == null ? new WsdlMockRunContext( mockService, null ) : mockRunner.getMockContext();

         scriptEngine.setVariable( "context", context );
         scriptEngine.setVariable( "requestContext", request == null ? null : request.getRequestContext() );
         scriptEngine.setVariable( "mockRequest", request );
         scriptEngine.setVariable( "mockOperation", this );
         scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );

         scriptEngine.setScript( dispatchPath );
         Object retVal = scriptEngine.run();
         return retVal;
      }
      catch( Throwable e )
      {
         SoapUI.logError( e );
         throw new DispatchException( "Failed to dispatch using script; " + e );
      }
      finally
      {
         scriptEnginePool.returnScriptEngine( scriptEngine );
      }
   }

   public DispatchStyleConfig.Enum getDispatchStyle()
   {
      return getConfig().isSetDispatchStyle() ? getConfig().getDispatchStyle() : DispatchStyleConfig.SEQUENCE;
   }

   public void setDispatchStyle( DispatchStyleConfig.Enum dispatchStyle )
   {
      Enum old = getDispatchStyle();
      getConfig().setDispatchStyle( dispatchStyle );
      notifyPropertyChanged( DISPATCH_STYLE_PROPERTY, old, dispatchStyle );
   }

   public String getDispatchPath()
   {
      return getConfig().getDispatchPath();
   }

   public void setDispatchPath( String dispatchPath )
   {
      String old = getDispatchPath();
      getConfig().setDispatchPath( dispatchPath );
      notifyPropertyChanged( DISPATCH_PATH_PROPERTY, old, dispatchPath );

      scriptEnginePool.setScript( dispatchPath );
   }

   public String getWsdlOperationName()
   {
      return operation == null ? null : operation.getName();
   }

   public String getDefaultResponse()
   {
      return getConfig().getDefaultResponse();
   }

   public void setDefaultResponse( String defaultResponse )
   {
      String old = getDefaultResponse();
      getConfig().setDefaultResponse( defaultResponse );
      notifyPropertyChanged( DEFAULT_RESPONSE_PROPERTY, old, defaultResponse );
   }

   public List<MockResponse> getMockResponses()
   {
      return new ArrayList<MockResponse>( responses );
   }

   public void propertyChange( PropertyChangeEvent arg0 )
   {
      if( arg0.getPropertyName().equals( WsdlMockResponse.NAME_PROPERTY ) )
      {
         if( arg0.getOldValue().equals( getDefaultResponse() ) )
            setDefaultResponse( arg0.getNewValue().toString() );
      }
      else if( arg0.getPropertyName().equals( WsdlInterface.NAME_PROPERTY ) )
      {
         getConfig().setInterface( arg0.getNewValue().toString() );
      }
   }

   public WsdlMockResult getLastMockResult()
   {
      WsdlMockResult result = null;

      for( WsdlMockResponse response : responses )
      {
         WsdlMockResult mockResult = response.getMockResult();
         if( mockResult != null )
         {
            if( result == null || result.getTimestamp() > mockResult.getTimestamp() )
               result = mockResult;
         }
      }

      return result;
   }

   public void setOperation( WsdlOperation operation )
   {
      WsdlOperation oldOperation = getOperation();

      if( operation == null )
      {
         getConfig().unsetInterface();
         getConfig().unsetOperation();
      }
      else
      {
         getConfig().setInterface( operation.getInterface().getName() );
         getConfig().setOperation( operation.getName() );
      }

      this.operation = operation;

      notifyPropertyChanged( OPERATION_PROPERTY, oldOperation, operation );
   }

   @Override
   public void beforeSave()
   {
      for( WsdlMockResponse mockResponse : responses )
         mockResponse.beforeSave();
   }

   private class InternalInterfaceListener extends InterfaceListenerAdapter
   {
      @Override
      public void operationUpdated( Operation operation )
      {
         if( operation == WsdlMockOperation.this.operation )
            getConfig().setOperation( operation.getName() );
      }

      @Override
      public void operationRemoved( Operation operation )
      {
         if( operation == WsdlMockOperation.this.operation )
            getMockService().removeMockOperation( WsdlMockOperation.this );
      }
   }

   private class InternalProjectListener extends ProjectListenerAdapter
   {
      @Override
      public void interfaceRemoved( Interface iface )
      {
         if( operation.getInterface() == iface )
            getMockService().removeMockOperation( WsdlMockOperation.this );
      }

      @Override
      public void interfaceUpdated( Interface iface )
      {
         if( operation.getInterface() == iface )
            getConfig().setInterface( iface.getName() );
      }
   }

   public boolean isOneWay()
   {
      return operation == null ? false : operation.isOneWay();
   }

   public boolean isNotification()
   {
      return operation == null ? false : operation.isNotification();
   }

   public boolean isSolicitResponse()
   {
      return operation == null ? false : operation.isSolicitResponse();
   }

   public boolean isUnidirectional()
   {
      return operation == null ? false : operation.isUnidirectional();
   }

   public boolean isBidirectional()
   {
      return !isUnidirectional();
   }

   public List<? extends ModelItem> getChildren()
   {
      return responses;
   }

   public void onStart()
   {
      currentDispatchIndex = 0;
   }

   private WsdlMockResponse getMatchingMockResponse( WsdlMockRequest request )
           throws DispatchException
   {
      try
      {
         XmlObject xmlObject = request.getRequestXmlObject();

         for( QueryValuePair pair : responseMap.keySet() )
         {
            log.debug( "Testing request for match: " + pair );

            XmlObject[] nodes = xmlObject.selectPath( pair.getQuery() );
            if( nodes != null && nodes.length > 0 )
            {
               // Only look at the first node.
               log.debug( "Comparing selected node " + nodes[0] +
                       " with the value " + pair.getValue() );
               XmlCursor cursor = nodes[0].newCursor();
               try
               {
                  if( pair.getValue().equals( cursor.getTextValue() ) )
                  {
                     log.debug( "Found a request with a matching query: +" +
                             request.getRequestContent() );

                     return responseMap.get( pair );
                  }
               }
               finally
               {
                  cursor.dispose();
               }
            }
         }

         return null;
//       throw new DispatchException("No request query matched");
      }
      catch( XmlException e )
      {
         throw new DispatchException( e );
      }
   }
}
