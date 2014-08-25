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

import java.util.HashMap;
import java.util.Map;

import org.n52.supervisor.resources.Api;
import org.n52.supervisor.resources.Checks;
import org.n52.supervisor.resources.Results;
import org.n52.supervisor.resources.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Names;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class SupervisorModule extends JerseyServletModule {

    private static Logger log = LoggerFactory.getLogger(SupervisorModule.class);

    public SupervisorModule() {
        super();

        log.info("NEW {}", this);
    }

    @Override
    protected void configureServlets() {
        String basepath = getServletContext().getRealPath("/");
        bindConstant().annotatedWith(Names.named("context.basepath")).to(basepath);
        bindConstant().annotatedWith(Names.named("service.baseurl")).to("http://localhost:8080/Supervisor");

        // TODO make task servlet injectable and remove servlet context
        // ThreadPoolTaskExecutor timerServlet = (ThreadPoolTaskExecutor)
        // getServletContext().getAttribute(ThreadPoolTaskExecutor.NAME_IN_CONTEXT);
        // if (timerServlet != null)
        // bind(ThreadPoolTaskExecutor.class).toInstance(timerServlet);

        // else
        // log.error("TimerServlet instance is null!");

        // bind the JAX-RS resources
        // http://code.google.com/p/google-guice/wiki/ServletModule
        bind(CheckerResolver.class);
        bind(Settings.class);
        bind(Api.class);
        bind(Results.class);

        bind(SupervisorInit.class);
        bind(Checks.class);

        Map<String, String> params = new HashMap<String, String>();
        params.put("com.sun.jersey.config.property.JSPTemplatesBasePath", "/");
        params.put("com.sun.jersey.config.property.WebPageContentRegex", "/.*\\.(jpg|ico|png|gif|html|id|txt|css|js)");
        // params.put("com.sun.jersey.config.property.packages","org.n52.oss.api;com.wordnik.swagger.jersey.listing");
        // params.put("api.version","1.0.0");
        // filter("/doc/api/*").through(GuiceContainer.class,params);
        // filter("/api-docs/*").through(GuiceContainer.class,params);

        filter("/*").through(GuiceContainer.class, params);
        log.debug("configured {} with context {}", this, getServletContext());
    }
}
