package com.example.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CsvProcessingResult {

	private int validCount;
	private int invalidCount;
	// private List<String> errorMessages;
	private long processingTimeMs;

}