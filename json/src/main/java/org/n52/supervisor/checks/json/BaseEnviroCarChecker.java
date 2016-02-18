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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/**
 *
 * @author matthes
 */
public abstract class BaseEnviroCarChecker extends JsonServiceCheck {
    
    
    private long interval;
    private String email;

    public BaseEnviroCarChecker(String email) {
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
    
    public long getInterval() {
        return interval;
    }
    
    protected Set<String> resolveApiTrackSet(String apiUrl) throws IOException {
        return resolveApiTrackSet(apiUrl, new HashSet<String>());
    }
    
    @SuppressWarnings("unchecked")
    protected Set<String> resolveApiTrackSet(String apiUrl, Set<String> resultSet) throws IOException {
        HttpClient c;
        try {
            c = createClient();
        } catch (Exception e) {
            throw new IOException(e);
        }
        HttpResponse resp = c.execute(new HttpGet(apiUrl));
        
        Map<?, ?> json = parseJsontoMap(resp);
        
        if (json == null || json.isEmpty()) {
            return resultSet;
        }
        
        List<Map<?, ?>> idList = (List<Map<?, ?>>) json.get("tracks");
        for (Map<?, ?> entry : idList) {
            resultSet.add((String) entry.get("id"));
        }
        
        /*
         * recursive call
        */
        Header[] headers = resp.getHeaders("Link");
        if (headers != null) {
            for (Header h : headers) {
                String a = h.getValue();
                if (h.getValue().contains("rel=next")) {
                    String nextUrl = a.substring(a.indexOf("<")+1, a.indexOf(">"));
                    resolveApiTrackSet(nextUrl, resultSet);
                }
            }
        }
        
        return resultSet;
    }
    
    
}
