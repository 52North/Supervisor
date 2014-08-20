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

package org.n52.supervisor.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.n52.supervisor.api.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * 
 * @author Daniel
 * 
 */
@Singleton
public class CheckDatabase {

    private static Logger log = LoggerFactory.getLogger(CheckDatabase.class);

    private ArrayList<Check> internalDatabase = new ArrayList<Check>();

    public CheckDatabase() {
        log.info("NEW {}", this);
    }

    public void addAll(Collection<Check> checks) {
        this.internalDatabase.addAll(checks);
    }

    public void close() {
        log.info("Closing ...");

        this.internalDatabase.clear();
        this.internalDatabase = null;
    }

    public List<Check> getAllChecks() {
        return this.internalDatabase;
    }

    public Check getCheck(String id) {
        for (Check c : internalDatabase) {
            if (c.getIdentifier().equals(id))
                return c;
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CheckDatabase [");
        if (internalDatabase != null) {
            builder.append("check count=");
            builder.append(internalDatabase.size());
        }
        builder.append("]");
        return builder.toString();
    }

}
