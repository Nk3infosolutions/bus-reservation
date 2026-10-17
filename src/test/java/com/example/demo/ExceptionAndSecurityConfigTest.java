package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.config.SecurityConfig;

class ExceptionAndSecurityConfigTest {

	@Test
	void exceptionHandlerReturnsExceptionView() {
		assertEquals("exception", new Exceptions().handler());
	}

	@Test
	void securityConfigProvidesPasswordEncoder() {
		PasswordEncoder encoder = new SecurityConfig().passwordEncoder();
		String encoded = encoder.encode("test-password");

		assertTrue(encoder.matches("test-password", encoded));
	}
}
