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

package com.eviware.soapui.impl.wadl.inference.schema.particles;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Particle;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.Settings;
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.inferredSchema.MapEntryConfig;
import com.eviware.soapui.inferredSchema.ReferenceParticleConfig;

/**
 * A ReferenceParticle is a reference to a particle in another namespace. It may
 * be either an xs:element or an xs:attribute.
 * 
 * @author Dain Nilsson
 */
public class ReferenceParticle implements Particle
{
	private Schema schema;
	private Particle reference;
	private QName referenceQName;
	private Map<String, String> attributes;

	public ReferenceParticle( Schema schema, Particle reference )
	{
		this.schema = schema;
		this.reference = reference;
		referenceQName = reference.getName();
		attributes = new HashMap<String, String>();
	}

	public ReferenceParticle( ReferenceParticleConfig xml, Schema schema )
	{
		this.schema = schema;
		referenceQName = xml.getReference();
		attributes = new HashMap<String, String>();
		for( MapEntryConfig entry : xml.getAttributeList() )
		{
			attributes.put( entry.getKey(), entry.getValue() );
		}
	}

	public ReferenceParticleConfig save()
	{
		ReferenceParticleConfig xml = ReferenceParticleConfig.Factory.newInstance();
		xml.setReference( referenceQName );
		for( Map.Entry<String, String> entry : attributes.entrySet() )
		{
			MapEntryConfig mapEntry = xml.addNewAttribute();
			mapEntry.setKey( entry.getKey() );
			mapEntry.setValue( entry.getValue() );
		}
		return xml;
	}

	private Particle getReference()
	{
		if( reference == null )
		{
			reference = schema.getSystem().getSchemaForNamespace( referenceQName.getNamespaceURI() ).getParticle(
					referenceQName.getLocalPart() );
		}
		return reference;
	}

	public QName getName()
	{
		return referenceQName;
	}

	public String getAttribute( String key )
	{
		String value = attributes.get( key );
		if( ( key.equals( "minOccurs" ) || key.equals( "maxOccurs" ) ) && value == null )
			value = "1";
		return value;
	}

	public void setAttribute( String key, String value )
	{
		attributes.put( key, value );
	}

	public Type getType()
	{
		return null;
	}

	public void setType( Type type )
	{
	}

	public void validate( Context context ) throws XmlException
	{
		context.pushPath();
		getReference().validate( context );
		context.popPath();
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder( "<" + schema.getPrefixForNamespace( Settings.xsdns ) + ":"
				+ getReference().getPType() + " ref=\"" + schema.getPrefixForNamespace( referenceQName.getNamespaceURI() )
				+ ":" + referenceQName.getLocalPart() + "\"" );
		for( Map.Entry<String, String> entry : attributes.entrySet() )
			s.append( " " + entry.getKey() + "=\"" + entry.getValue() + "\"" );
		s.append( "/>" );
		return s.toString();
	}

	public Particle.ParticleType getPType()
	{
		return getReference().getPType();
	}

}
