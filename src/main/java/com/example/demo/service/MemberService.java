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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CsvProcessingResult;
import com.example.demo.dto.MemberDto;
import com.example.demo.entity.Member;
import com.example.demo.entity.MemberId;
import com.example.demo.exceptions.CsvProcessingException;
import com.example.demo.exceptions.InvalidRecordException;
import com.example.demo.exceptions.MemberNotFoundException;
import com.example.demo.repository.MemberRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@Service
public class MemberService {

	@Autowired
	private MemberRepository memberRepository;
	
	//declaring variable valid count to track the valid record count
	int validCount = 0;
	//declaring variable invalid count to track the invalid record count
	int invalidCount = 0;

	// Set to track unique records in memory
	private Set<String> uniqueRecords = new HashSet<>();

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
					processRecord(data);
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
	public void processRecord(String[] data) throws InvalidRecordException {
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
		String city = data[5];
		String state = data[6];
		String address1 = data[7];
		String address2 = data[8];
		String country = data[9];
		String pincode = data[10];
		String mobile = data[11];

		// Validating required fields
		if (id.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || gender.isEmpty() || dob.isEmpty()
				|| city.isEmpty() || state.isEmpty() || address1.isEmpty() || address2.isEmpty() || country.isEmpty()
				|| pincode.isEmpty() || mobile.isEmpty()) {

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

		// Validating mobile number
		if (!Pattern.matches("^[789]\\d{9}$", mobile)) {
			throw new InvalidRecordException("Invalid mobile: " + mobile);
		}

		// validating date of birth
		LocalDate birthDate = parseDate(dob);
		if (birthDate.isAfter(LocalDate.now())) {
			throw new InvalidRecordException("Future date: " + dob);
		}

		// Validating age
		Period age = Period.between(birthDate, LocalDate.now());
		if (age.getYears() > 100) {
			throw new InvalidRecordException("Age is greater than 100: " + dob);
		}

		// Clean address fields
		address1 = cleanAddress(address1);
		address2 = cleanAddress(address2);

		// Creating member object and saving it to database
		MemberId memberId = new MemberId(firstName, lastName, birthDate, gender);
		Member member = new Member(memberId, id, city, state, address1, address2, country, pincode, mobile);
		List<Member> validRecords = new ArrayList<>();

		validRecords.add(member);
		validCount++;
		insertBatch(validRecords);
	}

	// Parsing date of birth
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

	// Removing SPecial charecters from the address
	private String cleanAddress(String address) {
		return address.replaceAll("[^a-zA-Z0-9\\s,]", "");
	}

	// Iterating the list and inserting to database
	private void insertBatch(List<Member> members) {
		members.forEach(member -> {
			memberRepository.insertOnly(member.getUniqId().getFirstName(), member.getUniqId().getLastName(),
					member.getUniqId().getDob(), member.getUniqId().getGender(), member.getMemberId(), member.getCity(),
					member.getState(), member.getAddress1(), member.getAddress2(), member.getCountry(),
					member.getPinCode(), member.getMobile());
		});
	}

	// Creating service which fetches records by member first name
	public List<Member> getMemberByFirstName(String firstName) {

		List<Member> members = memberRepository.findByUniqId_FirstName(firstName);

		if (members.isEmpty()) {
			throw new MemberNotFoundException("Member not found for this : " + firstName);
		}
		return members;
	}

	// Creating service which fetches records by member Last name
	public List<Member> getMemberByLastName(String lastName) {
		List<Member> members = memberRepository.findByUniqId_FirstName(lastName);
		if (members.isEmpty()) {
			throw new MemberNotFoundException("Member not found for this : " + lastName);
		}
		return members;
	}

	
	public List<MemberDto> getMembersByNamePattern() {
	    List<MemberDto> members = memberRepository.findMembersByNamePattern();
	    if (members.isEmpty()) {
	        throw new MemberNotFoundException("No members found matching the name pattern");
	    }
	    return members;
	}
}