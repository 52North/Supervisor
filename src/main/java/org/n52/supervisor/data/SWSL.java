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

package org.n52.supervisor.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.n52.supervisor.ICheckFactory;
import org.n52.supervisor.ICheckRunner;
import org.n52.supervisor.checks.Check;
import org.n52.supervisor.checks.SosLatestObservationCheckRunner;
import org.n52.supervisor.checks.ows.SirCapabilitiesCheckRunner;
import org.n52.supervisor.checks.ows.SorCapabilitiesCheckRunner;
import org.n52.supervisor.checks.ows.SosCapabilitiesCheckRunner;
import org.n52.supervisor.checks.ows.WpsCapabilitiesCheckRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
@SuppressWarnings("unused")
@Deprecated()
public class SWSL implements ICheckFactory {

    private static final String EMAIL_DN = "d.nuest@52north.org";

    private static Logger log = LoggerFactory.getLogger(SWSL.class);

    public SWSL() {
        log.info("NEW {}", this);
    }

    @Override
    public Collection<Check> getChecks() {
        Collection<Check> checks = new ArrayList<Check>();

        // Collection<Check> weathersos = initWeatherSOS(EMAIL_DN);
        // checks.addAll(weathersos);

        return checks;
    }

    // private Collection<ICheckRunner> initBS() {
    // Collection<ICheckRunner> checkers = new ArrayList<ICheckRunner>();
    //
    // // WPS @ giv-wps
    // try {
    // URL wps = new URL("http://giv-wps.uni-muenster.de:8080/wps/WebProcessingService");
    // ICheckRunner capsChecker = new WpsCapabilitiesCheckRunner("1.0.0",
    // wps,
    // "schaeffer@uni-muenster.de",
    // EVERY_12_HOURS);
    // checkers.add(capsChecker);
    // }
    // catch (MalformedURLException e) {
    // log.error("Could not create URL for checker.", e);
    // }
    //
    // return checkers;
    // }

    // private Collection<ICheckRunner> initGenesis(String notificationEmail) {
    // Collection<ICheckRunner> checkers = new ArrayList<ICheckRunner>();
    //
    // // SIR @ giv-genesis
    // try {
    // URL sir = new URL("http://giv-genesis.uni-muenster.de:8080/SIR/sir");
    // ICheckRunner capsChecker = new SirCapabilitiesCheckRunner(sir, notificationEmail, EVERY_12_HOURS);
    // checkers.add(capsChecker);
    // }
    // catch (MalformedURLException e) {
    // log.error("Could not create URL for checker.", e);
    // }
    //
    // // SOR @ giv-genesis
    // try {
    // URL sor = new URL("http://giv-genesis.uni-muenster.de:8080/SOR/sor");
    // ICheckRunner capsChecker = new SorCapabilitiesCheckRunner(sor, notificationEmail, EVERY_12_HOURS);
    // checkers.add(capsChecker);
    // }
    // catch (MalformedURLException e) {
    // log.error("Could not create URL for checker.", e);
    // }
    //
    // // TODO check of RESTful SOR interface
    //
    // return checkers;
    // }
    //
    // private Collection<ICheckRunner> initWeatherSOS(String notificationEmail) {
    // Collection<ICheckRunner> checkers = new ArrayList<ICheckRunner>();
    //
    // // WeatherSOS
    // try {
    // URL weathersos = new URL("http://v-swe.uni-muenster.de:8080/WeatherSOS/sos");
    // ICheckRunner capsChecker = new SosCapabilitiesCheckRunner(weathersos, notificationEmail,
    // EVERY_12_HOURS);
    // checkers.add(capsChecker);
    //
    // // TODO create checks for latest observation autmatically from
    // // capabilities document. Config file settings (for not all
    // // offerings):
    // // test.operation=GetObservation
    // // test.intervalSecs=3600
    // // test.allOfferings=FALSE
    // // observation.time=latest
    // // # ordered list of offering, property and procedure to be
    // // # checked:
    // // observation.offering=RAIN_GAUGE,LUMINANCE,LUMINANCE
    // // observation.allProcedures=FALSE
    // //
    // observation.procedure=urn:ogc:object:feature:OSIRIS-HWS:3d3b239f-7696-4864-9d07-15447eae2b93,urn:ogc:object:feature:OSIRIS-HWS:3d3b239f-7696-4864-9d07-15447eae2b93,urn:ogc:object:feature:OSIRIS-HWS:3d3b239f-7696-4864-9d07-15447eae2b93
    // // observation.allObservedProperties=FALSE
    // //
    // observation.observedProperties=urn:ogc:def:property:OGC::Precipitation1Hour,urn:ogc:def:property:OGC::Luminance,urn:ogc:def:property:OGC::Luminance
    // // test.notificationEmail=mail@provider.org
    // // service.type=SOS
    // // service.url=http://xzy.org/sos
    // String[] offerings = new String[] {"RAIN_GAUGE",
    // "LUMINANCE",
    // "HUMIDITY",
    // "ATMOSPHERIC_PRESSURE",
    // "ATMOSPHERIC_TEMPERATURE",
    // "WIND_SPEED",
    // "WIND_DIRECTION"};
    // String[] obsProps = new String[] {"urn:ogc:def:property:OGC::Precipitation1Hour",
    // "urn:ogc:def:property:OGC::Luminance",
    // "urn:ogc:def:property:OGC::RelativeHumidity",
    // "urn:ogc:def:property:OGC::BarometricPressure",
    // "urn:ogc:def:property:OGC::Temperature",
    // "urn:ogc:def:property:OGC::WindSpeed",
    // "urn:ogc:def:property:OGC::WindDirection"};
    // String proc = "urn:ogc:object:feature:OSIRIS-HWS:3d3b239f-7696-4864-9d07-15447eae2b93";
    // long maximumAge = 1000 * 60 * 30;
    //
    // for (int i = 0; i < offerings.length; i++) {
    // ICheckRunner checker = new SosLatestObservationCheckRunner(weathersos,
    // offerings[i],
    // obsProps[i],
    // proc,
    // maximumAge,
    // notificationEmail,
    // EVERY_HALF_HOUR);
    //
    // checkers.add(checker);
    // }
    // }
    // catch (MalformedURLException e) {
    // log.error("Could not create URL for checker.", e);
    // }
    //
    // return checkers;
    // }
}
