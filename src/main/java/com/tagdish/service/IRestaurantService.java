package com.tagdish.service;

import java.util.List;

import com.tagdish.domain.dto.detail.DetailInputDTO;
import com.tagdish.domain.elasticsearch.Restaurant;
import com.tagdish.exception.BizServiceException;

public interface IRestaurantService {

	public abstract List<Restaurant> getByName(String name) throws BizServiceException;

	public abstract void addRestaurant(Restaurant dish) throws BizServiceException;

	public abstract Restaurant getRestaurantById(Long dishId) throws BizServiceException;

	public abstract String getRestaurantDetail(Long dishId) throws BizServiceException;

	public abstract String getRestaurantDetail(DetailInputDTO detailInputDTO) throws BizServiceException;

}