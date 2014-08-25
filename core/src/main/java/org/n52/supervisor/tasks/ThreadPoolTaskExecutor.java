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
package org.n52.supervisor.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.api.CheckTask;
import org.n52.supervisor.api.CheckTaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * This class can be used to execute {@link TimerTask} instances. It runs as a servlet and can be accessed by
 * other servlets for task scheduling and cancelling. The actual service method for GET and POST requests are
 * not implemented. It also provides methods to access the appropriate instances of
 * {@link ICatalogStatusHandler} and {@link ICatalogFactory} for tasks that run within this servlet.
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
@Singleton
public class ThreadPoolTaskExecutor implements TaskExecutor {


    private static final String CHECK_THREAD_COUNT = "CHECK_THREAD_COUNT";

    private ScheduledThreadPoolExecutor executor;

    private static Logger log = LoggerFactory.getLogger(ThreadPoolTaskExecutor.class);

    private static final String SEND_EMAILS = "supervisor.tasks.email.send";

    private Properties props;

    /**
     * List that holds all repeated task during run-time.
     */
    private ArrayList<TaskElement> tasks = new ArrayList<ThreadPoolTaskExecutor.TaskElement>();

    private String configFile = "/supervisor.properties";

    private CheckTaskFactory taskFactory;
    
    @Inject
    public ThreadPoolTaskExecutor(CheckTaskFactory taskFactory) throws TaskExecutorException {
        log.info("NEW {}", this);

        this.taskFactory = taskFactory;
        
        init();
    }

    public void cancel(String identifier) {
        for (TaskElement te : this.tasks) {
            if (te.id.equals(identifier)) {
                te.task.cancel();
                log.info("CANCELED " + te);
            }

        }
    }

    @Override
    protected void finalize() throws Throwable {
        log.info("Finalize {}", this);
        executor.shutdownNow();
        executor = null;
        this.tasks.clear();
        this.tasks = null;
    }

    public ArrayList<TaskElement> getTasks() {
        return this.tasks;
    }

    public void init() throws TaskExecutorException {
        log.info(" * Initializing Timer ... ");

        // get configFile as Inputstream
        InputStream configStream = ThreadPoolTaskExecutor.class.getResourceAsStream(configFile);
        if (configStream == null) {
            log.error("Could not open the config file " + configFile);
            throw new TaskExecutorException("Could not open the config file.");
        }

        // TODO get properties via Guice
        // load properties file
        try {
            this.props = loadProperties(configStream);
        }
        catch (IOException e) {
            log.error("Could not load properties file!", e);
            throw new TaskExecutorException("Could not load properties file!");
        }

        // init executor
        int threadCount = Integer.parseInt(this.props.getProperty(CHECK_THREAD_COUNT));
        executor = new ScheduledThreadPoolExecutor(threadCount);

        boolean sendEmails = Boolean.parseBoolean(props.getProperty(SEND_EMAILS));

        if (sendEmails) {
        	/*
        	 * disabled for the moment, does not work as expected
        	 */
            // add task for email notifications, with out without admin email
//            String adminEmail = this.props.getProperty(EMAIL_ADMIN_EMAIL);
//
//            long emailSendInterval = Long.valueOf(this.props.getProperty(EMAIL_SEND_PERIOD_MINDS));
//            if ( !adminEmail.contains("@ADMIN_EMAIL@")) {
//                log.info("Found admin email address for send email task.");
//                SendEmailTask set = new SendEmailTask(adminEmail, this.rd);
//                submit(EMAIL_SENDER_TASK_ID, set, emailSendInterval, emailSendInterval);
//            }
        }
        else
            log.debug("Not sending emails, not starting email tasks.");

        log.info(" ***** Timer initiated successfully! ***** ");
    }

    private Properties loadProperties(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);

        return properties;
    }

    public void submit(String identifier, CheckRunner cr, long delay) throws TaskExecutorException {
    	TimerTask task = createTimerTask(cr);
    	
        executor.schedule(task, delay, TimeUnit.MILLISECONDS);
        if (log.isDebugEnabled()) {
            log.debug("Submitted: " + task + " with delay = " + delay);
        }

        this.tasks.add(new TaskElement(identifier, task, delay, 0l));
    }

    private TimerTask createTimerTask(CheckRunner cr) throws TaskExecutorException {
    	CheckTask t = this.taskFactory.create(cr);
    	
    	if (t instanceof TimerTask) {
    		return (TimerTask) t;
    	}

    	throw new TaskExecutorException(
    			String.format("Could not create TimerTask from CheckRunner %s", cr));
	}

	/**
     * 
     * " Finally, fixed-rate execution is appropriate for scheduling multiple repeating timer tasks that must
     * remain synchronized with respect to one another." See
     * {@link Timer#scheduleAtFixedRate(TimerTask, long, long)} for details.
     * 
     * @param task
     * @param delay
     * @param period
	 * @throws TaskExecutorException 
     */
    public void submit(String identifier, CheckRunner cr, long delay, long period) throws TaskExecutorException {
		if (executor == null)
            log.error("Executor is NULL, cannot submit task with id {}: {}", identifier, cr);
        else {
        	TimerTask task = createTimerTask(cr);
            executor.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);

            this.tasks.add(new TaskElement(identifier, task, delay, period));

            log.debug("Submitted: {} with period = {}, delay = {}", task, period, delay);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ThreadPoolTaskExecutor [");
        if (props != null) {
            builder.append("props=");
            builder.append(props);
            builder.append(", ");
        }
        if (tasks != null) {
            builder.append("tasks=");
            builder.append(tasks);
        }
        builder.append("]");
        return builder.toString();
    }

	 /**
     * Inner class to handle storage and cancelling of tasks at runtime.
     */
    public class TaskElement {
        protected Date date;
        protected long delay;
        protected String id;
        protected long period;
        protected TimerTask task;

        /**
         * 
         * @param identifier
         * @param task
         * @param delay
         * @param period
         */
        protected TaskElement(String identifier, TimerTask taskP, long delayP, long periodP) {
            this.id = identifier;
            this.task = taskP;
            this.delay = delayP;
            this.period = periodP;
            this.date = new Date(0l);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("TaskElement [");
            sb.append(this.task);
            sb.append(", delay=");
            sb.append(this.delay);
            sb.append(", period=");
            sb.append(this.period);
            sb.append(", date=");
            sb.append(this.date);
            sb.append("]");
            return sb.toString();
        }
    }
}
