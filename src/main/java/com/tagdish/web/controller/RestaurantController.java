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

import com.tagdish.domain.dto.detail.DetailInputDTO;
import com.tagdish.exception.BizServiceException;
import com.tagdish.service.IRestaurantService;

@Controller
@RequestMapping("/restuarant")
public class RestaurantController extends BaseController {
	
	private static final Logger logger = LoggerFactory
			.getLogger(RestaurantController.class);		

    @Autowired
    private IRestaurantService restuarantService;	
    
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	String getRestaurantDetail(@PathVariable long id,
			Model model) {	
		logger.debug(" RestaurantController .. getRestaurantDetail()");
		
		long start = System.currentTimeMillis();
		String responseJson = null;
		
		try {
			logger.debug("RestaurantController .. getRestaurantDetail .. id .." + id);
			responseJson = restuarantService.getRestaurantDetail(id);
		} catch (BizServiceException e) {

			e.printStackTrace();
			logger.error("BizServiceException occured @ RestaurantController.getRestaurantDetail", e);
			responseJson = createErrorJson(e);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Exception occured @ RestaurantController.getRestaurantDetail", e);
			responseJson = createErrorJson(e);
		}
		
		long end = System.currentTimeMillis();
		logger.info("RestaurantController getRestaurantDetail " + (end - start) + ",in ms");		

		return responseJson;
	}	    
    
	@RequestMapping(value = "/get/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	String getRestaurantDetail(@PathVariable long id,
			@RequestParam(value = "lant", required = false) String lantitude,
			@RequestParam(value = "long", required = false) String longtitude,
			@RequestParam(value = "zipcode", required = false) Long zipCode,
			@RequestParam(value = "city", required = false) String city,
			@RequestParam(value = "state", required = false) String state,	
			@RequestParam(value = "transactionId", required = false) String transactionId,
			@RequestParam(value = "timestamp", required = false) Long timestamp,
			Model model) {	
		logger.debug(" RestaurantController .. getRestaurantDetail()");
		
		long start = System.currentTimeMillis();
		String responseJson = null;
		DetailInputDTO detailInputDTO = null;
		
		try {
			logger.debug("RestaurantController .. getRestaurantDetail .. id .." + id);
			detailInputDTO = createDetailInputDTO(id, lantitude, longtitude, zipCode, city, state, transactionId, timestamp);
			responseJson = restuarantService.getRestaurantDetail(detailInputDTO);
		} catch (BizServiceException e) {

			e.printStackTrace();
			logger.error("BizServiceException occured @ RestaurantController.getRestaurantDetail", e);
			responseJson = createErrorJson(e);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Exception occured @ RestaurantController.getRestaurantDetail", e);
			responseJson = createErrorJson(e);
		}
		
		long end = System.currentTimeMillis();
		logger.info("RestaurantController getRestaurantDetail " + (end - start) + ",in ms");		

		return responseJson;
	}    
}
