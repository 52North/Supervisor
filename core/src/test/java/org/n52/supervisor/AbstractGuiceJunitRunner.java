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
package org.n52.supervisor;

import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

public abstract class AbstractGuiceJunitRunner extends BlockJUnit4ClassRunner {

	private final Injector injector;

	public AbstractGuiceJunitRunner(Class<?> c) throws InitializationError {
		super(c);
		
		List<Module> modules = getModules(c);
		if (!modules.isEmpty()) {
			this.injector = Guice.createInjector(Stage.DEVELOPMENT, modules);
		}
		else {
			this.injector = null;
		}
	}

	@Override
	protected Object createTest() throws Exception {
		if (injector != null) {
			Object instance = injector.getInstance(getTestClass()
					.getJavaClass());
			List<TestRule> testRules = getTestRules(instance);
			for (TestRule rule : testRules) {
				injector.injectMembers(rule);
			}
			return instance;
		} else {
			return super.createTest();
		}
	}


	protected abstract List<Module> getModules(Class<?> clazz) throws InitializationError;

}