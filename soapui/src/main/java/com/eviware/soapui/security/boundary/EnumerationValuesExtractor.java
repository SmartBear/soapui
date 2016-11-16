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

package com.eviware.soapui.security.boundary;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * NOT USED ANY MORE
 */
public class EnumerationValuesExtractor {
    private WsdlRequest request;
    // private List<String> enumerationParameters = new ArrayList<String>();
    private List<String> selectedEnumerationParameters = new ArrayList<String>();

    private XmlObjectTreeModel model;

    public EnumerationValuesExtractor(WsdlTestRequest request) {
        this.request = request;
        try {
            // model = new XmlObjectTreeModel(
            // request.getOperation().getInterface().getDefinitionContext()
            // .getSchemaTypeSystem(), XmlObject.Factory.parse(
            // request.getRequestContent() ) );
            model = new XmlObjectTreeModel(request.getOperation().getInterface().getDefinitionContext()
                    .getSchemaTypeSystem(), XmlUtils.createXmlObject(request.getRequestContent()));
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        // extractEnumerationParameters( model.getRootNode() );
    }

    public String extract() throws XmlException, Exception {

        getNextChild(model.getRootNode());

        return model.getXmlObject().toString();
    }

    private void getNextChild(XmlTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            XmlTreeNode mynode = node.getChild(i);

            if (mynode.getSchemaType() != null && mynode.getSchemaType().getEnumerationValues() != null
                    && mynode.getSchemaType().getEnumerationValues().length > 0) {
                EnumerationValues nodeInfo = new EnumerationValues(mynode.getSchemaType().getBaseType().getShortJavaName());
                for (XmlAnySimpleType s : mynode.getSchemaType().getEnumerationValues()) {
                    nodeInfo.addValue(s.getStringValue());
                }

                updateNodeValue(mynode, nodeInfo);

            }
            getNextChild(mynode);
        }
    }

    // private void extractEnumerationParameters( XmlTreeNode node )
    // {
    // for( int i = 0; i < node.getChildCount(); i++ )
    // {
    // XmlTreeNode mynode = node.getChild( i );
    //
    // if( mynode.getSchemaType() != null &&
    // mynode.getSchemaType().getEnumerationValues() != null
    // && mynode.getSchemaType().getEnumerationValues().length > 0 )
    // {
    // enumerationParameters.add( mynode.getDomNode().getLocalName() );
    // }
    // extractEnumerationParameters( mynode );
    // }
    // }

    private void updateNodeValue(XmlTreeNode mynode, EnumerationValues enumerationValues) {
        if (!selectedEnumerationParameters.contains(mynode.getDomNode().getLocalName())) {
            return;
        }

        int size = maxLengthStringSize(enumerationValues.getValuesList());
        String value = createOutOfBoundaryValue(enumerationValues, size);
        if (value != null) {
            mynode.setValue(1, value);
        }
    }

    private String createOutOfBoundaryValue(EnumerationValues enumValues, int size) {
        if ("XmlString".equals(enumValues.getType())) {
            String value = null;
            do {
                value = BoundaryUtils.createCharacterArray(StringBoundary.AVAILABLE_VALUES, size);
            }
            while (enumValues.getValuesList().contains(value));
            return value;
        }
        return null;
    }

    private int maxLengthStringSize(Collection<String> values) {
        int max = 0;
        for (String str : values) {
            if (max < str.length()) {
                max = str.length();
            }
        }
        return max;
    }

    class EnumerationValues {
        private String type;
        private List<String> valuesList = new ArrayList<String>();

        public EnumerationValues(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void addValue(String value) {
            valuesList.add(value);
        }

        public List<String> getValuesList() {
            return valuesList;
        }

    }

    // public List<String> getEnumerationParameters()
    // {
    // return enumerationParameters;
    // }
    //
    // public void setEnumerationParameters( List<String> enumerationParameters )
    // {
    // this.enumerationParameters = enumerationParameters;
    // }

    public void setSelectedEnumerationParameters(List<String> selectedEnumerationParameters) {
        this.selectedEnumerationParameters = selectedEnumerationParameters;
    }

    public List<String> getSelectedEnumerationParameters() {
        return selectedEnumerationParameters;
    }
}
