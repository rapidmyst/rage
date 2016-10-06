/*
 * MindmapsDB - A Distributed Semantic Database
 * Copyright (C) 2016  Mindmaps Research Ltd
 *
 * MindmapsDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MindmapsDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MindmapsDB. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

import io.mindmaps.MindmapsGraph;
import io.mindmaps.engine.loader.BlockingLoader;
import io.mindmaps.engine.loader.Loader;
import io.mindmaps.graql.Graql;
import io.mindmaps.migration.sql.SQLDataMigrator;
import io.mindmaps.migration.sql.SQLSchemaMigrator;

import java.sql.Connection;

import static io.mindmaps.graql.Graql.count;
import static io.mindmaps.graql.Graql.var;

public class SQLWorldMigrator {

    /**
     *
     * @param connection jdbc connection to the SQL databse
     * @param graph graph to migrate data into
     */
    public static void migrateWorld(Connection connection, MindmapsGraph graph){

        try (SQLSchemaMigrator schemaMigrator = new SQLSchemaMigrator();
               SQLDataMigrator dataMigrator = new SQLDataMigrator()) {

            Loader loader = new BlockingLoader(graph.getKeyspace());

            schemaMigrator
                    .configure(connection)
                    .migrate(loader);

            System.out.println("Schema migration successful");

            dataMigrator
                .configure(connection)
                .migrate(loader);

            System.out.println("Data migration successful");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints information about the migrated database
     */
    public static void printInformationAboutWorld(MindmapsGraph graph){
        graph.rollback();

        // What are the types that were migrated?
        System.out.println("Migrated Types:");
        graph.getMetaEntityType().instances().forEach(System.out::println);

        // How many countries are in the world?
        Long numberCountries = Graql.withGraph(graph).match(var().isa("country")).aggregate(count()).execute();
        System.out.println("\n" + numberCountries + " countries in our world");

        // How many cities in the world?
        Long numberCities = Graql.withGraph(graph).match(var().isa("city")).aggregate(count()).execute();
        System.out.println("\n" + numberCities + " cities in our world" + "\n");

        // What are the cities in Niger?
        System.out.println("Cities in Niger:");
        Graql.withGraph(graph).match(
                var("country").isa("country").has("country-Name-resource", "Niger"),
                var("city").isa("city").has("city-Name-resource", var("niger-city")),
                var().rel("country").rel("city")).select("niger-city").distinct()
        .stream().map(i -> i.get("niger-city").asResource().getValue()).forEach(System.out::println);

        // What are the languages are spoken in Sweden?


    }
}
