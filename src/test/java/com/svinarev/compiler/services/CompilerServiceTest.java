package com.svinarev.compiler.services;

import org.junit.jupiter.api.extension.ExtendWith;;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.svinarev.compiler.AbstractTest;
import com.svinarev.compiler.OnlineCompilerApplication;
import com.svinarev.compiler.services.CompilerService;
import com.svinarev.compiler.models.ExecutionResult;
import com.svinarev.compiler.models.RawCode;


public class CompilerServiceTest extends AbstractTest {

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
		
		
		ExecutionResult execResult = service.compile(rawCode);
		
		assertEquals(execResultRef.getStatus(), execResult.getStatus());
		assertEquals(execResultRef.getOutput(), execResult.getOutput());
		assertEquals(execResultRef.getError(), execResult.getError());
	}
	
	@Test
	public void executeWithExercise() {
		
		ExecutionResult execResultRef = ExecutionResult.builder()
				.status("success")
				.output("21")
				.error("")
			.build();
		
		RawCode rawCode = RawCode.builder()
				.code("print(len(date_ranges))")
		  .build();
	
		ExecutionResult execResult = service.executeWithExercise(rawCode, 90L);
		
		assertEquals(execResultRef.getStatus(), execResult.getStatus());
		assertEquals(execResultRef.getOutput(), execResult.getOutput());
		assertEquals(execResultRef.getError(), execResult.getError());
		
	}
	
	@Test
	public void negativeCheckExercise() {
		
		ExecutionResult execResultRef = ExecutionResult.builder()
				.status("error")
				.output("")
				.error("Проверьте первый цикл 'for'. Правильно ли вы указали тело? Вы вызвали функцию `pendulum.parse()`?")
			.build();
		
		RawCode rawCode = RawCode.builder()
				.code("for start_date, end_date in date_ranges:\n\ta = 5")
		  .build();
	
		ExecutionResult execResult = service.checkExercise(rawCode, 90L, 2L);
		
		assertEquals(execResultRef.getStatus(), execResult.getStatus());
		assertEquals(execResultRef.getOutput(), execResult.getOutput());
		assertEquals(execResultRef.getError(), execResult.getError());
		
	}
	
	@Test
	public void positiveCheckExercise() {
		
		ExecutionResult execResultRef = ExecutionResult.builder()
				.status("success")
				.output("")
				.error("")
			.build();
		
		String code = "for start_date, end_date in date_ranges:\n\tstart_dt = pendulum.parse(start_date, strict=False)\n\tend_dt = pendulum.parse(end_date, strict = False)\n\tprint(end_dt, start_dt)\n\tperiod = end_dt - start_dt\n\tprint(period.in_days())";
		
		RawCode rawCode = RawCode.builder()
				.code(code)
		  .build();
	
		ExecutionResult execResult = service.checkExercise(rawCode, 90L, 2L);
		
		assertEquals(execResultRef.getStatus(), execResult.getStatus());
		assertEquals(execResultRef.getOutput(), execResult.getOutput());
		assertEquals(execResultRef.getError(), execResult.getError());
		
	}
	
	
}
