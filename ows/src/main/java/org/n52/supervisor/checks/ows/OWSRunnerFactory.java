/**
 * ﻿Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source Software GmbH
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

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.checks.RunnerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OWSRunnerFactory implements RunnerFactory {

	private static final Logger log = LoggerFactory
			.getLogger(OWSRunnerFactory.class);

	@Override
	public CheckRunner resolveRunner(Check check) {
		if (check instanceof OwsCapabilitiesCheck) {
			final OwsCapabilitiesCheck occ = (OwsCapabilitiesCheck) check;
			final String serviceType = occ.getServiceType();

			switch (serviceType) {
			case "SOS":
				return new SosCapabilitiesCheckRunner(occ);
			case "SIR":
				return new SirCapabilitiesCheckRunner(occ);
			case "WPS":
				return new WpsCapabilitiesCheckRunner(occ);
			case "SPS":
				return new SpsCapabilitiesCheckRunner(occ);
			default:
				log.error(
						"Configured Servicetype '{}' is not supported! Using generic Checker.",
						serviceType);
				return new OwsCapabilitiesCheckRunner(occ);
			}
		} else if (check instanceof SosLatestObservationCheck) {
			return new SosLatestObservationCheckRunner((SosLatestObservationCheck) check);
		}
		return null;
	}

}
