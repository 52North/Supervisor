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

import net.opengis.wps.x100.CapabilitiesDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.owsSupervisor.ICheckResult.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Uses GET only
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class WpsCapabilitiesCheck extends OwsCapabilitiesCheck {

    private static Logger log = LoggerFactory.getLogger(WpsCapabilitiesCheck.class);

    private String getRequest;

    /**
     * 
     * @param owsVersion
     * @param serviceUrl
     * @param notifyEmail
     * @param checkIntervalMillis
     * @throws NumberFormatException
     * @throws MalformedURLException
     */
    public WpsCapabilitiesCheck(String owsVersion, String serviceUrl, String notifyEmail, String checkIntervalMillis) throws NumberFormatException,
            MalformedURLException {
        this(owsVersion, new URL(serviceUrl), notifyEmail, Long.valueOf(checkIntervalMillis).longValue());
    }

    /**
     * @param owsVersion
     * @param service
     * @param notifyEmail
     * @param checkIntervalMillis
     */
    public WpsCapabilitiesCheck(String owsVersion, URL service, String notifyEmail, long checkIntervalMillis) {
        super(owsVersion, service, notifyEmail, checkIntervalMillis);

        this.getRequest = buildGetRequest();
    }

    /**
     * @return
     */
    private String buildGetRequest() {
        return "Request=GetCapabilities&Service=WPS&serviceVersion=" + this.serviceVersion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.OwsCapabilitiesCheck#check()
     */
    @Override
    public boolean check() {
        URL sUrl = getServiceURL();
        
        if (log.isDebugEnabled()) {
            log.debug("Checking WPS Capabilities via GET " + sUrl);
        }

        clearResults();

        // send the request
        try {
            XmlObject response = this.client.xSendGetRequest(sUrl.toString(), this.getRequest);

            // parse response - this is the test!
            CapabilitiesDocument caps = CapabilitiesDocument.Factory.parse(response.getDomNode());
            log.debug("Parsed caps with serviceVersion " + caps.getCapabilities().getVersion());
        }
        catch (IOException e) {
            log.error("Could not send request", e);
            addResult(new ServiceCheckResult(new Date(), sUrl.toString(), NEGATIVE_TEXT
                    + " ... Could not send request: " + e.getMessage(), ResultType.NEGATIVE));
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

}
