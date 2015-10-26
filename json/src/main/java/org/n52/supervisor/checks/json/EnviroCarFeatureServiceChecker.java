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
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.n52.supervisor.checks.AbstractServiceCheckRunner;
import org.n52.supervisor.checks.ServiceCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement
public class EnviroCarFeatureServiceChecker extends BaseEnviroCarChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(EnviroCarFeatureServiceChecker.class);
    
    private String apiTrackUrl;
    private String featureServiceUrl;
    private String user;
    private String password;
    
    public static void main(String[] args) throws IOException {
        String u1 = "https://envirocar.org/api/stable/tracks?limit=250&page=0";
        String u2 = "http://ags.52north.org:6080/arcgis/rest/services/enviroCar/enviroCarTracksDistinctIDs/GPServer/Frequency/execute?frequency_fields=&summary_fields=&env:outSR=&env:processSR=&returnZ=false&returnM=false&f=pjson&token=%s";
        
        EnviroCarFeatureServiceChecker checker = new EnviroCarFeatureServiceChecker(u1, u2, null, "", "");
        boolean result = checker.new Runner().check();
    }
    private HashSet<Object> featureServiceTracks;
    
    public EnviroCarFeatureServiceChecker() {
        super(null);
    }
    
    
    public EnviroCarFeatureServiceChecker(String apiTrackUrl, String featureServiceUrl, String email, String user, String password) {
        super(email);
        this.apiTrackUrl = apiTrackUrl;
        this.featureServiceUrl = featureServiceUrl;
        this.user = user;
        this.password = password;
        
    }
    public class Runner extends AbstractServiceCheckRunner {
        
        public Runner() {
            super(EnviroCarFeatureServiceChecker.this);
        }
        
        @Override
        public boolean check() {
            try {
                Set<String> apiTrackSet = EnviroCarFeatureServiceChecker.this.resolveApiTrackSet(EnviroCarFeatureServiceChecker.this.apiTrackUrl);
                
                resolveFeatureServiceTracks();
                
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
                addResult(new ServiceCheckResult(ID_GENERATOR.generate(), e, check, e.getMessage()));
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
            return featureServiceTracks.contains(trackId);
        }
    
    }
    
    private void resolveFeatureServiceTracks() throws IOException {
        String ags = this.featureServiceUrl.substring(0, this.featureServiceUrl.indexOf("/arcgis"));
        String tokenURL = String.format("%s/arcgis/tokens?request=gettoken&username=%s&password=%s",
                ags, this.user, this.password);
        
        HttpClient c;
        try {
            c = this.createClient();
        } catch (Exception e) {
            throw new IOException(e);
        }
        HttpResponse resp = c.execute(new HttpGet(tokenURL));
        
        if (resp.getStatusLine() != null &&
                resp.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {
            String token = EntityUtils.toString(resp.getEntity());
            
            if (token != null) {
                String processUrl = String.format(this.featureServiceUrl, token);
                HttpResponse processResult = c.execute(new HttpGet(processUrl));
                
                if (processResult.getStatusLine() != null &&
                        processResult.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {

                    parseProcessResponse(processResult.getEntity().getContent());
                }
            }
        }
    }

    private void parseProcessResponse(InputStream content) throws IOException {
        featureServiceTracks = new HashSet<>();
        
        ObjectMapper om = new ObjectMapper();
        Map<?, ?> result = om.readValue(content, Map.class);
        List<?> results = (List<?>) result.get("results");
        if (results != null && !results.isEmpty()) {
            Map<?, ?> entry = (Map<?, ?>) results.get(0);
            if (entry != null && !entry.isEmpty()) {
                Map<?, ?> value = (Map<?, ?>) entry.get("value");
                List<Map<?, ?>> features = (List<Map<?, ?>>) value.get("features");
                
                for (Map<?, ?> feature : features) {
                    Map<?, ?> atts = (Map<?, ?>) feature.get("attributes");
                    this.featureServiceTracks.add(atts.get("trackid"));
                }
            }
        }
    }
    
}
