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

import java.io.IOException;
import java.util.Map;

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.n52.supervisor.checks.RunnerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class SOECacheMetadataChecker extends JsonServiceCheck {
	
	private static final Logger logger = LoggerFactory.getLogger(SOECacheMetadataChecker.class);
	private String soeUrl;

	public SOECacheMetadataChecker(String soeUrl) {
		this.soeUrl = soeUrl;
	}
	
	@Override
	public long getIntervalSeconds() {
		return 900;
	}

	public static class Runner extends AbstractServiceCheckRunner {

		private SOECacheMetadataChecker checker;

		public Runner(SOECacheMetadataChecker r) {
			super(r);
			this.checker = r;
		}
		
		@Override
		public boolean check() {
			try {
				Map<?, ?> json = checker.executeGetAndParseJson(this.checker.soeUrl);
				Map<?, ?> ooc = (Map<?, ?>) json.get("ObservationOfferingCache");
				String lastUpdated = ooc.get("lastUpdated").toString();
				addResult(createPositiveResult("OOC last update: ".concat(lastUpdated)));
				return true;
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
				addResult(createNegativeResult("IOException: "+e.getMessage()));
			}
			return false;
		}
		
	}
	
	public static class Factory implements RunnerFactory {

		@Override
		public CheckRunner resolveRunner(Check check) {
			if (!(check instanceof SOECacheMetadataChecker)) {
				return null;
			}
			
			return new Runner((SOECacheMetadataChecker) check);
		}
		
	}
	
	public static class Module extends AbstractModule {
		
		@Override
		protected void configure() {
			Multibinder<RunnerFactory> binder = Multibinder.newSetBinder(binder(),
					RunnerFactory.class);
			binder.addBinding().to(Factory.class);
		}
	}
}
