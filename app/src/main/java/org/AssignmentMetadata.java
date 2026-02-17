package org;

import java.time.Instant;

public record AssignmentMetadata(
		String assignedBy,
		String assignedAt,
		String reason) {

	public AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
		if (assignedBy == null || assignedBy.isEmpty()) {
			throw new IllegalArgumentException("assignedBy shouldn't be empty");
		}
		this.assignedBy = assignedBy;
		this.assignedAt = assignedAt;
		this.reason = reason;
	}

	public static AssignmentMetadata now(String assignedBy, String reason) {
		return new AssignmentMetadata(assignedBy, Instant.now().toString(), reason);
	}

	public String format() {
		return String.format("[%s] Assigned by: %s because %s", this.assignedAt, this.assignedBy, this.reason);
	}
}
