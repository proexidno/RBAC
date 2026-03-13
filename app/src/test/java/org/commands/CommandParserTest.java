package org.commands;

import org.junit.jupiter.api.*;
import org.RBACSystem;
import org.commands.CommandParser;
import org.mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommandParserTest {
	private CommandParser parser;

	@Mock
	private RBACSystem system;

	@Mock
	private Command mockCommand;

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		parser = new CommandParser();
		System.setOut(new PrintStream(outContent));
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}

	@Test
	@DisplayName("Should register command")
	void testRegisterCommand() {
		parser.registerCommand("test", "Test description", mockCommand);

		assertTrue(parser.hasCommand("test"));
		assertTrue(parser.hasCommand("TEST"));
		assertEquals(1, parser.getCommandCount());
	}

	@Test
	@DisplayName("Should execute registered command")
	void testExecuteCommand() {
		parser.registerCommand("test", "Test", mockCommand);
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		parser.executeCommand("test", scanner, system);

		verify(mockCommand).execute(scanner, system);
	}

	@Test
	@DisplayName("Should handle unknown command")
	void testExecuteUnknownCommand() {
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
		parser.executeCommand("unknown", scanner, system);

		assertTrue(outContent.toString().contains("Unknown command: unknown"));
	}

	@Test
	@DisplayName("Should parse and execute command")
	void testParseAndExecute() {
		parser.registerCommand("help", "Help", mockCommand);
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		parser.parseAndExecute("help", scanner, system);

		verify(mockCommand).execute(scanner, system);
	}

	@Test
	@DisplayName("Should handle empty input")
	void testParseAndExecuteEmpty() {
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		assertDoesNotThrow(() -> parser.parseAndExecute("", scanner, system));
		assertDoesNotThrow(() -> parser.parseAndExecute(null, scanner, system));
	}

	@Test
	@DisplayName("Should print help")
	void testPrintHelp() {
		parser.registerCommand("test1", "Description 1", mockCommand);
		parser.registerCommand("test2", "Description 2", mockCommand);

		parser.printHelp();

		String output = outContent.toString();
		assertTrue(output.contains("Available Commands"));
		assertTrue(output.contains("test1"));
		assertTrue(output.contains("test2"));
		assertTrue(output.contains("Description 1"));
		assertTrue(output.contains("Description 2"));
	}

	@Test
	@DisplayName("Should handle command with arguments")
	void testParseAndExecuteWithArgs() {
		parser.registerCommand("user-view", "View user", mockCommand);
		Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));

		parser.parseAndExecute("user-view admin", scanner, system);

		verify(mockCommand).execute(scanner, system);
	}
}
