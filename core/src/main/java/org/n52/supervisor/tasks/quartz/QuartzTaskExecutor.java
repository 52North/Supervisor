package org.n52.supervisor.tasks.quartz;

import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.tasks.TaskExecutor;
import org.n52.supervisor.tasks.TaskExecutorException;

public class QuartzTaskExecutor implements TaskExecutor {

	@Override
	public void submit(String identifier, CheckRunner t, long delay, long period)
			throws TaskExecutorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancel(String identifier) {
		// TODO Auto-generated method stub
		
	}

}
