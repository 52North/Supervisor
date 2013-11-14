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
public abstract class CheckResult {

    public static enum ResultType {
        NEGATIVE, NEUTRAL, POSITIVE
    }

    private String checkIdentifier;

    private String result;

    private Date timeOfCheck;

    private ResultType type;

    public CheckResult() {
        // required for jaxb
    }

    public CheckResult(String checkIdentifier, String result, Date timeOfCheck, ResultType type) {
        super();
        this.checkIdentifier = checkIdentifier;
        this.result = result;
        this.timeOfCheck = timeOfCheck;
        this.type = type;
    }

    public String getCheckIdentifier() {
        return checkIdentifier;
    }

    public String getResult() {
        return result;
    }

    public Date getTimeOfCheck() {
        return timeOfCheck;
    }

    public ResultType getType() {
        return type;
    }

    public void setCheckIdentifier(String checkIdentifier) {
        this.checkIdentifier = checkIdentifier;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setTimeOfCheck(Date timeOfCheck) {
        this.timeOfCheck = timeOfCheck;
    }

    public void setType(ResultType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CheckResult [");
        if (checkIdentifier != null) {
            builder.append("checkIdentifier=");
            builder.append(checkIdentifier);
            builder.append(", ");
        }
        if (result != null) {
            builder.append("result=");
            builder.append(result);
            builder.append(", ");
        }
        if (timeOfCheck != null) {
            builder.append("timeOfCheck=");
            builder.append(timeOfCheck);
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
