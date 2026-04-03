package org.repositories;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.User;
import org.filters.UserFilter;
import org.filters.UserFilters;

public class UserManager implements Repository<User> {

	ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

	public Optional<User> findByUsername(String username) {
		return Optional.ofNullable(users.get(username));
	}

	public Optional<User> findByEmail(String email) {
		return users.values().stream()
				.filter(UserFilters.byEmail(email)::test)
				.findFirst();
	}

	public List<User> findByFilterParallel(UserFilter filter) {
		return users.values().parallelStream()
				.filter(filter::test)
				.collect(Collectors.toList());
	}

	public List<User> findByFilter(UserFilter filter) {
		return users.values().stream()
				.filter(filter::test)
				.collect(Collectors.toList());
	}

	public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
		return users.values().stream()
				.filter(filter::test)
				.sorted(sorter)
				.collect(Collectors.toList());
	}

	public boolean exists(String username) {
		return username != null && users.containsKey(username);
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
		return List.copyOf(users.values());
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
