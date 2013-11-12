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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.n52.supervisor.tasks.IJobScheduler;
import org.n52.supervisor.ui.INotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.SessionScoped;
import com.sun.jersey.api.view.Viewable;

/**
 * 
 * @author Daniel Nüst
 * 
 */
@Path("/")
@SessionScoped
public class Supervisor {

    private static Queue<ICheckResult> latestResults;

    private static Logger log = LoggerFactory.getLogger(Supervisor.class);

    private static Queue<INotification> notifications;

    private static final String COMMENT_PREFIX = "#";

    public static final String NAME_IN_CONTEXT = "Supervisor";

    public static void appendLatestResult(ICheckResult result) {
        latestResults.add(result);
    }

    public static void appendLatestResults(Collection<ICheckResult> results) {
        // if (latestResults.size() >= 100) { // FIXME make append non static so that config parameter can be
        // // used: this.maxStoredResults
        // log.debug("Too many results. Got " + results.size() + " new and " + latestResults.size() +
        // " existing.");
        // for (int i = 0; i < Math.min(results.size(), latestResults.size()); i++) {
        // // remove the first element so many times that the new results
        // // fit.
        // latestResults.remove();
        // }
        // }

        latestResults.addAll(results);
    }

    public static void appendNotification(INotification notification) {
        notifications.add(notification);
    }

    public static void clearNotifications() {
        log.info("Clearing notifications!");
        notifications.clear();
    }

    public static void clearResults() {
        log.debug("Clearing all results: {}", Arrays.deepToString(latestResults.toArray()));
        latestResults.clear();
    }

    public Collection<INotification> getCurrentNotificationsCopy() {
        return new ArrayList<INotification>(notifications);
    }

    public List<ICheckResult> getLatestResults() {
        return new ArrayList<ICheckResult>(latestResults);
    }

    public static synchronized boolean removeAllNotifications(Collection<INotification> c) {
        return notifications.removeAll(c);
    }

    private Collection<IServiceChecker> checkers;

    private IJobScheduler scheduler;

    private int maxStoredResults;

    @Inject
    public Supervisor(@Named("context.basepath")
    String basepath, @Named("supervisor.checks.maxStoredResults")
    int maxStoredResults, IJobScheduler scheduler) {
        this.maxStoredResults = maxStoredResults;
        this.scheduler = scheduler;

        // this.appConstants = constants;
        //
        // log.info("{} | Version: {} | Build: {} | From: {}",
        // this,
        // this.appConstants.getApplicationVersion(),
        // this.appConstants.getApplicationCommit(),
        // this.appConstants.getApplicationTimestamp());

        init(basepath);

        log.info(" ***** NEW {} *****", this);
    }

    @PreDestroy
    protected void shutdown() throws Throwable {
        log.info("SHUTDOWN called...");

        checkers.clear();
        checkers = null;
        latestResults.clear();
        latestResults = null;
        notifications.clear();
        notifications = null;
    }

    public void init(String basepath) {
        log.debug("InitializING {} ...", this);

        checkers = new ArrayList<IServiceChecker>();
        latestResults = new LinkedBlockingQueue<ICheckResult>(this.maxStoredResults);
        notifications = new LinkedBlockingQueue<INotification>();

        SupervisorProperties sp = SupervisorProperties.getInstance();

        // initialize checkers
        checkers = loadCheckers(sp);// SWSL.checkers;

        // submit checkers
        for (IServiceChecker sc : checkers) {
            String id = this.scheduler.submit(sc, sp.getCheckSubmitDelaySecs() * 1000);
            sc.setIdentifier(id);
        }

        // SubmitCheckersTask sct = new SubmitCheckersTask(this.scheduler, this.checkers);
        // timerServlet.submit(SUBMIT_CHECKERS_TASK_ID,
        // sct,
        // SupervisorProperties.getInstance().getCheckSubmitDelaySecs() * 1000);

        log.trace("InitializED {}", this);
    }

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

    @GET
    @Path("/")
    @Produces("text/html")
    public Response index() {
        return Response.ok().entity(new Viewable("/index")).build();
    }

    public Collection<IServiceChecker> getCheckers() {
        return checkers;
    }

}