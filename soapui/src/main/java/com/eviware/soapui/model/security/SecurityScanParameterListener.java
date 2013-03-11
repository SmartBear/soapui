package com.eviware.soapui.model.security;

public interface SecurityScanParameterListener
{

	void parameterNameChanged( SecurityCheckedParameter parameter, String oldName, String newName );

	void parameterLabelChanged( SecurityCheckedParameter parameter, String oldLabel, String newLabel );

	void parameterXPathChanged( SecurityCheckedParameter parameter, String oldXPath, String newXPath );

	void parameterCheckedChanged( SecurityCheckedParameter parameter );

	void parameterTypeChanged( SecurityCheckedParameter paramter, String oldType, String newType );

}
