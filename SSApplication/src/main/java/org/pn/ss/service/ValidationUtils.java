package org.pn.ss.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class ValidationUtils {

	public static <T> List<String> validate( T t ) {
		List<String> messages = new ArrayList<String>();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<T>> violations = validator
				.validate(t);
		for (ConstraintViolation<T> constraintViolation : violations) {
			messages.add(constraintViolation.getMessage());
		}
		Collections.sort(messages);
		return messages;
	}
}