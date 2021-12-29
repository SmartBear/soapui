package com.smartbear.integrations.swaggerhub;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import javafx.application.Platform;

public class ReadFromSwaggerHubAction extends AbstractSoapUIAction<WsdlProject> {

    public ReadFromSwaggerHubAction() {
        super("Import From SwaggerHub", "Reads an API from SwaggerHub");
    }

    @Override
    public void perform(WsdlProject modelItem, Object o) {
        Platform.runLater(() -> {
            ImportFromHubDialog importFromHubDialog = new ImportFromHubDialog(modelItem);
            importFromHubDialog.showAndWait();
        });
    }
}
