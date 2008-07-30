package com.eviware.soapui.impl.wsdl.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.io.Buffer;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;


public class SoapUIHttpExchange extends HttpExchange {
	
	private Continuation continuation;
	private InputStream in;
	private OutputStream out;
	private HttpServletResponse httpResponse;
	private Logger log = Logger.getLogger(SoapUIHttpExchange.class);
	private Captured captured;
	
	
	public SoapUIHttpExchange(HttpServletRequest httpRequest, HttpServletResponse httpResponse, CaptureInputStream capture, Captured capturedData) throws IOException {
		this.httpResponse = httpResponse;
		continuation = ContinuationSupport.getContinuation(httpRequest,httpRequest);
		in = capture;
        out = httpResponse.getOutputStream();
        this.captured = capturedData;
	}
	
	@Override
	protected void onConnectionFailed(Throwable ex) {
//		log.warn("onConnectionFailed");
		try {
			httpResponse.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, ex.getMessage());
			httpResponse.flushBuffer();
			this.captured.stopCapture();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onException(Throwable ex) {
//		log.error("onException" + ex.getMessage());
		try {
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
			httpResponse.flushBuffer();
			this.captured.stopCapture();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onRequestCommitted() throws IOException
    {
//        log.info("onRequestCommitted()");
    }

	@Override
    protected void onRequestComplete() throws IOException
    {
//        log.info("onRequestComplete()");
        this.captured.setRequest(new String(((CaptureInputStream) in).getCapturedData(), "UTF-8"));
    }

	@Override
    protected void onResponseComplete() throws IOException
    {
//        log.info("onResponseComplete()");
        continuation.resume();
        this.captured.stopCapture();
        System.out.println(this.captured.toString());
    }

	@Override
    protected void onResponseContent(Buffer content) throws IOException
    {
//        log.info("onResponseContent()");
        byte[] buffer = new byte[content.length()];
        while (content.hasContent())
        {
			int len=content.get(buffer ,0,buffer.length);
			this.captured.setResponse(new String(buffer, "UTF-8"));
            out.write(buffer,0,len);  // May block here for a little bit!
        }
    }

	@Override
    protected void onResponseHeaderComplete() throws IOException
    {
//        log.info("onResponseCompleteHeader()");
    }

	@SuppressWarnings("deprecation")
	@Override
    protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
    {
        log.info("onResponseStatus("+version+","+status+","+reason+")");
        if (reason!=null && reason.length()>0)
            httpResponse.setStatus(status,reason.toString());
        else
            httpResponse.setStatus(status);
            
    }

	@Override
    protected void onResponseHeader(Buffer name, Buffer value) throws IOException
    {
//        log.info("onResponseHeader("+name+","+value+")");
        String s = name.toString().toLowerCase();
        if (!ProxyServlet.dontProxyHeaders.contains(s)) {
            httpResponse.addHeader(name.toString(),value.toString());
            captured.addResponseHeader(name.toString() + "::" + value.toString());
        }
    }
    
}
