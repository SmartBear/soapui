package com.eviware.soapui.support.components;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

public final class PropertyComponent
{
	@Nullable
	private final String property;
	@Nonnull
	private final JComponent component;

	public PropertyComponent( JComponent component )
	{
		this( null, component );
	}

	public PropertyComponent( String property, JComponent component )
	{
		this.property = property;

		Preconditions.checkNotNull( "You must provide a component", component );
		this.component = component;
	}

	public String getProperty()
	{
		return property;
	}

	public JComponent getComponent()
	{
		return component;
	}

	public boolean hasProperty()
	{
		return property != null;
	}
}