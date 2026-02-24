package org;

@FunctionalInterface
public interface AssignmentFilter {
	boolean test(RoleAssignment roleAssignment);

	default AssignmentFilter and(AssignmentFilter other) {
		return roleAssignment -> this.test(roleAssignment) && other.test(roleAssignment);
	}

	default AssignmentFilter or(AssignmentFilter other) {
		return roleAssignment -> this.test(roleAssignment) || other.test(roleAssignment);
	}

}
