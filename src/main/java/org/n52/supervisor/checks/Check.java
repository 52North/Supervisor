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

import javax.xml.bind.annotation.XmlRootElement;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * 
 * @author Daniel
 * 
 */
@XmlRootElement
public abstract class Check {

    private String identifier;

    private String notificationEmail;

    protected String type = "GenericCheck";

    @Inject
    @Named("supervisor.checks.defaultIntervalSeconds")
    private long intervalSeconds;

    public Check() {
        // required for jaxb
    }

    public Check(String identifier) {
        super();
        this.identifier = identifier;
    }
    
    public Check(String notificationEmail, long intervalSeconds) {
        this();
        this.notificationEmail = notificationEmail;
        this.intervalSeconds = intervalSeconds;
    }

    public Check(String identifier, String notificationEmail, long intervalSeconds) {
        this(identifier);
        this.notificationEmail = notificationEmail;
        this.intervalSeconds = intervalSeconds;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(long intervalMillis) {
        this.intervalSeconds = intervalMillis;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

}
