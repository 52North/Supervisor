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

package org.n52.supervisor.tasks;

import org.n52.supervisor.api.CheckTask;
import org.n52.supervisor.api.CheckTaskFactory;
import org.n52.supervisor.api.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class TaskModule extends AbstractModule {

    private static Logger log = LoggerFactory.getLogger(TaskModule.class);

    @Override
    protected void configure() {
        bind(TaskExecutor.class).to(ThreadPoolTaskExecutor.class);
        bind(Scheduler.class).to(JobSchedulerImpl.class);

        install(new FactoryModuleBuilder().implement(CheckTask.class, CheckTaskImpl.class).build(CheckTaskFactory.class));

        log.info("Configured {}", this);
        
    }

}
