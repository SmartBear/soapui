package com.eviware.soapui.security.support;

public interface SecurityCheckParameterListener
{

	void parameterAdded( SecurityCheckedParameter parameter );

	void parameterRemoved( SecurityCheckedParameter parameter );

	void parameterNameChanged( SecurityCheckedParameter parameter, String oldName, String newName );

	void parameterLabelChanged( SecurityCheckedParameter parameter, String oldLabel, String newLabel );

	void parameterXPathChanged( SecurityCheckedParameter parameter, String oldXPath, String newXPath );
	
	void parameterCheckedChanged( SecurityCheckedParameter parameter );

}
