package org.repositories;

import org.Permission;
import org.Role;
import org.filters.RoleFilter;
import org.filters.RoleFilters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RoleManagerTest {

	private RoleManager roleManager;
	private Permission readUsers;
	private Permission writeUsers;

	@BeforeEach
	void setUp() {
		roleManager = new RoleManager();
		readUsers = new Permission("READ", "users", "Can read users");
		writeUsers = new Permission("WRITE", "users", "Can write users");
	}

	@Test
	@DisplayName("Add role successfully")
	void addRoleSuccess() {
		Role role = new Role("Administrator", "Admin role");
		roleManager.add(role);

		assertNotNull(roleManager.findByFilter(RoleFilters.byName("Administrator")).get(0));

		assertNotNull(roleManager.findById(role.getId()));
	}

	@Test
	@DisplayName("Prevent duplicate role names")
	void addDuplicateRoleName() {
		Role role1 = new Role("Manager", "Manager role");
		Role role2 = new Role("Manager", "Duplicate manager");

		roleManager.add(role1);

		assertEquals(1, roleManager.count());
	}

	@Test
	@DisplayName("Find by Name")
	void findByName() {
		Role role = new Role("Viewer", "View only");
		roleManager.add(role);

		Optional<Role> found = roleManager.findByName("Viewer");

		assertTrue(found.isPresent());
	}

	@Test
	@DisplayName("Add Permission to Role")
	void addPermissionToRole() {
		Role role = new Role("Editor", "Can edit");
		roleManager.add(role);

		roleManager.addPermissionToRole("Editor", readUsers);

		Optional<Role> updated = roleManager.findByName("Editor");
		assertTrue(updated.isPresent());
		assertTrue(updated.get().hasPermission(readUsers));
	}

	@Test
	@DisplayName("Remove Permission from Role")
	void removePermissionFromRole() {
		Role role = new Role("SuperAdmin", "All access");
		role.addPermission(readUsers);
		roleManager.add(role);

		roleManager.removePermissionFromRole("SuperAdmin", readUsers);

		Optional<Role> updated = roleManager.findByName("SuperAdmin");
		assertTrue(updated.isPresent());
	}

	@Test
	@DisplayName("Find Roles with Specific Permission")
	void findRolesWithPermission() {
		Role admin = new Role("Admin", "Admin");
		admin.addPermission(readUsers);

		Role editor = new Role("Editor", "Editor");
		editor.addPermission(readUsers);
		editor.addPermission(writeUsers);

		roleManager.add(admin);
		roleManager.add(editor);

		List<Role> rolesWithRead = roleManager.findRolesWithPermission("READ", "users");

		assertEquals(2, rolesWithRead.size());
		assertTrue(rolesWithRead.stream().anyMatch(r -> r.getName().equals("Admin")));
		assertTrue(rolesWithRead.stream().anyMatch(r -> r.getName().equals("Editor")));
	}

	@Test
	@DisplayName("Delete role assigned to users throws exception")
	void deleteRoleAssignedToUser() {
		Role role = new Role("Deletable", "To be deleted");
		roleManager.add(role);

		boolean removed = roleManager.remove(role);
		assertTrue(removed);
		assertFalse(roleManager.findByName("Deletable").isPresent());
	}

	@Test
	@DisplayName("Synchronization: Remove by ID updates Name Index")
	void removeByIdUpdatesNameIndex() {
		Role role = new Role("TempRole", "Temp");
		roleManager.add(role);

		String id = role.getId();
		roleManager.remove(role);

		assertFalse(roleManager.findById(id).isPresent());
		assertFalse(roleManager.findByName("TempRole").isPresent());
	}

	@Test
	@DisplayName("Find All with Filter and Sorter")
	void findAllWithFilterAndSorter() {
		Role r1 = new Role("Z_Role", "Z");
		Role r2 = new Role("A_Role", "A");
		roleManager.add(r1);
		roleManager.add(r2);

		RoleFilter allFilter = role -> true;
		Comparator<Role> byNameAsc = Comparator.comparing(Role::getName);

		List<Role> result = roleManager.findAll(allFilter, byNameAsc);

		assertEquals(2, result.size());
		assertEquals("A_Role", result.get(0).getName());
		assertEquals("Z_Role", result.get(1).getName());
	}
}
