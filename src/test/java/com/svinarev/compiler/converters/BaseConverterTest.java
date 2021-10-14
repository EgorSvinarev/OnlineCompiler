package com.svinarev.compiler.converters;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.svinarev.compiler.OnlineCompilerApplication;
import com.svinarev.compiler.dto.*;
import com.svinarev.compiler.models.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OnlineCompilerApplication.class)
@WebAppConfiguration
@ActiveProfiles({"test"})
public class BaseConverterTest {

	public RawCode getRawCode() {
		return RawCode.builder()
					.code("import limits\nprint(1)")
				.build();
	}
	
	public RawCodeDTO getRawCodeDto() {
		return RawCodeDTO.builder()
					.code("print(1)")
				.build();
	}
	
	public ExecutionResult getExecutionResult() {
		return ExecutionResult.builder()
					.status("success")
					.output("1")
					.error("")
				.build();
	}
	
	public ExecutionResultDTO getExecutionResultDto() {
		return ExecutionResultDTO.builder()
					.status("success")
					.output("1")
					.error("")
				.build();
	}
	
}
