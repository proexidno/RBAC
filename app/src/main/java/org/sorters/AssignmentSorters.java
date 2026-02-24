package org.sorters;

import java.time.Instant;
import java.util.Comparator;

import org.AssignmentMetadata;
import org.RoleAssignment;

public class AssignmentSorters {
	static Comparator<RoleAssignment> byUsername() {
		return Comparator.comparing(
				RoleAssignment::user,
				Comparator.nullsLast(
						UserSorters.byUsername()));
	}

	static Comparator<RoleAssignment> byRoleName() {
		return Comparator.comparing(
				RoleAssignment::role,
				Comparator.nullsLast(
						RoleSorters.byName()));
	}

	static Comparator<RoleAssignment> byAssignmentDate() {
		return Comparator.<RoleAssignment, Instant>comparing(
				roleAssignment -> {
					AssignmentMetadata metadata = roleAssignment.metadata();
					String assignedAtStr = metadata.assignedAt();
					return Instant.parse(assignedAtStr);
				},
				Comparator.nullsLast(Comparator.naturalOrder()));
	}
}
