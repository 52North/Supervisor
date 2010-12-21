/*******************************************************************************
Copyright (C) 2010
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License serviceVersion 2 as published by the 
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

Author: Daniel Nüst
 
 ******************************************************************************/

package org.n52.owsSupervisor.checks;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import net.opengis.wps.x100.CapabilitiesDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.owsSupervisor.checks.ICheckResult.ResultType;

/**
 * 
 * Uses GET only
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class WpsCapabilitiesCheck extends OwsCapabilitiesCheck {

    private static Logger log = Logger.getLogger(WpsCapabilitiesCheck.class);

    private String getRequest;

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
        if (log.isDebugEnabled()) {
            log.debug("Checking WPS Capabilities via GET " + this.serviceUrl);
        }

        clearResults();

        // send the request
        try {
            XmlObject response = this.client.xSendGetRequest(this.serviceUrl.toString(), this.getRequest);

            // parse response - this is the test!
            CapabilitiesDocument caps = CapabilitiesDocument.Factory.parse(response.getDomNode());
            log.debug("Parsed caps with serviceVersion " + caps.getCapabilities().getVersion());
        }
        catch (IOException e) {
            log.error("Could not send request", e);
            addResult(new ServiceCheckResult(new Date(), this.serviceUrl.toString(), NEGATIVE_TEXT
                    + " ... Could not send request!", ResultType.NEGATIVE));
            return false;
        }
        catch (XmlException e) {
            log.error("Could not send request", e);
            addResult(new ServiceCheckResult(new Date(), this.serviceUrl.toString(), NEGATIVE_TEXT
                    + " ... Could not parse response to CapabilitiesBaseType!", ResultType.NEGATIVE));
            return false;
        }

        // save the good result
        addResult(new ServiceCheckResult(new Date(), this.serviceUrl.toString(), POSITIVE_TEXT, ResultType.POSITIVE));

        return true;
    }

}
