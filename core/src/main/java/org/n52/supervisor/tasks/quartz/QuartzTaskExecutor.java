/**
 * ﻿Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source Software GmbH
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
package org.n52.supervisor.tasks.quartz;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.db.ResultDatabase;
import org.n52.supervisor.tasks.TaskExecutor;
import org.n52.supervisor.tasks.TaskExecutorException;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class QuartzTaskExecutor implements TaskExecutor {

	private static final Logger logger = LoggerFactory
			.getLogger(QuartzTaskExecutor.class);
	private Scheduler quartz;
	private Map<String, JobDetail> jobs = new HashMap<>();
	private LocalJobFactory factory;
	private ResultDatabase db;

	@Inject
	public QuartzTaskExecutor(ResultDatabase db) throws SchedulerException {
		this.db = db;
		this.quartz = new StdSchedulerFactory().getScheduler();
		this.factory = new LocalJobFactory();
		this.quartz.setJobFactory(this.factory);
		this.quartz.start();
	}

	@Override
	public void submit(String identifier, CheckRunner t, long delay, long period)
			throws TaskExecutorException {
		QuartzRunner job = new QuartzRunner(t, this.db);
		JobDetailImpl jobDetail = new JobDetailImpl();
		jobDetail.setKey(new JobKey(identifier));
		jobDetail.setJobClass(job.getClass());
		
		this.factory.register(jobDetail.getKey().getName(), job);

		TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
				.withIdentity(identifier.concat("_trigger"));
		
		if (delay != 0) {
			Date delayDate = new Date();
			delayDate = new Date(delayDate.getTime() + delay);
			builder = builder.startAt(delayDate);
		}
		else {
			builder = builder.startNow();
		}
		
		if (period != 0) {
			builder.withSchedule(
					SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInMilliseconds(period)
							.repeatForever());
		}
		
		Trigger trigger = builder.build();

		try {
			this.quartz.scheduleJob(jobDetail, trigger);

			synchronized (this) {
				this.jobs.put(identifier, jobDetail);
			}

			logger.info("Added Job '{}' to scheduling.", identifier);
		} catch (SchedulerException e) {
			logger.warn(e.getMessage(), e);
			throw new TaskExecutorException(e);
		}

	}

	@Override
	public void cancel(String identifier) {
		JobDetail job;
		synchronized (this) {
			if (this.jobs.containsKey(identifier)) {
				job = this.jobs.get(identifier);
				this.jobs.remove(identifier);
				this.factory.unregister(identifier);
			} else {
				logger.info("No job with id '{}' registered", identifier);
				return;
			}
		}

		try {
			if (this.quartz.deleteJob(job.getKey())) {
				logger.info("Removed Job '{}' from scheduling.", identifier);
			} else {
				logger.warn("Job '{}' not found in this quartz instance.",
						identifier);
			}
		} catch (SchedulerException e) {
			logger.warn("Error while removing job from quartz", e);
		}
	}
	
	@Override
	public void shutdown() {
		try {
			this.quartz.shutdown();
		} catch (SchedulerException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	private static class LocalJobFactory implements JobFactory {

		private final Map<String, Job> jobByName = new HashMap<>();

		@Override
		public synchronized Job newJob(TriggerFiredBundle bundle, Scheduler scheduler)
				throws SchedulerException {
			return jobByName.get(bundle.getJobDetail().getKey().getName());
		}
		
		public synchronized void unregister(String identifier) {
			jobByName.remove(identifier);
		}

		public synchronized void register(String name, Job job) {
			jobByName.put(name, job);
		}


	}

}
