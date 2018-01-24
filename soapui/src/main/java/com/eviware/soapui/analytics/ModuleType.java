package com.eviware.soapui.analytics;

public enum ModuleType {

    PROJECTS("Projects", "ProjectsModule", "Project overview with API definitions for both RESTful and SOAP APIs"),
    SOAPUI_NG("SoapUI", "SoapUiNGModule", "Functional testing with structure, data-driven tests and a wide range of assertions"),
    LOADUI_NG("LoadUI", "LoadUINGModule", "Load testing with load scenarios, statistics and behavior"),
    SERVICE_V("ServiceV", "ServiceVirtualizationModule", "Virtual APIs for mocking responses and behavior"),
    SECURE("Secure", "SecurityModule", "Vulnerability testing with a wide range of security scans");

    private String name;
    private String id;
    private String description;

    ModuleType(String name, String id, String description) {
        this.name = name;
        this.id = id;
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public static ModuleType parse(String name) {
        for (ModuleType moduleType : values()) {
            if (name.equalsIgnoreCase(moduleType.getId())) {
                return moduleType;
            }
            if (name.equalsIgnoreCase(moduleType.toString())) {
                return moduleType;
            }
            if (name.equalsIgnoreCase(moduleType.getId())) {
                return moduleType;
            }
        }
        throw new IllegalArgumentException("No ModuleType enum exists for " + name);
    }
}
