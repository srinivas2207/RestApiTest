package com.rest.test.framework.unit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlunit.diff.ComparisonResult;

import com.rest.test.framework.util.ApiTestConstants;

public class RestStringUnitTest
{
	private String expectedString = null;
	private String actualString = null;
	public RestStringUnitTest(String expectedString, String actualString) {
		if (expectedString != null) {
			expectedString = expectedString.trim();
		}
		
		if (actualString != null) {
			actualString = actualString.trim();
		}
		
		this.expectedString = expectedString;
		this.actualString = actualString;
	}
	
	public boolean compare() {
		if (expectedString == null && actualString == null) {
			return true;
		}
		
		actualString = actualString == null ? "" : actualString.replaceAll("\n", "").replaceAll("\r", "").trim();
		expectedString = expectedString == null ? "" : expectedString.replaceAll("\n", "").replaceAll("\r", "").trim();
		
		
		if (expectedString.equalsIgnoreCase(actualString)) {
			return true;
		}
		
		// Ignoring the total unmatched string, if it's marked as IGNORE by using IGNORE_STRING
		if (expectedString.contains(ApiTestConstants.IGNORE_STRING)) {
			return true;
		}
		
		// Comparing unmatched strings using REGEX
		String expectedReg = expectedString;
		
		// Forming a expected regex to handle Ignoring Specific substrings marked as IGNORE_VALUE
		if (expectedString.contains(ApiTestConstants.IGNORE_VALUE)) {
			
			// Clearing Regex chars ^ and $ from expected and actual strings
			expectedReg = expectedReg.replaceAll("\\^", "");
			actualString = actualString.replaceAll("\\^", "");
			expectedReg = expectedReg.replaceAll("\\$", "");
			actualString = actualString.replaceAll("\\$", "");
			
			expectedReg = expectedReg.replaceAll("\\[", "\\\\[");
			expectedReg = expectedReg.replaceAll("\\]", "\\\\]");
			expectedReg = expectedReg.replaceAll("\\{", "\\\\{");
			expectedReg = expectedReg.replaceAll("\\}", "\\\\}");
			expectedReg = expectedReg.replaceAll("\\(", "\\\\(");
			expectedReg = expectedReg.replaceAll("\\)", "\\\\)");
			
			expectedReg = expectedReg.replaceAll(ApiTestConstants.IGNORE_VALUE, ".*");
			expectedReg = "^" + expectedReg + "$";
		}
	    
		try {
			Pattern pattern = Pattern.compile(expectedReg);
	        Matcher matcher = pattern.matcher(actualString);
	        boolean res =  matcher.matches();
	        return res;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
