package org.repositories;

import org.User;
import org.filters.UserFilter;
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
class UserManagerTest {

	private UserManager userManager;

	private User createUser(String username, String fullName, String email) {
		try {
			return User.create(username, fullName, email);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Test setup failed: " + e.getMessage(), e);
		}
	}

	@BeforeEach
	void setUp() {
		userManager = new UserManager();
	}

	@Test
	@DisplayName("Add valid user successfully")
	void addValidUser() {
		User user = createUser("john_doe", "John Doe", "john@example.com");

		userManager.add(user);

		Optional<User> found = userManager.findById("john_doe");
		assertTrue(found.isPresent());
		assertEquals("John Doe", found.get().fullname());
	}

	@Test
	@DisplayName("Prevent duplicate username")
	void addDuplicateUsernameThrowsException() {
		User user1 = createUser("alice", "Alice Smith", "alice@test.com");
		User user2 = createUser("alice", "Alice Jones", "alice_new@test.com");

		userManager.add(user1);
		userManager.add(user2);

		assertEquals(1, userManager.count());
	}

	@Test
	@DisplayName("Remove existing user")
	void removeExistingUser() {
		User user = createUser("bob", "Bob Brown", "bob@test.com");
		userManager.add(user);

		boolean removed = userManager.remove(user);

		assertTrue(removed);
		assertFalse(userManager.findById("bob").isPresent());
		assertEquals(0, userManager.count());
	}

	@Test
	@DisplayName("Remove non-existing user returns false")
	void removeNonExistingUser() {
		User fakeUser = createUser("ghost", "Ghost", "ghost@test.com");
		assertFalse(userManager.remove(fakeUser));
	}

	@Test
	@DisplayName("Find by Username")
	void findByUsername() {
		User user = createUser("charlie", "Charlie Chaplin", "charlie@movies.com");
		userManager.add(user);

		Optional<User> found = userManager.findByUsername("charlie");

		assertTrue(found.isPresent());
		assertEquals("charlie@movies.com", found.get().email());
	}

	@Test
	@DisplayName("Find by Email")
	void findByEmail() {
		User user = createUser("dave", "Dave Davis", "dave@sports.com");
		userManager.add(user);

		Optional<User> found = userManager.findByEmail("dave@sports.com");

		assertTrue(found.isPresent());
		assertEquals("dave", found.get().username());
	}

	@Test
	@DisplayName("Update user details")
	void updateUserDetails() {
		User user = createUser("eve", "Eve Original", "eve@old.com");
		userManager.add(user);

		userManager.update("eve", "Eve Updated", "eve@new.com");

		Optional<User> updated = userManager.findByUsername("eve");
		assertTrue(updated.isPresent());
		assertEquals("Eve Updated", updated.get().fullname());
		assertEquals("eve@new.com", updated.get().email());
	}

	@Test
	@DisplayName("Update non-existing user throws exception")
	void updateNonExistingUser() {
		assertThrows(IllegalArgumentException.class,
				() -> userManager.update("", "New Name", "new@email.com"));
	}

	@Test
	@DisplayName("Exists check")
	void existsCheck() {
		userManager.add(createUser("frank", "Frank", "frank@test.com"));

		assertTrue(userManager.exists("frank"));
		assertFalse(userManager.exists("george"));
	}

	@Test
	@DisplayName("Find All with Filter and Sorter")
	void findAllWithFilterAndSorter() {
		userManager.add(createUser("zack", "Zack Zebra", "z@a.com"));
		userManager.add(createUser("adam", "Adam Ant", "a@a.com"));
		userManager.add(createUser("billy", "Billy Bear", "b@a.com"));

		UserFilter allFilter = user -> true;
		Comparator<User> byNameDesc = Comparator.comparing(User::username).reversed();

		List<User> result = userManager.findAll(allFilter, byNameDesc);

		assertEquals(3, result.size());
		assertEquals("zack", result.get(0).username());
		assertEquals("adam", result.get(2).username());
	}

	@Test
	@DisplayName("Clear repository")
	void clearRepository() {
		userManager.add(createUser("us1", "One One", "1@t.com"));
		userManager.add(createUser("us2", "Two One", "2@t.com"));

		userManager.clear();

		assertEquals(0, userManager.count());
		assertTrue(userManager.findAll().isEmpty());
	}
}
