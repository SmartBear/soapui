package com.eviware.soapui.plugins;

import com.eviware.soapui.support.action.SoapUIActionGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides additional Action configuration to action classes in a plugin.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionConfiguration {

    Class<? extends SoapUIActionGroup> actionGroup();

    public String groupId();

    public String name();

    String beforeAction() default "";

    String afterAction() default "";

    public String description() default "";

    public boolean defaultAction() default false;

    public String iconPath() default "";

    public String keyStroke() default "";

    String addBefore() default "";


}
