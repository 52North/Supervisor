/**
 * ﻿Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source Software GmbH
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
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.api.Scheduler;
import org.n52.supervisor.db.CheckDatabase;
import org.n52.supervisor.db.ResultDatabase;
import org.n52.supervisor.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * 
 * @author Daniel Nüst
 * 
 */
@Singleton
public class SupervisorInit {

    private class DelayedStartThread extends Thread {

        private long sleep;
        private Collection<Check> checks;
        private Scheduler scheduler;
        private long submitDelaySecs;
        private CheckerResolver cr;

        public DelayedStartThread(long sleep,
                                  Collection<Check> checks,
                                  CheckerResolver cr,
                                  Scheduler scheduler,
                                  long submitDelaySecs) {
            this.sleep = sleep;
            this.checks = checks;
            this.scheduler = scheduler;
            this.submitDelaySecs = submitDelaySecs;
            this.cr = cr;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(this.sleep);
            }
            catch (InterruptedException e) {
                log.error("Error delaying startup thread...", e);
            }

            for (Check c : this.checks) {
                CheckRunner runner = this.cr.getRunner(c);
                if (runner != null) {
                	String id = this.scheduler.submit(runner, this.submitDelaySecs * 1000);

                    log.debug("Submitted check with id {} : {} \n\t and runner: {}", id, c, runner);	
                }
                else {
                	log.debug("Could not find a runner for check {}", c);
                }
                
            }
        }
    }

    private static Logger log = LoggerFactory.getLogger(SupervisorInit.class);

    private static final String COMMENT_PREFIX = "#";

    public static final String NAME_IN_CONTEXT = "Supervisor";

    private Scheduler scheduler;

    private CheckDatabase db;

    private ResultDatabase rd;

    private CheckerResolver cr;

    private IdentifierGenerator idGen;
    
    private SupervisorProperties sp;

    @Inject
    public SupervisorInit(@Named("context.basepath")
                          String basepath,
                          Scheduler scheduler,
                          CheckDatabase db,
                          ResultDatabase rd,
                          CheckerResolver cr,
                          IdentifierGenerator idGen, SupervisorProperties sp) {
        this.scheduler = scheduler;
        this.db = db;
        this.sp = sp;
        this.rd = rd;
        this.cr = cr;
        this.idGen = idGen;

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

    public void init(String basepath) {
        log.debug("InitializING {} ...", this);

        try {

            // initialize checkers
            db.addAll(loadCheckers());// SWSL.checkers;

            // submit checkers delayed
            DelayedStartThread dst = new DelayedStartThread(5 * 1000, db.getAllChecks(), this.cr, scheduler, 1);
            ExecutorService exec = Executors.newSingleThreadExecutor();
            exec.submit(dst);

        }
        catch (Exception e) {
            log.error("in init", e);
        }

        log.trace("InitializED {}", this);
    }

    private Collection<Check> loadCheckers() {
        Collection<Check> checks = new ArrayList<Check>();

        if (sp.isUseConfigCheckers()) {
            Collection<Check> configFileCheckers = loadConfigFileCheckers(sp.getCheckConfigurations());
            checks.addAll(configFileCheckers);
        }

        if (sp.isUseCompiledCheckers()) {
            Collection<Check> compiledCheckers = loadCompiledCheckers(sp.getCheckClasses());
            checks.addAll(compiledCheckers);
        }

        return checks;
    }

    private Collection<Check> loadCompiledCheckers(Collection<String> checkClasses) {
        Collection<Check> checks = new ArrayList<Check>();

        for (String check : checkClasses) {
            Class< ? > clazz;
            try {
                clazz = Class.forName(check);
            }
            catch (ClassNotFoundException e) {
                log.error("Checker class not found!", e);
                continue;
            }

            log.error("Not implemented yet to load {}", clazz);

            // ICheckerFactory factory;
            // try {
            // factory = ((ICheckerFactory) clazz.newInstance());
            // }
            // catch (InstantiationException e) {
            // log.error("Could not instantiate checker factory.", e);
            // continue;
            // }
            // catch (IllegalAccessException e) {
            // log.error("Could not instantiate checker factory.", e);
            // continue;
            // }
            //
            // Collection<Check> factoryCheckers = factory.getCheckers();
            // checks.addAll(factoryCheckers);
        }

        return checks;
    }

    private Collection<Check> loadConfigFileCheckers(Collection<String> checkConfigurations) {
        Collection<Check> checks = new ArrayList<Check>();

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
                Check check = null;

                if (params != null && params.length > 0) {
                    Constructor< ? > constructor = clazz.getConstructor(paramsClassArray);
                    check = (Check) constructor.newInstance((Object[]) params);
                }
                else {
                    try {
                        check = ((Check) clazz.newInstance());
                    }
                    catch (InstantiationException e) {
                        log.error("Could not instantiate checker without parameters.", e);
                        continue;
                    }
                }

                if (check != null) {
                    check.setIdentifier(this.idGen.generate());
                    checks.add(check);
                    log.info("Loaded checker: " + check);
                }
            }
            catch (IllegalAccessException | InstantiationException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                log.error("Could not access/instantiate class with name '{}': {} \n\t parameters: {}",
                          classString,
                          e.getMessage(),
                          (paramsClassArray == null) ? "NULL" : Integer.valueOf(paramsClassArray.length));
            }
        }

        return checks;
    }

    @PreDestroy
    protected void shutdown() throws Throwable {
        log.info("SHUTDOWN called...");

        this.db.close();
        this.rd.close();
    }
}