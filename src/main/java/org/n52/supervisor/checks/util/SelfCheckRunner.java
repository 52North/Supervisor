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

package org.n52.supervisor.checks.util;

import java.util.Collection;
import java.util.Date;

import org.n52.supervisor.SupervisorInit;
import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.n52.supervisor.checks.Check;
import org.n52.supervisor.checks.CheckResult;
import org.n52.supervisor.checks.CheckResult.ResultType;
import org.n52.supervisor.checks.UnsupportedCheckException;
import org.n52.supervisor.db.ResultDatabase;
import org.n52.supervisor.ui.EmailNotification;
import org.n52.supervisor.ui.INotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * this check collects information about currently running checks and the state of the service.
 * 
 * @author Daniel Nüst (d.nuest@52north.org)
 * 
 */
public class SelfCheckRunner extends AbstractServiceCheckRunner {

    private static final long L1024_2 = 1024 * 1024;

    private static Logger log = LoggerFactory.getLogger(SelfCheckRunner.class);

    private ResultDatabase rd;

    public SelfCheckRunner(SelfCheck check) {
        super(check);
    }

    @Override
    public boolean check() {
        // check if everything is running fine...
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        StringBuilder sb = new StringBuilder();

        sb.append("Self check ran succesfully, service is most probably up and running. Go to <a href='");
        sb.append(this.c.getServiceUrl());
        sb.append("' title='OwsSupervisor HTML Interface'>");
        sb.append(this.c.getServiceUrl());
        sb.append("</a>");
        sb.append(" for the current check status.");

        sb.append(" *** Heap Info: Size (Mb) is ");
        sb.append(heapSize / L1024_2);
        sb.append(" of ");
        sb.append(heapMaxSize / L1024_2);
        sb.append(" leaving ");
        sb.append(heapFreeSize / L1024_2);
        sb.append(".");

        // TODO add currently running tasks and their last message
        CheckResult result = new SelfCheckResult(this.c.getIdentifier(), sb.toString(), new Date(), ResultType.POSITIVE);
        addResult(result);

        return true;
    }

    @Override
    public void notifyFailure() {
        log.error("SelfChecker cannot fail!");
        if (this.rd != null)
            this.rd.appendResults(getResults());
    }

    @Override
    public void notifySuccess() {
        log.debug("Check SUCCESSFUL: {}", this);

        Collection<CheckResult> results = getResults();

        if (this.c.getNotificationEmail() == null)
            log.error("Can not notify via email, is null!");
        else {
            INotification noti = new EmailNotification(c, results);
            // append for email notification to queue
            SupervisorInit.appendNotification(noti);

            log.debug("Submitted email with {} successes.", results.size());
        }

        if (this.rd != null)
            this.rd.appendResults(getResults());
    }

    @Override
    public void setCheck(Check c) throws UnsupportedCheckException {
        if (c instanceof SelfCheck) {
            SelfCheck sc = (SelfCheck) c;
            this.c = sc;
        }
        else
            throw new UnsupportedCheckException();
    }

    @Override
    public void setResultDatabase(ResultDatabase rd) {
        this.rd = rd;
    }

}
