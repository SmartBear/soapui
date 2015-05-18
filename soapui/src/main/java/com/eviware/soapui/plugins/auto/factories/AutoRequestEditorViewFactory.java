package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.plugins.PluginProxies;
import com.eviware.soapui.plugins.auto.PluginRequestEditorView;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.EditorViewFactory;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by ole on 15/06/14.
 */
public class AutoRequestEditorViewFactory extends AbstractSoapUIFactory<EditorView<?>> implements RequestEditorViewFactory {
    private final String viewId;
    private final Class<? extends ModelItem> targetClass;
    private Class<EditorView> editorViewClass;

    public AutoRequestEditorViewFactory(PluginRequestEditorView annotation, Class<EditorView> editorViewClass) {
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
    public EditorView<?> createRequestEditorView(Editor<?> editor, ModelItem modelItem) {

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
                EditorView editorViewToReturn = constructor.newInstance(editor, modelItem);
                //injectMembers(editorViewToReturn);
                return PluginProxies.proxyIfApplicable(editorViewToReturn);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return null;
    }

}
