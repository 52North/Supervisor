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
package org.n52.supervisor.api;

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

    /**
     * Do NOT use any HTML code here because the source code will be rendered!
     */
    private String result;

    private Date checkTime;

    private ResultType type;

	private String identifier;

    public CheckResult() {
        // required for jaxb
    }

    public CheckResult(final String identifier) {
    	super();
    	this.identifier = identifier;
    }

    public CheckResult(final String identifier, final String checkIdentifier, final String result, final Date timeOfCheck, final ResultType type) {
        super();
        this.identifier = identifier;
        this.checkIdentifier = checkIdentifier;
        this.result = result;
        checkTime = timeOfCheck;
        this.type = type;
    }

    public String getCheckIdentifier() {
        return checkIdentifier;
    }

    public String getResult() {
        return result;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public ResultType getType() {
        return type;
    }

    public String getIdentifier() {
    	return identifier;
    }

    public void setIdentifier(final String identifier) {
    	this.identifier = identifier;
    }

    public void setCheckIdentifier(final String checkIdentifier) {
        this.checkIdentifier = checkIdentifier;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    public void setCheckTime(final Date checkTime) {
        this.checkTime = checkTime;
    }

    public void setType(final ResultType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
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
        if (checkTime != null) {
            builder.append("checkTime=");
            builder.append(checkTime);
            builder.append(", ");
        }
        if (type != null) {
            builder.append("type=");
            builder.append(type);
        }
        builder.append("]");
        return builder.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((checkIdentifier == null) ? 0 : checkIdentifier.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + ((checkTime == null) ? 0 : checkTime.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CheckResult)) {
			return false;
		}
		final CheckResult other = (CheckResult) obj;
		if (checkIdentifier == null) {
			if (other.checkIdentifier != null) {
				return false;
			}
		} else if (!checkIdentifier.equals(other.checkIdentifier)) {
			return false;
		}
		if (result == null) {
			if (other.result != null) {
				return false;
			}
		} else if (!result.equals(other.result)) {
			return false;
		}
		if (checkTime == null) {
			if (other.checkTime != null) {
				return false;
			}
		} else if (!checkTime.equals(other.checkTime)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

}
