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

package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.plugins.PluginProxies;
import com.eviware.soapui.plugins.auto.PluginResponseInspector;
import com.eviware.soapui.support.components.Inspector;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.registry.InspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by ole on 15/06/14.
 */
public class AutoResponseInspectorFactory extends AbstractSoapUIFactory<EditorInspector<?>> implements ResponseInspectorFactory {
    private final Class<? extends ModelItem> targetClass;
    private Class<Inspector> inspectorClass;
    private String inspectorId;

    public AutoResponseInspectorFactory(PluginResponseInspector annotation, Class<Inspector> inspectorClass) {
        super(InspectorFactory.class);
        this.inspectorClass = inspectorClass;
        targetClass = annotation.targetClass();
        inspectorId = annotation.inspectorId();
    }

    @Override
    public String getInspectorId() {
        return inspectorId;
    }

    @Override
    public EditorInspector<?> createResponseInspector(Editor<?> editor, ModelItem modelItem) {

        try {
            if( targetClass.isAssignableFrom( modelItem.getClass() ) ) {
                try {
                    Method appliesMethod = inspectorClass.getMethod("applies", targetClass);
                    if (appliesMethod != null) {
                        Object applies = appliesMethod.invoke(null, modelItem);
                        if (!Boolean.valueOf(String.valueOf(applies)))
                            return null;
                    }
                } catch (NoSuchMethodException e) {
                    // this is ok - ignore
                }

                Constructor<Inspector> constructor = inspectorClass.getConstructor(Editor.class, targetClass);
                EditorInspector<?> editorInspector = (EditorInspector<?>) constructor.newInstance(editor, modelItem);
                return PluginProxies.proxyIfApplicable(editorInspector);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return null;
    }
}
