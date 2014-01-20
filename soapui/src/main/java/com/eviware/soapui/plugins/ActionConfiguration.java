package com.eviware.soapui.plugins;

import com.eviware.soapui.support.action.SoapUIActionGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides additional Action configuration to action classes in a plugin.
 */
@Target( ElementType.TYPE)
@Retention( RetentionPolicy.RUNTIME)
public @interface ActionConfiguration
{
	Class<? extends SoapUIActionGroup> actionGroup();

	String keyStroke();




}
