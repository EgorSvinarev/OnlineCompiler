package com.svinarev.compiler.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность результата исполнения программы.")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExecutionResultDTO {

	@Schema(description = "Статус исполнения", example = "success")
	private String status;
	@Schema(description = "Данные из STDOUT", example = "Hello, world!")
	private String output;
	@Schema(description = "Данные из STDERR", example = "The memory limit was exceeded.")
	private String error;
	@Schema(description = "Полезная нагрузка в формате base64, полученная в результате компиляции.")
	private String bytePayload;
}
