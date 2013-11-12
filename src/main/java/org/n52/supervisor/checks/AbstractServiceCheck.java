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

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.n52.supervisor.ICheckResult;
import org.n52.supervisor.IServiceChecker;
import org.n52.supervisor.Supervisor;
import org.n52.supervisor.SupervisorProperties;
import org.n52.supervisor.ICheckResult.ResultType;
import org.n52.supervisor.ui.EmailNotification;
import org.n52.supervisor.util.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
public abstract class AbstractServiceCheck implements IServiceChecker {

    public static final DateFormat ISO8601LocalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS.SSS");

    private static Logger log = LoggerFactory.getLogger(AbstractServiceCheck.class);

    private long checkIntervalMillis = SupervisorProperties.getInstance().getDefaultCheckIntervalMillis();

    protected Client client = new Client();

    private String email = null;

    private List<ICheckResult> results = new ArrayList<ICheckResult>();

    private URL serviceURL = null;

    private String identifier;
    
    public AbstractServiceCheck() {
        // required for jaxb binding
    }

    public AbstractServiceCheck(String notifyEmail, URL serviceURL) {
        this.email = notifyEmail;
        this.serviceURL = serviceURL;
    }

    public AbstractServiceCheck(String notifyEmail, URL serviceURL, long checkInterval) {
        this(notifyEmail, serviceURL);
        this.checkIntervalMillis = checkInterval;
    }

    public AbstractServiceCheck(String identifier, String notifyEmail, URL serviceURL, long checkInterval) {
        this(notifyEmail, serviceURL, checkInterval);
        this.identifier = identifier;
    }

    @Override
    public void addResult(ICheckResult r) {
        log.info("Result added: " + r);

        if (r.getType().equals(ResultType.NEGATIVE))
            log.warn("NEGATIVE result added to " + toString() + ":\n\n" + r + "\n");

        this.results.add(r);
    }

    public void clearResults() {
        if (log.isDebugEnabled()) {
            log.debug("Clearing " + this.results.size() + " results");
        }
        this.results.clear();
    }

    @Override
    public long getCheckIntervalMillis() {
        return this.checkIntervalMillis;
    }

    public String getEmail() {
        return this.email;
    }

    @Override
    public Collection<ICheckResult> getResults() {
        return this.results;
    }

    @Override
    public String getService() {
        return this.serviceURL.toString();
    }

    public URL getServiceURL() {
        return this.serviceURL;
    }

    @Override
    public void notifyFailure() {
        if (log.isDebugEnabled())
            log.debug("Check FAILED: " + this);

        if (this.email == null) {
            log.error("Can not notify via email, is null!");
            return;
        }

        Collection<ICheckResult> failures = new ArrayList<ICheckResult>();
        for (ICheckResult r : this.results) {
            if (r.getType().equals(ResultType.NEGATIVE))
                failures.add(r);
        }

        // append for email notification to queue
        Supervisor.appendNotification(new EmailNotification(this.serviceURL.toString(), this.email, failures));

        if (log.isDebugEnabled())
            log.debug("Submitted email with " + failures.size() + " failures.");
    }

    @Override
    public void notifySuccess() {
        log.info("Check SUCCESSFUL:" + this);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}