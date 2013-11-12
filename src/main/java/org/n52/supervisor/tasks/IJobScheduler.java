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

import org.n52.supervisor.IServiceChecker;

/**
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public interface IJobScheduler {

    /**
     * Cancels the task with the given identifier.
     * 
     * See {@link java.util.TimerTask#cancel()} for details.
     * 
     * @param identifier
     */
    public abstract void cancel(String identifier);

    /**
     * 
     * @param checker
     * @return the id
     */
    public abstract String submit(IServiceChecker checker);

    /**
     * 
     * @param checker
     * @param delay
     *        delay in milliseconds after which the task is executed the first time
     * @return
     */
    public abstract String submit(IServiceChecker checker, long delay);

}