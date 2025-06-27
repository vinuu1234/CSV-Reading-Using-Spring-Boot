package com.example.demo.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Member;
import com.example.demo.service.MemberService;

@Controller
@RequestMapping("/api/page")
public class PaginationController {

	@Autowired
	private MemberService memberService;

	@GetMapping("/get-all")
	public String getMembers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			Model model) {

		Page<Member> members = memberService.getAllMembers(page, size);

		model.addAttribute("members", members);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", members.getTotalPages());
		model.addAttribute("totalItems", members.getTotalElements());
		model.addAttribute("pageSizes", Arrays.asList(5, 10, 20, 50));

		return "members";
	}

}
