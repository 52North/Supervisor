package org.n52.supervisor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.n52.supervisor.checks.CheckResult;
import org.n52.supervisor.db.ResultDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

/**
 * 
 * @author Daniel Nüst
 * 
 */
@Path("/api/v1/results")
@SessionScoped
public class ResultsResource {

    private static Logger log = LoggerFactory.getLogger(ResultsResource.class);

    private ResultDatabase db;

    @Inject
    public ResultsResource(ResultDatabase db) {
        this.db = db;

        log.info("NEW {}", this);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChecks(@Context
    UriInfo uriInfo, @QueryParam("checkId")
    String checkId) {
        List<CheckResult> results = this.db.getLatestResults();

        // GenericEntity<List<Check>> entity = new GenericEntity<List<Check>>(Lists.newArrayList(checks)) {};

        if (checkId != null) {
            results = filterResultsWithCheckId(results, checkId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{ \"results\": [");
        for (CheckResult r : results) {
            sb.append("{ \"check\": \"");
            URI path = uriInfo.getBaseUriBuilder().path(ChecksResource.class).path(r.getCheckIdentifier()).build();
            sb.append(path);
            sb.append("\" ,");
            sb.append("\"result\": \"");
            sb.append(r.getResult());
            sb.append("\", \"type\": \"");
            sb.append(r.getType());
            sb.append("\", \"checkTime\": \"");
            sb.append(r.getTimeOfCheck());
            sb.append("\" },");
        }
        sb.replace(sb.length() - 1, sb.length(), ""); // remove last comma
        sb.append("] }");
        return Response.ok(sb.toString()).build();

        // return Response.ok(entity).build();
    }

    private List<CheckResult> filterResultsWithCheckId(List<CheckResult> results, String id) {
        ArrayList<CheckResult> filtered = new ArrayList<>();

        for (CheckResult cr : results) {
            if (cr.getCheckIdentifier().equals(id))
                filtered.add(cr);
        }

        return filtered;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckResults(@PathParam("id")
    String id) {
        if (id != null)
            return Response.serverError().entity("not implemented yet").build();

        CheckResult latestResult = this.db.getResult(id);

        return Response.ok().entity(latestResult).build();
    }

}
