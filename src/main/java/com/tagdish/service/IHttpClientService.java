package com.tagdish.service;

import com.tagdish.exception.BizServiceException;

public interface IHttpClientService {

	public void postEvents(String body, String url) throws BizServiceException;
}