package com.rest.test.framework.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.rest.test.framework.network.RestNetworkUtil.RestCallResponse;

/**
 * HTTP Util method for downloading files
 * @author SrinivasDonapati
 *
 */
public class FileDownloader {
	private HttpURLConnection httpConn;
	private static final int BUFFER_SIZE = 4096;
	private String dirPath = null;
	
	/**
	 * Initializing the file downloader
	 * @param serverUrl Valid URL
	 * @param dirPath Location of the directory, in which the file to be stored
	 * @param headers headers
	 * @throws IOException
	 */
	public FileDownloader(String serverUrl, String httpMethod, String dirPath, Map<String, String> headers) throws IOException {
		this.dirPath = dirPath;
		
		URL url = new URL(serverUrl);
		httpConn = (HttpURLConnection) url.openConnection();
		if (httpMethod != null && httpMethod.trim().length() > 0) {
			httpConn.setRequestMethod(httpMethod.toUpperCase());
		}
        
        if (headers != null) {
			Iterator<Entry<String, String>> iterator = headers.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> header = iterator.next();
				httpConn.setRequestProperty(header.getKey(),  header.getValue()); 
			}
		}
	}
	
	/**
	 * Firing download operation
	 * @return {@link RestCallResponse} object containing the HTTP response information
	 * @throws IOException
	 */
	public RestCallResponse finish() throws IOException {
		RestCallResponse restCallResponse = new RestCallResponse();
		String fileName = null;
		int responseCode = httpConn.getResponseCode();
		 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            }
            
            // If response's Content-Disposition header is not available,
            // Using the URL to fetch the file name
            if (fileName == null) {
            	fileName = _fetchFileName(httpConn.getURL().toString());
            }
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = dirPath + File.separator + fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
            httpConn.disconnect();
            
            restCallResponse.setResponse(fileName);
        } else {
        	InputStream is = httpConn.getErrorStream();
        	String errorMsg = RestNetworkUtil.readStream(is);
        	restCallResponse.setResponse(errorMsg);
        }
        
        restCallResponse.setStatus(responseCode);
        return restCallResponse;
	}
	
	private String _fetchFileName(String url) {
		String[] fields = url.split("/");
		if (fields.length > 0) {
			return fields[fields.length-1];
		}
		return "file_" + new Date().getTime();
	}

}
