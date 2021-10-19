package com.svinarev.compiler.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExecutionResult {

	private String status;
	private String output;
	private String error;

	public boolean isLimited() {
		return error.contains("Limitation of execution");
	}
	
}
