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

package com.eviware.soapui.support.editor.registry;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.panels.request.views.html.HttpHtmlResponseViewFactory;
import com.eviware.soapui.impl.rest.panels.request.views.json.JsonResponseViewFactory;
import com.eviware.soapui.impl.support.http.HttpRequestContentViewFactory;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorViewFactory;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistryListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Registry of available XmlViews
 *
 * @author ole.matzura
 */

public class EditorViewFactoryRegistry implements SoapUIFactoryRegistryListener {
    private static EditorViewFactoryRegistry instance;
    private List<EditorViewFactory> factories = new ArrayList<>();

    public EditorViewFactoryRegistry() {
        // this should obviously come from a configuration file..
        addFactory(new XmlSourceEditorViewFactory());
        // addFactory( new RestRequestParamsViewFactory() );
        addFactory(new HttpRequestContentViewFactory());
        addFactory(new JsonResponseViewFactory());
        addFactory(new HttpHtmlResponseViewFactory());
        addFactory(new RawXmlEditorFactory());

        for (EditorViewFactory factory : SoapUI.getFactoryRegistry().getFactories(EditorViewFactory.class)) {
            addFactory(factory);
        }

        SoapUI.getFactoryRegistry().addFactoryRegistryListener(this);
    }

    public void addFactory(EditorViewFactory factory) {
        factories.add(factory);
    }

    public void setFactory(String viewId, EditorViewFactory factory) {
        for (int c = 0; c < factories.size(); c++) {
            if (factories.get(c).getViewId().equals(viewId)) {
                factories.set(c, factory);
            }
        }
    }

    public static final EditorViewFactoryRegistry getInstance() {
        if (instance == null) {
            instance = new EditorViewFactoryRegistry();
        }

        return instance;
    }

    public EditorViewFactory[] getFactories() {
        return factories.toArray(new EditorViewFactory[factories.size()]);
    }

    public EditorViewFactory[] getFactoriesOfType(Class<?> type) {
        List<EditorViewFactory> result = new ArrayList<>();
        for (EditorViewFactory factory : factories) {
            if (Arrays.asList(factory.getClass().getInterfaces()).contains(type)) {
                result.add(factory);
            }
        }

        return result.toArray(new EditorViewFactory[result.size()]);
    }

    public void removeFactory(EditorViewFactory factory) {
        factories.remove(factory);
    }

    @Override
    public void factoryAdded(Class<?> factoryType, Object factory) {
        if (factoryType.isAssignableFrom(EditorViewFactory.class)) {
            addFactory((EditorViewFactory) factory);
        }
    }

    @Override
    public void factoryRemoved(Class<?> factoryType, Object factory) {
        if (factoryType.isAssignableFrom(EditorViewFactory.class)) {
            removeFactory((EditorViewFactory) factory);
        }
    }
}
