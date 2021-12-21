package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLTestRequestInterface;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import java.awt.Component;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GraphQLRequestTestStepPanelBuilder extends EmptyPanelBuilder<GraphQLRequestTestStep> {
    @Override
    public DesktopPanel buildDesktopPanel(GraphQLRequestTestStep graphQLTestRequestTestStep) {
       return new GraphQLRequestTestStepDesktopPanel(graphQLTestRequestTestStep);
    }

    @Override
    public boolean hasDesktopPanel() {
        return true;
    }

    @Override
    public Component buildOverviewPanel(GraphQLRequestTestStep testStep) {
        GraphQLTestRequestInterface request = testStep.getTestRequest();
        JPropertiesTable<GraphQLTestRequestInterface> table = new JPropertiesTable<>(
                "GraphQL Request Properties");

        // basic properties
        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);
        table.addProperty("Encoding", "encoding", new String[]{null, StandardCharsets.UTF_8.name(), "iso-8859-1"});

        table.addProperty("Endpoint", "endpoint", true);
        table.addProperty("Timeout", "timeout", true);

        table.addProperty("Bind Address", "bindAddress", true);
        table.addProperty("Follow Redirects", "followRedirects", JPropertiesTable.BOOLEAN_OPTIONS);

        List<WssCrypto> keystores = new ArrayList<>((request.getTestStep().getTestCase().getTestSuite().getProject().getWssContainer().getCryptoList()));
        keystores.add(0, null);
        table.addProperty("SSL Keystore", "sslKeystore", keystores.toArray());

        // post-processing
        table.addProperty("Dump File", "dumpFile", true).setDescription("Dumps response message to specified file");
        table.addProperty("Max Size", "maxSize", true).setDescription("The maximum number of bytes to receive");
        table.addProperty("Discard Response", "discardResponse", JPropertiesTable.BOOLEAN_OPTIONS);

        table.setPropertyObject(request);

        return table;
    }

    @Override
    public boolean hasOverviewPanel() {
        return true;
    }
}
