package com.eviware.soapui.impl.rest.support;

import javax.xml.namespace.QName;

public interface RestParameter
{
   String getName();

   void setName( String name );

   String getDescription();

   void setDescription( String description );

   XmlBeansRestParamsTestPropertyHolder.ParameterStyle getStyle();

   void setStyle( XmlBeansRestParamsTestPropertyHolder.ParameterStyle style );

   String getValue();

   void setValue( String value );

   boolean isReadOnly();

   String getDefaultValue();

   String[] getOptions();

   boolean getRequired();

   QName getType();

   void setOptions( String[] arg0 );

   void setRequired( boolean arg0 );

   void setType( QName arg0 );

   void setDefaultValue( String default1 );
}
