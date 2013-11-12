
package org.n52.supervisor.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;

public class TaskModule extends AbstractModule {

    private static Logger log = LoggerFactory.getLogger(TaskModule.class);

    @Override
    protected void configure() {
        // bind(TaskServlet.class);
        bind(IJobScheduler.class).to(JobSchedulerImpl.class);

        log.info("Configured {}", this);
    }

}
