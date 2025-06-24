package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
	@EmbeddedId
	private MemberId uniqId;

	private String memberId;
	private String city;
	private String state;
	private String address1;
	private String address2;
	private String country;
	@Column(name = "pincode")  // Add this if your column is named "pincode"
	private String pinCode;
	private String mobile;

}