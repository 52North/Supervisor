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

import net.opengis.ows.x11.CapabilitiesBaseType;
import net.opengis.ows.x11.GetCapabilitiesDocument;
import net.opengis.ows.x11.GetCapabilitiesType;

import org.apache.xmlbeans.XmlObject;
import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.n52.supervisor.checks.CheckResult;
import org.n52.supervisor.checks.ServiceCheckResult;
import org.n52.supervisor.util.XmlTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x52North.sir.x032.CapabilitiesDocument;

/**
 * @author Daniel Nüst
 * 
 */
public class OwsCapabilitiesCheckRunner extends AbstractServiceCheckRunner {

    private static Logger log = LoggerFactory.getLogger(OwsCapabilitiesCheckRunner.class);

    protected static final String NEGATIVE_TEXT = "Request for capabilities document FAILED.";

    protected static final String POSITIVE_TEXT = "Successfully requested capabilities document.";

    public OwsCapabilitiesCheckRunner(OwsCapabilitiesCheck check) {
        super(check);
    }

    protected String buildGetRequest() {
        OwsCapabilitiesCheck check = (OwsCapabilitiesCheck) this.c;
        return "Request=GetCapabilities&Service=" + check.getServiceType() + "&serviceVersion="
                + check.getServiceVersion();
    }

    @Override
    public boolean check() {
        OwsCapabilitiesCheck check = (OwsCapabilitiesCheck) this.c;

        URL sUrl = check.getServiceUrl();
        log.debug("Checking Capabilities for {}", sUrl);

        if (check.getOwsVersion() != "1.1") {
            log.error("OWS Version not supported: " + check.getOwsVersion());
            String text = String.format("%s ... OWS Version not supported: %s", NEGATIVE_TEXT, check.getOwsVersion());
            ServiceCheckResult r = new ServiceCheckResult(check.getIdentifier(),
                                                          text,
                                                          new Date(),
                                                          CheckResult.ResultType.NEGATIVE,
                                                          check.getServiceIdentifier());
            addResult(r);
            return false;
        }

        clearResults();

        // create get capabilities document
        GetCapabilitiesDocument getCapDoc = GetCapabilitiesDocument.Factory.newInstance(XmlTools.DEFAULT_OPTIONS);
        GetCapabilitiesType getCapabilities = getCapDoc.addNewGetCapabilities();

        // send the document
        try {
            XmlObject response = this.client.xSendPostRequest(sUrl.toString(), getCapDoc);
            getCapDoc = null;

            // parse response - this is the test!
            CapabilitiesBaseType caps = CapabilitiesBaseType.Factory.parse(response.getDomNode());
            log.debug("Parsed caps with serviceVersion " + caps.getVersion());
        }
        catch (Exception e) {
            log.error("Could not send request", e);
            ServiceCheckResult r = new ServiceCheckResult(check.getIdentifier(),
                                                          String.format("... Could not send request: %s"
                                                                  + e.getMessage()),
                                                          new Date(),
                                                          CheckResult.ResultType.NEGATIVE,
                                                          check.getServiceIdentifier());
            addResult(r);
            return false;
        }

        // save the good result
        ServiceCheckResult r = new ServiceCheckResult(check.getIdentifier(),
                                                      POSITIVE_TEXT,
                                                      new Date(),
                                                      CheckResult.ResultType.POSITIVE,
                                                      check.getServiceIdentifier());
        addResult(r);
        return true;
    }

    protected boolean runGetRequestParseDocCheck() {
        OwsCapabilitiesCheck check = (OwsCapabilitiesCheck) this.c;

        if ( !check.getOwsVersion().equals("1.1")) {
            log.error("OWS Version not supported: " + check.getOwsVersion());
            String text = String.format("%s ... OWS Version not supported: %s", NEGATIVE_TEXT, check.getOwsVersion());
            ServiceCheckResult r = new ServiceCheckResult(check.getIdentifier(),
                                                          text,
                                                          new Date(),
                                                          CheckResult.ResultType.NEGATIVE,
                                                          check.getServiceIdentifier());
            addResult(r);
            return false;
        }

        try {
            XmlObject response = this.client.xSendGetRequest(this.c.getServiceUrl().toString(), buildGetRequest());

            ServiceCheckResult r = null;
            boolean rb = false;

            if (response instanceof CapabilitiesDocument) {
                CapabilitiesDocument caps = (CapabilitiesDocument) response;
                log.debug("Parsed caps, serviceVersion: " + caps.getCapabilities().getVersion());

                r = new ServiceCheckResult(this.c.getIdentifier(),
                                           POSITIVE_TEXT,
                                           new Date(),
                                           CheckResult.ResultType.POSITIVE,
                                           this.c.getServiceIdentifier());
                rb = true;
            }
            else {
                r = new ServiceCheckResult(this.c.getIdentifier(),
                                           " ... Response was not a Capabilities document!",
                                           new Date(),
                                           CheckResult.ResultType.NEGATIVE,
                                           this.c.getServiceIdentifier());
            }

            addResult(r);
            return rb;
        }
        catch (Exception e) {
            ServiceCheckResult r = new ServiceCheckResult(this.c.getIdentifier(),
                                                          String.format("%s ERROR: %s occured in %s",
                                                                        NEGATIVE_TEXT,
                                                                        e.getMessage(),
                                                                        this.getClass().getCanonicalName()),
                                                          new Date(),
                                                          CheckResult.ResultType.NEGATIVE,
                                                          this.c.getServiceIdentifier());
            addResult(r);
            return false;
        }
    }

}
