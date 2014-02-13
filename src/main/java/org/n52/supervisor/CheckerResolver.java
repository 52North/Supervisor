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

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.checks.ows.OwsCapabilitiesCheck;
import org.n52.supervisor.checks.ows.OwsCapabilitiesCheckRunner;
import org.n52.supervisor.checks.ows.SirCapabilitiesCheckRunner;
import org.n52.supervisor.checks.ows.SosCapabilitiesCheckRunner;
import org.n52.supervisor.checks.ows.SpsCapabilitiesCheckRunner;
import org.n52.supervisor.checks.ows.WpsCapabilitiesCheckRunner;
import org.n52.supervisor.checks.util.HeapCheck;
import org.n52.supervisor.checks.util.HeapCheckRunner;
import org.n52.supervisor.checks.util.SelfCheck;
import org.n52.supervisor.checks.util.SelfCheckRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * 
 * @author Daniel
 * 
 */
@Singleton
public class CheckerResolver {

    private static Logger log = LoggerFactory.getLogger(CheckerResolver.class);

    public CheckRunner getRunner(final Check check) {
        log.debug("Resolving check: {}", check);
        // String checkType = check.getType();

        CheckRunner r = null;

        if (check instanceof OwsCapabilitiesCheck) {
            final OwsCapabilitiesCheck occ = (OwsCapabilitiesCheck) check;
            final String serviceType = occ.getServiceType();
            log.debug("Resolving OWS check by service type: {}", occ);

            switch (serviceType) {
            case "SOS":
                r = new SosCapabilitiesCheckRunner(occ);
                break;
            case "SIR":
                r = new SirCapabilitiesCheckRunner(occ);
            case "WPS":
                r = new WpsCapabilitiesCheckRunner(occ);
            case "SPS":
            	r = new SpsCapabilitiesCheckRunner(occ);
            default:
                r = new OwsCapabilitiesCheckRunner(occ);
                log.error("Configured Servicetype '{}' is not supported! Using generic Checker '{}'.",
                		serviceType,
                		r.getClass().getName());
            }
        }
        else if (check instanceof HeapCheck) {
            final HeapCheck hc = (HeapCheck) check;
            r = new HeapCheckRunner(hc);
        }
        else if (check instanceof SelfCheck) {
            final SelfCheck sc = (SelfCheck) check;
            r = new SelfCheckRunner(sc);
        }

        return r;
    }

}
