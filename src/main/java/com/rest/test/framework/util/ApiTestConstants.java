package com.rest.test.framework.util;

/**
 * This class contains the all the constants used in the framework
 * @author SrinivasDonapati
 *
 */
public class ApiTestConstants {

	public static final String PROPERTY_TEST_NAME			= "TEST_NAME";
	
	public static final String PROPERTY_API					= "API_TEST";
	public static final String PROPERTY_URL					= "API_URL";
	public static final String PROPERTY_REQ_METHOD			= "API_METHOD";
	public static final String PROPERTY_REQ_STATUES			= "API_STATUS";
	public static final String PROPERTY_POLL				= "API_POLL";
	public static final String PROPERTY_REQUEST				= "API_REQUEST";
	public static final String PROPERTY_RESPONSE			= "API_RESPONSE";
	public static final String PROPERTY_HEADERS				= "API_HEADERS";
	public static final String PROPERTY_TEST_CONDITION		= "TEST_CONDITION";
	public static final String PROPERTY_TEST_TYPE			= "TEST_TYPE";
	public static final String PROPERTY_SUITE_VARS			= "SUITE_VARS";
	public static final String PROPERTY_TEST_VARS			= "TEST_VARS";
	public static final String PROPERTY_TEST_WAIT_TIME		= "WAIT_TIME";
	public static final String PROPERTY_POLL_TIME			= "POLL_TIME";
	public static final String PROPERTY_POLL_INTERVAL		= "POLL_INTERVAL";
	
	public static final String PROPERTY_UPLOAD_REQ_PREFIX	= "UPLOAD_REQUEST";
	public static final String PROPERTY_DOWNLOAD_REQ_PREFIX	= "DOWNLOAD_REQUEST";
	
	public static final String PROPERTY_VARIABLE_RANDOM		= "RANDOM_VALUE";
	public static final String PROPERTY_VARIABLE_SYS_TIME	= "SYSTEM_TIME";

	public static final String IGNORE_VALUE 				= "IGNORE_VALUE";
	
	public static final String EXPECTED_VALUE_PATH			= "path";
	public static final String EXPECTED_VALUE				= "value";
	
	
	public static enum TEST_TYPE {
		JSON_UNIT, XML_UNIT, STRING_UNIT
	}
}