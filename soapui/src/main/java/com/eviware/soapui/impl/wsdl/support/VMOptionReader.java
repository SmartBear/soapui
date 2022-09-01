package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;

public class VMOptionReader {
    private static final MessageSupport messages = MessageSupport.getMessages(VMOptionReader.class);

    public static boolean getValueAsBoolean(String vmOptionName, Boolean defaultValue) {
        String propertyValue = System.getProperty(vmOptionName);
        if (StringUtils.hasContent(propertyValue)) {
            if (propertyValue.equals("true")) {
                return true;
            } else if (propertyValue.equals("false")) {
                return false;
            } else {
                // TODO:
                //Logging.logError(new InvalidVmOptionValueException(vmOptionName, "true or false", defaultValue.toString()));
            }
        }
        return defaultValue;
    }
}