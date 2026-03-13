package org.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

	@Test
	public void testGetCurrentDate() {
		String date = DateUtils.getCurrentDate();
		assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
	}

	@Test
	public void testIsBefore() {
		assertTrue(DateUtils.isBefore("2024-01-01", "2024-12-31"));
		assertFalse(DateUtils.isBefore("2024-12-31", "2024-01-01"));
	}

	@Test
	public void testAddDays() {
		String result = DateUtils.addDays("2024-01-01", 5);
		assertEquals("2024-01-06", result);
	}

	@Test
	public void testIsExpired() {
		assertTrue(DateUtils.isExpired("2020-01-01"));
		assertFalse(DateUtils.isExpired("2030-01-01"));
	}
}
