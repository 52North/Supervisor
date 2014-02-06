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

import java.util.Collection;
import java.util.TimerTask;

import org.n52.supervisor.CheckRunner;
import org.n52.supervisor.checks.CheckResult;
import org.n52.supervisor.db.ResultDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * 
 * Class for executing the check.
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class CheckTaskImpl extends TimerTask implements CheckTask {

    private static Logger log = LoggerFactory.getLogger(CheckTaskImpl.class);

    private CheckRunner checker;

    private ResultDatabase db;

    @Inject
    public CheckTaskImpl(ResultDatabase db, @Assisted
    CheckRunner checker) {
        this.checker = checker;
        this.db = db;

        log.info("NEW {}", this);
    }

    @Override
    public boolean cancel() {
        log.debug("Cancelling [}", this);
        return super.cancel();
    }

    @Override
    public Collection<CheckResult> checkIt(CheckRunner c) {
        boolean b = c.check();
        if ( !b)
            c.notifyFailure();
        else
            c.notifySuccess();

        return c.getResults();
    }

    @Override
    protected void finalize() throws Throwable {
        log.debug("Finalizing {}", this);
        super.finalize();
    }

    @Override
    public void run() {
        log.debug("*** Running check: {}", this.checker);

        Collection<CheckResult> currentResults = checkIt(this.checker);
        this.db.appendResults(currentResults);

        log.info("*** Ran check, got {} results.", currentResults.size());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ServiceCheckTask [checker=");
        sb.append(this.checker);
        sb.append("]");
        return sb.toString();
    }

}
