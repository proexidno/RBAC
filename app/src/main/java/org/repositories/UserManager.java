package org.repositories;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.User;
import org.filters.UserFilter;
import org.filters.UserFilters;

public class UserManager implements Repository<User> {

	Map<String, User> users = new HashMap<>();

	public Optional<User> findByUsername(String username) {
		if (username == null) {
			return null;
		}
		return Optional.ofNullable(users.get(username));
	}

	public Optional<User> findByEmail(String email) {
		return users.values().stream()
				.filter(user -> UserFilters.byEmail(email).test(user))
				.findFirst();
	}

	public List<User> findByFilter(UserFilter filter) {
		return users.values().stream()
				.filter(user -> filter.test(user))
				.collect(Collectors.toList());
	}

	public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
		return users.values().stream()
				.filter(user -> filter.test(user))
				.sorted(sorter)
				.collect(Collectors.toList());
	}

	public boolean exists(String username) {
		if (username == null) {
			return false;
		}
		return users.containsKey(username);
	}

	public void update(String username, String newFullName, String newEmail) {
		User newUser = User.create(username, newFullName, newEmail);
		users.put(username, newUser);
	}

	@Override
	public void add(User item) {
		users.put(item.username(), item);
	}

	@Override
	public boolean remove(User item) {
		return users.remove(item.username(), item);
	}

	@Override
	public Optional<User> findById(String id) {
		return findByUsername(id);
	}

	@Override
	public List<User> findAll() {
		return users.values().stream()
				.collect(Collectors.toList());
	}

	@Override
	public int count() {
		return users.size();
	}

	@Override
	public void clear() {
		users.clear();
	}
}
