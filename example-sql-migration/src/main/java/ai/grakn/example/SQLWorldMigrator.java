/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016  Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License:
 * along with Grakn. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package ai.grakn.example;

import ai.grakn.Grakn;
import ai.grakn.GraknGraph;
import ai.grakn.exception.GraknValidationException;
import ai.grakn.graql.Graql;
import ai.grakn.migration.sql.SQLMigrator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;

import static ai.grakn.example.Main.SERVER_ADDRESS;
import static ai.grakn.graql.Graql.var;
import static java.util.stream.Collectors.joining;

public class SQLWorldMigrator {

      /**
     * Migrate data using SQL statements
     * @param connection jdbc connection to the SQL databse
     * @param keyspace graph to migrate data into
     */
    public static void migrateWorld(Connection connection, String keyspace){
        load("ontology.gql", keyspace);

        migrate(connection, keyspace, "continents");
        migrate(connection, keyspace, "regions");
        migrate(connection, keyspace, "countries");
        migrate(connection, keyspace, "districts");
        migrate(connection, keyspace, "cities");
        migrate(connection, keyspace, "capitals");
        migrate(connection, keyspace, "languages");
        migrate(connection, keyspace, "languagesspoken");
    }

    private static void migrate(Connection connection, String keyspace, String toMigrateDir){
        String query = get(toMigrateDir + "/query.sql");
        String template = get(toMigrateDir + "/template.gql");

        System.out.println("Migrating " + toMigrateDir);
        try(SQLMigrator migrator = new SQLMigrator(query, template, connection)){
            migrator.load(SERVER_ADDRESS, keyspace);
        }
    }

    /**
     * Prints information about the migrated database
     */
    public static void printInformationAboutWorld(String keyspace){
        GraknGraph graph = Grakn.factory(Grakn.DEFAULT_URI, keyspace).getGraph();

        // What are the types that were migrated?
        System.out.println("Migrated Types:");
        graph.admin().getMetaEntityType().instances().forEach(System.out::println);

        // How many countries are in the world?
        Long numberCountries = graph.graql().match(var("x").isa("country")).distinct().aggregate(Graql.count()).execute();
        System.out.println("\n" + numberCountries + " countries in our world");

        // How many cities in the world?
        Long numberCities = graph.graql().match(var("x").isa("city")).distinct().aggregate(Graql.count()).execute();
        System.out.println("\n" + numberCities + " cities in our world" + "\n");

        // What are the cities in Niger?
        System.out.println("Cities in Niger:");
        graph.graql().match(
                var("country").isa("country").has("name", "Niger"),
                var("city").isa("city").has("name", var("name")),
                var().rel("country").rel("city")).select("name").distinct()
                .stream().map(i -> i.get("name").asResource().getValue()).forEach(System.out::println);
    }

    private static void load(String ontologyFile, String keyspace){
        String ontology = get(ontologyFile);

        System.out.println("Loading " + ontologyFile);
        GraknGraph graph = Grakn.factory(Grakn.DEFAULT_URI, keyspace).getGraph();
        graph.graql().parse(ontology).execute();
        try {
            graph.commit();
        } catch (GraknValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private static String get(String resourceName) {
        try {
            return Files.readAllLines(getResource(resourceName)).stream().collect(joining("\n"));
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static Path getResource(String resourceName){
        return Paths.get(SQLWorldMigrator.class.getClassLoader().getResource(resourceName).getPath());
    }
}
