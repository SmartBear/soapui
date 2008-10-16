/*
 * soapUI, copyright (C) 2004-2008 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

/*
 * soapUI, copyright (C) 2004-2008 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

/*
 * soapUI, copyright (C) 2004-2008 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import com.eviware.soapui.config.MockOperationQueryDispatchConfigConfig;
import com.eviware.soapui.impl.wsdl.mock.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

public class QueryMatchMockOperationDispatcher extends AbstractMockOperationDispatcher
{
   private MockOperationQueryDispatchConfigConfig conf;

   public QueryMatchMockOperationDispatcher( WsdlMockOperation mockOperation, XmlObject config )
   {
      super( mockOperation, config );

      try
      {
         conf = MockOperationQueryDispatchConfigConfig.Factory.parse( config.getDomNode() );
      }
      catch( XmlException e )
      {
         e.printStackTrace();
      }

   }

   public WsdlMockResponse selectMockResponse( WsdlMockRequest request, WsdlMockResult result ) throws DispatchException
   {
      try
      {
         XmlObject xmlObject = request.getRequestXmlObject();

         for( MockOperationQueryDispatchConfigConfig.Query query : conf.getQueryList() )
         {
            XmlObject[] nodes = xmlObject.selectPath( query.getPath() );
            if( nodes != null && nodes.length > 0 )
            {
               XmlCursor cursor = nodes[0].newCursor();
               try
               {
                  if( query.getValue().equals( cursor.getTextValue() ) )
                  {
                     return getMockOperation().getMockResponseByName( query.getResponse() );
                  }
               }
               finally
               {
                  cursor.dispose();
               }
            }
         }

         return null;
      }
      catch( XmlException e )
      {
         throw new DispatchException( e );
      }
   }

   public void addQuery( String name, String path, String value, String response )
   {
      MockOperationQueryDispatchConfigConfig.Query query = conf.addNewQuery();
      query.setName( name );
      query.setPath( path );
      query.setValue( value );
      query.setResponse( response );

      saveConfig();
   }

   private void saveConfig()
   {
      getConfig().set( conf );
   }

   public static class Factory implements MockOperationDispatchFactory
   {
      public MockOperationDispatcher build( XmlObject config, WsdlMockOperation mockOperation )
      {
         return new QueryMatchMockOperationDispatcher( mockOperation, config );
      }
   }

   class QueryValuePair
   {
      private String query;
      private String value;

      public QueryValuePair( String query, String value )
      {
         assert ( query != null );
         assert ( value != null );

         this.query = query;
         this.value = value;
      }

      public String getQuery()
      {
         return query;
      }

      public String getValue()
      {
         return value;
      }

      @Override
      public boolean equals( Object obj )
      {
         if( obj != null && obj.getClass() == this.getClass() )
         {
            if( obj == this )
            {
               return true;
            }

            QueryValuePair that = (QueryValuePair) obj;

            return ( query.equals( that.query ) && value.equals( that.value ) );
         }
         else
         {
            return false;
         }
      }

      @Override
      public int hashCode()
      {
         int hash = 7;
         hash = 31 * hash + query.hashCode();
         hash = 31 * hash + value.hashCode();

         return hash;
      }

      @Override
      public String toString()
      {
         StringBuffer sb = new StringBuffer();
         sb.append( query ).append( ':' ).append( value );

         return sb.toString();
      }
   }
}