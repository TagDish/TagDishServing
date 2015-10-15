package com.tagdish.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.tagdish.constant.TagDishConstant;
import com.tagdish.domain.dto.MessageDTO;
import com.tagdish.service.IBaseService;

@Service
public class BaseService implements IBaseService {

	@Autowired
	private Gson gson;	
	
	public String createNoEntityJson() {
		
		MessageDTO msg = null;
		String noEntityJson = null;
		
		msg = new MessageDTO(TagDishConstant.NO_ENTITY_FOUND, TagDishConstant.ERROR_MSG_TYPE);
		noEntityJson = gson.toJson(msg);
		
		return noEntityJson;
	}
}