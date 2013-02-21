/**
 * ﻿Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.owsSupervisor.checks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import net.opengis.sos.x10.CapabilitiesDocument;
import net.opengis.sos.x10.GetCapabilitiesDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.n52.owsSupervisor.ICheckResult.ResultType;
import org.n52.owsSupervisor.util.XmlTools;

/**
 * @author Daniel Nüst
 * 
 */
public class SosCapabilitiesCheck extends OwsCapabilitiesCheck {

    private static Logger log = Logger.getLogger(SosCapabilitiesCheck.class);

    private static final String SOS_SERVICE = "SOS";

    /**
     * 
     * @param serviceUrl
     * @param notifyEmail
     * @param checkIntervalMillis
     * @throws NumberFormatException
     * @throws MalformedURLException
     */
    public SosCapabilitiesCheck(String serviceUrl, String notifyEmail, String checkIntervalMillis) throws NumberFormatException,
            MalformedURLException {
        super(new URL(serviceUrl), notifyEmail, Long.parseLong(checkIntervalMillis));
    }

    /**
     * 
     * @param owsVersion
     * @param service
     * @param notifyEmail
     * @param checkIntervalMillis
     */
    public SosCapabilitiesCheck(String owsVersion, URL service, String notifyEmail, long checkIntervalMillis) {
        super(owsVersion, service, notifyEmail, checkIntervalMillis);
    }

    /**
     * 
     * @param service
     * @param notifyEmail
     */
    public SosCapabilitiesCheck(URL service, String notifyEmail) {
        super(service, notifyEmail);
    }

    /**
     * 
     * @param service
     * @param notifyEmail
     * @param checkIntervalMillis
     */
    public SosCapabilitiesCheck(URL service, String notifyEmail, long checkIntervalMillis) {
        super(service, notifyEmail, checkIntervalMillis);
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
        getCapDoc.addNewGetCapabilities().setService(SOS_SERVICE);

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

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.OwsCapabilitiesCheck#toString()
     */
    @Override
    public String toString() {
        return "SosCapabilitiesCheck [" + getService() + ", check interval=" + getCheckIntervalMillis() + "]";
    }

}
