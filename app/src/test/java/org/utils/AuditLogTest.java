package org.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuditLogTest {

	@Test
	public void testLogAndRetrieve() {
		AuditLog log = new AuditLog();
		log.log("CREATE", "admin", "user1", "Created new user");

		assertEquals(1, log.getEntryCount());
		assertEquals(1, log.getByAction("CREATE").size());
		assertEquals(1, log.getByPerformer("admin").size());
	}
}
