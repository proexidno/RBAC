package org;

import java.util.Comparator;

public class UserSorters {
	Comparator<User> byUsername() {
		return Comparator.comparing(
				User::username,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}

	Comparator<User> byFullName() {
		return Comparator.comparing(
				User::fullname,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}

	Comparator<User> byEmail() {
		return Comparator.comparing(
				User::email,
				Comparator.nullsLast(Comparator.naturalOrder()));
	}
}
