package com.eviware.soapui.impl.actions;


import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class NewEmptyProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "NewEmptyProjectAction";
    public static final MessageSupport messages = MessageSupport.getMessages(NewEmptyProjectAction.class);

    public NewEmptyProjectAction() {
        super(messages.get("Title"),messages.get("Description"));
    }

    @Override
    public void perform(WorkspaceImpl target, Object param) {
        try {
            WsdlProject project = target.createProject(ModelItemNamer.createName("Project", target.getProjectList()), null);
            UISupport.selectAndShow(project);
        } catch (SoapUIException e) {
            UISupport.showErrorMessage(e);
        }
    }
}
