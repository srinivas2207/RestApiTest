package com.rest.test.framework;

import com.rest.test.framework.network.RestNetworkUtil;

/**
 * Base class for Authentication
 * @author SrinivasDonapati
 *
 */
public abstract class RestAuthenticator {
	public abstract void setUp();
	public abstract void tearDown();
	
	public RestNetworkUtil restUtil = null;
	
	/**
	 * Returns currently used rest util
	 * @return {@link RestNetworkUtil}
	 */
	public RestNetworkUtil getRestUtil()
	{
		return restUtil;
	}
	
	public void setRestUtil(RestNetworkUtil restUtil)
	{
		this.restUtil = restUtil;
	}
}
