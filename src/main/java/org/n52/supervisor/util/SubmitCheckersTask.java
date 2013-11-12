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

package org.n52.supervisor.util;

import java.util.Collection;
import java.util.TimerTask;

import org.n52.supervisor.IServiceChecker;
import org.n52.supervisor.tasks.IJobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class SubmitCheckersTask extends TimerTask {

    private static Logger log = LoggerFactory.getLogger(SubmitCheckersTask.class);

    private Collection<IServiceChecker> checkers;

    private IJobScheduler scheduler;

    public SubmitCheckersTask(IJobScheduler schedulerP, Collection<IServiceChecker> checkersP) {
        this.checkers = checkersP;
        this.scheduler = schedulerP;

        log.info("NEW " + this);
    }

    @Override
    public void run() {
        log.info("Submitting checkers.");

        for (IServiceChecker c : this.checkers) {
            this.scheduler.submit(c);
        }
    }

}
