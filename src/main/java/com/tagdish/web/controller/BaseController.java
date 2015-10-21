package com.tagdish.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.Gson;
import com.tagdish.constant.TagDishConstant;
import com.tagdish.domain.dto.MessageDTO;
import com.tagdish.domain.dto.TagDishInputDTO;
import com.tagdish.domain.dto.detail.DetailInputDTO;
import com.tagdish.domain.dto.search.SearchInputDTO;
import com.tagdish.domain.location.Address;
import com.tagdish.domain.location.Location;

@Controller
@RequestMapping("/base")
public class BaseController {
	
	@Autowired
	Gson gson;	 	
		
	public String createErrorJson(Exception e) {

		MessageDTO msg = new MessageDTO();
		
		msg.setType(TagDishConstant.ERROR_MSG_TYPE);
		if(e.getMessage() != null && e.getMessage().length() > 0) {
			msg.setMessage(e.getMessage());	
		} else {
			msg.setMessage(TagDishConstant.ERROR_RESPONSE);
		}
		
		return gson.toJson(msg);
	}
	
	public String createSuccessJson() {

		MessageDTO msg = new MessageDTO(TagDishConstant.SUCCESS_RESPONSE, TagDishConstant.SUCESS_MSG_TYPE);
				
		return gson.toJson(msg);
	}	
	
	
	public DetailInputDTO createDetailInputDTO(long id, String lantitude, String longtitude,
			Long zipCode, String city, String state, String transactionId, Long timestamp) {
		
		DetailInputDTO detailInputDTO = new DetailInputDTO();
		
		detailInputDTO.setId(id);
		detailInputDTO.setTransactionId(transactionId);
		if(timestamp == null) {
			detailInputDTO.setTimestamp(System.currentTimeMillis());	
		} else {
			detailInputDTO.setTimestamp(timestamp);
		}
		
		createLocation(lantitude, longtitude, zipCode, city, state, detailInputDTO);
		
		return detailInputDTO;
	}
	
	public SearchInputDTO createSearchInputDTO(String searchKeyWord, Integer startIndex, String lantitude, String longtitude,
			Long zipCode, String city, String state, String transactionId, Long timestamp) {
		
		SearchInputDTO searchInputDTO = new SearchInputDTO();
		
		searchInputDTO.setSearchKeyWord(searchKeyWord);
		if(startIndex == null) {
			searchInputDTO.setStartIndex(0);
		} else {
			searchInputDTO.setStartIndex(startIndex);
		}
		
		searchInputDTO.setTransactionId(transactionId);
		if(timestamp == null) {
			searchInputDTO.setTimestamp(System.currentTimeMillis());	
		} else {
			searchInputDTO.setTimestamp(timestamp);
		}		
		searchInputDTO.setTimestamp(timestamp);
		createLocation(lantitude, longtitude, zipCode, city, state, searchInputDTO);

		return searchInputDTO;
	}	
	
	private void createLocation(String lantitude, String longtitude,
			Long zipCode, String city, String state, 
			TagDishInputDTO tagDishInputDTO) {
		
		assignLocation(lantitude, longtitude, tagDishInputDTO);
		assignAddress(zipCode, city, state, tagDishInputDTO);
	}
	
	private void assignLocation(String lantitude, String longtitude,
			TagDishInputDTO tagDishInputDTO) {
		
		Location location = null;
		
		if(lantitude != null || longtitude != null) {
			location = new Location();
			location.setLatitude(lantitude);
			location.setLongitude(longtitude);
			tagDishInputDTO.setLocation(location);	
		}

	}
	
	private void assignAddress(Long zipCode, String city, String state, 
			TagDishInputDTO tagDishInputDTO) {
		
		Address address = null;
		
		if(zipCode != null || city != null || state != null) {
			
			address = new Address();
			address.setZipcode(zipCode);
			address.setCity(city);
			address.setState(state);
			tagDishInputDTO.setAddress(address);
		}
	}	
}
