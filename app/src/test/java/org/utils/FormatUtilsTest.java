package org.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class FormatUtilsTest {

	@Test
	public void testFormatTable() {
		String[] headers = { "Name", "Value" };
		List<String[]> rows = List.of(new String[][] { { "Key1", "Value1" } });
		String table = FormatUtils.formatTable(headers, rows);

		assertTrue(table.contains("Name"));
		assertTrue(table.contains("Value1"));
		assertTrue(table.contains("+"));
	}

	@Test
	public void testTruncate() {
		assertEquals("Hello...", FormatUtils.truncate("Hello World", 8));
		assertEquals("Hi", FormatUtils.truncate("Hi", 10));
	}
}
