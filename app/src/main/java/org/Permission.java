package org;

import java.util.Objects;
import java.util.regex.Pattern;

public record Permission(String name,
		String resource,
		String description) {

	public Permission(String name, String resource, String description) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name shouldn't be empty");
		}
		if (name.contains(" ")) {
			throw new IllegalArgumentException("Permission name shouldn't contain spaces");
		}
		this.name = name.toUpperCase();

		if (resource == null || resource.isEmpty()) {
			throw new IllegalArgumentException("Resource shouldn't be empty");
		}

		Pattern resourcePattern = Pattern.compile("[a-z]+");
		if (!resourcePattern.matcher(resource).matches()) {
			throw new IllegalArgumentException("Resousrce should all lowercase latin characters");
		}
		this.resource = resource;

		if (description == null || description.isEmpty()) {
			throw new IllegalArgumentException("Description shouldn't be empty");
		}
		this.description = description;
	}

	public String format() {
		return String.format("%s on %s: %s", name, resource, description);
	}

	public boolean matches(String namePattern, String resourcePattern) {
		Pattern namePatternPattern = Pattern.compile(namePattern);
		Pattern resourcePatternPattern = Pattern.compile(resourcePattern);
		return namePatternPattern.matcher(name).matches() && resourcePatternPattern.matcher(resource).matches();
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == this.getClass() && obj.hashCode() == this.hashCode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, resource);
	}
}
