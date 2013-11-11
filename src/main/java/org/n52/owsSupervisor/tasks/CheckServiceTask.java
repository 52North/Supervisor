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
package org.n52.owsSupervisor.tasks;

import java.util.Collection;
import java.util.TimerTask;

import org.n52.owsSupervisor.ICheckResult;
import org.n52.owsSupervisor.IServiceChecker;
import org.n52.owsSupervisor.Supervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Class for executing the method {@link ICatalog#pushAllDataToCatalog()}. Error
 * handling, logging and status updating are included.
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class CheckServiceTask extends TimerTask {

	private static Logger log = LoggerFactory.getLogger(CheckServiceTask.class);

	private IServiceChecker checker;

	private String connectionID;

	/**
	 * 
	 * @param connectionIDP
	 * @param checkerP
	 */
	public CheckServiceTask(String connectionIDP, IServiceChecker checkerP) {
		this.connectionID = connectionIDP;
		this.checker = checkerP;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.TimerTask#cancel()
	 */
	@Override
	public boolean cancel() {
		log.info("Cancelling " + this);
		return super.cancel();
	}

	/**
	 * 
	 * @param c
	 * @return
	 */
	private Collection<ICheckResult> checkIt(IServiceChecker c) {
		boolean b = c.check();
		if (!b) {
			c.notifyFailure();
		}
		else {
		    c.notifySuccess();
		}

		return c.getResults();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		if (log.isDebugEnabled())
			log.debug("Finalizing " + this);
		super.finalize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		log.info("*** Run check " + this.checker);

		Collection<ICheckResult> currentResults = checkIt(this.checker);
		Supervisor.appendLatestResults(currentResults);
		
		log.info("*** Ran check, got " + currentResults.size() + " results.");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ServiceCheckTask [connectionID=");
		sb.append(this.connectionID);
		sb.append(", checker=");
		sb.append(this.checker);
		sb.append("]");
		return sb.toString();
	}

}
