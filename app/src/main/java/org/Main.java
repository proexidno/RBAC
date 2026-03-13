package org;

import org.commands.CommandParser;
import org.commands.CommandRegistry;
import java.util.Scanner;

public class Main {
	private static final String PROMPT = "> ";

	public static void main(String[] args) {
		RBACSystem system = new RBACSystem();
		system.initialize();

		CommandParser parser = new CommandParser();
		CommandRegistry.registerAllCommands(parser);

		Scanner scanner = new Scanner(System.in);
		displayBanner(system);
		runCommandLoop(scanner, parser, system);
		scanner.close();

		System.out.println("Goodbye!");
	}

	private static void displayBanner(RBACSystem system) {
		System.out.println("RBAC Management System v1.0");
		System.out.println("Type 'help' for available commands");
		System.out.println("Type 'exit' to quit");
		System.out.println();
		System.out.println("Current user: " + system.getCurrentUser());
		System.out.println();
	}

	private static void runCommandLoop(Scanner scanner, CommandParser parser, RBACSystem system) {
		while (true) {
			System.out.print(PROMPT);

			if (!scanner.hasNextLine()) {
				break;
			}

			String input = scanner.nextLine();

			if (input == null || input.trim().isEmpty()) {
				continue;
			}

			String command = input.trim().split("\\s+")[0].toLowerCase();

			if ("exit".equals(command)) {
				System.out.print("Confirm exit (yes): ");
				if (!scanner.hasNextLine()) {
					break;
				}
				String confirmation = scanner.nextLine().trim().toLowerCase();
				if ("yes".equals(confirmation)) {
					break;
				}
				continue;
			}

			try {
				parser.parseAndExecute(input, scanner, system);
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
				if (System.getProperty("debug") != null) {
					e.printStackTrace();
				}
			}
		}
	}
}
