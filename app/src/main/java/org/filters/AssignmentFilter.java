package org.filters;

import org.RoleAssignment;

@FunctionalInterface
public interface AssignmentFilter {
	public boolean test(RoleAssignment roleAssignment);

	public default AssignmentFilter and(AssignmentFilter other) {
		return roleAssignment -> this.test(roleAssignment) && other.test(roleAssignment);
	}

	public default AssignmentFilter or(AssignmentFilter other) {
		return roleAssignment -> this.test(roleAssignment) || other.test(roleAssignment);
	}

}
