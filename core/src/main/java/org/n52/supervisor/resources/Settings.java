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
package org.n52.supervisor.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Path("/api/v1/settings")
@Singleton
public class Settings {

    private static final String MAX_RESULTS_PATH = "maxResults";

    private static final String PAGE_REFRESH_PATH = "pageRefreshSecs";

    private static Logger log = LoggerFactory.getLogger(Settings.class);

    private String baseUrl;

    private final long pageRefreshSecs;

    private final long maxResults;

    @Inject
    public Settings(@Named("supervisor.ui.html.pageRefreshSecs") final
    long pageRefreshSecs, @Named("supervisor.ui.html.maxChecks") final
    long maxResults) {
        this.pageRefreshSecs = pageRefreshSecs;
        this.maxResults = maxResults;

        log.debug("NEW {}", this);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatisticsIndex(@Context final
    UriInfo uriInfo) throws IllegalArgumentException, UriBuilderException, JSONException {

    	final JSONObject result = new JSONObject();
    	result.put(MAX_RESULTS_PATH, uriInfo.getBaseUriBuilder().path(Settings.class).path(MAX_RESULTS_PATH).build());
        result.put(PAGE_REFRESH_PATH, uriInfo.getBaseUriBuilder().path(Settings.class).path(PAGE_REFRESH_PATH).build());

        return Response.ok(result.toString()).build();
    }

    @GET
    @Path("/{setting}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response settingJson(@PathParam("setting") final String setting) throws JSONException {
    	final JSONObject result = new JSONObject();
        switch (setting) {
        case MAX_RESULTS_PATH:
        	result.put(MAX_RESULTS_PATH, maxResults);
            break;
        case PAGE_REFRESH_PATH:
        	result.put(PAGE_REFRESH_PATH,pageRefreshSecs);
            break;
        default:
            return Response.status(Status.BAD_REQUEST).entity("\"settings parameter not supported\"").build();
        }
        return Response.ok(result.toString()).build();
    }

    @GET
    @Path("/{setting}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response settingText(@PathParam("setting") final String setting) {
        switch (setting) {
        case MAX_RESULTS_PATH:
            return Response.ok(Long.toString(maxResults)).build();
        case PAGE_REFRESH_PATH:
            return Response.ok(Long.toString(pageRefreshSecs)).build();
        default:
            return Response.status(Status.BAD_REQUEST).entity("settings parameter not supported").build();
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Settings [baseUrl=");
        builder.append(baseUrl);
        builder.append(", pageRefreshSecs=");
        builder.append(pageRefreshSecs);
        builder.append(", maxResults=");
        builder.append(maxResults);
        builder.append("]");
        return builder.toString();
    }

}
