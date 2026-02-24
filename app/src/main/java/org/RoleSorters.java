package org;

import java.util.Comparator;

public class RoleSorters {
	Comparator<Role> byName() {
		return Comparator.comparing(
				Role::getName,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}

	Comparator<Role> byPermissionCount() {
		return Comparator.comparing(
				Role::getPermissionsLength,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}
}
