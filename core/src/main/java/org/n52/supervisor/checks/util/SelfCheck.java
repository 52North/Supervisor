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

package org.n52.supervisor.checks.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import javax.xml.bind.annotation.XmlRootElement;

import org.n52.supervisor.checks.ServiceCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel
 * 
 */
@XmlRootElement
public class SelfCheck extends ServiceCheck {

    private static Logger log = LoggerFactory.getLogger(SelfCheck.class);

    protected String type = "SelfCheck";

    public SelfCheck() {
        super();
    }

    public SelfCheck(String identifier) {
        super(identifier);
    }

    public SelfCheck(String notificationEmail, long intervalSeconds, URL serviceUrl) {
        super(notificationEmail, intervalSeconds, serviceUrl);

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                if ( !current.isUp() || current.isLoopback() || current.isVirtual())
                    continue;
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress currentAddress = addresses.nextElement();
                    if (currentAddress.isLoopbackAddress())
                        continue;

                    if (currentAddress instanceof Inet4Address) {
                        setServiceIdentifier(currentAddress.getHostAddress());
                        break;
                    }
                }
            }
        }
        catch (SocketException e) {
            log.warn("Could not detect IP", e);
        }
    }

    public SelfCheck(String notificationEmail, String intervalSeconds, String serviceUrl) throws NumberFormatException,
            MalformedURLException {
        this(notificationEmail, Long.valueOf(intervalSeconds).longValue(), new URL(serviceUrl));

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SelfCheck [");
        if (type != null) {
            builder.append("type=");
            builder.append(type);
            builder.append(", ");
        }
        if (getServiceIdentifier() != null) {
            builder.append("getServiceIdentifier()=");
            builder.append(getServiceIdentifier());
            builder.append(", ");
        }
        if (getServiceUrl() != null) {
            builder.append("getServiceUrl()=");
            builder.append(getServiceUrl());
            builder.append(", ");
        }
        if (getIdentifier() != null) {
            builder.append("getIdentifier()=");
            builder.append(getIdentifier());
            builder.append(", ");
        }
        builder.append("getIntervalSeconds()=");
        builder.append(getIntervalSeconds());
        builder.append(", ");
        if (getType() != null) {
            builder.append("getType()=");
            builder.append(getType());
            builder.append(", ");
        }
        if (getNotificationEmail() != null) {
            builder.append("getNotificationEmail()=");
            builder.append(getNotificationEmail());
        }
        builder.append("]");
        return builder.toString();
    }

}
