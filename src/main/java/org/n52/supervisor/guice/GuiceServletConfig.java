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

package org.n52.supervisor.guice;

import java.util.concurrent.Executors;

import org.n52.supervisor.SupervisorInit;
import org.n52.supervisor.db.StorageModule;
import org.n52.supervisor.id.IdentificationModule;
import org.n52.supervisor.tasks.TaskModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * http://code.google.com/p/google-guice/wiki/ServletModule
 */
public class GuiceServletConfig extends GuiceServletContextListener {

    private class InitThread extends Thread {

        private Injector i;

        public InitThread(Injector i) {
            this.i = i;
        }

        @Override
        public void run() {
            SupervisorInit instance = this.i.getInstance(SupervisorInit.class);
            System.out.println(instance);
        }

    }

    @Override
    protected Injector getInjector() {
        Injector injector = Guice.createInjector(new IdentificationModule(),
                                                 new ConfigModule(),
                                                 new StorageModule(),
                                                 new SupervisorModule(),
                                                 new TaskModule());

        // FIXME remove the hack to create an instance!
        Executors.newSingleThreadExecutor().submit(new InitThread(injector));

        return injector;
    }
}
