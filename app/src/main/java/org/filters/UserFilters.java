package org.filters;

public class UserFilters {

	static UserFilter byUsername(String username) {
		return user -> username != null && username.equals(user.username());
	}

	static UserFilter byUsernameContains(String substring) {
		return user -> substring != null && user.username().contains(substring);
	}

	static UserFilter byEmail(String email) {
		return user -> email != null && email.equals(user.email());
	}

	static UserFilter byEmailDomain(String domain) {
		return user -> domain != null && user.email().endsWith(domain);
	}

	static UserFilter byFullnameContains(String substring) {
		return user -> substring != null && user.fullname().contains(substring);
	}

}
