package org;

import java.util.Objects;
import java.util.regex.Pattern;

public record User(
		String username,
		String fullname,
		String email) {

	public User(String username, String fullname, String email) {
		if (username == null || username.isEmpty()) {
			throw new IllegalArgumentException("Empty username");
		}
		this.username = username;
		if (fullname == null || fullname.isEmpty()) {
			throw new IllegalArgumentException("Empty fullname");
		}
		this.fullname = fullname;
		if (email == null || email.isEmpty()) {
			throw new IllegalArgumentException("Empty email");
		}
		this.email = email;
	}

	static public User create(String username, String fullname, String email) {

		Pattern usernamePattern = Pattern.compile("[A-Za-z0-9_]+");
		if ((3 > username.length() || username.length() > 20)
				|| !usernamePattern.matcher(username).matches()) {
			throw new IllegalArgumentException("Illegal username");
		}

		Pattern emailPattern = Pattern.compile("\\w+@\\w+\\.\\w+");
		if (!emailPattern.matcher(email).matches()) {
			throw new IllegalArgumentException("Illegal email");
		}

		return new User(username, fullname, email);
	}

	public String format() {
		return String.format("%s (%s) <%s>", username, fullname, email);
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == this.getClass() && obj.hashCode() == this.hashCode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, fullname, email);
	}

}
