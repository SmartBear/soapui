package com.eviware.soapui.impl.wsdl.monitor;

import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Captured {

	private boolean startCapture;
	private boolean stopCapture;
	private Date operationStarted;
	private String requestHost;
	private String targetHost;
	private String wsInterface;
	private String wsOperation;
	private long timeTaken;
	private int requestSize;
	private int responseSize;
	private String request;
	private String response;

	private String requestHeader;
	private String responseHeader;

	public boolean isStartCapture() {
		return startCapture;
	}

	public void startCapture() {
		this.startCapture = true;
		this.stopCapture = false;
		setOperationStarted(new Date(System.currentTimeMillis()));
	}

	public boolean isStopCapture() {
		return stopCapture;
	}

	public void stopCapture() {
		this.startCapture = false;
		this.stopCapture = true;
		setTimeTaken(System.currentTimeMillis());
	}

	public Date getOperationStarted() {
		return operationStarted;
	}

	private void setOperationStarted(Date operationStarted) {
		this.operationStarted = operationStarted;
	}

	public String getRequestHost() {
		return requestHost;
	}

	public void setRequestHost(String requestHost) {
		this.requestHost = requestHost;
	}

	public String getTargetHost() {
		return targetHost;
	}

	public void setTargetHost(String targetHost) {
		this.targetHost = targetHost;
	}

	public String getWsInterface() {
		return wsInterface;
	}

	public void setWsInterface(String wsInterface) {
		this.wsInterface = wsInterface;
	}

	public String getWsOperation() {
		return wsOperation;
	}

	public void setWsOperation(String wsOperation) {
		this.wsOperation = wsOperation;
	}

	public long getTimeTaken() {
		return timeTaken;
	}

	private void setTimeTaken(long endTime) {
		this.timeTaken = -this.operationStarted.getTime() + endTime;
	}

	public int getRequestSize() {
		return requestSize;
	}

	private void setRequestSize(int requestSizeInCharacters) {
		this.requestSize = requestSizeInCharacters;
	}

	public int getResponseSize() {
		return responseSize;
	}

	private void setResponseSize() {
		int length = this.response.length();
		this.responseSize = length;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
		this.setRequestSize(this.request.length());
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		if (this.response == null) {
			this.response = response;
		} else {
			this.response += response;
		}
		this.setResponseSize();
	}

	@Override
	public String toString() {

		String toString = "Request host: " + this.requestHost + "\n";
		toString += "Request header : \n" + this.requestHeader + "\n";
		toString += "Request: " + this.request + "\n";
		toString += "Request size: " + this.requestSize + "\n";
		toString += "Response host:" + this.targetHost + "\n";
		toString += "Response header: \n" + this.responseHeader + "\n"; 
		toString += "Response: " + this.response + "\n";
		toString += "Response size:" + this.responseSize + "\n";
		toString += "Started: " + this.operationStarted.toString() + "\n";
		toString += "Time Taken: " + this.timeTaken + "ms\n";
		return toString;

	}

	@SuppressWarnings("unchecked")
	public void setRequestHeader(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		
		String headerValue = null;
		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			
			if (ProxyServlet.dontProxyHeaders.contains(name.toLowerCase())) {
				continue;
			}
			
			headerValue = name + "::";
			Enumeration<String> header = httpRequest.getHeaders(name);
			while (header.hasMoreElements()) {
				String value = header.nextElement();
				if (value != null) {
					headerValue += value;
				}
			}
			requestHeader = requestHeader == null ? headerValue : requestHeader + "\n" + headerValue;
		}
	}

	public void addResponseHeader(String responseHeader) {
		this.responseHeader = this.responseHeader == null ? responseHeader : this.responseHeader + "\n"+ responseHeader;
	}

}
