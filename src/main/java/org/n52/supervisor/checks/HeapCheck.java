/**
 * ﻿Copyright (C) 2013 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.supervisor.checks;

import java.util.ArrayList;
import java.util.Collection;

import org.n52.supervisor.ICheckResult;
import org.n52.supervisor.IServiceChecker;
import org.n52.supervisor.ICheckResult.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
public class HeapCheck implements IServiceChecker {

    private static final long L1024_2 = 1024 * 1024;

    private static Logger log = LoggerFactory.getLogger(HeapCheck.class);

    private long interval;

    private String lastCheckString = "";

    private ICheckResult result;

    /**
     * 
     * @param intervalMillis
     */
    public HeapCheck(long intervalMillis) {
        this.interval = intervalMillis;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#addResult(org.n52.owsSupervisor .ICheckResult)
     */
    @Override
    public void addResult(ICheckResult r) {
        if (this.result != null)
            log.debug("Overriding old result!");

        this.result = r;
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
     * @see org.n52.owsSupervisor.IServiceChecker#getCheckIntervalMillis()
     */
    @Override
    public long getCheckIntervalMillis() {
        return this.interval;
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
     * @see org.n52.owsSupervisor.IServiceChecker#getService()
     */
    @Override
    public String getService() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#notifyFailure()
     */
    @Override
    public void notifyFailure() {
        log.error("HeapChecker cannot fail!");
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

        addResult(new CheckResult("Internal Heap Checker", this.lastCheckString, ResultType.POSITIVE));
    }

}
