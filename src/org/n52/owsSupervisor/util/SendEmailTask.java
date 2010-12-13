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
package org.n52.owsSupervisor.util;

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
import org.n52.owsSupervisor.ICheckResult;
import org.n52.owsSupervisor.Supervisor;
import org.n52.owsSupervisor.SupervisorProperties;

/**
 * @author Daniel Nüst
 * 
 */
public class SendEmailTask extends TimerTask {

	private static final String EMAIL_CONTENT_ENCODING = "text/plain";

	private Collection<FailureNotificationElement> notifications;

	private static Logger log = Logger.getLogger(SendEmailTask.class);

	/**
	 * 
	 */
	public SendEmailTask(Collection<FailureNotificationElement> notificationsP) {
		this.notifications = notificationsP;
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
			return;
		}

		log.info("*** Sending emails based on " + this.notifications.size()
				+ " notifications.");

		// collect all notifications for one email address
		Map<String, Collection<FailureNotificationElement>> emails = new HashMap<String, Collection<FailureNotificationElement>>();

		for (FailureNotificationElement msg : this.notifications) {
			if (emails.containsKey(msg.getRecipientEmail())) {
				// add to failure list
				Collection<FailureNotificationElement> failures = emails
						.get(msg.getRecipientEmail());
				failures.add(msg);
			} else {
				// create new email
				ArrayList<FailureNotificationElement> failures = new ArrayList<FailureNotificationElement>();
				failures.add(msg);
				emails.put(msg.getRecipientEmail(), failures);
			}
		}

		// send emails
		for (Entry<String, Collection<FailureNotificationElement>> email : emails
				.entrySet()) {
			// create message
			StringBuilder sb = new StringBuilder();
			sb.append("Attention on deck!\n\nFailed check(s) occured while testing.\n\n");

			int failureCount = 0;
			for (FailureNotificationElement failure : email.getValue()) {
				for (ICheckResult f : failure.getCheckResults()) {
					sb.append("\n");
					sb.append(f.toString());
				}

				failureCount += failure.getCheckResults().size();
			}

			sb.append("\n\n\nGood Luck fixing it!");

			try {
				sendEmail(email.getKey(), sb.toString(), failureCount);
			} catch (MessagingException e) {
				log.error("Could not send email to " + email.getKey(), e);
			}
		}

		Supervisor.clearNotifications();
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
			message.setSubject("[OwsSupervisor] Checks failed");
			message.setContent(messageText, EMAIL_CONTENT_ENCODING);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					recipient));
			message.setSender(sp.getEmailSender());

			transport.connect();
			transport.sendMessage(message,
					message.getRecipients(Message.RecipientType.TO));
			transport.close();

			log.debug("Sent email to " + recipient + " with " + failureCount
					+ " failures.");
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
			return new PasswordAuthentication(
					this.p.getProperty(SupervisorProperties.MAIL_USER_PROPERTY),
					this.p.getProperty(SupervisorProperties.MAIL_PASSWORD_PROPERTY));
		}
	}

}
