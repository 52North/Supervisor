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

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckResult;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.api.UnsupportedCheckException;
import org.n52.supervisor.db.ResultDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 *
 */
@XmlRootElement
public class HeapCheckRunner implements CheckRunner {

    private static final long L1024_2 = 1024 * 1024;

    private static Logger log = LoggerFactory.getLogger(HeapCheckRunner.class);

    private String lastCheckString = "";

    private CheckResult result;

    private HeapCheck c;

    private ResultDatabase rd;

    public HeapCheckRunner(final HeapCheck c) {
        this.c = c;
    }

    @Override
    public void addResult(final CheckResult r) {
        if (result != null) {
			log.debug("Overriding old result!");
		}

        result = r;
    }

    @Override
    public boolean check() {
        final long heapSize = Runtime.getRuntime().totalMemory();
        final long heapMaxSize = Runtime.getRuntime().maxMemory();
        final long heapFreeSize = Runtime.getRuntime().freeMemory();
        lastCheckString = String.format("Size is %s Mb of Mb %s leaving %s Mb.", heapSize / L1024_2, heapMaxSize
                / L1024_2, heapFreeSize / L1024_2);

        notifySuccess();

        return true;
    }

    @Override
    public Check getCheck() {
        return c;
    }

    @Override
    public Collection<CheckResult> getResults() {
        final ArrayList<CheckResult> l = new ArrayList<CheckResult>();
        l.add(result);
        return l;
    }

    @Override
    public void notifyFailure() {
        log.error("HeapChecker should not fail!");

        if (rd != null) {
			rd.appendResults(getResults());
		}
    }

    @Override
    public void notifySuccess() {
    	log.debug(lastCheckString);

        addResult(new HeapCheckResult(
        		ID_GENERATOR.generate(),
        		c.getIdentifier(),
        		lastCheckString,
        		new Date(),
        		CheckResult.ResultType.POSITIVE));

        if (rd != null) {
			rd.appendResults(getResults());
		}
    }

    @Override
    public void setCheck(final Check c) throws UnsupportedCheckException {
        if (c instanceof HeapCheck) {
            final HeapCheck hc = (HeapCheck) c;
            this.c = hc;
        } else {
			throw new UnsupportedCheckException();
		}
    }

    @Override
    public void setResultDatabase(final ResultDatabase rd) {
        this.rd = rd;
    }

}
