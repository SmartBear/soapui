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

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.RestParamProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringList;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class RestUtils
{
   public static String[] extractTemplateParams( String path )
   {
      if( StringUtils.isNullOrEmpty( path ))
         return new String[0];

      StringList result = new StringList();

      int ix = path.indexOf( '{' );
      while( ix != -1 )
      {
         int endIx = path.indexOf( '}', ix );
         if( endIx == -1 )
            break;

         if( endIx > ix + 1 )
            result.add( path.substring( ix + 1, endIx ) );

         ix = path.indexOf( '{', ix + 1 );
      }

      return result.toStringArray();

   }

   public static String extractParams( URL param, XmlBeansRestParamsTestPropertyHolder params, boolean keepHost )
   {
      if( param == null || StringUtils.isNullOrEmpty( param.getPath() ))
         return "";

      String path = param.getPath();
      String[] items = path.split( "/" );

      int templateParamCount = 0;
      StringBuffer resultPath = new StringBuffer();

      for( int i = 0; i < items.length; i++ )
      {
         String item = items[i];
         try
         {
            String[] matrixParams = item.split( ";" );
            if( matrixParams.length > 0 )
            {
               item = matrixParams[0];
               for( int c = 1; c < matrixParams.length; c++ )
               {
                  String matrixParam = matrixParams[c];

                  int ix = matrixParam.indexOf( '=' );
                  if( ix == -1 )
                  {
                     String name = URLDecoder.decode( matrixParam, "Utf-8" );
                     if( !params.hasProperty( name ) )
                        params.addProperty( name ).setStyle( ParameterStyle.MATRIX );
                  }
                  else
                  {

                     String name = URLDecoder.decode( matrixParam.substring( 0, ix ), "Utf-8" );
                     RestParamProperty property = params.getProperty( name );
                     if( property == null )
                     {
                        property = params.addProperty( name );
                     }

                     property.setStyle( ParameterStyle.MATRIX );
                     property.setValue( URLDecoder.decode( matrixParam.substring( ix + 1 ), "Utf-8" ) );
                  }
               }
            }

            Integer.parseInt( item );

            String name = "param" + templateParamCount++;
            RestParamProperty property = params.getProperty( name );
            if( !params.hasProperty( name ) )
            {
               property = params.addProperty( name );
            }

            property.setStyle( ParameterStyle.TEMPLATE );
            property.setValue( item );

            item = "{" + property.getName() + "}";
         }
         catch( Exception e )
         {
         }

         if( StringUtils.hasContent( item ) )
            resultPath.append( '/' ).append( item );
      }

      String query = param.getQuery();
      if( StringUtils.hasContent( query ) )
      {
         items = query.split( "&" );
         for( String item : items )
         {
            try
            {
               int ix = item.indexOf( '=' );
               if( ix == -1 )
               {
                  String name = URLDecoder.decode( item, "Utf-8" );

                  if( !params.hasProperty( name ) )
                  {
                     params.addProperty( name ).setStyle( ParameterStyle.QUERY );
                  }
               }
               else
               {
                  String name = URLDecoder.decode( item.substring( 0, ix ), "Utf-8" );
                  RestParamProperty property = params.getProperty( name );
                  if( property == null )
                  {
                     property = params.addProperty( name );
                  }

                  property.setStyle( ParameterStyle.QUERY );
                  property.setValue( URLDecoder.decode( item.substring( ix + 1 ), "Utf-8" ) );
               }
            }
            catch( UnsupportedEncodingException e )
            {
               e.printStackTrace();
            }
         }
      }

      if( keepHost )
      {
         return Tools.getEndpointFromUrl( param ) + resultPath.toString();
      }

      return resultPath.toString();
   }
}
