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

package org.n52.supervisor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This singleton class handles service wide properties.
 * 
 * @author Daniel Nüst
 * 
 */
public class SupervisorProperties {

    private static final String ADMIN_EMAIL = "ADMIN_EMAIL";

    private static final String CHECK_CLASSES = "CHECK_CLASSES";

    private static final String CHECK_LIST_SEPERATOR = "CHECK_LIST_SEPERATOR";

    private static final String CHECK_SUBMIT_DELAY_SECS = "CHECK_SUBMIT_DELAY_SECS";

    private static final String CHECKS = "CHECKS";

    private static final String DEFAULT_CHECK_INTERVAL_SECS = "DEFAULT_CHECK_INTERVAL_SECS";

    private static SupervisorProperties instance;

    private static Logger log = LoggerFactory.getLogger(SupervisorProperties.class);

    private static final String MAIL_ENABLE_AUTH = "MAIL_ENABLE_AUTH";

    private static final String MAIL_ENABLE_TLS = "MAIL_ENABLE_TLS";

    private static final String MAIL_HOST = "MAIL_HOST";

    private static final String MAIL_HOST_PORT = "MAIL_HOST_PORT";

    private static final String MAIL_PASSWORD = "MAIL_PASSWORD";

    public static final String MAIL_PASSWORD_PROPERTY = "mail.password";

    private static final String MAIL_PROTOCOL = "MAIL_PROTOCOL";

    private static final String MAIL_SENDER_ADDRESS = "MAIL_SENDER_ADDRESS";

    private static final String MAIL_SOCKET_CLASS = "javax.net.ssl.SSLSocketFactory";

    private static final String MAIL_SOCKET_FALLBACK = "false";

    private static final String MAIL_USER = "MAIL_USER";

    public static final String MAIL_USER_PROPERTY = "mail.user";

    private static final String SEND_EMAILS = "supervisor.tasks.email.send";

    private static final String USE_COMPILED_CHECKERS = "supervisor.checks.load.compiledFiles";

    private static final String USE_CONFIG_CHECKERS = "supervisor.checks.load.configFile";

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
     * This methode provides the only instance of PropertiesManager.
     * 
     * @param configStream
     *        The servletcontext stream to get the path for the phenomenonXML file of the web.xml
     * @param basepath
     * @return The instance of the PropertiesManager
     */
    public static SupervisorProperties getInstance(Properties props) {
        if (instance == null) {
            instance = new SupervisorProperties(props);
        }
        return instance;
    }

    private String adminEmail;

    private ArrayList<String> checkClasses;

    private ArrayList<String> checkConfigurations;

    private int checkSubmitDelaySecs;

    private long defaultCheckIntervalMillis;

    private InternetAddress emailSender;

    private Properties mailProps = new Properties();

    private boolean sendEmails;

    private String serviceVersion;

    private boolean useCompiledCheckers;

    private boolean useConfigCheckers;

    private SupervisorProperties(Properties props) {
        // the actual checks
        this.checkConfigurations = new ArrayList<String>();
        String checksString = props.getProperty(CHECKS);
        String[] checks = checksString.split(props.getProperty(CHECK_LIST_SEPERATOR));
        for (String s : checks) {
            this.checkConfigurations.add(s.trim());
        }
        this.checkClasses = new ArrayList<String>();
        String checkClassesString = props.getProperty(CHECK_CLASSES);
        if ( !checkClassesString.isEmpty()) {
            checks = checkClassesString.split(props.getProperty(CHECK_LIST_SEPERATOR));
            for (String s : checks) {
                this.checkClasses.add(s.trim());
            }
        }

        this.useCompiledCheckers = Boolean.parseBoolean(props.getProperty(USE_COMPILED_CHECKERS));
        this.useConfigCheckers = Boolean.parseBoolean(props.getProperty(USE_CONFIG_CHECKERS));

        // set up SMTP properties with TLS
        this.mailProps.setProperty("mail.transport.protocol", props.getProperty(MAIL_PROTOCOL));
        this.mailProps.setProperty("mail.host", props.getProperty(MAIL_HOST));
        this.mailProps.setProperty(MAIL_USER_PROPERTY, props.getProperty(MAIL_USER));
        this.mailProps.setProperty(MAIL_PASSWORD_PROPERTY, props.getProperty(MAIL_PASSWORD));
        this.mailProps.put("mail.smtp.starttls.enable", props.getProperty(MAIL_ENABLE_TLS));
        this.mailProps.put("mail.smtp.starttls.required", props.getProperty(MAIL_ENABLE_TLS));
        this.mailProps.put("mail.smtp.auth", props.getProperty(MAIL_ENABLE_AUTH));
        this.mailProps.put("mail.smtp.socketFactory.port", props.getProperty(MAIL_HOST_PORT));
        this.mailProps.put("mail.smtp.socketFactory.class", MAIL_SOCKET_CLASS);
        this.mailProps.put("mail.smtp.socketFactory.fallback", MAIL_SOCKET_FALLBACK);
        try {
            this.emailSender = new InternetAddress(props.getProperty(MAIL_SENDER_ADDRESS));
        }
        catch (AddressException e) {
            log.error("Could not create sender email address!", e);
        }

        this.sendEmails = Boolean.parseBoolean(props.getProperty(SEND_EMAILS));

        this.adminEmail = props.getProperty(ADMIN_EMAIL);
        this.checkSubmitDelaySecs = Integer.parseInt(props.getProperty(CHECK_SUBMIT_DELAY_SECS));

        log.info("NEW " + this.toString());
    }

    public String getAdminEmail() {
        return this.adminEmail;
    }

    public Collection<String> getCheckClasses() {
        return this.checkClasses;
    }

    public Collection<String> getCheckConfigurations() {
        return this.checkConfigurations;
    }

    public int getCheckSubmitDelaySecs() {
        return this.checkSubmitDelaySecs;
    }

    public String getClientRequestContentType() {
        return "text/xml";
    }

    public String getClientRequestEncoding() {
        return "UTF-8";
    }

    public long getDefaultCheckIntervalMillis() {
        return this.defaultCheckIntervalMillis;
    }

    public Address getEmailSender() {
        return this.emailSender;
    }

    public Properties getMailSessionProperties() {
        return this.mailProps;
    }

    public String getServiceVersion() {
        return this.serviceVersion;
    }

    public boolean isSendEmails() {
        return this.sendEmails;
    }

    public boolean isUseCompiledCheckers() {
        return this.useCompiledCheckers;
    }

    public boolean isUseConfigCheckers() {
        return this.useConfigCheckers;
    }

}