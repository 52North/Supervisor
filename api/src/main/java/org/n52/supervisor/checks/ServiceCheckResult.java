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

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckResult;

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

    public ServiceCheckResult(final String identifier, final Exception e, final Check c, final String message) {
        this(identifier,c.getIdentifier(), String.format("%s -- ERROR: %s occured for %s",
                                              message,
                                              e.getMessage(),
                                              c.getIdentifier()), new Date(), CheckResult.ResultType.NEGATIVE, null);
    }

    public ServiceCheckResult(final String identifier, final Exception e, final ServiceCheck c, final String message) {
        this(identifier,
        	 c.getIdentifier(),
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

    public ServiceCheckResult(final String identifier,
    							final String checkIdentifier,
    							final String result,
    							final Date timeOfCheck,
    							final ResultType type,
    							final String serviceIdentifier) {
        super(identifier, checkIdentifier, result, timeOfCheck, type);
        this.serviceIdentifier = serviceIdentifier;
    }

    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    public void setServiceIdentifier(final String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ServiceCheckResult [serviceIdentifier=");
        builder.append(serviceIdentifier);
        builder.append("]");
        return builder.toString();
    }

}
