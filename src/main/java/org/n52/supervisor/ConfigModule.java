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

package org.n52.supervisor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConfigModule extends AbstractModule {

    private static Logger log = LoggerFactory.getLogger(ConfigModule.class);

	private static final String USER_HOME_PATH = System.getProperty("user.home") + File.separator + ".SuperVisor" + File.separator;

	private static final String DEFAULT_PATH = "/";

    private static Properties loadProperties(final String name) {
    	log.trace("Loading properties '{}'", name);

        final Properties properties = new Properties();
    	try {
			InputStream is;
			String filePath = USER_HOME_PATH + name;
			final File userHomeFile = new File(filePath);
			if (!userHomeFile.exists()) {
				filePath = DEFAULT_PATH + name;
				is = ConfigModule.class.getResourceAsStream(filePath);
				log.info("Load default properties from '{}'.", filePath);
			} else if (!userHomeFile.canRead()) {
				log.warn("Could not load properties. No reading permissions for '{}'.", userHomeFile);
				log.info("Load default properties from jar file.");
				filePath = DEFAULT_PATH + name;
				is = ConfigModule.class.getResourceAsStream(filePath);
			} else {
				log.info("Load properties from '{}'.", userHomeFile);
				is = new FileInputStream(userHomeFile);
			}
			properties.load(is);
		} catch (final FileNotFoundException e) {
			final String msg = "SuperVisor properties not found.";
			log.error(msg, e);
			throw new RuntimeException(msg,e);
		} catch (final IOException e) {
			final String msg = "SuperVisor properties not readable.";
			log.error(msg, e);
			throw new RuntimeException(msg,e);
		}
    	// try storing properties in user.home
    	final File folder = new File(USER_HOME_PATH);
		if (!folder.exists() && !folder.mkdir()) {
			log.warn("SuperVisor properties could not be saved. No writing permissions at '{}'.", folder);
			return properties;
		}
		final File file = new File(USER_HOME_PATH + name);
		log.info("Save properties at '{}'.", file.getAbsolutePath());
		try { //save properties
			final OutputStream os = new FileOutputStream(file);
			properties.store(os, null);
		} catch (final IOException e) {
			log.error("SuperVisor properties could not be saved.", e);
		}
		return properties;
    }

    @Override
    protected void configure() {
        final Properties appProps = loadProperties("app.properties");
        Names.bindProperties(binder(), appProps);

        final Properties supervisorProps = loadProperties("supervisor.properties");
        Names.bindProperties(binder(), supervisorProps);

        // FIXME remove manual instantiation of property manager
        final SupervisorProperties sp = SupervisorProperties.getInstance(supervisorProps);
        log.warn("Instantiated property manager manually: {}", sp);

        log.info("Configured {}", this);
    }
}
