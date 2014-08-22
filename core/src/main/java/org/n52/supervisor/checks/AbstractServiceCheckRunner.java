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

import org.n52.supervisor.SupervisorInit;
import org.n52.supervisor.SupervisorProperties;
import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckResult;
import org.n52.supervisor.api.CheckResult.ResultType;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.api.Notification;
import org.n52.supervisor.api.UnsupportedCheckException;
import org.n52.supervisor.db.ResultDatabase;
import org.n52.supervisor.notification.EmailNotification;
import org.n52.supervisor.notification.SendEmailTask;
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

    private final List<CheckResult> results = new ArrayList<CheckResult>();

    protected ServiceCheck check;

    private ResultDatabase rd;

    public AbstractServiceCheckRunner(final ServiceCheck check) {
        this.check = check;
    }

    @Override
    public void addResult(final CheckResult result) {
        results.add(result);
        log.debug("Result added: " + result);
    }

    public void clearResults() {
    	log.debug("Clearing {} results",results.size());
        results.clear();
    }

    protected ServiceCheckResult createNegativeResult(final String text) {
    	ResultType type = CheckResult.ResultType.NEGATIVE;
    	final ServiceCheckResult result = createResult(text, type);
        return result;
    }

    
    protected ServiceCheckResult createPositiveResult(final String text) {
    	ResultType type = CheckResult.ResultType.POSITIVE;
    	final ServiceCheckResult result = createResult(text, type);
        return result;
    }
    
    protected ServiceCheckResult createResult(final String text, ResultType type) {
		final ServiceCheckResult result = new ServiceCheckResult(
    			ID_GENERATOR.generate(),
    			check.getIdentifier(),
    			text,
    			new Date(),
    			type,
    			check.getServiceIdentifier());
		return result;
	}

    @Override
    public Check getCheck() {
        return check;
    }

    @Override
    public Collection<CheckResult> getResults() {
        return results;
    }

    @Override
    public void notifyFailure() {
        log.info("Check FAILED: {}", this);

        if (rd != null) {
			rd.appendResults(getResults());
		}

        if (check.getNotificationEmail() == null) {
            log.error("Can not notify via email, is null!");
        } else {
            final Collection<CheckResult> failures = new ArrayList<CheckResult>();
            for (final CheckResult r : results) {
                if (r.getType().equals(CheckResult.ResultType.NEGATIVE)) {
					failures.add(r);
				}
            }

            final Notification n = new EmailNotification(check, failures);
            SupervisorInit.appendNotification(n);
            SendEmailTask set = new SendEmailTask(
            		SupervisorProperties.instance().getAdminEmail(), rd);
            set.addNotification(n);
            set.run();
        }
    }

    @Override
    public void notifySuccess() {
        log.info("Check SUCCESSFUL: {}", this);

        if (rd != null) {
			rd.appendResults(getResults());
		}
    }

    protected boolean saveAndReturnNegativeResult(final String text) {
        final ServiceCheckResult r = createNegativeResult(text);
        addResult(r);
        return false;
    }

    @Override
    public void setCheck(final Check c) throws UnsupportedCheckException {
        if (c instanceof ServiceCheck) {
            final ServiceCheck sc = (ServiceCheck) c;
            check = sc;
        } else {
			throw new UnsupportedCheckException();
		}
    }

    @Override
    public void setResultDatabase(final ResultDatabase rd) {
        this.rd = rd;
    }

}
