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

package org.n52.supervisor.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.n52.supervisor.api.CheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 *
 * @author Daniel
 *
 */
@Singleton
public class ResultDatabase {

    public static final CheckResult NO_CHECK_RESULT_WITH_GIVEN_ID = new CheckResult() {};

	private Queue<CheckResult> latestResults;

    private static Logger log = LoggerFactory.getLogger(ResultDatabase.class);

    private final int maxStoredResults;

    @Inject
    public ResultDatabase(@Named("supervisor.checks.maxStoredResults") final
    int maxStoredResults) {
        this.maxStoredResults = maxStoredResults;
        latestResults = new LinkedBlockingQueue<CheckResult>(this.maxStoredResults);
        log.info("NEW {}", this);
    }

    public void appendResult(final CheckResult result) {
        latestResults.add(result);
    }

    public void appendResults(final Collection<CheckResult> results) {
        // if (latestResults.size() >= 100) { // FIXME make append non static so that config parameter can be
        // // used: this.maxStoredResults
        // log.debug("Too many results. Got " + results.size() + " new and " + latestResults.size() +
        // " existing.");
        // for (int i = 0; i < Math.min(results.size(), latestResults.size()); i++) {
        // // remove the first element so many times that the new results
        // // fit.
        // latestResults.remove();
        // }
        // }

        latestResults.addAll(results);
    }

    public void clearResults() {
        log.debug("Clearing all results: {}", Arrays.deepToString(latestResults.toArray()));
        latestResults.clear();
    }

    public void close() {
        latestResults.clear();
        latestResults = null;
    }

    public List<CheckResult> getLatestResults() {
        return new ArrayList<CheckResult>(latestResults);
    }

    public int getMaxStoredResults() {
        return maxStoredResults;
    }

    /**
     * @param id the identifier of a {@link CheckResult}.
     * @return The {@link CheckResult} with the given id or {@link ResultDatabase#NO_CHECK_RESULT_WITH_GIVEN_ID}.
     */
    public CheckResult getResult(final String id){
        for (final CheckResult checkResult : latestResults) {
			if (checkResult.getIdentifier().equals(id)) {
				return checkResult;
			}
		}
        return NO_CHECK_RESULT_WITH_GIVEN_ID;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ResultDatabase [maxStoredResults=");
        builder.append(maxStoredResults);
        builder.append("]");
        return builder.toString();
    }

	public boolean isEmpty() {
		return latestResults.isEmpty();
	}

}
