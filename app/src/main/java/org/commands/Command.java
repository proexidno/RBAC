package org.commands;

import java.util.Scanner;
import org.RBACSystem;

@FunctionalInterface
public interface Command {
	void execute(Scanner scanner, RBACSystem system);
}
