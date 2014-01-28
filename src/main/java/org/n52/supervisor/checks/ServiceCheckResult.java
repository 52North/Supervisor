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

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Daniel Nüst
 * 
 */
@XmlRootElement
public class ServiceCheckResult extends CheckResult {

    private String serviceIdentifier;

    public ServiceCheckResult() {
        super();
        //
    }

    public ServiceCheckResult(Exception e, Check c, String message) {
        this(c.getIdentifier(), String.format("%s -- ERROR: %s occured for %s",
                                              message,
                                              e.getMessage(),
                                              c.getIdentifier()), new Date(), CheckResult.ResultType.NEGATIVE, null);
    }

    public ServiceCheckResult(Exception e, ServiceCheck c, String message) {
        this(c.getIdentifier(),
             String.format("%s : %s occured for %s @ %s [%s]",
                           message,
                           e.getMessage(),
                           c.getIdentifier(),
                           c.getServiceUrl(),
                           c.getType()),
             new Date(),
             CheckResult.ResultType.NEGATIVE,
             c.getServiceIdentifier());
    }

    public ServiceCheckResult(String checkIdentifier,
                              String result,
                              Date timeOfCheck,
                              ResultType type,
                              String serviceIdentifier) {
        super(checkIdentifier, result, timeOfCheck, type);
        this.serviceIdentifier = serviceIdentifier;
    }

    public String getServiceIdentifier() {
        return this.serviceIdentifier;
    }

    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ServiceCheckResult [serviceIdentifier=");
        builder.append(serviceIdentifier);
        builder.append("]");
        return builder.toString();
    }

}
