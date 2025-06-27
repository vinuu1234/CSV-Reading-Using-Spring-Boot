package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CsvProcessingResult;
import com.example.demo.service.CSVProcessingService;

@RestController
@RequestMapping("/api/csv")
public class CSVProcessingController {
	
	@Autowired
	private CSVProcessingService csvProcessingService;

	@PostMapping("/upload")
	public ResponseEntity<CsvProcessingResult> upload(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(csvProcessingService.processCsvFile(file));
	}
}
