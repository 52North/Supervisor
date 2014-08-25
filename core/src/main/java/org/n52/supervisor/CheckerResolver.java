/**
 * ﻿Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source Software GmbH
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

import java.util.Set;

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.checks.RunnerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author Daniel
 * 
 */
@Singleton
public class CheckerResolver {

    private static Logger log = LoggerFactory.getLogger(CheckerResolver.class);

	@Inject
	private Set<RunnerFactory> factories;
    
    public CheckRunner getRunner(final Check check) {
        log.debug("Resolving check: {}", check);
        // String checkType = check.getType();

        CheckRunner r = null;

        for (RunnerFactory runnerFactory : factories) {
			r = runnerFactory.resolveRunner(check);
			
			if (r != null) {
				return r;
			}
		}
        
        return r;
    }

}
