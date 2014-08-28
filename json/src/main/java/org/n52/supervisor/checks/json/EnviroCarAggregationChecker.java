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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement
public class EnviroCarAggregationChecker extends JsonServiceCheck {
	
	private static final Logger logger = LoggerFactory.getLogger(EnviroCarAggregationChecker.class);
	
	private String apiTrackUrl;
	private String aggregationTrackUrl;

	private long interval;

	private String email;

//	public static void main(String[] args) {
//		String u1 = "https://envirocar.org/api/stable/tracks";
//		String u2 = "http://ags.dev.52north.org:8080/point-aggregation/aggregatedTracks";
//		
//		EnviroCarAggregationChecker checker = new EnviroCarAggregationChecker(u1, u2);
//		boolean result = new Runner(checker).check();
//	}
	
	public EnviroCarAggregationChecker() {
	}
	

	public EnviroCarAggregationChecker(String apiTrackUrl, String aggregationTrackUrl, String email) {
		this.apiTrackUrl = apiTrackUrl;
		this.aggregationTrackUrl = aggregationTrackUrl;
		this.email = email;
	}
	
	@Override
	public String getNotificationEmail() {
		return email;
	}
	
	@Override
	public long getIntervalSeconds() {
		return interval;
	}
	
	public String getApiTrackUrl() {
		return apiTrackUrl;
	}

	public String getAggregationTrackUrl() {
		return aggregationTrackUrl;
	}

	public long getInterval() {
		return interval;
	}

	public static class Runner extends AbstractServiceCheckRunner {

		private EnviroCarAggregationChecker ecCheck;

		public Runner(EnviroCarAggregationChecker sc) {
			super(sc);
			this.ecCheck = sc;
		}
		
		@Override
		public boolean check() {
			try {
				Map<?, ?> trackJson = this.ecCheck.executeGetAndParseJson(ecCheck.apiTrackUrl);
				Map<?, ?> aggregationJson = this.ecCheck.executeGetAndParseJson(ecCheck.aggregationTrackUrl);
				
				Set<String> apiTrackSet = resolveTrackSet(trackJson);
				Set<String> aggregationTrackSet = resolveAggregationSet(aggregationJson);
				
				for (String string : aggregationTrackSet) {
					apiTrackSet.remove(string);
				}
				
				if (apiTrackSet.isEmpty()) {
					addResult(createPositiveResult("No missing tracks in aggregation"));
					return true;
				}
				else {
					addResult(createNegativeResult(createMissingString(apiTrackSet)));
				}
				
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
			
			return false;
		}

		private String createMissingString(Set<String> missing) {
			StringBuilder sb = new StringBuilder();
			sb.append("Missing track IDs: [");
			for (String string : missing) {
				sb.append(string);
				sb.append(", ");
			}
			sb.delete(sb.length()-2, sb.length());
			sb.append("]");
			return sb.toString();
		}

		@SuppressWarnings("unchecked")
		private Set<String> resolveAggregationSet(Map<?, ?> aggregationJson) {
			Set<String> result = new HashSet<>();
			
			List<Map<?, ?>> idList = (List<Map<?, ?>>) aggregationJson.get("tracks");
			
			for (Map<?, ?> entry : idList) {
				for (Object k : entry.keySet()) {
					result.add((String) k);
				}
			}
			
			return result;
		}

		@SuppressWarnings("unchecked")
		private Set<String> resolveTrackSet(Map<?, ?> trackJson) {
			Set<String> result = new HashSet<>();
			
			List<Map<?, ?>> idList = (List<Map<?, ?>>) trackJson.get("tracks");
			
			for (Map<?, ?> entry : idList) {
				result.add((String) entry.get("id"));
			}
			
			return result;
		}

		
	}

}
