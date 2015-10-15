package com.tagdish.service;

import java.util.List;

import com.tagdish.domain.dto.detail.DetailInputDTO;
import com.tagdish.domain.dto.search.SearchInputDTO;
import com.tagdish.domain.elasticsearch.Dish;
import com.tagdish.exception.BizServiceException;

public interface IDishService {

	public abstract List<Dish> getByName(String name) throws BizServiceException;

	public abstract void addDish(Dish dish) throws BizServiceException;

	public abstract Dish findByDishId(Long dishId) throws BizServiceException;

	public abstract String getDishDetail(Long dishId) throws BizServiceException;
	
	public abstract String getDishList() throws BizServiceException;
	
	public abstract List<Dish> getDishByName(String name) throws BizServiceException;
	
	public abstract Dish getDishById(Long dishId) throws BizServiceException;

	public abstract String getDish(DetailInputDTO detailInputDTO) throws BizServiceException;

	public abstract String searchDish(SearchInputDTO searchInputDTO) throws BizServiceException;
}