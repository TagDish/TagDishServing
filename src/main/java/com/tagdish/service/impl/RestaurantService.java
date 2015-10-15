package com.tagdish.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.tagdish.dao.repository.RestaurantRepository;
import com.tagdish.domain.dto.RestaurantDTO;
import com.tagdish.domain.dto.detail.DetailInputDTO;
import com.tagdish.domain.elasticsearch.Restaurant;
import com.tagdish.exception.BizServiceException;
import com.tagdish.service.ILocationService;
import com.tagdish.service.IRestaurantService;
import com.tagdish.service.IValidationService;

@Service
public class RestaurantService extends BaseService implements IRestaurantService {
	
	@Autowired
	private Gson gson;
	
	@Autowired
	private ILocationService locationService;	
	
	@Autowired
	private IValidationService validationService;		
	
	@Autowired
	private RestaurantRepository restaurantRepository;
	
	@Value("${calculate.distance.flag}")
	private boolean calculateDistanceFlag;
	
	@Override
	public List<Restaurant> getByName(String name) throws BizServiceException {

        return restaurantRepository.findByRestaurantName(name);
	}
	
	@Override
	public void addRestaurant(Restaurant restaurant) throws BizServiceException {
		restaurantRepository.save(restaurant);
	}
	
	@Override
	public Restaurant getRestaurantById(Long restaurantId) throws BizServiceException {
		return restaurantRepository.findByRestaurantId(restaurantId);
	}
	
	@Override
	public String getRestaurantDetail(DetailInputDTO detailInputDTO)
			throws BizServiceException {

		Restaurant restaurant = null;
		String restaurantJson = null;
		RestaurantDTO restaurantDTO = null;
		
		validationService.validateInputDTO(detailInputDTO);
		restaurant = getRestaurantById(detailInputDTO.getId());
		
		if(restaurant != null) {
			
			restaurantDTO = new RestaurantDTO();
			BeanUtils.copyProperties(restaurant, restaurantDTO);
			calculateDistance(detailInputDTO, restaurant, restaurantDTO);
			
			restaurantJson = gson.toJson(restaurant);
		} else {
			
			restaurantJson = createNoEntityJson();
		}
		
		return restaurantJson;
	}	
	
	private void calculateDistance(DetailInputDTO detailInputDTO, Restaurant restaurant, 
			RestaurantDTO restaurantDTO) throws BizServiceException {
		
		double distance;
		
		distance = -1;
		if(calculateDistanceFlag) {
			distance = locationService.calculateDistance(detailInputDTO, restaurant.getLocation());	
		}
		restaurantDTO.setDistance(distance);
	}

	@Override
	public String getRestaurantDetail(Long restaurantId) throws BizServiceException {
		
		String restaurantJson = null;
		Restaurant restaurant = null;
		
		restaurant = getRestaurantById(restaurantId);
		if(restaurant != null) {
			restaurantJson = gson.toJson(restaurant);
		}
		return restaurantJson;
	}
	
}
