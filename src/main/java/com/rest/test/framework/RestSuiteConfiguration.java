package com.rest.test.framework;

/**
 * This class is for setting Test suite configuration.
 * <br>
 * It helps in customizing test suite by passing required configuration settings.
 * @author SrinivasDonapati
 *
 */
public class RestSuiteConfiguration
{
	private String baseUrl;
	private boolean trackPerformance;
	private boolean appendPerformanceResults;
	private String performanceRecordLocation;
	private RestAuthenticator restAuthenticator;

	private RestSuiteConfiguration(final Builder builder) {
		this.baseUrl = builder.baseUrl;
		this.trackPerformance = builder.trackPerformance;
		this.performanceRecordLocation = builder.performanceRecordLocation;
		this.restAuthenticator = builder.restAuthenticator;
		this.appendPerformanceResults = builder.appendPerformanceResults;
	}
	
	/**
	 * Getting base url of the application to be tested
	 * @return
	 */
	public String getBaseUrl()
	{
		return baseUrl;
	}

	/**
	 * Checks if the performance track is enabled
	 * @return Status of the performance track
	 */
	public boolean isTrackPerformance()
	{
		return trackPerformance;
	}

	/**
	 * Checks whether performance track results to be merged
	 * @return Status of merge type
	 */
	public boolean isAppendPerformanceResults()
	{
		return appendPerformanceResults;
	}

	/**
	 * Returns the record location of the tracked performances
	 * @return
	 */
	public String getPerformanceRecordLocation()
	{
		return performanceRecordLocation;
	}

	/**
	 * Returns the authenticator used
	 * @return
	 */
	public RestAuthenticator getRestAuthenticator()
	{
		return restAuthenticator;
	}
	
	
	/**
	 * This builder class is used to pass test configuration
	 * @author SrinivasDonapati
	 *
	 */
	public static class Builder {
		private String baseUrl;
		private boolean trackPerformance;
		private boolean appendPerformanceResults;
		private String performanceRecordLocation;
		private RestAuthenticator restAuthenticator;
		
		
		/**
		 * Sets the Application/Server's base URL
		 * @param baseUrl
		 * @return
		 */
		public Builder setBaseUrl(String baseUrl)
		{
			this.baseUrl = baseUrl;
			return this;
		}
	
		/**
		 * Enabling/Disabling performance track
		 * @param trackPerformance
		 * @return
		 */
		public Builder setTrackPerformance(boolean trackPerformance)
		{
			this.trackPerformance = trackPerformance;
			return this;
		}

		/**
		 * Enabling/Disabling performance result append option
		 * @param appendPerformanceResults
		 * @return
		 */
		public Builder setAppendPerformanceResults(boolean appendPerformanceResults)
		{
			this.appendPerformanceResults = appendPerformanceResults;
			return this;
		}
		
		/**
		 * Setting performace record location
		 * @param performanceRecordLocation
		 * @return
		 */
		public Builder setPerformanceRecordLocation(String performanceRecordLocation)
		{
			this.performanceRecordLocation = performanceRecordLocation;
			return this;
		}
		
		/**
		 * Passing server/application's authenticator
		 * @param restAuthenticator
		 * @return
		 */
		public Builder setRestAuthenticator(RestAuthenticator restAuthenticator)
		{
			this.restAuthenticator = restAuthenticator;
			return this;
		}
		
		/**
		 * Builds the configuration passed, and returns the configuration object
		 * @return {@link RestSuiteConfiguration}
		 */
		public RestSuiteConfiguration build() {
			return new RestSuiteConfiguration(this);
		}
	}
}
