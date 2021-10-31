package com.svinarev.compiler.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;

import com.svinarev.compiler.services.CompilerService;
import com.svinarev.compiler.dto.ExecutionResultDTO;
import com.svinarev.compiler.dto.RawCodeDTO;
import com.svinarev.compiler.converters.RawCodeConverter;
import com.svinarev.compiler.converters.ExecutionResultConverter;

@Tag(name = "Обработчик запросов с консоли.", 
	 description = "Контроллер для операций, связанных с обработкой пользовательского кода, поступающего с клиентской консоли.")
@RestController
public class CompileController {

	Logger logger = LoggerFactory.getLogger(CompileController.class);
	
	@Autowired
	CompilerService service;
	
	
	
	@Operation(summary = "Исполнение кода",
			   description = "Компиляция кода и возвращение результата его исполнения.")
	@ApiResponse(responseCode = "200", description = "Успешное выполнение операции.",
				 content = @Content(schema = @Schema(implementation = ExecutionResultDTO.class))
				)
	@ApiResponse(responseCode = "500", description = "Ошибка исполнения.")
	
	@PostMapping(value = "/compile", produces = {"application/json"})
	
	public ResponseEntity<?> compile(
			@Parameter(description = "Пользовательский код для исполнения.", 
            	required = true, 
            	schema = @Schema(implementation = RawCodeDTO.class))
			
			@Valid @RequestBody RawCodeDTO code) {
		
		logger.debug("A request to an endpoint /compile was received.");
		
		return ResponseEntity.ok(
				ExecutionResultConverter.toDTO(
						service.compile(RawCodeConverter.fromDTO(code), true)
				)
		);
	}
	
	
	@Operation(summary = "Проверка упражнения",
			   description = "Проверка правильность выполнения упраженения с возвращением развернутого сообщения об ошибке студента.")
	@ApiResponse(responseCode = "200", description = "Успешное выполнение операции.",
				 content = @Content(schema = @Schema(implementation = ExecutionResultDTO.class))
				)
	@ApiResponse(responseCode = "500", description = "Ошибка исполнения.")
	
	@PostMapping(value = "/checkExercise/{exerciseId}", produces = {"application/json"})
	public ResponseEntity<?> checkExercise(
			@Parameter(description = "Код, решающий упражнение",
					   required = true,
					   schema  = @Schema(implementation = RawCodeDTO.class))
			@Valid @RequestBody RawCodeDTO code,
			
			@Parameter(description = "Параметр, указывающий, что упражнение требует построение графика",
					   required = false)
			@RequestParam(name = "isGraphRequired", required = false, defaultValue = "false")
			boolean isGraphRequired,
			
			@PathVariable Long exerciseId) {
		
		logger.debug("A request to an endpoint /checkExercise was received.");
		
		if (isGraphRequired) {
			return ResponseEntity.ok(
					ExecutionResultConverter.toDTO(
							service.plotGraph(RawCodeConverter.fromDTO(code), exerciseId)
					)
			);
		}
		else {
			return ResponseEntity.ok(
					ExecutionResultConverter.toDTO(
							service.checkExercise(RawCodeConverter.fromDTO(code), exerciseId)
					)
			);
		}
		
		
	}
	
}
