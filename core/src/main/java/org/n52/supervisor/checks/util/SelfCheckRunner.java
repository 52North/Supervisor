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
package org.n52.supervisor.checks.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckResult;
import org.n52.supervisor.api.Notification;
import org.n52.supervisor.api.UnsupportedCheckException;
import org.n52.supervisor.api.CheckResult.ResultType;
import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.n52.supervisor.notification.EmailNotification;
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

	private Date lastNotificationTime = new Date();

    public SelfCheckRunner(final SelfCheck check) {
        super(check);
    }

    @Override
    public boolean check() {
        // check if everything is running fine...
        final long heapSize = Runtime.getRuntime().totalMemory();
        final long heapMaxSize = Runtime.getRuntime().maxMemory();
        final long heapFreeSize = Runtime.getRuntime().freeMemory();

        final StringBuilder sb = new StringBuilder();

        sb.append("Self check ran succesfully, service is most probably up and running. ");
        sb.append("Service URL: '");
        sb.append(check.getServiceUrl());
        sb.append(".");
        sb.append(" *** Heap Info: Size (Mb) is ");
        sb.append(heapSize / L1024_2);
        sb.append(" of ");
        sb.append(heapMaxSize / L1024_2);
        sb.append(" leaving ");
        sb.append(heapFreeSize / L1024_2);
        sb.append(".");

        // TODO add currently running tasks and their last message
        final CheckResult result = new SelfCheckResult(ID_GENERATOR.generate(),check.getIdentifier(), sb.toString(), new Date(), ResultType.POSITIVE);
        addResult(result);

        return true;
    }

    @Override
    public void notifyFailure() {
        log.error("SelfChecker cannot fail!");
    }

    @Override
    public void notifySuccess() {
        log.debug("Check SUCCESSFUL: {}", this);
        
        List<CheckResult> results = new ArrayList<>(super.getLatestDatabaseResults());
        
        Collections.sort(results, new Comparator<CheckResult>() {
			@Override
			public int compare(CheckResult o1, CheckResult o2) {
				if (o1 == null && o2 == null ) {
					return 0;
				}
				
				if (o1 == null) {
					return -1;
				}
				
				if (o2 == null) {
					return 1;
				}
				
				return o1.getCheckTime().compareTo(o2.getCheckTime());
			}
		});
        
        List<CheckResult> toRemove = new ArrayList<>();
        for (CheckResult checkResult : results) {
			if (checkResult.getCheckTime().before(this.lastNotificationTime)) {
				toRemove.add(checkResult);
			}
		}
        results.removeAll(toRemove);
        
        this.lastNotificationTime = new Date();
        
        if (check.getNotificationEmail() == null) {
			log.error("Can not notify via email, is null!");
		} else {
            final Notification n = new EmailNotification(check, results);
            super.submitNotification(n);
            log.debug("Submitted email with {} successes.", results.size());
        }

    }

    @Override
    public void setCheck(final Check c) throws UnsupportedCheckException {
        if (c instanceof SelfCheck) {
            final SelfCheck sc = (SelfCheck) c;
            check = sc;
        } else {
			throw new UnsupportedCheckException();
		}
    }


}
