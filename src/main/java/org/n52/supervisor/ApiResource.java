package org.n52.supervisor;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.google.inject.servlet.SessionScoped;

@Path("/api")
@SessionScoped
public class ApiResource {

    @GET
    @Path("/")
    public Response forwardToCurrentVersion(@Context
    UriInfo uriInfo) {
        UriBuilder redirect = uriInfo.getBaseUriBuilder().path(ApiResource.class).path("/v1");
        return Response.seeOther(redirect.build()).build();
    }

    @GET
    @Path("/v1")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiEndpoints(@Context
    UriInfo uriInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"resources\": [");
        sb.append("{ \"checks\": \"");
        URI path = uriInfo.getBaseUriBuilder().path(ChecksResource.class).build();
        sb.append(path);
        sb.append("\", ");
        sb.append("\"results\": \"");
        path = uriInfo.getBaseUriBuilder().path(ResultsResource.class).build();
        sb.append(path);
        sb.append("\" }");
        sb.append("] }");

        return Response.ok(sb.toString()).build();
    }

}
