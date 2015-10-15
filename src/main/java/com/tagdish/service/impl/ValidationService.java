package com.tagdish.service.impl;

import org.springframework.stereotype.Service;

import com.tagdish.constant.TagDishConstant;
import com.tagdish.domain.dto.TagDishInputDTO;
import com.tagdish.domain.location.Address;
import com.tagdish.domain.location.Location;
import com.tagdish.exception.BizServiceException;
import com.tagdish.service.IValidationService;

@Service
public class ValidationService extends BaseService implements IValidationService {

	public void validateInputDTO(TagDishInputDTO tagDishInputDTO) throws BizServiceException {
		
		String typeOfAddress = null;
		if(tagDishInputDTO == null) {
			throw new BizServiceException(TagDishConstant.IMPROPER_LOCATION);
		}
		
		if(tagDishInputDTO.getLocation() != null) {
		
			validateLocation(tagDishInputDTO.getLocation());
			typeOfAddress = TagDishConstant.LAT_LONG_TYPE;
			
		} else if(tagDishInputDTO.getAddress() != null) {
			
			if(validateZipCode(tagDishInputDTO.getAddress())) {
				typeOfAddress = TagDishConstant.ZIP_CODE_TYPE;	
			} else if(validateCityAndState(tagDishInputDTO.getAddress())) {
				typeOfAddress = TagDishConstant.CITY_STATE_TYPE;	
			}
		}
		
		if(typeOfAddress != null) {
			tagDishInputDTO.setTypeOfAddress(typeOfAddress);
		} else {
			throw new BizServiceException(TagDishConstant.IMPROPER_LOCATION);
		}
	}
	
	public void validateLocation(Location location) throws BizServiceException {
	
		if(location.getLatitude() != null || location.getLongitude() != null) {

			if(location.getLatitude() == null){
				throw new BizServiceException(TagDishConstant.IMPROPER_LAT_LONG_LOCATION);
			}
			if(location.getLongitude() == null) {
				throw new BizServiceException(TagDishConstant.IMPROPER_LAT_LONG_LOCATION);
			}
		}
		
		if(location.getLatitude() != null && location.getLongitude() != null) {
			
			try {
				Double.parseDouble(location.getLatitude());
			} catch (NumberFormatException e) {
				throw new BizServiceException(TagDishConstant.IMPROPER_LAT_LONG_LOCATION);
			}
			
			try {
				Double.parseDouble(location.getLongitude());
			} catch (NumberFormatException e) {
				throw new BizServiceException(TagDishConstant.IMPROPER_LAT_LONG_LOCATION);
			}
		}
	}
	
	public boolean validateZipCode(Address address) throws BizServiceException {
		
		boolean validateZipCode = true;
		if(address.getZipcode() != null && address.getZipcode().toString().trim().length() != 5) {

			throw new BizServiceException(TagDishConstant.IMPROPER_ZIP_CODE_LOCATION);
		}
		return validateZipCode;
	}
	
	public boolean validateCityAndState(Address address) throws BizServiceException {
		
		boolean validateCityAndState = true;
		if(address.getCity() != null || address.getState() != null) {

			if(address.getCity() == null || address.getCity().trim().length() == 0){
				throw new BizServiceException(TagDishConstant.IMPROPER_CITY_STATE_LOCATION);
			}
			if(address.getState() == null || address.getState().trim().length() == 0) {
				throw new BizServiceException(TagDishConstant.IMPROPER_CITY_STATE_LOCATION);
			}
		}
		
		if(address.getState() != null && address.getState().length() != 2) {
			throw new BizServiceException(TagDishConstant.IMPROPER_CITY_STATE_LOCATION);
		}
		
		return validateCityAndState;
	}

}
