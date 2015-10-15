package com.tagdish.util;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagDishUtility {

	private static final Logger logger = LoggerFactory
			.getLogger(TagDishUtility.class);		
	
	public static String getUniqueId(){		
		String key = "";
		 try {
			SecureRandom randomGenerator = SecureRandom.getInstance("SHA1PRNG");
			BigInteger b = new BigInteger(64,randomGenerator);
			key= new BigInteger(1, b.toByteArray()).toString(36);
		} catch (NoSuchAlgorithmException e) {
			logger.error("INVALID ALGORITHM EXCEPTION !!!!!!!!",e);
		}
		 return key;
	}
}
