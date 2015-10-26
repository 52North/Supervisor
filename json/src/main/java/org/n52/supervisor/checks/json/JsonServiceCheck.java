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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.n52.supervisor.checks.ServiceCheck;

public abstract class JsonServiceCheck extends ServiceCheck {
    
    protected Map<?, ?> executeGetAndParseJson(String url) throws IOException {
        HttpClient c;
        try {
            c = createClient();
        } catch (Exception e) {
            throw new IOException(e);
        }
        HttpResponse resp = c.execute(new HttpGet(url));
        
        return parseJsontoMap(resp);
    }
    
    protected Map<?, ?> parseJsontoMap(HttpResponse resp) throws IOException, IllegalStateException {
        if (resp.getStatusLine() != null &&
                resp.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {
            ObjectMapper om = new ObjectMapper();
            Map<?, ?> result = om.readValue(resp.getEntity().getContent(), Map.class);
            return result;
        }
        return null;
    }
    
    protected HttpClient createClient() throws Exception {
        DefaultHttpClient result = new DefaultHttpClient();
        SchemeRegistry sr = result.getConnectionManager().getSchemeRegistry();
        
        SSLSocketFactory sslsf = new SSLSocketFactory(new TrustStrategy() {
            
            @Override
            public boolean isTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
                return true;
            }
        }, new AllowAllHostnameVerifier());
        
        Scheme httpsScheme2 = new Scheme("https", 443, sslsf);
        sr.register(httpsScheme2);
        
        return result;
    }
    
}
