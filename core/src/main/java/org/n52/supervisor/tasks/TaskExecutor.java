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
package org.n52.supervisor.tasks;

import org.n52.supervisor.api.CheckRunner;

public interface TaskExecutor {
	
	/**
	 * Submit a new Task to the executor.
	 * An implementation shall execute the given
	 * runner after the specified delay repeat
	 * the execution using the provided period
	 * until it is cancelled.
	 * 
	 * @param identifier the task identifier
	 * @param runner the actual task object
	 * @param delay delay in ms after first execution
	 * @param period period in ms (0 = no recurrences)
	 * @throws TaskExecutorException if the submit fails for internal reasons
	 */
	void submit(String identifier, CheckRunner runner, long delay, long period) throws TaskExecutorException;
	
	/**
	 * an implementation shall remove the runner with this identifier
	 * from scheduled executions
	 * 
	 * @param identifier
	 */
	void cancel(String identifier);
	
}
