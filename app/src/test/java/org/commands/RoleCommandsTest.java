package org.commands;

import org.junit.jupiter.api.*;
import org.RBACSystem;
import org.Role;
import org.AssignmentMetadata;
import org.Permission;
import org.RoleAssignment;
import org.User;
import org.commands.CommandParser;
import org.commands.CommandRegistry;
import org.mockito.*;
import org.repositories.AssignmentManager;
import org.repositories.RoleManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RoleCommandsTest {
	private CommandParser parser;

	@Mock
	private RBACSystem system;

	@Mock
	private RoleManager roleManager;

	@Mock
	private AssignmentManager assignmentManager;

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		parser = new CommandParser();
		System.setOut(new PrintStream(outContent));

		when(system.getRoleManager()).thenReturn(roleManager);
		when(system.getAssignmentManager()).thenReturn(assignmentManager);

		CommandRegistry.registerAllCommands(parser);
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}

	@Test
	@DisplayName("Should list roles")
	void testRoleList() {
		Role role1 = new Role("Admin", "Administrator");
		Role role2 = new Role("Manager", "Manager");
		when(roleManager.findAll()).thenReturn(Arrays.asList(role1, role2));
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		parser.executeCommand("role-list", scanner, system);

		String output = outContent.toString();
		assertTrue(output.contains("Admin"));
		assertTrue(output.contains("Manager"));
	}

	@Test
	@DisplayName("Should create role")
	void testRoleCreate() {
		when(roleManager.exists("NewRole")).thenReturn(false);
		String input = "NewRole\nNew role description\nno\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("role-create", scanner, system);

		verify(roleManager).add(any(Role.class));
		assertTrue(outContent.toString().contains("created"));
	}

	@Test
	@DisplayName("Should not create duplicate role")
	void testRoleCreateDuplicate() {
		when(roleManager.exists("Existing")).thenReturn(true);
		String input = "Existing\nDescription\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("role-create", scanner, system);

		verify(roleManager, never()).add(any(Role.class));
		assertTrue(outContent.toString().contains("already exists"));
	}

	@Test
	@DisplayName("Should view role")
	void testRoleView() {
		Role role = new Role("Admin", "Administrator");
		when(roleManager.findByName("Admin")).thenReturn(Optional.of(role));
		String input = "Admin\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("role-view", scanner, system);

		verify(roleManager).findByName("Admin");
	}

	@Test
	@DisplayName("Should delete role without assignments")
	void testRoleDelete() {
		Role role = new Role("TestRole", "Test");
		when(roleManager.findByName("TestRole")).thenReturn(Optional.of(role));
		when(assignmentManager.findByRole(role)).thenReturn(Collections.emptyList());
		String input = "TestRole\nyes\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("role-delete", scanner, system);

		verify(roleManager).remove(role);
		assertTrue(outContent.toString().contains("deleted"));
	}

	@Test
	@DisplayName("Should search roles")
	void testRoleSearch() {
		Role role = new Role("Admin", "Administrator");
		when(roleManager.findByFilter(any())).thenReturn(Collections.singletonList(role));
		String input = "1\nAdmin\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("role-search", scanner, system);

		assertTrue(outContent.toString().contains("Admin"));
	}
}
