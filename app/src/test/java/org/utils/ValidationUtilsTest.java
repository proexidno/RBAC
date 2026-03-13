package org.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTest {

	@Test
	public void testIsValidUsername() {
		assertTrue(ValidationUtils.isValidUsername("john_doe"));
		assertTrue(ValidationUtils.isValidUsername("admin123"));
		assertFalse(ValidationUtils.isValidUsername("ab")); // too short
		assertFalse(ValidationUtils.isValidUsername("user name")); // contains space
		assertFalse(ValidationUtils.isValidUsername(null));
	}

	@Test
	public void testIsValidEmail() {
		assertTrue(ValidationUtils.isValidEmail("test@example.com"));
		assertFalse(ValidationUtils.isValidEmail("invalid"));
		assertFalse(ValidationUtils.isValidEmail(null));
	}

	@Test
	public void testNormalizeString() {
		assertEquals("hello", ValidationUtils.normalizeString("  HELLO  "));
		assertEquals("", ValidationUtils.normalizeString(null));
	}

	@Test
	public void testUsernameBoundaries() {
		assertTrue(ValidationUtils.isValidUsername("abc")); // Min length
		assertTrue(ValidationUtils.isValidUsername("abcdefghijklmnopqrst")); // Max length (20)
		assertFalse(ValidationUtils.isValidUsername("ab")); // Too short
		assertFalse(ValidationUtils.isValidUsername("abcdefghijklmnopqrstu")); // Too long (21)
		assertFalse(ValidationUtils.isValidUsername("user-name")); // Invalid char
	}

	@Test
	public void testRequireNonEmpty() {
		assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty("", "Test Field"));
		assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty(null, "Test Field"));
		// Should not throw
		ValidationUtils.requireNonEmpty("valid", "Test Field");
	}
}
