package com.svinarev.compiler.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

@ControllerAdvice
public class ExceptionHandlingController {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException e) {
		String message = "Invalid values in these fields: ";
		
		for (FieldError err: e.getFieldErrors()) {
			message += "\n";
			message += err.getField() + ": " + err.getRejectedValue();
		}
		
		return ResponseEntity.status(500).body(message);
	}
	
	
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<String> handleInvalidHttpMessageException(HttpRequestMethodNotSupportedException e) {
		String message = "Invalid http message: " + e.getMethod();
		
		return ResponseEntity.status(405).body(message);
	}
	
	@
	
}
