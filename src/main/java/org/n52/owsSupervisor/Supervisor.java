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
package org.n52.owsSupervisor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.n52.owsSupervisor.tasks.IJobScheduler;
import org.n52.owsSupervisor.tasks.SendEmailTask;
import org.n52.owsSupervisor.tasks.TaskServlet;
import org.n52.owsSupervisor.ui.INotification;
import org.n52.owsSupervisor.util.SubmitCheckersTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Main class of OwsSupervisor.
 * 
 * @author Daniel Nüst
 * 
 */
public class Supervisor extends GenericServlet {

    private class ManualChecker extends Thread {

        private Collection<IServiceChecker> checkers;
        private boolean notify;

        public ManualChecker(Collection<IServiceChecker> checkers, boolean notify) {
            this.checkers = checkers;
            this.notify = notify;
            log.debug("NEW {}", this);
        }

        @Override
        public void run() {
            for (IServiceChecker checker : this.checkers) {
                boolean b = checker.check();

                if (this.notify) {
                    if ( !b) {
                        checker.notifyFailure();
                    }
                    else {
                        checker.notifySuccess();
                    }
                }
                else
                    log.debug("Ran check manually, got result {} - not notifying!      Check: {}", b, checker);
            }
        }
    }

    private static final String CONFIG_FILE_INIT_PARAMETER = "configFile";

    private static final String EMAIL_SENDER_TASK_ID = "EmailSenderTask";

    private static Queue<ICheckResult> latestResults;

    private static Logger log = LoggerFactory.getLogger(Supervisor.class);

    private static Queue<INotification> notifications;

    private static final long serialVersionUID = -4629591718212281703L;

    private static final String SUBMIT_CHECKERS_TASK_ID = "SubmitCheckersTask";

    private static final String COMMENT_PREFIX = "#";

    public static final String NAME_IN_CONTEXT = "Supervisor";

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
    public static void appendLatestResults(Collection<ICheckResult> results) {
        if (latestResults.size() >= SupervisorProperties.getInstance().getMaximumResults()) {
            log.debug("Too many results. Got " + results.size() + " new and " + latestResults.size() + " existing.");
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
     * @param results
     */
    public static void appendNotification(INotification notification) {
        notifications.add(notification);
    }

    /**
	 * 
	 */
    public static void clearNotifications() {
        log.info("Clearing notifications!");
        notifications.clear();
    }

    /**
     * 
     */
    public static void clearResults() {
        log.debug("Clearing all results: {}", Arrays.deepToString(latestResults.toArray()));
        latestResults.clear();
    }

    /**
     * @return
     */
    public static Collection<INotification> getCurrentNotificationsCopy() {
        return new ArrayList<INotification>(notifications);
    }

    /**
     * 
     * @return
     */
    public static List<ICheckResult> getLatestResults() {
        return new ArrayList<ICheckResult>(latestResults);
    }

    /**
     * @return
     */
    public static synchronized boolean removeAllNotifications(Collection<INotification> c) {
        return notifications.removeAll(c);
    }

    private Collection<IServiceChecker> checkers;

    private IJobScheduler scheduler;

    private ExecutorService manualExecutor;

    /**
	 * 
	 */
    public Supervisor() {
        log.info("*** NEW " + this + " ***");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#destroy()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        // get ServletContext
        ServletContext context = getServletContext();

        context.setAttribute(NAME_IN_CONTEXT, this);

        String basepath = context.getRealPath("/");
        InputStream configStream = context.getResourceAsStream(getInitParameter(CONFIG_FILE_INIT_PARAMETER));

        // initialize property manager
        SupervisorProperties sp = SupervisorProperties.getInstance(configStream, basepath);

        // initialize lists
        this.checkers = new ArrayList<IServiceChecker>();
        latestResults = new LinkedBlockingQueue<ICheckResult>(SupervisorProperties.getInstance().getMaximumResults());
        notifications = new LinkedBlockingQueue<INotification>();

        // init timer servlet
        TaskServlet timerServlet = (TaskServlet) context.getAttribute(TaskServlet.NAME_IN_CONTEXT);
        this.scheduler = sp.getScheduler(timerServlet);

        // add task for email notifications, with out without admin email
        String adminEmail = sp.getAdminEmail();
        if (adminEmail.contains("@ADMIN_EMAIL@")) {
            timerServlet.submit(EMAIL_SENDER_TASK_ID,
                                new SendEmailTask(),
                                sp.getEmailSendPeriodMins(),
                                sp.getEmailSendPeriodMins());
        }
        else {
            log.info("Found admin email address for send email task.");
            timerServlet.submit(EMAIL_SENDER_TASK_ID,
                                new SendEmailTask(adminEmail),
                                sp.getEmailSendPeriodMins(),
                                sp.getEmailSendPeriodMins());
        }

        // initialize checkers
        this.checkers = loadCheckers(sp);// SWSL.checkers;

        // submit checkers
        SubmitCheckersTask sct = new SubmitCheckersTask(this.scheduler, this.checkers);
        timerServlet.submit(SUBMIT_CHECKERS_TASK_ID,
                            sct,
                            SupervisorProperties.getInstance().getCheckSubmitDelaySecs() * 1000);

        // create thread pool for manually started checks
        this.manualExecutor = Executors.newSingleThreadExecutor();

        log.info("*** INITIALIZED SUPERVISOR ***");
    }

    /**
     * class loading inspired by SPFRegistry.java
     * 
     * @param sp
     * @return
     */
    private Collection<IServiceChecker> loadCheckers(SupervisorProperties sp) {
        Collection<IServiceChecker> chkrs = new ArrayList<IServiceChecker>();

        if (sp.isUseConfigCheckers()) {
            Collection<IServiceChecker> configFileCheckers = loadConfigFileCheckers(sp.getCheckConfigurations());
            chkrs.addAll(configFileCheckers);
        }

        if (sp.isUseCompiledCheckers()) {
            Collection<IServiceChecker> compiledCheckers = loadCompiledCheckers(sp.getCheckClasses());
            chkrs.addAll(compiledCheckers);
        }

        return chkrs;
    }

    /**
     * 
     * @param checkClasses
     * @return
     */
    private Collection<IServiceChecker> loadCompiledCheckers(Collection<String> checkClasses) {
        Collection<IServiceChecker> chkrs = new ArrayList<IServiceChecker>();

        for (String check : checkClasses) {
            Class< ? > clazz;
            try {
                clazz = Class.forName(check);
            }
            catch (ClassNotFoundException e) {
                log.error("Checker class not found!", e);
                continue;
            }

            ICheckerFactory factory;
            try {
                factory = ((ICheckerFactory) clazz.newInstance());
            }
            catch (InstantiationException e) {
                log.error("Could not instantiate checker factory.", e);
                continue;
            }
            catch (IllegalAccessException e) {
                log.error("Could not instantiate checker factory.", e);
                continue;
            }

            Collection<IServiceChecker> factoryCheckers = factory.getCheckers();
            chkrs.addAll(factoryCheckers);
        }

        return chkrs;
    }

    /**
     * 
     * @param checkConfigurations
     * @return
     */
    private Collection<IServiceChecker> loadConfigFileCheckers(Collection<String> checkConfigurations) {
        Collection<IServiceChecker> chkrs = new ArrayList<IServiceChecker>();

        for (String configuration : checkConfigurations) {
            if (configuration.startsWith(COMMENT_PREFIX)) {
                log.debug("Skipping commented out checker: {}", configuration);
                continue;
            }

            String[] params = null;
            String classString = null;
            Class< ? >[] paramsClassArray = null;

            if (configuration.isEmpty())
                continue;

            /* do we have any parameters? */
            int pos = configuration.indexOf("(");
            if (pos > 0) {
                params = configuration.substring(pos + 1, configuration.length() - 1).split(",");
                paramsClassArray = new Class[params.length];

                /* load all parameters as strings */
                if (params.length > 0) {
                    for (int i = 0; i < params.length; i++) {
                        paramsClassArray[i] = String.class;
                        params[i] = params[i].trim();
                    }
                    classString = configuration.substring(0, pos);
                }
            }

            Class< ? > clazz;
            try {
                clazz = Class.forName(classString);
            }
            catch (ClassNotFoundException e) {
                log.error("Checker class not found!", e);
                continue;
            }

            try {
                IServiceChecker chckr = null;

                if (params != null && params.length > 0) {
                    Constructor< ? > constructor = clazz.getConstructor(paramsClassArray);
                    chckr = (IServiceChecker) constructor.newInstance((Object[]) params);
                }
                else {
                    try {
                        chckr = ((IServiceChecker) clazz.newInstance());
                    }
                    catch (InstantiationException e) {
                        log.error("Could not instantiate checker without parameters.", e);
                        continue;
                    }
                }

                if (chckr != null) {
                    chkrs.add(chckr);
                    log.info("Loaded checker: " + chckr);
                }
            }
            catch (IllegalAccessException e) {
                log.error("Could not access class with name '" + classString + "'.", e);
            }
            catch (InstantiationException e) {
                log.error("Could not instantiate class with name '" + classString + "'.", e);
            }
            catch (SecurityException e) {
                log.error(null, e);
            }
            catch (NoSuchMethodException e) {
                log.error("A constructor with "
                                  + ( (paramsClassArray == null) ? "NULL" : Integer.valueOf(paramsClassArray.length))
                                  + " String arguments for Class " + clazz.getName() + " could not be found.",
                          e);
            }
            catch (IllegalArgumentException e) {
                log.error(null, e);
            }
            catch (InvocationTargetException e) {
                log.error(null, e);
            }
            catch (ClassCastException e) {
                log.error(clazz.getName() + " is not an instance of IOutputPlugin! Could not instantiate.", e);
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return chkrs;
    }

    /**
     * 
     */
    public void runAllNow(boolean notify) {
        log.info("Running all checks now!");

        this.manualExecutor.submit(new ManualChecker(this.checkers, notify));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
        log.error("'service' method is not supported. ServletRequest: " + arg0);
    }

}