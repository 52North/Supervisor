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

package org.n52.supervisor.checks.ows;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.annotation.XmlRootElement;

import org.n52.supervisor.checks.ServiceCheck;

/**
 * 
 * @author Daniel
 * 
 */
@XmlRootElement
public class SosLatestObservationCheck extends ServiceCheck {

    private String offering;
    private String observedProperty;
    private String procedure;
    private long maximumAgeSeconds;

    public SosLatestObservationCheck() {
        super();
    }

    public SosLatestObservationCheck(String identifier) {
        super(identifier);
    }

    public SosLatestObservationCheck(String notificationEmail,
                                     long intervalSeconds,
                                     String serviceIdentifier,
                                     URL serviceUrl,
                                     String offering,
                                     String observedProperty,
                                     String procedure,
                                     long maximumAgeSeconds) {
        super(notificationEmail, intervalSeconds, serviceIdentifier, serviceUrl);

        this.offering = offering;
        this.observedProperty = observedProperty;
        this.procedure = procedure;
        this.maximumAgeSeconds = maximumAgeSeconds;
    }

    public SosLatestObservationCheck(String notificationEmail,
                                     String intervalSeconds,
                                     String serviceIdentifier,
                                     String serviceUrl,
                                     String offering,
                                     String observedProperty,
                                     String procedure,
                                     long maximumAgeSeconds) throws NumberFormatException, MalformedURLException {
        this(notificationEmail,
             Long.valueOf(intervalSeconds).longValue(),
             serviceIdentifier,
             new URL(serviceUrl),
             offering,
             observedProperty,
             procedure,
             Long.valueOf(maximumAgeSeconds).longValue());
    }

    public String getOffering() {
        return offering;
    }

    public void setOffering(String offering) {
        this.offering = offering;
    }

    public String getObservedProperty() {
        return observedProperty;
    }

    public void setObservedProperty(String observedProperty) {
        this.observedProperty = observedProperty;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public long getMaximumAgeSeconds() {
        return maximumAgeSeconds;
    }

    public void setMaximumAgeSeconds(long maximumAgeSeconds) {
        this.maximumAgeSeconds = maximumAgeSeconds;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SosLatestObservationCheck [offering=");
        builder.append(offering);
        builder.append(", observedProperty=");
        builder.append(observedProperty);
        builder.append(", procedure=");
        builder.append(procedure);
        builder.append(", maximumAgeSeconds=");
        builder.append(maximumAgeSeconds);
        builder.append("]");
        return builder.toString();
    }

}
