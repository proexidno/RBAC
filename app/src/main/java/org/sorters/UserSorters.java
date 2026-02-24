package org.sorters;

import java.util.Comparator;

import org.User;

public class UserSorters {
	static Comparator<User> byUsername() {
		return Comparator.comparing(
				User::username,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}

	static Comparator<User> byFullName() {
		return Comparator.comparing(
				User::fullname,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}

	static Comparator<User> byEmail() {
		return Comparator.comparing(
				User::email,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}
}
