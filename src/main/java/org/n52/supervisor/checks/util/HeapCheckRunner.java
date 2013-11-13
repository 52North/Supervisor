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

package org.n52.supervisor.checks.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.n52.supervisor.ICheckRunner;
import org.n52.supervisor.checks.Check;
import org.n52.supervisor.checks.CheckResult;
import org.n52.supervisor.checks.UnsupportedCheckException;
import org.n52.supervisor.checks.CheckResult.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
@XmlRootElement
public class HeapCheckRunner implements ICheckRunner {

    private static final long L1024_2 = 1024 * 1024;

    private static Logger log = LoggerFactory.getLogger(HeapCheckRunner.class);

    private String lastCheckString = "";

    private CheckResult result;

    private HeapCheck c;

    public HeapCheckRunner(HeapCheck c) {
        this.c = c;
    }

    @Override
    public void addResult(CheckResult r) {
        if (this.result != null)
            log.debug("Overriding old result!");

        this.result = r;
    }

    @Override
    public boolean check() {
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        this.lastCheckString = String.format("Size is %s Mb of Mb %s leaving %s Mb.", heapSize / L1024_2, heapMaxSize
                / L1024_2, heapFreeSize / L1024_2);

        notifySuccess();

        return true;
    }

    @Override
    public Collection<CheckResult> getResults() {
        ArrayList<CheckResult> l = new ArrayList<CheckResult>();
        l.add(this.result);
        return l;
    }

    @Override
    public void notifyFailure() {
        log.error("HeapChecker cannot fail!");
    }

    @Override
    public void notifySuccess() {
        if (log.isDebugEnabled()) {
            log.debug(this.lastCheckString);
        }

        addResult(new HeapCheckResult(this.c.getIdentifier(),
                                      this.lastCheckString,
                                      new Date(),
                                      CheckResult.ResultType.POSITIVE));
    }

    @Override
    public void setCheck(Check c) throws UnsupportedCheckException {
        if (c instanceof HeapCheck) {
            HeapCheck hc = (HeapCheck) c;
            this.c = hc;
        }
        else
            throw new UnsupportedCheckException();
    }

    @Override
    public Check getCheck() {
        return this.c;
    }

}
