package com.rest.test.framework.network;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;





import javax.ws.rs.HttpMethod;

import com.rest.test.framework.ApiTestInfo.ApiCallInfo;
import com.rest.test.framework.util.ApiTestConstants;

/**
 * This class handles network operations
 * @author SrinivasDonapati
 *
 */
public class RestNetworkUtil {
	private String restBaseUrl = null;
	
	private Map<String, String> headers = new HashMap<>();
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	public void removeHeader(String key) {
		headers.remove(key);
	}
	
	public String getHeader(String key) {
		return headers.get(key);
	}
	
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public Map getHeaders() {
		HashMap clonedHeaders = (HashMap) ((HashMap) headers).clone();
		return clonedHeaders;
	}
	
	public String getBaseUrl() {
		return restBaseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		
		if(baseUrl == null) {
			return;
		}
		baseUrl = baseUrl.trim();
		
		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length()-1);
		}
		
		try {
			// Checking the connection using base URL
			doGet(baseUrl, getHeaders());
		} catch(Exception e){
			e.printStackTrace();
		}
		
		restBaseUrl = baseUrl;
	}
	

	public void sendRequest(ApiCallInfo apiCallInfo) throws Exception {
		RestCallResponse restCallResponse = null;
		Map<String, String> headers = getHeaders();
		if (apiCallInfo.getHeaders() != null) {
			for (Map.Entry<String, String> entry : apiCallInfo.getHeaders().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				headers.put(key, value);
			}
		}
		
		String url;
		if (apiCallInfo.getUrl().startsWith("http") || apiCallInfo.getUrl().startsWith("www")) {
			url = apiCallInfo.getUrl();
		}
		else {
			url = restBaseUrl + apiCallInfo.getUrl();
		}
		
		String requestBody =  apiCallInfo.getRequest();
		
		if (requestBody != null && 
				requestBody.trim().length() > 0 ) {
			if (requestBody.startsWith(ApiTestConstants.PROPERTY_UPLOAD_REQ_PREFIX)) {
				restCallResponse =  uploadFile(url, requestBody, headers);
				apiCallInfo.setRestCallResponse(restCallResponse);
				return;
			} else if (requestBody.startsWith(ApiTestConstants.PROPERTY_DOWNLOAD_REQ_PREFIX)) {
				restCallResponse =  downloadFile(url, requestBody, headers);
				apiCallInfo.setRestCallResponse(restCallResponse);
				return;
			}
		}
		
		if (apiCallInfo.getMethod().equals("POST")) {
			restCallResponse =  doPost(url, requestBody, headers);
		} else if (apiCallInfo.getMethod().equals("GET")) {
			restCallResponse =  doGet(url, headers);
		}  else if (apiCallInfo.getMethod().equals("PUT")) {
			restCallResponse =  doPut(url, requestBody, headers);
		}  else if (apiCallInfo.getMethod().equals("DELETE")) {
			restCallResponse =  doDelete(url, requestBody, headers);
		}
		
		apiCallInfo.setRestCallResponse(restCallResponse);
	}
	
	public RestCallResponse doPost(String reqUrl, String postBody, Map<String, String> headers) throws Exception {
		return sendHttpRequest(HttpMethod.POST, reqUrl, postBody, headers);
	}

	private RestCallResponse doGet(String reqUrl, Map<String, String> headers) throws Exception{
		return sendHttpRequest(HttpMethod.GET, reqUrl, null, headers);
	}

	private RestCallResponse doPut(String reqUrl, String postBody, Map<String, String> headers) throws Exception{
		return sendHttpRequest(HttpMethod.PUT, reqUrl, postBody, headers);
	}
	
	private RestCallResponse doDelete(String reqUrl, String postBody, Map<String, String> headers) throws Exception{
		return sendHttpRequest(HttpMethod.DELETE, reqUrl, postBody, headers);
	}
	
	private RestCallResponse sendHttpRequest(String httpMethod, String reqUrl, String body, Map<String, String> headers) throws Exception {
		reqUrl = reqUrl.replaceAll(" ", "%20");
		RestCallResponse restCallResponse = null;
		InputStream is = null;
		String result = null;
		HttpURLConnection conn = null;
		try {
			URL url;
			try {
				url = new URL(reqUrl);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Invalid url: " + reqUrl);
			}
			
			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.setUseCaches(false);
				conn.setRequestMethod(httpMethod);
				conn.setRequestProperty("Content-Type", "application/json; charset=utf8");
				
				// adding header params
				if (headers != null) {
					Iterator<Entry<String, String>> iterator = headers.entrySet().iterator();
					while (iterator.hasNext()) {
						Entry<String, String> header = iterator.next();
						conn.setRequestProperty(header.getKey(), header.getValue());
					}
				}
				
				byte[] bytes = null;
				if (body != null) {
					bytes = body.getBytes();
					conn.setFixedLengthStreamingMode(bytes.length);
				}
				
				if (httpMethod.equals(HttpMethod.POST)) {
					conn.setDoOutput(true);
				}
				else if (httpMethod.equals(HttpMethod.GET)) {

				}
				else if (httpMethod.equals(HttpMethod.PUT)) {
					conn.setDoOutput(true);
				}
				else if (httpMethod.equals(HttpMethod.DELETE)) {
					
				}
								
				if (body != null) {
					OutputStream out = conn.getOutputStream();
					out.write(bytes);
					out.close();
				}
			
				int responseCode = conn.getResponseCode();
				if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
				    is = conn.getInputStream();
				} else {
					is = conn.getErrorStream();
				}
				
				// Convert the InputStream into a string
				if (is != null) {
					result = readStream(is);
				}
				
				restCallResponse = new RestCallResponse();
				restCallResponse.setStatus(responseCode);
				restCallResponse.setResponse(result);
				
				Map<String, List<String>> map = conn.getHeaderFields();
				Map<String, String> responseHeaders = null;
				if (map != null) {
					responseHeaders = new HashMap<>();
					for (Map.Entry<String, List<String>> entry : map.entrySet()) {
						String key = entry.getKey();
						String value = conn.getHeaderField(entry.getKey());
						responseHeaders.put(key, value);
					}
				}
				restCallResponse.setHeaders(responseHeaders);
				
			} catch(Exception e) {
				throw e;
			} finally {
				if (is != null) {
					is.close();
				}
				if (conn != null) {
					conn.disconnect();
				}

			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeConnection(conn);
		}

		return restCallResponse;
	}
	
	
	private void closeConnection(HttpURLConnection httpURLConnection) {
		try {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
				httpURLConnection = null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
 	public RestCallResponse uploadFile(String url, String request, Map<String, String> headers) throws Exception {
 		// Handling Rest Based file uploads
 		RestFileUploader fileUploader = new RestFileUploader(url, request, headers);
		return fileUploader.upload();
 	}
	
 
	public RestCallResponse downloadFile(String url, String request, Map<String, String> headers) throws Exception {
		
		File resourcesDirectory = new File("src/test/resources");
		String downLoadDirPath = resourcesDirectory.getAbsolutePath();
		downLoadDirPath = downLoadDirPath + File.separator + "downloads";
		
		FileDownloader fileDownloader = new FileDownloader(url, downLoadDirPath, headers);
		RestCallResponse restCallResponse = fileDownloader.finish();
		
		assertTrue("Could not download the file from server ! ", restCallResponse.getResponse() != null);
		return restCallResponse;
	}
	

	/**
	 * Reads an InputStream and converts it to a String.
	 */
	private String readStream(InputStream stream) {
		BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
		String line;
		StringBuffer response = new StringBuffer();
		try {
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}
			rd.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String result = response.toString();
		result = result.replaceAll("[\\t\\n\\r]"," ");
		return result.trim();
	}
	
	
	public static class RestCallResponse {
		private int status;
		private String response;
		private Map<String , String> headers;
		
		public int getStatus()
		{
			return status;
		}
		public void setStatus(int status)
		{
			this.status = status;
		}
		public String getResponse()
		{
			return response;
		}
		public void setResponse(String response)
		{
			this.response = response;
		}
		public void setHeaders(Map headers) {
			this.headers = headers;
		}
		
		public Map getHeaders() {
			return this.headers;
		}
	}
}