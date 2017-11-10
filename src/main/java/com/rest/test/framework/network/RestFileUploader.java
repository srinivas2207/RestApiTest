package com.rest.test.framework.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.rest.test.framework.network.RestNetworkUtil.RestCallResponse;


/**
 * Class for uploading files to rest URLs using jersey client implementation
 * @author SrinivasDonapati
 *
 */
public class RestFileUploader {
	private Map<String, String> headers = null;
	private String url = null;
	private String[] filePaths = null;
	private String requestPayLoad = null;
	
	public RestFileUploader(String url, String request, Map<String, String> headers)
			throws IOException {
		
		String[] requestFileds = request.split(";");

		filePaths = request.split(";")[1].split(",");
		
		if (requestFileds.length == 3) {
			requestPayLoad = requestFileds[2];
		}
		
		this.headers = headers;
		this.url = url;
	}

	/**
	 * Initiating HTTP upload
	 * @return
	 * @throws Exception
	 */
	public RestCallResponse upload() throws Exception {
		RestCallResponse responseObj = new RestCallResponse();
		
		URI uri = UriBuilder.fromUri(url).build();
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri);
		target.register(MultiPartFeature.class);
	    FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
		
	    String formDataName = "";
	    
		if (requestPayLoad != null && requestPayLoad.trim().length() > 0) {
			String formDataArr[] = requestPayLoad.split("\\|");
			for (String formData : formDataArr) {
				if (formData.contains("=")) {
					formDataMultiPart.field(formData.split("=")[0], formData.split("=")[1]);
				} else {
					formDataName = formData;
				}
			     
			}
		}
		
		for (String fileInfo : filePaths) {
			String filePath = fileInfo.split(",")[0];
			File resourcesDirectory = new File("src/test/resources");
			File file = new File(resourcesDirectory.getAbsolutePath()
					+ File.separator + filePath);
			if (file == null || !file.exists()) {
				throw new Exception("Could n't find the file to upload at : "
						+ filePath);
			}

			String fileName = file.getName();
			FormDataContentDisposition contentDisposition = FormDataContentDisposition
					.name(formDataName).fileName(fileName).build();

			FormDataBodyPart fdp = new FormDataBodyPart(formDataName,
					new FileInputStream(file),
					MediaType.APPLICATION_OCTET_STREAM_TYPE);
			fdp.setFormDataContentDisposition(contentDisposition);
			formDataMultiPart.bodyPart(fdp);
		}
		
		Builder requestBuilder = target.request();
		
		// Passing access token as header, if it's available
		MultivaluedMap reqHeaders = new MultivaluedHashMap();
		
		if (headers != null) {
			Iterator<Entry<String, String>> iterator = headers.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> header = iterator.next();
				reqHeaders.add(header.getKey(), header.getValue());
			}
		}
		requestBuilder.headers(reqHeaders); 
		
		Response response = requestBuilder.post(Entity.entity(formDataMultiPart, formDataMultiPart.getMediaType()));
	    int responseStatus = response.getStatus();
	    String responseData = response.readEntity(String.class);
	   
	    responseObj.setStatus(responseStatus);
	    responseObj.setResponse(responseData);
	    
	    return responseObj;
 	}

}
