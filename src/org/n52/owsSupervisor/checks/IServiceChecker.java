/*******************************************************************************
Copyright (C) 2010
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License serviceVersion 2 as published by the 
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

Author: Daniel Nüst
 
 ******************************************************************************/
package org.n52.owsSupervisor.checks;

import java.util.Collection;


/**
 * @author Daniel Nüst
 * 
 */
public interface IServiceChecker {

	/**
	 * add a result to the result list
	 */
	public void addResult(ICheckResult r);

	/**
	 * 
	 * run the checks and 
	 * 
	 * @return
	 */
	public boolean check();

	/**
	 * 
	 * @return
	 */
	public long getCheckIntervalMillis();

	/**
	 * 
	 * @return
	 */
	public Collection<ICheckResult> getResults();

	/**
	 * 
	 * @return the identifier of the checked service
	 */
	public String getService();
	
	/**
	 * notify about failure of (one ore more) of the contained checks
	 */
	public void notifyFailure();

	/**
	 * notify about successful completition of the check/all checks
	 */
	public void notifySuccess();

}
