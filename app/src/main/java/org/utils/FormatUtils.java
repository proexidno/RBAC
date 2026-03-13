package org.utils;

import java.util.List;

public class FormatUtils {

	public static String formatTable(String[] headers, List<String[]> rows) {
		if (headers == null || headers.length == 0) {
			return "";
		}

		int[] colWidths = new int[headers.length];
		for (int i = 0; i < headers.length; i++) {
			colWidths[i] = headers[i] != null ? headers[i].length() : 0;
		}

		for (String[] row : rows) {
			for (int i = 0; i < Math.min(row.length, colWidths.length); i++) {
				if (row[i] != null && row[i].length() > colWidths[i]) {
					colWidths[i] = row[i].length();
				}
			}
		}

		for (int i = 0; i < colWidths.length; i++) {
			colWidths[i] += 2;
		}

		StringBuilder sb = new StringBuilder();

		sb.append(createBorder(colWidths, '+', '-'));
		sb.append("\n");

		sb.append("|");
		for (int i = 0; i < headers.length; i++) {
			sb.append(" ").append(padRight(headers[i] != null ? headers[i] : "", colWidths[i] - 2))
					.append(" |");
		}
		sb.append("\n");

		sb.append(createBorder(colWidths, '+', '-'));
		sb.append("\n");

		for (String[] row : rows) {
			sb.append("|");
			for (int i = 0; i < colWidths.length; i++) {
				String cell = (i < row.length && row[i] != null) ? row[i] : "";
				sb.append(" ").append(padRight(cell, colWidths[i] - 2)).append(" |");
			}
			sb.append("\n");
		}

		sb.append(createBorder(colWidths, '+', '-'));

		return sb.toString();
	}

	private static String createBorder(int[] widths, char corner, char horizontal) {
		StringBuilder sb = new StringBuilder();
		sb.append(corner);
		for (int width : widths) {
			sb.append(String.valueOf(horizontal).repeat(width));
			sb.append(corner);
		}
		return sb.toString();
	}

	public static String formatBox(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		String[] lines = text.split("\n");
		int maxLength = 0;
		for (String line : lines) {
			if (line.length() > maxLength) {
				maxLength = line.length();
			}
		}

		StringBuilder sb = new StringBuilder();
		String horizontal = "═".repeat(maxLength + 4);

		sb.append("╔").append(horizontal).append("╗\n");
		for (String line : lines) {
			sb.append("║ ").append(padRight(line, maxLength)).append(" ║\n");
		}
		sb.append("╚").append(horizontal).append("╝\n");

		return sb.toString();
	}

	public static String formatHeader(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		int width = Math.max(text.length() + 4, 60);
		int padding = (width - text.length()) / 2;

		StringBuilder sb = new StringBuilder();
		sb.append("=".repeat(width)).append("\n");
		sb.append("  ").append(text).append("\n");
		sb.append("=".repeat(width)).append("\n");

		return sb.toString();
	}

	public static String truncate(String text, int maxLength) {
		if (text == null) {
			return "";
		}
		if (text.length() <= maxLength) {
			return text;
		}
		return text.substring(0, maxLength - 3) + "...";
	}

	public static String padRight(String text, int length) {
		if (text == null) {
			text = "";
		}
		if (text.length() >= length) {
			return text.substring(0, length);
		}
		return text + " ".repeat(length - text.length());
	}

	public static String padLeft(String text, int length) {
		if (text == null) {
			text = "";
		}
		if (text.length() >= length) {
			return text.substring(0, length);
		}
		return " ".repeat(length - text.length()) + text;
	}

	public static String padCenter(String text, int length) {
		if (text == null) {
			text = "";
		}
		if (text.length() >= length) {
			return text.substring(0, length);
		}
		int totalPadding = length - text.length();
		int leftPadding = totalPadding / 2;
		int rightPadding = totalPadding - leftPadding;
		return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
	}

	public static String horizontalLine(char character, int length) {
		return String.valueOf(character).repeat(length);
	}

	public static String formatKeyValue(String key, String value, int keyWidth) {
		return String.format("%-" + keyWidth + "s : %s", key, value != null ? value : "N/A");
	}
}
