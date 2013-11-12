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
package org.n52.supervisor.ui;

import java.util.Arrays;
import java.util.Collection;

import org.n52.supervisor.ICheckResult;

/**
 * @author Daniel Nüst
 * 
 */
public class EmailNotification implements INotification {

    private Collection<ICheckResult> results;

    private String recipientEmail;

    private String serviceUrl;

    /**
     * 
     * @param results
     * @param recipientEmail
     * @param serviceURL
     */
    public EmailNotification(String serviceUrlP, String recipientEmailP, Collection<ICheckResult> resultsP) {
        this.results = resultsP;
        this.recipientEmail = recipientEmailP;
        this.serviceUrl = serviceUrlP;
    }

    /**
     * 
     * @return
     */
    public String getRecipientEmail() {
        return this.recipientEmail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.ui.INotification#getResults()
     */
    @Override
    public Collection<ICheckResult> getResults() {
        return this.results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.ui.INotification#getServiceUrl()
     */
    @Override
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "EmailNotification [recipient=" + this.recipientEmail + ", service=" + this.serviceUrl + ", results="
                + Arrays.toString(this.results.toArray());
    }

}
