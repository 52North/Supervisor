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
 
 Created on: 12.01.2010
 *********************************************************************************/

package org.n52.owsSupervisor.tasks;

import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.IServiceChecker;
import org.n52.owsSupervisor.SupervisorProperties;

/**
 * 
 * Class encapsulates a {@link TaskServlet} where tasks are forwared to.
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class JobSchedulerImpl implements IJobScheduler {

	private static final long DEFAULT_DELAY_MILLISECS = 10;

	private static Logger log = Logger.getLogger(JobSchedulerImpl.class);

	private TaskServlet timerServlet;

	/**
	 * 
	 * @param timer
	 */
	protected JobSchedulerImpl(TaskServlet timer) {
		this.timerServlet = timer;
		log.info("NEW " + this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.n52.owsSupervisor.tasks.IJobScheduler#cancel(java.lang.String)
	 */
	@Override
	public void cancel(String identifier) {
		if (log.isDebugEnabled()) {
			log.debug("Cancelling Task: " + identifier + ".");
		}
		this.timerServlet.cancel(identifier);
	}

	/*
	 * (non-Javadoc)
	 * @see org.n52.owsSupervisor.tasks.IJobScheduler#submit(org.n52.owsSupervisor.checks.IServiceChecker)
	 */
	@Override
	public String submit(IServiceChecker checker) {
		return submit(checker, DEFAULT_DELAY_MILLISECS);
	}

	/**
	 * 
	 * @param checker
	 * @param delay
	 * @return
	 */
	private String submit(IServiceChecker checker, long delay) {
		log.debug("Added " + checker);
		
		// schedule periodic push
		String id = SupervisorProperties.getInstance().getUUID();
		submitRepeating(id, new CheckServiceTask(id, checker), delay,
				checker.getCheckIntervalMillis());
		
		return id;
	}

	/**
	 * 
	 * @param identifier
	 * @param task
	 * @param delay
	 * @param period
	 */
	private void submitRepeating(String identifier, TimerTask task, long delay,
			long period) {
		if (log.isDebugEnabled()) {
			log.debug("Scheduling Task: " + task
					+ " for execution now and with period of " + period
					+ "ms after a delay of " + delay + "ms.");
		}
		this.timerServlet.submit(identifier, task, delay, period);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("JobSchedulerImpl [default delay (msecs) (ALWAYS applied!)=");
		sb.append(DEFAULT_DELAY_MILLISECS);
		sb.append(", internal task handler: ");
		sb.append(this.timerServlet);
		sb.append("]");
		return sb.toString();
	}
}