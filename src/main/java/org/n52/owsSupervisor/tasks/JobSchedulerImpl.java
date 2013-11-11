/**
 * ﻿Copyright (C) 2013 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.owsSupervisor.tasks;

import java.util.TimerTask;

import org.n52.owsSupervisor.IServiceChecker;
import org.n52.owsSupervisor.SupervisorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Class encapsulates a {@link TaskServlet} where tasks are forwared to.
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class JobSchedulerImpl implements IJobScheduler {

	private static final long DEFAULT_DELAY_MILLISECS = 10;

	private static Logger log = LoggerFactory.getLogger(JobSchedulerImpl.class);

	private TaskServlet timerServlet;

	/**
	 * 
	 * @param timer
	 */
	protected JobSchedulerImpl(TaskServlet timer) {
		this.timerServlet = timer;
		log.info("NEW " + this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.n52.owsSupervisor.tasks.IJobScheduler#cancel(java.lang.String)
	 */
	@Override
	public void cancel(String identifier) {
		if (log.isDebugEnabled()) {
			log.debug("Cancelling Task: " + identifier + ".");
		}
		this.timerServlet.cancel(identifier);
	}

	/*
	 * (non-Javadoc)
	 * @see org.n52.owsSupervisor.tasks.IJobScheduler#submit(org.n52.owsSupervisor.checks.IServiceChecker)
	 */
	@Override
	public String submit(IServiceChecker checker) {
		return submit(checker, DEFAULT_DELAY_MILLISECS);
	}

	/**
	 * 
	 * @param checker
	 * @param delay
	 * @return
	 */
	private String submit(IServiceChecker checker, long delay) {
		log.debug("Added " + checker);
		
		// schedule periodic push
		String id = SupervisorProperties.getInstance().getUUID();
		submitRepeating(id, new CheckServiceTask(id, checker), delay,
				checker.getCheckIntervalMillis());
		
		return id;
	}

	/**
	 * 
	 * @param identifier
	 * @param task
	 * @param delay
	 * @param period
	 */
	private void submitRepeating(String identifier, TimerTask task, long delay,
			long period) {
		if (log.isDebugEnabled()) {
			log.debug("Scheduling Task: " + task
					+ " for execution now and with period of " + period
					+ "ms after a delay of " + delay + "ms.");
		}
		this.timerServlet.submit(identifier, task, delay, period);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("JobSchedulerImpl [default delay (msecs) (ALWAYS applied!)=");
		sb.append(DEFAULT_DELAY_MILLISECS);
		sb.append(", internal task handler: ");
		sb.append(this.timerServlet);
		sb.append("]");
		return sb.toString();
	}
}