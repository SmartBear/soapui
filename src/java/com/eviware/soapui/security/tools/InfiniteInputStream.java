package com.eviware.soapui.security.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class InfiniteInputStream extends InputStream {
	private long bytesSent = 0;
	private long byteLimit;

	public InfiniteInputStream(long maxSize) {
		super();
		byteLimit = maxSize;
	}

	@Override
	public int read() throws IOException {
		if (bytesSent >= byteLimit) 
			return -1;
		Random rnd = new Random();
		bytesSent++;
		return rnd.nextInt();
	}

}
