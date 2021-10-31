package com.svinarev.compiler.converters;

import com.svinarev.compiler.dto.ExecutionResultDTO;
import com.svinarev.compiler.models.ExecutionResult;


public class ExecutionResultConverter {

	public static ExecutionResult fromDTO(ExecutionResultDTO execResDto) {
		return ExecutionResult.builder()
			.status(execResDto.getStatus())
			.output(execResDto.getOutput())
			.error(execResDto.getError())
			.bytePayload(execResDto.getBytePayload())
		.build();
	}
	
	public static ExecutionResultDTO toDTO(ExecutionResult execRes) {
		return ExecutionResultDTO.builder()
			.status(execRes.getStatus())
			.output(execRes.getOutput())
			.error(execRes.getError())
			.bytePayload(execRes.getBytePayload())
		.build();
	}
	
}
