package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.MemberDto;
import com.example.demo.entity.Member;
import com.example.demo.entity.MemberId;

import jakarta.transaction.Transactional;

@Repository
public interface MemberRepository extends JpaRepository<Member, MemberId> {

	List<Member> findByUniqId_FirstName(String firstName);

	List<Member> findByUniqId_LastName(String lastName);

	@Modifying
	@Transactional
	@Query(value = "INSERT IGNORE INTO members (first_name, last_name, dob, gender, member_id, city, state, address1, address2, country, pincode, mobile) "
			+ "VALUES (:firstName, :lastName, :dob, :gender, :memberId, :city, :state, :address1, :address2, :country, :pinCode, :mobile)", nativeQuery = true)
	void insertOnly(@Param("firstName") String firstName, @Param("lastName") String lastName,
			@Param("dob") LocalDate dob, @Param("gender") String gender, @Param("memberId") String memberId,
			@Param("city") String city, @Param("state") String state, @Param("address1") String address1,
			@Param("address2") String address2, @Param("country") String country, @Param("pinCode") String pinCode,
			@Param("mobile") String mobile);
	
	
	    
	 @Query("SELECT NEW com.example.demo.dto.MemberDto(" +
	           "m.memberId, m.uniqId.firstName, m.uniqId.lastName, " +
	           "m.uniqId.dob, m.uniqId.gender, m.city) " +
	           "FROM Member m " +
	           "WHERE m.uniqId.firstName LIKE 'FirstName2%' " +
	           "AND m.uniqId.lastName LIKE 'LastName2%'")
	    List<MemberDto> findMembersByNamePattern();
	}
}



