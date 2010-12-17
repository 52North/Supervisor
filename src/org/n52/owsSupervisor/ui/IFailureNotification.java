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
	
	public String getServiceUrl();
	
	public Collection<ICheckResult> getCheckResults();

}
