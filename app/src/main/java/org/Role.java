package org;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Role {

	String id;
	String name;
	String description;
	HashSet<Permission> permissions;

	public Role(String name, String description) {
		id = String.format("role_%s", UUID.randomUUID());
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name shouldn't be empty");
		}
		this.name = name;

		if (description == null || description.isEmpty()) {
			throw new IllegalArgumentException("Description shouldn't be empty");
		}
		this.description = description;
		this.permissions = new HashSet<Permission>();
	}

	public String format() {
		String formatOut = String.format("Role: %s [ID: %s]\nDescription: %s\nPermissions (%d):\n", name, id, description,
				permissions.size());

		for (Permission permission : permissions) {
			formatOut += String.format("\t- %s\n", permission.format());
		}
		return formatOut;
	}

	public void addPermission(Permission permission) {
		permissions.add(permission);
	}

	public void removePermission(Permission permission) {
		permissions.remove(permission);
	}

	public boolean hasPermission(Permission permission) {
		return permissions.contains(permission);
	}

	public boolean hasPermission(String permissionName, String resource) {
		return permissions.contains(new Permission(permissionName, resource, "Temp permission"));
	}

	public Set<Permission> getPermissions() {
		return Set.copyOf(permissions);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name shouldn't be empty");
		}
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (description == null || description.isEmpty()) {
			throw new IllegalArgumentException("Name shouldn't be empty");
		}
		this.description = description;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == this.getClass() && obj.hashCode() == this.hashCode();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return format();
	}
}
