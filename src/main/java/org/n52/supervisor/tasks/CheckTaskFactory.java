
package org.n52.supervisor.tasks;

import org.n52.supervisor.ICheckRunner;

/**
 * 
 * @author Daniel
 * 
 */
public interface CheckTaskFactory {

    CheckTask create(ICheckRunner checker);

}
