package com.svinarev.compiler.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString(callSuper = true)
public class DemonExecutionResult extends ExecutionResult {
	
	private Long pid;
	
	@Builder(builderMethodName = "demonExecResBuilder") 
	public DemonExecutionResult(String status, String output, String error, String bytePayload, Long pid) {
		super(status, output, error, bytePayload);
		this.pid = pid;
	}
	
	public static String parseJupyterOutput(String output) {
		String line = output.split("\n")[2];
		
		String res = line.substring("[KernelApp] To connect a client: --existing kernel-".length());
		res = res.substring(0, res.length() - 5);
		
		return res;
	}
}
