package org.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {

	private final List<AuditEntry> entries;

	public AuditLog() {
		this.entries = new ArrayList<>();
	}

	public void log(String action, String performer, String target, String details) {
		AuditEntry entry = new AuditEntry(
				Instant.now().toString(),
				action,
				performer != null ? performer : "unknown",
				target != null ? target : "N/A",
				details != null ? details : "");
		entries.add(entry);
	}

	public List<AuditEntry> getAll() {
		return new ArrayList<>(entries);
	}

	public List<AuditEntry> getByPerformer(String performer) {
		return entries.stream()
				.filter(e -> e.performer().equalsIgnoreCase(performer))
				.collect(Collectors.toList());
	}

	public List<AuditEntry> getByAction(String action) {
		return entries.stream()
				.filter(e -> e.action().equalsIgnoreCase(action))
				.collect(Collectors.toList());
	}

	public List<AuditEntry> getByTarget(String target) {
		return entries.stream()
				.filter(e -> e.target().equalsIgnoreCase(target))
				.collect(Collectors.toList());
	}

	public void printLog() {
		if (entries.isEmpty()) {
			System.out.println("No audit entries found.");
			return;
		}

		System.out.println(FormatUtils.formatHeader("AUDIT LOG"));
		System.out.println();

		String[] headers = { "Timestamp", "Action", "Performer", "Target", "Details" };
		List<String[]> rows = entries.stream()
				.map(e -> new String[] {
						truncate(e.timestamp(), 25),
						e.action(),
						e.performer(),
						e.target(),
						truncate(e.details(), 30)
				})
				.collect(Collectors.toList());

		System.out.println(FormatUtils.formatTable(headers, rows));
	}

	public void saveToFile(String filename) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
			writer.println("=== AUDIT LOG ===");
			writer.println("Generated: " + Instant.now());
			writer.println("Total Entries: " + entries.size());
			writer.println();

			for (AuditEntry entry : entries) {
				writer.println("----------------------------------------");
				writer.println("Timestamp: " + entry.timestamp());
				writer.println("Action: " + entry.action());
				writer.println("Performer: " + entry.performer());
				writer.println("Target: " + entry.target());
				writer.println("Details: " + entry.details());
				writer.println();
			}

			System.out.println("Audit log saved to: " + filename);
		} catch (IOException e) {
			System.err.println("Error saving audit log: " + e.getMessage());
		}
	}

	public int getEntryCount() {
		return entries.size();
	}

	public void clear() {
		entries.clear();
	}

	private String truncate(String text, int maxLength) {
		if (text == null || text.length() <= maxLength) {
			return text != null ? text : "";
		}
		return text.substring(0, maxLength - 3) + "...";
	}

	public record AuditEntry(
			String timestamp,
			String action,
			String performer,
			String target,
			String details) {
	}
}
