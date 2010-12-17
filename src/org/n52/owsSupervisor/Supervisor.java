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
package org.n52.owsSupervisor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.checks.ICheckResult;
import org.n52.owsSupervisor.checks.IServiceChecker;
import org.n52.owsSupervisor.data.SWSL;
import org.n52.owsSupervisor.tasks.IJobScheduler;
import org.n52.owsSupervisor.tasks.SendEmailTask;
import org.n52.owsSupervisor.tasks.TaskServlet;
import org.n52.owsSupervisor.ui.IFailureNotification;

/**
 * @author Daniel Nüst
 * 
 */
public class Supervisor extends GenericServlet {

	private static final long serialVersionUID = -4629591718212281703L;

	private static final String CONFIG_FILE_INIT_PARAMETER = "configFile";

	private static final String EMAIL_SENDER_TASK_ID = "EmailSenderTask";

	private static Logger log = Logger.getLogger(Supervisor.class);

	private Collection<IServiceChecker> checkers;

	private static Queue<ICheckResult> latestResults;

	private static Queue<IFailureNotification> notifications;

	private IJobScheduler scheduler;

	/**
	 * 
	 */
	public Supervisor() {
		log.info("*** NEW " + this + " ***");
	}

	@Override
	public void init() throws ServletException {
		// get ServletContext
		ServletContext context = getServletContext();

		String basepath = context.getRealPath("/");
		InputStream configStream = context
				.getResourceAsStream(getInitParameter(CONFIG_FILE_INIT_PARAMETER));

		// initialize property manager
		SupervisorProperties sp = SupervisorProperties.getInstance(
				configStream, basepath);

		// initialize lists
		this.checkers = new ArrayList<IServiceChecker>();
		latestResults = new LinkedBlockingQueue<ICheckResult>(
				SupervisorProperties.getInstance().getMaximumResults());
		notifications = new LinkedBlockingQueue<IFailureNotification>();

		// init timer servlet
		TaskServlet timerServlet = (TaskServlet) context
				.getAttribute(TaskServlet.NAME_IN_CONTEXT);
		this.scheduler = sp.getScheduler(timerServlet);

		// initialize checkers
		initCheckers();

		// add task for email notifications, with out without admin email
		String adminEmail = sp.getAdminEmail();
		if (adminEmail.contains("@ADMIN_EMAIL@")) {
			timerServlet.submit(EMAIL_SENDER_TASK_ID, new SendEmailTask(
					notifications), sp.getEmailSendPeriodMins(), sp
					.getEmailSendPeriodMins());
		} else {
			log.info("Found admin email address for send email task.");
			timerServlet.submit(EMAIL_SENDER_TASK_ID, new SendEmailTask(
					adminEmail, notifications), sp.getEmailSendPeriodMins(), sp
					.getEmailSendPeriodMins());
		}

		log.info("*** INITIALIZED SUPERVISOR ***");
	}

	private void initCheckers() {
		this.checkers = SWSL.checkers;

		for (IServiceChecker c : this.checkers) {
			this.scheduler.submit(c);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#service(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse)
	 */
	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		log.fatal("'service' method is not supported. ServletRequest: " + arg0);
	}

	@Override
	public void destroy() {
		super.destroy();
		log.info("Destroy " + this.toString());
		this.checkers.clear();
		this.checkers = null;
		latestResults.clear();
		latestResults = null;
		notifications.clear();
		notifications = null;
	}

	/**
	 * 
	 * @return
	 */
	public static Collection<ICheckResult> getLatestResults() {
		return new ArrayList<ICheckResult>(latestResults);
	}

	/**
	 * 
	 * @param results
	 */
	public static void appendLatestResults(Collection<ICheckResult> results) {
		if (latestResults.size() >= SupervisorProperties.getInstance()
				.getMaximumResults()) {
			log.debug("Too many results. Got " + results.size() + " new and "
					+ latestResults.size() + " existing.");
			for (int i = 0; i < Math.min(results.size(), latestResults.size()); i++) {
				// remove the first element so many times that the new results
				// fit.
				latestResults.remove();
			}
		}

		latestResults.addAll(results);
	}

	/**
	 * 
	 * @param result
	 */
	public static void appendLatestResult(ICheckResult result) {
		latestResults.add(result);
	}

	/**
	 * 
	 * @param results
	 */
	public static void appendNotification(IFailureNotification notification) {
		notifications.add(notification);
	}

	/**
	 * 
	 */
	public static void clearNotifications() {
		log.info("Clearing notifications!");
		notifications.clear();
	}

}