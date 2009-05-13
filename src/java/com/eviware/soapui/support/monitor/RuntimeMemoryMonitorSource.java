package com.eviware.soapui.support.monitor;

/**
 * @author Angelo De Caro
 */
public class RuntimeMemoryMonitorSource implements MonitorSource
{

	private Runtime runtime = Runtime.getRuntime();

	public float getTotal()
	{
		return runtime.totalMemory();
	}

	public float getUsed()
	{
		return runtime.totalMemory() - runtime.freeMemory();
	}

}
