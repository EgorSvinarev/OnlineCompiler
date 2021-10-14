package com.svinarev.compiler.controllers;

import com.svinarev.compiler.dto.RawCodeDTO;

public class BaseControllerTest extends AbstractControllerTest {

	public RawCodeDTO getTestSuccessfulRawCode() {
		return RawCodeDTO.builder()
					.code("print(1)")
			   .build();
	}
	
	public RawCodeDTO getTestFailedRawCode() {
		return RawCodeDTO.builder()
					.code("print(1")
			   .build();
	}

	public RawCodeDTO getTestTimeLimitedRawCode() {
		return RawCodeDTO.builder()
					.code("while True: pass")
			   .build();
	}
	
	public RawCodeDTO getTestMemoryLimitedRawCode() {
		String code = "dict_ = {}\narr = []\nfor i in range(10000):\n\tarr.append(i)\n\tdict_[i] = arr";
		
		return RawCodeDTO.builder()
					.code(code)
			   .build();
	}
	
}
