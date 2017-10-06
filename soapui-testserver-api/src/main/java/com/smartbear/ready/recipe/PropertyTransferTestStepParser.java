package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.PathLanguage;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.PropertyTransfersStepFactory;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.smartbear.ready.recipe.teststeps.PropertyTransferStruct;
import com.smartbear.ready.recipe.teststeps.PropertyTransferTestStepStruct;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;

import static com.smartbear.ready.recipe.TestStepNames.createUniqueName;

public class PropertyTransferTestStepParser implements TestStepJsonParser {
    @Override
    public void createTestStep(WsdlTestCase testCase, TestStepStruct struct, StringToObjectMap context) {
        PropertyTransferTestStepStruct propertyTransferTestStepElement = (PropertyTransferTestStepStruct)struct;
        String testStepName = createUniqueName(testCase, struct.name, "Property Transfer");
        PropertyTransfersTestStep transfersTestStep = (PropertyTransfersTestStep) testCase.insertTestStep(PropertyTransfersStepFactory.TRANSFER_TYPE, testStepName, propertyTransferTestStepElement.index);
        for (PropertyTransferStruct transfer : propertyTransferTestStepElement.transfers) {
            PropertyTransfer propertyTransfer = transfersTestStep.addTransfer(transfer.transferName);
            addSource(transfer, propertyTransfer);
            addTarget(transfer, propertyTransfer);
            addTransferOptions(transfer, propertyTransfer);
        }
    }

    private void addSource(PropertyTransferStruct transfer, PropertyTransfer propertyTransfer) {
        propertyTransfer.setSourceStepName(transfer.source.sourceName);
        propertyTransfer.setSourcePropertyName(transfer.source.property);
        propertyTransfer.setSourcePath(transfer.source.path);
        propertyTransfer.setSourcePathLanguage(getPathLanguage(transfer.source.pathLanguage));
    }

    private void addTarget(PropertyTransferStruct transfer, PropertyTransfer propertyTransfer) {
        propertyTransfer.setTargetStepName(transfer.target.targetName);
        propertyTransfer.setTargetPropertyName(transfer.target.property);
        propertyTransfer.setTargetPath(transfer.target.path);
        propertyTransfer.setTargetPathLanguage(getPathLanguage(transfer.target.pathLanguage));
    }

    private PathLanguage getPathLanguage(String pathLanguage) {
        return pathLanguage == null ? PathLanguage.XPATH : PathLanguage.fromDisplayName(pathLanguage);
    }

    private void addTransferOptions(PropertyTransferStruct transfer, PropertyTransfer propertyTransfer) {
        propertyTransfer.setFailOnError(transfer.failTransferOnError);
        propertyTransfer.setSetNullOnMissingSource(transfer.setNullOnMissingSource);
        propertyTransfer.setTransferTextContent(transfer.transferTextContent);
        propertyTransfer.setIgnoreEmpty(transfer.ignoreEmptyValue);
        propertyTransfer.setTransferToAll(transfer.transferToAll);
        propertyTransfer.setTransferChildNodes(transfer.transferChildNodes);
        propertyTransfer.setEntitize(transfer.entitizeTransferredValues);
    }
}
