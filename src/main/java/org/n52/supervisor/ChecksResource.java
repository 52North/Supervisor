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

import org.n52.supervisor.checks.Check;
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
public class ChecksResource {

    private static Logger log = LoggerFactory.getLogger(ChecksResource.class);

    private ExecutorService manualExecutor;

    private CheckDatabase cd;

    private ResultDatabase rd;

    private CheckerResolver cr;

    @Inject
    public ChecksResource(CheckDatabase cd, ResultDatabase rd, CheckerResolver cr) {
        this.cd = cd;
        this.rd = rd;
        this.cr = cr;

        // create thread pool for manually started checks
        this.manualExecutor = Executors.newSingleThreadExecutor();

        log.info("NEW {}", this);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheck(@PathParam("id")
    String id) {
        log.debug("Requesting checker with id {}", id);

        Check c = this.cd.getCheck(id);

        if (c != null)
            return Response.ok().entity(c).build();

        return Response.status(Status.NOT_FOUND).entity("{\"error\": \"entitiy for id not found:" + id + "\" } ").build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChecks(@Context
    UriInfo uriInfo) {
        List<Check> checks = this.cd.getAllChecks();

        // GenericEntity<List<Check>> entity = new GenericEntity<List<Check>>(Lists.newArrayList(checks)) {};
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"checks\": [");
        for (Check c : checks) {
            sb.append("{ \"id\": \"");
            sb.append(c.getIdentifier());
            sb.append("\", \"uri\": \"");
            URI path = uriInfo.getBaseUriBuilder().path(ChecksResource.class).path(c.getIdentifier()).build();
            sb.append(path);
            sb.append("\", ");
            sb.append("\"results\": \"");
            path = uriInfo.getBaseUriBuilder().path(ChecksResource.class).path(c.getIdentifier() + "/results").build();
            sb.append(path);
            sb.append("\" },");
        }
        sb.replace(sb.length() - 1, sb.length(), ""); // remove last comma
        sb.append("] }");
        return Response.ok(sb.toString()).build();

        // return Response.ok(entity).build();
    }

    @GET
    @Path("/{id}/results")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChecksResults(@Context
    UriInfo uriInfo, @PathParam("id")
    String id) {
        UriBuilder redirect = uriInfo.getBaseUriBuilder().path(ResultsResource.class).queryParam("checkId", id);
        return Response.seeOther(redirect.build()).build();
    }

    @PUT
    @Path("/run")
    @Produces(MediaType.TEXT_PLAIN)
    public Response runChecksNow(@QueryParam("notify")
    @DefaultValue("true")
    boolean notify, @QueryParam("all")
    @DefaultValue("false")
    boolean runAll, @QueryParam("ids")
    String ids, @Context
    UriInfo uri) {
        log.debug("Running processes: notify = {}, all = {}, ids = {}", notify, runAll, ids);
        log.debug(uri.toString());

        if (runAll && ids != null)
            log.warn("Both ids and 'all' specified.");

        ManualChecker c = null;
        if (runAll) {
            Collection<ICheckRunner> checkers = new ArrayList<>();
            List<Check> allChecks = this.cd.getAllChecks();
            for (Check check : allChecks) {
                ICheckRunner runner = this.cr.getRunner(check);
                runner.setResultDatabase(this.rd);

                if (runner != null)
                    checkers.add(runner);
            }

            c = new ManualChecker(checkers, notify);
            this.manualExecutor.submit(c);

            return Response.ok(Response.Status.CREATED).entity("running all processes").build();
        }

        return Response.serverError().entity("not implemented yet").build();
    }

}
