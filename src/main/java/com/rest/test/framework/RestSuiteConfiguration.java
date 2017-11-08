package com.rest.test.framework;

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
	
	public String getBaseUrl()
	{
		return baseUrl;
	}

	public boolean isTrackPerformance()
	{
		return trackPerformance;
	}

	public boolean isAppendPerformanceResults()
	{
		return appendPerformanceResults;
	}

	public String getPerformanceRecordLocation()
	{
		return performanceRecordLocation;
	}

	public RestAuthenticator getRestAuthenticator()
	{
		return restAuthenticator;
	}
	
	public static class Builder {
		private String baseUrl;
		private boolean trackPerformance;
		private boolean appendPerformanceResults;
		private String performanceRecordLocation;
		private RestAuthenticator restAuthenticator;
		
		public Builder setBaseUrl(String baseUrl)
		{
			this.baseUrl = baseUrl;
			return this;
		}
	
		public Builder setTrackPerformance(boolean trackPerformance)
		{
			this.trackPerformance = trackPerformance;
			return this;
		}

		public Builder setAppendPerformanceResults(boolean appendPerformanceResults)
		{
			this.appendPerformanceResults = appendPerformanceResults;
			return this;
		}
		
		public Builder setPerformanceRecordLocation(String performanceRecordLocation)
		{
			this.performanceRecordLocation = performanceRecordLocation;
			return this;
		}
		
		public Builder setRestAuthenticator(RestAuthenticator restAuthenticator)
		{
			this.restAuthenticator = restAuthenticator;
			return this;
		}
		
		public RestSuiteConfiguration build() {
			return new RestSuiteConfiguration(this);
		}
	}
}
