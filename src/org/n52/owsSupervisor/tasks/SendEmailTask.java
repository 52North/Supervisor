/*******************************************************************************
Copyright (C) 2010
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License serviceVersion 2 as published by the 
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
            String userName = this.p.getProperty(SupervisorProperties.MAIL_USER_PROPERTY);
            String password = this.p.getProperty(SupervisorProperties.MAIL_PASSWORD_PROPERTY);
            return new PasswordAuthentication(userName, password);
        }
    }

    private static final String EMAIL_CONTENT_ENCODING = "text/plain";

    private static final Object EMAIL_FAILURE_DELIMITER_TEXT = "\n";

    private static final Object EMAIL_GOODBYE_TEXT = "\n\n\nGood Luck fixing it!";

    private static final Object EMAIL_HELLO_TEXT = "Attention on deck!\n\nFailed check(s) occured while testing.\n\n";

    private static Logger log = Logger.getLogger(SendEmailTask.class);

    private static final String RESULT_IDENTIFIER = "Send Email Task";

    private String adminEmail = null;

    /**
     * 
     * @param notifications2
     */
    public SendEmailTask() {
        log.info("NEW " + this.toString());
    }

    /**
     * 
     * @param adminEmail
     * @param notificationsP
     */
    public SendEmailTask(String adminEmailP) {
        this.adminEmail = adminEmailP;
        log.info("NEW " + this.toString());
    }

    private boolean doTask(Collection<IFailureNotification> notifications) {
        log.info("*** Sending emails based on " + notifications.size() + " notifications.");

        // collect all notifications for one email address
        Map<String, Collection<EmailFailureNotification>> emails = new HashMap<String, Collection<EmailFailureNotification>>();

        for (IFailureNotification iMsg : notifications) {
            if (iMsg instanceof EmailFailureNotification) {
                EmailFailureNotification msg = (EmailFailureNotification) iMsg;
                if (emails.containsKey(msg.getRecipientEmail())) {
                    // add to failure list
                    Collection<EmailFailureNotification> failures = emails.get(msg.getRecipientEmail());
                    failures.add(msg);
                }
                else {
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
        boolean noError = true;
        for (Entry<String, Collection<EmailFailureNotification>> email : emails.entrySet()) {
            // create message
            StringBuilder sb = new StringBuilder();
            sb.append(EMAIL_HELLO_TEXT);

            int failureCount = 0;
            for (EmailFailureNotification failure : email.getValue()) {
                for (ICheckResult f : failure.getCheckResults()) {
                    sb.append(EMAIL_FAILURE_DELIMITER_TEXT);
                    sb.append(f.toString());
                }

                failureCount += failure.getCheckResults().size();
            }

            sb.append(EMAIL_GOODBYE_TEXT);

            try {
                sendEmail(email.getKey(), sb.toString(), failureCount);

                overallEmailCounter++;
                overallFailureCounter += failureCount;

                ICheckResult result = new CheckResult(RESULT_IDENTIFIER, "Sent " + overallEmailCounter
                        + " email(s) with " + overallFailureCounter + " failure(s).", ResultType.NEUTRAL);
                Supervisor.appendLatestResult(result);
            }
            catch (MessagingException e) {
                log.error("Could not send email to " + email.getKey(), e);

                ICheckResult result = new CheckResult(RESULT_IDENTIFIER, "FAILED to send email to " + email.getKey()
                        + ": " + e.getMessage(), ResultType.NEGATIVE);
                Supervisor.appendLatestResult(result);
                noError = false;
            }
        }
        
        return noError;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
        Collection<IFailureNotification> notifications = Supervisor.getCurrentNotificationsCopy();

        if (notifications.size() < 1) {
            log.debug("No notifications. Yay!");
            return;
        }

        try {
            boolean noError = doTask(notifications);

            // all went ok, clear notifications
            if (noError)
                Supervisor.removeAllNotifications(notifications);
        }
        catch (Error e) {
            log.error("Error fulfilling task");
            if (this.adminEmail != null) {
                try {
                    sendEmail(this.adminEmail, "ERROR: " + e.getMessage(), 0);
                }
                catch (MessagingException e1) {
                    log.error("Could not send email on error!");
                }
            }
            throw e;
        }
    }

    /**
     * 
     * @param recipient
     * @param failures
     * @throws MessagingException
     */
    protected void sendEmail(String recipient, String messageText, int failureCount) throws MessagingException {
        SupervisorProperties sp = SupervisorProperties.getInstance();

        // send it
        if (sp.getSendEmails()) {
            Properties mailProps = SupervisorProperties.getInstance().getMailSessionProperties();
            Session mailSession = Session.getDefaultInstance(mailProps, new PropertyAuthenticator(mailProps));
            Transport transport = mailSession.getTransport();

            MimeMessage message = new MimeMessage(mailSession);
            if (failureCount > 1)
                message.setSubject("[OwsSupervisor] " + failureCount + " checks failed");
            else
                message.setSubject("[OwsSupervisor] " + failureCount + " check failed");
            message.setContent(messageText, EMAIL_CONTENT_ENCODING);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSender(sp.getEmailSender());

            transport.connect();
            transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            transport.close();

            log.debug("Sent email to " + recipient + " with " + failureCount + " failure(s).");
        }
        else {
            log.warn("Not sending Email (disabled in properties!)");
            log.debug("Email Content: " + messageText);
        }
    }

}
