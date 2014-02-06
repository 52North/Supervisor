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
import javax.xml.namespace.QName;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.sos.x20.GetObservationDocument;

import org.apache.xmlbeans.XmlObject;
import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.n52.supervisor.util.XmlTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
@XmlRootElement
public class SosV2LatestObservationCheckRunner extends AbstractServiceCheckRunner {

    private static final String GET_OBS_RESPONSE_FORMAT = "text/xml;subtype=\"om/1.0.0\""; // text/xml;subtype="om/1.0.0

    private static final QName GET_OBS_RESULT_MODEL = new QName(XmlTools.OM_NAMESPACE_URI,
                                                                "Measurement",
                                                                XmlTools.OM_NAMESPACE_PREFIX);

    private static final String LATEST_OBSERVATION_VALUE = "latest";

    private static Logger log = LoggerFactory.getLogger(SosV2LatestObservationCheckRunner.class);

    protected static final String NEGATIVE_TEXT = "Request for latest observation FAILED.";

    protected static final String POSITIVE_TEXT = "Successfully requested latest observation within time limits.";

    private static final String SOS_SERVICE = "SOS";

    private static final String SOS_VERSION = "2.0";

    private static final String TEMP_OP_PROPERTY_NAME = "om:samplingTime";

    public SosV2LatestObservationCheckRunner(SosLatestObservationCheck check) {
        super(check);
    }

    private GetObservationDocument buildRequest() {
        // TODO build the request - use OX-F here?
        return null;
    }

    @Override
    public boolean check() {
        URL sUrl = this.c.getServiceUrl();

        Date maxAge = new Date(System.currentTimeMillis() - (theCheck().getMaximumAgeSeconds() * 1000));

        log.debug("Checking for latest observation with {}", theCheck());

        clearResults();

        GetObservationDocument getObsDoc = buildRequest();

        try {
            XmlObject response = this.client.xSendPostRequest(sUrl.toString(), getObsDoc);
            getObsDoc = null;

            if (response instanceof ObservationCollectionDocument) {
                ObservationCollectionDocument obsColl = (ObservationCollectionDocument) response;
                return checkObservationCollection(maxAge, obsColl);
            }

            boolean b = saveAndReturnNegativeResult(NEGATIVE_TEXT
                    + " ... Response was not the correct document: "
                    + new String(response.xmlText().substring(0, Math.max(200, response.xmlText().length()))));
            response = null;

            return b;
        }
        catch (Exception e) {
            log.error("Error during check", e);
            return saveAndReturnNegativeResult(NEGATIVE_TEXT + " -- ERROR: " + e.getMessage());
        }
    }

    private boolean checkObservationCollection(Date maxAge, ObservationCollectionDocument obsColl) {
        // implement metho
        return false;
    }

    private SosLatestObservationCheck theCheck() {
        return (SosLatestObservationCheck) this.c;
    }

}
