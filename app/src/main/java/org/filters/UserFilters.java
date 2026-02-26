package org.filters;

public class UserFilters {

	public static UserFilter byUsername(String username) {
		return user -> username != null && username.equals(user.username());
	}

	public static UserFilter byUsernameContains(String substring) {
		return user -> substring != null && user.username().contains(substring);
	}

	public static UserFilter byEmail(String email) {
		return user -> email != null && email.equals(user.email());
	}

	public static UserFilter byEmailDomain(String domain) {
		return user -> domain != null && user.email().endsWith(domain);
	}

	public static UserFilter byFullnameContains(String substring) {
		return user -> substring != null && user.fullname().contains(substring);
	}

}
