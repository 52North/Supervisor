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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.DefaultValue;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.supervisor.CheckerResolver;
import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.db.CheckDatabase;
import org.n52.supervisor.db.ResultDatabase;
import org.n52.supervisor.tasks.ManualChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

/**
 *
 * @author Daniel Nüst
 *
 */
@Path("/api/v1/checks")
@SessionScoped
public class Checks {

    private static Logger log = LoggerFactory.getLogger(Checks.class);

    private final ExecutorService manualExecutor;

    private final CheckDatabase checkDatabase;

    private final ResultDatabase resultDatabase;

    private final CheckerResolver checkResolver;

    @Inject
    public Checks(final CheckDatabase cd, final ResultDatabase rd, final CheckerResolver cr) {
        checkDatabase = cd;
        resultDatabase = rd;
        checkResolver = cr;

        // create thread pool for manually started checks
        manualExecutor = Executors.newSingleThreadExecutor();

        log.info("NEW {}", this);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheck(@PathParam("id") final String id) {
        log.debug("Requesting checker with id {}", id);

        final Check check = checkDatabase.getCheck(id);

        if (check != null) {
			return Response.ok().entity(check).build();
		}

        return Response.status(Status.NOT_FOUND).entity("{\"error\": \"entitiy for id not found:" + id + "\" } ").build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChecks(@Context final UriInfo uriInfo) throws JSONException {
        final List<Check> checks = checkDatabase.getAllChecks();

        // GenericEntity<List<Check>> entity = new GenericEntity<List<Check>>(Lists.newArrayList(checks)) {};
        final JSONObject result = new JSONObject();
        final JSONArray jsonChecks = new JSONArray();
        result.put("checks", jsonChecks);
        for (final Check check : checks) {
        	final JSONObject jsonCheck = new JSONObject();
        	jsonCheck.put("id", check.getIdentifier());
        	URI path = uriInfo.getBaseUriBuilder().path(Checks.class).path(check.getIdentifier()).build();
        	jsonCheck.put("uri", path);
        	path = uriInfo.getBaseUriBuilder().path(Checks.class).path(check.getIdentifier() + "/results").build();
        	jsonCheck.put("results",path);
            jsonChecks.put(jsonCheck);
        }
        return Response.ok(result).build();

        // return Response.ok(entity).build();
    }

    @GET
    @Path("/{id}/results")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChecksResults(
    		@Context 			final UriInfo uriInfo,
    		@PathParam("id") 	final String id) {
        final UriBuilder redirect = uriInfo.getBaseUriBuilder().path(Results.class).queryParam("checkId", id);
        return Response.seeOther(redirect.build()).build();
    }

    @PUT
    @Path("/run")
    @Produces(MediaType.TEXT_PLAIN)
    public Response runChecksNow(
    		@QueryParam("notify") @DefaultValue("true")		final boolean notify,
    		@QueryParam("all")    @DefaultValue("false")	final boolean runAll,
    		@QueryParam("ids")								final String ids,
    		@Context										final UriInfo uri) {
        log.debug("Running processes: notify = {}, all = {}, ids = {}", notify, runAll, ids);
        log.debug(uri.toString());

        if (runAll && ids != null) {
			log.warn("Both ids and 'all' specified.");
		}

        ManualChecker manualChecker = null;
        if (runAll) {
            final Collection<CheckRunner> checkers = new ArrayList<>();
            final List<Check> allChecks = checkDatabase.getAllChecks();
            for (final Check check : allChecks) {
                final CheckRunner runner = checkResolver.getRunner(check);
                runner.setResultDatabase(resultDatabase);

                if (runner != null) {
					checkers.add(runner);
				}
            }

            manualChecker = new ManualChecker(checkers, notify);
            manualExecutor.submit(manualChecker);

            return Response.ok(Response.Status.CREATED).entity("running all processes").build();
        }

        return Response.serverError().entity("not implemented yet").build();
    }

}
