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

package org.n52.supervisor.checks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.n52.supervisor.CheckRunner;
import org.n52.supervisor.SupervisorInit;
import org.n52.supervisor.db.ResultDatabase;
import org.n52.supervisor.ui.EmailNotification;
import org.n52.supervisor.ui.Notification;
import org.n52.supervisor.util.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
public abstract class AbstractServiceCheckRunner implements CheckRunner {

    protected static final DateFormat ISO8601LocalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS.SSS");

    private static Logger log = LoggerFactory.getLogger(AbstractServiceCheckRunner.class);

    protected Client client = new Client();

    private List<CheckResult> results = new ArrayList<CheckResult>();

    protected ServiceCheck c;

    private ResultDatabase rd;

    public AbstractServiceCheckRunner(ServiceCheck check) {
        this.c = check;
    }

    @Override
    public void addResult(CheckResult r) {
        // if (r.getType().equals(CheckResult.ResultType.NEGATIVE))
        // log.debug("NEGATIVE result added to {}:", this, r);

        this.results.add(r);
        log.debug("Result added: " + r);
    }

    public void clearResults() {
        if (log.isDebugEnabled()) {
            log.debug("Clearing " + this.results.size() + " results");
        }
        this.results.clear();
    }

    protected ServiceCheckResult createNegativeResult(String text) {
        ServiceCheckResult r = new ServiceCheckResult(this.c.getIdentifier(),
                                                      text,
                                                      new Date(),
                                                      CheckResult.ResultType.NEGATIVE,
                                                      this.c.getServiceIdentifier());
        return r;
    }

    @Override
    public Check getCheck() {
        return this.c;
    }

    @Override
    public Collection<CheckResult> getResults() {
        return this.results;
    }

    @Override
    public void notifyFailure() {
        log.info("Check FAILED: {}", this);

        if (this.rd != null)
            this.rd.appendResults(getResults());

        if (this.c.getNotificationEmail() == null) {
            log.error("Can not notify via email, is null!");

            Collection<CheckResult> failures = new ArrayList<CheckResult>();
            for (CheckResult r : this.results) {
                if (r.getType().equals(CheckResult.ResultType.NEGATIVE))
                    failures.add(r);
            }

            Notification n = new EmailNotification(this.c, failures);
            SupervisorInit.appendNotification(n);

            log.debug("Submitted email with {} failures to {}.", failures.size(), this.c.getNotificationEmail());
        }
    }

    @Override
    public void notifySuccess() {
        log.info("Check SUCCESSFUL: {}", this);

        if (this.rd != null)
            this.rd.appendResults(getResults());
    }

    protected boolean saveAndReturnNegativeResult(String text) {
        ServiceCheckResult r = createNegativeResult(text);
        addResult(r);
        return false;
    }

    @Override
    public void setCheck(Check c) throws UnsupportedCheckException {
        if (c instanceof ServiceCheck) {
            ServiceCheck sc = (ServiceCheck) c;
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
