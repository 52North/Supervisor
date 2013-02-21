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
package org.n52.owsSupervisor.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.n52.owsSupervisor.ICheckerFactory;
import org.n52.owsSupervisor.IServiceChecker;
import org.n52.owsSupervisor.checks.SirCapabilitiesCheck;
import org.n52.owsSupervisor.checks.SorCapabilitiesCheck;
import org.n52.owsSupervisor.checks.SosCapabilitiesCheck;
import org.n52.owsSupervisor.checks.SosLatestObservationCheck;
import org.n52.owsSupervisor.checks.WpsCapabilitiesCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Nüst
 * 
 */
@SuppressWarnings("unused")
@Deprecated()
public class SWSL implements ICheckerFactory {

    private static final String EMAIL_DN = "d.nuest@52north.org";

    private static Logger log = LoggerFactory.getLogger(SWSL.class);

    /**
     * 
     */
    public SWSL() {
        //
    }
    
    /*
     * (non-Javadoc)
     * @see org.n52.owsSupervisor.data.ICheckerFactory#getCheckers()
     */
    @Override
    public Collection<IServiceChecker> getCheckers() {
        Collection<IServiceChecker> checkers = new ArrayList<IServiceChecker>();

        Collection<IServiceChecker> weathersos = initWeatherSOS(EMAIL_DN);
        checkers.addAll(weathersos);
        
        return checkers;
    }

    /**
     * @return 
     * 
     */
    private Collection<IServiceChecker> initBS() {
        Collection<IServiceChecker> checkers = new ArrayList<IServiceChecker>();
        
        // WPS @ giv-wps
        try {
            URL wps = new URL("http://giv-wps.uni-muenster.de:8080/wps/WebProcessingService");
            IServiceChecker capsChecker = new WpsCapabilitiesCheck("1.0.0",
                                                                   wps,
                                                                   "schaeffer@uni-muenster.de",
                                                                   EVERY_12_HOURS);
            checkers.add(capsChecker);
        }
        catch (MalformedURLException e) {
            log.error("Could not create URL for checker.", e);
        }
        
        return checkers;
    }

    /**
     * 
     * @param notificationEmail
     * @return 
     */
    private Collection<IServiceChecker> initGenesis(String notificationEmail) {
        Collection<IServiceChecker> checkers = new ArrayList<IServiceChecker>();
        
        // SIR @ giv-genesis
        try {
            URL sir = new URL("http://giv-genesis.uni-muenster.de:8080/SIR/sir");
            IServiceChecker capsChecker = new SirCapabilitiesCheck(sir, notificationEmail, EVERY_12_HOURS);
            checkers.add(capsChecker);
        }
        catch (MalformedURLException e) {
            log.error("Could not create URL for checker.", e);
        }

        // SOR @ giv-genesis
        try {
            URL sor = new URL("http://giv-genesis.uni-muenster.de:8080/SOR/sor");
            IServiceChecker capsChecker = new SorCapabilitiesCheck(sor, notificationEmail, EVERY_12_HOURS);
            checkers.add(capsChecker);
        }
        catch (MalformedURLException e) {
            log.error("Could not create URL for checker.", e);
        }

        // TODO check of RESTful SOR interface
        
        return checkers;
    }

    /**
     * 
     * @param notificationEmail
     * @return 
     */
    private Collection<IServiceChecker> initWeatherSOS(String notificationEmail) {
        Collection<IServiceChecker> checkers = new ArrayList<IServiceChecker>();
        
        // WeatherSOS
        try {
            URL weathersos = new URL("http://v-swe.uni-muenster.de:8080/WeatherSOS/sos");
            IServiceChecker capsChecker = new SosCapabilitiesCheck(weathersos, notificationEmail, EVERY_12_HOURS);
            checkers.add(capsChecker);

            // TODO create checks for latest observation autmatically from
            // capabilities document. Config file settings (for not all
            // offerings):
            // test.operation=GetObservation
            // test.intervalSecs=3600
            // test.allOfferings=FALSE
            // observation.time=latest
            // # ordered list of offering, property and procedure to be
            // # checked:
            // observation.offering=RAIN_GAUGE,LUMINANCE,LUMINANCE
            // observation.allProcedures=FALSE
            // observation.procedure=urn:ogc:object:feature:OSIRIS-HWS:3d3b239f-7696-4864-9d07-15447eae2b93,urn:ogc:object:feature:OSIRIS-HWS:3d3b239f-7696-4864-9d07-15447eae2b93,urn:ogc:object:feature:OSIRIS-HWS:3d3b239f-7696-4864-9d07-15447eae2b93
            // observation.allObservedProperties=FALSE
            // observation.observedProperties=urn:ogc:def:property:OGC::Precipitation1Hour,urn:ogc:def:property:OGC::Luminance,urn:ogc:def:property:OGC::Luminance
            // test.notificationEmail=mail@provider.org
            // service.type=SOS
            // service.url=http://xzy.org/sos
            String[] offerings = new String[] {"RAIN_GAUGE",
                                               "LUMINANCE",
                                               "HUMIDITY",
                                               "ATMOSPHERIC_PRESSURE",
                                               "ATMOSPHERIC_TEMPERATURE",
                                               "WIND_SPEED",
                                               "WIND_DIRECTION"};
            String[] obsProps = new String[] {"urn:ogc:def:property:OGC::Precipitation1Hour",
                                              "urn:ogc:def:property:OGC::Luminance",
                                              "urn:ogc:def:property:OGC::RelativeHumidity",
                                              "urn:ogc:def:property:OGC::BarometricPressure",
                                              "urn:ogc:def:property:OGC::Temperature",
                                              "urn:ogc:def:property:OGC::WindSpeed",
                                              "urn:ogc:def:property:OGC::WindDirection"};
            String proc = "urn:ogc:object:feature:OSIRIS-HWS:3d3b239f-7696-4864-9d07-15447eae2b93";
            long maximumAge = 1000 * 60 * 30;

            for (int i = 0; i < offerings.length; i++) {
                IServiceChecker checker = new SosLatestObservationCheck(weathersos,
                                                                        offerings[i],
                                                                        obsProps[i],
                                                                        proc,
                                                                        maximumAge,
                                                                        notificationEmail,
                                                                        EVERY_HALF_HOUR);

                checkers.add(checker);
            }
        }
        catch (MalformedURLException e) {
            log.error("Could not create URL for checker.", e);
        }
        
        return checkers;
    }
}
