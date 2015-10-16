package com.tagdish.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.tagdish.constant.TagDishConstant;
import com.tagdish.domain.dto.MessageDTO;
import com.tagdish.domain.dto.NotificationDTO;
import com.tagdish.domain.dto.detail.DetailInputDTO;
import com.tagdish.domain.dto.search.SearchInputDTO;
import com.tagdish.domain.dto.search.SearchResultDTO;
import com.tagdish.exception.BizServiceException;
import com.tagdish.service.IBaseService;
import com.tagdish.service.IHttpClientService;

@Service
public class BaseService implements IBaseService {

	private static final Logger logger = LoggerFactory
			.getLogger(BaseService.class);
	
	@Autowired
	private Gson gson;	
	
	@Autowired
	private IHttpClientService httpClientService;	
	
	@Value("${event.notification.url}")
	private String eventNotificationUrl;		
	
	public String createNoEntityJson() {
		
		MessageDTO msg = null;
		String noEntityJson = null;
		
		msg = new MessageDTO(TagDishConstant.NO_ENTITY_FOUND, TagDishConstant.ERROR_MSG_TYPE);
		noEntityJson = gson.toJson(msg);
		
		return noEntityJson;
	}
	
	public void sendNotification(DetailInputDTO detailInputDTO, String action) {
		
		NotificationDTO notificationDTO = null;
		String notificaitonJson = null;
		
		notificationDTO = createNotificationDTO(detailInputDTO, action);
		
		try {
			notificaitonJson = gson.toJson(notificationDTO);
			httpClientService.postEvents(notificaitonJson, eventNotificationUrl);
		} catch (BizServiceException e) {
			
			e.printStackTrace();
			logger.error("BaseService sendNotification - Unable to send the notification" + notificaitonJson, e);
		}
	}
	
	public void sendNotification(SearchInputDTO searchInputDTO, 
			SearchResultDTO searchResultDTO, String action) {
		
		NotificationDTO notificationDTO = null;
		String notificaitonJson = null;
		
		notificationDTO = createNotificationDTO(searchInputDTO, searchResultDTO, action);
		
		try {
			notificaitonJson = gson.toJson(notificationDTO);
			httpClientService.postEvents(notificaitonJson, eventNotificationUrl);
		} catch (BizServiceException e) {
			
			e.printStackTrace();
			logger.error("BaseService sendNotification - Unable to send the notification" + notificaitonJson, e);
		}
	}
	
	public NotificationDTO createNotificationDTO(DetailInputDTO detailInputDTO, String action) {
		
		NotificationDTO notificationDTO = null;
		
		notificationDTO = new NotificationDTO();
		notificationDTO.setAction(action);
		notificationDTO.setTrasactionId(detailInputDTO.getTransactionId());
		notificationDTO.setEntityId(String.valueOf(detailInputDTO.getId()));
		notificationDTO.setCount(1);
		
		return notificationDTO;
	}
	
	public NotificationDTO createNotificationDTO(SearchInputDTO searchInputDTO, 
			SearchResultDTO searchResultDTO, String action) {
		
		NotificationDTO notificationDTO = null;
		
		notificationDTO = new NotificationDTO();
		notificationDTO.setAction(action);
		notificationDTO.setTrasactionId(searchInputDTO.getTransactionId());
		notificationDTO.setSearchInputDTO(searchInputDTO);
		notificationDTO.setSearchResultDTO(searchResultDTO);
		notificationDTO.setCount(1);
		
		return notificationDTO;
	}
}