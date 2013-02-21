/**
 * ﻿Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.owsSupervisor.checks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.owsSupervisor.ICheckResult;
import org.n52.owsSupervisor.ICheckResult.ResultType;
import org.n52.owsSupervisor.Supervisor;
import org.n52.owsSupervisor.ui.EmailNotification;
import org.n52.owsSupervisor.ui.INotification;

/**
 * 
 * this check collects information about currently running checks and the state of the service.
 * 
 * @author Daniel Nüst (d.nuest@52north.org)
 * 
 */
public class SelfCheck extends AbstractServiceCheck {

    private static final long L1024_2 = 1024 * 1024;

    private static Logger log = LoggerFactory.getLogger(SelfCheck.class);

    private String message;

    /**
     * 
     * @param serviceURL
     * @param notifyEmail
     * @param checkInterval
     * @throws MalformedURLException
     */
    public SelfCheck(String serviceURL, String notifyEmail, String checkInterval) throws MalformedURLException {
        this(notifyEmail, new URL(serviceURL), Long.valueOf(checkInterval).longValue());
    }

    /**
     * 
     * @param notifyEmail
     * @param serviceURL
     * @param checkInterval
     */
    public SelfCheck(String notifyEmail, URL serviceURL, long checkInterval) {
        super(notifyEmail, serviceURL, checkInterval);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.IServiceChecker#check()
     */
    @Override
    public boolean check() {
        // check if everything is running fine...
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        StringBuilder sb = new StringBuilder();

        sb.append("Self check ran succesfully, service is most probably up and running.\n Go to <a href='");
        sb.append(getServiceURL().toString());
        sb.append("' title='OwsSupervisor HTML Interface'>");
        sb.append(getServiceURL().toString());
        sb.append("</a>");
        sb.append(" for the current check status.");

        sb.append(" *** Heap Info: Size (Mb) is ");
        sb.append(heapSize / L1024_2);
        sb.append(" of ");
        sb.append(heapMaxSize / L1024_2);
        sb.append(" leaving ");
        sb.append(heapFreeSize / L1024_2);
        sb.append(".");

        this.message = sb.toString();

        // TODO add currently running tasks and their last message

        addResult(new ServiceCheckResult(getService(), this.message, ResultType.POSITIVE));

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.AbstractServiceCheck#notifyFailure()
     */
    @Override
    public void notifyFailure() {
        log.error("SelfChecker cannot fail!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.AbstractServiceCheck#notifySuccess()
     */
    @Override
    public void notifySuccess() {
        // send email
        if (log.isDebugEnabled())
            log.debug("Check SUCCESSFUL: " + this);

        if (getEmail() == null) {
            log.error("Can not notify via email, is null!");
            return;
        }

        Collection<ICheckResult> results = getResults();

        INotification noti = new EmailNotification(getServiceURL().toString(), getEmail(), results);
        // append for email notification to queue
        Supervisor.appendNotification(noti);

        if (log.isDebugEnabled())
            log.debug("Submitted email with " + results.size() + " successes.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SelfCheck [" + getServiceURL() + ", email=" + getEmail() + ", check interval=" + getCheckIntervalMillis();
    }

}
