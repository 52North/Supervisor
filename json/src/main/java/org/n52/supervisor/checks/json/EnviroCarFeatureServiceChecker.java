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
public class EnviroCarFeatureServiceChecker extends BaseEnviroCarChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(EnviroCarFeatureServiceChecker.class);
    
    private String apiTrackUrl;
    private String featureServiceUrl;
    
    public static void main(String[] args) {
        String u1 = "https://envirocar.org/api/stable/tracks";
        String u2 = "http://ags.dev.52north.org:6080/arcgis/rest/services/enviroCar/enviroCarTracks/FeatureServer/0/query?geometryType=esriGeometryEnvelope&spatialRel=esriSpatialRelIntersects&returnGeometry=false&returnDistinctValues=false&returnIdsOnly=false&returnCountOnly=true&returnZ=false&returnM=false&f=pjson&where=%s";

        EnviroCarFeatureServiceChecker checker = new EnviroCarFeatureServiceChecker(u1, u2, null);
        boolean result = checker.new Runner().check();
    }
    
    public EnviroCarFeatureServiceChecker() {
        super(null);
    }
    
    
    public EnviroCarFeatureServiceChecker(String apiTrackUrl, String featureServiceUrl, String email) {
        super(email);
        this.apiTrackUrl = apiTrackUrl;
        this.featureServiceUrl = featureServiceUrl;
        
    }
    public class Runner extends AbstractServiceCheckRunner {
        
        public Runner() {
            super(EnviroCarFeatureServiceChecker.this);
        }
        
        @Override
        public boolean check() {
            try {
                Set<String> apiTrackSet = EnviroCarFeatureServiceChecker.this.resolveApiTrackSet(EnviroCarFeatureServiceChecker.this.apiTrackUrl);
                
                Set<String> missing = new HashSet<>();
                for (String string : apiTrackSet) {
                    if (!featureServiceHasTrack(string, EnviroCarFeatureServiceChecker.this.featureServiceUrl)) {
                        missing.add(string);
                    }
                }

                if (missing.isEmpty()) {
                    addResult(createPositiveResult("No missing tracks in feature service"));
                    return true;
                }
                else {
                    addResult(createNegativeResult(createMissingString(missing)));
                }
                
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
            
            return false;
        }
        
        private String createMissingString(Set<String> missing) {
            StringBuilder sb = new StringBuilder();
            sb.append("Missing track IDs in FeatureService: [");
            for (String string : missing) {
                sb.append(string);
                sb.append(", ");
            }
            sb.delete(sb.length()-2, sb.length());
            sb.append("]");
            return sb.toString();
        }
        
        protected boolean featureServiceHasTrack(String trackId, String serviceUrl) throws IOException {
            String where = "trackId+%3D+%27"+trackId+"%27";
            String target = String.format(serviceUrl, where);
            
            HttpClient c;
            try {
                c = EnviroCarFeatureServiceChecker.this.createClient();
            } catch (Exception e) {
                throw new IOException(e);
            }
            HttpResponse resp = c.execute(new HttpGet(target));
            
            if (resp.getStatusLine() != null &&
                    resp.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {
                ObjectMapper om = new ObjectMapper();
                Map<?, ?> result = om.readValue(resp.getEntity().getContent(), Map.class);
                int count = (Integer) result.get("count");
                return count > 0;
            }
            
            return false;
        }
        
        
    }
    
    
    
}
