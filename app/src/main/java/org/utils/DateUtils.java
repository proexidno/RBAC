package org.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static String getCurrentDate() {
		return LocalDate.now().format(DATE_FORMATTER);
	}

	public static String getCurrentDateTime() {
		return LocalDateTime.now().format(DATETIME_FORMATTER);
	}

	public static String getCurrentTimestamp() {
		return Instant.now().toString();
	}

	public static boolean isBefore(String date1, String date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		return date1.compareTo(date2) < 0;
	}

	public static boolean isAfter(String date1, String date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		return date1.compareTo(date2) > 0;
	}

	public static boolean isEqual(String date1, String date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		return date1.equals(date2);
	}

	public static String addDays(String date, int days) {
		if (date == null || date.isEmpty()) {
			return getCurrentDate();
		}

		try {
			LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
			LocalDate newDate = localDate.plusDays(days);
			return newDate.format(DATE_FORMATTER);
		} catch (Exception e) {
			return date;
		}
	}

	public static String addDaysToDateTime(String dateTime, int days) {
		if (dateTime == null || dateTime.isEmpty()) {
			return getCurrentDateTime();
		}

		try {
			LocalDateTime localDateTime = LocalDateTime.parse(dateTime, DATETIME_FORMATTER);
			LocalDateTime newDateTime = localDateTime.plusDays(days);
			return newDateTime.format(DATETIME_FORMATTER);
		} catch (Exception e) {
			return dateTime;
		}
	}

	public static String formatRelativeTime(String date) {
		if (date == null || date.isEmpty()) {
			return "Unknown";
		}

		try {
			LocalDate targetDate = LocalDate.parse(date, DATE_FORMATTER);
			LocalDate today = LocalDate.now();

			long daysDiff = ChronoUnit.DAYS.between(today, targetDate);

			if (daysDiff == 0) {
				return "Today";
			} else if (daysDiff == 1) {
				return "Tomorrow";
			} else if (daysDiff == -1) {
				return "Yesterday";
			} else if (daysDiff > 0) {
				return "in " + daysDiff + " days";
			} else {
				return Math.abs(daysDiff) + " days ago";
			}
		} catch (Exception e) {
			return date;
		}
	}

	public static String formatRelativeTime(String dateTime, boolean includeTime) {
		if (dateTime == null || dateTime.isEmpty()) {
			return "Unknown";
		}

		try {
			Instant targetInstant = Instant.parse(dateTime);
			Instant now = Instant.now();

			long secondsDiff = ChronoUnit.SECONDS.between(now, targetInstant);

			if (secondsDiff < 0) {
				secondsDiff = Math.abs(secondsDiff);
				if (secondsDiff < 60) {
					return secondsDiff + " seconds ago";
				} else if (secondsDiff < 3600) {
					return (secondsDiff / 60) + " minutes ago";
				} else if (secondsDiff < 86400) {
					return (secondsDiff / 3600) + " hours ago";
				} else {
					return (secondsDiff / 86400) + " days ago";
				}
			} else {
				if (secondsDiff < 60) {
					return "in " + secondsDiff + " seconds";
				} else if (secondsDiff < 3600) {
					return "in " + (secondsDiff / 60) + " minutes";
				} else if (secondsDiff < 86400) {
					return "in " + (secondsDiff / 3600) + " hours";
				} else {
					return "in " + (secondsDiff / 86400) + " days";
				}
			}
		} catch (Exception e) {
			return dateTime;
		}
	}

	public static LocalDate parseDate(String date) {
		if (date == null || date.isEmpty()) {
			return null;
		}
		try {
			return LocalDate.parse(date, DATE_FORMATTER);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isValidDate(String date) {
		return parseDate(date) != null;
	}

	public static long getDaysBetween(String date1, String date2) {
		if (date1 == null || date2 == null) {
			return 0;
		}
		try {
			LocalDate d1 = LocalDate.parse(date1, DATE_FORMATTER);
			LocalDate d2 = LocalDate.parse(date2, DATE_FORMATTER);
			return ChronoUnit.DAYS.between(d1, d2);
		} catch (Exception e) {
			return 0;
		}
	}

	public static boolean isExpired(String date) {
		if (date == null || date.isEmpty()) {
			return false;
		}
		return isBefore(date, getCurrentDate());
	}
}
