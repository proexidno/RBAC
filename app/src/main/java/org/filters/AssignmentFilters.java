package org.filters;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.Role;
import org.TemporaryAssignment;
import org.User;

public class AssignmentFilters {
	public static AssignmentFilter byUser(User user) {
		return roleAssignment -> user != null && user.equals(roleAssignment.user());
	}

	public static AssignmentFilter byUsername(String username) {
		return roleAssignment -> {
			if (username == null) {
				return false;
			}
			return UserFilters.byUsername(username).test(roleAssignment.user());
		};
	}

	public static AssignmentFilter byRole(Role role) {
		return roleAssignment -> role != null && role.equals(roleAssignment.role());
	}

	public static AssignmentFilter byRoleName(String roleName) {
		return roleAssignment -> {
			if (roleName == null) {
				return false;
			}
			return RoleFilters.byName(roleName).test(roleAssignment.role());
		};
	}

	public static AssignmentFilter activeOnly() {
		return roleAssignment -> roleAssignment.isActive();
	}

	public static AssignmentFilter inactiveOnly() {
		return roleAssignment -> !roleAssignment.isActive();
	}

	public static AssignmentFilter byType(String type) {
		return roleAssignment -> type != null && type.equals(roleAssignment.assignmentType());
	}

	public static AssignmentFilter assignedBy(String username) {
		return roleAssignment -> username != null && username.equals(roleAssignment.metadata().assignedBy());
	}

	public static AssignmentFilter assignedAfter(String date) {
		return roleAssignment -> {
			try {
				return Instant.parse(roleAssignment.metadata().assignedAt()).isAfter(Instant.parse(date));
			} catch (DateTimeParseException e) {
				return false;
			}
		};
	}

	public static AssignmentFilter expiringBefore(Instant time) {
		return roleAssignment -> {
			if (!(roleAssignment instanceof TemporaryAssignment)) {
				return false;
			}
			TemporaryAssignment temporaryAssignment = (TemporaryAssignment) roleAssignment;
			return Instant.parse(temporaryAssignment.getExpiresAt()).isBefore(time);
		};
	}

	public static AssignmentFilter expiringBefore(String date) {
		return roleAssignment -> {
			if (!(roleAssignment instanceof TemporaryAssignment)) {
				return false;
			}
			TemporaryAssignment temporaryAssignment = (TemporaryAssignment) roleAssignment;
			return Instant.parse(temporaryAssignment.getExpiresAt()).isBefore(Instant.parse(date));
		};
	}
}
