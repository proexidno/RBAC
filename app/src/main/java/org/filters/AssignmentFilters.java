package org.filters;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.Role;
import org.TemporaryAssignment;
import org.User;

public class AssignmentFilters {
	static AssignmentFilter byUser(User user) {
		return roleAssignment -> user != null && user.equals(roleAssignment.user());
	}

	static AssignmentFilter byUsername(String username) {
		return roleAssignment -> {
			if (username == null) {
				return false;
			}
			return UserFilters.byUsername(username).test(roleAssignment.user());
		};
	}

	static AssignmentFilter byRole(Role role) {
		return roleAssignment -> role != null && role.equals(roleAssignment.role());
	}

	static AssignmentFilter byRoleName(String roleName) {
		return roleAssignment -> {
			if (roleName == null) {
				return false;
			}
			return RoleFilters.byName(roleName).test(roleAssignment.role());
		};
	}

	static AssignmentFilter activeOnly() {
		return roleAssignment -> roleAssignment.isActive();
	}

	static AssignmentFilter inactiveOnly() {
		return roleAssignment -> !roleAssignment.isActive();
	}

	static AssignmentFilter byType(String type) {
		return roleAssignment -> type != null && type.equals(roleAssignment.assignmentType());
	}

	static AssignmentFilter assignedBy(String username) {
		return roleAssignment -> username != null && username.equals(roleAssignment.metadata().assignedBy());
	}

	static AssignmentFilter assignedAfter(String date) {
		return roleAssignment -> {
			try {
				return Instant.parse(roleAssignment.metadata().assignedAt()).isAfter(Instant.parse(date));
			} catch (DateTimeParseException e) {
				return false;
			}
		};
	}

	static AssignmentFilter expiringBefore(String date) {
		return roleAssignment -> {
			if (!(roleAssignment instanceof TemporaryAssignment)) {
				return false;
			}
			TemporaryAssignment temporaryAssignment = (TemporaryAssignment) roleAssignment;
			return Instant.parse(temporaryAssignment.getExpiresAt()).isBefore(Instant.parse(date));
		};
	}
}
