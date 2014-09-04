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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.n52.supervisor.api.CheckResult;
import org.n52.supervisor.checks.util.DebugCheckResult;
import org.n52.supervisor.db.ResultDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Names;

@RunWith(ResultDatabaseTest.LocalRunner.class)
public class ResultDatabaseTest {

	protected static final int MAX = 3;
	
    private ResultDatabase db;

	@Inject
    public void setDatabase(ResultDatabase d) {
        this.db = d;
    }
	
	@Test
	public void testMaximumSize() {
		List<DebugCheckResult> candidates = new ArrayList<>();
		for (int i = 0; i < MAX + 1; i++) {
			candidates.add(new DebugCheckResult(Integer.toString(i)));			
		}

		for (DebugCheckResult debugCheckResult : candidates) {
			db.appendResult(debugCheckResult);
		}
		
		List<CheckResult> results = db.getLatestResults();
		
		Assert.assertThat(results.size(), is(MAX));
		
		Assert.assertEquals(results.get(0), candidates.get(1));
		Assert.assertEquals(results.get(results.size() - 1), candidates.get(candidates.size() - 1));
		
	}
	
	public static class LocalRunner extends AbstractGuiceJunitRunner {
		
		public LocalRunner(Class<?> c) throws InitializationError {
			super(c);
		}

		protected List<Module> getModules(Class<?> clazz)
				throws InitializationError {
			List<Module> modules = new ArrayList<>(1);
			modules.add(new AbstractModule() {
				@Override
				protected void configure() {
					bindConstant().annotatedWith(Names.named("supervisor.checks.maxStoredResults")).to(MAX);
				}
			});
			return modules;
		}
		
	}
	
}
