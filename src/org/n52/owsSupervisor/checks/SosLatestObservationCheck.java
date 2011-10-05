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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractTimeObjectType;
import net.opengis.gml.TimeInstantType;
import net.opengis.gml.TimePositionType;
import net.opengis.ogc.BinaryTemporalOpType;
import net.opengis.ogc.TEqualsDocument;
import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationPropertyType;
import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.GetObservationDocument.GetObservation.EventTime;
import net.opengis.swe.x101.TimeObjectPropertyType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.n52.owsSupervisor.checks.ICheckResult.ResultType;
import org.n52.owsSupervisor.util.XmlTools;

/**
 * @author Daniel Nüst
 * 
 */
public class SosLatestObservationCheck extends AbstractServiceCheck {

    private static final String GET_OBS_RESPONSE_FORMAT = "text/xml;subtype=\"om/1.0.0\""; // text/xml;subtype="om/1.0.0

    private static final QName GET_OBS_RESULT_MODEL = new QName(XmlTools.OM_NAMESPACE_URI,
                                                                "Measurement",
                                                                XmlTools.OM_NAMESPACE_PREFIX);

    private static final String LATEST_OBSERVATION_VALUE = "latest";

    private static Logger log = Logger.getLogger(SosLatestObservationCheck.class);

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

    /**
     * 
     * @param serviceUrl
     * @param offering
     * @param observedProperty
     * @param procedure
     * @param maximumAgeMillis
     * @param notifyEmail
     * @param checkIntervalMillis
     * @throws NumberFormatException
     * @throws MalformedURLException
     */
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

    /**
     * 
     * @param service
     * @param offering
     * @param observedProperty
     * @param procedure
     * @param maximumAgeMillis
     * @param notifyEmail
     * @param checkIntervalMillis
     */
    public SosLatestObservationCheck(URL service,
                                     String offering,
                                     String observedProperty,
                                     String procedure,
                                     long maximumAgeMillis,
                                     String notifyEmail,
                                     long checkIntervalMillis) {
        super(notifyEmail);
        this.off = offering;
        this.observedProp = observedProperty;
        this.checkInterval = checkIntervalMillis;
        this.serviceUrl = service;
        this.maximumAgeOfObservationMillis = maximumAgeMillis;
        this.proc = procedure;
    }

    private GetObservationDocument buildRequest() {
        // build the request
        GetObservationDocument getObs = GetObservationDocument.Factory.newInstance();
        GetObservation obs = getObs.addNewGetObservation();
        EventTime eventTime = obs.addNewEventTime();

        // TM_Equals
        TEqualsDocument tmEqDoc = TEqualsDocument.Factory.newInstance();

        BinaryTemporalOpType tmEq = tmEqDoc.addNewTEquals();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#check()
     */
    @Override
    public boolean check() {
        // max age
        Date maxAge = new Date(System.currentTimeMillis() - this.maximumAgeOfObservationMillis);

        log.debug("Checking for latest observation " + this.off + "/" + this.observedProp + " which must be after "
                + maxAge);

        clearResults();

        // build the request
        GetObservationDocument getObsDoc = buildRequest();

        // send the document and check response
        try {
            XmlObject response = this.client.xSendPostRequest(this.serviceUrl.toString(), getObsDoc);
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
                    addResult(new ServiceCheckResult(new Date(), this.serviceUrl.toString(), POSITIVE_TEXT
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

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#getCheckIntervalMillis()
     */
    @Override
    public long getCheckIntervalMillis() {
        return this.checkInterval;
    }

    private String getObservationString() {
        return " Offering: " + this.off + "; Observed property: " + this.observedProp + "; Procedure: " + this.proc
                + ".";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.AbstractServiceCheck#getService()
     */
    @Override
    public String getService() {
        return this.serviceUrl.toString();
    }

    private boolean saveAndReturnNegativeResult(String text) {
        addResult(new ServiceCheckResult(new Date(), this.serviceUrl.toString(), text, ResultType.NEGATIVE));
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SosLatestObservationCheck [" + getService() + ", check interval=" + getCheckIntervalMillis()
                + ", offering/bserved property/procedure=" + this.observedProp + "/" + this.off + "/" + this.proc
                + ", maximum age=" + this.maximumAgeOfObservationMillis + "]";
    }
}
