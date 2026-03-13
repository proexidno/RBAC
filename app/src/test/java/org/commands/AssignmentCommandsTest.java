package org.commands;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.repositories.AssignmentManager;
import org.repositories.RoleManager;
import org.repositories.UserManager;
import org.RBACSystem;
import org.AssignmentMetadata;
import org.PermanentAssignment;
import org.Role;
import org.RoleAssignment;
import org.TemporaryAssignment;
import org.User;
import org.commands.CommandParser;
import org.commands.CommandRegistry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AssignmentCommandsTest {
	private CommandParser parser;

	@Mock
	private RBACSystem system;

	@Mock
	private UserManager userManager;

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

		when(system.getUserManager()).thenReturn(userManager);
		when(system.getRoleManager()).thenReturn(roleManager);
		when(system.getAssignmentManager()).thenReturn(assignmentManager);
		when(system.getCurrentUser()).thenReturn("admin");

		CommandRegistry.registerAllCommands(parser);
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}

	@Test
	@DisplayName("Should assign permanent role")
	void testAssignRolePermanent() {
		User user = User.create("testuser", "Test", "test@example.com");
		Role role = new Role("Admin", "Administrator");
		when(userManager.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(roleManager.findByName("Admin")).thenReturn(Optional.of(role));
		String input = "testuser\nAdmin\nyes\nTest reason\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("assign-role", scanner, system);

		verify(assignmentManager).add(any(RoleAssignment.class));
		assertTrue(outContent.toString().contains("assigned"));
	}

	@Test
	@DisplayName("Should assign temporary role")
	void testAssignRoleTemporary() {
		User user = User.create("testuser", "Test", "test@example.com");
		Role role = new Role("Admin", "Administrator");
		when(userManager.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(roleManager.findByName("Admin")).thenReturn(Optional.of(role));
		String input = "testuser\nAdmin\nno\nTest reason\n30\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("assign-role", scanner, system);

		verify(assignmentManager).add(any(TemporaryAssignment.class));
	}

	@Test
	@DisplayName("Should list assignments")
	void testAssignmentList() {
		RoleAssignment assignment = mock(RoleAssignment.class);
		when(assignment.assignmentId()).thenReturn("test-id-123");
		when(assignment.user()).thenReturn(User.create("user1", "User", "user@example.com"));
		when(assignment.role()).thenReturn(new Role("Admin", "Admin"));
		when(assignment.assignmentType()).thenReturn("PERMANENT");
		when(assignment.isActive()).thenReturn(true);
		when(assignmentManager.findAll()).thenReturn(Collections.singletonList(assignment));
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		parser.executeCommand("assignment-list", scanner, system);

		String output = outContent.toString();
		assertTrue(output.contains("user1"));
		assertTrue(output.contains("Admin"));
	}

	@Test
	@DisplayName("Should list user assignments")
	void testAssignmentListUser() {
		User user = User.create("testuser", "Test", "test@example.com");
		Role role = new Role("Admin", "Administrator");
		AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test reason");

		RoleAssignment assignment = mock(RoleAssignment.class);
		when(assignment.assignmentId()).thenReturn("test-assignment-id-123");
		when(assignment.user()).thenReturn(user);
		when(assignment.role()).thenReturn(role);
		when(assignment.assignmentType()).thenReturn("PERMANENT");
		when(assignment.isActive()).thenReturn(true);
		when(assignment.metadata()).thenReturn(metadata);

		when(userManager.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(assignmentManager.findByUser(user)).thenReturn(Collections.singletonList(assignment));

		String input = "testuser\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
		parser.executeCommand("assignment-list-user", scanner, system);

		String output = outContent.toString();
		assertTrue(output.contains("Assignment"));
		assertTrue(output.contains("test-assignment-id-123"));
		assertTrue(output.contains("testuser"));
		assertTrue(output.contains("Admin"));
		assertTrue(output.contains("PERMANENT"));
		assertTrue(output.contains("ACTIVE"));
		assertTrue(output.contains("admin"));
		assertTrue(output.contains("Test reason"));
	}

	@Test
	@DisplayName("Should list active assignments")
	void testAssignmentActive() {
		RoleAssignment assignment = mock(RoleAssignment.class);
		when(assignment.isActive()).thenReturn(true);
		when(assignment.user()).thenReturn(User.create("user1", "User", "user@example.com"));
		when(assignment.role()).thenReturn(new Role("Admin", "Admin"));
		when(assignmentManager.findAll()).thenReturn(Collections.singletonList(assignment));
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		parser.executeCommand("assignment-active", scanner, system);

		assertTrue(outContent.toString().contains("user1"));
	}

	@Test
	@DisplayName("Should extend temporary assignment")
	void testAssignmentExtend() {
		String input = "test-id\n2026-12-31T23:59:59Z\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("assignment-extend", scanner, system);

		verify(assignmentManager).extendTemporaryAssignment(eq("test-id"), anyString());
	}

	@Test
	@DisplayName("Should revoke permanent role")
	void testRevokeRole() {
		User user = User.create("testuser", "Test", "test@example.com");
		PermanentAssignment assignment = mock(PermanentAssignment.class);
		when(assignment.assignmentId()).thenReturn("test-id");
		when(assignment.role()).thenReturn(new Role("Admin", "Admin"));
		when(assignment.assignmentType()).thenReturn("PERMANENT");
		when(userManager.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(assignmentManager.findByUser(user)).thenReturn(Collections.singletonList(assignment));
		when(assignmentManager.findById("test-id")).thenReturn(Optional.of(assignment));
		String input = "testuser\ntest-id\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("revoke-role", scanner, system);

		verify(assignment).revoke();
	}

	@Test
	@DisplayName("Should search assignments by user")
	void testAssignmentSearchByUser() {
		User user = User.create("testuser", "Test", "test@example.com");
		RoleAssignment assignment = mock(RoleAssignment.class);
		when(assignment.assignmentId()).thenReturn("test-id");
		when(assignment.user()).thenReturn(user);
		when(assignment.role()).thenReturn(new Role("Admin", "Admin"));
		when(userManager.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(assignmentManager.findByUser(user)).thenReturn(Collections.singletonList(assignment));
		String input = "1\ntestuser\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("assignment-search", scanner, system);

		assertTrue(outContent.toString().contains("test-id"));
	}
}
