package com.eviware.soapui.plugins;

import com.eviware.soapui.model.ModelItem;

/**
 * Created by ole on 26/08/14.
 */
public interface TargetSelector {
    boolean applies(ModelItem target);
}
