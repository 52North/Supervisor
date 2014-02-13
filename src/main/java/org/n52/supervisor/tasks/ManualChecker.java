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

import org.n52.supervisor.api.CheckRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManualChecker extends Thread {

    private static Logger log = LoggerFactory.getLogger(ManualChecker.class);

    private Collection<CheckRunner> checkers;

    private boolean notify;

    public ManualChecker(Collection<CheckRunner> checkers, boolean notify) {
        this.checkers = checkers;
        this.notify = notify;

        log.debug("NEW {} with {} checkers", this, checkers.size());
    }

    @Override
    public void run() {
        for (CheckRunner checker : this.checkers) {
            try {
                log.debug("Running checker {} ...", checker);
                boolean b = checker.check();
                log.debug("Result: {} for {}", b, checker);

                if (this.notify) {
                    log.debug("Notify about result {}", b);
                    if ( !b) {
                        checker.notifyFailure();
                    }
                    else {
                        checker.notifySuccess();
                    }
                }
                else
                    log.info("Ran check manually, got result {} - not notifying!      Check: {}", b, checker);
            }
            catch (Exception e) {
                log.error("running a check.", e);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ManualChecker [");
        if (checkers != null) {
            builder.append("checkers=");
            builder.append(checkers);
            builder.append(", ");
        }
        builder.append("notify=");
        builder.append(notify);
        builder.append(", thread=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}