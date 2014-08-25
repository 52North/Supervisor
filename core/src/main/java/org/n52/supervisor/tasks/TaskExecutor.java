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
