package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aleshin on 5/8/2014.
 */
public class RenameInterfaceAction extends AbstractSoapUIAction<WsdlInterface> {

    public RenameInterfaceAction(){
        super("Rename Interface", "Renames this Interface");
    }

    public void perform(WsdlInterface iface, Object param) {

        String newInterfaceName = UISupport.prompt("Please specify a new name of the selected interface", "Rename Interface", iface.getName());
        if (newInterfaceName == null) {
            return;
        }
        iface.setName(newInterfaceName);
    }

}
