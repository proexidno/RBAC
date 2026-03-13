package org.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.RBACSystem;

public class CommandParser {
	private final Map<String, Command> commands = new HashMap<>();
	private final Map<String, String> commandDescriptions = new HashMap<>();

	public void registerCommand(String name, String description, Command command) {
		commands.put(name.toLowerCase(), command);
		commandDescriptions.put(name.toLowerCase(), description);
	}

	public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
		Command command = commands.get(commandName.toLowerCase());
		if (command != null) {
			command.execute(scanner, system);
		} else {
			System.out.println("Unknown command: " + commandName);
		}
	}

	public void printHelp() {
		System.out.println("=== Available Commands ===");
		commandDescriptions
				.forEach((name, desc) -> System.out.println(String.format("%-25s - %s", name, desc)));
	}

	public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
		if (input == null || input.trim().isEmpty())
			return;
		String[] parts = input.trim().split("\\s+", 2);
		String commandName = parts[0];
		executeCommand(commandName, scanner, system);
	}

	public boolean hasCommand(String name) {
		return commands.containsKey(name.toLowerCase());
	}

	public int getCommandCount() {
		return commands.size();
	}
}
