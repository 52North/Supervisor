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

import org.n52.supervisor.ICheckResult;

/**
 * 
 * @author Daniel Nüst
 *
 */
public class CheckResult implements ICheckResult {

	private String checkIdentifier;
	
	private String result;
	
	private Date timeOfCheck;
	
	private ResultType type;

	/**
	 * 
	 * @param checkIdentifierP
	 * @param resultP
	 * @param typeP
	 */
	public CheckResult(String checkIdentifierP, String resultP, ResultType typeP) {
		this.checkIdentifier = checkIdentifierP;
		this.result = resultP;
		this.type = typeP;
		this.timeOfCheck = new Date();
	}

	/* (non-Javadoc)
	 * @see org.n52.owsSupervisor.checks.ICheckResult#getCheckIdentifier()
	 */
	@Override
	public String getCheckIdentifier() {
		return this.checkIdentifier;
	}

	/* (non-Javadoc)
	 * @see org.n52.owsSupervisor.checks.ICheckResult#getResult()
	 */
	@Override
	public String getResult() {
		return this.result;
	}

	/* (non-Javadoc)
	 * @see org.n52.owsSupervisor.checks.ICheckResult#getTimeOfCheck()
	 */
	@Override
	public Date getTimeOfCheck() {
		return this.timeOfCheck;
	}

	/* (non-Javadoc)
	 * @see org.n52.owsSupervisor.checks.ICheckResult#getType()
	 */
	@Override
	public ResultType getType() {
		return this.type;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    // TODO Auto-generated method stub
	    return "CheckResult [identifier=" + this.checkIdentifier + ", type=" + this.type + ", time=" + this.timeOfCheck + ", result=" + this.result + "]";
	}

}
