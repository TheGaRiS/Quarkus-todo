package org.acme;



import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class Task {

    public int id;

    public String name;

    public String state = "IN_PROCESS";

    public Task() {
    }

    public Task(int id, String name, String state) {

        this.id = id;
        this.name = name;
        this.state = state;

    }

    private static Task from(Row row) {
        return new Task(row.getInteger("id"), row.getString("name"), row.getString("state"));
    }

    public static Multi<Task> findAll(PgPool client) {
        return client.query("SELECT id,name,state FROM task ORDER BY name").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Task::from);
    }

    public static Uni<RowSet<Row>> taskDo(PgPool client, int id) {

        return client.preparedQuery("UPDATE task SET state = 'DONE' WHERE id = $1").execute(Tuple.of(id));

    }

    public static Uni<Task> findById(PgPool client, int id) {
        return client.preparedQuery("SELECT id, name, state FROM task WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Integer> save(PgPool client) {
        return client.preparedQuery("INSERT INTO task (name,state) VALUES ($1,$2) RETURNING id").execute(Tuple.of(name,state))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getInteger("id"));
    }

    public static Uni<Boolean> delete(PgPool client, int id) {
        return client.preparedQuery("DELETE FROM task WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> update(PgPool client, int id) {
        return client.preparedQuery("UPDATE task SET state = 'DONE' WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

}
