package com.tagdish.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tagdish.exception.BizServiceException;
import com.tagdish.service.IHttpClientService;


@Service
public class HttpClientService implements IHttpClientService {

	private static final Logger logger = LoggerFactory
			.getLogger(HttpClientService.class);
	
	@Value("${httpclient.timeout}")
	private int httpClientTimeout;	
	
	@Value("${connection.timeout}")
	private int connectionTimeout;		
	
	public void postEvents(String body, String url) throws BizServiceException {
		
		HttpClient httpClient = null;
		PostMethod poster = null;

    	try {
    		httpClient = createHttpClient(httpClientTimeout, connectionTimeout);
			poster = new PostMethod(url);
			poster.setRequestHeader("Content-Type","application/json");

			StringRequestEntity entity=new StringRequestEntity(body);
			poster.setRequestEntity(entity);
			poster.setRequestHeader("Content-Length", "" + body.length());
			poster.setRequestHeader("Cache-Control", "no-cache");
			
			httpClient.executeMethod(poster);
			readResponse(poster);
			
		} catch (HttpException e) {

			e.printStackTrace();
			logger.error("EventNotificationService postEvents HttpException exectpion occured while calling service", e);
			throw new BizServiceException("EventNotificationService postEvents HttpException exectpion occured while calling service");
		} catch (IOException e) {

			e.printStackTrace();
			logger.error("EventNotificationService postEvents IOException exectpion occured while calling service", e);
			throw new BizServiceException("EventNotificationService postEvents IOException exectpion occured while calling service");
		} finally {

    	    closeConnection(poster);
		}

	}
	
	private void closeConnection(PostMethod poster) {
		if (poster != null) {
			poster.releaseConnection();
		}
	}
	
	private String readResponse(PostMethod poster) throws BizServiceException {
		
	    InputStream is = null;
	    InputStreamReader isr = null;
	    BufferedReader reader = null;
	    String response = null;
        try {
            is = poster.getResponseBodyAsStream();
            isr  = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            response = out.toString();   //Prints the string content read from input stream
            
        } catch (IOException e) {
            
            e.printStackTrace();
            logger.error("EventNotificationService readResponse IOException exectpiont occured", e);
            throw new BizServiceException("EventNotificationService readResponse IOException exectpiont occured");
        } finally {
        	
        	try {
				reader.close();
			} catch (IOException e) {

				e.printStackTrace();
				logger.error("EventNotificationService readResponse IOException exectpion occured while closing BufferedReader", e);
				throw new BizServiceException("EventNotificationService readResponse IOException exectpion occured while closing BufferedReader");
			}
        	try {
				isr.close();
			} catch (IOException e) {
				
				e.printStackTrace();
				logger.error("EventNotificationService readResponse IOException exectpion occured while closing InputStreamReader", e);
				throw new BizServiceException("EventNotificationService readResponse IOException exectpion occured while closing InputStreamReader");
			}
        	try {
				is.close();
			} catch (IOException e) {

				e.printStackTrace();
				logger.error("EventNotificationService readResponse IOException exectpion occured while closing InputStream", e);
				throw new BizServiceException("EventNotificationService readResponse IOException exectpion occured while closing InputStream");
			}
        }
        return response;
	}
	
	private HttpClient createHttpClient(long httpClientTimeout, long connectionTimeout) {

		HttpClient httpClient = null;

		httpClient = new HttpClient();
		httpClient.getHttpConnectionManager().getParams()
				.setSoTimeout((int) httpClientTimeout);
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout((int) (connectionTimeout));
		httpClient.getHttpConnectionManager().getParams()
				.setStaleCheckingEnabled(true);
		httpClient.getHttpConnectionManager().getParams()
				.setDefaultMaxConnectionsPerHost(1);
		httpClient.getHttpConnectionManager().getParams()
				.setMaxTotalConnections(1);
		
		return httpClient;
	}
	
}
