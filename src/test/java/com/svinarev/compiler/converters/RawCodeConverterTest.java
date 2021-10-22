package com.svinarev.compiler.converters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.svinarev.compiler.converters.RawCodeConverter;
import com.svinarev.compiler.models.RawCode;
import com.svinarev.compiler.dto.RawCodeDTO;


public class RawCodeConverterTest extends BaseConverterTest {

	@Test
	public void fromDTO() {
		RawCodeDTO rawCodeDto = getRawCodeDto();
		RawCode rawCode = RawCodeConverter.fromDTO(rawCodeDto);
		
		assertEquals(rawCodeDto.getCode(), rawCode.getCode());
	}
	
	@Test
	public void toDTO() {
		RawCode rawCode = getRawCode();
		RawCodeDTO rawCodeDto = RawCodeConverter.toDTO(rawCode);
		
		assertEquals(rawCodeDto.getCode(), rawCode.getCode());
	}
	
}
