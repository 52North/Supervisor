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
package org.n52.supervisor.tasks.quartz;

import java.util.ArrayList;
import java.util.Collection;

import org.n52.supervisor.api.CheckResult;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.db.ResultDatabase;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzRunner implements Job {
	
	private static final Logger log = LoggerFactory.getLogger(QuartzRunner.class);

	private CheckRunner check;

	private ResultDatabase db;

	public QuartzRunner(CheckRunner r, ResultDatabase db) {
		this.check = r;
		this.db = db;
	}
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		boolean success = this.check.check();

		Collection<CheckResult> currentResults = new ArrayList<>(check.getResults());
		db.appendResults(currentResults);
		
        if (!success) {
        	this.check.notifyFailure();
        }
        else {
        	this.check.notifySuccess();
        }
        
        this.check.getAndClearResults();

        log.info("*** Ran check, got {} results.", currentResults.size());
        
	}

}
