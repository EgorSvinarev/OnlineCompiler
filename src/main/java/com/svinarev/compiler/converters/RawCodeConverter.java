package com.svinarev.compiler.converters;

import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.dto.RawCodeDTO;

public class RawCodeConverter {

	public static RawCode fromDTO(RawCodeDTO rawCode) {
		return RawCode.builder()
					.code(rawCode.getCode())
			   .build();
	}
	
	public static RawCodeDTO toDTO(RawCode rawCode) {
		return RawCodeDTO.builder()
					.code(rawCode.getCode())
			   .build();
	}
	
}
