package com.babeeta.butterfly.application.app.schedule;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ScheduledTaskServiceListener implements ServletContextListener {
	private Timer timer = null;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		timer = new Timer(true);

		timer.schedule(
				new TimerTask()
				{
					@Override
					public void run() {
						ScheduledMessageTask task = null;
						do
						{
							task = ScheduledTaskService
									.getDefaultInstance().getTimeoutTask();
							if (task != null)
							{
								task.run();
							}
						} while (task != null);
					}

				}, 0, 60 * 1000);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		timer.cancel();
	}

}
