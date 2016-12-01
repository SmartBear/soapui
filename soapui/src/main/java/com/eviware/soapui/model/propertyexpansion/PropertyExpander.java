/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.model.propertyexpansion;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.resolvers.ContextPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.DynamicPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.EvalPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.GlobalPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.MockRunPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.ModelItemPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.PropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.PropertyResolverFactory;
import com.eviware.soapui.model.propertyexpansion.resolvers.SubmitPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.TestRunPropertyResolver;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.settings.GlobalPropertySettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistryListener;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that can expand properties using property resolvers
 *
 * @author ole
 */

public class PropertyExpander implements SoapUIFactoryRegistryListener {
    private List<PropertyResolver> propertyResolvers = new ArrayList<PropertyResolver>();
    private static List<PropertyResolver> defaultResolvers = new ArrayList<PropertyResolver>();
    private static PropertyExpander defaultExpander;
    private static boolean debuggingMode;
    private static Map<String, StringToStringMap> debuggingExpandedProperties;

    static {
        // add default resolvers - this should be read from some external config
        // in the future
        defaultResolvers.add(new ModelItemPropertyResolver());
        defaultResolvers.add(new TestRunPropertyResolver());
        defaultResolvers.add(new MockRunPropertyResolver());
        defaultResolvers.add(new SubmitPropertyResolver());
        defaultResolvers.add(new ContextPropertyResolver());
        defaultResolvers.add(new DynamicPropertyResolver());
        defaultResolvers.add(new GlobalPropertyResolver());
        defaultResolvers.add(new EvalPropertyResolver());


        defaultExpander = new PropertyExpander(true);

        for (PropertyResolverFactory factory : SoapUI.getFactoryRegistry().getFactories(PropertyResolverFactory.class)) {
            defaultExpander.addResolverFactory(factory);
        }

        // WORKAROUND: eliminates a potential problem with a circular dependency between HttpClientSupport and this class
        ProxyUtils.setGlobalProxy(SoapUI.getSettings());
        debuggingExpandedProperties = new HashMap<String, StringToStringMap>();
    }

    public PropertyExpander(boolean addDefaultResolvers) {
        if (addDefaultResolvers) {
            propertyResolvers.addAll(defaultResolvers);

            SoapUI.getFactoryRegistry().addFactoryRegistryListener( this );
        }
    }

    public static PropertyExpander getDefaultExpander() {
        return defaultExpander;
    }

    @Deprecated
    public static void addDefaultResolver(PropertyResolver resolver) {
        defaultResolvers.add(resolver);
        defaultExpander.addResolver(resolver);
    }

    public void addResolver(PropertyResolver propertyResolver) {
        propertyResolvers.add(propertyResolver);
    }

    public void addResolverFactory( PropertyResolverFactory factory )
    {
        PropertyResolver resolver = factory.createPropertyResolver();
        addResolver( resolver );

        resolverFactories.put( factory, resolver );
    }

    private Map<PropertyResolverFactory,PropertyResolver> resolverFactories = new HashMap<PropertyResolverFactory, PropertyResolver>();

    public void removeResolverFactory( PropertyResolverFactory factory )
    {
        if( resolverFactories.containsKey( factory )) {
            removeResolver( resolverFactories.get( factory ));
            resolverFactories.remove(factory);
        }
    }

    private void removeResolver(PropertyResolver propertyResolver) {
        propertyResolvers.remove( propertyResolver );
    }

    public static String expandProperties(String content) {
        return defaultExpander.expand(content);
    }

    public static String expandProperties(PropertyExpansionContext context, String content) {
        return defaultExpander.expand(context, content, false);
    }

    public static String expandProperties(PropertyExpansionContext context, String content, boolean entitize) {
        return defaultExpander.expand(context, content, entitize);
    }

    public String expand(String content) {
        return expand(new PropertyExpansionUtils.GlobalPropertyExpansionContext(), content, false);
    }

    public String expand(PropertyExpansionContext context, String content) {
        return expand(context, content, false);
    }

    public String expand(PropertyExpansionContext context, String content, boolean entitize) {
        SoapUIClassLoaderState clState = SoapUIExtensionClassLoader.ensure();

        try {

            if (StringUtils.isNullOrEmpty(content)) {
                return content;
            }

            int ix = content.indexOf("${");
            if (ix == -1) {
                return content;
            }

            StringBuffer buf = new StringBuffer();
            int lastIx = 0;
            while (ix != -1) {
                if (ix > lastIx && content.charAt(ix - 1) == '$') {
                    buf.append(content.substring(lastIx, ix - 1));
                    lastIx = ix;
                    ix = content.indexOf("${", lastIx + 1);
                    continue;
                }

                if (ix > lastIx) {
                    buf.append(content.substring(lastIx, ix));
                }

                int ix2 = content.indexOf('}', ix + 2);
                if (ix2 == -1) {
                    break;
                }

                // check for nesting
                int ix3 = content.lastIndexOf("${", ix2);
                if (ix3 != ix) {
                    // buf.append( content.substring( ix, ix3 ));
                    content = content.substring(0, ix3) + expand(context, content.substring(ix3, ix2 + 1))
                            + content.substring(ix2 + 1);

                    lastIx = ix;
                    continue;
                }

                String propertyName = content.substring(ix + 2, ix2);
                String propertyValue = null;

                if (StringUtils.hasContent(propertyName)) {
                    boolean globalOverrideEnabled = SoapUI.getSettings().getBoolean(GlobalPropertySettings.ENABLE_OVERRIDE);

                    for (int c = 0; c < propertyResolvers.size() && propertyValue == null; c++) {
                        propertyValue = propertyResolvers.get(c).resolveProperty(context, propertyName,
                                globalOverrideEnabled);
                    }
                }

                // found a value?
                if (propertyValue != null) {
                    if (!content.equals(propertyValue)) {
                        propertyValue = expand(context, propertyValue);
                    }

                    if (entitize) {
                        propertyValue = XmlUtils.entitize(propertyValue);
                    }

                    TestCase testCase = ModelSupport.getModelItemTestCase(context.getModelItem());
                    if (debuggingMode && testCase != null) {
                        StringToStringMap props = debuggingExpandedProperties.get(testCase.getId());
                        if (props == null) {
                            props = new StringToStringMap();
                        }
                        props.put(propertyName, propertyValue);
                        debuggingExpandedProperties.put(testCase.getId(), props);
                    }
                    buf.append(propertyValue);
                } else {
                    // if( log.isEnabledFor( Priority.WARN ))
                    // log.warn( "Missing property value for [" + propertyName + "]"
                    // );

                    // buf.append( "${" ).append( propertyName ).append( '}' );
                }

                lastIx = ix2 + 1;
                ix = content.indexOf("${", lastIx);
            }

            if (lastIx < content.length()) {
                buf.append(content.substring(lastIx));
            }

            return buf.toString();
        } finally {
            clState.restore();
        }
    }

    public String expand(ModelItem contextModelItem, String content) {
        return expand(new DefaultPropertyExpansionContext(contextModelItem), content);
    }

    public static String expandProperties(ModelItem contextModelItem, String content) {
        return defaultExpander.expand(contextModelItem, content);
    }

    public static void setDebuggingMode(String testCaseId, boolean debug) {
        debuggingMode = debug;
        if (debug) {
            if (debuggingExpandedProperties.get(testCaseId) == null) {
                debuggingExpandedProperties.put(testCaseId, new StringToStringMap());
            }
        } else {
            if (debuggingExpandedProperties.get(testCaseId) != null) {
                debuggingExpandedProperties.remove(testCaseId);
            }

        }
    }

    public static StringToStringMap getDebuggingExpandedProperties(String testCaseId) {
        return debuggingExpandedProperties.get(testCaseId);
    }

    public static void clearDebuggingExpandedProperties(String testCaseId) {
        debuggingExpandedProperties.remove(testCaseId);
    }

    @Override
    public void factoryAdded(Class<?> factoryType, Object factory) {
        if( factory instanceof PropertyResolverFactory )
            addResolverFactory((PropertyResolverFactory) factory);
    }

    @Override
    public void factoryRemoved(Class<?> factoryType, Object factory) {
        if( factory instanceof PropertyResolverFactory )
            removeResolverFactory((PropertyResolverFactory) factory);
    }
}
