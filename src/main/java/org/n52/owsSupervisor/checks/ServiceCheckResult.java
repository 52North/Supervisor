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
package org.n52.owsSupervisor.checks;

import java.util.Date;

import org.n52.owsSupervisor.ICheckResult;

/**
 * @author Daniel Nüst
 * 
 */
public class ServiceCheckResult implements ICheckResult {

	private String result;

	private String serviceIdentifier;

	private Date time;

	private ResultType type;

	/**
	 * 
	 * @param timeP
	 * @param serviceP
	 * @param resultP
	 * @param typeP
	 */
	public ServiceCheckResult(Date timeP, String serviceP, String resultP,
			ResultType typeP) {
		this.time = timeP;
		this.serviceIdentifier = serviceP;
		this.result = resultP;
		this.type = typeP;
	}

	/**
	 * 
	 * @param serviceP
	 * @param resultP
	 * @param typeP
	 */
	public ServiceCheckResult(String serviceP, String resultP, ResultType typeP) {
		this(new Date(), serviceP, resultP, typeP);
	}

	/*
	 * (non-Javadoc)
	 * @see org.n52.owsSupervisor.ICheckResult#getCheckIdentifier()
	 */
	@Override
	public String getCheckIdentifier() {
		return getServiceIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.owsSupervisor.ICheckResult#getResult()
	 */
	@Override
	public String getResult() {
		return this.result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.owsSupervisor.ICheckResult#getServiceIdentifier()
	 */
	public String getServiceIdentifier() {
		return this.serviceIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.owsSupervisor.ICheckResult#getTimeOfCheck()
	 */
	@Override
	public Date getTimeOfCheck() {
		return this.time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.owsSupervisor.ICheckResult#getType()
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
		return "[" + getType().name() + "] " + this.time + ": "
				+ this.serviceIdentifier + " - " + this.result;
	}

}
