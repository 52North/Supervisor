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

package org.n52.supervisor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Path("/api/v1/settings")
@Singleton
public class SettingsResource {

    private static final String MAX_RESULTS_PATH = "maxResults";

    private static final String PAGE_REFRESH_PATH = "pageRefreshSecs";

    private static Logger log = LoggerFactory.getLogger(SettingsResource.class);

    private String baseUrl;

    private long pageRefreshSecs;

    private long maxResults;

    @Inject
    public SettingsResource(@Named("supervisor.ui.html.pageRefreshSecs")
    long pageRefreshSecs, @Named("supervisor.ui.html.maxChecks")
    long maxResults) {
        this.pageRefreshSecs = pageRefreshSecs;
        this.maxResults = maxResults;

        log.debug("NEW {}", this);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatisticsIndex(@Context
    UriInfo uriInfo) {

        StringBuilder sb = new StringBuilder();
        sb.append(" { ");
        sb.append("\"maxResults\" : \"");
        sb.append(uriInfo.getBaseUriBuilder().path(SettingsResource.class).path(MAX_RESULTS_PATH).build());
        sb.append("\"");
        sb.append(" , ");
        sb.append("\"pageRefresh\" : \"");
        sb.append(uriInfo.getBaseUriBuilder().path(SettingsResource.class).path(PAGE_REFRESH_PATH).build());
        sb.append("\"");
        sb.append(" } ");

        return Response.ok(sb.toString()).build();
    }

    @GET
    @Path("/{setting}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response settingJson(@PathParam("setting")
    String setting) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(" { ");
        switch (setting) {
        case MAX_RESULTS_PATH:
            sb.append("\"maxResults\" : \"");
            sb.append(this.maxResults);
            sb.append("\"");
            break;
        case PAGE_REFRESH_PATH:
            sb.append("\"pageRefreshSecs\" : \"");
            sb.append(this.pageRefreshSecs);
            sb.append("\"");
            break;
        default:
            return Response.status(Status.BAD_REQUEST).entity("\"settings parameter not supported\"").build();
        }
        sb.append(" } ");

        return Response.ok(sb.toString()).build();
    }

    @GET
    @Path("/{setting}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response settingText(@PathParam("setting")
    String setting) {
        switch (setting) {
        case MAX_RESULTS_PATH:
            return Response.ok(Long.toString(this.maxResults)).build();
        case PAGE_REFRESH_PATH:
            return Response.ok(Long.toString(this.pageRefreshSecs)).build();
        default:
            return Response.status(Status.BAD_REQUEST).entity("settings parameter not supported").build();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SettingsResource [baseUrl=");
        builder.append(baseUrl);
        builder.append(", pageRefreshSecs=");
        builder.append(pageRefreshSecs);
        builder.append(", maxResults=");
        builder.append(maxResults);
        builder.append("]");
        return builder.toString();
    }

}
