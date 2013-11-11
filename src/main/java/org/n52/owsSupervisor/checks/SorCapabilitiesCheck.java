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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.xmlbeans.XmlObject;
import org.n52.owsSupervisor.ICheckResult.ResultType;
import org.n52.owsSupervisor.util.XmlTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x52North.sor.x031.CapabilitiesDocument;
import org.x52North.sor.x031.GetCapabilitiesDocument;

/**
 * @author Daniel Nüst
 * 
 */
public class SorCapabilitiesCheck extends OwsCapabilitiesCheck {

    private static Logger log = LoggerFactory.getLogger(SorCapabilitiesCheck.class);

    /**
     * 
     * @param serviceUrl
     * @param notifyEmail
     * @param checkIntervalMillis
     * @throws MalformedURLException
     */
    public SorCapabilitiesCheck(String serviceUrl, String notifyEmail, String checkIntervalMillis) throws MalformedURLException {
        this(new URL(serviceUrl), notifyEmail, Long.valueOf(checkIntervalMillis).longValue());
    }

    /**
     * 
     * @param owsVersion
     * @param service
     * @param notifyEmail
     * @param checkIntervalMillis
     */
    public SorCapabilitiesCheck(String owsVersion, URL service, String notifyEmail, long checkIntervalMillis) {
        super(owsVersion, service, notifyEmail, checkIntervalMillis);
    }

    /**
     * 
     * @param service
     * @param notifyEmail
     */
    public SorCapabilitiesCheck(URL service, String notifyEmail) {
        super(service, notifyEmail);
    }

    /**
     * 
     * @param service
     * @param notifyEmail
     * @param checkIntervalMillis
     */
    public SorCapabilitiesCheck(URL service, String notifyEmail, long checkIntervalMillis) {
        super(service, notifyEmail, checkIntervalMillis);
    }

    @Override
    public boolean check() {
        URL sUrl = getServiceURL();
        
        if (log.isDebugEnabled()) {
            log.debug("Checking SOS Capabilities for " + sUrl);
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

            // check it!
            if (response instanceof CapabilitiesDocument) {
                CapabilitiesDocument caps = (CapabilitiesDocument) response;
                log.debug("Parsed caps, serviceVersion: " + caps.getCapabilities().getVersion());

                // save the result
                addResult(new ServiceCheckResult(new Date(),
                                                 sUrl.toString(),
                                                 POSITIVE_TEXT,
                                                 ResultType.POSITIVE));
                return true;
            }
            addResult(new ServiceCheckResult(new Date(), sUrl.toString(), NEGATIVE_TEXT
                    + " ... Response was not a Capabilities document!", ResultType.NEGATIVE));
            return false;
        }
        catch (IOException e) {
            log.error("Could not send request", e);
            addResult(new ServiceCheckResult(new Date(), sUrl.toString(), NEGATIVE_TEXT
                    + " ... Could not send request: " + e.getMessage(), ResultType.NEGATIVE));
            return false;
        }
    }

    @Override
    public String toString() {
        return "SorCapabilitiesCheck [" + getService() + ", check interval=" + getCheckIntervalMillis() + "]";
    }

}
