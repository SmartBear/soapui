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
import com.eviware.soapui.plugins.auto.PluginResponseEditorView;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.EditorViewFactory;
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by ole on 15/06/14.
 */
public class AutoResponseEditorViewFactory extends AbstractSoapUIFactory<EditorView<?>> implements ResponseEditorViewFactory {
    private final Class<? extends ModelItem> targetClass;
    private Class<EditorView> editorViewClass;
    private String viewId;

    public AutoResponseEditorViewFactory(PluginResponseEditorView annotation, Class<EditorView> editorViewClass) {
        super(EditorViewFactory.class);
        this.editorViewClass = editorViewClass;
        targetClass = annotation.targetClass();
        viewId = annotation.viewId();
    }

    @Override
    public String getViewId() {
        return viewId;
    }

    @Override
    public EditorView<?> createResponseEditorView(Editor<?> editor, ModelItem modelItem) {

        try {
            if( targetClass.isAssignableFrom( modelItem.getClass() ) ) {
                try {
                    Method appliesMethod = editorViewClass.getMethod("applies", targetClass);
                    if (appliesMethod != null) {
                        Object applies = appliesMethod.invoke(null, modelItem);
                        if (!Boolean.valueOf(String.valueOf(applies)))
                            return null;
                    }
                } catch (NoSuchMethodException e) {
                    // this is ok - ignore
                }

                Constructor<EditorView> constructor = editorViewClass.getConstructor(Editor.class, targetClass);
                EditorView editorView = constructor.newInstance(editor, modelItem);
                return PluginProxies.proxyIfApplicable(editorView);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return null;
    }
}
