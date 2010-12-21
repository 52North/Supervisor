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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.checks.ICheckResult.ResultType;
import org.n52.owsSupervisor.tasks.SendEmailTask;

/**
 * @author Daniel Nüst
 * 
 */
public class HeapChecker implements IServiceChecker {

    private long interval;

    private ICheckResult result;

    private String lastCheckString = "";

    private static Logger log = Logger.getLogger(SendEmailTask.class);

    private static final long L1024_2 = 1024 * 1024;

    /**
     * @param l
     * 
     */
    public HeapChecker(long intervalMillis) {
        this.interval = intervalMillis;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#getService()
     */
    @Override
    public String getService() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#check()
     */
    @Override
    public boolean check() {
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        this.lastCheckString = "Size (Mb) is " + heapSize / L1024_2 + " of " + heapMaxSize / L1024_2 + " leaving "
                + heapFreeSize / L1024_2 + ".";

        notifySuccess();

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#getResults()
     */
    @Override
    public Collection<ICheckResult> getResults() {
        ArrayList<ICheckResult> l = new ArrayList<ICheckResult>();
        l.add(this.result);
        return l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#addResult(org.n52.owsSupervisor .ICheckResult)
     */
    @Override
    public void addResult(ICheckResult r) {
        //
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#notifyFailure()
     */
    @Override
    public void notifyFailure() {
        //
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#getCheckIntervalMillis()
     */
    @Override
    public long getCheckIntervalMillis() {
        return this.interval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.IServiceChecker#notifySuccess()
     */
    @Override
    public void notifySuccess() {
        if (log.isDebugEnabled()) {
            log.debug(this.lastCheckString);
        }

        this.result = new CheckResult("Internal Heap Checker", this.lastCheckString, ResultType.POSITIVE);
    }

}
