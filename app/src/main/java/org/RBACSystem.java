package org;

import org.repositories.UserManager;
import org.repositories.RoleManager;
import org.repositories.AssignmentManager;

public class RBACSystem {
	private final UserManager userManager;
	private final RoleManager roleManager;
	private final AssignmentManager assignmentManager;
	private String currentUser;

	public RBACSystem() {
		this.userManager = new UserManager();
		this.roleManager = new RoleManager();
		this.assignmentManager = new AssignmentManager();
		this.currentUser = "system";
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public RoleManager getRoleManager() {
		return roleManager;
	}

	public AssignmentManager getAssignmentManager() {
		return assignmentManager;
	}

	public void setCurrentUser(String username) {
		this.currentUser = username;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void initialize() {
		Permission readUsers = new Permission("READ", "users", "Read users");
		Permission writeUsers = new Permission("WRITE", "users", "Write users");
		Permission deleteUsers = new Permission("DELETE", "users", "Delete users");
		Permission readRoles = new Permission("READ", "roles", "Read roles");
		Permission writeRoles = new Permission("WRITE", "roles", "Write roles");
		Permission deleteRoles = new Permission("DELETE", "roles", "Delete roles");

		Role adminRole = new Role("Admin", "Administrator with full access");
		adminRole.addPermission(readUsers);
		adminRole.addPermission(writeUsers);
		adminRole.addPermission(deleteUsers);
		adminRole.addPermission(readRoles);
		adminRole.addPermission(writeRoles);
		adminRole.addPermission(deleteRoles);

		Role managerRole = new Role("Manager", "Manager with limited access");
		managerRole.addPermission(readUsers);
		managerRole.addPermission(writeUsers);
		managerRole.addPermission(readRoles);

		Role viewerRole = new Role("Viewer", "Read-only access");
		viewerRole.addPermission(readUsers);
		viewerRole.addPermission(readRoles);

		roleManager.add(adminRole);
		roleManager.add(managerRole);
		roleManager.add(viewerRole);

		User admin = User.create("admin", "System Administrator", "admin@example.com");
		userManager.add(admin);

		AssignmentMetadata metadata = AssignmentMetadata.now("system", "Initial setup");
		RoleAssignment adminAssignment = new PermanentAssignment(admin, adminRole, metadata);
		assignmentManager.add(adminAssignment);

		setCurrentUser("admin");
	}

	public String generateStatistics() {
		int userCount = userManager.count();
		int roleCount = roleManager.count();
		int assignmentCount = assignmentManager.count();
		int activeCount = (int) assignmentManager.findAll().stream().filter(RoleAssignment::isActive).count();
		int expiredCount = assignmentCount - activeCount;

		double avgRolesPerUser = userCount > 0 ? (double) assignmentCount / userCount : 0;

		StringBuilder sb = new StringBuilder();
		sb.append("=== System Statistics ===\n");
		sb.append("Users: ").append(userCount).append("\n");
		sb.append("Roles: ").append(roleCount).append("\n");
		sb.append("Assignments: ").append(assignmentCount)
				.append(" (Active: ").append(activeCount)
				.append(", Expired: ").append(expiredCount).append(")\n");
		sb.append(String.format("Average roles per user: %.2f\n", avgRolesPerUser));

		return sb.toString();
	}
}
