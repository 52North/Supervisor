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
package org.n52.supervisor.resources;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.inject.servlet.SessionScoped;

/**
 * This is the API starting point providing links to all available resources.
 *
 * Current available resources are:
 * 	<ul><li>checks</li>
 *  <li>results</li></ul>
 *
 */
@Path("/api")
@SessionScoped
public class Api {

	protected static final String RESULTS = "results";
	protected static final String CHECKS = "checks";
	protected static final String RESOURCES = "resources";
	private static final String API_ROOT = "/v1";

	@GET
    @Path("/")
    public Response forwardToCurrentVersion(@Context final UriInfo uriInfo) {
        final UriBuilder redirect = uriInfo.getBaseUriBuilder().path(Api.class).path(API_ROOT);
        return Response.seeOther(redirect.build()).build();
    }

    @GET
    @Path(API_ROOT)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiEndpoints(@Context final UriInfo uriInfo) throws JSONException {
    	final JSONObject result = new JSONObject();
    	final JSONObject resources = new JSONObject();
    	result.put(RESOURCES, resources);
    	URI path = uriInfo.getBaseUriBuilder().path(Checks.class).build();
    	resources.put(CHECKS,path);
    	path = uriInfo.getBaseUriBuilder().path(Results.class).build();
    	resources.put(RESULTS,path);
        return Response.ok(result.toString()).build();
    }

}
