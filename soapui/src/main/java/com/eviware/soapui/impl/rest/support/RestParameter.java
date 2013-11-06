/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;

import javax.xml.namespace.QName;

public interface RestParameter
{
	String getName();

	void setName( String name );

	String getDescription();

	void setDescription( String description );

	XmlBeansRestParamsTestPropertyHolder.ParameterStyle getStyle();

	void setStyle( XmlBeansRestParamsTestPropertyHolder.ParameterStyle style );

	NewRestResourceActionBase.ParamLocation getParamLocation();

	void  setParamLocation( NewRestResourceActionBase.ParamLocation paramLocation );

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

	String getPath();

	void setPath( String path );
}
