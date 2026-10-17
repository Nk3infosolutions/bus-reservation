package com.example.demo.count;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LogicTest {

	@Test
	void countTotalMultipliesPriceByQuantity() {
		assertEquals(37.5, Logic.countTotal(12.5, 3));
	}

	@Test
	void countTotalReturnsZeroWhenQuantityIsZero() {
		assertEquals(0.0, Logic.countTotal(12.5, 0));
	}

	@Test
	void countTotalSupportsZeroPrice() {
		assertEquals(0.0, Logic.countTotal(0.0, 5));
	}

	@Test
	void countTotalSupportsNegativeValues() {
		assertEquals(-15.0, Logic.countTotal(-5.0, 3));
	}
}
