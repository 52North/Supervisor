
package org.n52.supervisor.tasks;

import java.net.URI;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.n52.supervisor.ICheckResult;
import org.n52.supervisor.IServiceChecker;
import org.n52.supervisor.SettingsResource;
import org.n52.supervisor.Supervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
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

    private Supervisor supervisor;

    @Inject
    public ChecksResource(Supervisor supervisor) {
        this.supervisor = supervisor;

        // create thread pool for manually started checks
        this.manualExecutor = Executors.newSingleThreadExecutor();

        log.info("NEW {}", this);
    }

    @GET
    @Path("/results/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckResults(@PathParam("id")
    String id) {
        if (id != null)
            return Response.serverError().entity("not implemented yet").build();

        List<ICheckResult> latestResults = this.supervisor.getLatestResults();

        // http://stackoverflow.com/questions/6081546/jersey-can-produce-listt-but-cannot-response-oklistt-build
        GenericEntity<List<ICheckResult>> entity = new GenericEntity<List<ICheckResult>>(Lists.newArrayList(latestResults)) {};

        return Response.ok().entity(entity).build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckers(@Context
    UriInfo uriInfo) {
        List<IServiceChecker> checkers = (List<IServiceChecker>) this.supervisor.getCheckers();
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder(); // .path(SettingsResource.class).path(MAX_RESULTS_PATH).build()

        // GenericEntity<List<IServiceChecker>> entity = new
        // GenericEntity<List<IServiceChecker>>(Lists.newArrayList(checkers)) {};
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"checks\": [");
        for (IServiceChecker c : checkers) {
            sb.append("{ \"");
            sb.append(c.getIdentifier());
            sb.append("\": \"");
            URI path = baseUriBuilder.path(ChecksResource.class).path(c.getIdentifier()).build();
            sb.append(path);
            sb.append("\" },");
        }
        sb.replace(sb.length() - 1, sb.length(), ""); // remove last comma
        sb.append("] }");

        return Response.ok(sb.toString()).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChecker(@PathParam("id")
    String id) {
        log.debug("Requesting checker with id {}", id);

        List<IServiceChecker> checks = (List<IServiceChecker>) this.supervisor.getCheckers();
        for (IServiceChecker iServiceChecker : checks) {
            if(iServiceChecker.getIdentifier().equals(id)) {
                return Response.ok().entity(iServiceChecker).build();
            }
        }

        return Response.status(Status.BAD_REQUEST).entity("id not found").build();
    }

    @GET
    @Path("/{id}/results")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChecksResults() {
        return null;
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
            Collection<IServiceChecker> checkers = this.supervisor.getCheckers();
            c = new ManualChecker(checkers, notify);
            this.manualExecutor.submit(c);

            return Response.ok().entity("running all processes").build();
        }

        return Response.serverError().entity("not implemented yet").build();
    }

}
