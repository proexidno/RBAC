package org.executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundExecutor {
	private final ExecutorService executor;
	private final ScheduledExecutorService scheduler;
	private final BlockingQueue<AuditLogEntry> auditQueue;
	private final AtomicBoolean running = new AtomicBoolean(true);
	private final Thread auditProcessor;

	public BackgroundExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime) {
		this.executor = new ThreadPoolExecutor(
				corePoolSize,
				maxPoolSize,
				keepAliveTime,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(100),
				new ThreadPoolExecutor.CallerRunsPolicy());

		this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "scheduled-task-runner");
			t.setDaemon(true);
			return t;
		});

		this.auditQueue = new LinkedBlockingQueue<>();

		this.auditProcessor = new Thread(this::processAuditQueue, "audit-processor");
		this.auditProcessor.setDaemon(true);
		this.auditProcessor.start();
	}

	public <T> Future<T> submitAsync(Callable<T> task) {
		return executor.submit(task);
	}

	public void submitAsync(Runnable task) {
		executor.submit(task);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	public void logAudit(String action, String performer, String target, String details) {
		if (running.get()) {
			auditQueue.offer(new AuditLogEntry(
					System.currentTimeMillis(),
					action,
					performer,
					target,
					details));
		}
	}

	private void processAuditQueue() {
		while (running.get() || !auditQueue.isEmpty()) {
			try {
				AuditLogEntry entry = auditQueue.poll(100, TimeUnit.MILLISECONDS);
				if (entry != null) {
					System.out.printf("[AUDIT] %s | %s | %s -> %s | %s%n",
							entry.timestamp(), entry.action(), entry.performer(),
							entry.target(), entry.details());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	public void scheduleExpiredAssignmentsCleanup(Runnable cleanupTask, long intervalSeconds) {
		scheduler.scheduleAtFixedRate(cleanupTask, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
	}

	public void shutdown() {
		running.set(false);
		executor.shutdown();
		scheduler.shutdown();

		try {
			if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
			if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
				scheduler.shutdownNow();
			}
			auditProcessor.join(5000);
		} catch (InterruptedException e) {
			executor.shutdownNow();
			scheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	public boolean isShutdown() {
		return executor.isShutdown();
	}

	public record AuditLogEntry(long timestamp, String action, String performer, String target, String details) {
	}
}
