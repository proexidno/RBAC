package org.utils;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_BOLD = "\u001B[1m";

	public static String promptString(Scanner scanner, String message, boolean required) {
		while (true) {
			System.out.print(message);
			if (!scanner.hasNextLine()) {
				return "";
			}
			String input = scanner.nextLine().trim();

			if (required && input.isEmpty()) {
				System.out.println(ANSI_RED + "This field is required!" + ANSI_RESET);
				continue;
			}

			return input;
		}
	}

	public static String promptString(Scanner scanner, String message, boolean required,
			java.util.function.Predicate<String> validator,
			String errorMessage) {
		while (true) {
			String input = promptString(scanner, message, required);

			if (input.isEmpty()) {
				return input;
			}

			if (validator != null && !validator.test(input)) {
				System.out.println(ANSI_RED + errorMessage + ANSI_RESET);
				continue;
			}

			return input;
		}
	}

	public static int promptInt(Scanner scanner, String message, int min, int max) {
		while (true) {
			System.out.print(message);
			if (!scanner.hasNextLine()) {
				return min;
			}
			String input = scanner.nextLine().trim();

			try {
				int value = Integer.parseInt(input);
				if (value < min || value > max) {
					System.out.println(ANSI_RED +
							"Please enter a number between " + min + " and " + max
							+ ANSI_RESET);
					continue;
				}
				return value;
			} catch (NumberFormatException e) {
				System.out.println(ANSI_RED + "Invalid number! Please try again." + ANSI_RESET);
			}
		}
	}

	public static boolean promptYesNo(Scanner scanner, String message) {
		while (true) {
			System.out.print(message + " (yes/no): ");
			if (!scanner.hasNextLine()) {
				return false;
			}
			String input = scanner.nextLine().trim().toLowerCase();

			if ("yes".equals(input) || "y".equals(input)) {
				return true;
			} else if ("no".equals(input) || "n".equals(input)) {
				return false;
			}

			System.out.println(ANSI_YELLOW + "Please enter 'yes' or 'no'" + ANSI_RESET);
		}
	}

	public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
		if (options == null || options.isEmpty()) {
			System.out.println(ANSI_RED + "No options available!" + ANSI_RESET);
			return null;
		}

		System.out.println("\n" + ANSI_BOLD + message + ANSI_RESET);
		for (int i = 0; i < options.size(); i++) {
			System.out.printf("  %d. %s\n", i + 1, options.get(i).toString());
		}

		int choice = promptInt(scanner, "Enter choice (0 to cancel): ", 0, options.size());

		if (choice == 0) {
			return null;
		}

		return options.get(choice - 1);
	}

	public static <T> T promptChoice(Scanner scanner, String message, List<T> options,
			java.util.function.Function<T, String> displayFunc) {
		if (options == null || options.isEmpty()) {
			System.out.println(ANSI_RED + "No options available!" + ANSI_RESET);
			return null;
		}

		System.out.println("\n" + ANSI_BOLD + message + ANSI_RESET);
		for (int i = 0; i < options.size(); i++) {
			System.out.printf("  %d. %s\n", i + 1, displayFunc.apply(options.get(i)));
		}

		int choice = promptInt(scanner, "Enter choice (0 to cancel): ", 0, options.size());

		if (choice == 0) {
			return null;
		}

		return options.get(choice - 1);
	}

	public static void printSuccess(String message) {
		System.out.println(ANSI_GREEN + "✓ " + message + ANSI_RESET);
	}

	public static void printError(String message) {
		System.out.println(ANSI_RED + "✗ " + message + ANSI_RESET);
	}

	public static void printWarning(String message) {
		System.out.println(ANSI_YELLOW + "⚠ " + message + ANSI_RESET);
	}

	public static void printInfo(String message) {
		System.out.println(ANSI_BLUE + "ℹ " + message + ANSI_RESET);
	}

	public static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	public static void printSeparator(char character, int length) {
		System.out.println(String.valueOf(character).repeat(length));
	}

	public static void printSeparator() {
		printSeparator('=', 60);
	}
}
