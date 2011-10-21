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

package org.n52.owsSupervisor.ui;

import java.util.Arrays;
import java.util.Collection;

import org.n52.owsSupervisor.ICheckResult;

/**
 * @author Daniel Nüst
 * 
 */
public class EmailNotification implements INotification {

    private Collection<ICheckResult> results;

    private String recipientEmail;

    private String serviceUrl;

    /**
     * 
     * @param results
     * @param recipientEmail
     * @param serviceURL
     */
    public EmailNotification(String serviceUrlP, String recipientEmailP, Collection<ICheckResult> resultsP) {
        this.results = resultsP;
        this.recipientEmail = recipientEmailP;
        this.serviceUrl = serviceUrlP;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.ui.INotification#getResults()
     */
    @Override
    public Collection<ICheckResult> getResults() {
        return this.results;
    }

    /**
     * 
     * @return
     */
    public String getRecipientEmail() {
        return this.recipientEmail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.ui.INotification#getServiceUrl()
     */
    @Override
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "EmailNotification [recipient=" + this.recipientEmail + ", service=" + this.serviceUrl + ", results="
                + Arrays.toString(this.results.toArray());
    }

}
