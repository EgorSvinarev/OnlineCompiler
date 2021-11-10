package com.svinarev.compiler.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;

import com.svinarev.compiler.dto.RawCodeDTO;
import com.svinarev.compiler.dto.ExecutionResultDTO;

public class CompileControllerTest extends BaseControllerTest {
	
	private static final String URI = "/execute";
	
	@Override
	@BeforeEach
	public void setUp() {
		super.setUp();
	}
	
	@Test
	public void successfulCompilation() throws Exception {
		RawCodeDTO rawCode = getTestSuccessfulRawCode();
		
		MvcResult result = mvc.perform(MockMvcRequestBuilders.post(
								URI
						   ).contentType(MediaType.APPLICATION_JSON_VALUE)
							.content(super.mapToJson(rawCode))).andReturn();
		
		int responseStatus = result.getResponse().getStatus();
		assertEquals(responseStatus, 200);
		
		String content = result.getResponse().getContentAsString();
		ExecutionResultDTO execResult = super.mapFromJson(content, ExecutionResultDTO.class);
		
		assertEquals(execResult.getStatus(), "success");
		assertEquals(execResult.getOutput(), "1");
		assertEquals(execResult.getError(), "");
	}
	
	@Test
	public void failedCompilation() throws Exception {
		RawCodeDTO rawCode = getTestFailedRawCode();
		
		MvcResult result = mvc.perform(MockMvcRequestBuilders.post(
								URI
						   ).contentType(MediaType.APPLICATION_JSON_VALUE)
							.content(super.mapToJson(rawCode))).andReturn();

		int responseStatus = result.getResponse().getStatus();
		assertEquals(responseStatus, 200);
		
		String content = result.getResponse().getContentAsString();
		ExecutionResultDTO execResult = super.mapFromJson(content, ExecutionResultDTO.class);
		
		assertEquals(execResult.getStatus(), "error");
		assertEquals(execResult.getOutput(), "");
		assertTrue(execResult.getError().length() > 0);
	}
	
	@Test
	public void timelimitCompilation() throws Exception {
		RawCodeDTO rawCode = getTestTimeLimitedRawCode();
		
		MvcResult result = mvc.perform(MockMvcRequestBuilders.post(
								URI
				   ).contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(super.mapToJson(rawCode))).andReturn();
		
		int responseStatus = result.getResponse().getStatus();
		assertEquals(responseStatus, 200);
		
		String content = result.getResponse().getContentAsString();
		ExecutionResultDTO execResult = super.mapFromJson(content, ExecutionResultDTO.class);
		
		assertEquals(execResult.getStatus(), "error");
		assertEquals(execResult.getOutput(), "");
		assertEquals(execResult.getError(), "Limitation of execution: the execution time was exceeded.");
	}
	
//	@Test
//	public void memorylimitCompilation() throws Exception {
//		RawCodeDTO rawCode = getTestMemoryLimitedRawCode();
//		
//		MvcResult result = mvc.perform(MockMvcRequestBuilders.post(
//								URI
//				   ).contentType(MediaType.APPLICATION_JSON_VALUE)
//					.content(super.mapToJson(rawCode))).andReturn();
//		
//		int responseStatus = result.getResponse().getStatus();
//		assertEquals(responseStatus, 200);
//		
//		String content = result.getResponse().getContentAsString();
//		ExecutionResultDTO execResult = super.mapFromJson(content, ExecutionResultDTO.class);
//		
//		assertEquals(execResult.getStatus(), "error");
//		assertEquals(execResult.getOutput(), "");
//		assertEquals(execResult.getError(), "Limitation of execution: the memory limit was exceeded.");
//	}
	
}
