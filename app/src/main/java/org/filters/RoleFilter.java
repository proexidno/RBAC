package org.filters;

import org.Role;

@FunctionalInterface
public interface RoleFilter {
	public boolean test(Role role);

	public default RoleFilter and(RoleFilter other) {
		return role -> this.test(role) && other.test(role);
	}

	public default RoleFilter or(RoleFilter other) {
		return role -> this.test(role) || other.test(role);
	}

}
