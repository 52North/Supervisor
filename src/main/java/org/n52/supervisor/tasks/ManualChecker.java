
package org.n52.supervisor.tasks;

import java.util.Collection;

import org.n52.supervisor.IServiceChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ManualChecker extends Thread {

    private static Logger log = LoggerFactory.getLogger(ManualChecker.class);

    private Collection<IServiceChecker> checkers;

    private boolean notify;

    public ManualChecker(Collection<IServiceChecker> checkers, boolean notify) {
        this.checkers = checkers;
        this.notify = notify;

        log.debug("NEW {}", this);
    }

    @Override
    public void run() {
        for (IServiceChecker checker : this.checkers) {
            log.debug("Running checker {} ...", checker);
            boolean b = checker.check();
            log.debug("Result: {}", b);

            if (this.notify) {
                log.debug("Notify about result {}", b);
                if ( !b) {
                    checker.notifyFailure();
                }
                else {
                    checker.notifySuccess();
                }
            }
            else
                log.debug("Ran check manually, got result {} - not notifying!      Check: {}", b, checker);
        }
    }
}