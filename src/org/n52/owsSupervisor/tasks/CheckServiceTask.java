/**********************************************************************************
 Copyright (C) 2010
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk 
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under the
 terms of the GNU General Public License serviceVersion 2 as published by the Free
 Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with this 
 program (see gnu-gplv2.txt). If not, write to the Free Software Foundation, Inc., 
 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or visit the Free Software
 Foundation web page, http://www.fsf.org.
 
 Created on: 08.01.2010
 *********************************************************************************/

package org.n52.owsSupervisor.tasks;

import java.util.Collection;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.Supervisor;
import org.n52.owsSupervisor.checks.ICheckResult;
import org.n52.owsSupervisor.checks.IServiceChecker;

/**
 * 
 * Class for executing the method {@link ICatalog#pushAllDataToCatalog()}. Error
 * handling, logging and status updating are included.
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class CheckServiceTask extends TimerTask {

	private static Logger log = Logger.getLogger(CheckServiceTask.class);

	private String connectionID;

	private IServiceChecker checker;

	public CheckServiceTask(String connectionIDP, IServiceChecker checkerP) {
		this.connectionID = connectionIDP;
		this.checker = checkerP;
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

	@Override
	public boolean cancel() {
		log.info("Cancelling " + this);
		return super.cancel();
	}

	@Override
	protected void finalize() throws Throwable {
		if (log.isDebugEnabled())
			log.debug("Finalizing " + this);
		super.finalize();
	}

}
