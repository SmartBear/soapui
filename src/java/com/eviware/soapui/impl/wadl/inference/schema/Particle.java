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

package com.eviware.soapui.impl.wadl.inference.schema;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wadl.inference.schema.particles.AttributeParticle;
import com.eviware.soapui.impl.wadl.inference.schema.particles.ElementParticle;
import com.eviware.soapui.impl.wadl.inference.schema.particles.ReferenceParticle;
import com.eviware.soapui.inferredSchema.AttributeParticleConfig;
import com.eviware.soapui.inferredSchema.ElementParticleConfig;
import com.eviware.soapui.inferredSchema.ParticleConfig;
import com.eviware.soapui.inferredSchema.ReferenceParticleConfig;

/**
 * An attribute or element in the schema. Has a name, a type, and zero or more
 * attributes.
 * 
 * @author Dain Nilsson
 */
public interface Particle
{

	/**
	 * Get the QName of this Particle.
	 * 
	 * @return The QName describing the particles name and namespace.
	 */
	public QName getName();

	/**
	 * Get the ParticleType of the Particle, that is, attribute or element.
	 * 
	 * @return Returns the type of particle this is.
	 */
	public ParticleType getPType();

	/**
	 * Get the Type of the element or attribute that is described by this
	 * particle.
	 * 
	 * @return Returns the Type that corresponds to the particle.
	 */
	public Type getType();

	/**
	 * Set the Type of the element or attribute that is described by this
	 * particle.
	 * 
	 * @param type
	 *           The Type to set.
	 */
	public void setType( Type type );

	/**
	 * Get the attribute value that corresponds to the given name.
	 * 
	 * @param key
	 *           The name of the attribute to get the value for.
	 * @return Returns the value for the attribute.
	 */
	public String getAttribute( String key );

	/**
	 * Set an attribute.
	 * 
	 * @param key
	 *           The name of the attribute to set.
	 * @param value
	 *           The value to set.
	 */
	public void setAttribute( String key, String value );

	/**
	 * Validates an XML document contained in a given Context object, at the
	 * position specified by the cursor contained in same Context object.
	 * 
	 * @param context
	 *           A Context object containing the XML data to be validated, and
	 *           other needed contextual variables.
	 * @throws XmlException
	 *            On unresolvable validation error.
	 */
	public void validate( Context context ) throws XmlException;

	public ParticleConfig save();

	/**
	 * An enum representing one of two particle types, element or attribute.
	 * 
	 * @author Dain Nilsson
	 */
	public enum ParticleType
	{
		ATTRIBUTE( "attribute" ), ELEMENT( "element" );
		private final String name;

		ParticleType( String name )
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}
	};

	/**
	 * A static factory class for creating new instances.
	 * 
	 * @author Dain Nilsson
	 */
	public class Factory
	{

		/**
		 * Create a blank new Particle representing an xs:element.
		 * 
		 * @param schema
		 *           The Schema in which the element will live.
		 * @param name
		 *           A name to give the newly created element.
		 * @return Returns the newly created particle.
		 */
		public static Particle newElementInstance( Schema schema, String name )
		{
			return new ElementParticle( schema, name );
		}

		/**
		 * Create a blank new Particle representing an xs:attribute.
		 * 
		 * @param schema
		 *           The Schema in which the attribute will live.
		 * @param name
		 *           A name to give the newly created attribute.
		 * @return Returns the newly created particle.
		 */
		public static Particle newAttributeInstance( Schema schema, String name )
		{
			return new AttributeParticle( schema, name );
		}

		/**
		 * Create a blank new Particle representing a reference to an element or
		 * attribute within a separate namespace.
		 * 
		 * @param schema
		 *           The Schema in which the reference will live.
		 * @param reference
		 *           The Particle to create a reference to.
		 * @return Returns the newly created particle.
		 */
		public static Particle newReferenceInstance( Schema schema, Particle reference )
		{
			return new ReferenceParticle( schema, reference );
		}

		/**
		 * Constructs a Particle object using previously saved data.
		 * 
		 * @param xml
		 *           XmlObject to which data has previously been saved.
		 * @param schema
		 *           The Schema in which to place the newly constructed Particle.
		 * @return Returns the newly constructed Particle.
		 */
		public static Particle parse( ParticleConfig xml, Schema schema )
		{
			if( xml instanceof AttributeParticleConfig )
				return new AttributeParticle( ( AttributeParticleConfig )xml, schema );
			if( xml instanceof ElementParticleConfig )
				return new ElementParticle( ( ElementParticleConfig )xml, schema );
			if( xml instanceof ReferenceParticleConfig )
				return new ReferenceParticle( ( ReferenceParticleConfig )xml, schema );
			return null;
		}
	}
}
