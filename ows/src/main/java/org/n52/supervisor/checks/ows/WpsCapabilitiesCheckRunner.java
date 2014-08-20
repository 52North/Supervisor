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

import java.net.URL;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import net.opengis.wps.x100.CapabilitiesDocument;

import org.apache.xmlbeans.XmlObject;
import org.n52.supervisor.api.CheckResult;
import org.n52.supervisor.checks.ServiceCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Uses GET only
 *
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 *
 */
@XmlRootElement
public class WpsCapabilitiesCheckRunner extends OwsCapabilitiesCheckRunner {

    private static Logger log = LoggerFactory.getLogger(WpsCapabilitiesCheckRunner.class);

    public WpsCapabilitiesCheckRunner(final OwsCapabilitiesCheck check) {
        super(check);
    }

    @Override
    public boolean check() {
        final URL sUrl = check.getServiceUrl();

        log.debug("Checking WPS Capabilities via GET to {}", sUrl);

        clearResults();

        try {
            final XmlObject response = client.xSendGetRequest(sUrl.toString(), buildGetRequest());

            // parse response - this is the test!
            final CapabilitiesDocument caps = CapabilitiesDocument.Factory.parse(response.getDomNode());
            log.debug("Parsed caps with serviceVersion " + caps.getCapabilities().getVersion());
        }
        catch (final Exception e) {
            log.error("Could not send request", e);
            final ServiceCheckResult r = new ServiceCheckResult(ID_GENERATOR.generate(),e, check, "ERROR");
            addResult(r);
            return false;
        }

        final ServiceCheckResult r = new ServiceCheckResult(
        		ID_GENERATOR.generate(),
        		check.getIdentifier(),
        		POSITIVE_TEXT,
        		new Date(),
        		CheckResult.ResultType.POSITIVE,
        		check.getServiceIdentifier());
        addResult(r);

        return true;
    }
}
