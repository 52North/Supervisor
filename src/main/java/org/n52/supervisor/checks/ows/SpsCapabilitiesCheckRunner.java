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
package org.n52.supervisor.checks.ows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">J&uuml;rrens, Eike Hinderk</a>
 */
public class SpsCapabilitiesCheckRunner extends OwsCapabilitiesCheckRunner {
	
	private static final Logger log = LoggerFactory.getLogger(SpsCapabilitiesCheckRunner.class);
	
	private static final String SPS_SERVICE = "SPS";

	public SpsCapabilitiesCheckRunner(final OwsCapabilitiesCheck check) {
		super(check);
		// TODO move this check and log statement to superclass
		if ( !check.getServiceType().equals(SPS_SERVICE)) {
			log.warn("Checking non-SPS {} with SPS runner: {}", check, this);
		}
	}
	
	@Override
    public boolean check() {
        log.debug("Checking SOS Capabilities for " + check.getServiceUrl());
        return runGetRequestParseDocCheck();
    }
	
	@Override
    protected String buildGetRequest() {
    	return super.buildGetRequest();
    }

}
