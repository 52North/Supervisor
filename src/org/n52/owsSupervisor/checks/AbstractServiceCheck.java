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

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.Supervisor;
import org.n52.owsSupervisor.SupervisorProperties;
import org.n52.owsSupervisor.checks.ICheckResult.ResultType;
import org.n52.owsSupervisor.ui.EmailFailureNotification;
import org.n52.owsSupervisor.util.Client;

/**
 * @author Daniel Nüst
 * 
 */
public abstract class AbstractServiceCheck implements IServiceChecker {

    public static final DateFormat ISO8601LocalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS.SSS");

    private static Logger log = Logger.getLogger(AbstractServiceCheck.class);

    private long checkIntervalMillis = SupervisorProperties.getInstance().getDefaultCheckIntervalMillis();

    protected Client client = new Client();

    private String email = null;

    private List<ICheckResult> results = new ArrayList<ICheckResult>();

    protected URL serviceUrl;

    /**
     * 
     * @param notifyEmail
     */
    public AbstractServiceCheck(String notifyEmail) {
        this.email = notifyEmail;
    }

    /**
     * 
     * @param notifyEmail
     * @param checkInterval
     */
    public AbstractServiceCheck(String notifyEmail, long checkInterval) {
        this.email = notifyEmail;
        this.checkIntervalMillis = checkInterval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.IServiceChecker#addResult(org.n52.owsSupervisor.checks.ICheckResult)
     */
    @Override
    public void addResult(ICheckResult r) {
        log.info("Result added: " + r);

        if (r.getType().equals(ResultType.NEGATIVE))
            log.warn("NEGATIVE result added to " + toString() + ":\n\n" + r + "\n");

        this.results.add(r);
    }

    /**
	 * 
	 */
    public void clearResults() {
        if (log.isDebugEnabled()) {
            log.debug("Clearing " + this.results.size() + " results");
        }
        this.results.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.IServiceChecker#getCheckIntervalMillis()
     */
    @Override
    public long getCheckIntervalMillis() {
        return this.checkIntervalMillis;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#getResults()
     */
    @Override
    public Collection<ICheckResult> getResults() {
        return this.results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.IServiceChecker#getService()
     */
    @Override
    public String getService() {
        return this.serviceUrl.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.IServiceChecker#notifyFailure()
     */
    @Override
    public void notifyFailure() {
        if (log.isDebugEnabled())
            log.debug("Check FAILED: " + this);

        if (this.email == null) {
            log.error("Can not notify via email, is null!");
            return;
        }

        Collection<ICheckResult> failures = new ArrayList<ICheckResult>();
        for (ICheckResult r : this.results) {
            if (r.getType().equals(ResultType.NEGATIVE))
                failures.add(r);
        }

        // append for email notification to queue
        Supervisor.appendNotification(new EmailFailureNotification(this.serviceUrl.toString(), this.email, failures));

        if (log.isDebugEnabled())
            log.debug("Submitted email with " + failures.size() + " failures.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.IServiceChecker#notifySuccess()
     */
    @Override
    public void notifySuccess() {
        if (log.isDebugEnabled())
            log.debug("Check SUCCESSFUL:" + this);

    }

}
