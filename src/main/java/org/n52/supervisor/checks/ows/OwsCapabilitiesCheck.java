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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel
 * 
 */
@XmlRootElement
public class OwsCapabilitiesCheck extends ServiceCheck {

    protected static final String DEFAULT_OWS_VERSION = "1.1";

    private static Logger log = LoggerFactory.getLogger(OwsCapabilitiesCheck.class);

    private String owsVersion = DEFAULT_OWS_VERSION;

    private String serviceVersion;

    private String serviceType;

    protected String type = "OwsCapabilitiesCheck";

    public OwsCapabilitiesCheck() {
        super();
    }

    public OwsCapabilitiesCheck(String identifier) {
        super(identifier);
    }

    public OwsCapabilitiesCheck(String notificationEmail,
                                long intervalSeconds,
                                String serviceIdentifier,
                                URL serviceUrl,
                                String owsVersion,
                                String serviceVersion,
                                String serviceType) {
        super(notificationEmail, intervalSeconds, serviceIdentifier, serviceUrl);
        this.owsVersion = owsVersion;
        this.serviceVersion = serviceVersion;
        this.serviceType = serviceType;

        log.info("NEW {}", this);
    }

    public OwsCapabilitiesCheck(String notificationEmail,
                                String intervalSeconds,
                                String serviceIdentifier,
                                String serviceUrl,
                                String owsVersion,
                                String serviceVersion,
                                String serviceType) throws NumberFormatException, MalformedURLException {
        this(notificationEmail,
             Long.valueOf(intervalSeconds).longValue(),
             serviceIdentifier,
             new URL(serviceUrl),
             owsVersion,
             serviceVersion,
             serviceType);
    }

    public String getOwsVersion() {
        return owsVersion;
    }

    public void setOwsVersion(String owsVersion) {
        this.owsVersion = owsVersion;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OwsCapabilitiesCheck [");
        if (owsVersion != null) {
            builder.append("owsVersion=");
            builder.append(owsVersion);
            builder.append(", ");
        }
        if (serviceVersion != null) {
            builder.append("serviceVersion=");
            builder.append(serviceVersion);
            builder.append(", ");
        }
        if (serviceType != null) {
            builder.append("serviceType=");
            builder.append(serviceType);
            builder.append(", ");
        }
        if (type != null) {
            builder.append("type=");
            builder.append(type);
        }
        builder.append("]");
        return builder.toString();
    }

}
