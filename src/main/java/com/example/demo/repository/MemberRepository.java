package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.MemberDto;
import com.example.demo.dto.MemberSalaryDto;
import com.example.demo.entity.Member;
import com.example.demo.entity.MemberId;

@Repository
public interface MemberRepository extends JpaRepository<Member, MemberId> {

	List<Member> findByUniqId_FirstName(String firstName);

	List<Member> findByUniqId_LastName(String lastName);

	// Selecting specific fields with dynamic parameters
	@Query("SELECT NEW com.example.demo.dto.MemberDto(" + "m.memberId, m.uniqId.firstName, m.uniqId.lastName, "
			+ "m.uniqId.dob, m.uniqId.gender, m.city) " + "FROM Member m "
			+ "WHERE m.uniqId.firstName LIKE :firstNamePattern " + "AND m.uniqId.lastName LIKE :lastNamePattern")
	List<MemberDto> findMembersByNamePattern(@Param("firstNamePattern") String firstNamePattern,
			@Param("lastNamePattern") String lastNamePattern);

	// JPA query for Selecting records between two dates
	@Query("SELECT m FROM Member m " + "WHERE m.uniqId.dob BETWEEN :startDate AND :endDate")
	List<Member> findMembersByDobBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	// Fetching records whose salary is greater than given salary
	@Query("""
			SELECT NEW com.example.demo.dto.MemberSalaryDto(
			    m.memberId,
			    m.uniqId.firstName,
			    m.uniqId.lastName,
			    m.uniqId.dob,
			    m.uniqId.gender,
			    m.city,
			    m.monthlySalary
			)
			FROM Member m
			WHERE m.monthlySalary >= :minSalary
			""")
	List<MemberSalaryDto> findBySalary(@Param("minSalary") String minSalary);

}
