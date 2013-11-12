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

package org.n52.supervisor.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TimerTask;

import net.opengis.sensorML.x101.ArrayLinkDocument.ArrayLink;

import org.n52.supervisor.IServiceChecker;
import org.n52.supervisor.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

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

    private IdentifierGenerator idGen;

    @Inject
    protected JobSchedulerImpl(TaskServlet timer, IdentifierGenerator idGen) {
        this.timerServlet = timer;
        this.idGen = idGen;
        log.info("NEW " + this);
    }

    @Override
    public void cancel(String identifier) {
        if (log.isDebugEnabled()) {
            log.debug("Cancelling Task: " + identifier + ".");
        }
        this.timerServlet.cancel(identifier);
    }

    @Override
    public String submit(IServiceChecker checker) {
        return submit(checker, DEFAULT_DELAY_MILLISECS);
    }

    @Override
    public String submit(IServiceChecker checker, long delay) {
        log.debug("Added " + checker);

        String id = this.idGen.generate();
        submit(id, new CheckServiceTask(id, checker), delay, checker.getCheckIntervalMillis());

        return id;
    }

    private void submit(String identifier, TimerTask task, long delay, long period) {
        if (log.isDebugEnabled()) {
            log.debug("Scheduling Task: " + task + " for execution now and with period of " + period
                    + "ms after a delay of " + delay + "ms.");
        }
        this.timerServlet.submit(identifier, task, delay, period);
    }

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