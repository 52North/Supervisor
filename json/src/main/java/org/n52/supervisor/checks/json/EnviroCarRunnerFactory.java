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
package org.n52.supervisor.checks.json;

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.checks.RunnerFactory;

public class EnviroCarRunnerFactory implements RunnerFactory {

	@Override
	public CheckRunner resolveRunner(Check check) {
		if (check instanceof EnviroCarAggregationChecker) {
                    EnviroCarAggregationChecker ecaChecker = (EnviroCarAggregationChecker) check;
                    return ecaChecker.new Runner();
		}
                else if (check instanceof EnviroCarFeatureServiceChecker) {
                    EnviroCarFeatureServiceChecker ecfsCheck = (EnviroCarFeatureServiceChecker) check;
                    return ecfsCheck.new Runner();
                }
		return null;
	}

}
