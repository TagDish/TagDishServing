package com.tagdish.service.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.tagdish.constant.TagDishDomainConstant;
import com.tagdish.dao.elasticsearch.DishSearchQueryDSL;
import com.tagdish.dao.repository.DishRepository;
import com.tagdish.dao.repository.DishSearchRepository;
import com.tagdish.domain.dto.DishDTO;
import com.tagdish.domain.dto.RestaurantDTO;
import com.tagdish.domain.dto.RestaurantDishDTO;
import com.tagdish.domain.dto.TagDishInputDTO;
import com.tagdish.domain.dto.detail.DetailInputDTO;
import com.tagdish.domain.dto.search.SearchInputDTO;
import com.tagdish.domain.dto.search.SearchResultDTO;
import com.tagdish.domain.elasticsearch.Dish;
import com.tagdish.domain.elasticsearch.DishSearch;
import com.tagdish.domain.location.Location;
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
	
	@Autowired
	private DishSearchRepository dishSearchRepository;
	
	@Autowired
	private DishSearchQueryDSL dishSearchQueryDSL;
		
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
			calculateDistance(detailInputDTO, dish.getLocation(), restaurantDishDTO);
			if(restaurantDishDTO != null) {
				restaurantDishJson = gson.toJson(restaurantDishDTO);
			}			
		} else {
			
			restaurantDishJson = createNoEntityJson();
		}
		
		sendNotification(detailInputDTO, TagDishDomainConstant.VIEW_DISH_DETAIL_NOTIFY_TYPE);
	
		return restaurantDishJson;
	}
	
	@Override
	public String searchDish(SearchInputDTO searchInputDTO)
			throws BizServiceException {
	
		String searchResultJson = null;
		List<Long> zipCodeList = null;
		List<DishSearch> dishSearchList = null;
		LinkedList<RestaurantDishDTO> restaurantDishDTOList = null;
		SearchResultDTO searchResultDTO = null;
		
		validationService.validateInputDTO(searchInputDTO);
		zipCodeList =  locationService.getZipCode(searchInputDTO);
		
		dishSearchList = dishSearchQueryDSL.fuzzySearchDish(searchInputDTO.getSearchKeyWord(), zipCodeList);
		restaurantDishDTOList = covertToRestaruantDishDTOList(dishSearchList, searchInputDTO);
		searchResultDTO = createSearchResultDTO(restaurantDishDTOList, searchInputDTO);
		searchResultJson = createSearchResultJson(searchResultDTO);
		
		if(searchInputDTO.getStartIndex() == 0) {
			sendNotification(searchInputDTO, searchResultDTO, TagDishDomainConstant.SEARCH_NOTIFY_TYPE);	
		} else {
			sendNotification(searchInputDTO, searchResultDTO, TagDishDomainConstant.MORE_RESULTS_NOTIFY_TYPE);
		}
		
		
		return searchResultJson;
	}
	
	private SearchResultDTO createSearchResultDTO(LinkedList<RestaurantDishDTO> restaurantDishDTOList, 
			SearchInputDTO searchInputDTO) {
		
		SearchResultDTO searchResultDTO = null;
		
		if(restaurantDishDTOList != null && restaurantDishDTOList.size() > 0) {
			searchResultDTO = new SearchResultDTO();
			
			searchResultDTO.setResultSize(restaurantDishDTOList.size());
			searchResultDTO.setDishRestaurantList(restaurantDishDTOList);
			searchResultDTO.setResultToDisplay(restaurantDishDTOList.size());
			
			if(searchInputDTO.getTransactionId() == null || searchInputDTO.getTransactionId().trim().length() == 0) {
				searchInputDTO.setTransactionId(TagDishUtility.getUniqueId());
			} else {
				searchInputDTO.setTransactionId(searchInputDTO.getTransactionId());
			}
		}	
		return searchResultDTO;
	}
	
	private String createSearchResultJson(SearchResultDTO searchResultDTO) {
		
		String searchResultJson = null;
		if(searchResultDTO != null) {
			
			searchResultJson = gson.toJson(searchResultDTO);
		} else {
			searchResultJson = createNoEntityJson();
		}
		return searchResultJson;
	}

	private LinkedList<RestaurantDishDTO> covertToRestaruantDishDTOList(
			List<DishSearch> dishSearchList, SearchInputDTO searchInputDTO) throws BizServiceException {
		
		RestaurantDishDTO restaurantDishDTO = null;
		int startIndex;
		LinkedList<RestaurantDishDTO> restaurantDishDTOList = null;
		List<DishSearch> dishSearchSubList = null; 
		
		startIndex = searchInputDTO.getStartIndex();
		restaurantDishDTOList = new LinkedList<RestaurantDishDTO>();
		
		if(dishSearchList != null && dishSearchList.size() > 0) {

			dishSearchSubList = getSubDishList(dishSearchList, startIndex);
			if(dishSearchSubList != null && dishSearchSubList.size() > 0) {

				for (DishSearch dishSearch : dishSearchSubList) {
					
					restaurantDishDTO = convertToRestaurantDishDTO(dishSearch);
					calculateDistance(searchInputDTO, dishSearch.getLocation(), restaurantDishDTO);
					restaurantDishDTOList.add(restaurantDishDTO);
				}
			}			
		}
		return restaurantDishDTOList;
	}
	
	private List<DishSearch> getSubDishList(List<DishSearch> dishSearchList, int startIndex) {
		
		List<DishSearch> dishSearchSubList = null; 
		
		int toIndex = caluculateToIndex(dishSearchList, startIndex);
		int fromIndex = caluculateFromIndex(dishSearchList, startIndex);			
		dishSearchSubList = dishSearchList.subList(fromIndex, toIndex);
		
		return dishSearchSubList;
	}
	
	private <T> int caluculateFromIndex(List<T> list, int startIndex) {
		
		int fromIndex = 0;
		if(list.size() < startIndex) {
			fromIndex = startIndex;
		} else {
			fromIndex = list.size() - 1;
		}
		return fromIndex;
	}
	
	private <T> int  caluculateToIndex(List<T> list, int startIndex) {
		
		int toIndex = 0;
		
		if(list.size() < (startIndex + searchResultSize)) {
			toIndex = list.size();
		} else {
			toIndex = startIndex + searchResultSize;
		}
		
		return toIndex;
	}

	private void calculateDistance(TagDishInputDTO tagDishInputDTO, Location location, 
			RestaurantDishDTO restaurantDishDTO) throws BizServiceException {
		
		double distance;
		
		distance = -1;

		if(calculateDistanceFlag && location != null) {
			distance = locationService.calculateDistance(tagDishInputDTO, location);	
		}
		
		restaurantDishDTO.getRestaurantDTO().setDistance(distance);
	}

	private RestaurantDishDTO convertToRestaurantDishDTO(DishSearch dishSearch) {
		
		RestaurantDishDTO restaurantDishDTO = null;
		DishDTO dishDTO = null;
		RestaurantDTO restaurantDTO = null;
	
		restaurantDishDTO = new RestaurantDishDTO();
		dishDTO = new DishDTO();
		restaurantDTO = new RestaurantDTO();
		
		BeanUtils.copyProperties(dishSearch, dishDTO);
		BeanUtils.copyProperties(dishSearch, restaurantDTO);
		
		restaurantDishDTO.setDishDTO(dishDTO);
		restaurantDishDTO.setRestaurantDTO(restaurantDTO);
				
		return restaurantDishDTO;
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
