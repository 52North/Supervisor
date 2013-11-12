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

package org.n52.supervisor.checks;

import java.io.IOException;
import java.net.MalformedURLException;
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
import org.n52.supervisor.ICheckResult.ResultType;
import org.n52.supervisor.util.XmlTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
@XmlRootElement
public class SosLatestObservationCheck extends AbstractServiceCheck {

    private static final String GET_OBS_RESPONSE_FORMAT = "text/xml;subtype=\"om/1.0.0\""; // text/xml;subtype="om/1.0.0

    private static final QName GET_OBS_RESULT_MODEL = new QName(XmlTools.OM_NAMESPACE_URI,
                                                                "Measurement",
                                                                XmlTools.OM_NAMESPACE_PREFIX);

    private static final String LATEST_OBSERVATION_VALUE = "latest";

    private static Logger log = LoggerFactory.getLogger(SosLatestObservationCheck.class);

    protected static final String NEGATIVE_TEXT = "Request for latest observation FAILED.";

    protected static final String POSITIVE_TEXT = "Successfully requested latest observation within time limits.";

    private static final String SOS_SERVICE = "SOS";

    private static final String SOS_VERSION = "1.0.0";

    private static final String TEMP_OP_PROPERTY_NAME = "om:samplingTime";

    private long checkInterval;

    private long maximumAgeOfObservationMillis;

    private String observedProp;

    private String off;

    private String proc;

    public SosLatestObservationCheck(String serviceUrl,
                                     String offering,
                                     String observedProperty,
                                     String procedure,
                                     String maximumAgeMillis,
                                     String notifyEmail,
                                     String checkIntervalMillis) throws NumberFormatException, MalformedURLException {
        this(new URL(serviceUrl),
             offering,
             observedProperty,
             procedure,
             Long.parseLong(maximumAgeMillis),
             notifyEmail,
             Long.parseLong(checkIntervalMillis));
    }

    public SosLatestObservationCheck(URL serviceURL,
                                     String offering,
                                     String observedProperty,
                                     String procedure,
                                     long maximumAgeMillis,
                                     String notifyEmail,
                                     long checkIntervalMillis) {
        super(notifyEmail, serviceURL);
        this.off = offering;
        this.observedProp = observedProperty;
        this.checkInterval = checkIntervalMillis;
        this.maximumAgeOfObservationMillis = maximumAgeMillis;
        this.proc = procedure;
    }

    private GetObservationDocument buildRequest() {
        // build the request
        GetObservationDocument getObs = GetObservationDocument.Factory.newInstance();
        GetObservation obs = getObs.addNewGetObservation();
        EventTime eventTime = obs.addNewEventTime();

        // TM_Equals
        TMEqualsDocument tmEqDoc = TMEqualsDocument.Factory.newInstance();

        BinaryTemporalOpType tmEq = tmEqDoc.addNewTMEquals();
        AbstractTimeObjectType timeInstType = tmEq.addNewTimeObject();
        TimeInstantType timeInst = net.opengis.gml.TimeInstantType.Factory.newInstance();
        TimePositionType timePos = timeInst.addNewTimePosition();
        timePos.setStringValue(LATEST_OBSERVATION_VALUE);
        timeInstType.set(timeInst);
        tmEq.addNewPropertyName();

        // workaround
        XmlCursor tmEqualsCursor = tmEq.newCursor();
        if (tmEqualsCursor.toChild(new QName("http://www.opengis.net/gml", "_TimeObject"))) {
            tmEqualsCursor.setName(new QName("http://www.opengis.net/gml", "TimeInstant"));
        }
        XmlCursor tmEqualsCursor2 = tmEq.newCursor();
        if (tmEqualsCursor2.toChild(new QName("http://www.opengis.net/ogc", "PropertyName"))) {
            tmEqualsCursor2.setTextValue(TEMP_OP_PROPERTY_NAME);
        }

        eventTime.addNewTemporalOps();

        eventTime.setTemporalOps(tmEqDoc.getTemporalOps());

        XmlCursor cursor = eventTime.newCursor();
        if (cursor.toChild(new QName("http://www.opengis.net/ogc", "temporalOps"))) {
            cursor.setName(new QName("http://www.opengis.net/ogc", "TM_Equals"));
        }
        // binOp.set(tmEqDoc);

        // rest
        obs.addProcedure(this.proc);
        obs.setOffering(this.off);
        obs.addObservedProperty(this.observedProp);
        obs.setService(SOS_SERVICE);
        obs.setVersion(SOS_VERSION);
        // obs.addNewFeatureOfInterest().addNewObjectID().setStringValue(foi);
        obs.setResponseFormat(GET_OBS_RESPONSE_FORMAT);
        obs.setResultModel(GET_OBS_RESULT_MODEL);

        return getObs;
    }

    @Override
    public boolean check() {
        URL sUrl = getServiceURL();

        // max age
        Date maxAge = new Date(System.currentTimeMillis() - this.maximumAgeOfObservationMillis);

        log.debug("Checking for latest observation " + this.off + "/" + this.observedProp + " which must be after "
                + maxAge);

        clearResults();

        // build the request
        GetObservationDocument getObsDoc = buildRequest();

        // send the document and check response
        try {
            XmlObject response = this.client.xSendPostRequest(sUrl.toString(), getObsDoc);
            getObsDoc = null;

            // check it!
            if (response instanceof ObservationCollectionDocument) {
                ObservationCollectionDocument obsColl = (ObservationCollectionDocument) response;
                return checkObservationCollection(maxAge, obsColl);
            }

            boolean b = saveAndReturnNegativeResult(NEGATIVE_TEXT + getObservationString()
                    + " ... Response was not the correct document: "
                    + new String(response.xmlText().substring(0, Math.max(200, response.xmlText().length()))));
            response = null;

            return b;
        }
        catch (IOException e) {
            log.error("Could not send request", e);
            return saveAndReturnNegativeResult(NEGATIVE_TEXT + getObservationString() + " ... Could not send request!");
        }
    }

    private boolean checkObservationCollection(Date maxAge, ObservationCollectionDocument obsColl) {
        ObservationPropertyType observation = obsColl.getObservationCollection().getMemberArray(0);

        TimeObjectPropertyType samplingTime = observation.getObservation().getSamplingTime();
        AbstractTimeObjectType timeObj = samplingTime.getTimeObject();

        if (timeObj instanceof TimeInstantType) {
            TimeInstantType timeInstant = (TimeInstantType) timeObj;
            String timeString = timeInstant.getTimePosition().getStringValue();
            log.debug("Parsed response, latest observation was at " + timeString);

            try {
                Date timeToCheck = ISO8601LocalFormat.parse(timeString);
                if (timeToCheck.after(maxAge)) {
                    // ALL OKAY - save the result
                    addResult(new ServiceCheckResult(new Date(), getServiceURL().toString(), POSITIVE_TEXT
                            + getObservationString(), ResultType.POSITIVE));
                    return true;
                }

                // to old!
                return saveAndReturnNegativeResult(NEGATIVE_TEXT + " " + this.observedProp + " " + this.proc
                        + " -- latest observation is too old (" + timeString + ")!");
            }
            catch (ParseException e) {
                log.error("Could not parse sampling time " + timeString, e);
                return saveAndReturnNegativeResult(NEGATIVE_TEXT + getObservationString()
                        + " -- Could not parse the given time " + timeString + ".");
            }
        }
        log.warn("Response does not contain time instant, not handling this!");
        return saveAndReturnNegativeResult(NEGATIVE_TEXT + getObservationString()
                + " -- Response did not contain TimeInstant as samplingTime!");
    }

    @Override
    public long getCheckIntervalMillis() {
        return this.checkInterval;
    }

    private String getObservationString() {
        return " Offering: " + this.off + "; Observed property: " + this.observedProp + "; Procedure: " + this.proc
                + ".";
    }

    private boolean saveAndReturnNegativeResult(String text) {
        addResult(new ServiceCheckResult(new Date(), getServiceURL().toString(), text, ResultType.NEGATIVE));
        return false;
    }

    @Override
    public String toString() {
        return "SosLatestObservationCheck [" + getService() + ", check interval=" + getCheckIntervalMillis()
                + ", offering=" + this.off + ", observed property=" + this.observedProp + ", procedure=" + this.proc
                + ", maximum age=" + this.maximumAgeOfObservationMillis + "]";
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public long getMaximumAgeOfObservationMillis() {
        return maximumAgeOfObservationMillis;
    }

    public void setMaximumAgeOfObservationMillis(long maximumAgeOfObservationMillis) {
        this.maximumAgeOfObservationMillis = maximumAgeOfObservationMillis;
    }

    public String getObservedProp() {
        return observedProp;
    }

    public void setObservedProp(String observedProp) {
        this.observedProp = observedProp;
    }

    public String getOff() {
        return off;
    }

    public void setOff(String off) {
        this.off = off;
    }

    public String getProc() {
        return proc;
    }

    public void setProc(String proc) {
        this.proc = proc;
    }
    
    @Override
    public String getType() {
        return "SosLatestObservationCheck";
    }
}
