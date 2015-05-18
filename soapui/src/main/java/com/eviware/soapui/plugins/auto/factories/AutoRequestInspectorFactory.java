package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.plugins.PluginProxies;
import com.eviware.soapui.plugins.auto.PluginRequestInspector;
import com.eviware.soapui.support.components.Inspector;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.registry.InspectorFactory;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by ole on 15/06/14.
 */
public class AutoRequestInspectorFactory extends AbstractSoapUIFactory<EditorInspector<?>> implements RequestInspectorFactory {
    private final Class<? extends ModelItem> targetClass;
    private String inspectorId;
    private Class<Inspector> inspectorClass;

    public AutoRequestInspectorFactory(PluginRequestInspector annotation, Class<Inspector> inspectorClass) {
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
    public EditorInspector<?> createRequestInspector(Editor<?> editor, ModelItem modelItem) {

        try {
            if (targetClass.isAssignableFrom(modelItem.getClass())) {

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
                EditorInspector<?> inspectorToReturn = (EditorInspector<?>) constructor.newInstance(editor, modelItem);
                //injectMembers(inspectorToReturn);
                return PluginProxies.proxyIfApplicable(inspectorToReturn);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return null;
    }
}
