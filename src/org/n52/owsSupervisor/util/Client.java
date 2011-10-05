/*******************************************************************************
Copyright (C) 2010
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License serviceVersion 2 as published by the 
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

Authors: Jan Schulte, Daniel Nüst
 
 ******************************************************************************/

package org.n52.owsSupervisor.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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

    private static final String SYSTEM_PROPERTY_PROXY_HOST = "http.proxyHost";

    private static final String SYSTEM_PROPERTY_PROXY_PORT = "http.proxyPort";

    /**
     * @param request
     * @param requestUrl
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws HttpException
     */
    private XmlObject doSend(String requestUrl, String request, String requestMethod) throws UnsupportedEncodingException,
            IOException,
            HttpException {
        if (log.isDebugEnabled())
            log.debug("Sending request (first 100 characters): "
                    + request.substring(0, Math.min(request.length(), 100)));

        // create and set up HttpClient
        HttpClient httpClient = new HttpClient();
        String host = System.getProperty(SYSTEM_PROPERTY_PROXY_HOST);
        String port = System.getProperty(SYSTEM_PROPERTY_PROXY_PORT);
        if (host != null && host.length() > 0 && port != null && port.length() > 0) {
            int portNumber = Integer.parseInt(port);
            HostConfiguration hostConfig = new HostConfiguration();
            hostConfig.setProxy(host, portNumber);
            httpClient.setHostConfiguration(hostConfig);
        }

        HttpMethodBase method = null;
        if (requestMethod.equals(GET_METHOD)) {
            if (log.isDebugEnabled())
                log.debug("Client connecting via GET to " + requestUrl);
            GetMethod getMethod = new GetMethod(requestUrl);
            getMethod.setQueryString(request);
            method = getMethod;
        }
        else if (requestMethod.equals(POST_METHOD)) {
            if (log.isDebugEnabled())
                log.debug("Client connecting via POST to " + requestUrl);
            PostMethod postMethod = new PostMethod(requestUrl);

            postMethod.setRequestEntity(new StringRequestEntity(request,
                                                                SupervisorProperties.getInstance().getClientRequestContentType(),
                                                                SupervisorProperties.getInstance().getClientRequestEncoding()));
            method = postMethod;
        }
        else {
            throw new IllegalArgumentException("requestMethod not supported!");
        }

        httpClient.executeMethod(method);

        XmlObject response = null;
        try {
            response = XmlObject.Factory.parse(method.getResponseBodyAsStream());
        }
        catch (XmlException e) {
            log.error("Error parsing response: " + e.getMessage());
        }
        return response;
    }

    /**
     * 
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     * @throws HttpException
     * @throws IOException
     */
    public String sendGetRequest(String requestUrl, String request) throws UnsupportedEncodingException,
            HttpException,
            IOException {
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
            HttpException,
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