package org.utils;

import org.repositories.UserManager;
import org.repositories.RoleManager;
import org.repositories.AssignmentManager;
import org.User;
import org.Role;
import org.RoleAssignment;
import org.Permission;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

	public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
		StringBuilder sb = new StringBuilder();
		sb.append(FormatUtils.formatHeader("USER REPORT"));
		sb.append("Generated: ").append(Instant.now()).append("\n\n");

		List<User> users = userManager.findAll();

		if (users.isEmpty()) {
			sb.append("No users found.\n");
			return sb.toString();
		}

		String[] headers = { "Username", "Full Name", "Email", "Roles", "Status" };
		List<String[]> rows = new ArrayList<>();

		for (User user : users) {
			List<RoleAssignment> assignments = assignmentManager.findByUser(user);
			List<String> roleNames = assignments.stream()
					.filter(RoleAssignment::isActive)
					.map(a -> a.role().getName())
					.collect(Collectors.toList());

			String roles = roleNames.isEmpty() ? "None" : String.join(", ", roleNames);
			String status = assignments.stream().anyMatch(RoleAssignment::isActive) ? "Active" : "Inactive";

			rows.add(new String[] {
					user.username(),
					truncate(user.fullname(), 25),
					truncate(user.email(), 30),
					truncate(roles, 30),
					status
			});
		}

		sb.append(FormatUtils.formatTable(headers, rows));
		sb.append("\n\nTotal Users: ").append(users.size());

		return sb.toString();
	}

	public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
		StringBuilder sb = new StringBuilder();
		sb.append(FormatUtils.formatHeader("ROLE REPORT"));
		sb.append("Generated: ").append(Instant.now()).append("\n\n");

		List<Role> roles = roleManager.findAll();

		if (roles.isEmpty()) {
			sb.append("No roles found.\n");
			return sb.toString();
		}

		String[] headers = { "Role Name", "Description", "Permissions", "Users", "Active Assignments" };
		List<String[]> rows = new ArrayList<>();

		for (Role role : roles) {
			List<RoleAssignment> assignments = assignmentManager.findByRole(role);
			long activeCount = assignments.stream().filter(RoleAssignment::isActive).count();

			rows.add(new String[] {
					role.getName(),
					truncate(role.getDescription(), 25),
					String.valueOf(role.getPermissions().size()),
					String.valueOf(assignments.size()),
					String.valueOf(activeCount)
			});
		}

		sb.append(FormatUtils.formatTable(headers, rows));
		sb.append("\n\nTotal Roles: ").append(roles.size());

		return sb.toString();
	}

	public String generateUserReportParallel(UserManager userManager, AssignmentManager assignmentManager) {
		StringBuilder sb = new StringBuilder();
		sb.append(FormatUtils.formatHeader("USER REPORT (Parallel)"));
		sb.append("Generated: ").append(Instant.now()).append("\n\n");

		List<User> users = userManager.findAll();
		if (users.isEmpty()) {
			sb.append("No users found.\n");
			return sb.toString();
		}

		String[] headers = { "Username", "Full Name", "Email", "Roles", "Status" };

		List<String[]> rows = users.parallelStream().map(user -> {
			List<RoleAssignment> assignments = assignmentManager.findByUser(user);
			List<String> roleNames = assignments.stream()
					.filter(RoleAssignment::isActive)
					.map(a -> a.role().getName())
					.collect(Collectors.toList());

			String roles = roleNames.isEmpty() ? "None" : String.join(", ", roleNames);
			String status = assignments.stream().anyMatch(RoleAssignment::isActive) ? "Active" : "Inactive";

			return new String[] {
					user.username(),
					truncate(user.fullname(), 25),
					truncate(user.email(), 30),
					truncate(roles, 30),
					status
			};
		}).collect(Collectors.toList());

		sb.append(FormatUtils.formatTable(headers, rows));
		sb.append("\n\nTotal Users: ").append(users.size());
		return sb.toString();
	}

	public String generatePermissionMatrixParallel(UserManager userManager, AssignmentManager assignmentManager) {
		StringBuilder sb = new StringBuilder();
		sb.append(FormatUtils.formatHeader("PERMISSION MATRIX (Parallel)"));
		sb.append("Generated: ").append(Instant.now()).append("\n\n");

		List<User> users = userManager.findAll();
		if (users.isEmpty()) {
			sb.append("No users found.\n");
			return sb.toString();
		}

		Set<String> resources = users.parallelStream()
				.map(assignmentManager::getUserPermissions)
				.flatMap(Set::stream)
				.map(Permission::resource)
				.collect(Collectors.toCollection(TreeSet::new));

		if (resources.isEmpty()) {
			sb.append("No permissions configured.\n");
			return sb.toString();
		}

		List<String> headerList = new ArrayList<>();
		headerList.add("Username");
		headerList.addAll(resources);
		String[] headers = headerList.toArray(new String[0]);

		List<String[]> rows = users.parallelStream().map(user -> {
			Set<Permission> userPerms = assignmentManager.getUserPermissions(user);
			Set<String> userResources = userPerms.stream()
					.map(Permission::resource)
					.collect(Collectors.toSet());

			List<String> row = new ArrayList<>();
			row.add(user.username());
			for (String resource : resources) {
				row.add(userResources.contains(resource) ? "✓" : "✗");
			}
			return row.toArray(new String[0]);
		}).collect(Collectors.toList());

		sb.append(FormatUtils.formatTable(headers, rows));
		sb.append("\n\nLegend: ✓ = Has Permission, ✗ = No Permission");
		return sb.toString();
	}

	public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
		StringBuilder sb = new StringBuilder();
		sb.append(FormatUtils.formatHeader("PERMISSION MATRIX"));
		sb.append("Generated: ").append(Instant.now()).append("\n\n");

		List<User> users = userManager.findAll();

		if (users.isEmpty()) {
			sb.append("No users found.\n");
			return sb.toString();
		}

		Set<String> resources = new TreeSet<>();
		for (User user : users) {
			Set<Permission> perms = assignmentManager.getUserPermissions(user);
			perms.forEach(p -> resources.add(p.resource()));
		}

		if (resources.isEmpty()) {
			sb.append("No permissions configured.\n");
			return sb.toString();
		}

		List<String> headerList = new ArrayList<>();
		headerList.add("Username");
		headerList.addAll(resources);
		String[] headers = headerList.toArray(new String[0]);

		List<String[]> rows = new ArrayList<>();
		for (User user : users) {
			Set<Permission> userPerms = assignmentManager.getUserPermissions(user);
			Set<String> userResources = userPerms.stream()
					.map(Permission::resource)
					.collect(Collectors.toSet());

			List<String> row = new ArrayList<>();
			row.add(user.username());

			for (String resource : resources) {
				row.add(userResources.contains(resource) ? "✓" : "✗");
			}

			rows.add(row.toArray(new String[0]));
		}

		sb.append(FormatUtils.formatTable(headers, rows));
		sb.append("\n\nLegend: ✓ = Has Permission, ✗ = No Permission");

		return sb.toString();
	}

	public void exportToFile(String report, String filename) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
			writer.println(report);
			System.out.println("Report saved to: " + filename);
		} catch (IOException e) {
			System.err.println("Error saving report: " + e.getMessage());
		}
	}

	public String generateStatisticsReport(UserManager userManager,
			RoleManager roleManager,
			AssignmentManager assignmentManager) {
		StringBuilder sb = new StringBuilder();
		sb.append(FormatUtils.formatHeader("SYSTEM STATISTICS"));
		sb.append("Generated: ").append(Instant.now()).append("\n\n");

		int userCount = userManager.count();
		int roleCount = roleManager.count();
		int assignmentCount = assignmentManager.count();
		int activeCount = (int) assignmentManager.findAll().stream()
				.filter(RoleAssignment::isActive).count();

		sb.append(FormatUtils.formatBox("SUMMARY"));
		sb.append(String.format("Users: %d\n", userCount));
		sb.append(String.format("Roles: %d\n", roleCount));
		sb.append(String.format("Assignments: %d (Active: %d, Inactive: %d)\n",
				assignmentCount, activeCount, assignmentCount - activeCount));

		if (userCount > 0) {
			double avgRoles = (double) assignmentCount / userCount;
			sb.append(String.format("Average roles per user: %.2f\n", avgRoles));
		}

		List<RoleAssignment> allAssignments = assignmentManager.findAll();
		if (!allAssignments.isEmpty()) {
			sb.append("\n").append(FormatUtils.formatBox("TOP ROLES"));
			Map<String, Long> roleCounts = allAssignments.stream()
					.collect(Collectors.groupingBy(a -> a.role().getName(), Collectors.counting()));

			roleCounts.entrySet().stream()
					.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
					.limit(5)
					.forEach(e -> sb.append(String.format("  %s: %d assignments\n",
							e.getKey(), e.getValue())));
		}

		return sb.toString();
	}

	private String truncate(String text, int maxLength) {
		if (text == null || text.length() <= maxLength) {
			return text != null ? text : "";
		}
		return text.substring(0, maxLength - 3) + "...";
	}
}
