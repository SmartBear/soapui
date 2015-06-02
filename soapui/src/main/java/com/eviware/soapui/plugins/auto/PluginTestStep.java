package com.eviware.soapui.plugins.auto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AutoFactory
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginTestStep {
    String typeName();

    String name();

    String description();

    String iconPath() default "";
}
