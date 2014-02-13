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
import java.text.ParseException;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import net.opengis.gml.AbstractTimeObjectType;
import net.opengis.gml.TimeInstantType;
import net.opengis.gml.TimePositionType;
import net.opengis.ogc.BinaryTemporalOpType;
import net.opengis.ogc.TMEqualsDocument;
import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationPropertyType;
import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.GetObservationDocument.GetObservation.EventTime;
import net.opengis.swe.x101.TimeObjectPropertyType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.n52.supervisor.api.CheckResult;
import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.n52.supervisor.checks.ServiceCheckResult;
import org.n52.supervisor.util.XmlTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 *
 */
@XmlRootElement
public class SosLatestObservationCheckRunner extends AbstractServiceCheckRunner {

    private static final String GET_OBS_RESPONSE_FORMAT = "text/xml;subtype=\"om/1.0.0\""; // text/xml;subtype="om/1.0.0

    private static final QName GET_OBS_RESULT_MODEL = new QName(XmlTools.OM_NAMESPACE_URI,
                                                                "Measurement",
                                                                XmlTools.OM_NAMESPACE_PREFIX);

    private static final String LATEST_OBSERVATION_VALUE = "latest";

    private static Logger log = LoggerFactory.getLogger(SosLatestObservationCheckRunner.class);

    protected static final String NEGATIVE_TEXT = "Request for latest observation FAILED.";

    protected static final String POSITIVE_TEXT = "Successfully requested latest observation within time limits.";

    private static final String SOS_SERVICE = "SOS";

    private static final String SOS_VERSION = "1.0.0";

    private static final String TEMP_OP_PROPERTY_NAME = "om:samplingTime";

    public SosLatestObservationCheckRunner(final SosLatestObservationCheck check) {
        super(check);
    }

    private GetObservationDocument buildRequest() {
        // build the request
        final GetObservationDocument getObs = GetObservationDocument.Factory.newInstance();
        final GetObservation obs = getObs.addNewGetObservation();
        final EventTime eventTime = obs.addNewEventTime();

        // TM_Equals
        final TMEqualsDocument tmEqDoc = TMEqualsDocument.Factory.newInstance();

        final BinaryTemporalOpType tmEq = tmEqDoc.addNewTMEquals();
        final AbstractTimeObjectType timeInstType = tmEq.addNewTimeObject();
        final TimeInstantType timeInst = net.opengis.gml.TimeInstantType.Factory.newInstance();
        final TimePositionType timePos = timeInst.addNewTimePosition();
        timePos.setStringValue(LATEST_OBSERVATION_VALUE);
        timeInstType.set(timeInst);
        tmEq.addNewPropertyName();

        // workaround
        final XmlCursor tmEqualsCursor = tmEq.newCursor();
        if (tmEqualsCursor.toChild(new QName("http://www.opengis.net/gml", "_TimeObject"))) {
            tmEqualsCursor.setName(new QName("http://www.opengis.net/gml", "TimeInstant"));
        }
        final XmlCursor tmEqualsCursor2 = tmEq.newCursor();
        if (tmEqualsCursor2.toChild(new QName("http://www.opengis.net/ogc", "PropertyName"))) {
            tmEqualsCursor2.setTextValue(TEMP_OP_PROPERTY_NAME);
        }

        eventTime.addNewTemporalOps();

        eventTime.setTemporalOps(tmEqDoc.getTemporalOps());

        final XmlCursor cursor = eventTime.newCursor();
        if (cursor.toChild(new QName("http://www.opengis.net/ogc", "temporalOps"))) {
            cursor.setName(new QName("http://www.opengis.net/ogc", "TM_Equals"));
        }
        // binOp.set(tmEqDoc);

        obs.addProcedure(theCheck().getProcedure());
        obs.setOffering(theCheck().getOffering());
        obs.addObservedProperty(theCheck().getObservedProperty());
        obs.setService(SOS_SERVICE);
        obs.setVersion(SOS_VERSION);
        // obs.addNewFeatureOfInterest().addNewObjectID().setStringValue(foi);
        obs.setResponseFormat(GET_OBS_RESPONSE_FORMAT);
        obs.setResultModel(GET_OBS_RESULT_MODEL);

        return getObs;
    }

    @Override
    public boolean check() {
        final URL sUrl = check.getServiceUrl();

        // max age
        final Date maxAge = new Date(System.currentTimeMillis() - (theCheck().getMaximumAgeSeconds() * 1000));

        log.debug("Checking for latest observation with {}", theCheck());

        clearResults();

        // build the request
        GetObservationDocument getObsDoc = buildRequest();

        // send the document and check response
        try {
            XmlObject response = client.xSendPostRequest(sUrl.toString(), getObsDoc);
            getObsDoc = null;

            // check it!
            if (response instanceof ObservationCollectionDocument) {
                final ObservationCollectionDocument obsColl = (ObservationCollectionDocument) response;
                return checkObservationCollection(maxAge, obsColl);
            }

            final boolean b = saveAndReturnNegativeResult(NEGATIVE_TEXT + getObservationString()
                    + " ... Response was not the correct document: "
                    + new String(response.xmlText().substring(0, Math.max(200, response.xmlText().length()))));
            response = null;

            return b;
        }
        catch (final Exception e) {
            log.error("Error during check", e);
            return saveAndReturnNegativeResult(NEGATIVE_TEXT + getObservationString() + " -- ERROR: " + e.getMessage());
        }
    }

    private boolean checkObservationCollection(final Date maxAge, final ObservationCollectionDocument obsColl) {
        final ObservationPropertyType observation = obsColl.getObservationCollection().getMemberArray(0);

        final TimeObjectPropertyType samplingTime = observation.getObservation().getSamplingTime();
        final AbstractTimeObjectType timeObj = samplingTime.getTimeObject();

        if (timeObj instanceof TimeInstantType) {
            final TimeInstantType timeInstant = (TimeInstantType) timeObj;
            final String timeString = timeInstant.getTimePosition().getStringValue();
            log.debug("Parsed response, latest observation was at " + timeString);

            try {
                final Date timeToCheck = ISO8601LocalFormat.parse(timeString);
                if (timeToCheck.after(maxAge)) {
                    // ALL OKAY - save the result
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

                // to old!
                return saveAndReturnNegativeResult(
                		String.format("%s %s %s  -- latest observation is too old (%s)!",
                				NEGATIVE_TEXT,
                				theCheck().getOffering(),
                				theCheck().getProcedure(),
                				timeString));
            }
            catch (final ParseException e) {
                log.error("Could not parse sampling time " + timeString, e);
                return saveAndReturnNegativeResult(NEGATIVE_TEXT + getObservationString()
                        + " -- Could not parse the given time " + timeString + ".");
            }
        }
        log.warn("Response does not contain time instant, not handling this!");
        return saveAndReturnNegativeResult(NEGATIVE_TEXT + getObservationString()
                + " -- Response did not contain TimeInstant as samplingTime!");
    }

    private String getObservationString() {
        final SosLatestObservationCheck sloc = theCheck();
        return String.format(" Offering: %s; Observed property: %s; Procedure: %s.",
                             sloc.getOffering(),
                             sloc.getObservedProperty(),
                             sloc.getProcedure());
    }

    private SosLatestObservationCheck theCheck() {
        return (SosLatestObservationCheck) check;
    }

}
