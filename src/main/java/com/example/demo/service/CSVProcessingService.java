package com.example.demo.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CsvProcessingResult;
import com.example.demo.entity.Member;
import com.example.demo.entity.MemberId;
import com.example.demo.exceptions.CsvProcessingException;
import com.example.demo.exceptions.InvalidRecordException;
import com.example.demo.repository.CSVProcessingRepository;
import com.example.demo.repository.MemberRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@Service
public class CSVProcessingService {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CSVProcessingRepository csvProcessingRepository;

	private static final int BATCH_SIZE = 100;

	int validCount = 0;
	int invalidCount = 0;

	// Set to track unique records in memory
	Set<String> uniqueRecords = new HashSet<>();

	// Created the Array list to store valid records
	List<Member> validRecords = new ArrayList<>();

	public CsvProcessingResult processCsvFile(MultipartFile file) {
		long startTime = System.currentTimeMillis();

		try (CSVReader cr = new CSVReader(new InputStreamReader(file.getInputStream()))) {
			String[] headers = cr.readNext();
			if (headers == null || headers.length < 14) {
				throw new CsvProcessingException("Invalid header or insufficient columns");
			}

			String[] data;
			while ((data = cr.readNext()) != null) {
				try {
					Member member = processRecord(data);
					validRecords.add(member);
					validCount++;

					// Insert in batches
					if (validRecords.size() >= BATCH_SIZE) {
						insertBatchUsingJdbcTemplate(validRecords);
						validRecords.clear();
					}
				} catch (InvalidRecordException e) {
					invalidCount++;
				}
			}

		} catch (IOException | CsvValidationException e) {
			throw new CsvProcessingException("Error processing CSV file", e);
		}

		long endTime = System.currentTimeMillis();
		return new CsvProcessingResult(validCount, invalidCount, endTime - startTime);
	}

	@Transactional
	public Member processRecord(String[] data) throws InvalidRecordException {
		// Validate record length
		if (data.length < 14) {
			throw new InvalidRecordException("Insufficient fields");
		}

		// Trim all fields
		for (int i = 0; i < data.length; i++) {
			data[i] = data[i] != null ? data[i].trim() : "";
		}

		// Extract fields
		String id = data[0];
		String firstName = data[1];
		String lastName = data[2];
		String dob = data[3];
		String gender = data[4];
		String education = data[5];
		String houseNumber = data[6];
		String address1 = data[7];
		String address2 = data[8];
		String city = data[9];
		String pincode = data[10];
		String mobile = data[11];
		String company = data[12];
		String monthlySalary = data[13];

		// Validating required fields
		if (id.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || gender.isEmpty() || dob.isEmpty()
				|| city.isEmpty() || education.isEmpty() || address1.isEmpty() || address2.isEmpty()
				|| houseNumber.isEmpty() || pincode.isEmpty() || mobile.isEmpty() || monthlySalary.isEmpty()) {

			throw new InvalidRecordException("Empty fields");
		}

		// Checking for duplicates
		// Create a unique key for the record
		String recordKey = String.join("|", firstName, lastName, gender, dob.toString());

		// Check for duplicates using the Set
		if (uniqueRecords.contains(recordKey)) {
			throw new InvalidRecordException("Duplicate record");
		}

		// adding unique records to set
		uniqueRecords.add(recordKey);

		// Validating mobile number for 10 digits and number starts with 7/8/9
		if (!Pattern.matches("^[789]\\d{9}$", mobile)) {
			throw new InvalidRecordException("Invalid mobile: " + mobile);
		}

		// validating date of birth if input date is after the current date,if date is
		// future date it is invalid record
		LocalDate birthDate = parseDate(dob);
		if (birthDate.isAfter(LocalDate.now())) {
			throw new InvalidRecordException("Future date: " + dob);
		}

		// Validating age is greater than 100 or not if greater than 100 it is invalid
		// date
		Period age = Period.between(birthDate, LocalDate.now());
		if (age.getYears() > 100) {
			throw new InvalidRecordException("Age is greater than 100: " + dob);
		}

		// Clean address fields
		address1 = cleanAddress(address1);
		address2 = cleanAddress(address2);

		// Creating member object and saving it to database
		MemberId memberId = new MemberId(firstName, lastName, birthDate, gender);
		Member member = new Member(memberId, id, address1, address2, city, company, education, houseNumber, mobile,
				monthlySalary, pincode);

		return member;
	}

	// Parsing date of birth to Local Date
	private LocalDate parseDate(String dateStr) throws InvalidRecordException {
		try {
			if (dateStr.contains("-")) {
				return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			} else if (dateStr.contains("/")) {
				return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("d/M/yyyy"));
			}
			throw new InvalidRecordException("Unsupported date format: " + dateStr);
		} catch (DateTimeParseException e) {
			throw new InvalidRecordException("Invalid date format: " + dateStr);
		}
	}

	// Removing SPecial characters from the address
	private String cleanAddress(String address) {
		return address.replaceAll("[^a-zA-Z0-9\\s,]", "");
	}

	// Iterating the list and inserting to database using JDBC Template
	private void insertBatchUsingJdbcTemplate(List<Member> members) {
		try {
			csvProcessingRepository.batchInsert(members);

		} catch (DataAccessException e) {
			throw new DataIntegrityViolationException("Batch insert failed: " + e.getMessage(), e);
		}
	}

}
