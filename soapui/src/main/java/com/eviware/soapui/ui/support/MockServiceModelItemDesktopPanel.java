package com.eviware.soapui.ui.support;

import com.eviware.soapui.model.mock.MockService;

/**
 * Base of all MockService desktop panels.
 */
public abstract class MockServiceModelItemDesktopPanel extends KeySensitiveModelItemDesktopPanel<MockService> {

    protected MockServiceModelItemDesktopPanel(MockService modelItem) {
        super(modelItem);
    }

}
