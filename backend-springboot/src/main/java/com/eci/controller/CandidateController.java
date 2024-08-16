package com.eci.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eci.dto.LoginDto;
import com.eci.dto.CandidateNominationDto;
import com.eci.dto.CandidateRegistrationDto;

import com.eci.service.CandidateService;

@RestController
@RequestMapping("/candidate")
public class CandidateController {
	@Autowired
	private CandidateService candidateService;

	@PostMapping("/login")
	public ResponseEntity<?> loginCandidate(@RequestBody LoginDto dto) {
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(candidateService.loginCandidate(dto));
	}

	@PostMapping("/register")
	public ResponseEntity<?> candidateRegister(@RequestBody CandidateRegistrationDto candidateRegisterDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.registerCandidate(candidateRegisterDto));
	}

	@PostMapping("/nomination")
	public ResponseEntity<?> candidateNomination(@RequestBody CandidateNominationDto dto) {
		System.out.println(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.nominateCandidate(dto));
	}
	
	@GetMapping("/form-status/{candidateId}")
	public ResponseEntity<?> formStatus(@PathVariable String candidateId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.formStatus(candidateId));
	}
}
