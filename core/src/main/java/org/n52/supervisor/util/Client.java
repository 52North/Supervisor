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

package org.n52.supervisor.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.supervisor.SupervisorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * 
 * @author Daniel
 * 
 */
public class Client {

    private static final String GET_METHOD = "GET";

    private static Logger log = LoggerFactory.getLogger(Client.class);

    private static final String POST_METHOD = "POST";
    
    @Inject
    private SupervisorProperties properties;

    private synchronized XmlObject doSend(final String requestUrl, final String requestContent, final String requestMethod) throws UnsupportedEncodingException,
            IOException,
            IllegalStateException,
            XmlException {
    	log.debug("Sending request (first 100 characters): {}",
                    requestContent.substring(0, Math.min(requestContent.length(), 100)));

        final HttpClient httpClient = new DefaultHttpClient();

        HttpRequestBase request = null;
        if (requestMethod.equals(GET_METHOD)) {
        	log.debug("Client connecting via GET to {}",requestUrl);

            final StringBuilder fullUrl = new StringBuilder();
            fullUrl.append(requestUrl);
            if ( !requestUrl.endsWith("?")) {
				fullUrl.append("?");
			}
            fullUrl.append(requestContent);

            final HttpGet httpget = new HttpGet(fullUrl.toString());
            request = httpget;
        }
        else if (requestMethod.equals(POST_METHOD)) {
        	log.debug("Client connecting via POST to {}",requestUrl);
            final HttpPost httppost = new HttpPost(requestUrl);

            final StringEntity e = new StringEntity(requestContent, properties.getClientRequestEncoding());
            e.setContentType(properties.getClientRequestContentType());
            httppost.setEntity(e);
            request = httppost;
        } else {
			throw new IllegalArgumentException("requestMethod not supported!");
		}

        log.debug("Sending request: {}", request);

        XmlObject xmlResponse = null;
        final HttpResponse response = httpClient.execute(request);
        xmlResponse = XmlObject.Factory.parse(response.getEntity().getContent());

        log.debug("Got response: {}", xmlResponse);

        return xmlResponse;
    }

    public String sendGetRequest(final String requestUrl, final String request) throws UnsupportedEncodingException,
            IOException,
            IllegalStateException,
            XmlException {
        if (request.isEmpty()) {
            return "The request is empty!";
        }
        return xSendGetRequest(requestUrl, request).xmlText();
    }

    public String sendPostRequest(final String requestUrl, final String request) throws IOException,
            IllegalStateException,
            XmlException {
        if (request.isEmpty()) {
            return "The request is empty!";
        }
        final XmlObject response = doSend(requestUrl, request, POST_METHOD);
        return response.toString();
    }

    public XmlObject xSendGetRequest(final String requestUrl, final String request) throws UnsupportedEncodingException,
            IOException,
            IllegalStateException,
            XmlException {
        final XmlObject response = doSend(requestUrl, request, GET_METHOD);
        return response;
    }

    public XmlObject xSendPostRequest(final String requestUrl, final XmlObject request) throws IOException,
            IllegalStateException,
            XmlException {
        final XmlObject response = doSend(requestUrl, request.xmlText(), POST_METHOD);
        return response;
    }

}