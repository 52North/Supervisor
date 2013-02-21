/**
 * ﻿Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.owsSupervisor.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.owsSupervisor.SupervisorProperties;

/**
 * 
 * A simple client to connect to a SOR. It sends the given request string to the url defined
 * 
 * @author Jan Schulte, Daniel Nüst
 * 
 */
public class Client {

    private static final String GET_METHOD = "GET";

    private static Logger log = Logger.getLogger(Client.class);

    private static final String POST_METHOD = "POST";

    /**
     * @param requestContent
     * @param requestUrl
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws HttpException
     */
    private XmlObject doSend(String requestUrl, String requestContent, String requestMethod) throws UnsupportedEncodingException,
            IOException {
        if (log.isDebugEnabled())
            log.debug("Sending request (first 100 characters): "
                    + requestContent.substring(0, Math.min(requestContent.length(), 100)));

        HttpClient httpClient = new DefaultHttpClient();

        HttpRequestBase request = null;
        if (requestMethod.equals(GET_METHOD)) {
            if (log.isDebugEnabled())
                log.debug("Client connecting via GET to " + requestUrl);

            HttpGet httpget = new HttpGet(requestUrl + requestContent);
            request = httpget;
        }
        else if (requestMethod.equals(POST_METHOD)) {
            if (log.isDebugEnabled())
                log.debug("Client connecting via POST to " + requestUrl);
            HttpPost httppost = new HttpPost(requestUrl);

            httppost.setEntity(new StringEntity(requestContent,
                                                SupervisorProperties.getInstance().getClientRequestContentType(),
                                                SupervisorProperties.getInstance().getClientRequestEncoding()));
            request = httppost;
        }
        else {
            throw new IllegalArgumentException("requestMethod not supported!");
        }

        XmlObject xmlResponse = null;
        try {
            HttpResponse response = httpClient.execute(request);
            xmlResponse = XmlObject.Factory.parse(response.getEntity().getContent());
        }
        catch (XmlException e) {
            log.error("Error parsing response: " + e.getMessage());
        }
        catch (Exception e) {
            log.error("Could not execute request.", e);
        }

        return xmlResponse;
    }

    /**
     * 
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     * @throws HttpException
     * @throws IOException
     */
    public String sendGetRequest(String requestUrl, String request) throws UnsupportedEncodingException, IOException {
        if (request.isEmpty()) {
            return "The request is empty!";
        }
        return xSendGetRequest(requestUrl, request).xmlText();
    }

    /**
     * 
     * @param requestUrl
     * @param request
     * @return
     * @throws IOException
     */
    public String sendPostRequest(String requestUrl, String request) throws IOException {
        if (request.isEmpty()) {
            return "The request is empty!";
        }
        XmlObject response = doSend(requestUrl, request, POST_METHOD);
        return response.toString();
    }

    /**
     * 
     * @param request
     * @param requestUrl
     * @return
     * @throws UnsupportedEncodingException
     * @throws HttpException
     * @throws IOException
     */
    public XmlObject xSendGetRequest(String requestUrl, String request) throws UnsupportedEncodingException,
            IOException {
        XmlObject response = doSend(requestUrl, request, GET_METHOD);
        return response;
    }

    /**
     * 
     * @param requestUrl
     * @param request
     * @return
     * @throws IOException
     */
    public XmlObject xSendPostRequest(String requestUrl, XmlObject request) throws IOException {
        XmlObject response = doSend(requestUrl, request.xmlText(), POST_METHOD);
        return response;
    }

}