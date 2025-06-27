package com.example.demo.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.dto.MemberDto;
import com.example.demo.dto.MemberSalaryDto;
import com.example.demo.entity.Member;
import com.example.demo.exceptions.InvalidRecordException;
import com.example.demo.exceptions.MemberNotFoundException;
import com.example.demo.repository.MemberRepository;

@Service
public class MemberService {

	@Autowired
	private MemberRepository memberRepository;

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
		List<Member> members = memberRepository.findByUniqId_LastName(lastName);
		if (members.isEmpty()) {
			throw new MemberNotFoundException("Member not found for this : " + lastName);
		}
		return members;
	}

	// Fetches the Records based on given pattern
	public List<MemberDto> getMembersByNamePattern(String firstNamePrefix, String lastNamePrefix) {
		String firstNamePattern = firstNamePrefix + "%";
		String lastNamePattern = lastNamePrefix + "%";

		List<MemberDto> members = memberRepository.findMembersByNamePattern(firstNamePattern, lastNamePattern);

		if (members.isEmpty()) {
			throw new MemberNotFoundException("No matched records found for this name pattern");
		}
		return members;
	}

	// fetch the records between two dates
	public List<Member> getMembersByDobRange(String startDateStr, String endDateStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		try {
			LocalDate startDate = LocalDate.parse(startDateStr, formatter);
			LocalDate endDate = LocalDate.parse(endDateStr, formatter);

			List<Member> members = memberRepository.findMembersByDobBetween(startDate, endDate);

			if (members.isEmpty()) {
				throw new MemberNotFoundException("No members found in the specified date range");
			}
			return members;
		} catch (DateTimeParseException e) {
			throw new InvalidRecordException("Invalid date format. Please use dd-MM-yyyy format");
		}
	}

	// fetch the records Salary greater than 2000
	public List<MemberSalaryDto> getMembersBySalary(String salary) {
		return memberRepository.findBySalary(salary);
	}

	// fetching all records
	public Page<Member> getAllMembers(int page, int size) {

		return memberRepository.findAll(PageRequest.of(page, size));
	}

}