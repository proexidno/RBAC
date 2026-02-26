package org.repositories;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.Permission;
import org.Role;
import org.RoleAssignment;
import org.TemporaryAssignment;
import org.User;
import org.filters.AssignmentFilter;
import org.filters.AssignmentFilters;
import org.filters.RoleFilters;

public class AssignmentManager implements Repository<RoleAssignment> {
	Map<String, RoleAssignment> roleAssignments = new HashMap<>();

	public List<RoleAssignment> findByUser(User user) {
		return roleAssignments.values().stream()
				.filter(roleAssignment -> AssignmentFilters.byUser(user).test(roleAssignment))
				.collect(Collectors.toList());
	}

	public List<RoleAssignment> findByRole(Role role) {
		return roleAssignments.values().stream()
				.filter(roleAssignment -> AssignmentFilters.byRole(role).test(roleAssignment))
				.collect(Collectors.toList());
	}

	public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
		return roleAssignments.values().stream()
				.filter(roleAssignment -> filter.test(roleAssignment))
				.collect(Collectors.toList());
	}

	public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
		return roleAssignments.values().stream()
				.filter(roleAssignment -> filter.test(roleAssignment))
				.sorted(sorter)
				.collect(Collectors.toList());
	}

	public List<RoleAssignment> getActiveAssignments() {
		return roleAssignments.values().stream()
				.filter(roleAssignment -> AssignmentFilters.activeOnly().test(roleAssignment))
				.collect(Collectors.toList());
	}

	public List<RoleAssignment> getExpiredAssignments() {
		return roleAssignments.values().stream()
				.filter(roleAssignment -> AssignmentFilters.expiringBefore(Instant.now()).test(roleAssignment))
				.collect(Collectors.toList());
	}

	public boolean userHasRole(User user, Role role) {
		return roleAssignments.values().stream()
				.filter(
						roleAssignment -> AssignmentFilters.byUser(user).and(AssignmentFilters.byRole(role)).test(roleAssignment))
				.findFirst().isPresent();
	}

	public boolean userHasPermission(User user, String permissionName, String resource) {
		return findByUser(user).stream()
				.filter(roleAssignment -> RoleFilters.hasPermission(permissionName, resource).test(roleAssignment.role()))
				.findFirst().isPresent();
	}

	public Set<Permission> getUserPermissions(User user) {
		return findByUser(user).stream()
				.map(roleAssignment -> roleAssignment.role().getPermissions())
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	public void revokeAssignment(String assignmentId) {
		roleAssignments.remove(assignmentId);
	}

	public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
		RoleAssignment currentAssignment = roleAssignments.get(assignmentId);
		if (!(currentAssignment instanceof TemporaryAssignment)) {
			return;
		}
		TemporaryAssignment temporaryAssignment = (TemporaryAssignment) currentAssignment;
		temporaryAssignment.extend(newExpirationDate);
	}

	@Override
	public void add(RoleAssignment item) {
		roleAssignments.put(item.assignmentId(), item);
	}

	@Override
	public boolean remove(RoleAssignment item) {
		return roleAssignments.remove(item.assignmentId(), item);
	}

	@Override
	public Optional<RoleAssignment> findById(String id) {
		return Optional.ofNullable(roleAssignments.get(id));
	}

	@Override
	public List<RoleAssignment> findAll() {
		return roleAssignments.values().stream()
				.collect(Collectors.toList());
	}

	@Override
	public int count() {
		return roleAssignments.size();
	}

	@Override
	public void clear() {
		roleAssignments.clear();
	}
}
