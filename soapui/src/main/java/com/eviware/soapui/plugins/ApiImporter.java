package com.eviware.soapui.plugins;

import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;

import java.util.List;

public interface ApiImporter {
    List<Interface> importApis(Project project);
}
