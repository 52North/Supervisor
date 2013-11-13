package org.n52.supervisor.tasks;

import java.util.Collection;

import org.n52.supervisor.ICheckRunner;
import org.n52.supervisor.checks.CheckResult;

/**
 * 
 * @author Daniel
 * 
 */
public interface CheckTask {

    public Collection<CheckResult> checkIt(ICheckRunner c);

}
