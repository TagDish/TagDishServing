package com.tagdish.service.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.tagdish.dao.repository.DishRepository;
import com.tagdish.domain.dto.DishDTO;
import com.tagdish.domain.dto.RestaurantDTO;
import com.tagdish.domain.dto.RestaurantDishDTO;
import com.tagdish.domain.dto.TagDishInputDTO;
import com.tagdish.domain.dto.detail.DetailInputDTO;
import com.tagdish.domain.dto.search.SearchInputDTO;
import com.tagdish.domain.dto.search.SearchResultDTO;
import com.tagdish.domain.elasticsearch.Dish;
import com.tagdish.exception.BizServiceException;
import com.tagdish.service.IDishService;
import com.tagdish.service.ILocationService;
import com.tagdish.service.IValidationService;
import com.tagdish.util.TagDishUtility;

@Service
public class DishService extends BaseService implements IDishService {
	
	@Autowired
	private Gson gson;
	
	@Autowired
	private ILocationService locationService;
	
	@Autowired
	private IValidationService validationService;	
	
	@Autowired
	private DishRepository dishRepository;
	
	@Value("${calculate.distance.flag}")
	private boolean calculateDistanceFlag;
	
	@Value("${search.dish.size}")
	private int searchResultSize;
	
	@Override
	public List<Dish> getByName(String name) throws BizServiceException {

        return dishRepository.findByDishName(name);
	}
	
	@Override
	public void addDish(Dish dish) throws BizServiceException {
		dishRepository.save(dish);
	}
	
	@Override
	public Dish findByDishId(Long dishId) throws BizServiceException {
		return dishRepository.findByDishId(dishId);
	}
	
	@Override
	public Dish getDishById(Long dishId) throws BizServiceException {
		return dishRepository.getDishById(dishId);
	}
	
	@Override
	public List<Dish> getDishByName(String name) throws BizServiceException {
		return dishRepository.getDishByName(name);
	}	
		
	@Override
	public String getDishDetail(Long dishId) throws BizServiceException {
		
		String dishJson = null;
		Dish dish = null;
		
		dish = findByDishId(dishId);
		if(dish != null) {
			dishJson = gson.toJson(dish);
		}
		return dishJson;
	}
	
	@Override
	public String getDishList() throws BizServiceException {
		
		String dishJson = null;
		List<Dish> dishList = null;
		Iterable<Dish> dishIterable = null;
		
		dishIterable = dishRepository.findAll();
		if(dishIterable != null) {
			
			dishList = new ArrayList<Dish>();
			for (Dish dish : dishIterable) {
				dishList.add(dish);
			}
			if(dishList != null && dishList.size() > 0) {
				dishJson = gson.toJson(dishList);
			}
		}
		return dishJson;
	}

	@Override
	public String getDish(DetailInputDTO detailInputDTO)
			throws BizServiceException {
		
		Dish dish = null;
		String restaurantDishJson = null;
		RestaurantDishDTO restaurantDishDTO = null;
		
		validationService.validateInputDTO(detailInputDTO);
		dish = getDishById(detailInputDTO.getId());
		if(dish != null) {
			
			restaurantDishDTO = convertToRestaurantDishDTO(dish);
			calculateDistance(detailInputDTO, dish, restaurantDishDTO);
			if(restaurantDishDTO != null) {
				restaurantDishJson = gson.toJson(restaurantDishDTO);
			}			
		} else {
			
			restaurantDishJson = createNoEntityJson();
		}
	
		return restaurantDishJson;
	}
	
	@Override
	public String searchDish(SearchInputDTO searchInputDTO)
			throws BizServiceException {
	
		String searchResultJson = null;
		List<Long> zipCodeList = null;
		List<Dish> dishList = null;
		LinkedList<RestaurantDishDTO> restaurantDishDTOList = null;
		
		validationService.validateInputDTO(searchInputDTO);
		zipCodeList =  locationService.getZipCode(searchInputDTO);
		
		dishList = dishRepository.findByDishNameContainingAndZipCodeIn(searchInputDTO.getSearchKeyWord(), zipCodeList);
		restaurantDishDTOList = covertToRestaruantDishDTOList(dishList, searchInputDTO);
		searchResultJson = createSearchResultJson(restaurantDishDTOList, searchInputDTO);
		
		return searchResultJson;
	}
	
	private SearchResultDTO createSearchResultDTO(LinkedList<RestaurantDishDTO> restaurantDishDTOList, 
			SearchInputDTO searchInputDTO) {
		
		SearchResultDTO searchResultDTO = null;
		
		searchResultDTO = new SearchResultDTO();
		
		searchResultDTO.setResultSize(restaurantDishDTOList.size());
		searchResultDTO.setDishRestaurantList(restaurantDishDTOList);
		searchResultDTO.setResultToDisplay(restaurantDishDTOList.size());
		
		if(searchInputDTO.getTransactionId() == null || searchInputDTO.getTransactionId().trim().length() == 0) {
			searchInputDTO.setTransactionId(TagDishUtility.getUniqueId());
		} else {
			searchInputDTO.setTransactionId(searchInputDTO.getTransactionId());
		}
		return searchResultDTO;
	}
	 
	private String createSearchResultJson(LinkedList<RestaurantDishDTO> restaurantDishDTOList, 
			SearchInputDTO searchInputDTO) {
		
		String searchResultJson = null;
		SearchResultDTO searchResultDTO = null;
		
		if(restaurantDishDTOList != null && restaurantDishDTOList.size() > 0) {
			

			searchResultDTO = createSearchResultDTO(restaurantDishDTOList, searchInputDTO);
			searchResultJson = gson.toJson(searchResultDTO);
		} else {
			searchResultJson = createNoEntityJson();
		}
		return searchResultJson;
	}

	private LinkedList<RestaurantDishDTO> covertToRestaruantDishDTOList(
			List<Dish> dishList, SearchInputDTO searchInputDTO) throws BizServiceException {
		
		RestaurantDishDTO restaurantDishDTO = null;
		int startIndex;
		LinkedList<RestaurantDishDTO> restaurantDishDTOList = null;
		List<Dish> dishSubList = null; 
		
		startIndex = searchInputDTO.getStartIndex();
		restaurantDishDTOList = new LinkedList<RestaurantDishDTO>();
		
		if(dishList != null && dishList.size() > 0) {

			dishSubList = getSubDishList(dishList, startIndex);
			if(dishSubList != null && dishSubList.size() > 0) {

				for (Dish dish : dishSubList) {
					
					restaurantDishDTO = convertToRestaurantDishDTO(dish);
					calculateDistance(searchInputDTO, dish, restaurantDishDTO);
					restaurantDishDTOList.add(restaurantDishDTO);
				}
			}			
		}
		return restaurantDishDTOList;
	}
	
	private List<Dish> getSubDishList(List<Dish> dishList, int startIndex) {
		
		List<Dish> dishSubList = null; 
		
		int toIndex = caluculateToIndex(dishList, startIndex);
		int fromIndex = caluculateFromIndex(dishList, startIndex);			
		dishSubList = dishList.subList(fromIndex, toIndex);
		
		return dishSubList;
	}
	
	private int caluculateFromIndex(List<Dish> dishList, int startIndex) {
		
		int fromIndex = 0;
		if(dishList.size() < startIndex) {
			fromIndex = startIndex;
		} else {
			fromIndex = dishList.size() - 1;
		}
		return fromIndex;
	}
	
	private int caluculateToIndex(List<Dish> dishList, int startIndex) {
		
		int toIndex = 0;
		
		if(dishList.size() > (startIndex + searchResultSize)) {
			toIndex = dishList.size() - 1;
		} else {
			toIndex = startIndex + searchResultSize;
		}
		
		return toIndex;
	}

	private void calculateDistance(TagDishInputDTO tagDishInputDTO, Dish dish, 
			RestaurantDishDTO restaurantDishDTO) throws BizServiceException {
		
		double distance;
		
		distance = -1;
		System.out.println("calculateDistanceFlag" + calculateDistanceFlag);
		if(calculateDistanceFlag) {
			distance = locationService.calculateDistance(tagDishInputDTO, dish.getLocation());	
		}
		
		restaurantDishDTO.getRestaurantDTO().setDistance(distance);
	}

	private RestaurantDishDTO convertToRestaurantDishDTO(Dish dish) {
		
		RestaurantDishDTO restaurantDishDTO = null;
		DishDTO dishDTO = null;
		RestaurantDTO restaurantDTO = null;
	
		restaurantDishDTO = new RestaurantDishDTO();
		dishDTO = new DishDTO();
		restaurantDTO = new RestaurantDTO();
		
		BeanUtils.copyProperties(dish, dishDTO);
		BeanUtils.copyProperties(dish, restaurantDTO);
		
		restaurantDishDTO.setDishDTO(dishDTO);
		restaurantDishDTO.setRestaurantDTO(restaurantDTO);
				
		return restaurantDishDTO;
	}


}
