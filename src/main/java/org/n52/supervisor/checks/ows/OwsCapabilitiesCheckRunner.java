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

import org.apache.xmlbeans.XmlObject;
import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.n52.supervisor.checks.CheckResult;
import org.n52.supervisor.checks.ServiceCheckResult;
import org.n52.supervisor.util.XmlTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
public class OwsCapabilitiesCheckRunner extends AbstractServiceCheckRunner {

    private static Logger log = LoggerFactory.getLogger(OwsCapabilitiesCheckRunner.class);

    protected static final String NEGATIVE_TEXT = "Request for capabilities document FAILED.";

    protected static final String POSITIVE_TEXT = "Successfully requested capabilities document.";

    public OwsCapabilitiesCheckRunner(final OwsCapabilitiesCheck check) {
        super(check);
    }

    protected String buildGetRequest() {
        final OwsCapabilitiesCheck owsCheck = (OwsCapabilitiesCheck) check;
        return "Request=GetCapabilities&Service=" + owsCheck.getServiceType() + "&serviceVersion="
                + owsCheck.getServiceVersion();
    }

    @Override
    public boolean check() {
        final OwsCapabilitiesCheck owsCheck = (OwsCapabilitiesCheck) check;

        final URL sUrl = owsCheck.getServiceUrl();
        log.debug("Checking Capabilities for {}", sUrl);

        if (!owsCheck.getOwsVersion().equals("1.1")) {
            log.error("OWS Version not supported: " + owsCheck.getOwsVersion());
            final String text = String.format("%s ... OWS Version not supported: %s", NEGATIVE_TEXT, owsCheck.getOwsVersion());
            final ServiceCheckResult r = new ServiceCheckResult(owsCheck.getIdentifier(),
                                                          text,
                                                          new Date(),
                                                          CheckResult.ResultType.NEGATIVE,
                                                          owsCheck.getServiceIdentifier());
            addResult(r);
            return false;
        }

        clearResults();

        // create get capabilities document
        GetCapabilitiesDocument getCapDoc = GetCapabilitiesDocument.Factory.newInstance(XmlTools.DEFAULT_OPTIONS);
        getCapDoc.addNewGetCapabilities();

        // send the document
        try {
            final XmlObject response = client.xSendPostRequest(sUrl.toString(), getCapDoc);
            getCapDoc = null;

            // parse response - this is the test!
            final CapabilitiesBaseType caps = CapabilitiesBaseType.Factory.parse(response.getDomNode());
            log.debug("Parsed caps with serviceVersion " + caps.getVersion());
        }
        catch (final Exception e) {
            log.error("Could not send request", e);
            final ServiceCheckResult r = new ServiceCheckResult(owsCheck.getIdentifier(),
                                                          String.format("... Could not send request: %s"
                                                                  + e.getMessage()),
                                                          new Date(),
                                                          CheckResult.ResultType.NEGATIVE,
                                                          owsCheck.getServiceIdentifier());
            addResult(r);
            return false;
        }

        // save the good result
        final ServiceCheckResult r = new ServiceCheckResult(owsCheck.getIdentifier(),
                                                      POSITIVE_TEXT,
                                                      new Date(),
                                                      CheckResult.ResultType.POSITIVE,
                                                      owsCheck.getServiceIdentifier());
        addResult(r);
        return true;
    }

    protected boolean runGetRequestParseDocCheck() {
        final OwsCapabilitiesCheck owsCheck = (OwsCapabilitiesCheck) check;

        if ( !owsCheck.getOwsVersion().equals("1.1")) {
            log.error("OWS Version not supported: " + owsCheck.getOwsVersion());
            final String text = String.format("%s ... OWS Version not supported: %s", NEGATIVE_TEXT, owsCheck.getOwsVersion());
            final ServiceCheckResult r = new ServiceCheckResult(owsCheck.getIdentifier(),
                                                          text,
                                                          new Date(),
                                                          CheckResult.ResultType.NEGATIVE,
                                                          owsCheck.getServiceIdentifier());
            addResult(r);
            return false;
        }

        try {
            final XmlObject response = client.xSendGetRequest(check.getServiceUrl().toString(), buildGetRequest());

            ServiceCheckResult r = null;
            boolean rb = false;

            if (isCapabilitiesDocument(response) && 
            		hasVersionAttribute(response) && 
            		isVersionMatching(response,owsCheck.getServiceVersion())) {
                log.debug("Parsed caps, serviceVersion: " + getVersion(response));

                r = new ServiceCheckResult(check.getIdentifier(),
                                           POSITIVE_TEXT,
                                           new Date(),
                                           CheckResult.ResultType.POSITIVE,
                                           check.getServiceIdentifier());
                rb = true;
            }
            else {
                r = new ServiceCheckResult(check.getIdentifier(),
                                           " ... Response was not a Capabilities document!",
                                           new Date(),
                                           CheckResult.ResultType.NEGATIVE,
                                           check.getServiceIdentifier());
            }

            addResult(r);
            return rb;
        }
        catch (final Exception e) {
            final ServiceCheckResult r = new ServiceCheckResult(check.getIdentifier(),
                                                          String.format("%s ERROR: %s occured in %s",
                                                                        NEGATIVE_TEXT,
                                                                        e.getMessage(),
                                                                        this.getClass().getCanonicalName()),
                                                          new Date(),
                                                          CheckResult.ResultType.NEGATIVE,
                                                          check.getServiceIdentifier());
            addResult(r);
            return false;
        }
    }

	private String getVersion(final XmlObject response) {
		return response.getDomNode().getFirstChild().getAttributes().getNamedItem("version").getNodeValue();
	}

	private boolean isVersionMatching(final XmlObject response,
			final String owsVersion) {
		return getVersion(response).equals(owsVersion);
	}

	private boolean hasVersionAttribute(final XmlObject response) {
		return response.getDomNode().getFirstChild().getAttributes().getNamedItem("version") != null;
	}

	private boolean isCapabilitiesDocument(final XmlObject response) {
		return response.getDomNode().getFirstChild().getLocalName().equals("Capabilities");
	}

}
