package com.rest.test.framework;

import com.rest.test.framework.network.RestNetworkUtil;
import com.rest.test.framework.util.PropertiesHelper;

public abstract class RestAuthenticator {
	public abstract void setUp();
	public abstract void tearDown();
	
	public RestNetworkUtil restUtil = null;
	
	public RestNetworkUtil getRestUtil()
	{
		return restUtil;
	}
	
	public void setRestUtil(RestNetworkUtil restUtil)
	{
		this.restUtil = restUtil;
	}
}
