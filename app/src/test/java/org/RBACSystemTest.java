package org;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

public class RBACSystemTest {
	private RBACSystem system;

	@BeforeEach
	void setUp() {
		system = new RBACSystem();
	}

	@Test
	@DisplayName("Should initialize system with default data")
	void testInitialize() {
		system.initialize();

		assertTrue(system.getUserManager().exists("admin"));
		assertTrue(system.getRoleManager().exists("Admin"));
		assertTrue(system.getRoleManager().exists("Manager"));
		assertTrue(system.getRoleManager().exists("Viewer"));
		assertEquals("admin", system.getCurrentUser());
	}

	@Test
	@DisplayName("Should set and get current user")
	void testSetGetCurrentUser() {
		system.setCurrentUser("testuser");
		assertEquals("testuser", system.getCurrentUser());
	}

	@Test
	@DisplayName("Should return managers")
	void testGetManagers() {
		assertNotNull(system.getUserManager());
		assertNotNull(system.getRoleManager());
		assertNotNull(system.getAssignmentManager());
	}

	@Test
	@DisplayName("Should generate statistics")
	void testGenerateStatistics() {
		system.initialize();
		String stats = system.generateStatistics();

		assertTrue(stats.contains("Users:"));
		assertTrue(stats.contains("Roles:"));
		assertTrue(stats.contains("Assignments:"));
		assertTrue(stats.contains("Active:"));
		assertTrue(stats.contains("Average roles per user:"));
	}

	@Test
	@DisplayName("Should handle empty statistics")
	void testGenerateStatisticsEmpty() {
		String stats = system.generateStatistics();
		assertTrue(stats.contains("Users: 0"));
	}
}
