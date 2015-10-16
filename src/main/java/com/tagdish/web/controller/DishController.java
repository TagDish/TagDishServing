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
import com.tagdish.service.IDishService;

@Controller
@RequestMapping("/dish")
public class DishController extends BaseController {
	
	private static final Logger logger = LoggerFactory
			.getLogger(DishController.class);		

    @Autowired
    private IDishService dishService;	
    
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	String getDishDetail(@PathVariable long id,		
			Model model) {	
		logger.debug(" DishController .. getDishDetail()");
		
		long start = System.currentTimeMillis();
		String responseJson = null;
		
		try {
			logger.debug("DishController .. getDishDetail .. id .." + id);
			
			responseJson = dishService.getDishDetail(id);
		} catch (BizServiceException e) {

			e.printStackTrace();
			logger.error("BizServiceException occured @ DishController.getDishDetail", e);
			responseJson = createErrorJson(e);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Exception occured @ DishController.getDishDetail", e);
			responseJson = createErrorJson(e);
		}
		
		long end = System.currentTimeMillis();
		logger.info("DishController getDishDetail " + (end - start) + ",in ms");		

		return responseJson;
	}    
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	String getDishDetail(@PathVariable long id,
			@RequestParam(value = "lant", required = false) String lantitude,
			@RequestParam(value = "long", required = false) String longtitude,
			@RequestParam(value = "zipcode", required = false) Long zipCode,
			@RequestParam(value = "city", required = false) String city,
			@RequestParam(value = "state", required = false) String state,	
			@RequestParam(value = "transactionId", required = false) String transactionId,
			@RequestParam(value = "timestamp", required = false) long timestamp,
			Model model) {	
		logger.debug(" DishController .. getDishDetail()");
		
		long start = System.currentTimeMillis();
		String responseJson = null;
		DetailInputDTO detailInputDTO = null;
		try {
			logger.debug("DishController .. getDishDetail .. id .." + id);
			
			detailInputDTO = createDetailInputDTO(id, lantitude, longtitude, zipCode, city, state, transactionId, timestamp);
			responseJson = dishService.getDish(detailInputDTO);
		} catch (BizServiceException e) {

			e.printStackTrace();
			logger.error("BizServiceException occured @ DishController.getDishDetail", e);
			responseJson = createErrorJson(e);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Exception occured @ DishController.getDishDetail", e);
			responseJson = createErrorJson(e);
		}
		
		long end = System.currentTimeMillis();
		logger.info("DishController getDishDetail " + (end - start) + ",in ms");		

		return responseJson;
	}	
		
	@RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	String getDishList(
			Model model) {	
		logger.debug(" DishController .. getDishList()");
		
		long start = System.currentTimeMillis();
		String responseJson = null;
		
		try {
			logger.debug("DishController .. getDishList .. ");
			responseJson = dishService.getDishList();
		} catch (BizServiceException e) {

			e.printStackTrace();
			logger.error("BizServiceException occured @ DishController.getDishList", e);
			responseJson = createErrorJson(e);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Exception occured @ DishController.getDishList", e);
			responseJson = createErrorJson(e);
		}
		
		long end = System.currentTimeMillis();
		logger.info("DishController getDishList " + (end - start) + ",in ms");		

		return responseJson;
	}	
}
