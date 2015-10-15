package com.tagdish.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tagdish.domain.dto.search.SearchInputDTO;
import com.tagdish.exception.BizServiceException;
import com.tagdish.service.IDishService;


@Controller
@RequestMapping("/dish/search")
public class DishSearchController extends BaseController {
	
	private static final Logger logger = LoggerFactory
			.getLogger(DishSearchController.class);		
	
    @Autowired
    private IDishService dishService;		
	
	@RequestMapping(value = "/{searchKeyWord}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	String searchDish(@PathVariable String searchKeyWord,
			@RequestParam(value = "lant", required = false) String lantitude,
			@RequestParam(value = "long", required = false) String longtitude,
			@RequestParam(value = "zipcode", required = false) Long zipCode,
			@RequestParam(value = "city", required = false) String city,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "startIndex", required = false) int startIndex,
			@RequestParam(value = "transcationId", required = false) String transcationId,
			Model model) {	
		logger.debug(" DishSearchController .. searchDish()");
		
		long start = System.currentTimeMillis();
		String responseJson = null;
		SearchInputDTO searchInputDTO = null;
		try {
			logger.debug("DishSearchController .. searchDish .. searchKeyWord .." + searchKeyWord);
			
			searchInputDTO = createSearchInputDTO(searchKeyWord, startIndex, lantitude, longtitude, zipCode, city, state, transcationId);
			responseJson = dishService.searchDish(searchInputDTO);
		} catch (BizServiceException e) {

			e.printStackTrace();
			logger.error("BizServiceException occured @ DishSearchController.searchDish", e);
			responseJson = createErrorJson(e);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Exception occured @ DishSearchController.searchDish", e);
			responseJson = createErrorJson(e);
		}
		
		long end = System.currentTimeMillis();
		logger.info("DishSearchController searchDish " + (end - start) + ",in ms");		

		return responseJson;
	}	
}
