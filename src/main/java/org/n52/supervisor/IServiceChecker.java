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

/**
 * @author Daniel Nüst
 * 
 */
// @XmlJavaTypeAdapter(AnyTypeAdapter.class)
// @XmlRootElement
public interface IServiceChecker {

    /**
     * add a result to the result list
     */
    public void addResult(ICheckResult r);

    public boolean check();

    public long getCheckIntervalMillis();

    public Collection<ICheckResult> getResults();

    /**
     * 
     * @return the identifier of the checked service
     */
    public String getService();

    /**
     * notify about failure of (one ore more) of the contained checks
     */
    public void notifyFailure();

    /**
     * notify about successful completition of the check/all checks
     */
    public void notifySuccess();

    /**
     * 
     * @return the identifier of the checker itself
     */
    public String getIdentifier();

    public void setIdentifier(String id);

    /**
     * 
     * @return the type of the checker
     */
    public String getType();
    
}