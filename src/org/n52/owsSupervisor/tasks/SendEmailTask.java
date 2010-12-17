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
package org.n52.owsSupervisor.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimerTask;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.Supervisor;
import org.n52.owsSupervisor.SupervisorProperties;
import org.n52.owsSupervisor.checks.CheckResult;
import org.n52.owsSupervisor.checks.ICheckResult;
import org.n52.owsSupervisor.checks.ICheckResult.ResultType;
import org.n52.owsSupervisor.ui.EmailFailureNotification;
import org.n52.owsSupervisor.ui.IFailureNotification;

/**
 * @author Daniel Nüst
 * 
 */
public class SendEmailTask extends TimerTask {

	private static final String EMAIL_CONTENT_ENCODING = "text/plain";

	private static final String RESULT_IDENTIFIER = "Send Email Task";

	private Collection<IFailureNotification> notifications;

	private static Logger log = Logger.getLogger(SendEmailTask.class);

	private String adminEmail = null;

	/**
	 * 
	 * @param notifications2
	 */
	public SendEmailTask(Collection<IFailureNotification> notificationsP) {
		this.notifications = notificationsP;
		log.info("NEW " + this.toString());
	}

	/**
	 * 
	 * @param adminEmail
	 * @param notificationsP
	 */
	public SendEmailTask(String adminEmailP,
			Collection<IFailureNotification> notificationsP) {
		this.notifications = notificationsP;
		this.adminEmail = adminEmailP;
		log.info("NEW " + this.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		if (this.notifications.size() < 1) {
			log.debug("No notifications. Yay!");
			// long heapSize = Runtime.getRuntime().totalMemory();
			// long heapMaxSize = Runtime.getRuntime().maxMemory();
			// long heapFreeSize = Runtime.getRuntime().freeMemory();
			// System.out.println("Size is " + heapSize/1024 + " of " +
			// heapMaxSize/1024 + " leaving " + heapFreeSize/1024 + ".");
			// ICheckResult result = new CheckResultImpl("Send Email Task",
			// "No notifactions.", ResultType.POSITIVE);
			// Supervisor.appendLatestResult(result);
			return;
		}

		try {
			doTask();
		} catch (Error e) {
			log.error("Error fulfilling task");
			if (this.adminEmail != null) {
				try {
					sendEmail(this.adminEmail, "ERROR: " + e.getMessage(), 0);
				} catch (MessagingException e1) {
					log.error("Could not send email on error!");
				}
			}
			throw e;
		}
	}

	private void doTask() {
		log.info("*** Sending emails based on " + this.notifications.size()
				+ " notifications.");

		// collect all notifications for one email address
		Map<String, Collection<EmailFailureNotification>> emails = new HashMap<String, Collection<EmailFailureNotification>>();

		for (IFailureNotification iMsg : this.notifications) {
			if (iMsg instanceof EmailFailureNotification) {
				EmailFailureNotification msg = (EmailFailureNotification) iMsg;
				if (emails.containsKey(msg.getRecipientEmail())) {
					// add to failure list
					Collection<EmailFailureNotification> failures = emails
							.get(msg.getRecipientEmail());
					failures.add(msg);
				} else {
					// create new email
					ArrayList<EmailFailureNotification> failures = new ArrayList<EmailFailureNotification>();
					failures.add(msg);
					emails.put(msg.getRecipientEmail(), failures);
				}
			}
			// not an email notification
		}

		// send emails
		int overallFailureCounter = 0;
		int overallEmailCounter = 0;
		for (Entry<String, Collection<EmailFailureNotification>> email : emails
				.entrySet()) {
			// create message
			StringBuilder sb = new StringBuilder();
			sb.append("Attention on deck!\n\nFailed check(s) occured while testing.\n\n");

			int failureCount = 0;
			for (EmailFailureNotification failure : email.getValue()) {
				for (ICheckResult f : failure.getCheckResults()) {
					sb.append("\n");
					sb.append(f.toString());
				}

				failureCount += failure.getCheckResults().size();
			}

			sb.append("\n\n\nGood Luck fixing it!");

			try {
				sendEmail(email.getKey(), sb.toString(), failureCount);

				overallEmailCounter++;
				overallFailureCounter += failureCount;

				ICheckResult result = new CheckResult(RESULT_IDENTIFIER,
						"Sent " + overallEmailCounter + " email(s) with "
								+ overallFailureCounter + " failure(s).",
						ResultType.NEUTRAL);
				Supervisor.appendLatestResult(result);

				// all went ok, clear notifications
				Supervisor.clearNotifications();
			} catch (MessagingException e) {
				log.error("Could not send email to " + email.getKey(), e);

				ICheckResult result = new CheckResult(RESULT_IDENTIFIER,
						"FAILED to send email to " + email.getKey() + ": "
								+ e.getMessage(), ResultType.NEGATIVE);
				Supervisor.appendLatestResult(result);
			}
		}
	}

	/**
	 * 
	 * @param recipient
	 * @param failures
	 * @throws MessagingException
	 */
	protected void sendEmail(String recipient, String messageText,
			int failureCount) throws MessagingException {
		SupervisorProperties sp = SupervisorProperties.getInstance();

		// send it
		if (sp.getSendEmails()) {
			Properties mailProps = SupervisorProperties.getInstance()
					.getMailSessionProperties();
			Session mailSession = Session.getDefaultInstance(mailProps,
					new PropertyAuthenticator(mailProps));
			Transport transport = mailSession.getTransport();

			MimeMessage message = new MimeMessage(mailSession);
			if (failureCount > 1)
				message.setSubject("[OwsSupervisor] " + failureCount
						+ " checks failed");
			else
				message.setSubject("[OwsSupervisor] " + failureCount
						+ " check failed");
			message.setContent(messageText, EMAIL_CONTENT_ENCODING);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					recipient));
			message.setSender(sp.getEmailSender());

			transport.connect();
			transport.sendMessage(message,
					message.getRecipients(Message.RecipientType.TO));
			transport.close();

			log.debug("Sent email to " + recipient + " with " + failureCount
					+ " failure(s).");
		} else {
			log.warn("Not sending Email (disabled in properties!)");
			log.debug("Email Content: " + messageText);
		}
	}

	/**
	 * 
	 * @author Daniel Nüst
	 * 
	 */
	private class PropertyAuthenticator extends Authenticator {
		private Properties p;

		public PropertyAuthenticator(Properties sp) {
			this.p = sp;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			String userName = this.p
					.getProperty(SupervisorProperties.MAIL_USER_PROPERTY);
			String password = this.p
					.getProperty(SupervisorProperties.MAIL_PASSWORD_PROPERTY);
			return new PasswordAuthentication(userName, password);
		}
	}

}
