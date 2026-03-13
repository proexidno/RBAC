package org.commands;

import org.junit.jupiter.api.*;
import org.RBACSystem;
import org.commands.CommandParser;
import org.commands.CommandRegistry;
import org.repositories.UserManager;
import org.repositories.RoleManager;
import org.repositories.AssignmentManager;
import org.mockito.*;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommandRegistryTest {
	private CommandParser parser;

	@Mock
	private RBACSystem system;

	@Mock
	private UserManager userManager;

	@Mock
	private RoleManager roleManager;

	@Mock
	private AssignmentManager assignmentManager;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		parser = new CommandParser();
		when(system.getUserManager()).thenReturn(userManager);
		when(system.getRoleManager()).thenReturn(roleManager);
		when(system.getAssignmentManager()).thenReturn(assignmentManager);
		when(system.getCurrentUser()).thenReturn("admin");
	}

	@Test
	@DisplayName("Should register all commands")
	void testRegisterAllCommands() {
		CommandRegistry.registerAllCommands(parser);

		assertTrue(parser.hasCommand("user-list"));
		assertTrue(parser.hasCommand("user-create"));
		assertTrue(parser.hasCommand("user-view"));
		assertTrue(parser.hasCommand("user-update"));
		assertTrue(parser.hasCommand("user-delete"));
		assertTrue(parser.hasCommand("user-search"));

		assertTrue(parser.hasCommand("role-list"));
		assertTrue(parser.hasCommand("role-create"));
		assertTrue(parser.hasCommand("role-view"));
		assertTrue(parser.hasCommand("role-delete"));
		assertTrue(parser.hasCommand("role-add-permission"));
		assertTrue(parser.hasCommand("role-remove-permission"));
		assertTrue(parser.hasCommand("role-search"));

		assertTrue(parser.hasCommand("assign-role"));
		assertTrue(parser.hasCommand("revoke-role"));
		assertTrue(parser.hasCommand("assignment-list"));
		assertTrue(parser.hasCommand("assignment-list-user"));
		assertTrue(parser.hasCommand("assignment-list-role"));
		assertTrue(parser.hasCommand("assignment-active"));
		assertTrue(parser.hasCommand("assignment-expired"));
		assertTrue(parser.hasCommand("assignment-extend"));
		assertTrue(parser.hasCommand("assignment-search"));

		assertTrue(parser.hasCommand("permissions-user"));
		assertTrue(parser.hasCommand("permissions-check"));

		assertTrue(parser.hasCommand("help"));
		assertTrue(parser.hasCommand("stats"));
		assertTrue(parser.hasCommand("clear"));
		assertTrue(parser.hasCommand("exit"));
		assertTrue(parser.hasCommand("save"));
		assertTrue(parser.hasCommand("load"));
	}

	@Test
	@DisplayName("Should have minimum command count")
	void testCommandCount() {
		CommandRegistry.registerAllCommands(parser);
		assertTrue(parser.getCommandCount() >= 25);
	}

	@Test
	@DisplayName("Should execute user-list command")
	void testUserListCommand() {
		CommandRegistry.registerAllCommands(parser);
		when(userManager.findAll()).thenReturn(java.util.Collections.emptyList());
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		assertDoesNotThrow(() -> parser.executeCommand("user-list", scanner, system));
	}

	@Test
	@DisplayName("Should execute stats command")
	void testStatsCommand() {
		CommandRegistry.registerAllCommands(parser);
		when(system.generateStatistics()).thenReturn("Test stats");
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		assertDoesNotThrow(() -> parser.executeCommand("stats", scanner, system));
	}

	@Test
	@DisplayName("Should execute help command")
	void testHelpCommand() {
		CommandRegistry.registerAllCommands(parser);
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		assertDoesNotThrow(() -> parser.executeCommand("help", scanner, system));
	}
}
