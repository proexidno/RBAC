package org.sorters;

import java.util.Comparator;

import org.Role;

public class RoleSorters {
	static Comparator<Role> byName() {
		return Comparator.comparing(
				Role::getName,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}

	static Comparator<Role> byPermissionCount() {
		return Comparator.comparing(
				Role::getPermissionsLength,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}
}
