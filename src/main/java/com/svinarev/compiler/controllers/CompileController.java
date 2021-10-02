package com.svinarev.compiler.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.svinarev.compiler.services.CompilerService;
import com.svinarev.compiler.dto.RawCodeDTO;
import com.svinarev.compiler.converters.RawCodeConverter;
import com.svinarev.compiler.converters.ExecutionResultConverter;

@RestController
public class CompileController {

	Logger logger = LoggerFactory.getLogger(CompileController.class);
	
	@Autowired
	CompilerService service;
	
	@PostMapping(value = "/compile")
	public ResponseEntity<?> compile(@Valid @RequestBody RawCodeDTO code) {
		logger.debug("A request to an endpoint /compile was received.");
		return ResponseEntity.ok(
				ExecutionResultConverter.toDTO(
						service.compile(RawCodeConverter.fromDto(code))
				)
		);
	}
	
}
