package com.smartbear.integrations.swagger;

import com.eviware.soapui.impl.rest.mock.RestMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.support.MockRunListenerAdapter;
//import com.eviware.soapui.plugins.ListenerConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@ListenerConfiguration()
public class SwaggerGeneratorMockRunListener extends MockRunListenerAdapter {
    @Override
    public MockResult onMockRequest(MockRunner runner, HttpServletRequest request, HttpServletResponse response) {
        if (request.getMethod().toLowerCase().equals("get") && request.getPathInfo().equals("/api-docs.json")) {
            try {
                return new Swagger2FromVirtGenerator(new RestMockRequest(request, response, (WsdlMockRunContext) runner.getMockContext())).generate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
