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
package org.n52.supervisor.checks;

import java.net.URL;

import javax.xml.bind.annotation.XmlRootElement;

import org.n52.supervisor.api.Check;

/**
 * 
 * @author Daniel
 * 
 */
@XmlRootElement
public class ServiceCheck extends Check {

    private String serviceIdentifier;

    private URL serviceUrl;

    public ServiceCheck() {
        super();
        this.type = "ServiceCheck";
    }

    public ServiceCheck(String identifier) {
        super(identifier);
        this.type = "ServiceCheck";
    }

    public ServiceCheck(String notificationEmail,
                        long intervalSeconds,
                        String serviceIdentifier,
                        URL serviceUrl) {
        super(notificationEmail, intervalSeconds);
        this.serviceIdentifier = serviceIdentifier;
        this.serviceUrl = serviceUrl;
    }

    public ServiceCheck(String notificationEmail, long intervalSeconds, URL serviceUrl) {
        super(notificationEmail, intervalSeconds);
        this.serviceIdentifier = serviceUrl.toString();
        this.serviceUrl = serviceUrl;
    }

    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    public URL getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceIdentifier(String service) {
        this.serviceIdentifier = service;
    }

    public void setServiceUrl(URL serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

}
