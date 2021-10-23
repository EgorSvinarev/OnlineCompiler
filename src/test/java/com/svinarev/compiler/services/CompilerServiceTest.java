package com.svinarev.compiler.services;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.svinarev.compiler.OnlineCompilerApplication;
import com.svinarev.compiler.services.CompilerService;
import com.svinarev.compiler.models.ExecutionResult;
import com.svinarev.compiler.models.RawCode;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OnlineCompilerApplication.class)
@WebAppConfiguration
@ActiveProfiles({"test"})
public class CompilerServiceTest {

	@Autowired
	CompilerService service;
	
	@Test
	public void compile() {
		ExecutionResult execResultRef = ExecutionResult.builder()
											.status("success")
											.output("1")
											.error("")
										.build();
		
		RawCode rawCode = RawCode.builder()
								.code("print(1)")
						  .build();
		
		
		ExecutionResult execResult = service.compile(rawCode, true);
		
		assertEquals(execResultRef.getStatus(), execResult.getStatus());
		assertEquals(execResultRef.getOutput(), execResult.getOutput());
		assertEquals(execResultRef.getError(), execResult.getError());
	}
	
}
