package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CsvProcessingResult {

	private int validCount;
	private int invalidCount;
	private long processingTimeMs;
}