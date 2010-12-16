/*******************************************************************************
Copyright (C) 2010
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License version 2 as published by the 
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

Author: Daniel Nüst
 
 ******************************************************************************/
package org.n52.owsSupervisor.checkImpl;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.ICheckResult;
import org.n52.owsSupervisor.ICheckResult.ResultType;
import org.n52.owsSupervisor.IServiceChecker;
import org.n52.owsSupervisor.Supervisor;
import org.n52.owsSupervisor.SupervisorProperties;

/**
 * @author Daniel Nüst
 * 
 */
public abstract class AbstractServiceCheck implements IServiceChecker {

	private static Logger log = Logger.getLogger(AbstractServiceCheck.class);

	private List<ICheckResult> results = new ArrayList<ICheckResult>();

	protected URL serviceUrl;

	private String email = null;

	private long checkIntervalMillis = SupervisorProperties.getInstance()
			.getDefaultCheckIntervalMillis();

	public static final DateFormat ISO8601LocalFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:SS.SSS");

	/**
	 * 
	 * @param notifyEmail
	 */
	public AbstractServiceCheck(String notifyEmail) {
		this.email = notifyEmail;
	}

	/**
	 * 
	 * @param notifyEmail
	 * @param checkInterval
	 */
	public AbstractServiceCheck(String notifyEmail, long checkInterval) {
		this.email = notifyEmail;
		this.checkIntervalMillis = checkInterval;
	}

	@Override
	public String getService() {
		return this.serviceUrl.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.owsSupervisor.IServiceChecker#getResults()
	 */
	@Override
	public Collection<ICheckResult> getResults() {
		return this.results;
	}

	@Override
	public void addResult(ICheckResult r) {
		this.results.add(r);
	}

	@Override
	public void notifyFailure() {
		if (this.email == null) {
			log.error("Can not notify via email, is null!");
			return;
		}

		Collection<ICheckResult> failures = new ArrayList<ICheckResult>();
		for (ICheckResult r : this.results) {
			if (r.getType().equals(ResultType.NEGATIVE))
				failures.add(r);
		}

		// append for email notification to queue
		Supervisor.appendNotification(this.serviceUrl.toString(), this.email,
				failures);
		log.debug("Sent email with " + failures.size() + " failures.");
	}

	@Override
	public long getCheckIntervalMillis() {
		return this.checkIntervalMillis;
	}

	/**
	 * 
	 */
	public void clearResults() {
		if (log.isDebugEnabled()) {
			log.debug("Clearing " + this.results.size() + " results");
		}
		this.results.clear();
	}

}
