package com.rest.test.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.fico.modeler.ModelerRuntimeException;

/**
 * The PropertiesHelper is treated as a singleton and the instance is available via the 
 * getPropertiesHandlerInstance() method. The helper is initialized with test properties, and this 
 * initialization needs to be done before the PropertiesHelper can be used.
 * Initializing the test properties is done via a call to initializeProperties(). Initialization is 
 * a one time step and additional calls to initializeProperties() will be ignored.
 */
public class PropertiesHelper
{
	private static final Logger	_log	= LoggerFactory.getLogger(PropertiesHelper.class);
	private Properties _envProps = new Properties();
	private static boolean _initialized;
	private static PropertiesHelper _instance;
	
	// The constructor private so that access to the PropertiesHelper is via the 
	// getDefaultPropertiesHandler() method
	private PropertiesHelper()
	{
		_envProps = new Properties();
		_initialized = false;
	}

	void loadProperties(InputStream propStream) 
	{
		try {
			_envProps.load(propStream);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method will first check if we have have environment variable
	 * with the same name as propertyName, if value is find in environment
	 * variable then we will use that value otherwise we will look up for the
	 * value in env.properties file.
	 */
	public String getProperty(String propertyName)
	{
		if (!_initialized) {
			//throw new ModelerRuntimeException("Proreties not initialized");
			System.out.println("Properties not initialized");
		}
		// First check if environment value is present.
		String envValue = System.getenv(propertyName);
		if (envValue != null && envValue.length() > 0) return envValue;
		
		// Second preference would be given to java property.
		String javaValue = System.getProperty(propertyName);
		if (javaValue != null && javaValue.length() > 0) return javaValue;
		
		// If we didn't find this property yet, then return value from env.properties file.
		return _envProps.getProperty(propertyName);
	}
	
	void printProperties() 
	{
		String revision = _initialized ? "updated" : "default";
		Enumeration props = _envProps.keys();
		_log.info("------------------------------------");
		_log.info("Starting test using following " + revision + " properties");
		while (props.hasMoreElements()) {
			String prop = (String) props.nextElement();
			
			// Avoid printing passwords and access keys by mistake.
			if (prop != null && !prop.toLowerCase().contains("pass") && !prop.toLowerCase().contains("key")) {
				String value = getProperty(prop);
				_log.info(prop + " = " + value);
			}
		}
		_log.info("------------------------------------");
	}
	
	// singleton methods
	/**
	 * get the instance of the properties helper
	 */
	public static PropertiesHelper getPropertiesHelperInstance()
	{
		if (_instance == null) {
			_instance = new PropertiesHelper();
		}
		
		return _instance;
	}
	
	/**
	 * setModelService the properties with the test properties. The properties need to be initialized
	 * before the PropertiesHandler can be used
	 */
	public static void initializeProperties(InputStream testPropertyStream)
	{
		if (!_initialized && testPropertyStream != null) {
			getPropertiesHelperInstance().loadProperties(testPropertyStream);
			_initialized = true;
			getPropertiesHelperInstance().printProperties();
		}
	}
}
