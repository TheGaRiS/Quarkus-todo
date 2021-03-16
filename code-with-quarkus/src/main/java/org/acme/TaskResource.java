package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/task")
public class TaskResource {

    @Inject
    io.vertx.mutiny.pgclient.PgPool client;

//    @Inject
//    @ConfigProperty(name = "myapp.schema.create", defaultValue = "true")
//    boolean schemaCreate;
//
//    @PostConstruct
//    void config() {
//        if (schemaCreate) {
//            initdb();
//        }
//    }
//
//    private void initdb() {
//        client.query("DROP TABLE IF EXISTS task").execute()
//                .flatMap(r -> client.query("CREATE TABLE task (id SERIAL PRIMARY KEY, name TEXT NOT NULL, state TEXT NOT NULL )").execute())
//                .flatMap(r -> client.query("INSERT INTO task (name,state) VALUES ('Vert.x','IN_PROCESS')").execute())
//                .flatMap(r -> client.query("INSERT INTO task (name,state) VALUES ('Quarkus','IN_PROCESS')").execute())
//                .flatMap(r -> client.query("INSERT INTO task (name,state) VALUES ('Spring','IN_PROCESS')").execute())
//                .await().indefinitely();
//    }
    @GET
    public Multi<Task> get() {
        return Task.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(@PathParam("id") int id) {
        return Task.findById(client, id)
                .onItem().transform(task -> task != null ? Response.ok(task) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") int id) {
        return Task.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @POST
    public Uni<Response> create(Task task) {
        return task.save(client)
                .onItem().transform(id -> URI.create("/task/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(@PathParam("id") int id) {

        return Task.update(client, id)
                .onItem().transform(updated -> updated ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());

    }
}
