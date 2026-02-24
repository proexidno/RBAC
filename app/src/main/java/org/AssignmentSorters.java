package org;

import java.time.Instant;
import java.util.Comparator;

public class AssignmentSorters {
	Comparator<RoleAssignment> byUsername() {
		return Comparator.comparing(
				RoleAssignment::user,
				Comparator.nullsLast(
						Comparator.comparing(
								User::username,
								Comparator.nullsLast(Comparator.naturalOrder()))));
	}

	Comparator<RoleAssignment> byRoleName() {
		return Comparator.comparing(
				RoleAssignment::role,
				Comparator.nullsLast(
						Comparator.comparing(
								Role::getName,
								Comparator.nullsLast(Comparator.naturalOrder()))));
	}

	Comparator<RoleAssignment> byAssignmentDate() {
		return Comparator.<RoleAssignment, Instant>comparing(
				roleAssignment -> {
					AssignmentMetadata metadata = roleAssignment.metadata();
					String assignedAtStr = metadata.assignedAt();
					return Instant.parse(assignedAtStr);
				},
				Comparator.nullsLast(Comparator.naturalOrder()));
	}
}
