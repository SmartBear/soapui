package com.eviware.soapui.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that plugin defined by the annotated class has a dependency on another plugin.
 * This annotation should only ever be used on classes annotated with <code>@PluginConfiguration</code>.
 * @see PluginConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginDependency {
    String groupId();

    String name();

    String minimumVersion() default "0.0";

}
