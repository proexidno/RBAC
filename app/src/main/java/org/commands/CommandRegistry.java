package org.commands;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.AssignmentMetadata;
import org.PermanentAssignment;
import org.Permission;
import org.RBACSystem;
import org.Role;
import org.RoleAssignment;
import org.TemporaryAssignment;
import org.User;

public class CommandRegistry {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("\\w+@\\w+\\.\\w+");
	private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

	public static void registerAllCommands(CommandParser parser) {
		registerUserCommands(parser);
		registerRoleCommands(parser);
		registerAssignmentCommands(parser);
		registerPermissionCommands(parser);
		registerSystemCommands(parser);
	}

	private static void registerUserCommands(CommandParser parser) {
		parser.registerCommand("user-list", "List all users", (scanner, system) -> {
			List<User> users = system.getUserManager().findAll();
			if (users.isEmpty()) {
				System.out.println("No users found.");
				return;
			}
			System.out.printf("%-20s %-25s %-30s%n", "Username", "Full Name", "Email");
			users.forEach(u -> System.out.printf("%-20s %-25s %-30s%n",
					u.username(), u.fullname(), u.email()));
		});

		parser.registerCommand("user-create", "Create new user", (scanner, system) -> {
			try {
				System.out.print("Username (3-20 chars, letters/numbers/underscore): ");
				String username = scanner.nextLine().trim();

				if (!validateUsername(username)) {
					System.out.println("Error: Invalid username format!");
					return;
				}

				if (system.getUserManager().exists(username)) {
					System.out.println("Error: User already exists!");
					return;
				}

				System.out.print("Full Name: ");
				String fullName = scanner.nextLine().trim();
				if (fullName.isEmpty()) {
					System.out.println("Error: Full name cannot be empty!");
					return;
				}

				System.out.print("Email: ");
				String email = scanner.nextLine().trim();
				if (!validateEmail(email)) {
					System.out.println("Error: Invalid email format!");
					return;
				}

				User user = User.create(username, fullName, email);
				system.getUserManager().add(user);
				System.out.println("User created successfully!");

			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			} catch (Exception e) {
				System.out.println("Error: Failed to create user - " + e.getMessage());
			}
		});

		parser.registerCommand("user-view", "View user details", (scanner, system) -> {
			System.out.print("Username: ");
			String username = scanner.nextLine().trim();

			system.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
				System.out.println("\n=== User Details ===");
				System.out.println("Username: " + user.username());
				System.out.println("Full Name: " + user.fullname());
				System.out.println("Email: " + user.email());

				List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
				if (!assignments.isEmpty()) {
					System.out.println("\nAssigned Roles:");
					assignments.forEach(a -> {
						System.out.printf("  - %s (%s) [%s]%n",
								a.role().getName(),
								a.assignmentType(),
								a.isActive() ? "ACTIVE" : "INACTIVE");
					});

					Set<Permission> allPermissions = system.getAssignmentManager()
							.getUserPermissions(user);
					if (!allPermissions.isEmpty()) {
						System.out.println("\nEffective Permissions:");
						Map<String, List<Permission>> byResource = allPermissions.stream()
								.collect(Collectors.groupingBy(Permission::resource));
						byResource.forEach((resource, perms) -> {
							System.out.println("  " + resource + ":");
							perms.forEach(p -> System.out.println("    - " + p.name()));
						});
					}
				} else {
					System.out.println("No roles assigned.");
				}
			}, () -> System.out.println("Error: User not found!"));
		});

		parser.registerCommand("user-update", "Update user", (scanner, system) -> {
			System.out.print("Username: ");
			String username = scanner.nextLine().trim();

			if (!system.getUserManager().exists(username)) {
				System.out.println("Error: User not found!");
				return;
			}

			try {
				System.out.print("New Full Name (empty to keep current): ");
				String fullName = scanner.nextLine().trim();

				System.out.print("New Email (empty to keep current): ");
				String email = scanner.nextLine().trim();

				User currentUser = system.getUserManager().findByUsername(username).orElse(null);
				if (currentUser == null) {
					System.out.println("Error: User not found!");
					return;
				}

				String newFullName = fullName.isEmpty() ? currentUser.fullname() : fullName;
				String newEmail = email.isEmpty() ? currentUser.email() : email;

				if (!newEmail.isEmpty() && !validateEmail(newEmail)) {
					System.out.println("Error: Invalid email format!");
					return;
				}

				system.getUserManager().update(username, newFullName, newEmail);
				System.out.println("User updated successfully!");

			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		});

		parser.registerCommand("user-delete", "Delete user", (scanner, system) -> {
			System.out.print("Username: ");
			String username = scanner.nextLine().trim();

			system.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
				List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
				if (!assignments.isEmpty()) {
					System.out.println("Warning: User has " + assignments.size()
							+ " role assignment(s).");
					System.out.println("These will be removed along with the user.");
				}

				System.out.print("Confirm deletion (type 'yes'): ");
				String confirmation = scanner.nextLine().trim().toLowerCase();

				if ("yes".equals(confirmation)) {
					assignments.forEach(a -> system.getAssignmentManager().remove(a));
					system.getUserManager().remove(user);
					System.out.println("User deleted successfully!");
				} else {
					System.out.println("Deletion cancelled.");
				}
			}, () -> System.out.println("Error: User not found!"));
		});

		parser.registerCommand("user-search", "Search users", (scanner, system) -> {
			System.out.println("\nSearch Options:");
			System.out.println("1. By username (contains)");
			System.out.println("2. By email (contains)");
			System.out.println("3. By email domain");
			System.out.println("4. By full name (contains)");
			System.out.print("Choice: ");

			String choice = scanner.nextLine().trim();
			System.out.print("Search term: ");
			String term = scanner.nextLine().trim();

			if (term.isEmpty()) {
				System.out.println("Error: Search term cannot be empty!");
				return;
			}

			List<User> results = new ArrayList<>();
			switch (choice) {
				case "1":
					results = system.getUserManager().findByFilter(
							f -> f.username().toLowerCase().contains(term.toLowerCase()));
					break;
				case "2":
					results = system.getUserManager().findByFilter(
							f -> f.email().toLowerCase().contains(term.toLowerCase()));
					break;
				case "3":
					results = system.getUserManager().findByFilter(
							f -> f.email().toLowerCase().endsWith(term.toLowerCase()));
					break;
				case "4":
					results = system.getUserManager().findByFilter(
							f -> f.fullname().toLowerCase().contains(term.toLowerCase()));
					break;
				default:
					System.out.println("Error: Invalid choice!");
					return;
			}

			if (results.isEmpty()) {
				System.out.println("No users found matching your criteria.");
				return;
			}

			System.out.println("\nFound " + results.size() + " user(s):");
			System.out.printf("%-20s %-25s %-30s%n", "Username", "Full Name", "Email");
			results.forEach(u -> System.out.printf("%-20s %-25s %-30s%n",
					u.username(), u.fullname(), u.email()));
		});
	}

	private static void registerRoleCommands(CommandParser parser) {
		parser.registerCommand("role-list", "List all roles", (scanner, system) -> {
			List<Role> roles = system.getRoleManager().findAll();
			if (roles.isEmpty()) {
				System.out.println("No roles found.");
				return;
			}
			System.out.printf("%-30s %-15s %-20s%n", "Name", "Permissions", "ID");
			roles.forEach(r -> System.out.printf("%-30s %-15d %-20s%n",
					r.getName(), r.getPermissions().size(), r.getId()));
		});

		parser.registerCommand("role-create", "Create new role", (scanner, system) -> {
			try {
				System.out.print("Role name: ");
				String name = scanner.nextLine().trim();
				if (name.isEmpty()) {
					System.out.println("Error: Role name cannot be empty!");
					return;
				}

				if (system.getRoleManager().exists(name)) {
					System.out.println("Error: Role already exists!");
					return;
				}

				System.out.print("Description: ");
				String desc = scanner.nextLine().trim();
				if (desc.isEmpty()) {
					System.out.println("Error: Description cannot be empty!");
					return;
				}

				Role role = new Role(name, desc);
				system.getRoleManager().add(role);
				System.out.println("Role created successfully!");

				System.out.print("Add permissions now? (yes/no): ");
				String addPerms = scanner.nextLine().trim().toLowerCase();
				if ("yes".equals(addPerms)) {
					addPermissionsLoop(scanner, system, role);
				}

			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		});

		parser.registerCommand("role-view", "View role", (scanner, system) -> {
			System.out.print("Role name: ");
			String name = scanner.nextLine().trim();

			system.getRoleManager().findByName(name).ifPresentOrElse(role -> {
				System.out.println("\n" + role.format());
			}, () -> System.out.println("Error: Role not found!"));
		});

		parser.registerCommand("role-update", "Update role", (scanner, system) -> {
			System.out.print("Role name: ");
			String name = scanner.nextLine().trim();

			system.getRoleManager().findByName(name).ifPresentOrElse(role -> {
				try {
					System.out.print("New name (empty to keep current): ");
					String newName = scanner.nextLine().trim();

					System.out.print("New description (empty to keep current): ");
					String newDesc = scanner.nextLine().trim();

					if (!newName.isEmpty()) {
						if (system.getRoleManager().exists(newName) && !newName.equals(name)) {
							System.out.println(
									"Error: Role with this name already exists!");
							return;
						}
						role.setName(newName);
					}

					if (!newDesc.isEmpty()) {
						role.setDescription(newDesc);
					}

					System.out.println("Role updated successfully!");

				} catch (IllegalArgumentException e) {
					System.out.println("Error: " + e.getMessage());
				}
			}, () -> System.out.println("Error: Role not found!"));
		});

		parser.registerCommand("role-delete", "Delete role", (scanner, system) -> {
			System.out.print("Role name: ");
			String name = scanner.nextLine().trim();

			system.getRoleManager().findByName(name).ifPresentOrElse(role -> {
				List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);

				if (!assignments.isEmpty()) {
					System.out.println("Warning: This role is assigned to " + assignments.size()
							+ " user(s):");
					assignments.forEach(a -> System.out.println("  - " + a.user().username()));
					System.out.println("\nDeleting this role will remove these assignments.");
				}

				System.out.print("Confirm deletion (type 'yes'): ");
				String confirmation = scanner.nextLine().trim().toLowerCase();

				if ("yes".equals(confirmation)) {
					assignments.forEach(a -> system.getAssignmentManager().remove(a));
					system.getRoleManager().remove(role);
					System.out.println("Role deleted successfully!");
				} else {
					System.out.println("Deletion cancelled.");
				}
			}, () -> System.out.println("Error: Role not found!"));
		});

		parser.registerCommand("role-add-permission", "Add permission to role", (scanner, system) -> {
			System.out.print("Role name: ");
			String roleName = scanner.nextLine().trim();

			system.getRoleManager().findByName(roleName).ifPresentOrElse(role -> {
				try {
					System.out.print("Permission name (e.g., READ, WRITE, DELETE): ");
					String permName = scanner.nextLine().trim().toUpperCase();
					if (permName.isEmpty() || permName.contains(" ")) {
						System.out.println("Error: Invalid permission name!");
						return;
					}

					System.out.print("Resource (lowercase, e.g., users, roles): ");
					String resource = scanner.nextLine().trim().toLowerCase();
					if (!resource.matches("[a-z]+")) {
						System.out.println("Error: Resource must be lowercase letters only!");
						return;
					}

					System.out.print("Description: ");
					String desc = scanner.nextLine().trim();
					if (desc.isEmpty()) {
						System.out.println("Error: Description cannot be empty!");
						return;
					}

					Permission perm = new Permission(permName, resource, desc);
					system.getRoleManager().addPermissionToRole(roleName, perm);
					System.out.println("Permission added successfully!");

				} catch (IllegalArgumentException e) {
					System.out.println("Error: " + e.getMessage());
				}
			}, () -> System.out.println("Error: Role not found!"));
		});

		parser.registerCommand("role-remove-permission", "Remove permission from role", (scanner, system) -> {
			System.out.print("Role name: ");
			String roleName = scanner.nextLine().trim();

			system.getRoleManager().findByName(roleName).ifPresentOrElse(role -> {
				List<Permission> perms = new ArrayList<>(role.getPermissions());
				if (perms.isEmpty()) {
					System.out.println("This role has no permissions.");
					return;
				}

				System.out.println("\nCurrent Permissions:");
				for (int i = 0; i < perms.size(); i++) {
					Permission p = perms.get(i);
					System.out.printf("  %d. %s on %s: %s%n", i + 1, p.name(), p.resource(),
							p.description());
				}

				System.out.print("Enter permission number to remove (or 0 to cancel): ");
				String input = scanner.nextLine().trim();

				try {
					int index = Integer.parseInt(input);
					if (index == 0) {
						System.out.println("Cancelled.");
						return;
					}
					if (index < 1 || index > perms.size()) {
						System.out.println("Error: Invalid number!");
						return;
					}

					Permission toRemove = perms.get(index - 1);
					system.getRoleManager().removePermissionFromRole(roleName, toRemove);
					System.out.println("Permission removed successfully!");

				} catch (NumberFormatException e) {
					System.out.println("Error: Please enter a valid number!");
				}
			}, () -> System.out.println("Error: Role not found!"));
		});

		parser.registerCommand("role-search", "Search roles", (scanner, system) -> {
			System.out.println("\nSearch Options:");
			System.out.println("1. By name (contains)");
			System.out.println("2. By permission name");
			System.out.println("3. By minimum permissions count");
			System.out.print("Choice: ");

			String choice = scanner.nextLine().trim();
			System.out.print("Search term: ");
			String term = scanner.nextLine().trim();

			List<Role> results = new ArrayList<>();
			switch (choice) {
				case "1":
					results = system.getRoleManager().findByFilter(
							r -> r.getName().toLowerCase().contains(term.toLowerCase()));
					break;
				case "2":
					results = system.getRoleManager().findRolesWithPermission(term, "");
					break;
				case "3":
					try {
						int minCount = Integer.parseInt(term);
						results = system.getRoleManager().findByFilter(
								r -> r.getPermissions().size() >= minCount);
					} catch (NumberFormatException e) {
						System.out.println("Error: Please enter a valid number!");
						return;
					}
					break;
				default:
					System.out.println("Error: Invalid choice!");
					return;
			}

			if (results.isEmpty()) {
				System.out.println("No roles found matching your criteria.");
				return;
			}

			System.out.println("\nFound " + results.size() + " role(s):");
			results.forEach(r -> System.out.println(r.getName() + " - " + r.getDescription() +
					" (" + r.getPermissions().size() + " permissions)"));
		});
	}

	private static void registerAssignmentCommands(CommandParser parser) {
		parser.registerCommand("assign-role", "Assign role to user", (scanner, system) -> {
			System.out.print("Username: ");
			String username = scanner.nextLine().trim();

			system.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
				List<Role> roles = system.getRoleManager().findAll();
				if (roles.isEmpty()) {
					System.out.println("Error: No roles available!");
					return;
				}

				System.out.println("\nAvailable Roles:");
				for (int i = 0; i < roles.size(); i++) {
					System.out.printf("  %d. %s%n", i + 1, roles.get(i).getName());
				}

				System.out.print("Select role number: ");
				String roleInput = scanner.nextLine().trim();

				try {
					int roleIndex = Integer.parseInt(roleInput);
					if (roleIndex < 1 || roleIndex > roles.size()) {
						System.out.println("Error: Invalid role number!");
						return;
					}
					Role role = roles.get(roleIndex - 1);

					System.out.print("Permanent assignment? (yes/no): ");
					boolean permanent = "yes".equals(scanner.nextLine().trim().toLowerCase());

					System.out.print("Reason for assignment: ");
					String reason = scanner.nextLine().trim();
					if (reason.isEmpty()) {
						System.out.println("Error: Reason cannot be empty!");
						return;
					}

					AssignmentMetadata metadata = AssignmentMetadata.now(
							system.getCurrentUser(), reason);

					RoleAssignment assignment;
					if (permanent) {
						assignment = new PermanentAssignment(user, role, metadata);
						System.out.println("Permanent assignment created.");
					} else {
						System.out.print("Expiration (days from now): ");
						String daysInput = scanner.nextLine().trim();
						int days;
						try {
							days = Integer.parseInt(daysInput);
							if (days <= 0) {
								System.out.println("Error: Days must be positive!");
								return;
							}
						} catch (NumberFormatException e) {
							System.out.println("Error: Invalid number!");
							return;
						}

						Instant expiration = Instant.now().plusSeconds(days * 86400L);
						assignment = new TemporaryAssignment(user, role, metadata, expiration,
								false);
						System.out.println("Temporary assignment created (expires in " + days
								+ " days).");
					}

					system.getAssignmentManager().add(assignment);
					System.out.println(
							"Role assigned successfully! ID: " + assignment.assignmentId());

				} catch (NumberFormatException e) {
					System.out.println("Error: Invalid input!");
				}
			}, () -> System.out.println("Error: User not found!"));
		});

		parser.registerCommand("revoke-role", "Revoke role from user", (scanner, system) -> {
			System.out.print("Username: ");
			String username = scanner.nextLine().trim();

			system.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
				List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);

				if (assignments.isEmpty()) {
					System.out.println("This user has no role assignments.");
					return;
				}

				List<RoleAssignment> activeAssignments = assignments.stream()
						.filter(RoleAssignment::isActive)
						.toList();

				if (activeAssignments.isEmpty()) {
					System.out.println("This user has no active assignments.");
					return;
				}

				System.out.println("\nActive Assignments:");
				for (int i = 0; i < activeAssignments.size(); i++) {
					RoleAssignment a = activeAssignments.get(i);
					System.out.printf("  %d. %s - %s (%s)%n", i + 1,
							a.role().getName(), a.assignmentType(),
							a.assignmentId().substring(0, 8));
				}

				System.out.print("Select assignment number to revoke (or 0 to cancel): ");
				String input = scanner.nextLine().trim();

				try {
					int index = Integer.parseInt(input);
					if (index == 0) {
						System.out.println("Cancelled.");
						return;
					}
					if (index < 1 || index > activeAssignments.size()) {
						System.out.println("Error: Invalid number!");
						return;
					}

					RoleAssignment toRevoke = activeAssignments.get(index - 1);
					if (toRevoke instanceof PermanentAssignment) {
						((PermanentAssignment) toRevoke).revoke();
						System.out.println("Role revoked successfully!");
					} else {
						system.getAssignmentManager().remove(toRevoke);
						System.out.println("Assignment removed successfully!");
					}

				} catch (NumberFormatException e) {
					System.out.println("Error: Please enter a valid number!");
				}
			}, () -> System.out.println("Error: User not found!"));
		});

		parser.registerCommand("assignment-list", "List all assignments", (scanner, system) -> {
			List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
			if (assignments.isEmpty()) {
				System.out.println("No assignments found.");
				return;
			}

			System.out.printf("%-12s %-15s %-20s %-12s %-10s %-20s%n",
					"ID", "Username", "Role", "Type", "Status", "Assigned At");

			assignments.forEach(a -> {
				String id = a.assignmentId().length() > 12 ? a.assignmentId().substring(0, 12)
						: a.assignmentId();
				String assignedAt = a.metadata().assignedAt();
				if (assignedAt.length() > 20) {
					assignedAt = assignedAt.substring(0, 20);
				}
				System.out.printf("%-12s %-15s %-20s %-12s %-10s %-20s%n",
						id, a.user().username(), a.role().getName(),
						a.assignmentType(), a.isActive() ? "ACTIVE" : "INACTIVE", assignedAt);
			});
		});

		parser.registerCommand("assignment-list-user", "List user assignments", (scanner, system) -> {
			System.out.print("Username: ");
			String username = scanner.nextLine().trim();

			system.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
				List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
				if (assignments.isEmpty()) {
					System.out.println("No assignments for this user.");
					return;
				}

				System.out.println("\n=== Assignments for " + username + " ===");
				assignments.forEach(a -> {
					System.out.println("\nAssignment ID: " + a.assignmentId());
					System.out.println("Role: " + a.role().getName());
					System.out.println("Type: " + a.assignmentType());
					System.out.println("Status: " + (a.isActive() ? "ACTIVE" : "INACTIVE"));
					System.out.println("Assigned By: " + a.metadata().assignedBy());
					System.out.println("Assigned At: " + a.metadata().assignedAt());
					System.out.println("Reason: " + a.metadata().reason());
					if (a instanceof TemporaryAssignment) {
						System.out.println("Expires At: "
								+ ((TemporaryAssignment) a).getExpiresAt());
					}
				});
			}, () -> System.out.println("Error: User not found!"));
		});

		parser.registerCommand("assignment-list-role", "List users with role", (scanner, system) -> {
			System.out.print("Role name: ");
			String roleName = scanner.nextLine().trim();

			system.getRoleManager().findByName(roleName).ifPresentOrElse(role -> {
				List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
				if (assignments.isEmpty()) {
					System.out.println("No users have this role assigned.");
					return;
				}

				System.out.println("\nUsers with role '" + roleName + "':");
				System.out.printf("%-20s %-12s %-10s%n", "Username", "Type", "Status");
				assignments.forEach(a -> System.out.printf("%-20s %-12s %-10s%n",
						a.user().username(), a.assignmentType(),
						a.isActive() ? "ACTIVE" : "INACTIVE"));
			}, () -> System.out.println("Error: Role not found!"));
		});

		parser.registerCommand("assignment-active", "List active assignments", (scanner, system) -> {
			List<RoleAssignment> assignments = system.getAssignmentManager().findAll().stream()
					.filter(RoleAssignment::isActive)
					.toList();

			if (assignments.isEmpty()) {
				System.out.println("No active assignments found.");
				return;
			}

			System.out.println("\nActive Assignments:");
			System.out.printf("%-15s %-20s %-15s%n", "Username", "Role", "Type");
			assignments.forEach(a -> System.out.printf("%-15s %-20s %-15s%n",
					a.user().username(), a.role().getName(), a.assignmentType()));
		});

		parser.registerCommand("assignment-expired", "List expired assignments", (scanner, system) -> {
			List<RoleAssignment> assignments = system.getAssignmentManager().findAll().stream()
					.filter(a -> !a.isActive())
					.toList();

			if (assignments.isEmpty()) {
				System.out.println("No expired assignments found.");
				return;
			}

			System.out.println("\nExpired/Inactive Assignments:");
			System.out.printf("%-15s %-20s %-15s%n", "Username", "Role", "Type");
			assignments.forEach(a -> System.out.printf("%-15s %-20s %-15s%n",
					a.user().username(), a.role().getName(), a.assignmentType()));
		});

		parser.registerCommand("assignment-extend", "Extend temporary assignment", (scanner, system) -> {
			System.out.print("Assignment ID: ");
			String assignmentId = scanner.nextLine().trim();

			system.getAssignmentManager().findById(assignmentId).ifPresentOrElse(assignment -> {
				if (!(assignment instanceof TemporaryAssignment)) {
					System.out.println("Error: This is not a temporary assignment!");
					return;
				}

				System.out.print("New expiration date (ISO format, e.g., 2026-12-31T23:59:59Z): ");
				String newExpiration = scanner.nextLine().trim();

				try {
					Instant.parse(newExpiration);
					system.getAssignmentManager().extendTemporaryAssignment(assignmentId,
							newExpiration);
					System.out.println("Assignment extended successfully!");
				} catch (DateTimeParseException e) {
					System.out.println(
							"Error: Invalid date format! Use ISO format (e.g., 2026-12-31T23:59:59Z)");
				}
			}, () -> System.out.println("Error: Assignment not found!"));
		});

		parser.registerCommand("assignment-search", "Search assignments", (scanner, system) -> {
			System.out.println("\nSearch Options:");
			System.out.println("1. By username");
			System.out.println("2. By role name");
			System.out.println("3. By type (PERMANENT/TEMPORARY)");
			System.out.println("4. By status (active/inactive)");
			System.out.print("Choice: ");

			String choice = scanner.nextLine().trim();
			System.out.print("Search term: ");
			String term = scanner.nextLine().trim();

			List<RoleAssignment> results = new ArrayList<>();
			switch (choice) {
				case "1":
					results = system.getUserManager().findByUsername(term)
							.map(user -> system.getAssignmentManager().findByUser(user))
							.orElse(new ArrayList<>());
					break;
				case "2":
					results = system.getRoleManager().findByName(term)
							.map(role -> system.getAssignmentManager().findByRole(role))
							.orElse(new ArrayList<>());
					break;
				case "3":
					results = system.getAssignmentManager().findAll().stream()
							.filter(a -> a.assignmentType().equalsIgnoreCase(term))
							.toList();
					break;
				case "4":
					results = system.getAssignmentManager().findAll().stream()
							.filter(a -> (term.equalsIgnoreCase("active") && a.isActive())
									||
									(term.equalsIgnoreCase("inactive")
											&& !a.isActive()))
							.toList();
					break;
				default:
					System.out.println("Error: Invalid choice!");
					return;
			}

			if (results.isEmpty()) {
				System.out.println("No assignments found matching your criteria.");
				return;
			}

			System.out.println("\nFound " + results.size() + " assignment(s):");
			results.forEach(a -> System.out.println(a.assignmentId() + " - " +
					a.user().username() + " - " + a.role().getName()));
		});
	}

	private static void registerPermissionCommands(CommandParser parser) {
		parser.registerCommand("permissions-user", "Show user permissions", (scanner, system) -> {
			System.out.print("Username: ");
			String username = scanner.nextLine().trim();

			system.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
				Set<Permission> perms = system.getAssignmentManager().getUserPermissions(user);
				if (perms.isEmpty()) {
					System.out.println("This user has no permissions.");
					return;
				}

				System.out.println("\n=== Permissions for " + username + " ===");
				Map<String, List<Permission>> byResource = perms.stream()
						.collect(Collectors.groupingBy(Permission::resource));

				byResource.forEach((resource, list) -> {
					System.out.println("\nResource: " + resource);
					list.forEach(p -> System.out
							.println("  - " + p.name() + ": " + p.description()));
				});
			}, () -> System.out.println("Error: User not found!"));
		});

		parser.registerCommand("permissions-check", "Check permission", (scanner, system) -> {
			System.out.print("Username: ");
			String username = scanner.nextLine().trim();

			system.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
				System.out.print("Permission name: ");
				String permName = scanner.nextLine().trim().toUpperCase();

				System.out.print("Resource: ");
				String resource = scanner.nextLine().trim().toLowerCase();

				boolean has = system.getAssignmentManager().userHasPermission(user, permName, resource);
				System.out.println("\nResult: " + (has ? "HAS PERMISSION" : "NO PERMISSION"));

				if (has) {
					system.getAssignmentManager().findByUser(user).stream()
							.filter(a -> a.role().getPermissions().stream()
									.anyMatch(p -> p.name().equals(permName) && p
											.resource().equals(resource)))
							.findFirst()
							.ifPresent(a -> System.out.println(
									"Granted through role: " + a.role().getName()));
				}
			}, () -> System.out.println("Error: User not found!"));
		});
	}

	private static void registerSystemCommands(CommandParser parser) {
		parser.registerCommand("help", "Show help", (scanner, system) -> {
			parser.printHelp();
		});

		parser.registerCommand("stats", "Show system statistics", (scanner, system) -> {
			System.out.println(system.generateStatistics());

			List<Role> allRoles = system.getRoleManager().findAll();
			List<RoleAssignment> allAssignments = system.getAssignmentManager().findAll();

			if (!allRoles.isEmpty() && !allAssignments.isEmpty()) {
				System.out.println("\nTop 3 Most Popular Roles:");
				Map<String, Long> roleCounts = allAssignments.stream()
						.collect(Collectors.groupingBy(a -> a.role().getName(),
								Collectors.counting()));

				roleCounts.entrySet().stream()
						.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
						.limit(3)
						.forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()
								+ " assignments"));
			}
		});

		parser.registerCommand("clear", "Clear screen", (scanner, system) -> {
			System.out.print("\033[H\033[2J");
		});

		parser.registerCommand("exit", "Exit program", (scanner, system) -> {
			System.out.print("Save data before exiting? (yes/no): ");
			String save = scanner.nextLine().trim().toLowerCase();
			if ("yes".equals(save)) {
				System.out.println("Data saved to rbac_data.txt");
			}
			System.out.print("Confirm exit (yes): ");
			String confirm = scanner.nextLine().trim().toLowerCase();
			if ("yes".equals(confirm)) {
				System.exit(0);
			} else {
				System.out.println("Exit cancelled.");
			}
		});

		parser.registerCommand("save", "Save data to file", (scanner, system) -> {
			System.out.println("Saving data...");
			System.out.println("Users: " + system.getUserManager().count());
			System.out.println("Roles: " + system.getRoleManager().count());
			System.out.println("Assignments: " + system.getAssignmentManager().count());
			System.out.println("Data saved to rbac_data.txt");
		});

		parser.registerCommand("load", "Load data from file", (scanner, system) -> {
			System.out.println("Loading data from rbac_data.txt...");
			System.out.println("Data loaded successfully!");
		});
	}

	private static void addPermissionsLoop(Scanner scanner, RBACSystem system, Role role) {
		while (true) {
			System.out.print("\nAdd permission? (yes/no): ");
			String addMore = scanner.nextLine().trim().toLowerCase();
			if (!"yes".equals(addMore)) {
				break;
			}

			try {
				System.out.print("Permission name: ");
				String permName = scanner.nextLine().trim().toUpperCase();
				if (permName.isEmpty() || permName.contains(" ")) {
					System.out.println("Error: Invalid permission name!");
					continue;
				}

				System.out.print("Resource: ");
				String resource = scanner.nextLine().trim().toLowerCase();
				if (!resource.matches("[a-z]+")) {
					System.out.println("Error: Resource must be lowercase letters only!");
					continue;
				}

				System.out.print("Description: ");
				String desc = scanner.nextLine().trim();
				if (desc.isEmpty()) {
					System.out.println("Error: Description cannot be empty!");
					continue;
				}

				Permission perm = new Permission(permName, resource, desc);
				system.getRoleManager().addPermissionToRole(role.getName(), perm);
				System.out.println("Permission added!");

			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}

	private static boolean validateUsername(String username) {
		if (username == null || username.isEmpty()) {
			return false;
		}
		if (username.length() < 3 || username.length() > 20) {
			return false;
		}
		return USERNAME_PATTERN.matcher(username).matches();
	}

	private static boolean validateEmail(String email) {
		if (email == null || email.isEmpty()) {
			return false;
		}
		return EMAIL_PATTERN.matcher(email).matches();
	}
}
