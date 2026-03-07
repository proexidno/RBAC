package org;

import java.time.Duration;
import java.time.Instant;

public class TemporaryAssignment extends AbstractRoleAssignment {
	String expiresAt;
	boolean autoRenew;

	public TemporaryAssignment(User user, Role role, AssignmentMetadata assignmentMetadata, Instant futureDate,
			boolean autoRenew) {
		super(user, role, assignmentMetadata);
		this.expiresAt = futureDate.toString();
		this.autoRenew = autoRenew;
	}

	public void extend(String newExpirationDate) {
		if (Instant.parse(newExpirationDate).isAfter(Instant.parse(expiresAt))) {
			expiresAt = newExpirationDate;
		}
	}

	public String getTimeRemaining() {
		Duration diff = Duration.between(Instant.now(), Instant.parse(expiresAt));
		return diff.toString();
	}

	@Override
	public String summary() {
		return String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s\nExpires At: %s",
				assignmentType(), role.getName(), user.username(), assignmentMetadata.assignedBy(),
				assignmentMetadata.assignedAt(), assignmentMetadata.reason(),
				isActive() ? "ACTIVE" : "NOT ACTIVE", expiresAt);
	}

	public boolean isExpired() {
		return Instant.now().isAfter(Instant.parse(expiresAt));
	}

	public String getExpiresAt() {
		return expiresAt;
	};

	@Override
	public boolean isActive() {
		return !isExpired();
	}

	@Override
	public String assignmentType() {
		return "TEMPORARY";
	}
}
