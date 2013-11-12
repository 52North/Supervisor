
package org.n52.supervisor.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.n52.supervisor.Supervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public ChecksResource() {
        log.info("NEW {}", this);

        // create thread pool for manually started checks
        this.manualExecutor = Executors.newSingleThreadExecutor();
    }

    @PUT
    @Path("/run")
    @Produces(MediaType.TEXT_PLAIN)
    public Response runChecksNow(@QueryParam("notify")
    @DefaultValue("false")
    boolean notify, @QueryParam("all")
    @DefaultValue("false")
    boolean runAll, @QueryParam("ids")
    String ids, @Context UriInfo uri) {
        log.debug("Running processes: notify = {}, all = {}, ids = {}", notify, runAll, ids);
        log.debug(uri.toString());
        
        if (runAll && ids != null)
            log.warn("Both ids and 'all' specified.");

        ManualChecker c = null;
        if (runAll) {
            c = new ManualChecker(Supervisor.getCheckers(), notify);
            this.manualExecutor.submit(c);

            return Response.ok().entity("running all processes").build();
        }

        return Response.serverError().entity("not implemented yet").build();
    }

}
