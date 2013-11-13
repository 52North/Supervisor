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

import javax.xml.bind.annotation.XmlRootElement;

import org.n52.supervisor.ICheckRunner;
import org.n52.supervisor.checks.Check;
import org.n52.supervisor.checks.CheckResult;
import org.n52.supervisor.checks.UnsupportedCheckException;
import org.n52.supervisor.db.ResultDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Daniel Nüst
 * 
 */
@XmlRootElement
public class CheckCollectionRunner implements ICheckRunner {

    private static Logger log = LoggerFactory.getLogger(CheckCollectionRunner.class);

    private Multimap<ICheckRunner, Check> checks = ArrayListMultimap.create();

    public CheckCollectionRunner(Multimap<ICheckRunner, Check> checks) {
        this.checks = checks;
    }

    @Override
    public void addResult(CheckResult r) {
        throw new UnsupportedOperationException("Collection of checks, don't know which results to add this to!");
    }

    @Override
    public boolean check() {
        log.debug("Checking collection of {} checkers with {} checks",
                  this.checks.keys().size(),
                  this.checks.values().size());

        boolean b = true;
        int success = 0;
        int failure = 0;
        for (ICheckRunner cr : this.checks.keys()) {
            Collection<Check> collection = this.checks.get(cr);

            for (Check c : collection) {
                try {
                    cr.setCheck(c);

                    if ( !cr.check()) {
                        b = false;
                        failure++;
                    }
                    else {
                        success++;
                    }
                }
                catch (UnsupportedCheckException e) {
                    log.error("Error in setting one check to checker.", e);
                }
            }
        }

        log.debug("Checked collection: " + success + " successful and " + failure + " failed checks.");

        return b;
    }

    @Override
    public Collection<CheckResult> getResults() {
        ArrayList<CheckResult> results = new ArrayList<CheckResult>();
        for (ICheckRunner c : this.checks.keys()) {
            results.addAll(c.getResults());
        }
        return results;
    }

    @Override
    public void notifyFailure() {
        for (ICheckRunner c : this.checks.keys()) {
            c.notifyFailure();
        }
    }

    @Override
    public void notifySuccess() {
        for (ICheckRunner c : this.checks.keys()) {
            c.notifySuccess();
        }
    }

    @Override
    public void setCheck(Check c) throws UnsupportedCheckException {
        throw new UnsupportedCheckException("must use collection of checks");
    }

    @Override
    public Check getCheck() {
        return null;
    }

    @Override
    public void setResultDatabase(ResultDatabase rd) {
        for (ICheckRunner r : this.checks.keySet()) {
            r.setResultDatabase(rd);
        }
    }

}
