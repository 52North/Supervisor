/**
 * ﻿Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
