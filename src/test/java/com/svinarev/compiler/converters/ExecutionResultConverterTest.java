package com.svinarev.compiler.converters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.svinarev.compiler.converters.ExecutionResultConverter;
import com.svinarev.compiler.models.ExecutionResult;
import com.svinarev.compiler.dto.ExecutionResultDTO;

public class ExecutionResultConverterTest extends BaseConverterTest {

	@Test
	public void fromDTO() {
		ExecutionResultDTO execResultDto = getExecutionResultDto();
		ExecutionResult execResult = ExecutionResultConverter.fromDTO(execResultDto);
		
		assertEquals(execResult.getStatus(), execResultDto.getStatus());
		assertEquals(execResult.getOutput(), execResultDto.getOutput());
		assertEquals(execResult.getError(), execResultDto.getError());
	}
	
	@Test
	public void toDTO() {
		ExecutionResult execResult = getExecutionResult();
		ExecutionResultDTO execResultDto = ExecutionResultConverter.toDTO(execResult);
		
		assertEquals(execResult.getStatus(), execResultDto.getStatus());
		assertEquals(execResult.getOutput(), execResultDto.getOutput());
		assertEquals(execResult.getError(), execResultDto.getError());
	}
	
}
