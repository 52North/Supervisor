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

package org.n52.supervisor.guice;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.n52.supervisor.SupervisorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConfigModule extends AbstractModule {

    private static Logger log = LoggerFactory.getLogger(ConfigModule.class);

    @Override
    protected void configure() {
        Properties appProps = loadProperties("/app.properties");
        Names.bindProperties(binder(), appProps);

        Properties supervisorProps = loadProperties("/supervisor.properties");
        Names.bindProperties(binder(), supervisorProps);

        // FIXME remove manual instantiation of property manager
        SupervisorProperties sp = SupervisorProperties.getInstance(supervisorProps);
        log.warn("Instantiated property manager manually: {}", sp);
        
        log.info("Configured {}", this);
    }

    private static Properties loadProperties(String name) {
        URL url = ConfigModule.class.getResource(name);
        log.trace("Loading properties for {} from {}", name, url);

        Properties properties = new Properties();
        try (InputStream s = url.openStream();) {
            properties.load(s);
            log.trace("Loaded properties: {}", properties);
        }
        catch (Exception e) {
            log.error("Could not load properties file.", e);
        }

        return properties;
    }
}
