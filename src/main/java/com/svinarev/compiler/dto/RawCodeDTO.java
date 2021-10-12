package com.svinarev.compiler.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность пользовательского кода")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RawCodeDTO {

	@Schema(description = "Пользовательский код", example = "print('Hello, world!')")
	@NotNull
	private String code;
	
}
