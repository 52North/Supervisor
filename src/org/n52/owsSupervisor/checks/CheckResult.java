/*******************************************************************************
Copyright (C) 2011
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

import java.util.Date;

import org.n52.owsSupervisor.ICheckResult;

/**
 * 
 * @author Daniel Nüst
 *
 */
public class CheckResult implements ICheckResult {

	private String checkIdentifier;
	
	private String result;
	
	private Date timeOfCheck;
	
	private ResultType type;

	/**
	 * 
	 * @param checkIdentifierP
	 * @param resultP
	 * @param typeP
	 */
	public CheckResult(String checkIdentifierP, String resultP, ResultType typeP) {
		this.checkIdentifier = checkIdentifierP;
		this.result = resultP;
		this.type = typeP;
		this.timeOfCheck = new Date();
	}

	/* (non-Javadoc)
	 * @see org.n52.owsSupervisor.checks.ICheckResult#getCheckIdentifier()
	 */
	@Override
	public String getCheckIdentifier() {
		return this.checkIdentifier;
	}

	/* (non-Javadoc)
	 * @see org.n52.owsSupervisor.checks.ICheckResult#getResult()
	 */
	@Override
	public String getResult() {
		return this.result;
	}

	/* (non-Javadoc)
	 * @see org.n52.owsSupervisor.checks.ICheckResult#getTimeOfCheck()
	 */
	@Override
	public Date getTimeOfCheck() {
		return this.timeOfCheck;
	}

	/* (non-Javadoc)
	 * @see org.n52.owsSupervisor.checks.ICheckResult#getType()
	 */
	@Override
	public ResultType getType() {
		return this.type;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    // TODO Auto-generated method stub
	    return "CheckResult [identifier=" + this.checkIdentifier + ", type=" + this.type + ", time=" + this.timeOfCheck + ", result=" + this.result + "]";
	}

}
