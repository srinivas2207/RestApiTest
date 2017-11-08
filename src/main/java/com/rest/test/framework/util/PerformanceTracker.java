package com.rest.test.framework.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import com.rest.test.framework.ApiTestInfo;
import com.rest.test.framework.ApiTestInfo.ApiCallInfo;

public class PerformanceTracker
{
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	private static final String FILE_HEADER_SNO = "S.No";
	private static final String FILE_HEADER_API = "API";
	private static final String FILE_HEADER_URL = "URL";
	private static final String FILE_HEADER_TIME= "Time";
	private static final String FILE_HEADER_STATUS= "Status";
	private static final String FILE_HEADER_AVGTIME= "Avg.Time(ms)";

	private static final String DEFAULT_RECORD_LOCATION = "src/test/resources/Performance";
	private String folderName;
	private String testName;
	private LinkedHashMap<Long, ApiPerformanceInfo> apiList = null;
	private boolean isAppendOn;
	private String recordLocation;

	public PerformanceTracker(String propertyFilePath, boolean isAppendOn)
	{
		this.isAppendOn = isAppendOn;
		propertyFilePath = propertyFilePath.replace(".properties", "");
		String[] parts = propertyFilePath.split("/");
		if (parts != null && parts.length > 1) {
			folderName = parts[parts.length - 2];
			testName = parts[parts.length - 1];
		}
		else {
			testName = propertyFilePath;
		}
		testName += ".csv";
		apiList = new LinkedHashMap<>();
	}
	
	public void setRecordLocation(String recorrdLocation) {
		this.recordLocation = recorrdLocation;
	}

	public void setApiInfo(ApiCallInfo apiInfo)
	{
		ApiPerformanceInfo apiPerformanceInfo = new ApiPerformanceInfo();
		apiPerformanceInfo.setApiName(apiInfo.getName());
		String url = apiInfo.getUrl();
		if (url != null) {
			url = url.split("\\?")[0];
			if (url.startsWith("www") || url.startsWith("http")) {
				try {
					URI uri = new URI(url);
					String domain = uri.getHost();
					url= url.split(domain)[1];
				}
				catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		
		apiPerformanceInfo.setUrl(apiInfo.getMethod() + ":" + url);
		
		apiList.put(apiInfo.getId(), apiPerformanceInfo);
	}

	public void setRequestTime(long apiId, long time)
	{
		ApiPerformanceInfo apiPerformanceInfo = apiList.get(apiId);
		if (apiPerformanceInfo != null) {
			apiPerformanceInfo.setRequestTime(time);
		}
	}

	public void setRequestStatus(long apiId, boolean status)
	{
		ApiPerformanceInfo apiPerformanceInfo  = apiList.get(apiId);
		if (apiPerformanceInfo != null) { 
			apiPerformanceInfo.setRunStatus(status);
		}
	}

	public void downloadReport()
	{
		String FILE_PATH = recordLocation != null ? recordLocation : DEFAULT_RECORD_LOCATION;

		String filePath = FILE_PATH;

		if (folderName != null) {
			File dir = new File( FILE_PATH + "/" + folderName);
			if (!dir.exists() || !dir.isDirectory()) {
				dir.mkdir();
			}
			filePath +=  "/" + folderName + "/" + testName;
		}
		else {
			filePath +=  "/" + testName;
		}

		File file = new File(filePath);
		
		List<String> oldFileData = null;
		if (isAppendOn && file.exists()) {
			Scanner scanner;
			try {
				oldFileData   = new ArrayList<String>();
				scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					oldFileData . add(scanner.nextLine());
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		if (file.exists()) {
			file.delete();
		}

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(file);

			String header = getHeader(oldFileData ,  0) + NEW_LINE_SEPARATOR;
			// Write the CSV file header
			fileWriter.append(header);

			int sNo = 1;
			for (Long key : apiList.keySet()) {
				ApiPerformanceInfo apiPerformanceInfo = apiList.get(key);
				String rowData = getRowData(oldFileData, sNo++, apiPerformanceInfo) + NEW_LINE_SEPARATOR;
				fileWriter.append(rowData);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getRowData(List<String> oldFileData, int sNo, ApiPerformanceInfo apiPerformanceInfo)
	{
		String rowData = null;
		if (oldFileData != null && oldFileData.size() > sNo) {
			rowData = oldFileData.get(sNo);
		}
		
		float time =apiPerformanceInfo.getRequestTime();
		String status = apiPerformanceInfo.isRunStatus() ? "T" : "F";
		
		if (rowData == null || rowData.trim().length() == 0) {
			rowData = sNo + COMMA_DELIMITER 
					+ apiPerformanceInfo.getApiName() + COMMA_DELIMITER 
					+ apiPerformanceInfo.getUrl() + COMMA_DELIMITER 
					+ time + COMMA_DELIMITER 
					+ status;
			return rowData;
		}
		
		String[] rowParts = rowData.split(COMMA_DELIMITER);
		
		float totalTime = time;
		int trialCount = 0;
		
		if(rowParts != null && rowParts.length > 3 ) {
			int length = rowParts.length % 2 == 0 ? rowParts.length - 1 : rowParts.length;
			for (int i = 3; i < length; i+=2) {
				try {
					totalTime += Float.parseFloat(rowParts[i]);
					trialCount++;
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			
			rowData = "";
			for (int i = 0; i < length; i++) {
				rowData += rowParts[i] + COMMA_DELIMITER;
			}
			rowData += time + COMMA_DELIMITER + status + COMMA_DELIMITER;
			rowData += (int)(totalTime /(trialCount + 1));
		}
		
		return rowData;
	}

	private String getHeader(List<String> prevFileData, int index)
	{
		String header = null;
		String extHeader = null;
		if (prevFileData != null && prevFileData.size() > index) {
			extHeader = prevFileData.get(index);
		}
		if (extHeader == null || extHeader.trim().length() == 0) { 
			return FILE_HEADER_SNO + COMMA_DELIMITER
				+ FILE_HEADER_API + COMMA_DELIMITER 
				+ FILE_HEADER_URL + COMMA_DELIMITER
				+ FILE_HEADER_TIME + COMMA_DELIMITER 
				+ FILE_HEADER_STATUS; 
		}
		
		extHeader = extHeader.replace(COMMA_DELIMITER + FILE_HEADER_AVGTIME, "");
		String[] headerParts = extHeader.split(COMMA_DELIMITER);
		
		header = FILE_HEADER_SNO + COMMA_DELIMITER + FILE_HEADER_API + COMMA_DELIMITER + FILE_HEADER_URL + COMMA_DELIMITER;
		
		if(headerParts != null && headerParts.length > 3) {
			for (int i = 3; i < headerParts.length; i++) {
				String column = headerParts[i];
				if(!column.contains("(" + ((i-1)/2) + ")")) {
					column = column + "(" + ((i-1)/2) + ")";
				}
				header += column + COMMA_DELIMITER;
			}
			
			header += FILE_HEADER_TIME + "(" + (headerParts.length -1) / 2 + ")" + COMMA_DELIMITER;
			header += FILE_HEADER_STATUS + "(" + (headerParts.length -1) / 2 + ")" + COMMA_DELIMITER;
		}
		
		
		header += FILE_HEADER_AVGTIME;
		return header;
	}
	

	private class ApiPerformanceInfo
	{
		private String apiName;
		private String url;
		private long requestTime;
		private boolean runStatus;
		public String getApiName()
		{
			return apiName;
		}

		public void setApiName(String apiName)
		{
			this.apiName = apiName;
		}

		public long getRequestTime()
		{
			return requestTime;
		}

		public void setRequestTime(long requestTime)
		{
			this.requestTime = requestTime;
		}

		public boolean isRunStatus()
		{
			return runStatus;
		}

		public void setRunStatus(boolean runStatus)
		{
			this.runStatus = runStatus;
		}
		
		public String getUrl()
		{
			return url;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}
	}
}
