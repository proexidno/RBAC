package org.filters;

import org.Permission;

public class RoleFilters {
	static RoleFilter byName(String name) {
		return role -> name != null && name.equals(role.getName());
	}

	static RoleFilter byNameContains(String substring) {
		return role -> substring != null && role.getName().contains(substring);
	}

	static RoleFilter hasPermission(Permission permission) {
		return role -> permission != null && role.hasPermission(permission);
	}

	static RoleFilter hasPermission(String permissionName, String resource) {
		return role -> permissionName != null && resource != null && role.hasPermission(permissionName, resource);
	}

	static RoleFilter hasAtLeastNPermissions(int n) {
		return role -> n <= role.getPermissionsLength();
	}
}
