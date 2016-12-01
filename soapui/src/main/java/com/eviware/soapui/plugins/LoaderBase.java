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

package com.eviware.soapui.plugins;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.plugins.auto.AutoFactory;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionGroup;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.StandaloneActionMapping;
import com.eviware.soapui.support.action.support.WrapperSoapUIAction;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.ListenerRegistry;
import org.apache.commons.lang.ObjectUtils;
import org.reflections.Reflections;
import org.reflections.adapters.JavaReflectionAdapter;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Action;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoaderBase {

    private static Logger logger = LoggerFactory.getLogger(LoaderBase.class);

    protected SoapUIFactoryRegistry factoryRegistry;
    protected SoapUIActionRegistry actionRegistry;
    protected ListenerRegistry listenerRegistry;

    public LoaderBase(ListenerRegistry listenerRegistry, SoapUIActionRegistry actionRegistry,
                      SoapUIFactoryRegistry factoryRegistry) {
        this.listenerRegistry = listenerRegistry;
        this.actionRegistry = actionRegistry;
        this.factoryRegistry = factoryRegistry;
    }

    protected Collection<? extends SoapUIFactory> loadFactories(Reflections jarFileScanner)
            throws IllegalAccessException, InstantiationException {
        Collection<SoapUIFactory> factories = new HashSet<SoapUIFactory>();

        Set<Class<?>> factoryClasses = jarFileScanner.getTypesAnnotatedWith(FactoryConfiguration.class);
        for (Class<?> factoryClass : factoryClasses) {
            if (!SoapUIFactory.class.isAssignableFrom(factoryClass)) {
                logger.warn("Class " + factoryClass + " is annotated with @FactoryConfiguration " +
                        "but does not implement SoapUIFactory");
            } else
                factories.add(createFactory((Class<SoapUIFactory>) factoryClass));
        }

        loadAutoFactories(jarFileScanner, factories);

        return registerFactories(factories);
    }

    protected SoapUIFactory createFactory(Class<SoapUIFactory> factoryClass) throws InstantiationException, IllegalAccessException {
        return createObject(factoryClass);
    }

    protected <T extends Object> T createObject(Class<T> objectClass) throws IllegalAccessException, InstantiationException {
        return PluginProxies.proxyIfApplicable(objectClass.newInstance());
    }

    protected Collection<? extends SoapUIFactory> registerFactories(Collection<? extends SoapUIFactory> factories) {
        for (SoapUIFactory factory : factories) {
            factoryRegistry.addFactory(factory.getFactoryType(), factory);
        }

        return factories;
    }

    protected void loadAutoFactories(Reflections jarFileScanner, Collection<SoapUIFactory> factories) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addUrls(ClasspathHelper.forClass(AutoFactory.class));
        builder.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());
        builder.addClassLoader(Thread.currentThread().getContextClassLoader());
        Reflections autoAnnotationFinder = new Reflections(builder);

        for (Class clazz : autoAnnotationFinder.getTypesAnnotatedWith(AutoFactory.class)) {
            if (clazz.isAnnotation() && clazz.getSimpleName().startsWith("Plugin")) {
                try {
                    String className = "Auto" + clazz.getSimpleName().substring(6) + "Factory";
                    Class<? extends SoapUIFactory> factoryClass = (Class<? extends SoapUIFactory>)
                            Class.forName(clazz.getPackage().getName() + ".factories." + className);
                    factories.addAll(findAutoFactoryObjects(jarFileScanner, clazz, factoryClass));
                } catch (ClassNotFoundException e) {
                    SoapUI.logError(e);
                }
            }
        }
    }

    protected Collection<SoapUIFactory> findAutoFactoryObjects(Reflections jarFileScanner, Class<? extends Annotation> annotationType,
                                                               Class<? extends SoapUIFactory> factoryClass) {

        Collection<SoapUIFactory> factories = new HashSet<SoapUIFactory>();
        Set<Class<?>> objectClasses = jarFileScanner.getTypesAnnotatedWith(annotationType);

        for (Class<?> clazz : objectClasses) {
            try {

                Annotation annotation = clazz.getAnnotation(annotationType);
                factories.add(createAutoFactory(annotationType, factoryClass, clazz, annotation));
                SoapUI.log("Added AutoFactory for [" + annotationType.getSimpleName() + "]");
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }

        return factories;
    }

    protected SoapUIFactory createAutoFactory(Class<? extends Annotation> annotationType, Class<? extends SoapUIFactory> factoryClass, Class<?> clazz, Annotation annotation) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
        return factoryClass.getConstructor(annotationType, clazz.getClass()).newInstance(annotation, clazz);
    }

    protected List<Class<? extends SoapUIListener>> registerListeners(List<Class<? extends SoapUIListener>> listeners) {
        for (Class<?> listenerClass : listeners) {

            Class currentListenerClass = listenerClass;
            while (currentListenerClass != null) {
                for (Class<?> implementedInterface : currentListenerClass.getInterfaces()) {
                    if (SoapUIListener.class.isAssignableFrom(implementedInterface)) {
                        listenerRegistry.addListener(implementedInterface, listenerClass, null);
                    }
                }

                currentListenerClass = currentListenerClass.getSuperclass();
            }
        }

        return listeners;
    }

    protected List<Class<? extends SoapUIListener>> loadListeners(Reflections jarFileScanner) throws IllegalAccessException, InstantiationException {
        List<Class<? extends SoapUIListener>> listeners = new ArrayList<Class<? extends SoapUIListener>>();

        Set<Class<?>> listenerClasses = jarFileScanner.getTypesAnnotatedWith(ListenerConfiguration.class);
        for (Class<?> listenerClass : listenerClasses) {
            if (!SoapUIListener.class.isAssignableFrom(listenerClass)) {
                logger.warn("Class " + listenerClass + " is annotated with @ListenerConfiguration " +
                        "but does not implement SoapUIListener");
            } else {
                listeners.add(((Class<SoapUIListener>) listenerClass));
            }
        }

        return registerListeners(listeners);
    }

    protected List<? extends SoapUIAction> registerActions(List<? extends SoapUIAction> actions) {
        // sort actions so references work consistently
        Collections.sort(actions, new Comparator<SoapUIAction>() {
            @Override
            public int compare(SoapUIAction o1, SoapUIAction o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        for (SoapUIAction action : actions) {
            actionRegistry.addAction(action.getId(), action);
            ActionConfiguration configuration = PluginProxies.getAnnotation(action, ActionConfiguration.class);
            if (configuration != null) {
                configureAction(action, configuration);
            }

            ActionConfigurations configurations = PluginProxies.getAnnotation(action, ActionConfigurations.class);
            if (configurations != null) {
                for (ActionConfiguration config : configurations.configurations()) {
                    configureAction(action, config);
                }
            }
        }

        return actions;
    }

    protected List<? extends SoapUIActionGroup> registerActionGroups(List<SoapUIActionGroup> actionGroups) {

        for (SoapUIActionGroup actionGroup : actionGroups)
            actionRegistry.addActionGroup(actionGroup);

        return actionGroups;
    }

    protected List<? extends SoapUIActionGroup> loadActionGroups(Reflections jarFileScanner) throws InstantiationException, IllegalAccessException {
        List<SoapUIActionGroup> actionGroups = new ArrayList<SoapUIActionGroup>();
        Set<Class<?>> actionGroupClasses = jarFileScanner.getTypesAnnotatedWith(ActionGroup.class);
        for (Class<?> actionGroupClass : actionGroupClasses) {
            if (!SoapUIActionGroup.class.isAssignableFrom(actionGroupClass)) {
                logger.warn("Class " + actionGroupClass + " is annotated with @ActionGroup " +
                        "but does not implement SoapUIActionGroup");
            } else {
                ActionGroup annotation = actionGroupClass.getAnnotation(ActionGroup.class);
                SoapUIActionGroup actionGroup = createActionGroup((Class<SoapUIActionGroup>) actionGroupClass);

                for (ActionMapping mapping : annotation.actions()) {
                    try {
                        if (mapping.type() == ActionMapping.Type.SEPARATOR) {
                            actionGroup.addMapping(
                                    SoapUIActionRegistry.SeperatorAction.SOAPUI_ACTION_ID,
                                    SoapUIActionRegistry.SeperatorAction.getDefaultMapping());
                        } else if (mapping.type() == ActionMapping.Type.GROUP || mapping.type() == ActionMapping.Type.INSERT) {
                            SoapUIActionRegistry.SoapUIActionGroupAction actionListAction = new SoapUIActionRegistry.SoapUIActionGroupAction(mapping.name(),
                                    mapping.description(), mapping.groupId());
                            actionListAction.setInsert(mapping.type() == ActionMapping.Type.INSERT);
                            StandaloneActionMapping actionMapping = new StandaloneActionMapping(actionListAction);
                            actionGroup.addMapping(mapping.groupId(), actionMapping);
                        } else if (mapping.type() == ActionMapping.Type.ACTION) {
                            Class<?> actionClass = mapping.actionClass();
                            String actionId = mapping.actionId();

                            if (actionClass != ObjectUtils.Null.class) {
                                if (SoapUIAction.class.isAssignableFrom(actionClass)) {
                                    SoapUIAction action = createAction((Class<SoapUIAction>) actionClass);
                                    actionRegistry.addAction(action.getId(), action);
                                    actionId = action.getId();
                                } else if (Action.class.isAssignableFrom(actionClass)) {
                                    Action swingAction = createObject((Class<Action>) actionClass);
                                    SoapUIAction action = new WrapperSoapUIAction(swingAction);
                                    actionRegistry.addAction(action.getId(), action);
                                    actionId = action.getId();
                                }
                            }

                            DefaultActionMapping actionMapping = new DefaultActionMapping(actionId,
                                    mapping.keyStroke(), mapping.iconPath(), actionId.equals(
                                    annotation.defaultAction()), mapping.param());
                            actionMapping.setToolbarAction(mapping.isToolbarAction());
                            actionMapping.setToolbarIndex(mapping.toolbarIndex());
                            if (!mapping.name().equals("")) {
                                actionMapping.setName(mapping.name());
                            }

                            if (!mapping.description().equals("")) {
                                actionMapping.setDescription(mapping.description());
                            }

                            actionGroup.addMapping(actionId, actionMapping);
                        }
                    } catch (Throwable e) {
                        logger.error("Error adding actionMapping", e);
                    }
                }

                actionGroups.add(actionGroup);
            }
        }

        return registerActionGroups(actionGroups);
    }

    protected List<? extends SoapUIAction> loadActions(Reflections jarFileScanner) throws InstantiationException, IllegalAccessException {
        List<SoapUIAction> actions = new ArrayList<SoapUIAction>();
        Set<Class<?>> actionClasses = jarFileScanner.getTypesAnnotatedWith(ActionConfiguration.class);
        for (Class<?> actionClass : actionClasses) {
            if (!SoapUIAction.class.isAssignableFrom(actionClass)) {
                logger.error("Class " + actionClass + " is annotated with @ActionConfiguration " +
                        "but does not implement SoapUIAction");
            } else {
                actions.add(createAction((Class<SoapUIAction>) actionClass));
            }
        }

        actionClasses = jarFileScanner.getTypesAnnotatedWith(ActionConfigurations.class);
        for (Class<?> actionClass : actionClasses) {
            if (!SoapUIAction.class.isAssignableFrom(actionClass)) {
                logger.error("Class " + actionClass + " is annotated with @ActionConfigurations " +
                        "but does not implement SoapUIAction");
            } else {
                actions.add(createAction((Class<SoapUIAction>) actionClass));
            }
        }

        return registerActions(actions);
    }

    protected SoapUIAction createAction(Class<SoapUIAction> actionClass) throws InstantiationException, IllegalAccessException {
        return createObject(actionClass);
    }

    protected SoapUIActionGroup createActionGroup(Class<SoapUIActionGroup> actionGroupClass) throws InstantiationException, IllegalAccessException {
        return createObject(actionGroupClass);
    }

    protected void configureAction(final SoapUIAction action, ActionConfiguration configuration) {
        String groupId = configuration.actionGroup();
        SoapUIActionGroup targetGroup = actionRegistry.getActionGroup(groupId);
        if (targetGroup == null) {
            targetGroup = new DefaultSoapUIActionGroup(groupId, groupId);
            actionRegistry.addActionGroup(targetGroup);
        }

        DefaultActionMapping mapping = new DefaultActionMapping(action.getId(), configuration.keyStroke(),
                configuration.iconPath(), configuration.defaultAction(), null);
        mapping.setName(action.getName());
        mapping.setDescription(configuration.description());
        mapping.setToolbarAction(configuration.isToolbarAction());

        int insertIndex = -1;
        if (StringUtils.hasContent(configuration.beforeAction())) {
            insertIndex = targetGroup.getMappingIndex(configuration.beforeAction());
        } else if (StringUtils.hasContent(configuration.afterAction())) {
            insertIndex = targetGroup.getMappingIndex(configuration.afterAction()) + 1;
        }

        if (configuration.separatorBefore()) {
            targetGroup.addMapping(SoapUIActionRegistry.SeperatorAction.SOAPUI_ACTION_ID,
                    insertIndex++, SoapUIActionRegistry.SeperatorAction.getDefaultMapping());
        }

        targetGroup.addMapping(action.getId(), insertIndex, mapping);

        if (configuration.separatorAfter()) {
            targetGroup.addMapping(SoapUIActionRegistry.SeperatorAction.SOAPUI_ACTION_ID,
                    ++insertIndex, SoapUIActionRegistry.SeperatorAction.getDefaultMapping());
        }
    }
    protected void unregisterListeners(List<Class<? extends SoapUIListener>> listeners) {
        for (Class<? extends SoapUIListener> listenerClass : listeners) {
            for (Class<?> implementedInterface : listenerClass.getInterfaces()) {
                if (SoapUIListener.class.isAssignableFrom(implementedInterface)) {
                    listenerRegistry.removeListener(implementedInterface, listenerClass);
                    listenerRegistry.removeSingletonListener(implementedInterface, listenerClass);
                }
            }
        }
    }

    protected void unregisterFactories(Collection<? extends SoapUIFactory> factories) {
        for (SoapUIFactory soapUIFactory : factories) {
            factoryRegistry.removeFactory(soapUIFactory.getFactoryType(), soapUIFactory);
        }
    }

    protected void unregisterActions(List<? extends SoapUIAction> actions) {
        for (SoapUIAction soapUIAction : actions) {
            actionRegistry.removeAction(soapUIAction.getId());
        }
    }

    private boolean isToolbarAction(SoapUIAction soapUIAction) {
        ActionConfiguration annotation = PluginProxies.getAnnotation(soapUIAction, ActionConfiguration.class);
        return annotation != null && StringUtils.hasContent(annotation.toolbarIcon());
    }

    // due to Reflections internals (or my misunderstanding of them) this class has to be
    // named as its superclass
    protected static class TypeAnnotationsScanner extends org.reflections.scanners.TypeAnnotationsScanner {
        @Override
        public boolean acceptsInput(String file) {
            if (file.endsWith(".groovy")) {
                return true;
            } else {
                return super.acceptsInput(file);
            }
        }
    }

    // loads both groovy and java classes for Reflections package
    protected static class GroovyAndJavaReflectionAdapter extends JavaReflectionAdapter {

        private final JarClassLoader jarClassLoader;

        public GroovyAndJavaReflectionAdapter(JarClassLoader jarClassLoader) {
            this.jarClassLoader = jarClassLoader;
        }

        @Override
        public Class getOfCreateClassObject(Vfs.File file) throws Exception {
            if (file.getName().endsWith(".groovy")) {
                return jarClassLoader.loadScriptClass(file.getRelativePath());
            } else {
                return super.getOfCreateClassObject(file, jarClassLoader);
            }
        }
    }
}
