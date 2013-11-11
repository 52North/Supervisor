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
package org.n52.owsSupervisor.checks;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import net.opengis.ows.x11.CapabilitiesBaseType;
import net.opengis.ows.x11.GetCapabilitiesDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.owsSupervisor.ICheckResult.ResultType;
import org.n52.owsSupervisor.util.XmlTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
public class OwsCapabilitiesCheck extends AbstractServiceCheck {

    protected static final String DEFAULT_OWS_VERSION = "1.1";

    private static Logger log = LoggerFactory.getLogger(OwsCapabilitiesCheck.class);

    protected static final String NEGATIVE_TEXT = "Request for capabilities document FAILED.";

    protected static final String POSITIVE_TEXT = "Successfully requested capabilities document.";

    protected String serviceVersion;

    /**
     * 
     * @param owsVersion
     * @param serviceURL
     * @param notifyEmail
     * @param checkIntervalMillis
     */
    public OwsCapabilitiesCheck(String owsVersion, URL serviceURL, String notifyEmail, long checkIntervalMillis) {
        super(notifyEmail, serviceURL, checkIntervalMillis);
        this.serviceVersion = owsVersion;
    }

    /**
     * 
     * @param serviceURL
     * @param notifyEmail
     */
    public OwsCapabilitiesCheck(URL serviceURL, String notifyEmail) {
        super(notifyEmail, serviceURL);
        this.serviceVersion = DEFAULT_OWS_VERSION;
    }

    /**
     * 
     * @param service
     * @param notifyEmail
     * @param checkIntervalMillis
     */
    public OwsCapabilitiesCheck(URL service, String notifyEmail, long checkIntervalMillis) {
        this(DEFAULT_OWS_VERSION, service, notifyEmail, checkIntervalMillis);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#check()
     */
    @Override
    public boolean check() {
        URL sUrl = getServiceURL();
        if (log.isDebugEnabled()) {
            log.debug("Checking Capabilities for " + sUrl);
        }

        if (this.serviceVersion != "1.1") {
            log.error("OWS Version not supported: " + this.serviceVersion);
            addResult(new ServiceCheckResult(new Date(), sUrl.toString(), NEGATIVE_TEXT
                    + " ... OWS Version not supported: " + this.serviceVersion, ResultType.NEGATIVE));
            return false;
        }

        clearResults();

        // create get capabilities document
        GetCapabilitiesDocument getCapDoc = GetCapabilitiesDocument.Factory.newInstance(XmlTools.DEFAULT_OPTIONS);
        getCapDoc.addNewGetCapabilities();

        // send the document
        try {
            XmlObject response = this.client.xSendPostRequest(sUrl.toString(), getCapDoc);
            getCapDoc = null;

            // parse response - this is the test!
            CapabilitiesBaseType caps = CapabilitiesBaseType.Factory.parse(response.getDomNode());
            log.debug("Parsed caps with serviceVersion " + caps.getVersion());
        }
        catch (IOException e) {
            log.error("Could not send request", e);
            addResult(new ServiceCheckResult(new Date(), sUrl.toString(), NEGATIVE_TEXT
                    + " ... Could not send request!", ResultType.NEGATIVE));
            return false;
        }
        catch (XmlException e) {
            log.error("Could not send request", e);
            addResult(new ServiceCheckResult(new Date(), sUrl.toString(), NEGATIVE_TEXT
                    + " ... Could not parse response to CapabilitiesBaseType!", ResultType.NEGATIVE));
            return false;
        }

        // save the good result
        addResult(new ServiceCheckResult(new Date(), sUrl.toString(), POSITIVE_TEXT, ResultType.POSITIVE));

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OwsCapabilitiesCheck [" + getService() + ", check interval=" + getCheckIntervalMillis() + "]";
    }

}
