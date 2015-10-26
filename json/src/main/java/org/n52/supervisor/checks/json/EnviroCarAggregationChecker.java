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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.map.ObjectMapper;
import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement
public class EnviroCarAggregationChecker extends BaseEnviroCarChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(EnviroCarAggregationChecker.class);
    
    private String apiTrackUrl;
    private String aggregationTrackUrl;
    
	public static void main(String[] args) {
		String u1 = "https://envirocar.org/api/stable/tracks?limit=250&page=0";
		String u2 = "http://ags.dev.52north.org:8080/point-aggregation/aggregatedTracks";

		EnviroCarAggregationChecker checker = new EnviroCarAggregationChecker(u1, u2, null);
		boolean result = checker.new Runner().check();
	}
    
    public EnviroCarAggregationChecker() {
        super(null);
    }
    
    
    public EnviroCarAggregationChecker(String apiTrackUrl, String aggregationTrackUrl, String email) {
        super(email);
        this.apiTrackUrl = apiTrackUrl;
        this.aggregationTrackUrl = aggregationTrackUrl;
    }
    
    public class Runner extends AbstractServiceCheckRunner {
        
        public Runner() {
            super(EnviroCarAggregationChecker.this);
        }
        
        @Override
        public boolean check() {
            try {
                Set<String> apiTrackSet = EnviroCarAggregationChecker.this.resolveApiTrackSet(EnviroCarAggregationChecker.this.apiTrackUrl);

                Map<?, ?> aggregationJson = EnviroCarAggregationChecker.this.executeGetAndParseJson(EnviroCarAggregationChecker.this.aggregationTrackUrl);
                Set<String> aggregationTrackSet = resolveAggregationSet(aggregationJson);
                
                for (String string : aggregationTrackSet) {
                    apiTrackSet.remove(string);
                }
                
                if (apiTrackSet.isEmpty()) {
                    addResult(createPositiveResult("No missing tracks in aggregation"));
                    return true;
                }
                else {
                    Set<String> missing = new HashSet<>();
                    for (String string : apiTrackSet) {
                        if (!aggregationHasTrack(string, EnviroCarAggregationChecker.this.aggregationTrackUrl)) {
                            missing.add(string);
                        }
                    }
                    
                    if (missing.isEmpty()) {
                        addResult(createPositiveResult("No missing tracks in aggregation"));
                        return true;
                    }
                    else {
                        addResult(createNegativeResult(createMissingString(missing)));
                    }
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
        
        
        protected boolean aggregationHasTrack(String trackId, String aggregationTrackUrl) throws IOException {
            HttpClient c;
            try {
                c = EnviroCarAggregationChecker.this.createClient();
            } catch (Exception e) {
                throw new IOException(e);
            }
            HttpResponse resp = c.execute(new HttpGet(aggregationTrackUrl.concat("/").concat(trackId)));
            
            if (resp.getStatusLine() != null &&
                    resp.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {
                ObjectMapper om = new ObjectMapper();
                Map<?, ?> result = om.readValue(resp.getEntity().getContent(), Map.class);
                return Boolean.valueOf(result.get("aggregated").toString());
            }
            
            return false;
        }
        
        
    }
    
    
    
}
