/*******************************************************************************
Copyright (C) 2010
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License version 2 as published by the 
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


/**
 * @author Daniel Nüst
 * 
 */
public class CheckResultImpl implements ICheckResult {

	private Date time;

	private String serviceIdentifier;

	private String result;

	private ResultType type;

	/**
	 * 
	 * @param timeP
	 * @param serviceP
	 * @param resultP
	 * @param typeP
	 */
	public CheckResultImpl(Date timeP, String serviceP, String resultP,
			ResultType typeP) {
		this.time = timeP;
		this.serviceIdentifier = serviceP;
		this.result = resultP;
		this.type = typeP;
	}

	/**
	 * 
	 * @param serviceP
	 * @param resultP
	 * @param typeP
	 */
	public CheckResultImpl(String serviceP, String resultP, ResultType typeP) {
		this(new Date(), serviceP, resultP, typeP);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.owsSupervisor.ICheckResult#getTimeOfCheck()
	 */
	@Override
	public Date getTimeOfCheck() {
		return this.time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.owsSupervisor.ICheckResult#getServiceIdentifier()
	 */
	@Override
	public String getServiceIdentifier() {
		return this.serviceIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.owsSupervisor.ICheckResult#getResult()
	 */
	@Override
	public String getResult() {
		return this.result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.owsSupervisor.ICheckResult#getType()
	 */
	@Override
	public ResultType getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return "[" + getType().name() + "] " + this.time + ": "
				+ this.serviceIdentifier + " - " + this.result;
	}

}
