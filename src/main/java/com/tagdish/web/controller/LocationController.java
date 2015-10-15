package com.tagdish.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tagdish.exception.BizServiceException;
import com.tagdish.service.ILocationService;

@Controller
@RequestMapping("/location")
public class LocationController extends BaseController {
	
	private static final Logger logger = LoggerFactory
			.getLogger(LocationController.class);		

    @Autowired
    private ILocationService locationService;	
    
	@RequestMapping(value = "/like/{city}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	String getCity(@PathVariable String city,		
			Model model) {	
		logger.debug(" LocationController .. getCity()");
		
		long start = System.currentTimeMillis();
		String responseJson = null;
		
		try {
			logger.debug("LocationController .. getCity .. id .." + city);
			
			responseJson = locationService.getCity(city);
		} catch (BizServiceException e) {

			e.printStackTrace();
			logger.error("BizServiceException occured @ LocationController.getCity", e);
			responseJson = createErrorJson(e);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Exception occured @ LocationController.getCity", e);
			responseJson = createErrorJson(e);
		}
		
		long end = System.currentTimeMillis();
		logger.info("LocationController getCity " + (end - start) + ",in ms");		

		return responseJson;
	}    
	
}
