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

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import net.opengis.wps.x100.CapabilitiesDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.supervisor.checks.CheckResult;
import org.n52.supervisor.checks.ServiceCheckResult;
import org.n52.supervisor.checks.CheckResult.ResultType;
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

    public WpsCapabilitiesCheckRunner(OwsCapabilitiesCheck check) {
        super(check);
    }

    private static Logger log = LoggerFactory.getLogger(WpsCapabilitiesCheckRunner.class);

    @Override
    public boolean check() {
        URL sUrl = this.c.getServiceUrl();

        if (log.isDebugEnabled()) {
            log.debug("Checking WPS Capabilities via GET " + sUrl);
        }

        clearResults();

        try {
            XmlObject response = this.client.xSendGetRequest(sUrl.toString(), buildGetRequest());

            // parse response - this is the test!
            CapabilitiesDocument caps = CapabilitiesDocument.Factory.parse(response.getDomNode());
            log.debug("Parsed caps with serviceVersion " + caps.getCapabilities().getVersion());
        }
        catch (IOException | XmlException e) {
            log.error("Could not send request", e);
            ServiceCheckResult r = new ServiceCheckResult(this.c.getIdentifier(),
                                                          String.format("%s ... Could not send request or parse response: %s",
                                                                        NEGATIVE_TEXT,
                                                                        e.getMessage()),
                                                          new Date(),
                                                          CheckResult.ResultType.NEGATIVE,
                                                          this.c.getServiceIdentifier());
            addResult(r);
            return false;
        }

        ServiceCheckResult r = new ServiceCheckResult(this.c.getIdentifier(),
                                                      POSITIVE_TEXT,
                                                      new Date(),
                                                      CheckResult.ResultType.POSITIVE,
                                                      this.c.getServiceIdentifier());
        addResult(r);

        return true;
    }
}
