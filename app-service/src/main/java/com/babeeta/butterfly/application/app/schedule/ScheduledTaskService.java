package com.babeeta.butterfly.application.app.schedule;

import java.util.concurrent.DelayQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledTaskService {
	private final static Logger logger = LoggerFactory
			.getLogger(ScheduledTaskService.class);
	private static final ScheduledTaskService defaultInstance = new ScheduledTaskService();

	private final DelayQueue<ScheduledMessageTask> SCHEDULED_TASK_QUEUE =
			new DelayQueue<ScheduledMessageTask>();

	private ScheduledTaskService() {

	}

	public static ScheduledTaskService getDefaultInstance() {
		return defaultInstance;
	}

	public boolean setupTask(ScheduledMessageTask task) {
		SCHEDULED_TASK_QUEUE.put(task);
		return true;
	}

	public boolean removeTask(ScheduledMessageTask task) {
		if (task == null) {
			logger.error("Input parameter is null!");
			return false;
		}
		if (SCHEDULED_TASK_QUEUE.remove(task)) {
			return true;
		} else {
			logger.debug("Remove task (id = {}) failed.", task.getUid());
			return false;
		}
	}

	public ScheduledMessageTask getTimeoutTask() {
		ScheduledMessageTask task = SCHEDULED_TASK_QUEUE.poll();
		if (task == null) {
			logger.debug("No timeout task!");
		} else {
			logger.info("Message {} delay timeout!", task.getUid());
		}
		return task;
	}

}
