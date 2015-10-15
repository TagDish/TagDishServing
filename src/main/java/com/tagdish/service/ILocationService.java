package com.tagdish.service;

import java.util.List;

import com.tagdish.domain.dto.TagDishInputDTO;
import com.tagdish.domain.location.Location;
import com.tagdish.exception.BizServiceException;

public interface ILocationService {

	public abstract double calculateDistance(double lat1, double lon1,
			long zipCode) throws BizServiceException;

	public abstract double calculateDistance(double lat1, double lon1,
			String city, String state) throws BizServiceException;

	public abstract double distance(String lat1, String lon1, String lat2,
			String lon2, String unit);

	public abstract double distance(double lat1, double lon1, String lat2,
			String lon2, String unit);

	public abstract double distance(double lat1, double lon1, double lat2,
			double lon2, String unit);

	public abstract Location convertZipCodeToLocation(long zipCode)
			throws BizServiceException;

	public abstract Location convertCityToLocation(String city, String state)
			throws BizServiceException;

	public abstract double calculateDistance(TagDishInputDTO tagDishInputDTO,
			Location location) throws BizServiceException;
	
	public abstract List<Long> getZipCode(TagDishInputDTO tagDishInputDTO) throws BizServiceException;

	public abstract String getCity(String city) throws BizServiceException;

}