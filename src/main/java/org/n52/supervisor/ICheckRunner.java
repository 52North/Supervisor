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

package org.n52.supervisor;

import java.util.Collection;

import org.n52.supervisor.checks.Check;
import org.n52.supervisor.checks.CheckResult;
import org.n52.supervisor.checks.UnsupportedCheckException;
import org.n52.supervisor.db.ResultDatabase;

/**
 * @author Daniel Nüst
 * 
 */
public interface ICheckRunner {

    public void addResult(CheckResult r);

    public void setCheck(Check c) throws UnsupportedCheckException;

    public Check getCheck();

    public boolean check();

    public Collection<CheckResult> getResults();

    public void notifyFailure();

    public void notifySuccess();

    /**
     * @param rd
     *        is required so that the runner can announce its results
     */
    public void setResultDatabase(ResultDatabase rd);

}
