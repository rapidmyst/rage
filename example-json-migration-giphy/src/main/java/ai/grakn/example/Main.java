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
import ai.grakn.client.Client;
import ai.grakn.exception.GraknValidationException;
import ai.grakn.migration.base.Migrator;
import ai.grakn.migration.json.JsonMigrator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;

public class Main {

    private static final String SERVER_ADDRESS = "127.0.0.1:4567";
    private static final String KEYSPACE = "giphy";
    private static final String DATA_DIR = "trending";
    private static final String TEMPLATE = "template.gql";
    private static final String ONTOLOGY = "ontology.gql";

    public static void main(String[] args){
        if(!Client.serverIsRunning(SERVER_ADDRESS)){
            System.out.println("Please start Mindmaps Engine");
            System.out.println("You can get more information on how to do so using our setup guide: https://mindmaps.io/pages/documentation/get-started/setup-guide.html");
            return;
        } else {
            System.out.println("=================================================================================================");
            System.out.println("|||||||||||||||||||||||||||||||   Mindmaps JSON Migration Example   |||||||||||||||||||||||||||||");
            System.out.println("=================================================================================================");
        }


        try {
            GraknGraph graph = Grakn.factory(Grakn.DEFAULT_URI, KEYSPACE).getGraph();

            // load your ontology
            loadOntology(graph);

            // get resources
            String template = getResourceAsString(TEMPLATE);
            File jsonData = new File(getResource(DATA_DIR).toString());

            System.out.println("Beginning migration");

            // create a migrator with your macro
            Migrator migrator = new JsonMigrator(template, jsonData)
                    .registerMacro(new GiphyMacro());

            // load data in directory
            migrator.load(SERVER_ADDRESS, KEYSPACE);

            graph.commit();
            graph.close();

            migrator.close();

            System.out.println("Migration complete");
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        System.exit(0);
    }

    public static void loadOntology(GraknGraph graph) throws IOException, GraknValidationException {
        String ontology = getResourceAsString(ONTOLOGY);
        graph.graql().parse(ontology).execute();
        graph.commit();
    }

    public static Path getResource(String resourceName){
        return Paths.get(Main.class.getClassLoader().getResource(resourceName).getPath());
    }

    public static String getResourceAsString(String resourceName) throws IOException {
        return Files.readAllLines(getResource(resourceName)).stream().collect(joining("\n"));
    }
}
