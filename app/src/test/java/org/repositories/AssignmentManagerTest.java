package org.repositories;

import org.AssignmentMetadata;
import org.PermanentAssignment;
import org.Permission;
import org.Role;
import org.RoleAssignment;
import org.TemporaryAssignment;
import org.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AssignmentManagerTest {

	private AssignmentManager assignmentManager;

	private User testUser;
	private User testUser2;
	private Role testRole;
	private Permission testPermission;

	@BeforeEach
	void setUp() {
		assignmentManager = new AssignmentManager();

		try {
			testUser = User.create("test_user", "Test User", "test@example.com");
			testUser2 = User.create("test_user2", "Test User 2", "test2@example.com");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		testRole = new Role("TEST_ROLE", "Test Role Description");
		testPermission = new Permission("READ", "docs", "Read docs");
		testRole.addPermission(testPermission);
	}

	@Test
	@DisplayName("Add Permanent Assignment successfully")
	void addPermanentAssignmentSuccess() {
		AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Initial setup");
		RoleAssignment assignment = new PermanentAssignment(testUser, testRole, metadata);

		assignmentManager.add(assignment);

		List<RoleAssignment> assignments = assignmentManager.findByUser(testUser);
		assertEquals(1, assignments.size());

		RoleAssignment found = assignments.get(0);
		assertEquals("PERMANENT", found.assignmentType());
		assertTrue(found.isActive());
	}

	@Test
	@DisplayName("Add Temporary Assignment successfully")
	void addTemporaryAssignmentSuccess() {
		Instant futureDate = Instant.now().plusSeconds(30 * 24 * 60 * 60);
		AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Trial period");

		RoleAssignment assignment = new TemporaryAssignment(testUser, testRole, metadata, futureDate, false);

		assignmentManager.add(assignment);

		List<RoleAssignment> assignments = assignmentManager.findByUser(testUser);
		assertEquals(1, assignments.size());
		assertEquals("TEMPORARY", assignments.get(0).assignmentType());
		assertTrue(assignments.get(0).isActive());
	}

	@Test
	@DisplayName("Revoke Assignment removes it from storage")
	void revokeAssignmentRemovesFromStorage() {
		AssignmentMetadata meta = AssignmentMetadata.now("admin", "First");
		RoleAssignment assignment = new PermanentAssignment(testUser, testRole, meta);

		assignmentManager.add(assignment);
		assertEquals(1, assignmentManager.count());

		String id = assignment.assignmentId();
		assignmentManager.revokeAssignment(id);

		assertEquals(0, assignmentManager.count());
		assertTrue(assignmentManager.findByUser(testUser).isEmpty());

		RoleAssignment newAssignment = new PermanentAssignment(testUser, testRole,
				AssignmentMetadata.now("admin", "Second"));
		assertDoesNotThrow(() -> assignmentManager.add(newAssignment));
		assertEquals(1, assignmentManager.count());
	}

	@Test
	@DisplayName("Get User Permissions Aggregation")
	void getUserPermissionsAggregation() {
		Role role1 = new Role("R1", "Role 1");
		role1.addPermission(new Permission("READ", "files", "testPermission"));

		Role role2 = new Role("R2", "Role 2");
		role2.addPermission(new Permission("WRITE", "files", "testPermission"));
		role2.addPermission(new Permission("DELETE", "files", "testPermission"));

		AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Reason");
		AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Reason");

		assignmentManager.add(new PermanentAssignment(testUser, role1, meta1));
		assignmentManager.add(new PermanentAssignment(testUser, role2, meta2));

		Set<Permission> perms = assignmentManager.getUserPermissions(testUser);

		assertEquals(3, perms.size());
		assertTrue(perms.stream().anyMatch(p -> p.name().equals("READ")));
		assertTrue(perms.stream().anyMatch(p -> p.name().equals("DELETE")));
	}

	@Test
	@DisplayName("Check User Has Permission")
	void userHasPermission() {
		AssignmentMetadata meta = AssignmentMetadata.now("admin", "Reason");
		assignmentManager.add(new PermanentAssignment(testUser, testRole, meta));

		assertTrue(assignmentManager.userHasPermission(testUser, "READ", "docs"));
		assertFalse(assignmentManager.userHasPermission(testUser, "WRITE", "docs"));
	}

	@Test
	@DisplayName("Extend Temporary Assignment (Convert Instant to String)")
	void extendTemporaryAssignment() {
		Instant originalDate = Instant.now().plusSeconds(5 * 24 * 60 * 60);
		Instant newDate = Instant.now().plusSeconds(60 * 24 * 60 * 60);

		AssignmentMetadata meta = AssignmentMetadata.now("admin", "Trial");
		RoleAssignment assignment = new TemporaryAssignment(testUser, testRole, meta, originalDate, false);

		assignmentManager.add(assignment);

		List<RoleAssignment> assignments = assignmentManager.findByUser(testUser);
		String id = assignments.get(0).assignmentId();

		String newDateStr = newDate.toString();
		assignmentManager.extendTemporaryAssignment(id, newDateStr);
		assertDoesNotThrow(() -> assignmentManager.extendTemporaryAssignment(id, newDateStr));
	}

	@Test
	@DisplayName("Get Active vs Expired Assignments")
	void getActiveAndExpiredAssignments() {
		Instant pastDate = Instant.now().minusSeconds(5 * 24 * 60 * 60);
		Instant futureDate = Instant.now().plusSeconds(5 * 24 * 60 * 60);

		AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Expired");
		AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Active");

		assignmentManager.add(new TemporaryAssignment(testUser, testRole, meta1, pastDate, false));
		assignmentManager.add(new TemporaryAssignment(testUser2, testRole, meta2, futureDate, false));

		List<RoleAssignment> active = assignmentManager.getActiveAssignments();
		List<RoleAssignment> expired = assignmentManager.getExpiredAssignments();

		assertEquals(1, active.size());
		assertEquals(1, expired.size());

		assertTrue(active.get(0).user().username().equals("test_user2"));
		assertTrue(expired.get(0).user().username().equals("test_user"));
	}

	@Test
	@DisplayName("Temporary Assignment Expires Correctly")
	void temporaryAssignmentExpirationLogic() {
		Instant oneSecondAgo = Instant.now().minusSeconds(1);

		AssignmentMetadata meta = AssignmentMetadata.now("admin", "Just Expired");
		RoleAssignment assignment = new TemporaryAssignment(testUser, testRole, meta, oneSecondAgo, false);

		assignmentManager.add(assignment);

		List<RoleAssignment> assignments = assignmentManager.findByUser(testUser);
		assertEquals(1, assignments.size());

		assertFalse(assignments.get(0).isActive());

		List<RoleAssignment> expired = assignmentManager.getExpiredAssignments();
		assertTrue(expired.contains(assignments.get(0)));

		List<RoleAssignment> active = assignmentManager.getActiveAssignments();
		assertFalse(active.contains(assignments.get(0)));
	}
}
