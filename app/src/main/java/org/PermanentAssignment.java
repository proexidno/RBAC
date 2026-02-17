package org;

public class PermanentAssignment extends AbstractRoleAssignment {

	boolean revoked;

	public PermanentAssignment(User user, Role role, AssignmentMetadata assignmentMetadata) {
		super(user, role, assignmentMetadata);
		revoked = false;
	}

	public void revoke() {
		revoked = true;
	}

	public boolean isRevoked() {
		return revoked;
	}

	@Override
	public boolean isActive() {
		return !revoked;
	}

	@Override
	public String assignmentType() {
		return "PERMANENT";
	}
}
