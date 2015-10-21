package com.tagdish.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.tagdish.constant.TagDishConstant;
import com.tagdish.dao.repository.ZipCodeRepository;
import com.tagdish.domain.dto.DishDTO;
import com.tagdish.domain.dto.TagDishInputDTO;
import com.tagdish.domain.dto.ZipCodeDTO;
import com.tagdish.domain.elasticsearch.Dish;
import com.tagdish.domain.elasticsearch.ZipCode;
import com.tagdish.domain.google.AddressComponent;
import com.tagdish.domain.google.GoogleResponse;
import com.tagdish.domain.location.Address;
import com.tagdish.domain.location.Location;
import com.tagdish.exception.BizServiceException;
import com.tagdish.service.ILocationService;

@Service
public class LocationService extends BaseService implements ILocationService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(LocationService.class);	
	
	@Autowired
	private Gson gson;	
	
	@Autowired
	private ZipCodeRepository zipCodeRepository;
	
	// Working Url
	
	//Lat /Log <-> Address conversion
	//http://maps.googleapis.com/maps/api/geocode/json?address=Torrance,CA&sensor=false
	//http://maps.googleapis.com/maps/api/geocode/json?address=90503&sensor=false
	//http://maps.googleapis.com/maps/api/geocode/json?latlng=33.84136%2C-118.3546&sensor=false
	
	//Distance Url
	//https://maps.googleapis.com/maps/api/distancematrix/json?origins=Vancouver+BC|Seattle&destinations=San+Francisco|Victoria+BC"
	
	//Direction Url
	//http://maps.googleapis.com/maps/api/directions/xml?sensor=true&origin=Chicago&destination=Orlando"
	
	public double calculateDistance(TagDishInputDTO tagDishInputDTO,
			Location location) throws BizServiceException {
		
		double distance = -1 ; 
		Location loc = null;
		if(TagDishConstant.LAT_LONG_TYPE.equalsIgnoreCase(tagDishInputDTO.getTypeOfAddress())) {
			distance = distance(location.getLatitude(), location.getLongitude(), 
					tagDishInputDTO.getLocation().getLatitude(), tagDishInputDTO.getLocation().getLongitude(), null);
		}
		
		if(TagDishConstant.ZIP_CODE_TYPE.equalsIgnoreCase(tagDishInputDTO.getTypeOfAddress())) {
			loc = convertZipCodeToLocation(tagDishInputDTO.getAddress().getZipcode());
			
			distance = distance(location.getLatitude(), location.getLongitude(), 
					loc.getLatitude(), loc.getLongitude(), null);			
		}
		
		if(TagDishConstant.CITY_STATE_TYPE.equalsIgnoreCase(tagDishInputDTO.getTypeOfAddress())) {
			loc = convertCityToLocation(tagDishInputDTO.getAddress().getCity(), tagDishInputDTO.getAddress().getState());
			
			distance = distance(location.getLatitude(), location.getLongitude(), 
					loc.getLatitude(), loc.getLongitude(), null);			
		}
		
		return distance;
	}
	
	
	@Override
	public double calculateDistance(double lat1, double lon1, long zipCode) throws BizServiceException {
		
		Location location = null;
		
		location = convertZipCodeToLocation(zipCode);
		return distance(lat1, lon1, location.getLatitude(), location.getLongitude(), null);
	}
	
	@Override
	public double calculateDistance(double lat1, double lon1, String city, String state) throws BizServiceException {
		
		Location location = null;
		
		location = convertCityToLocation(city, state);
		return distance(lat1, lon1, location.getLatitude(), location.getLongitude(), null);		
	}
	
	@Override
	public double distance(String lat1, String lon1, String lat2, String lon2, String unit) {
		double distance = -1;
		if(lat1 != null && lon1 != null && lat2 != null && lon2 != null) {
			distance = distance(Double.valueOf(lat1), Double.valueOf(lon1), Double.valueOf(lat2), Double.valueOf(lon2), unit);
		}
		return distance;
	}
	
	@Override
	public double distance(double lat1, double lon1, String lat2, String lon2, String unit) {
		
		double distance = -1;
		if(lat2 != null && lon2 != null) {
			distance = distance(lat1, lon1, Double.valueOf(lat2), Double.valueOf(lon2), unit);
		}
		return distance;
	}

	@Override
	public double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (TagDishConstant.KILOMETER.equalsIgnoreCase(unit)) {
			dist = dist * 1.609344;
		} else if (TagDishConstant.NAUTICAL_MILES.equalsIgnoreCase(unit)) {
			dist = dist * 0.8684;
		}

		return (dist);
	}
	
	@Override
	public Location convertZipCodeToLocation(long zipCode) throws BizServiceException {
		
		return getLocation(String.valueOf(zipCode));
	}	
	
	@Override
	public Location convertCityToLocation(String city, String state) throws BizServiceException {
				
		return getLocation(city + "," + state);
	}
	
	@Override
	public String getCity(String city) throws BizServiceException {
		
		List<ZipCode> zipCodeList = null;
		List<ZipCodeDTO> zipCodeDTOList = null;
		ZipCodeDTO zipCodeDTO = null;
		String zipCodeJson = null;
		
		
		zipCodeList = zipCodeRepository.findByCityLikeIgnoreCase(city);
		if(zipCodeList != null && zipCodeList.size() > 0) {

			zipCodeDTOList = new ArrayList<ZipCodeDTO>();
			for (ZipCode zipCode : zipCodeList) {
				zipCodeDTO = new ZipCodeDTO();
				
				BeanUtils.copyProperties(zipCode, zipCodeDTO);
				zipCodeDTO.setDisplayName(zipCodeDTO.getCity() + " , " + zipCodeDTO.getState());
				
				zipCodeDTOList.add(zipCodeDTO);
			}
			
			zipCodeJson = gson.toJson(zipCodeDTOList);
		} else {
		
			zipCodeJson = createNoEntityJson();
		}
		
		return zipCodeJson;
	}	
	
	@Override
	public List<Long> getZipCode(TagDishInputDTO tagDishInputDTO) throws BizServiceException {

		List<Long> zipCodeList = null;
		long zipCode = 0;
		
		if(TagDishConstant.LAT_LONG_TYPE.equalsIgnoreCase(tagDishInputDTO.getTypeOfAddress())) {
			zipCodeList = new ArrayList<Long>();
			zipCode = getZipCode(tagDishInputDTO.getLocation().getLatitude() + "," + tagDishInputDTO.getLocation().getLongitude());
			zipCodeList.add(zipCode);
		}
		
		if(TagDishConstant.ZIP_CODE_TYPE.equalsIgnoreCase(tagDishInputDTO.getTypeOfAddress())) {
			zipCodeList = new ArrayList<Long>();
			zipCode = tagDishInputDTO.getAddress().getZipcode();
			zipCodeList.add(zipCode);
		}
		
		if(TagDishConstant.CITY_STATE_TYPE.equalsIgnoreCase(tagDishInputDTO.getTypeOfAddress())) {
			zipCodeList = getZipCodeListByCityAndState(tagDishInputDTO.getAddress().getCity(), tagDishInputDTO.getAddress().getState());
		}
		
		return zipCodeList;
	}
	
	private List<Long> getZipCodeListByCityAndState(String city, String state) throws BizServiceException {
		
		List<ZipCode> zipCodeList = null;
		List<Long> zipCodeLongList = null;
		
		zipCodeList = zipCodeRepository.findByCityAndState(city, state);
		
		zipCodeLongList = new ArrayList<Long>();
		for (ZipCode zipCode : zipCodeList) {
			zipCodeLongList.add(zipCode.getZipCode());
		}
		return zipCodeLongList;
	}

	private GoogleResponse getGoogleResponse(String addressParameter) throws BizServiceException {
		
		GoogleResponse gr = null;
		String str = null;
		try {
			str = URLEncoder.encode(addressParameter, TagDishConstant.UTF_ENCODING);
		} catch (UnsupportedEncodingException e) {
			
			logger.error("UnsupportedEncodingException Occurred for addressParameter" + addressParameter, e);
			throw new BizServiceException("LocationService - getLocation - UnsupportedEncodingException Occurred for urlString" + addressParameter);
		}
		
		String urlString = TagDishConstant.GOOGLE_API_BASE_URL + "?address="
				    + str + "&sensor=false";
		gr = callGoogleService(urlString);
				 
		return gr;		
	}
	
	private long getZipCode(String addressParameter)  throws BizServiceException {
		
		long zipCode = -1;
		GoogleResponse gr = null;
		
		List<AddressComponent> addressComponentList = null;

		gr = getGoogleResponse(addressParameter);
		
		if(gr.getStatus() != null && gr.getStatus().equalsIgnoreCase(TagDishConstant.GOOGLE_SUCCESS_RESPONSE)) {
			addressComponentList = gr.getResults()[0].getAddressComponentList();
			
			for (AddressComponent addressComponent : addressComponentList) {
				
				for (int i = 0; i < addressComponent.getTypes().length; i++) {
					if(addressComponent.getTypes()[i].equalsIgnoreCase("postal_code")) {
						zipCode = Long.getLong(addressComponent.getLongName());
					}
				}
			}
		}
		 
		return zipCode;		
	}
	
	private Location getLocation(String addressParameter)  throws BizServiceException {
		
		Location location = null;
		GoogleResponse gr = null;

		gr = getGoogleResponse(addressParameter);
		
		if(gr.getStatus() != null && gr.getStatus().equalsIgnoreCase(TagDishConstant.GOOGLE_SUCCESS_RESPONSE)) {
			location = gr.getResults()[0].getGeometry().getLocation();
		}
		 
		return location;		
	}
	
	private GoogleResponse callGoogleService(String urlString) throws BizServiceException{
		
		GoogleResponse gr = null;
		Reader reader = null;
		InputStream in = null;
		try {
			URL url = new URL(urlString);
			// Open the Connection
			URLConnection conn = url.openConnection();

			in = conn.getInputStream();
			reader = new InputStreamReader(in);
			gr = gson.fromJson(reader, GoogleResponse.class);


		} catch (JsonSyntaxException e) {

			logger.error("JsonSyntaxException Occurred for urlString" + urlString, e);
			throw new BizServiceException("LocationService - getGoogleResponse - JsonSyntaxException Occurred for urlString" + urlString);
		} catch (JsonIOException e) {

			logger.error("JsonIOException Occurred for urlString" + urlString, e);
			throw new BizServiceException("LocationService - getGoogleResponse - JsonIOException Occurred for urlString" + urlString);
		} catch (MalformedURLException e) {

			logger.error("MalformedURLException Occurred for urlString" + urlString, e);
			throw new BizServiceException("LocationService - getGoogleResponse - MalformedURLException Occurred for urlString" + urlString);			
		} catch (IOException e) {
			
			logger.error("IOException Occurred for urlString" + urlString, e);
			throw new BizServiceException("LocationService - getGoogleResponse - IOException Occurred for urlString" + urlString);
		} finally {
			
			try {
				if(reader != null) {
					reader.close();	
				}
				
			} catch (IOException e) {
				
				logger.error("IOException Occurred for urlString" + urlString, e);
				throw new BizServiceException("LocationService - getGoogleResponse - IOException Occurred while closing reader for urlString" + urlString);
			}				
			
			try {
				if(in != null) {
					in.close();	
				}
			
			} catch (IOException e) {
				
				logger.error("IOException Occurred for urlString" + urlString, e);
				throw new BizServiceException("LocationService - getGoogleResponse - IOException Occurred while closing inputstream for urlString" + urlString);
			}
			
		}

		return gr;
	}
	
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}	
	
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
	
	public static void main1(String[] args) throws IOException {
		
		// Torrance latitude longitude
		String latlongString =  "33.84136,-118.3546";
		
		// Mumbai latitude longitude
		//String latlongString =  "18.92038860,72.83013059999999";
		String str = URLEncoder.encode(latlongString, "UTF-8");
		System.out.println(str);
		
		 URL url = new URL(TagDishConstant.GOOGLE_API_BASE_URL + "?latlng="
				    + str + "&sensor=false");
		// Open the Connection
		URLConnection conn = url.openConnection();

		InputStream in = conn.getInputStream();

		Gson gson = new Gson();
		Reader reader = new InputStreamReader(in);
		GoogleResponse gr = gson.fromJson(reader, GoogleResponse.class);
		// gson.
		// ObjectMapper mapper = new ObjectMapper();
		// GoogleResponse response =
		// (GoogleResponse)mapper.readValue(in,GoogleResponse.class);
		reader.close();
		in.close();

		System.out.println(gr.getStatus());
		// return response;
	}
	
	public static void main(String[] args) {
		
		Dish dish = new Dish();
		dish.setDishId(1l);
		dish.setDishName("Dadfas");
		Address ad = new Address();
		ad.setCity("Trorrance");
		dish.setAddress(ad);
		
		
		DishDTO dishDTO = new DishDTO();
		
		System.out.println("null" + dishDTO.getDishId());
		System.out.println("null" + dishDTO.getDishName());
		BeanUtils.copyProperties(dish, dishDTO);
		
		System.out.println(dishDTO.getDishId());
		System.out.println(dishDTO.getDishName());
		//System.out.println(dishDTO.getAddress().getCity());
	}
}
