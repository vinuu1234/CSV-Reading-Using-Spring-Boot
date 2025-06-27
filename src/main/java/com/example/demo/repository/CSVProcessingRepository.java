package com.example.demo.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Member;
import com.example.demo.entity.MemberId;

@Repository
public class CSVProcessingRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// This is batch insert method,here i am inserting records batch-wise
	@Transactional
	public void batchInsert(List<Member> members) {

		String sql = """
				INSERT IGNORE INTO members (
				    first_name, last_name, dob, gender,
				    member_id, education, house_number,
				    address1, address2, pin_code,
				    city, mobile, company, monthly_salary
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";
		jdbcTemplate.batchUpdate(sql, members, members.size(), (ps, member) -> {
			MemberId uniqId = member.getUniqId();
			ps.setString(1, uniqId.getFirstName());
			ps.setString(2, uniqId.getLastName());
			ps.setDate(3, Date.valueOf(uniqId.getDob()));
			ps.setString(4, uniqId.getGender());
			ps.setString(5, member.getMemberId());
			ps.setString(6, member.getEducation());
			ps.setString(7, member.getHouseNumber());
			ps.setString(8, member.getAddress1());
			ps.setString(9, member.getAddress2());
			ps.setString(10, member.getPinCode());
			ps.setString(11, member.getCity());
			ps.setString(12, member.getMobile());
			ps.setString(13, member.getCompany());
			ps.setString(14, member.getMonthlySalary());
		});
	}

}
