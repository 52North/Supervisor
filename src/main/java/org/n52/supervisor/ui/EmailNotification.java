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

    public EmailNotification(String serviceUrlP, String recipientEmailP, Collection<ICheckResult> resultsP) {
        this.results = resultsP;
        this.recipientEmail = recipientEmailP;
        this.serviceUrl = serviceUrlP;
    }

    public String getRecipientEmail() {
        return this.recipientEmail;
    }

    @Override
    public Collection<ICheckResult> getResults() {
        return this.results;
    }

    @Override
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EmailNotification [");
        if (results != null) {
            builder.append("results=");
            builder.append(results);
            builder.append(", ");
        }
        if (recipientEmail != null) {
            builder.append("recipientEmail=");
            builder.append(recipientEmail);
            builder.append(", ");
        }
        if (serviceUrl != null) {
            builder.append("serviceUrl=");
            builder.append(serviceUrl);
        }
        builder.append("]");
        return builder.toString();
    }

}
