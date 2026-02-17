package org.example;

import org.User;

import java.time.Instant;
import java.util.Set;

import org.AssignmentMetadata;
import org.PermanentAssignment;
import org.Permission;
import org.Role;
import org.TemporaryAssignment;

public class App {
	public static void main(String[] args) {

		try {
			User.create("usr123", "Alice Smith", "alice@example.com");
		} catch (Exception e) {
			System.out.println("Valid user failed: " + e.getMessage());
		}

		try {
			User.create("", "Alice Smith", "alice@example.com");
			System.out.println("Empty username should fail");
		} catch (IllegalArgumentException e) {
		}

		try {
			User.create("ab", "Alice Smith", "alice@example.com");
			System.out.println("Short username should fail");
		} catch (IllegalArgumentException e) {
		}

		try {
			User.create("user@name", "Alice Smith", "alice@example.com");
			System.out.println("Invalid username chars should fail");
		} catch (IllegalArgumentException e) {
		}

		try {
			User.create("valid_user", "Alice Smith", "invalid-email");
			System.out.println("Invalid email should fail");
		} catch (IllegalArgumentException e) {
		}

		User user = User.create("testuser", "Test User", "test@example.com");
		if (!"testuser (Test User) <test@example.com>".equals(user.format())) {
			System.out.println("User format incorrect");
		}

		Permission perm = new Permission("read", "users", "Can read users");
		if (!"READ on users: Can read users".equals(perm.format())) {
			System.out.println("Permission format incorrect");
		}

		if (!perm.matches("READ", "users")) {
			System.out.println("Permission matches exact failed");
		}

		try {
			new Permission("", "res", "desc");
			System.out.println("Empty name should fail");
		} catch (IllegalArgumentException e) {
		}

		try {
			new Permission("READ WRITE", "res", "desc");
			System.out.println("Space in name should fail");
		} catch (IllegalArgumentException e) {
		}

		Role role = new Role("Admin", "System administrator");
		Permission p1 = new Permission("READ", "users", "View users");
		Permission p2 = new Permission("WRITE", "users", "Edit users");
		role.addPermission(p1);
		role.addPermission(p2);

		if (!role.hasPermission(p1) && role.hasPermission("READ", "users")) {
			System.out.println("Role hasPermission failed");
		}

		Set<Permission> perms = role.getPermissions();
		if (perms.size() != 2 || !perms.contains(p1) || !perms.contains(p2)) {
			System.out.println("Role getPermissions failed");
		}

		role.removePermission(p1);
		if (role.hasPermission(p1)) {
			System.out.println("Role removePermission failed");
		}

		Role role2 = new Role("Admin", "System administrator");
		if (role == role2) {
			System.out.println("Role equality by ID failed");
		}

		AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");
		if (!meta.assignedBy().equals("admin") || !meta.reason().equals("Initial setup")) {
			System.out.println("AssignmentMetadata.now failed");
		}

		User adminUser = User.create("admin", "Admin User", "admin@example.com");
		Role adminRole = new Role("Administrator", "Full access");
		AssignmentMetadata permMeta = AssignmentMetadata.now("system", "Bootstrap");
		PermanentAssignment permAssign = new PermanentAssignment(adminUser, adminRole, permMeta);

		if (!permAssign.isActive() || "PERMANENT" != permAssign.assignmentType()) {
			System.out.println("PermanentAssignment state/type incorrect");
		}

		permAssign.revoke();
		if (permAssign.isActive()) {
			System.out.println("PermanentAssignment revoke failed");
		}

		String future = Instant.now().plusSeconds(3600).toString();
		String past = Instant.now().minusSeconds(3600).toString();

		TemporaryAssignment tempActive = new TemporaryAssignment(
				adminUser, adminRole,
				AssignmentMetadata.now("admin", "Trial"),
				future, false);

		if (!tempActive.isActive() || !"TEMPORARY".equals(tempActive.assignmentType())) {
			System.out.println("TemporaryAssignment active check failed");
		}

		TemporaryAssignment tempExpired = new TemporaryAssignment(
				adminUser, adminRole,
				AssignmentMetadata.now("admin", "Expired trial"),
				past, false);

		if (tempExpired.isActive()) {
			System.out.println("TemporaryAssignment expired check failed");
		}

		TemporaryAssignment expiresCheck = new TemporaryAssignment(
				adminUser, adminRole,
				AssignmentMetadata.now("admin", "Trial"),
				Instant.now().plusSeconds(-1).toString(), false);

		boolean wasActive = expiresCheck.isActive();
		String newTS = Instant.now().plusSeconds(7200).toString();
		tempActive.extend(newTS);

		if (wasActive || !tempActive.isActive()) {
			System.out.println("TemporaryAssignment extend failed");
		}

		if (!permAssign.summary().contains("[PERMANENT]") ||
				!permAssign.summary().contains("Administrator") ||
				!permAssign.summary().contains("admin")) {
			System.out.println("PermanentAssignment summary format broken");
		}

		if (!tempActive.summary().contains("[TEMPORARY]") ||
				!tempActive.summary().contains("Expires At:")) {
			System.out.println("TemporaryAssignment summary format broken");
		}

		System.out.println("All manual tests completed.");
	}
}
