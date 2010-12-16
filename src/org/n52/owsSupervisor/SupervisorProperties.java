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
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

/**
 * This singleton class handles service wide properties.
 * 
 * @author Daniel Nüst
 * 
 */
public class SupervisorProperties {

	private static Logger log = Logger.getLogger(SupervisorProperties.class);

	private static final String SERVICEVERSION = "SERVICEVERSION";

	private static final String DEFAULT_CHECK_INTERVAL_SECS = "DEFAULT_CHECK_INTERVAL_SECS";

	private static final String MAIL_PROTOCOL = "MAIL_PROTOCOL";

	private static final String MAIL_HOST = "MAIL_HOST";

	private static final String MAIL_USER = "MAIL_USER";

	private static final String MAIL_PASSWORD = "MAIL_PASSWORD";

	private static final String MAIL_HOST_PORT = "MAIL_HOST_PORT";

	private static final String MAIL_ENABLE_AUTH = "MAIL_ENABLE_AUTH";

	private static final String MAIL_ENABLE_TLS = "MAIL_ENABLE_TLS";

	private static final String MAIL_SENDER_ADDRESS = "MAIL_SENDER_ADDRESS";

	public static final String MAIL_USER_PROPERTY = "mail.user";

	public static final String MAIL_PASSWORD_PROPERTY = "mail.password";

	private static final String MAIL_SOCKET_FALLBACK = "false";

	private static final String MAIL_SOCKET_CLASS = "javax.net.ssl.SSLSocketFactory";

	private static final String SEND_EMAILS = "SEND_EMAILS";

	private static final String MAX_CHECK_LIST_SIZE = "MAX_CHECK_LIST_SIZE";

	private static final String SEND_EMAIL_INTERVAL_MINS = "SEND_EMAIL_INTERVAL_MINS";
	
	private static final String HTML_PAGE_REFRESH_SECS = "HTML_PAGE_REFRESH_SECS";

	private static final String ADMIN_EMAIL = "ADMIN_EMAIL";
	
	private static SupervisorProperties instance;

	private String serviceVersion;

	private long defaultCheckIntervalMillis;

	private Properties mailProps = new Properties();

	private InternetAddress emailSender;

	private boolean sendEmails;

	private int maximumResults;

	private int emailSendPeriodMins;

	private int pageRefreshSecs;

	private String adminEmail;

	/**
	 * Constructor to create an instance of the PropertiesManager
	 * 
	 * @param configStream
	 *            The servletcontext stream to get the path for the
	 *            phenomenonXML file of the web.xml
	 * @param basepath
	 * @throws AddressException
	 */
	private SupervisorProperties(InputStream configStream, String basepath) {
		Properties props = new Properties();
		// load properties
		try {
			props.load(configStream);
		} catch (IOException e) {
			log.error("Loading properties failed.");
		}

		this.serviceVersion = props.getProperty(SERVICEVERSION);
		this.defaultCheckIntervalMillis = Long.parseLong(props
				.getProperty(DEFAULT_CHECK_INTERVAL_SECS)) * 1000;

		// set up SMTP properties with TLS
		this.mailProps.setProperty("mail.transport.protocol",
				props.getProperty(MAIL_PROTOCOL));
		this.mailProps.setProperty("mail.host", props.getProperty(MAIL_HOST));
		this.mailProps.setProperty(MAIL_USER_PROPERTY,
				props.getProperty(MAIL_USER));
		this.mailProps.setProperty(MAIL_PASSWORD_PROPERTY,
				props.getProperty(MAIL_PASSWORD));
		this.mailProps.put("mail.smtp.starttls.enable",
				props.getProperty(MAIL_ENABLE_TLS));
		this.mailProps.put("mail.smtp.starttls.required",
				props.getProperty(MAIL_ENABLE_TLS));
		this.mailProps.put("mail.smtp.auth",
				props.getProperty(MAIL_ENABLE_AUTH));
		this.mailProps.put("mail.smtp.socketFactory.port",
				props.getProperty(MAIL_HOST_PORT));
		this.mailProps.put("mail.smtp.socketFactory.class", MAIL_SOCKET_CLASS);
		this.mailProps.put("mail.smtp.socketFactory.fallback",
				MAIL_SOCKET_FALLBACK);
		try {
			this.emailSender = new InternetAddress(
					props.getProperty(MAIL_SENDER_ADDRESS));
		} catch (AddressException e) {
			log.error("Could not create sender email address!", e);
		}
		this.sendEmails = Boolean.parseBoolean(props.getProperty(SEND_EMAILS));
		this.emailSendPeriodMins = Integer.parseInt(props
				.getProperty(SEND_EMAIL_INTERVAL_MINS)) * 1000 * 60;

		this.maximumResults = Integer.parseInt(props
				.getProperty(MAX_CHECK_LIST_SIZE));
		this.pageRefreshSecs = Integer.parseInt(props.getProperty(HTML_PAGE_REFRESH_SECS));
		this.adminEmail = props.getProperty(ADMIN_EMAIL);

		log.info("NEW " + this.toString());
	}

	/**
	 * This methode provides the only instance of PropertiesManager.
	 * 
	 * @param configStream
	 *            The servletcontext stream to get the path for the
	 *            phenomenonXML file of the web.xml
	 * @param basepath
	 * @return The instance of the PropertiesManager
	 */
	public static SupervisorProperties getInstance(InputStream configStream,
			String basepath) {
		if (instance == null) {
			instance = new SupervisorProperties(configStream, basepath);
		}
		return instance;
	}

	/**
	 * This methode provides the only instance of PropertiesManager.
	 * 
	 * @return The instance of the PropertiesManager
	 */
	public static SupervisorProperties getInstance() {
		if (instance == null) {
			log.error("PropertiesManager is not instantiated!");
			return null;
		}
		return instance;
	}

	/**
	 * 
	 * @return
	 */
	public String getServiceVersion() {
		return this.serviceVersion;
	}

	/**
	 * 
	 * @return
	 */
	public String getClientRequestContentType() {
		return "text/xml";
	}

	/**
	 * 
	 * @return
	 */
	public String getClientRequestEncoding() {
		return "UTF-8";
	}

	/**
	 * 
	 */
	public Properties getMailSessionProperties() {
		return this.mailProps;
	}

	/**
	 * 
	 * @return
	 */
	public Address getEmailSender() {
		return this.emailSender;
	}

	public long getDefaultCheckIntervalMillis() {
		return this.defaultCheckIntervalMillis;
	}

	/**
	 * 
	 * @return
	 */
	public boolean getSendEmails() {
		return this.sendEmails;
	}

	/**
	 * 
	 * @return
	 */
	public String getUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 
	 * @return
	 */
	public int getMaximumResults() {
		return this.maximumResults;
	}

	/**
	 * 
	 * @return email send period in minutes
	 */
	public int getEmailSendPeriodMins() {
		return this.emailSendPeriodMins;
	}

	/**
	 * 
	 * @return
	 */
	public int getPageRefreshSecs() {
		return this.pageRefreshSecs;
	}

	public String getAdminEmail() {
		return this.adminEmail;
	}

}