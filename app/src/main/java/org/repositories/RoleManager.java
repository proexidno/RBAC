package org.repositories;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.Permission;
import org.Role;
import org.filters.RoleFilter;
import org.filters.RoleFilters;

public class RoleManager implements Repository<Role> {
	ConcurrentHashMap<String, Role> rolesById = new ConcurrentHashMap<>();
	ConcurrentHashMap<String, Role> rolesByName = new ConcurrentHashMap<>();

	public Optional<Role> findByName(String name) {
		return Optional.ofNullable(rolesByName.get(name));
	}

	public List<Role> findByFilter(RoleFilter filter) {
		return rolesById.values().stream()
				.filter(filter::test)
				.collect(Collectors.toList());
	}

	public List<Role> findByFilterParrallel(RoleFilter filter) {
		return rolesById.values().parallelStream()
				.filter(filter::test)
				.collect(Collectors.toList());
	}

	public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
		return rolesById.values().stream()
				.filter(filter::test)
				.sorted(sorter)
				.collect(Collectors.toList());
	}

	public boolean exists(String name) {
		return rolesByName.containsKey(name);
	}

	public void addPermissionToRole(String roleName, Permission permission) {
		Role changeRole = findByName(roleName).orElse(null);
		if (changeRole == null)
			return;
		changeRole.addPermission(permission);
	}

	public void removePermissionFromRole(String roleName, Permission permission) {
		Role changeRole = findByName(roleName).orElse(null);
		if (changeRole == null)
			return;
		changeRole.removePermission(permission);
	}

	public List<Role> findRolesWithPermission(String permissionName, String resource) {
		return rolesById.values().stream()
				.filter(RoleFilters.hasPermission(permissionName, resource)::test)
				.collect(Collectors.toList());
	}

	@Override
	public void add(Role item) {
		if (rolesByName.containsKey(item.getName())) {
			remove(rolesByName.get(item.getName()));
		}
		rolesById.put(item.getId(), item);
		rolesByName.put(item.getName(), item);
	}

	@Override
	public boolean remove(Role item) {
		return rolesById.remove(item.getId(), item)
				&& rolesByName.remove(item.getName(), item);
	}

	@Override
	public Optional<Role> findById(String id) {
		return Optional.ofNullable(rolesById.get(id));
	}

	@Override
	public List<Role> findAll() {
		return List.copyOf(rolesById.values());
	}

	@Override
	public int count() {
		return rolesById.size();
	}

	@Override
	public void clear() {
		rolesById.clear();
	}
}
