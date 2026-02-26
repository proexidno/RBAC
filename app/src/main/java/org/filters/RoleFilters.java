package org.filters;

import org.Permission;

public class RoleFilters {
	public static RoleFilter byName(String name) {
		return role -> name != null && name.equals(role.getName());
	}

	public static RoleFilter byNameContains(String substring) {
		return role -> substring != null && role.getName().contains(substring);
	}

	public static RoleFilter hasPermission(Permission permission) {
		return role -> permission != null && role.hasPermission(permission);
	}

	public static RoleFilter hasPermission(String permissionName, String resource) {
		return role -> permissionName != null && resource != null && role.hasPermission(permissionName, resource);
	}

	public static RoleFilter hasAtLeastNPermissions(int n) {
		return role -> n <= role.getPermissionsLength();
	}
}
