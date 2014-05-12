package com.eviware.soapui.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated class should be singled out as the configuration of the plugin. If more than one
 * class in a JAR file is annotated with this, the loading of the plugin will be aborted.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginConfiguration {
    String groupId();

    String name();

    String version();
}
