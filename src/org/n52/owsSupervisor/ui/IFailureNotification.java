/**
 * 
 */
package org.n52.owsSupervisor.ui;

import java.util.Collection;

import org.n52.owsSupervisor.checks.ICheckResult;

/**
 * @author Daniel Nüst
 *
 */
public interface IFailureNotification {
	
    /**
     * 
     * @return
     */
	public Collection<ICheckResult> getCheckResults();
	
	/**
	 * 
	 * @return
	 */
	public String getServiceUrl();

}
