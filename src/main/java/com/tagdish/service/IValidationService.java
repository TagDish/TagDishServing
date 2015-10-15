package com.tagdish.service;

import com.tagdish.domain.dto.TagDishInputDTO;
import com.tagdish.exception.BizServiceException;

public interface IValidationService {

	public void validateInputDTO(TagDishInputDTO tagDishInputDTO) throws BizServiceException;
}
