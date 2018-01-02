package com.eviware.soapui.impl.rest;

import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.wsdl.submit.AbstractRestMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;


public class RestMockResultMessageExchange extends AbstractRestMessageExchange<MockResponse>{

	private final MockResult mockResult;
    private final MockResponse mockResponse;
    private final MockRequest mockRequest;
    
	public RestMockResultMessageExchange(MockResult mockResult) {
		super(mockResult.getMockResponse());
		
        this.mockResult = mockResult;
        this.mockResponse = (RestMockResponse) mockResult.getMockResponse();
        this.mockRequest = mockResult.getMockRequest();
	}

	public String getEndpoint() {
		return mockRequest.getHttpRequest().getRequestURI();
    }

    public String getRequestContent() {
    	return mockRequest.getRequestContent();
    }

    public StringToStringsMap getRequestHeaders() {
    	return mockRequest.getRequestHeaders();
    }

    public Attachment[] getRequestAttachments() {
        return getModelItem().getAttachments();
    }

    public Attachment[] getResponseAttachments() {
    	return mockResponse.getAttachments();
    }

    public String getResponseContent() {
    	return mockResult.getResponseContent();
    }

    public HttpResponse getResponse() {
        return null;
    }

    public StringToStringsMap getResponseHeaders() {
    	return mockResult == null ? new StringToStringsMap() : mockResult.getResponseHeaders();
    }

    public long getTimeTaken() {
    	return mockResult == null ? -1 : mockResult.getTimeTaken();
    }

    public long getTimestamp() {
    	return mockResult == null ? -1 : mockResult.getTimestamp();
    }

    public boolean isDiscarded() {
        return discardResponse;
    }

    public RestResource getResource() {
    	return null;
    }

    public RestRequestInterface getRestRequest() {
    	return null;
    }

    public Operation getOperation() {
    	
    	if (mockResponse != null && mockResponse instanceof RestMockResponse) {
            RestMockResponse restMockResponse = (RestMockResponse) mockResponse;
            if (mockResult.getMockOperation() != null) {
                	return mockResult.getMockOperation().getOperation();
            }
            return restMockResponse.getMockOperation().getOperation();
        }
        return null;
    }

    public int getResponseStatusCode() {
    	return mockResponse.getResponseHttpStatus();
    }

    public String getResponseContentType() {
    	return mockResponse.getContentType();
    }
    
    @Override
    public byte[] getRawRequestData() {
    	if(getRequestContent() == null)
    		return null;
    	return mockRequest.getRawRequestData();
    }
    
    @Override
    public boolean hasRawData() {
    	return mockResponse == null;
    }
    
    @Override
    public byte[] getRawResponseData() {
    	return mockResult.getRawResponseData();
    }
    
    @Override
    public String getRequestContentAsXml() {
        String result = getRequestContent();
        return XmlUtils.seemsToBeXml(result) ? result : "<not-xml/>";
    }
}