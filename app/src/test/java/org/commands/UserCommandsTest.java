package org.commands;

import org.junit.jupiter.api.*;
import org.RBACSystem;
import org.User;
import org.commands.CommandParser;
import org.commands.CommandRegistry;
import org.repositories.AssignmentManager;
import org.repositories.UserManager;
import org.mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserCommandsTest {
	private CommandParser parser;

	@Mock
	private RBACSystem system;

	@Mock
	private UserManager userManager;

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
		when(system.getAssignmentManager()).thenReturn(assignmentManager);

		CommandRegistry.registerAllCommands(parser);
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}

	@Test
	@DisplayName("Should list users")
	void testUserList() {
		User user1 = User.create("user1", "User One", "user1@example.com");
		User user2 = User.create("user2", "User Two", "user2@example.com");
		when(userManager.findAll()).thenReturn(Arrays.asList(user1, user2));
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		parser.executeCommand("user-list", scanner, system);

		String output = outContent.toString();
		assertTrue(output.contains("user1"));
		assertTrue(output.contains("user2"));
	}

	@Test
	@DisplayName("Should show no users when empty")
	void testUserListEmpty() {
		when(userManager.findAll()).thenReturn(Collections.emptyList());
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		parser.executeCommand("user-list", scanner, system);

		assertTrue(outContent.toString().contains("No users found"));
	}

	@Test
	@DisplayName("Should create user")
	void testUserCreate() {
		when(userManager.exists("newuser")).thenReturn(false);
		String input = "newuser\nNew User\nnew@example.com\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("user-create", scanner, system);

		verify(userManager).add(any(User.class));
		assertTrue(outContent.toString().contains("created successfully"));
	}

	@Test
	@DisplayName("Should not create duplicate user")
	void testUserCreateDuplicate() {
		when(userManager.exists("existing")).thenReturn(true);
		String input = "existing\nName\nemail@example.com\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("user-create", scanner, system);

		verify(userManager, never()).add(any(User.class));
		assertTrue(outContent.toString().contains("already exists"));
	}

	@Test
	@DisplayName("Should view user")
	void testUserView() {
		User user = User.create("testuser", "Test User", "test@example.com");
		when(userManager.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(assignmentManager.findByUser(user)).thenReturn(Collections.emptyList());
		String input = "testuser\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("user-view", scanner, system);

		String output = outContent.toString();
		assertTrue(output.contains("testuser"));
		assertTrue(output.contains("Test User"));
	}

	@Test
	@DisplayName("Should show user not found")
	void testUserViewNotFound() {
		when(userManager.findByUsername("notfound")).thenReturn(Optional.empty());
		String input = "notfound\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("user-view", scanner, system);

		assertTrue(outContent.toString().contains("not found"));
	}

	@Test
	@DisplayName("Should update user")
	void testUserUpdate() {
		when(userManager.exists("testuser")).thenReturn(true);
		String input = "testuser\nNew Name\nnew@example.com\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("user-update", scanner, system);

		verify(userManager).update(eq("testuser"), anyString(), anyString());
		assertTrue(outContent.toString().contains("updated"));
	}

	@Test
	@DisplayName("Should delete user with confirmation")
	void testUserDelete() {
		User user = User.create("testuser", "Test", "test@example.com");
		when(userManager.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(assignmentManager.findByUser(user)).thenReturn(Collections.emptyList());
		String input = "testuser\nyes\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("user-delete", scanner, system);

		verify(userManager).remove(user);
		assertTrue(outContent.toString().contains("deleted"));
	}

	@Test
	@DisplayName("Should cancel delete without confirmation")
	void testUserDeleteCancelled() {
		User user = User.create("testuser", "Test", "test@example.com");
		when(userManager.findByUsername("testuser")).thenReturn(Optional.of(user));
		String input = "testuser\nno\n";
		Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

		parser.executeCommand("user-delete", scanner, system);

		verify(userManager, never()).remove(user);
		assertTrue(outContent.toString().contains("cancelled"));
	}
}
