package org;

import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
	String assignmentId;
	User user;
	Role role;
	AssignmentMetadata assignmentMetadata;

	public AbstractRoleAssignment(User user, Role role, AssignmentMetadata assignmentMetadata) {
		this.assignmentId = UUID.randomUUID().toString();
		this.user = user;
		this.role = role;
		this.assignmentMetadata = assignmentMetadata;
	}

	public abstract boolean isActive();

	public String summary() {
		return String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s", assignmentType(), role.getName(),
				user.username(), assignmentMetadata.assignedBy(), assignmentMetadata.assignedAt(), assignmentMetadata.reason(),
				isActive() ? "ACTIVE" : "NOT ACTIVE");
	}

	public abstract String assignmentType();

	public String getAssignmentId() {
		return assignmentId;
	}

	@Override
	public AssignmentMetadata metadata() {
		return assignmentMetadata;
	}

	@Override
	public String assignmentId() {
		return assignmentId;
	}

	@Override
	public Role role() {
		return role;
	}

	@Override
	public User user() {
		return user;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == this.getClass() && obj.hashCode() == this.hashCode();
	}

	@Override
	public int hashCode() {
		return assignmentId.hashCode();
	}
}
