package com.svinarev.compiler.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExecutionResultDTO {
	
	private String status;
	private String output;
	private String error;
	
}
