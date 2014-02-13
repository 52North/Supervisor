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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.supervisor.api.CheckResult;
import org.n52.supervisor.db.ResultDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

/**
 *
 * @author Daniel Nüst
 *
 * TODO implement abstract resource for common methods
 *
 */
@Path("/api/v1/results")
@SessionScoped
public class Results {

    private static Logger log = LoggerFactory.getLogger(Results.class);

    private final ResultDatabase db;

    @Inject
    public Results(final ResultDatabase db) {
        this.db = db;

        log.info("NEW {}", this);
    }

    private List<CheckResult> filterResultsWithCheckId(final List<CheckResult> results, final String id) {
        final ArrayList<CheckResult> filtered = new ArrayList<>(results.size());

        for (final CheckResult cr : results) {
            if (cr.getCheckIdentifier().equals(id)) {
				filtered.add(cr);
			}
        }

        return filtered;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckResults(@PathParam("id") final String id) throws JSONException {
    	log.debug("Requesting results with id '{}'",id);

    	final CheckResult checkResult = db.getResult(id);
    	if (!checkResult.equals(ResultDatabase.NO_CHECK_RESULT_WITH_GIVEN_ID)) {
    		return Response.ok().entity(checkResult).build();
    	}
    	return Response.status(Status.NOT_FOUND).entity("{\"error\": \"entitiy for id not found:" + id + "\" } ").build();
    }

    @PUT
    @Path("/clear")
    public Response clearResults() {
    	log.debug("Clearing the results database");
    	db.clearResults();
    	if (db.isEmpty()) {
    		return Response.status(Status.NO_CONTENT).build();
    	} else {
    		final String msg = "Results database could not be cleared.";
    		log.error(msg);
    		return Response.serverError().entity("{\"error\": \"" + msg + "\" } ").build();
    	}
    }

	@GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChecks(
    		@Context final UriInfo uriInfo,
    		@QueryParam("checkId") final String checkId,
    		@QueryParam("expanded") final boolean expanded) throws JSONException {
        List<CheckResult> results = db.getLatestResults();

        // GenericEntity<List<Check>> entity = new GenericEntity<List<Check>>(Lists.newArrayList(checks)) {};

        if (checkId != null) {
            results = filterResultsWithCheckId(results, checkId);
        }
        final JSONObject result = new JSONObject();
        final JSONArray jsonResults = new JSONArray();
        result.put("results", jsonResults);
        for (final CheckResult checkResult : results) {
        	final JSONObject jsonResult = new JSONObject();
        	jsonResult.put("id", checkResult.getIdentifier());
        	URI path = uriInfo.getBaseUriBuilder().path(Results.class).path(checkResult.getIdentifier()).build();
        	jsonResult.put("uri", path);
        	path = uriInfo.getBaseUriBuilder().path(Checks.class).path(checkResult.getCheckIdentifier()).build();
        	jsonResult.put("check", path);
        	if (expanded) {
        		jsonResult.put("checkIdentifier", checkResult.getCheckIdentifier());
        		jsonResult.put("checkTime", checkResult.getCheckTime());
        		jsonResult.put("result", checkResult.getResult());
        		jsonResult.put("type", checkResult.getType());
        	}
            jsonResults.put(jsonResult);
        }
        return Response.ok(result.toString()).build();

        // return Response.ok(entity).build();
    }

}
