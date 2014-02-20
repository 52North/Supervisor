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

package org.n52.supervisor.notification;

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

import org.n52.supervisor.SupervisorInit;
import org.n52.supervisor.SupervisorProperties;
import org.n52.supervisor.api.CheckResult;
import org.n52.supervisor.api.Notification;
import org.n52.supervisor.checks.util.DebugCheckResult;
import org.n52.supervisor.db.ResultDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author Daniel Nüst
 * 
 */
public class SendEmailTask extends TimerTask {

    private class PropertyAuthenticator extends Authenticator {
        private Properties p;

        public PropertyAuthenticator(Properties sp) {
            this.p = sp;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            String userName = this.p.getProperty(SupervisorProperties.MAIL_USER_PROPERTY);
            String password = this.p.getProperty(SupervisorProperties.MAIL_PASSWORD_PROPERTY);
            return new PasswordAuthentication(userName, password);
        }
    }

    private static final String EMAIL_CONTENT_ENCODING = "text/plain";

    private static final Object EMAIL_RESULT_DELIMITER_TEXT = "\n";

    private static final Object EMAIL_GOODBYE_TEXT = "\n\nThis message is provided by Supervisor, courtesy of 52°North. Enjoy!\nhttp://52north.org/ -- exploring horizons";

    private static final Object EMAIL_HELLO_TEXT = "Attention on deck! The following check results were reported in the system since the last email:\n";

    private static Logger log = LoggerFactory.getLogger(SendEmailTask.class);

    private String adminEmail = null;

    // FIXME need to get notifications here
    private Collection<Notification> notifications;

    private ResultDatabase rd;

    @Inject
    public SendEmailTask(@Named("supervisor.admin.email")
    String adminEmail, ResultDatabase rd) {
        this.adminEmail = adminEmail;
        this.rd = rd;

        log.info("NEW " + this.toString());
    }

    private boolean doTask(Collection<Notification> notifications) {
        boolean noError = true;
        int overallFailureCounter = 0;
        int overallEmailCounter = 0;

        // collect all notifications for each email address
        Map<String, Collection<EmailNotification>> emails = new HashMap<String, Collection<EmailNotification>>();
        for (Notification iNoti : notifications) {
            if (iNoti instanceof EmailNotification) {
                EmailNotification msg = (EmailNotification) iNoti;
                if (emails.containsKey(msg.getRecipientEmail())) {
                    // add to existing notification list
                    Collection<EmailNotification> notificationsForEmailaddress = emails.get(msg.getRecipientEmail());
                    notificationsForEmailaddress.add(msg);
                }
                else {
                    // create new notification list for the address
                    ArrayList<EmailNotification> notificationsForEmailaddress = new ArrayList<EmailNotification>();
                    notificationsForEmailaddress.add(msg);
                    emails.put(msg.getRecipientEmail(), notificationsForEmailaddress);
                }
            }
            // not an email notification, not handled
        }

        // iterate through emails
        for (Entry<String, Collection<EmailNotification>> email : emails.entrySet()) {
            // create message
            StringBuilder sb = new StringBuilder();
            sb.append(EMAIL_HELLO_TEXT);

            int failureCount = 0;
            for (EmailNotification noti : email.getValue()) {
                for (CheckResult r : noti.getResults()) {
                    if (r.getType().equals(CheckResult.ResultType.NEGATIVE)) {
                        sb.append(r.toString());
                        sb.append(EMAIL_RESULT_DELIMITER_TEXT);

                        failureCount++;
                    }
                }
            }
            for (EmailNotification noti : email.getValue()) {
                sb.append("\n");
                for (CheckResult r : noti.getResults()) {
                    if (r.getType().equals(CheckResult.ResultType.NEUTRAL)) {
                        sb.append(r.toString());
                        sb.append(EMAIL_RESULT_DELIMITER_TEXT);
                    }
                }
            }
            for (EmailNotification noti : email.getValue()) {
                sb.append("\n");
                for (CheckResult r : noti.getResults()) {
                    if (r.getType().equals(CheckResult.ResultType.POSITIVE)) {
                        sb.append(r.toString());
                        sb.append(EMAIL_RESULT_DELIMITER_TEXT);
                    }
                }
            }

            sb.append(EMAIL_GOODBYE_TEXT);

            // do the sending
            try {
                sendEmail(email.getKey(), sb.toString(), failureCount);
                overallEmailCounter++;
                overallFailureCounter += failureCount;
            }
            catch (MessagingException e) {
                log.error("Could not send email to " + email.getKey(), e);

                String text = String.format("FAILED to send email to %s : %s", email.getKey(), e.getMessage());
                CheckResult result = new DebugCheckResult(text);
                this.rd.appendResult(result);
                noError = false;
            }
        } // loop over all email addresses

        String text = String.format("Sent %s email(s) with %s failure(s).", overallEmailCounter, overallFailureCounter);

        CheckResult result = new DebugCheckResult(text);
        this.rd.appendResult(result);

        return noError;
    }

    @Override
    public void run() {
        if (notifications.size() < 1) {
            log.info("** No notifications, skipping to send emails.");
            return;
        }

        try {
            log.info("** Sending emails based on " + notifications.size() + " notifications.");
            boolean noError = doTask(notifications);

            // all went ok, clear notifications
            if (noError)
                SupervisorInit.removeAllNotifications(notifications);
            else
                log.error("** Error sending emails.");
        }
        catch (Error e) {
            log.error("Error fulfilling SendEmailTask");
            if (this.adminEmail != null) {
                try {
                    sendEmail(this.adminEmail, "ERROR: " + e.getMessage(), 1);
                }
                catch (MessagingException e1) {
                    log.error("Could not send email to admin with error!");
                }
            }
            // throw e;
        }
    }

    protected void sendEmail(String recipient, String messageText, int failureCount) throws MessagingException {
        SupervisorProperties sp = SupervisorProperties.getInstance();

        // send it
        if (sp.isSendEmails()) {
            Properties mailProps = SupervisorProperties.getInstance().getMailSessionProperties();
            Session mailSession = Session.getDefaultInstance(mailProps, new PropertyAuthenticator(mailProps));
            Transport transport = mailSession.getTransport();

            MimeMessage message = new MimeMessage(mailSession);

            if (failureCount > 1)
                message.setSubject("[Supervisor] " + failureCount + " checks failed");
            else if (failureCount == 1)
                message.setSubject("[Supervisor] " + failureCount + " check failed");
            else
                message.setSubject("[Supervisor] All checks passed");

            message.setContent(messageText, EMAIL_CONTENT_ENCODING);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSender(sp.getEmailSender());

            transport.connect();
            transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            transport.close();

            log.info("Sent email to " + recipient + " with " + failureCount + " failure(s).");
        }
        else {
            log.warn("Not sending Email (disabled in properties!)");
            log.debug("Email Content: " + messageText);
        }
    }

}
