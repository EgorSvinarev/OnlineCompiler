package com.svinarev.compiler.converters;

import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.dto.RawCodeDTO;

public class RawCodeConverter {

	public static RawCode fromDto(RawCodeDTO rawCode) {
		return RawCode.builder()
					.code(rawCode.getCode())
			   .build();
	}
	
	public static RawCodeDTO toDto(RawCode rawCode) {
		return RawCodeDTO.builder()
					.code(rawCode.getCode())
			   .build();
	}
	
}
